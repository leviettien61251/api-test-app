insert into users(role_id, full_name, phone, email, password)
values
    ('2', 'TESTER 3', '0980000004', 'b', 'b'),
    ('2', 'TESTER 2', '0980000003', 'a', 'a'),
       ('2', 'TESTER 1', '0980000001', 'tester1@tester.com', 'tester1@tester.com'),
       ('1', 'ADMIN 1', '0980000002', 'admin1@tester.com', 'SuperSecurePassword123@');

INSERT INTO roles(name, description)
values ('admin', 'this is admin role'),
       ('tester', 'this is tester role');

insert into client_machines(user_id, machine_name, os_name, os_version, ip_address, cpu_info, ram_info, hostname)
values (?, ?, ?, ?, ?, ?, ?, ?);

SELECT *
FROM roles;
SELECT *
FROM users;
select *
from client_machines;
select *
from user_test_suites;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- ============================================
-- DROP TABLES (ngược thứ tự dependency)
-- ============================================

DROP TABLE IF EXISTS test_reports CASCADE;
DROP TABLE IF EXISTS test_assertions CASCADE;
DROP TABLE IF EXISTS test_results CASCADE;
DROP TABLE IF EXISTS test_runs CASCADE;
DROP TABLE IF EXISTS user_test_cases CASCADE;
DROP TABLE IF EXISTS user_test_suites CASCADE;
-- DROP TABLE IF EXISTS environment_variables CASCADE;
DROP TABLE IF EXISTS test_data_sets CASCADE;
DROP TABLE IF EXISTS test_cases CASCADE;
DROP TABLE IF EXISTS api_endpoints CASCADE;
DROP TABLE IF EXISTS test_suits CASCADE;
DROP TABLE IF EXISTS folders CASCADE;
DROP TABLE IF EXISTS collections CASCADE;
DROP TABLE IF EXISTS client_machines CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;


CREATE TABLE roles
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE, -- admin, tester
    description TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE users
(
    id         VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_id    INTEGER      NOT NULL REFERENCES roles (id),
    full_name  VARCHAR(255) NOT NULL,
    phone      VARCHAR(15)  NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN      NOT NULL    DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL    DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE client_machines
(
    id           VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      VARCHAR(255) NOT NULL REFERENCES users (id),
    machine_name VARCHAR(255) NOT NULL,
    os           VARCHAR(100) NOT NULL,
    ip_address   VARCHAR(45),
    hostname     VARCHAR(255),
    is_active    BOOLEAN      NOT NULL    DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL    DEFAULT NOW()
);


CREATE TABLE user_test_suites
(
    id               VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          VARCHAR(255) REFERENCES users (id),
    owner_name       VARCHAR(255)  NOT NULL,
    name             VARCHAR(255)  NOT NULL,
    method           VARCHAR(10)   NOT NULL,
    endpoint         VARCHAR(2048) NOT NULL,
    description      TEXT,
    cleanup_requests JSONB         NOT NULL   DEFAULT '[]',
    is_active        BOOLEAN       NOT NULL   DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL   DEFAULT NOW(),
    updated_at       TIMESTAMP
);

-- Testcase do user tạo trực tiếp trong app. Đây là nguồn dữ liệu chính cho testcase động;
-- JSON file chỉ dùng cho import/export hoặc seed dữ liệu mẫu.
CREATE TABLE user_test_cases
(
    id                   VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id              VARCHAR(255) REFERENCES users (id),
    suite_id             VARCHAR(255) REFERENCES user_test_suites (id),
    owner_name           VARCHAR(255)  NOT NULL,
    api_label            VARCHAR(255)  NOT NULL,
    name                 VARCHAR(255)  NOT NULL,
    description          TEXT,
    method               VARCHAR(10)   NOT NULL,
    endpoint             VARCHAR(2048) NOT NULL,
    request_headers      JSONB         NOT NULL   DEFAULT '{}',
    query_params         JSONB         NOT NULL   DEFAULT '{}',
    path_params          JSONB         NOT NULL   DEFAULT '{}',
    request_body         TEXT,
    setup_requests       JSONB         NOT NULL   DEFAULT '[]',
    cleanup_requests     JSONB         NOT NULL   DEFAULT '[]',
    payload_assertions   JSONB         NOT NULL   DEFAULT '[]',
    expected_response_body TEXT,
    expected_status_code INTEGER       NOT NULL,
    is_active            BOOLEAN       NOT NULL   DEFAULT TRUE,
    created_at           TIMESTAMP     NOT NULL   DEFAULT NOW(),
    updated_at           TIMESTAMP
);

-- ============================================
-- INDEXES GỢI Ý
-- ============================================

-- Tra cứu user theo email
CREATE INDEX idx_users_email ON users (email);

-- Tra cứu machines theo user
CREATE INDEX idx_client_machines_user_id ON client_machines (user_id);

-- Tra cứu testcase user theo user/API
CREATE INDEX idx_user_test_suites_user ON user_test_suites (user_id);
CREATE INDEX idx_user_test_suites_owner ON user_test_suites (owner_name);
CREATE INDEX idx_user_test_cases_user_api ON user_test_cases (user_id, api_label);
CREATE INDEX idx_user_test_cases_owner_api ON user_test_cases (owner_name, api_label);
CREATE INDEX idx_user_test_cases_suite_id ON user_test_cases (suite_id);
