[CmdletBinding()]
param(
    [string]$GatewayUrl = "http://localhost:8080",
    [string]$AdminEmail = "admin@swissotel.com",
    [string]$AdminPassword = "Admin123!",
    [int]$AsyncTimeoutSeconds = 20
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$BaseUrl = $GatewayUrl.TrimEnd('/')
$UuidRegex = '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'

function Assert-Condition {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) {
        throw "ASSERTION FAILED: $Message"
    }
}

function Assert-NoRawIdFields {
    param(
        [Parameter(ValueFromPipeline = $true)]
        $Node,
        [string]$Path = "$"
    )
    if ($null -eq $Node) { return }

    if ($Node -is [System.Collections.IDictionary]) {
        foreach ($key in $Node.Keys) {
            $childPath = "$Path.$key"
            $val = $Node[$key]
            if ($key -ceq "id") {
                throw "PRIVATE ID LEAK: Found forbidden field '$childPath' with value '$val'"
            }
            if ($key -match '(?:Id|_id)$' -and $null -ne $val) {
                if ($val -isnot [string] -or $val -notmatch $UuidRegex) {
                    throw "PRIVATE ID LEAK: Expected UUID string for '$childPath', got '$val'"
                }
            }
            Assert-NoRawIdFields -Node $val -Path $childPath
        }
    }
    elseif ($Node -is [System.Management.Automation.PSCustomObject]) {
        foreach ($prop in $Node.PSObject.Properties) {
            $childPath = "$Path.$($prop.Name)"
            $val = $prop.Value
            if ($prop.Name -ceq "id") {
                throw "PRIVATE ID LEAK: Found forbidden field '$childPath' with value '$val'"
            }
            if ($prop.Name -match '(?:Id|_id)$' -and $null -ne $val) {
                if ($val -isnot [string] -or $val -notmatch $UuidRegex) {
                    throw "PRIVATE ID LEAK: Expected UUID string for '$childPath', got '$val'"
                }
            }
            Assert-NoRawIdFields -Node $val -Path $childPath
        }
    }
    elseif ($Node -is [System.Collections.IEnumerable] -and $Node -isnot [string]) {
        $idx = 0
        foreach ($item in $Node) {
            Assert-NoRawIdFields -Node $item -Path "$Path[$idx]"
            $idx++
        }
    }
}

function Invoke-Api {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Path,
        [hashtable]$Headers = @{},
        $Body = $null
    )

    $uri = "$BaseUrl$Path"
    $params = @{
        Uri             = $uri
        Method          = $Method
        Headers         = $Headers
        UseBasicParsing = $true
        TimeoutSec      = 30
    }
    if ($null -ne $Body) {
        $params.ContentType = "application/json; charset=utf-8"
        $params.Body = if ($Body -is [string]) { $Body } else { ($Body | ConvertTo-Json -Depth 10 -Compress) }
    }

    try {
        $resp = Invoke-WebRequest @params
        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($resp.Content)) {
            $json = $resp.Content | ConvertFrom-Json
        }
        return [pscustomobject]@{
            StatusCode = [int]$resp.StatusCode
            Headers    = $resp.Headers
            RawBody    = $resp.Content
            Json       = $json
        }
    }
    catch [System.Net.WebException] {
        $webResp = $_.Exception.Response
        if ($null -eq $webResp) { throw }
        $status = [int]([System.Net.HttpWebResponse]$webResp).StatusCode
        $raw = ""
        $stream = $webResp.GetResponseStream()
        if ($null -ne $stream) {
            $reader = New-Object System.IO.StreamReader($stream)
            $raw = $reader.ReadToEnd()
            $reader.Dispose()
        }
        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($raw)) {
            try { $json = $raw | ConvertFrom-Json } catch {}
        }
        return [pscustomobject]@{
            StatusCode = $status
            Headers    = $webResp.Headers
            RawBody    = $raw
            Json       = $json
        }
    }
    catch {
        # PowerShell 7+ HttpResponseException handling
        if ($_.Exception.PSObject.Properties['Response']) {
            $httpResp = $_.Exception.Response
            $status = [int]$httpResp.StatusCode
            $raw = $httpResp.Content.ReadAsStringAsync().GetAwaiter().GetResult()
            $json = $null
            if (-not [string]::IsNullOrWhiteSpace($raw)) {
                try { $json = $raw | ConvertFrom-Json } catch {}
            }
            return [pscustomobject]@{
                StatusCode = $status
                Headers    = $httpResp.Headers
                RawBody    = $raw
                Json       = $json
            }
        }
        throw
    }
}

function Write-Step {
    param([string]$Message)
    Write-Host "[E2E] $Message" -ForegroundColor Cyan
}

Write-Host "Running Swissotel 360 E2E smoke suite against $BaseUrl" -ForegroundColor Green

# 1. Gateway health check
Write-Step "1/15 Checking Gateway /actuator/health"
$health = Invoke-Api -Method "GET" -Path "/actuator/health"
Assert-Condition ($health.StatusCode -eq 200) "Expected 200 on /actuator/health, got $($health.StatusCode): $($health.RawBody)"
Assert-Condition ($health.Json.status -eq "UP") "Expected Gateway status UP, got $($health.RawBody)"

# 2. Internal endpoint blocking at edge
Write-Step "2/15 Verifying /internal/** is blocked at Gateway"
$internalBlocked = Invoke-Api -Method "GET" -Path "/internal/users/1"
Assert-Condition ($internalBlocked.StatusCode -eq 403) "Expected 403 on /internal/users/1, got $($internalBlocked.StatusCode): $($internalBlocked.RawBody)"
Assert-Condition ($internalBlocked.Json.code -eq "FORBIDDEN") "Expected code FORBIDDEN on /internal/users/1, got $($internalBlocked.RawBody)"

# 3. OpenAPI aggregation across all 6 microservices
Write-Step "3/15 Verifying aggregated OpenAPI swagger-config and 6 service docs"
$swaggerCfg = Invoke-Api -Method "GET" -Path "/v3/api-docs/swagger-config"
Assert-Condition ($swaggerCfg.StatusCode -eq 200) "Expected 200 on /v3/api-docs/swagger-config, got $($swaggerCfg.StatusCode)"
foreach ($doc in @("iam", "guests", "rooms", "reservations", "operations", "billing")) {
    $docResp = Invoke-Api -Method "GET" -Path "/v3/api-docs/$doc"
    Assert-Condition ($docResp.StatusCode -eq 200) "Expected 200 on /v3/api-docs/$doc, got $($docResp.StatusCode)"
}

# 4. Sign in and verify /me
Write-Step "4/15 Signing in as $AdminEmail and checking /api/v1/authentication/me"
$signIn = Invoke-Api -Method "POST" -Path "/api/v1/authentication/sign-in" -Body @{
    email    = $AdminEmail
    password = $AdminPassword
}
Assert-Condition ($signIn.StatusCode -eq 200) "Expected 200 on sign-in, got $($signIn.StatusCode): $($signIn.RawBody)"
Assert-NoRawIdFields $signIn.Json

$setCookie = $signIn.Headers["Set-Cookie"]
$cookieJoined = if ($setCookie -is [System.Array]) { $setCookie -join "; " } else { [string]$setCookie }
Assert-Condition ($cookieJoined -match '(?:^|;\s*)token=([^;]+)') "JWT token cookie not returned on sign-in"
$jwt = $matches[1]
$authHeaders = @{ Authorization = "Bearer $jwt" }

$me = Invoke-Api -Method "GET" -Path "/api/v1/authentication/me" -Headers $authHeaders
Assert-Condition ($me.StatusCode -eq 200) "Expected 200 on /me, got $($me.StatusCode): $($me.RawBody)"
Assert-NoRawIdFields $me.Json

# 5. Document types catalog
Write-Step "5/15 Fetching document types catalog"
$docTypes = Invoke-Api -Method "GET" -Path "/api/v1/document-types" -Headers $authHeaders
Assert-Condition ($docTypes.StatusCode -eq 200) "Expected 200 on /document-types, got $($docTypes.StatusCode): $($docTypes.RawBody)"
Assert-NoRawIdFields $docTypes.Json
Assert-Condition (($docTypes.Json | Where-Object { $_.code -eq "DNI" }).Count -ge 1) "Expected DNI in document-types catalog"

# 6. Create guest
$dni = [string](Get-Random -Minimum 10000000 -Maximum 99999999)
$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
Write-Step "6/15 Registering guest with DNI $dni"
$guestResp = Invoke-Api -Method "POST" -Path "/api/v1/guests" -Headers $authHeaders -Body @{
    documentTypeCode = "DNI"
    documentNumber   = $dni
    firstName        = "Carlos"
    lastName         = "Mendoza"
    email            = "e2e.$suffix@swissotel.com"
    phone            = "+51987654321"
    countryOfOrigin  = "Peru"
}
Assert-Condition ($guestResp.StatusCode -eq 201) "Expected 201 on create guest, got $($guestResp.StatusCode): $($guestResp.RawBody)"
Assert-NoRawIdFields $guestResp.Json
$guestPublicId = $guestResp.Json.publicId

# 7. Room types & availability (America/Lima UTC-5 date)
$limaToday = [DateTime]::UtcNow.AddHours(-5).Date
$checkIn = $limaToday.ToString("yyyy-MM-dd")
$checkOut = $limaToday.AddDays(2).ToString("yyyy-MM-dd")
Write-Step "7/15 Listing room types and checking availability ($checkIn -> $checkOut)"
$roomTypes = Invoke-Api -Method "GET" -Path "/api/v1/room-types" -Headers $authHeaders
Assert-Condition ($roomTypes.StatusCode -eq 200) "Expected 200 on /room-types, got $($roomTypes.StatusCode): $($roomTypes.RawBody)"
Assert-NoRawIdFields $roomTypes.Json
$selectedType = $roomTypes.Json | Where-Object { $_.name -eq "Deluxe" } | Select-Object -First 1
if ($null -eq $selectedType) { $selectedType = $roomTypes.Json | Select-Object -First 1 }
$roomTypePublicId = $selectedType.publicId

$avail = Invoke-Api -Method "GET" -Path "/api/v1/availability?checkIn=$checkIn&checkOut=$checkOut" -Headers $authHeaders
Assert-Condition ($avail.StatusCode -eq 200) "Expected 200 on /availability, got $($avail.StatusCode): $($avail.RawBody)"
Assert-NoRawIdFields $avail.Json

# 8. Create & confirm reservation
Write-Step "8/15 Creating and confirming reservation"
$resResp = Invoke-Api -Method "POST" -Path "/api/v1/reservations" -Headers $authHeaders -Body @{
    guestPublicId    = $guestPublicId
    roomTypePublicId = $roomTypePublicId
    checkInDate      = $checkIn
    checkOutDate     = $checkOut
    paymentType      = "deferred"
}
Assert-Condition ($resResp.StatusCode -eq 201) "Expected 201 on create reservation, got $($resResp.StatusCode): $($resResp.RawBody)"
Assert-NoRawIdFields $resResp.Json
$reservationPublicId = $resResp.Json.publicId
$reservationCode = $resResp.Json.reservationCode

if ($resResp.Json.status -ne "confirmed") {
    $confirmResp = Invoke-Api -Method "POST" -Path "/api/v1/reservations/$reservationPublicId/confirm" -Headers $authHeaders
    Assert-Condition ($confirmResp.StatusCode -eq 200) "Expected 200 on confirm reservation, got $($confirmResp.StatusCode): $($confirmResp.RawBody)"
    Assert-NoRawIdFields $confirmResp.Json
}

# 9. Check-in
Write-Step "9/15 Performing check-in for reservation $reservationCode"
$checkInResp = Invoke-Api -Method "POST" -Path "/api/v1/stays/check-in" -Headers $authHeaders -Body @{
    reservationCode = $reservationCode
    documentType    = "DNI"
    documentNumber  = $dni
}
Assert-Condition ($checkInResp.StatusCode -eq 201) "Expected 201 on check-in, got $($checkInResp.StatusCode): $($checkInResp.RawBody)"
Assert-NoRawIdFields $checkInResp.Json
$stayId = $checkInResp.Json.stayId
Assert-Condition (-not [string]::IsNullOrWhiteSpace($checkInResp.Json.accessCode)) "Expected non-empty digital key accessCode"

# 10. Poll RabbitMQ async folio creation
Write-Step "10/15 Polling billing-service for async folio opened via RabbitMQ (stayId=$stayId)"
$folio = $null
for ($attempt = 1; $attempt -le $AsyncTimeoutSeconds; $attempt++) {
    $folioResp = Invoke-Api -Method "GET" -Path "/api/v1/folios?stayId=$stayId" -Headers $authHeaders
    if ($folioResp.StatusCode -eq 200) {
        Assert-NoRawIdFields $folioResp.Json
        $folio = $folioResp.Json
        break
    }
    Start-Sleep -Seconds 1
}
Assert-Condition ($null -ne $folio) "Timed out waiting for async folio creation in billing-service"
Assert-Condition ($folio.status -eq "open") "Expected folio status 'open', got '$($folio.status)'"
$folioId = $folio.folioId

# 11. Create and complete billable service request
Write-Step "11/15 Creating and completing billable service request (45.00 PEN)"
$srCreate = Invoke-Api -Method "POST" -Path "/api/v1/service-requests" -Headers $authHeaders -Body @{
    stayId          = $stayId
    serviceTypeCode = "room_service"
    description     = "E2E Espresso Service"
}
Assert-Condition ($srCreate.StatusCode -eq 201) "Expected 201 on create service request, got $($srCreate.StatusCode): $($srCreate.RawBody)"
Assert-NoRawIdFields $srCreate.Json
$requestId = $srCreate.Json.requestId

$srComplete = Invoke-Api -Method "PATCH" -Path "/api/v1/service-requests/$requestId/status" -Headers $authHeaders -Body @{
    status       = "completed"
    chargeAmount = 45.00
}
Assert-Condition ($srComplete.StatusCode -eq 200) "Expected 200 on complete service request, got $($srComplete.StatusCode): $($srComplete.RawBody)"
Assert-NoRawIdFields $srComplete.Json

# 12. Poll RabbitMQ async charge posting to folio
Write-Step "12/15 Polling folio summary for async service-request charge (folioId=$folioId)"
$summary = $null
for ($attempt = 1; $attempt -le $AsyncTimeoutSeconds; $attempt++) {
    $sumResp = Invoke-Api -Method "GET" -Path "/api/v1/folios/$folioId/summary" -Headers $authHeaders
    Assert-Condition ($sumResp.StatusCode -eq 200) "Expected 200 on folio summary, got $($sumResp.StatusCode): $($sumResp.RawBody)"
    Assert-NoRawIdFields $sumResp.Json
    if ($sumResp.Json.charges.Count -ge 1 -and [decimal]$sumResp.Json.totalAmount -ge 45.00) {
        $summary = $sumResp.Json
        break
    }
    Start-Sleep -Seconds 1
}
Assert-Condition ($null -ne $summary) "Timed out waiting for async charge on folio $folioId"

# 13. Pay folio
Write-Step "13/15 Processing full payment for folio ($($summary.totalAmount) PEN)"
$payResp = Invoke-Api -Method "POST" -Path "/api/v1/payments" -Headers $authHeaders -Body @{
    folioId           = $folioId
    paymentMethodCode = "credit_card"
    amount            = [decimal]$summary.totalAmount
}
Assert-Condition ($payResp.StatusCode -eq 201) "Expected 201 on payment, got $($payResp.StatusCode): $($payResp.RawBody)"
Assert-NoRawIdFields $payResp.Json
Assert-Condition ($payResp.Json.status -eq "processed") "Expected payment status 'processed', got '$($payResp.Json.status)'"
$paymentId = $payResp.Json.paymentId

# 14. Issue receipt/invoice
Write-Step "14/15 Issuing receipt for payment $paymentId"
$invResp = Invoke-Api -Method "POST" -Path "/api/v1/invoices" -Headers $authHeaders -Body @{
    paymentId    = $paymentId
    documentType = "receipt"
}
Assert-Condition ($invResp.StatusCode -eq 201) "Expected 201 on issue invoice, got $($invResp.StatusCode): $($invResp.RawBody)"
Assert-NoRawIdFields $invResp.Json

# 15. Check-out stay
Write-Step "15/15 Checking out stay $stayId"
$checkOutResp = Invoke-Api -Method "POST" -Path "/api/v1/stays/$stayId/check-out" -Headers $authHeaders
Assert-Condition ($checkOutResp.StatusCode -eq 200) "Expected 200 on check-out, got $($checkOutResp.StatusCode): $($checkOutResp.RawBody)"
Assert-NoRawIdFields $checkOutResp.Json
Assert-Condition ($checkOutResp.Json.status -eq "closed") "Expected stay status 'closed', got '$($checkOutResp.Json.status)'"

Write-Host ""
Write-Host "ALL 15 E2E SMOKE CHECKS PASSED!" -ForegroundColor Green
Write-Host ("Summary: Guest={0} Reservation={1} Stay={2} Room={3} Folio={4} Invoice={5}-{6} Total={7}" -f `
    $guestPublicId, $reservationCode, $stayId, $checkInResp.Json.roomNumber, $folioId, `
    $invResp.Json.series, $invResp.Json.number, $invResp.Json.totalAmount)
