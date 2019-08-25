CREATE TABLE active_rule_event (
    rule_id INTEGER NOT NULL,
    begin INTEGER NOT NULL,
    end INTEGER NOT NULL,
    vibrate INTEGER NOT NULL,
    PRIMARY KEY(rule_id));

CREATE TABLE schedule_rule_cancel (
    rule_id INTEGER NOT NULL,
    end INTEGER NOT NULL,
    PRIMARY KEY(rule_id),
    FOREIGN KEY(rule_id) REFERENCES rule(id) ON UPDATE CASCADE ON DELETE CASCADE);

CREATE TABLE event_cancel (
    event_id INTEGER NOT NULL,
    end INTEGER NOT NULL,
    PRIMARY KEY(event_id));
