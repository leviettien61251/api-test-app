-- API Test App - PostgreSQL schema and initial account
-- Run this script while connected to the api_test_app database.
-- Safe to run more than once: it does not delete existing application data.

BEGIN;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS roles
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users
(
    id         VARCHAR(255) PRIMARY KEY DEFAULT (uuid_generate_v4()::text),
    role_id    INTEGER      NOT NULL REFERENCES roles (id),
    full_name  VARCHAR(255) NOT NULL,
    phone      VARCHAR(15)  NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS client_machines
(
    id           VARCHAR(255) PRIMARY KEY DEFAULT (uuid_generate_v4()::text),
    user_id      VARCHAR(255) NOT NULL REFERENCES users (id),
    machine_name VARCHAR(255) NOT NULL,
    os           VARCHAR(100) NOT NULL,
    ip_address   VARCHAR(45),
    hostname     VARCHAR(255),
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_test_suites
(
    id               VARCHAR(255) PRIMARY KEY DEFAULT (uuid_generate_v4()::text),
    user_id          VARCHAR(255) REFERENCES users (id),
    owner_name       VARCHAR(255)  NOT NULL,
    name             VARCHAR(255)  NOT NULL,
    method           VARCHAR(10)   NOT NULL,
    endpoint         VARCHAR(2048) NOT NULL,
    description      TEXT,
    cleanup_requests JSONB         NOT NULL DEFAULT '[]'::jsonb,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_test_cases
(
    id                     VARCHAR(255) PRIMARY KEY DEFAULT (uuid_generate_v4()::text),
    user_id                VARCHAR(255) REFERENCES users (id),
    suite_id               VARCHAR(255) REFERENCES user_test_suites (id),
    owner_name             VARCHAR(255)  NOT NULL,
    api_label              VARCHAR(255)  NOT NULL,
    name                   VARCHAR(255)  NOT NULL,
    description            TEXT,
    method                 VARCHAR(10)   NOT NULL,
    endpoint               VARCHAR(2048) NOT NULL,
    request_headers        JSONB         NOT NULL DEFAULT '{}'::jsonb,
    query_params           JSONB         NOT NULL DEFAULT '{}'::jsonb,
    path_params            JSONB         NOT NULL DEFAULT '{}'::jsonb,
    request_body           TEXT,
    setup_requests         JSONB         NOT NULL DEFAULT '[]'::jsonb,
    cleanup_requests       JSONB         NOT NULL DEFAULT '[]'::jsonb,
    payload_assertions     JSONB         NOT NULL DEFAULT '[]'::jsonb,
    expected_response_body TEXT,
    expected_status_code   INTEGER       NOT NULL,
    is_active              BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (email);
CREATE INDEX IF NOT EXISTS idx_client_machines_user_id
    ON client_machines (user_id);
CREATE INDEX IF NOT EXISTS idx_user_test_suites_user
    ON user_test_suites (user_id);
CREATE INDEX IF NOT EXISTS idx_user_test_suites_owner
    ON user_test_suites (owner_name);
CREATE INDEX IF NOT EXISTS idx_user_test_cases_user_api
    ON user_test_cases (user_id, api_label);
CREATE INDEX IF NOT EXISTS idx_user_test_cases_owner_api
    ON user_test_cases (owner_name, api_label);
CREATE INDEX IF NOT EXISTS idx_user_test_cases_suite_id
    ON user_test_cases (suite_id);

INSERT INTO roles (name, description)
VALUES ('admin', 'Application administrator'),
       ('tester', 'API tester')
ON CONFLICT (name) DO NOTHING;

-- Initial local account. Change this password after the first successful login.
-- The current application compares passwords as plain text.
INSERT INTO users (role_id, full_name, phone, email, password)
SELECT id, 'Local Administrator', '0900000000', 'admin@local.test', 'ChangeMe123!'
FROM roles
WHERE name = 'admin'
ON CONFLICT (email) DO NOTHING;

COMMIT;
