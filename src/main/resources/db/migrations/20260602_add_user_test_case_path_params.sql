ALTER TABLE user_test_cases
    ADD COLUMN IF NOT EXISTS path_params JSONB NOT NULL DEFAULT '{}';
