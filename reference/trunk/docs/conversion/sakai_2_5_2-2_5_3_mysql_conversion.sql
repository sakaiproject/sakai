-- SAK-11130 Localization breaks default folders in Messages because of internationalization bug
update MFR_TOPIC_T
set TITLE='pvt_received'
where TITLE in (
	'Received' /* en */, '\u0645\u0633\u062a\u0644\u0645' /* ar */,
	'Rebut' /* ca */, 'Recibidos' /* es */, 'Re\u00e7u' /* fr_CA */,
	'\u53d7\u4fe1\u3057\u307e\u3057\u305f' /* ja */, 'Ontvangen' /* nl */,
	'Recebidas' /* pt_BR */, 'Recebidas' /* pt_PT */,
	'Mottagna' /* sv */) and
    TYPE_UUID = (select uuid from CMN_TYPE_T where KEYWORD = 'privateForums');

update MFR_TOPIC_T
set TITLE='pvt_sent'
where TITLE in (
	'Sent' /* en */, '\u0623\u0631\u0633\u0644' /* ar */,
	'Enviat' /* ca */, 'Enviados' /* es */, 'Envoy\u00e9' /* fr_CA */,
	'\u9001\u4fe1\u3057\u307e\u3057\u305f' /* ja */, 'Verzonden' /* nl */,
	'Enviadas' /* pt_BR  */, 'Enviada' /* pt_PT  */,
	'Skickade' /* sv */) and
    TYPE_UUID = (select uuid from CMN_TYPE_T where KEYWORD = 'privateForums');

update MFR_TOPIC_T
set TITLE='pvt_deleted'
where TITLE in (
	'Deleted' /* en */, '\u062d\u0630\u0641' /* ar */,
	'Suprimit' /* ca */, 'Borrados' /* es */, 'Supprim\u00e9' /* fr_CA */,
	'\u524a\u9664\u3057\u307e\u3057\u305f' /* ja */, 'Verwijderd' /* nl */,
	'Apagadas' /* pt_BR */, 'Eliminadas' /* pt_PT */,
	'Borttagna' /* sv */) and
    TYPE_UUID = (select uuid from CMN_TYPE_T where KEYWORD = 'privateForums');

update MFR_TOPIC_T
set TITLE='pvt_drafts'
where TITLE in (
	'Drafts' /*en */, '\u0645\u0634\u0631\u0648\u0639' /* ar */,
	'Esborrany' /* ca */, 'Preliminar' /* es */, 'Brouillon' /* fr_CA */,
	'\u4e0b\u66f8\u304d' /* ja */, 'Concept' /* nl */,
	'Rascunho' /* pt_BR */, 'Rascunho' /* pt_PT */,
	'Utkast' /* sv */) and
    TYPE_UUID = (select uuid from CMN_TYPE_T where KEYWORD = 'privateForums');





-- ---------------------------------------------------------------------------
-- Fix http://bugs.sakaiproject.org/jira/browse/SAK-13842
-- 
-- Remove discussions and recent discussions from mercury and !worksite
-- 
-- Sites which still run discussions from contrib - can simply patch
-- this so it never runs - either by commenting out all this SQL
-- or by removing the call to this script in SiteService.
-- ---------------------------------------------------------------------------

-- Undo the hard-coded inserts

-- INSERT INTO SAKAI_SITE_TOOL VALUES('mercury-130', 'mercury-100', 
-- 'mercury', 'sakai.synoptic.discussion', 3, 'Recent Discussion Items', NULL );

DELETE FROM SAKAI_SITE_TOOL WHERE 
       TOOL_ID              = 'mercury-130' AND
       PAGE_ID              = 'mercury-100' AND 
       SITE_ID              = 'mercury' AND 
       REGISTRATION         = 'sakai.synoptic.discussion';

-- INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('mercury', 'mercury-510', 'category', 'false' );

DELETE FROM SAKAI_SITE_TOOL_PROPERTY WHERE
       SITE_ID = 'mercury' and
       TOOL_ID = 'mercury-510';

-- sakai_site.sql:INSERT INTO SAKAI_SITE_TOOL VALUES('mercury-510', 'mercury-500', 
-- 'mercury', 'sakai.discussion', 1, 'Discussion', NULL );

DELETE FROM SAKAI_SITE_TOOL WHERE 
       TOOL_ID              = 'mercury-510' AND
       PAGE_ID              = 'mercury-500' AND 
       SITE_ID              = 'mercury' AND 
       REGISTRATION         = 'sakai.discussion';

-- INSERT INTO SAKAI_SITE_TOOL VALUES('!worksite-130', '!worksite-100', 
-- '!worksite', 'sakai.synoptic.discussion', 3, 'Recent Discussion Items', NULL );

DELETE FROM SAKAI_SITE_TOOL WHERE 
       TOOL_ID              = '!worksite-130' AND
       PAGE_ID              = '!worksite-100' AND 
       SITE_ID              = '!worksite' AND 
       REGISTRATION         = 'sakai.synoptic.discussion';

-- INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!worksite', '!worksite-510', 'category', 'false' );

DELETE FROM SAKAI_SITE_TOOL_PROPERTY WHERE
       SITE_ID = '!worksite' and
       TOOL_ID = '!worksite-510';

-- sakai_site.sql:INSERT INTO SAKAI_SITE_TOOL VALUES('!worksite-510', '!worksite-500', 
--  '!worksite', 'sakai.discussion', 1, 'Discussion', NULL );

DELETE FROM SAKAI_SITE_TOOL WHERE 
       TOOL_ID              = '!worksite-510' AND
       PAGE_ID              = '!worksite-500' AND 
       SITE_ID              = '!worksite' AND 
       REGISTRATION         = 'sakai.discussion';

