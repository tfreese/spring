INSERT INTO USER(USERNAME, PASSWORD) VALUES ('admin', '{NOOP}pw');	
INSERT INTO USER(USERNAME, PASSWORD) VALUES ('user', '{NOOP}pw');
	
	
INSERT INTO AUTHORITY(USERNAME, ROLE) VALUES ('admin',	'ADMIN');
INSERT INTO AUTHORITY(USERNAME, ROLE) VALUES ('admin',	'USER');
	
INSERT INTO AUTHORITY(USERNAME, ROLE) VALUES ('user', 'USER');		
