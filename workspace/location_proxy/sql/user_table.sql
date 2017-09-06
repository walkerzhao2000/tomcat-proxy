DROP TABLE IF EXISTS users;
CREATE TABLE users (
    username   VARCHAR(20)   NOT NULL, -- max 20 bytes username
    password   BINARY(40)    NOT NULL, -- 40 bytes (fixed size) password
    salt       BINARY(8)     NOT NULL, -- use with (encrypted) password for authentication
    recovery   VARCHAR(40)   NOT NULL, -- password recovery email
    create_d   TIMESTAMP     NOT NULL,
    access_d   TIMESTAMP     NOT NULL,
    privilege  TINYINT(3)    NOT NULL, -- 0: free, >0: paid
    PRIMARY KEY (username)
);
