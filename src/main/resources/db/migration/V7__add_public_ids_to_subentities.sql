ALTER TABLE rates
    ADD COLUMN IF NOT EXISTS public_id UUID NOT NULL DEFAULT gen_random_uuid();
CREATE UNIQUE INDEX IF NOT EXISTS uq_rates_public_id ON rates (public_id);

ALTER TABLE digital_keys
    ADD COLUMN IF NOT EXISTS public_id UUID NOT NULL DEFAULT gen_random_uuid();
CREATE UNIQUE INDEX IF NOT EXISTS uq_digital_keys_public_id ON digital_keys (public_id);

ALTER TABLE charges
    ADD COLUMN IF NOT EXISTS public_id UUID NOT NULL DEFAULT gen_random_uuid();
CREATE UNIQUE INDEX IF NOT EXISTS uq_charges_public_id ON charges (public_id);
