--Signup-60
-- add the UUID columns
ALTER TABLE 'signup_meetings' ADD 'vevent_uuid' VARCHAR(255)  NULL;
ALTER TABLE 'signup_ts' ADD 'vevent_uuid' VARCHAR(255)  NULL;