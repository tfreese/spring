DROP TABLE IF EXISTS OAUTH_ACCESS_TOKEN CASCADE;
DROP TABLE IF EXISTS OAUTH_REFRESH_TOKEN CASCADE;
DROP TABLE IF EXISTS OAUTH_CLIENT_TOKEN CASCADE;

-- LONGVARBINARY
CREATE TABLE OAUTH_ACCESS_TOKEN (
	TOKEN_ID			VARCHAR(255),
	TOKEN				VARBINARY(4096),
	AUTHENTICATION_ID	VARCHAR(255),
	USER_NAME			VARCHAR(255),
	CLIENT_ID			VARCHAR(255),
	AUTHENTICATION		VARBINARY(4096),
	REFRESH_TOKEN		VARCHAR(255)
);

ALTER TABLE OAUTH_ACCESS_TOKEN ADD CONSTRAINT OAUTH_ACCESS_TOKEN_PK PRIMARY KEY (AUTHENTICATION_ID);
CREATE INDEX OAUTH_ACCESS_TOKEN_IDX_TOKEN_ID ON OAUTH_ACCESS_TOKEN (TOKEN_ID);

COMMENT ON TABLE OAUTH_ACCESS_TOKEN IS 'Tabelle für OAUTH_ACCESS_TOKEN';
COMMENT ON COLUMN OAUTH_ACCESS_TOKEN.TOKEN IS 'TOKEN';


CREATE TABLE OAUTH_REFRESH_TOKEN (
	TOKEN_ID		VARCHAR(255),
	TOKEN			VARBINARY(4096),
	AUTHENTICATION	VARBINARY(4096)
);

CREATE INDEX OAUTH_REFRESH_TOKEN_IDX_TOKEN_ID ON OAUTH_REFRESH_TOKEN (TOKEN_ID);

COMMENT ON TABLE OAUTH_REFRESH_TOKEN IS 'Tabelle für OAUTH_REFRESH_TOKEN';
COMMENT ON COLUMN OAUTH_REFRESH_TOKEN.TOKEN IS 'TOKEN';


CREATE TABLE OAUTH_CLIENT_TOKEN (
	TOKEN_ID			VARCHAR(255),
	TOKEN				VARBINARY(4096),
	AUTHENTICATION_ID	VARCHAR(255),
	USER_NAME			VARCHAR(255),
	CLIENT_ID			VARCHAR(255)
);

ALTER TABLE OAUTH_CLIENT_TOKEN ADD CONSTRAINT OAUTH_CLIENT_TOKEN_PK PRIMARY KEY (AUTHENTICATION_ID);
CREATE INDEX OAUTH_CLIENT_TOKEN_IDX_TOKEN_ID ON OAUTH_CLIENT_TOKEN (TOKEN_ID);

COMMENT ON TABLE OAUTH_CLIENT_TOKEN IS 'Tabelle für OAUTH_CLIENT_TOKEN';
COMMENT ON COLUMN OAUTH_CLIENT_TOKEN.TOKEN IS 'TOKEN';