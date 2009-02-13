-- SAK-13474 Increase length of announcement channel to 255 chars
ALTER TABLE announcement_channel MODIFY (CHANNEL_ID VARCHAR2(255));
ALTER TABLE announcement_message MODIFY (CHANNEL_ID VARCHAR2(255)); 
