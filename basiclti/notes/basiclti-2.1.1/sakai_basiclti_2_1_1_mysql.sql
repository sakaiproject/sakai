ALTER TABLE lti_mapping MODIFY     matchpattern VARCHAR(255);
ALTER TABLE lti_mapping MODIFY     launch VARCHAR(255);
ALTER TABLE lti_content MODIFY     title VARCHAR(255);
ALTER TABLE lti_tools MODIFY     title VARCHAR(255);
ALTER TABLE lti_tools MODIFY     launch TEXT(1024);
ALTER TABLE lti_tools MODIFY     consumerkey VARCHAR(255);
ALTER TABLE lti_tools MODIFY     secret VARCHAR(255);
