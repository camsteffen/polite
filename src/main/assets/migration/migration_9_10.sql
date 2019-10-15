ALTER TABLE rule ADD mute_media INTEGER NOT NULL DEFAULT 0;
ALTER TABLE rule ADD interrupt_filter INTEGER NOT NULL DEFAULT 2;

CREATE TABLE active_rule_event_new (
    rule_id INTEGER NOT NULL,
    begin INTEGER NOT NULL,
    end INTEGER NOT NULL,
    PRIMARY KEY(rule_id));
INSERT INTO active_rule_event_new (rule_id, begin, end)
    SELECT rule_id, begin, end from active_rule_event;
DROP TABLE active_rule_event;
ALTER TABLE active_rule_event_new RENAME TO active_rule_event;
