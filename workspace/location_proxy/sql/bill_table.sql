DROP TABLE IF EXISTS bills;
CREATE TABLE bills (
    username   VARCHAR(20)   NOT NULL,
    latest_d   TIMESTAMP     NOT NULL,
    freq       SMALLINT(5)   NOT NULL, -- min seconds to wait dormantly
                                       -- incoming timestamp must be >= latest_d + freq; otherwise, denied. When permitted, count += 1.
    count      SMALLINT(5)   NOT NULL, -- daily request count
                                       -- bill daily; reset daily.
                                       -- max request frequency per user is one per 2 seconds
                                       -- free: allows one request per hour
                                       -- request: first come first serve
    PRIMARY KEY (username)
);
