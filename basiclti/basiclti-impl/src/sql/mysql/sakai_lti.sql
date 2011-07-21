
CREATE TABLE lti_tools (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT(4096) NOT NULL,
    toolurl VARCHAR(255) NOT NULL,
    resourcekey VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    SITE_ID VARCHAR(99),
    preferheight INT,
    allowpreferheight SMALLINT DEFAULT '0',
    launchinpopup SMALLINT DEFAULT '0',
    debuglaunch SMALLINT DEFAULT '0',
    sendname SMALLINT DEFAULT '0',
    sendemailaddr SMALLINT DEFAULT '0',
    allowroster SMALLINT DEFAULT '0',
    allowsetting SMALLINT DEFAULT '0',
    allowcustomparameters SMALLINT DEFAULT '0',
    customparameters TEXT(1024),
    organizationid VARCHAR(255),
    organizationurl VARCHAR(255),
    organizationdescr VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

        status mediumint(1) NOT NULL DEFAULT '0',
        
        PRIMARY KEY ( id)
);

CREATE TABLE lti_content (
    id INT NOT NULL AUTO_INCREMENT,
    tool_id INT,
    SITE_ID VARCHAR(99),
    title VARCHAR(255) NOT NULL,
    preferheight INT,
    launchinpopup SMALLINT DEFAULT '0',
    debuglaunch SMALLINT DEFAULT '0',
    customparameters TEXT(1024),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

	placementsecret varchar(1023),
	timeplacementsecret mediumint(10) NOT NULL DEFAULT '0',
	oldplacementsecret varchar(1023),
	setting text(8192),
	xmlimport text(16384),
	PRIMARY KEY ( id, tool_id)
);

CREATE TABLE lti_mapping (
    id INT NOT NULL AUTO_INCREMENT,
    matchpattern VARCHAR(255) NOT NULL,
    toolurl VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY ( id, matchpattern )
);

