CREATE SEQUENCE person_seq start with 1 increment by 1;

CREATE TABLE person (
id BIGINT NOT NULL PRIMARY KEY,
name VARCHAR(50) NOT NULL
)
