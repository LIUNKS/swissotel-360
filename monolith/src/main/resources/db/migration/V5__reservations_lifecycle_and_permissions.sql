ALTER TABLE reservations
    ADD COLUMN cancelled_at TIMESTAMPTZ,
    ADD COLUMN cancellation_reason VARCHAR(20),
    ADD COLUMN cancellation_note VARCHAR(200),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp();

UPDATE reservations
SET cancelled_at = created_at,
    cancellation_reason = 'staff'
WHERE status = 'cancelled';

ALTER TABLE reservations
    ADD CONSTRAINT ck_reservations_cancellation_reason
        CHECK (cancellation_reason IS NULL OR cancellation_reason IN ('guest_request','staff','payment_timeout')),
    ADD CONSTRAINT ck_reservations_cancellation
        CHECK ((status = 'cancelled') = (cancelled_at IS NOT NULL AND cancellation_reason IS NOT NULL));

CREATE TRIGGER trg_reservations_updated_at
    BEFORE UPDATE ON reservations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE UNIQUE INDEX uq_reservations_active_duplicate
    ON reservations (guest_id, room_type_id, check_in_date, check_out_date)
    WHERE status IN ('pending','confirmed');

CREATE INDEX idx_reservations_pending_created
    ON reservations (created_at) WHERE status = 'pending';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_GUEST' AND p.code = 'reservations.cancel'
ON CONFLICT DO NOTHING;
