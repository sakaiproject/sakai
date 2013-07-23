ALTER TABLE lti_mapping MODIFY (     matchpattern VARCHAR2(255) );
ALTER TABLE lti_mapping MODIFY (     launch VARCHAR2(255) );
ALTER TABLE lti_content MODIFY (     title VARCHAR2(255) );
ALTER TABLE lti_tools MODIFY (     title VARCHAR2(255) );
ALTER TABLE lti_tools MODIFY (     launch VARCHAR2(1024) );
ALTER TABLE lti_tools MODIFY (     consumerkey VARCHAR2(255) );
ALTER TABLE lti_tools MODIFY (     secret VARCHAR2(255) );
