CREATE TABLE pasystem_popup_screens (
  uuid CHAR(36) PRIMARY KEY,
  descriptor VARCHAR(255),
  start_time BIGINT,
  end_time BIGINT,
  open_campaign INT DEFAULT NULL
);

CREATE TABLE pasystem_popup_content (
  uuid char(36) PRIMARY KEY,
  template_content LONGVARCHAR,
  FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);

CREATE TABLE pasystem_popup_assign (
  uuid char(36),
  user_id varchar(99) DEFAULT NULL,
   FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);

CREATE TABLE pasystem_popup_dismissed (
  uuid char(36),
  user_id varchar(99) DEFAULT NULL,
  state varchar(50) DEFAULT NULL,
  dismiss_time BIGINT,
   FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);

ALTER TABLE pasystem_popup_dismissed add constraint unique_popup_dismissed unique (user_id,state, uuid);

CREATE TABLE pasystem_banner_alert
( uuid CHAR(36) PRIMARY KEY,
  message VARCHAR(4000) NOT NULL,
  hosts VARCHAR(512) DEFAULT NULL,
  active INT DEFAULT 0,
  dismissible INT DEFAULT 1,
  start_time BIGINT DEFAULT NULL,
  end_time BIGINT DEFAULT NULL,
  banner_type VARCHAR(255) DEFAULT 'warning'
);

CREATE TABLE pasystem_banner_dismissed (
  uuid char(36),
  user_id varchar(99) DEFAULT NULL,
  state varchar(50) DEFAULT NULL,
  dismiss_time BIGINT,
   FOREIGN KEY (uuid) REFERENCES pasystem_banner_alert(uuid)
);

ALTER TABLE pasystem_banner_dismissed add constraint unique_banner_dismissed unique (user_id, state, uuid);

INSERT INTO SAKAI_REALM_FUNCTION (function_key, function_name) VALUES(NEXT VALUE FOR SAKAI_REALM_FUNCTION_SEQ, 'pasystem.manage');
