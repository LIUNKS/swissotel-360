INSERT INTO permissions (code, module, description)
VALUES ('rooms.update_status', 'rooms', 'Update room status after operational authorization')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_RECEPTIONIST' AND p.code = 'rooms.update_status'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_HOUSEKEEPING' AND p.code = 'rooms.update_status'
ON CONFLICT DO NOTHING;
