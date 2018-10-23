INSERT INTO OAUTH_CLIENT_DETAILS(
	CLIENT_ID,
	RESOURCE_IDS,
	CLIENT_SECRET,
	SCOPE,
	AUTHORIZED_GRANT_TYPES,
	AUTHORITIES,
	ACCESS_TOKEN_VALIDITY,
	REFRESH_TOKEN_VALIDITY,
	AUTOAPPROVE)
VALUES (
	'my-client-id-read',
	'my-oauth-app',
	'{noop}my-secret',
	'user_info,read',
	'authorization_code,client_credentials,password,refresh_token,implicit',
	'USER',
	120,
	3600,
	'user_info,read');
	
INSERT INTO OAUTH_CLIENT_DETAILS(
	CLIENT_ID,
	RESOURCE_IDS,
	CLIENT_SECRET,
	SCOPE,
	AUTHORIZED_GRANT_TYPES,
	AUTHORITIES,
	ACCESS_TOKEN_VALIDITY,
	REFRESH_TOKEN_VALIDITY,
	AUTOAPPROVE)
VALUES (
	'my-client-id-write',
	'my-oauth-app',
	'{noop}my-secret',
	'user_info,read,write',
	'authorization_code,client_credentials,password,refresh_token,implicit',
	'USER,ADMIN',
	120,
	3600,
	'user_info,read,write');	