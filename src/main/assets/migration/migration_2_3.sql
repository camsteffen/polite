-- add inverse match to calendar rule

ALTER TABLE CalendarRule RENAME TO CalendarRuleOld;
CREATE TABLE CalendarRule(
    _id INTEGER PRIMARY KEY NOT NULL,
    matchAll INTEGER NOT NULL,
    matchTitle INTEGER NOT NULL,
    matchDesc INTEGER NOT NULL,
    inverseMatch INTEGER NOT NULL NOT NULL,
    FOREIGN KEY (_id) REFERENCES
    Rule (_id) ON UPDATE CASCADE ON DELETE CASCADE);
INSERT INTO CalendarRule(_id, matchAll, matchTitle, matchDesc, inverseMatch)
    SELECT _id, matchAll, matchTitle, matchDesc, 0 FROM CalendarRuleOld;
DROP TABLE CalendarRuleOld;
