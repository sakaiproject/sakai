-- Site related tables changes needed after 2.1.0.003
ALTER TABLE SAKAI_SITE_PAGE ADD (POPUP CHAR(1) DEFAULT '0' CHECK (POPUP IN (1, 0)));
