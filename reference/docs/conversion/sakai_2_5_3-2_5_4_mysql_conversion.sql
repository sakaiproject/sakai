-- SAK-13474 Increase length of announcement channel to 255 chars
ALTER TABLE announcement_channel MODIFY COLUMN CHANNEL_ID VARCHAR(255);
ALTER TABLE announcement_message MODIFY COLUMN CHANNEL_ID VARCHAR(255);
