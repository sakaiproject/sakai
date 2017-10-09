-- SAK-23634 - new table to track user add/drop/update actions done in sites
CREATE TABLE user_audits_log (
	id BIGINT AUTO_INCREMENT NOT NULL,
	site_id VARCHAR(99) NOT NULL,
	user_id VARCHAR(99) NOT NULL,
	role_name VARCHAR(99) NOT NULL,
	action_taken VARCHAR(1) NOT NULL,
	audit_stamp TIMESTAMP NOT NULL,
	source VARCHAR(1),
	action_user_id VARCHAR(99),
	PRIMARY KEY(id)
);

CREATE INDEX user_audits_log_index ON user_audits_log (site_id);
