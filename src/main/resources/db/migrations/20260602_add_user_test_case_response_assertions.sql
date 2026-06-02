ALTER TABLE user_test_cases
    ADD COLUMN IF NOT EXISTS payload_assertions JSONB NOT NULL DEFAULT '[]',
    ADD COLUMN IF NOT EXISTS expected_response_body TEXT;
