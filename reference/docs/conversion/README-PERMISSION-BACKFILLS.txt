# Per SAK-23613 this is an attempt to provide a correct template for performing 
# permissions backfills in Sakai CLE conversion scripts.


# First thing is to put the permissions we want to backfill into a temporary table.
# Every possible role and permission combination needs its own row.

CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','tool.permission.name');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','tool.permission.name');


# Second step will translate the text strings for role and tool permission
# into the integer keys stored in the Sakai realm tables.

CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
  SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
    from PERMISSIONS_SRC_TEMP TMPSRC
    JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
    JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);


# Third step will perform the actual permission backfill and will insert into SAKAI_REALM_RL_FN.
# This step will only run on existing realms that have the relevant role.
# NOTE: we ignore template realms like !site.helper or any of the !user.template.XXXX realms. 

INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
  SELECT SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
  FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper' AND SR.REALM_ID NOT LIKE '!user.template%'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );


# Final Step: cleanup the temp tables

DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;
