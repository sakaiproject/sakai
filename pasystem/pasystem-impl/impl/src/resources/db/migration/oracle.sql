CREATE TABLE pasystem_popup_screens (
  uuid char(36) PRIMARY KEY ,
  descriptor varchar2(255),
  start_time NUMBER,
  end_time NUMBER,
  open_campaign number(1) DEFAULT NULL
);

CREATE INDEX popup_screen_descriptor on pasystem_popup_screens (descriptor);
CREATE INDEX popup_screen_start_time on pasystem_popup_screens (start_time);
CREATE INDEX popup_screen_end_time on pasystem_popup_screens (end_time);


CREATE TABLE pasystem_popup_content (
  uuid char(36),
  template_content CLOB,
  CONSTRAINT popup_content_uuid_fk FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);


CREATE TABLE pasystem_popup_assign (
  uuid char(36),
  user_id varchar2(99) DEFAULT NULL,
  CONSTRAINT popup_assign_uuid_fk FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);

CREATE INDEX popup_assign_lower_user_id on pasystem_popup_assign (user_id);

CREATE TABLE pasystem_popup_dismissed (
  uuid char(36),
  user_id varchar2(99) DEFAULT NULL,
  state varchar2(50) DEFAULT NULL,
  dismiss_time NUMBER,
  CONSTRAINT popup_dismissed_uuid_fk FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid),
  CONSTRAINT popup_dismissed_unique UNIQUE (user_id, state, uuid)
);

CREATE INDEX popup_dismissed_lower_user_id on pasystem_popup_dismissed (user_id);
CREATE INDEX popup_dismissed_state on pasystem_popup_dismissed (state);

CREATE TABLE pasystem_banner_alert
( uuid CHAR(36) NOT NULL PRIMARY KEY,
  message VARCHAR2(4000) NOT NULL,
  hosts VARCHAR2(512),
  active NUMBER(1,0) DEFAULT 0 NOT NULL,
  start_time NUMBER,
  end_time NUMBER,
  banner_type VARCHAR2(255) DEFAULT 'warning'
);


MERGE INTO SAKAI_REALM_FUNCTION srf
USING (
SELECT -123 as function_key,
'pasystem.manage' as function_name
FROM dual
) t on (srf.function_name = t.function_name)
WHEN NOT MATCHED THEN
INSERT (function_key, function_name)
VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, t.function_name);


CREATE TABLE pasystem_banner_dismissed (
  uuid char(36),
  user_id varchar2(99) DEFAULT NULL,
  state varchar2(50) DEFAULT NULL,
  dismiss_time NUMBER,
  CONSTRAINT banner_dismissed_uuid_fk FOREIGN KEY (uuid) REFERENCES pasystem_banner_alert(uuid),
  CONSTRAINT banner_dismissed_unique UNIQUE (user_id, state, uuid)
);

CREATE INDEX banner_dismissed_user_id on pasystem_banner_dismissed (user_id);
CREATE INDEX banner_dismissed_state on pasystem_banner_dismissed (state);
