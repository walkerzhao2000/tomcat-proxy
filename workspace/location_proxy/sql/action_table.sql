DROP TABLE IF EXISTS actions;
CREATE TABLE actions (
    actionid   TINYINT(3)    NOT NULL,
    action     VARCHAR(256), -- action trigger (maybe URL, phone#, speech, internal activity, 3rd-party sw, etc.)
    PRIMARY KEY (actionid)
);
