CREATE TABLE drivers (
    id                      VARCHAR(32) PRIMARY KEY,
    name                    VARCHAR(120) NOT NULL,
    terminal                VARCHAR(3)   NOT NULL,
    hazmat_endorsed         BOOLEAN      NOT NULL DEFAULT FALSE,
    on_duty                 BOOLEAN      NOT NULL DEFAULT FALSE,
    available_capacity_lbs  DOUBLE PRECISION NOT NULL DEFAULT 0,
    last_lat                DOUBLE PRECISION,
    last_lng                DOUBLE PRECISION,
    last_ping_at            TIMESTAMPTZ
);

CREATE INDEX ix_drivers_terminal_on_duty ON drivers (terminal) WHERE on_duty;

CREATE TABLE assignments (
    shipment_id   VARCHAR(32) PRIMARY KEY,
    pro_number    VARCHAR(12) NOT NULL UNIQUE,
    terminal      VARCHAR(3)  NOT NULL,
    pickup_date   DATE        NOT NULL,
    pickup_lat    DOUBLE PRECISION NOT NULL,
    pickup_lng    DOUBLE PRECISION NOT NULL,
    weight_lbs    DOUBLE PRECISION NOT NULL,
    hazmat        BOOLEAN     NOT NULL DEFAULT FALSE,
    status        VARCHAR(24) NOT NULL,
    driver_id     VARCHAR(32) REFERENCES drivers (id),
    offered_at    TIMESTAMPTZ,
    accepted_at   TIMESTAMPTZ,
    version       BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX ix_assignments_board ON assignments (terminal, pickup_date, status);
CREATE INDEX ix_assignments_driver ON assignments (driver_id) WHERE driver_id IS NOT NULL;
