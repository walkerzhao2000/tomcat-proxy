DROP TABLE IF EXISTS households;
CREATE TABLE households (
    household  VARCHAR(40)   NOT NULL,
    username   VARCHAR(20)   NOT NULL,
    deviceid   BINARY(6)     NOT NULL, -- alias of mac address
    latest_d   TIMESTAMP     NOT NULL,
    FOREIGN KEY (username, deviceid) REFERENCES user_device (username, deviceid),
                                       -- ON DELETE CASCADE??
                                       -- ON UPDATE CASCADE??
    PRIMARY KEY (household)
);
