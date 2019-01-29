-- add schedule rules

-- Note: CalendarRuleCalendar and CalendarRuleKeyword are missing some NOT NULL's after this
-- migration. This is fixed in DB version 4.

ALTER TABLE Rule RENAME TO RuleOld;
ALTER TABLE RuleCalendar RENAME TO CalendarRuleCalendar;
ALTER TABLE RuleKeyword RENAME TO CalendarRuleKeyword;
CREATE TABLE Rule(
    _id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    enable INTEGER NOT NULL,
    vibrate INTEGER NOT NULL);
CREATE TABLE CalendarRule(
    _id INTEGER PRIMARY KEY NOT NULL,
    matchAll INTEGER NOT NULL,
    matchTitle INTEGER NOT NULL,
    matchDesc INTEGER NOT NULL,
    FOREIGN KEY (_id) REFERENCES Rule (_id) ON UPDATE CASCADE ON DELETE CASCADE);
INSERT INTO Rule (_id, name, enable, vibrate)
    SELECT _id, name, enable, vibrate FROM RuleOld;
INSERT INTO CalendarRule(_id, matchAll, matchTitle, matchDesc)
    SELECT _id, matchAll, matchTitle, matchDesc FROM RuleOld;
DROP TABLE RuleOld;
CREATE TABLE ScheduleRule(
    _id INTEGER PRIMARY KEY NOT NULL,
    begin INTEGER NOT NULL,
    end INTEGER NOT NULL,
    sunday INTEGER NOT NULL,
    monday INTEGER NOT NULL,
    tuesday INTEGER NOT NULL,
    wednesday INTEGER NOT NULL,
    thursday INTEGER NOT NULL,
    friday INTEGER NOT NULL,
    saturday INTEGER NOT NULL,
    FOREIGN KEY (_id) REFERENCES Rule (_id) ON UPDATE CASCADE ON DELETE CASCADE);
