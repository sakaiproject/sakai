-- SAK-30667 Polls fails on Oracle migrated database
-- You should execute this only if you have not the column poll_poll.poll_details in CLOB datatype
ALTER TABLE poll_poll ADD (tmpdetails CLOB);
UPDATE poll_poll SET tmpdetails=poll_details;
ALTER TABLE poll_poll DROP COLUMN poll_details;
ALTER TABLE poll_poll RENAME COLUMN tmpdetails TO poll_details;
-- END SAK-30667 Polls fails on Oracle migrated database
