-- MSGCNTR-449
-- Restores correct thread sorting
update MFR_MESSAGE_T set LASTTHREADATE = CREATED where THREADID is null and LASTTHREADATE is null;
