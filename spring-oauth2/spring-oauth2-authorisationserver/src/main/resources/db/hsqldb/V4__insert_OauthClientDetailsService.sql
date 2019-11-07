INSERT INTO OAUTH_CLIENT_DETAILS(
	CLIENT_ID,
	RESOURCE_IDS,
	CLIENT_SECRET,
	SCOPE,
	AUTHORIZED_GRANT_TYPES,
	AUTHORITIES,
	WEB_SERVER_REDIRECT_URI,	
	ACCESS_TOKEN_VALIDITY,
	REFRESH_TOKEN_VALIDITY,
	ADDITIONAL_INFORMATION,
	AUTOAPPROVE)
VALUES
	(
	'my-app',
	'my-app',
	'{PLAIN}app-secret',
	'user_info,read,write',
	'authorization_code,client_credentials,password,refresh_token,implicit',
	'ROLE_ADMIN,ROLE_USER',
	'http://localhost:8888/res_srv/login/oauth2/code/',	
	300,
	3600,
	'{"description":"my oauth app"}',
	'true'
	)
--	,
--	(
--	'...',
--	)
;

-- 'authorization_code, client_credentials, password, refresh_token, implicit',
