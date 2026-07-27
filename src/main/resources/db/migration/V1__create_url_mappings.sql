CREATE TABLE url_mappings (
    id           BIGSERIAL    PRIMARY KEY,
    short_code   VARCHAR(32)  NOT NULL UNIQUE,
    destination_url VARCHAR(2048) NOT NULL,
    click_count  BIGINT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL
);
