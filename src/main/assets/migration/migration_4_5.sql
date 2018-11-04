-- prepare for Room migration
-- Note: AUTOINCREMENT is not recommended by SQLite but is unavoidable in Room

ALTER TABLE Rule RENAME TO RuleOld;
CREATE TABLE rule(
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    enabled INTEGER NOT NULL,
    vibrate INTEGER NOT NULL);
INSERT INTO rule(id, name, enabled, vibrate)
    SELECT _id, name, enable, vibrate FROM RuleOld;
DROP TABLE RuleOld;
CREATE TABLE calendar_rule(
    id INTEGER PRIMARY KEY NOT NULL,
    match_all INTEGER NOT NULL,
    match_title INTEGER NOT NULL,
    match_description INTEGER NOT NULL,
    inverse_match INTEGER NOT NULL NOT NULL,
    FOREIGN KEY (id) REFERENCES rule (id) ON UPDATE CASCADE ON DELETE CASCADE);
INSERT INTO calendar_rule(id, match_all, match_title, match_description, inverse_match)
    SELECT _id, matchAll, matchTitle, matchDesc, inverseMatch FROM CalendarRule;
DROP TABLE CalendarRule;
CREATE TABLE calendar_rule_calendar(
    id INTEGER PRIMARY KEY NOT NULL,
    rule_id INTEGER NOT NULL,
    calendar_id INTEGER NOT NULL,
    FOREIGN KEY (rule_id) REFERENCES rule (id) ON UPDATE CASCADE ON DELETE CASCADE);
INSERT INTO calendar_rule_calendar(id, rule_id, calendar_id)
    SELECT _id, rule, calendarID FROM CalendarRuleCalendar;
DROP TABLE CalendarRuleCalendar;
CREATE INDEX index_calendar_rule_calendar_rule_id ON calendar_rule_calendar (rule_id);
CREATE TABLE calendar_rule_keyword(
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    rule_id INTEGER NOT NULL,
    keyword TEXT NOT NULL,
    FOREIGN KEY (rule_id) REFERENCES rule (id) ON UPDATE CASCADE ON DELETE CASCADE);
INSERT INTO calendar_rule_keyword(id, rule_id, keyword)
    SELECT _id, rule, word FROM CalendarRuleKeyword;
DROP TABLE CalendarRuleKeyword;
CREATE INDEX index_calendar_rule_keyword_rule_id ON calendar_rule_keyword (rule_id);
CREATE TABLE schedule_rule(
    id INTEGER PRIMARY KEY NOT NULL,
    begin_time INTEGER NOT NULL,
    end_time INTEGER NOT NULL,
    sunday INTEGER NOT NULL,
    monday INTEGER NOT NULL,
    tuesday INTEGER NOT NULL,
    wednesday INTEGER NOT NULL,
    thursday INTEGER NOT NULL,
    friday INTEGER NOT NULL,
    saturday INTEGER NOT NULL,
    FOREIGN KEY (id) REFERENCES rule (id) ON UPDATE CASCADE ON DELETE CASCADE);
INSERT INTO schedule_rule(id, begin_time, end_time, sunday, monday, tuesday, wednesday, thursday, friday, saturday)
    SELECT _id, begin * 60, end * 60, sunday, monday, tuesday, wednesday, thursday, friday, saturday FROM ScheduleRule;
DROP TABLE ScheduleRule;
