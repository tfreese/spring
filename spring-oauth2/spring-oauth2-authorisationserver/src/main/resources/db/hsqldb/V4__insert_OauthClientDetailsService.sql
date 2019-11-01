INSERT INTO OAUTH_CLIENT_DETAILS(
	CLIENT_ID,
	RESOURCE_IDS,
	CLIENT_SECRET,
	SCOPE,
	AUTHORIZED_GRANT_TYPES,
	WEB_SERVER_REDIRECT_URI,
	AUTHORITIES,
	ACCESS_TOKEN_VALIDITY,
	REFRESH_TOKEN_VALIDITY,
	ADDITIONAL_INFORMATION,
	AUTOAPPROVE)
VALUES
	(
	'my-client-id-read',
	'my-oauth-app',
	'{NOOP}my-client-secret',
	'user_info, read',
	'authorization_code',
	'http://localhost:8082/login/oauth2/code/',
	'USER',
	120,
	3600,
	'{"description":"read-only client"}',
	'true'
	)
	,
	(
	'my-client-id-write',
	'my-oauth-app',
	'{NOOP}my-client-secret',
	'user_info, read, write',
	'authorization_code',
	'http://localhost:8082/login/oauth2/code/',
	'USER,ADMIN',
	120,
	3600,
	'{"description":"read-write client"}',
	'true'
	)
;

-- 'authorization_code, client_credentials, password, refresh_token, implicit',
-- 'http://localhost:8082/ui/login, http://localhost:8083/ui2/login, http://localhost:8082/login',
