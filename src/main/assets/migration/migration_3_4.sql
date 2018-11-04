-- repair earlier migrations that did not match the schema exactly

ALTER TABLE CalendarRuleCalendar RENAME TO CalendarRuleCalendarOld;
CREATE TABLE CalendarRuleCalendar(
    _id INTEGER PRIMARY KEY NOT NULL,
    rule INTEGER NOT NULL,
    calendarID INTEGER NOT NULL,
    FOREIGN KEY (rule) REFERENCES Rule (_id) ON UPDATE CASCADE ON DELETE CASCADE);
INSERT INTO CalendarRuleCalendar(_id, rule, calendarID)
    SELECT _id, rule, calendarID FROM CalendarRuleCalendarOld;
DROP TABLE CalendarRuleCalendarOld;
ALTER TABLE CalendarRuleKeyword RENAME TO CalendarRuleKeywordOld;
CREATE TABLE CalendarRuleKeyword(
    _id INTEGER PRIMARY KEY NOT NULL,
    rule INTEGER NOT NULL,
    word TEXT NOT NULL,
    FOREIGN KEY (rule) REFERENCES Rule (_id) ON UPDATE CASCADE ON DELETE CASCADE);
INSERT INTO CalendarRuleKeyword(_id, rule, word)
    SELECT _id, rule, word FROM CalendarRuleKeywordOld;
DROP TABLE CalendarRuleKeywordOld;
