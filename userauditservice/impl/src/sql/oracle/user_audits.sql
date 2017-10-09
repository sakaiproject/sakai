-- SAK-23634 - new table to track user add/drop/update actions done in sites
CREATE TABLE user_audits_log (
	id NUMBER(19,0) NOT NULL,
	site_id VARCHAR2(99) NOT NULL,
	user_id VARCHAR2(99) NOT NULL,
	role_name VARCHAR2(99) NOT NULL,
	action_taken VARCHAR2(1) NOT NULL,
	audit_stamp TIMESTAMP NOT NULL,
	source VARCHAR2(1),
	action_user_id VARCHAR2(99),
	PRIMARY KEY(id)
);

CREATE INDEX user_audits_log_index ON user_audits_log (site_id);

CREATE SEQUENCE user_audits_log_seq;
