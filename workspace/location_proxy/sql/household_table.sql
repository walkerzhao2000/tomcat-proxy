DROP TABLE IF EXISTS households;
CREATE TABLE households (
    household  VARCHAR(40)   NOT NULL,
    -- household can bind either username or deviceid or both.
    -- username is customer self defined, while deviceid is mac (which is immutable).
    -- household primarily bind with deviceid, but deviceid is hard to remember by customers.
    -- If username is given, deviceid is automatically obtained from table user_device, and username is easy to be remembered by customers.
    -- username is bound with different deviceid at different time.
    -- One username is only allowed to bind with one deviceid at a time.
    -- Both username and deviceid are given, it defines who on what device. It is most deirable way to build household.
    username   VARCHAR(20)   NOT NULL,
    deviceid   BINARY(6)     NOT NULL, -- alias of mac address
    latest_d   TIMESTAMP     NOT NULL,
    FOREIGN KEY (username, deviceid) REFERENCES user_device (username, deviceid),
                                       -- ON DELETE CASCADE??
                                       -- ON UPDATE CASCADE??
    PRIMARY KEY (household)
);
