

    Nope: alter table SAKAI_SITE add column CONTEXT_GUID varchar(99);

    alter table GB_GRADABLE_OBJECT_T add column PLUS_LINEITEM longtext;

    mysql -u sakaiuser -p

    select TENNANT_GUID, TITLE, TRUST_EMAIL from PLUS_TENANT;


    select TENNANT_GUID, TITLE, TRUST_EMAIL, ISSUER, CLIENT_ID, DEPLOYMENT_ID, OIDC_KEYSET, OIDC_TOKEN, OIDC_AUTH from PLUS_TENANT;

    +--------------+------------+-------------+--------------------------------+--------------------------------------+----------------------------------------------+--------------------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------+--------------------------------------------------------------+
    | TENNANT_GUID | TITLE      | TRUST_EMAIL | ISSUER                         | CLIENT_ID                            | DEPLOYMENT_ID                                | OIDC_KEYSET                                                                                                        | OIDC_TOKEN                                                          | OIDC_AUTH                                                    |
    | 123456       | Sakai Plus | NULL        | https://canvas.instructure.com | 85530000000000147                    | 326:a16deed8f169b120bdd14743e67ca7916eaea622 | https://canvas.instructure.com/api/lti/security/jwks                                                               | https://canvas.instructure.com/login/oauth2/token                   | https://canvas.instructure.com/api/lti/authorize_redirect    |
    | 54321        | Blackboard | NULL        | https://blackboard.com         | 4c43e5f0-9eef-425f-bf7c-c81689013cb7 | 14af10f1-04ed-4457-8e40-a581681458ce         | https://devportal-stage.saas.bbpd.io/api/v1/management/applications/4c43e5f0-9eef-425f-bf7c-c81689013cb7/jwks.json | https://devportal-stage.saas.bbpd.io/api/v1/gateway/oauth2/jwttoken | https://devportal-stage.saas.bbpd.io/api/v1/gateway/oidcauth |

    https://dev1.sakaicloud.com/plus/sakai/canvas-config.json?guid=123456

    UPDATE PLUS_TENANT SET
        ISSUER = 'https://blackboard.com',
        CLIENT_ID = '4c43e5f0-9eef-425f-bf7c-c81689013cb7',
        DEPLOYMENT_ID = '14af10f1-04ed-4457-8e40-a581681458ce',
        OIDC_KEYSET = 'https://devportal-stage.saas.bbpd.io/api/v1/management/applications/4c43e5f0-9eef-425f-bf7c-c81689013cb7/jwks.json',
        OIDC_TOKEN = 'https://devportal-stage.saas.bbpd.io/api/v1/gateway/oauth2/jwttoken',
        OIDC_AUTH = 'https://devportal-stage.saas.bbpd.io/api/v1/gateway/oidcauth',
        ALLOWED_TOOLS = 'sakai.resources',
        NEW_WINDOW_TOOLS = 'sakai.site:sakai.site.roster2',
        TRUST_EMAIL = 1,
        VERBOSE = 1
    WHERE TENNANT_GUID='54321';


    UPDATE PLUS_TENANT SET
        ALLOWED_TOOLS = 'sakai.site:sakai.resources:sakai.lessonbuildertool:sakai.conversations:sakai.assignment.grades:sakai.mycalendar:sakai.podcasts:sakai.poll:sakai.dropbox:sakai.mailbox:sakai.chat:sakai.postem:sakai.site.roster2:sakai.samigo',
        NEW_WINDOW_TOOLS = 'sakai.site:sakai.site.roster2',
        TRUST_EMAIL = 1,
        VERBOSE = 1,
        DELETED = 0,
        SUCCESS = 0
    ;

    INSERT INTO PLUS_TENANT
    (TENNANT_GUID, TITLE, ISSUER, OIDC_REGISTRATION_LOCK)
    VALUES
    ('123', 'Local Moodle', 'http://localhost:8888/moodle', '42');

    http://localhost:8080/plus/sakai/dynamic/123?unlock_token=42

    DROP TABLE PLUS_SCORE;
    DROP TABLE PLUS_LINEITEM;
    DROP TABLE PLUS_LINK;
    DROP TABLE PLUS_SUBJECT;
    DROP TABLE PLUS_CONTEXT;

    App Key (REST): 7cbbfd88-------REST-----ONLY----6ddc
    Secret(REST): ijWQ------REST----ONLY----fkn4U6
    ClientId: 4c43e5f0-9eef-425f-bf7c-c81689013cb7
    https://blackboard.com
    https://devportal-stage.saas.bbpd.io/api/v1/management/applications/4c43e5f0-9eef-425f-bf7c-c81689013cb7/jwks.json
    https://devportal-stage.saas.bbpd.io/api/v1/gateway/oauth2/jwttoken
    https://devportal-stage.saas.bbpd.io/api/v1/gateway/oidcauth

    mysql> describe PLUS_TENANT;
    +----------------------------+---------------+------+-----+---------+-------+
    | Field                      | Type          | Null | Key | Default | Extra |
    +----------------------------+---------------+------+-----+---------+-------+
    | TENNANT_GUID               | varchar(36)   | NO   | PRI | NULL    |       |
    | CACHE_KEYSET               | varchar(4000) | YES  |     | NULL    |       |
    | CLIENT_ID                  | varchar(200)  | YES  |     | NULL    |       |
    | DEPLOYMENT_ID              | varchar(200)  | YES  |     | NULL    |       |
    | DESCRIPTION                | varchar(4000) | YES  |     | NULL    |       |
    | ISSUER                     | varchar(200)  | YES  | MUL | NULL    |       |
    | OIDC_AUDIENCE              | varchar(200)  | YES  |     | NULL    |       |
    | OIDC_AUTH                  | varchar(500)  | YES  |     | NULL    |       |
    | OIDC_KEYSET                | varchar(500)  | YES  |     | NULL    |       |
    | OIDC_TOKEN                 | varchar(500)  | YES  |     | NULL    |       |
    | TITLE                      | varchar(500)  | NO   |     | NULL    |       |
    | TRUST_EMAIL                | bit(1)        | YES  |     | NULL    |       |
    | ALLOWED_TOOLS              | varchar(500)  | YES  |     | NULL    |       |
    | TIMEZONE                   | varchar(100)  | YES  |     | NULL    |       |
    | VERBOSE                    | bit(1)        | YES  |     | NULL    |       |
    | CREATED_AT                 | datetime      | YES  |     | NULL    |       |
    | DEBUG_LOG                  | longtext      | YES  |     | NULL    |       |
    | DELETED                    | bit(1)        | YES  |     | NULL    |       |
    | DELETED_AT                 | datetime      | YES  |     | NULL    |       |
    | DELETOR                    | varchar(99)   | YES  |     | NULL    |       |
    | JSON                       | longtext      | YES  |     | NULL    |       |
    | LOGIN_AT                   | datetime      | YES  |     | NULL    |       |
    | LOGIN_COUNT                | int(11)       | YES  |     | NULL    |       |
    | LOGIN_IP                   | varchar(64)   | YES  |     | NULL    |       |
    | LOGIN_USER                 | varchar(99)   | YES  |     | NULL    |       |
    | MODIFIED_AT                | datetime      | YES  |     | NULL    |       |
    | MODIFIER                   | varchar(99)   | YES  |     | NULL    |       |
    | SENT_AT                    | datetime      | YES  |     | NULL    |       |
    | STATUS                     | varchar(200)  | YES  |     | NULL    |       |
    | SUCCESS                    | bit(1)        | YES  |     | NULL    |       |
    | UPDATED_AT                 | datetime      | YES  |     | NULL    |       |
    | OIDC_REGISTRATION          | longtext      | YES  |     | NULL    |       |
    | OIDC_REGISTRATION_ENDPOINT | varchar(500)  | YES  |     | NULL    |       |
    | OIDC_REGISTRATION_LOCK     | varchar(200)  | YES  |     | NULL    |       |
    +----------------------------+---------------+------+-----+---------+-------+
    34 rows in set (0.01 sec)

