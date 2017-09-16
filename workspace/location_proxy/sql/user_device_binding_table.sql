DROP TABLE IF EXISTS user_device;
CREATE TABLE user_device (
    username   VARCHAR(20)   NOT NULL,
    deviceid   BINARY(6)     NOT NULL, -- alias of mac address
    -- One username is only allowed to bind with one deviceid at a time.
    latest_d   TIMESTAMP     NOT NULL,
    actions    VARCHAR(256), -- JSON contains a list of actions
    PRIMARY KEY (username, deviceid)
);
