#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${1:-${GATEWAY_URL:-http://localhost:8080}}"
BASE_URL="${GATEWAY_URL%/}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@swissotel.com}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin123!}"
ASYNC_TIMEOUT_SECONDS="${ASYNC_TIMEOUT_SECONDS:-20}"

command -v curl >/dev/null 2>&1 || { echo "curl is required" >&2; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "python3 is required" >&2; exit 1; }

assert_no_raw_ids() {
  python3 - "$1" <<'PY'
import json, re, sys
uuid_re = re.compile(r"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
data = json.loads(sys.argv[1]) if sys.argv[1].strip() else None

def walk(node, path="$"):
    if isinstance(node, dict):
        for k, v in node.items():
            p = f"{path}.{k}"
            if k == "id":
                raise SystemExit(f"PRIVATE ID LEAK: forbidden field {p}={v!r}")
            if (k.endswith("Id") or k.endswith("_id")) and v is not None:
                if not isinstance(v, str) or not uuid_re.match(v):
                    raise SystemExit(f"PRIVATE ID LEAK: expected UUID for {p}, got {v!r}")
            walk(v, p)
    elif isinstance(node, list):
        for idx, item in enumerate(node):
            walk(item, f"{path}[{idx}]")

walk(data)
PY
}

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local token="${4:-}"
  local tmp_body tmp_headers status
  tmp_body="$(mktemp)"
  tmp_headers="$(mktemp)"

  local args=(-sS -D "$tmp_headers" -o "$tmp_body" -w "%{http_code}" -X "$method" "${BASE_URL}${path}")
  if [[ -n "$token" ]]; then
    args+=(-H "Authorization: Bearer ${token}")
  fi
  if [[ -n "$body" ]]; then
    args+=(-H "Content-Type: application/json" -d "$body")
  fi

  status="$(curl "${args[@]}")"
  RESP_STATUS="$status"
  RESP_BODY="$(cat "$tmp_body")"
  RESP_HEADERS="$(cat "$tmp_headers")"
  rm -f "$tmp_body" "$tmp_headers"
}

step() {
  echo "[E2E] $1"
}

echo "Running Swissotel 360 E2E smoke suite against ${BASE_URL}"

step "1/15 Checking Gateway /actuator/health"
request GET "/actuator/health"
[[ "$RESP_STATUS" == "200" ]] || { echo "Expected 200, got $RESP_STATUS: $RESP_BODY" >&2; exit 1; }
python3 -c 'import json,sys; d=json.loads(sys.argv[1]); assert d["status"]=="UP"' "$RESP_BODY"

step "2/15 Verifying /internal/** is blocked at Gateway"
request GET "/internal/users/1"
[[ "$RESP_STATUS" == "403" ]] || { echo "Expected 403, got $RESP_STATUS: $RESP_BODY" >&2; exit 1; }
python3 -c 'import json,sys; d=json.loads(sys.argv[1]); assert d["code"]=="FORBIDDEN"' "$RESP_BODY"

step "3/15 Verifying aggregated OpenAPI swagger-config and 6 service docs"
request GET "/v3/api-docs/swagger-config"
[[ "$RESP_STATUS" == "200" ]] || { echo "swagger-config failed: $RESP_STATUS" >&2; exit 1; }
for doc in iam guests rooms reservations operations billing; do
  request GET "/v3/api-docs/${doc}"
  [[ "$RESP_STATUS" == "200" ]] || { echo "/v3/api-docs/${doc} failed: $RESP_STATUS" >&2; exit 1; }
done

step "4/15 Signing in as ${ADMIN_EMAIL} and checking /api/v1/authentication/me"
SIGNIN_PAYLOAD="$(python3 -c 'import json,sys; print(json.dumps({"email":sys.argv[1],"password":sys.argv[2]}))' "$ADMIN_EMAIL" "$ADMIN_PASSWORD")"
request POST "/api/v1/authentication/sign-in" "$SIGNIN_PAYLOAD"
[[ "$RESP_STATUS" == "200" ]] || { echo "Sign-in failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
JWT="$(python3 -c 'import re,sys; m=re.search(r"Set-Cookie:[^\r\n]*token=([^;\r\n]+)", sys.argv[1], re.I); assert m; print(m.group(1))' "$RESP_HEADERS")"

request GET "/api/v1/authentication/me" "" "$JWT"
[[ "$RESP_STATUS" == "200" ]] || { echo "/me failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"

step "5/15 Fetching document types catalog"
request GET "/api/v1/document-types" "" "$JWT"
[[ "$RESP_STATUS" == "200" ]] || { echo "/document-types failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
python3 -c 'import json,sys; assert any(x["code"]=="DNI" for x in json.loads(sys.argv[1]))' "$RESP_BODY"

DNI="$(python3 -c 'import random; print(random.randint(10000000, 99999999))')"
SUFFIX="$(python3 -c 'import time; print(int(time.time()*1000))')"
step "6/15 Registering guest with DNI ${DNI}"
GUEST_PAYLOAD="$(python3 -c 'import json,sys; print(json.dumps({"documentTypeCode":"DNI","documentNumber":sys.argv[1],"firstName":"Carlos","lastName":"Mendoza","email":f"e2e.{sys.argv[2]}@swissotel.com","phone":"+51987654321","countryOfOrigin":"Peru"}))' "$DNI" "$SUFFIX")"
request POST "/api/v1/guests" "$GUEST_PAYLOAD" "$JWT"
[[ "$RESP_STATUS" == "201" ]] || { echo "Create guest failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
GUEST_PUBLIC_ID="$(python3 -c 'import json,sys; print(json.loads(sys.argv[1])["publicId"])' "$RESP_BODY")"

DATES="$(python3 -c 'from datetime import datetime,timedelta,timezone; d=(datetime.now(timezone.utc)-timedelta(hours=5)).date(); print(d.isoformat(), (d+timedelta(days=2)).isoformat())')"
CHECK_IN="${DATES% *}"
CHECK_OUT="${DATES#* }"

step "7/15 Listing room types and checking availability (${CHECK_IN} -> ${CHECK_OUT})"
request GET "/api/v1/room-types" "" "$JWT"
[[ "$RESP_STATUS" == "200" ]] || { echo "/room-types failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
ROOM_TYPE_PUBLIC_ID="$(python3 -c 'import json,sys; items=json.loads(sys.argv[1]); pick=next((x for x in items if x["name"]=="Deluxe"), items[0]); print(pick["publicId"])' "$RESP_BODY")"

request GET "/api/v1/availability?checkIn=${CHECK_IN}&checkOut=${CHECK_OUT}" "" "$JWT"
[[ "$RESP_STATUS" == "200" ]] || { echo "/availability failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"

step "8/15 Creating and confirming reservation"
RES_PAYLOAD="$(python3 -c 'import json,sys; print(json.dumps({"guestPublicId":sys.argv[1],"roomTypePublicId":sys.argv[2],"checkInDate":sys.argv[3],"checkOutDate":sys.argv[4],"paymentType":"deferred"}))' "$GUEST_PUBLIC_ID" "$ROOM_TYPE_PUBLIC_ID" "$CHECK_IN" "$CHECK_OUT")"
request POST "/api/v1/reservations" "$RES_PAYLOAD" "$JWT"
[[ "$RESP_STATUS" == "201" ]] || { echo "Create reservation failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
RES_INFO="$(python3 -c 'import json,sys; d=json.loads(sys.argv[1]); print(d["publicId"], d["reservationCode"], d["status"])' "$RESP_BODY")"
read -r RES_PUBLIC_ID RES_CODE RES_STATUS <<< "$RES_INFO"

if [[ "$RES_STATUS" != "confirmed" ]]; then
  request POST "/api/v1/reservations/${RES_PUBLIC_ID}/confirm" "" "$JWT"
  [[ "$RESP_STATUS" == "200" ]] || { echo "Confirm reservation failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
  assert_no_raw_ids "$RESP_BODY"
fi

step "9/15 Performing check-in for reservation ${RES_CODE}"
CHECKIN_PAYLOAD="$(python3 -c 'import json,sys; print(json.dumps({"reservationCode":sys.argv[1],"documentType":"DNI","documentNumber":sys.argv[2]}))' "$RES_CODE" "$DNI")"
request POST "/api/v1/stays/check-in" "$CHECKIN_PAYLOAD" "$JWT"
[[ "$RESP_STATUS" == "201" ]] || { echo "Check-in failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
STAY_INFO="$(python3 -c 'import json,sys; d=json.loads(sys.argv[1]); assert d.get("accessCode"); print(d["stayId"], d["roomNumber"])' "$RESP_BODY")"
read -r STAY_ID ROOM_NUMBER <<< "$STAY_INFO"

step "10/15 Polling billing-service for async folio opened via RabbitMQ (stayId=${STAY_ID})"
FOLIO_ID=""
for ((i=1; i<=ASYNC_TIMEOUT_SECONDS; i++)); do
  request GET "/api/v1/folios?stayId=${STAY_ID}" "" "$JWT"
  if [[ "$RESP_STATUS" == "200" ]]; then
    assert_no_raw_ids "$RESP_BODY"
    FOLIO_ID="$(python3 -c 'import json,sys; d=json.loads(sys.argv[1]); assert d["status"]=="open"; print(d["folioId"])' "$RESP_BODY")"
    break
  fi
  sleep 1
done
[[ -n "$FOLIO_ID" ]] || { echo "Timed out waiting for async folio" >&2; exit 1; }

step "11/15 Creating and completing billable service request (45.00 PEN)"
SR_PAYLOAD="$(python3 -c 'import json,sys; print(json.dumps({"stayId":sys.argv[1],"serviceTypeCode":"room_service","description":"E2E Espresso Service"}))' "$STAY_ID")"
request POST "/api/v1/service-requests" "$SR_PAYLOAD" "$JWT"
[[ "$RESP_STATUS" == "201" ]] || { echo "Create service request failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
REQUEST_ID="$(python3 -c 'import json,sys; print(json.loads(sys.argv[1])["requestId"])' "$RESP_BODY")"

request PATCH "/api/v1/service-requests/${REQUEST_ID}/status" '{"status":"completed","chargeAmount":45.00}' "$JWT"
[[ "$RESP_STATUS" == "200" ]] || { echo "Complete service request failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"

step "12/15 Polling folio summary for async service-request charge (folioId=${FOLIO_ID})"
FOLIO_TOTAL=""
for ((i=1; i<=ASYNC_TIMEOUT_SECONDS; i++)); do
  request GET "/api/v1/folios/${FOLIO_ID}/summary" "" "$JWT"
  [[ "$RESP_STATUS" == "200" ]] || { echo "Folio summary failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
  assert_no_raw_ids "$RESP_BODY"
  FOLIO_TOTAL="$(python3 -c 'from decimal import Decimal; import json,sys; d=json.loads(sys.argv[1]); print(d["totalAmount"] if len(d["charges"])>=1 and Decimal(str(d["totalAmount"]))>=Decimal("45.00") else "")' "$RESP_BODY")"
  if [[ -n "$FOLIO_TOTAL" ]]; then
    break
  fi
  sleep 1
done
[[ -n "$FOLIO_TOTAL" ]] || { echo "Timed out waiting for async charge on folio ${FOLIO_ID}" >&2; exit 1; }

step "13/15 Processing full payment for folio (${FOLIO_TOTAL} PEN)"
PAY_PAYLOAD="$(python3 -c 'import json,sys; print(json.dumps({"folioId":sys.argv[1],"paymentMethodCode":"credit_card","amount":float(sys.argv[2])}))' "$FOLIO_ID" "$FOLIO_TOTAL")"
request POST "/api/v1/payments" "$PAY_PAYLOAD" "$JWT"
[[ "$RESP_STATUS" == "201" ]] || { echo "Payment failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
PAYMENT_ID="$(python3 -c 'import json,sys; d=json.loads(sys.argv[1]); assert d["status"]=="processed"; print(d["paymentId"])' "$RESP_BODY")"

step "14/15 Issuing receipt for payment ${PAYMENT_ID}"
INV_PAYLOAD="$(python3 -c 'import json,sys; print(json.dumps({"paymentId":sys.argv[1],"documentType":"receipt"}))' "$PAYMENT_ID")"
request POST "/api/v1/invoices" "$INV_PAYLOAD" "$JWT"
[[ "$RESP_STATUS" == "201" ]] || { echo "Invoice failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
INV_REF="$(python3 -c 'import json,sys; d=json.loads(sys.argv[1]); print(f"{d[\"series\"]}-{d[\"number\"]} ({d[\"totalAmount\"]})")' "$RESP_BODY")"

step "15/15 Checking out stay ${STAY_ID}"
request POST "/api/v1/stays/${STAY_ID}/check-out" "" "$JWT"
[[ "$RESP_STATUS" == "200" ]] || { echo "Check-out failed ($RESP_STATUS): $RESP_BODY" >&2; exit 1; }
assert_no_raw_ids "$RESP_BODY"
python3 -c 'import json,sys; assert json.loads(sys.argv[1])["status"]=="closed"' "$RESP_BODY"

echo
echo "ALL 15 E2E SMOKE CHECKS PASSED!"
echo "Summary: Guest=${GUEST_PUBLIC_ID} Reservation=${RES_CODE} Stay=${STAY_ID} Room=${ROOM_NUMBER} Folio=${FOLIO_ID} Invoice=${INV_REF}"
