DROP TABLE IF EXISTS order_book_levels;
DROP TABLE IF EXISTS order_book_snapshots;

CREATE TABLE IF NOT EXISTS order_book_snapshots
(
    snapshot_id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    symbol
    VARCHAR
(
    10
) NOT NULL,
    timestamp TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS order_book_levels
(
    snapshot_id
    BIGINT
    NOT
    NULL,
    level_id
    BIGINT
    NOT
    NULL,
    side
    VARCHAR
(
    4
) NOT NULL,
    price DOUBLE NOT NULL,
    size BIGINT NOT NULL,
    PRIMARY KEY
(
    snapshot_id,
    level_id
),
    CONSTRAINT fk_snapshot
    FOREIGN KEY
(
    snapshot_id
)
    REFERENCES order_book_snapshots
(
    snapshot_id
)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_topn_levels
    ON order_book_levels (snapshot_id, side, price);



-- Example Query
-- Top latest 10 Best Bids and Asks


-- SELECT l.*
-- FROM order_book_levels l
--          JOIN order_book_snapshots s ON l.snapshot_id = s.snapshot_id
-- WHERE s.symbol = 'ETHUSD'
--   AND l.side = 'BUY'
--   AND s.snapshot_id = (
--     SELECT MAX(snapshot_id)
--     FROM order_book_snapshots
--     WHERE symbol = 'ETHUSD'
-- )
-- ORDER BY l.price DESC
--     LIMIT 10;


-- SELECT l.*
-- FROM order_book_levels l
--          JOIN order_book_snapshots s ON l.snapshot_id = s.snapshot_id
-- WHERE s.symbol = 'ETHUSD'
--   AND l.side = 'SELL'
--   AND s.snapshot_id = (
--     SELECT MAX(snapshot_id)
--     FROM order_book_snapshots
--     WHERE symbol = 'ETHUSD'
-- )
-- ORDER BY l.price ASC
--     LIMIT 10;


