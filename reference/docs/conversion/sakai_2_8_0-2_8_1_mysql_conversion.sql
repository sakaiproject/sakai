-- SAK-8005/SAK-20560
-- -- The conversion for SAK-8005 in 2.8.0 conversion do not handle the message_order data in the xml clob
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=REPLACE(XML, ' subject=', ' message_order="1" subject=') WHERE MESSAGE_ORDER IS NULL; 