UPDATE active_rule_event SET begin = begin / 1000, end = end / 1000;
UPDATE schedule_rule_cancel SET end = end / 1000;

DROP TABLE event_cancel;

CREATE TABLE event_cancel(
  rule_id INTEGER NOT NULL,
  event_id INTEGER NOT NULL,
  end INTEGER NOT NULL,
  PRIMARY KEY(rule_id, event_id)
  FOREIGN KEY(rule_id) REFERENCES rule(id) ON UPDATE CASCADE ON DELETE CASCADE);
