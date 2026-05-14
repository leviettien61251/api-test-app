INSERT INTO roles(name, description)
values ('admin', 'this is admin role'),
       ('tester', 'this is tester role');

insert into users(role_id, full_name, phone, email, password)
values ('2', 'TESTER 1', '0980000001', 'tester1@tester.com', 'SuperSecurePassword123@'),
       ('1', 'ADMIN 1', '0980000002', 'admin1@tester.com', 'SuperSecurePassword123@');

insert into client_machines(user_id, machine_name, os_name, os_version, ip_address, cpu_info, ram_info, hostname)
values (?, ?, ?, ?, ?, ?, ?, ?);

SELECT *
FROM roles;
SELECT *
FROM users;
select * from client_machines;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- ============================================
-- DROP TABLES (ngược thứ tự dependency)
-- ============================================

DROP TABLE IF EXISTS test_reports CASCADE;
DROP TABLE IF EXISTS test_assertions CASCADE;
DROP TABLE IF EXISTS test_results CASCADE;
DROP TABLE IF EXISTS test_runs CASCADE;
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

-- ============================================
-- PHẦN 1: QUẢN LÝ NGƯỜI DÙNG & PHÂN QUYỀN
-- ============================================

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

-- ============================================
-- PHẦN 2: QUẢN LÝ MÁY KHÁCH
-- ============================================

CREATE TABLE client_machines
(
    id           VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      VARCHAR(255) NOT NULL REFERENCES users (id),
    machine_name VARCHAR(255) NOT NULL,
    os           VARCHAR(100) NOT NULL, -- Windows, macOS, Linux
    ip_address   VARCHAR(45),           -- IPv4 hoặc IPv6
    hostname     VARCHAR(255),          -- Tên máy trong mạng
    is_active    BOOLEAN      NOT NULL    DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL    DEFAULT NOW()
);

-- ============================================
-- PHẦN 3: TỔ CHỨC TEST SUITE
-- ============================================

CREATE TABLE collections
(
    id          SERIAL PRIMARY KEY,
    user_id     VARCHAR(255) NOT NULL REFERENCES users (id),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    icon        VARCHAR(50), -- Emoji hoặc icon name
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

CREATE TABLE folders
(
    id               SERIAL PRIMARY KEY,
    collection_id    INTEGER      NOT NULL REFERENCES collections (id),
    parent_folder_id INTEGER REFERENCES folders (id), -- NULL nếu là folder gốc
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    sort_order       INTEGER      NOT NULL DEFAULT 0,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP
);

CREATE TABLE test_suits
(
    id            VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    collection_id INTEGER       NOT NULL REFERENCES collections (id),
    folder_id     INTEGER REFERENCES folders (id), -- NULL nếu không thuộc folder nào
    name          VARCHAR(255)  NOT NULL,
    description   TEXT,
    base_url      VARCHAR(2048) NOT NULL,          -- https://api.benhvien.com/v1
    is_active     BOOLEAN       NOT NULL   DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL   DEFAULT NOW(),
    updated_at    TIMESTAMP
);

-- ============================================
-- PHẦN 4: ĐỊNH NGHĨA API ENDPOINTS & TEST CASES
-- ============================================

CREATE TABLE api_endpoints
(
    id           VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    test_suit_id VARCHAR(255)  NOT NULL REFERENCES test_suits (id),
    method       VARCHAR(10)   NOT NULL,                -- GET, POST, PUT, PATCH, DELETE
    path         VARCHAR(2048) NOT NULL,                -- /patients, /guides/{id}
    description  TEXT,
    tags         JSONB         NOT NULL   DEFAULT '[]', -- ["patients", "navigation"]
    is_active    BOOLEAN       NOT NULL   DEFAULT TRUE,
    created_at   TIMESTAMP     NOT NULL   DEFAULT NOW(),
    updated_at   TIMESTAMP
);

CREATE TABLE test_cases
(
    id                       VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    api_endpoint_id          VARCHAR(255) NOT NULL REFERENCES api_endpoints (id),
    name                     VARCHAR(255) NOT NULL,
    description              TEXT,
    request_headers          JSONB        NOT NULL    DEFAULT '{}',
    request_body             JSONB,
    query_params             JSONB        NOT NULL    DEFAULT '{}',
    path_params              JSONB        NOT NULL    DEFAULT '{}',
    expected_status_code     INTEGER      NOT NULL,
    expected_response_schema JSONB,
    expected_response_body   JSONB,
    expected_headers         JSONB        NOT NULL    DEFAULT '{}',
    failure_strategy         VARCHAR(20)  NOT NULL    DEFAULT 'STOP_ON_FAIL', -- STOP_ON_FAIL | CONTINUE
    timeout_ms               INTEGER      NOT NULL    DEFAULT 30000,
    retry_count              INTEGER      NOT NULL    DEFAULT 0,
    retry_delay_ms           INTEGER      NOT NULL    DEFAULT 1000,
    sort_order               INTEGER      NOT NULL    DEFAULT 0,
    is_active                BOOLEAN      NOT NULL    DEFAULT TRUE,
    created_at               TIMESTAMP    NOT NULL    DEFAULT NOW(),
    updated_at               TIMESTAMP
);

-- ============================================
-- PHẦN 5: LƯU TRỮ TEST DATA & BIẾN MÔI TRƯỜNG
-- ============================================

CREATE TABLE test_data_sets
(
    id           SERIAL PRIMARY KEY,
    test_case_id VARCHAR(255) NOT NULL REFERENCES test_cases (id),
    name         VARCHAR(255) NOT NULL,
    data         JSONB        NOT NULL,
    description  TEXT,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
--
-- CREATE TABLE environment_variables
-- (
--     id            SERIAL PRIMARY KEY,
--     collection_id INTEGER      NOT NULL REFERENCES collections (id),
--     key           VARCHAR(255) NOT NULL,
--     value         TEXT         NOT NULL,
--     is_secret     BOOLEAN      NOT NULL DEFAULT FALSE,
--     description   TEXT,
--     created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
--     updated_at    TIMESTAMP,
--
--     CONSTRAINT uq_env_vars_collection_key UNIQUE (collection_id, key)
-- );

-- ============================================
-- PHẦN 6: LỊCH SỬ CHẠY TEST
-- ============================================

CREATE TABLE test_runs
(
    id                VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           VARCHAR(255) NOT NULL REFERENCES users (id),
    client_machine_id VARCHAR(255) NOT NULL REFERENCES client_machines (id),
    run_timestamp     TIMESTAMP    NOT NULL    DEFAULT NOW(),
    run_name          VARCHAR(255),

    -- Chế độ chạy
    run_mode          VARCHAR(20)  NOT NULL    DEFAULT 'SUITE',    -- SUITE | SINGLE_CASE
    test_suit_id      VARCHAR(255) REFERENCES test_suits (id),
    test_case_id      VARCHAR(255) REFERENCES test_cases (id),

    -- Chế độ cảnh báo
    failure_strategy  VARCHAR(20)  NOT NULL    DEFAULT 'CONTINUE', -- STOP_ON_FAIL | CONTINUE

    -- Kết quả tổng thể
    overall_status    VARCHAR(20)  NOT NULL    DEFAULT 'RUNNING',  -- RUNNING | PASSED | FAILED | PARTIALLY_PASSED | STOPPED | ERROR
    total_cases       INTEGER      NOT NULL    DEFAULT 0,
    passed_cases      INTEGER      NOT NULL    DEFAULT 0,
    failed_cases      INTEGER      NOT NULL    DEFAULT 0,
    skipped_cases     INTEGER      NOT NULL    DEFAULT 0,
    error_cases       INTEGER      NOT NULL    DEFAULT 0,
    total_duration_ms INTEGER,

    notes             TEXT,
    created_at        TIMESTAMP    NOT NULL    DEFAULT NOW(),
    completed_at      TIMESTAMP
);

CREATE TABLE test_results
(
    id                   VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    test_run_id          VARCHAR(255) NOT NULL REFERENCES test_runs (id),
    test_case_id         VARCHAR(255) NOT NULL REFERENCES test_cases (id),

    -- Kết quả
    status               VARCHAR(20)  NOT NULL    DEFAULT 'PENDING', -- PENDING | RUNNING | PASSED | FAILED | SKIPPED | ERROR
    response_status_code INTEGER,
    response_headers     JSONB,
    response_body        JSONB,
    response_size_bytes  INTEGER,
    response_time_ms     INTEGER,
    total_duration_ms    INTEGER,

    -- Chi tiết lỗi
    error_type           VARCHAR(100),                               -- NETWORK_ERROR | TIMEOUT | ASSERTION_FAILED
    error_message        TEXT,
    error_stack          TEXT,

    -- Retry
    retry_attempts       INTEGER      NOT NULL    DEFAULT 0,

    executed_at          TIMESTAMP    NOT NULL    DEFAULT NOW(),
    completed_at         TIMESTAMP,

    CONSTRAINT uq_test_results_run_case UNIQUE (test_run_id, test_case_id)
);

-- ============================================
-- PHẦN 7: CHI TIẾT ASSERTIONS
-- ============================================

CREATE TABLE test_assertions
(
    id                  VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    test_result_id      VARCHAR(255) NOT NULL REFERENCES test_results (id),

    -- Loại assertion
    assertion_type      VARCHAR(50)  NOT NULL,                     -- STATUS_CODE | RESPONSE_SCHEMA | RESPONSE_BODY | RESPONSE_HEADER | RESPONSE_TIME

    -- Đối tượng kiểm tra
    target_path         VARCHAR(500),                              -- JSON path: $.data.patients[0].name
    target_description  TEXT,

    -- Giá trị
    expected_value      TEXT,
    actual_value        TEXT,
    comparison_operator VARCHAR(20)  NOT NULL    DEFAULT 'EQUALS', -- EQUALS | CONTAINS | REGEX | GREATER_THAN | LESS_THAN | EXISTS | NOT_NULL

    -- Kết quả
    passed              BOOLEAN      NOT NULL,
    message             TEXT,

    executed_at         TIMESTAMP    NOT NULL    DEFAULT NOW()
);

-- ============================================
-- PHẦN 8: BÁO CÁO & THỐNG KÊ
-- ============================================

CREATE TABLE test_reports
(
    id              VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    test_run_id     VARCHAR(255) NOT NULL REFERENCES test_runs (id),

    report_type     VARCHAR(50)  NOT NULL    DEFAULT 'JSON', -- JSON | HTML | PDF
    report_data     JSONB        NOT NULL,
    summary         TEXT,
    file_path       VARCHAR(500),
    file_size_bytes INTEGER,

    created_at      TIMESTAMP    NOT NULL    DEFAULT NOW()
);

-- ============================================
-- INDEXES GỢI Ý
-- ============================================

-- Tra cứu user theo email
CREATE INDEX idx_users_email ON users (email);

-- Tra cứu machines theo user
CREATE INDEX idx_client_machines_user_id ON client_machines (user_id);

-- Tra cứu test runs theo user và thời gian
CREATE INDEX idx_test_runs_user_id ON test_runs (user_id);
CREATE INDEX idx_test_runs_timestamp ON test_runs (run_timestamp DESC);

-- Tra cứu results theo run
CREATE INDEX idx_test_results_run_id ON test_results (test_run_id);

-- Tra cứu assertions theo result
CREATE INDEX idx_test_assertions_result_id ON test_assertions (test_result_id);

-- Tra cứu test cases theo endpoint
CREATE INDEX idx_test_cases_endpoint_id ON test_cases (api_endpoint_id);

-- Tra cứu endpoints theo suit
CREATE INDEX idx_api_endpoints_suit_id ON api_endpoints (test_suit_id);