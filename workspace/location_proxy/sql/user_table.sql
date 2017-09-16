DROP TABLE IF EXISTS users;
CREATE TABLE users (
    username   VARCHAR(20)   NOT NULL, -- max 20 bytes username
    password   BINARY(20)    NOT NULL, -- 20 bytes (fixed size) encrypted password
    salt       BINARY(8)     NOT NULL, -- 8 bytes salt, use with (encrypted) password for authentication
    recovery   VARCHAR(40)   NOT NULL, -- password recovery email
    create_d   TIMESTAMP     NOT NULL,
    access_d   TIMESTAMP     NOT NULL,
    privilege  TINYINT(3)    NOT NULL, -- 0: free, >0: paid (1=min privilege, ..., 127=max privilege)
    PRIMARY KEY (username)
);
