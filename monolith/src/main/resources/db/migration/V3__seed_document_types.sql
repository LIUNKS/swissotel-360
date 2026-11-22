-- Identity document types used when registering hotel guests.
INSERT INTO document_types (code, name, has_check_digit) VALUES
  ('DNI', 'Documento Nacional de Identidad', FALSE),
  ('PASSPORT', 'Passport', FALSE),
  ('CE', 'Carné de Extranjería', FALSE)
ON CONFLICT (code) DO NOTHING;
