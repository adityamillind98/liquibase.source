-- liquibase formatted sql

-- changeset alex:1
CREATE TABLE PRIMARY_TABLE (NAME VARCHAR(20));

-- changeset alex:2
CREATE OR REPLACE VIEW "SOME_VIEW" AS SELECT * FROM "PRIMARY_TABLE";
COMMENT ON TABLE SOME_VIEW IS 'THIS IS A COMMENT ON SOME_VIEW VIEW. THIS VIEW COMMENT SHOULD BE CAPTURED BY GenerateChangeLog.';
