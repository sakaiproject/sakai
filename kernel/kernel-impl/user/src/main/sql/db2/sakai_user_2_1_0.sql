-----------------------------------------------------------------------------
-- Clear the password field for the postmaster if it has not yet been changed
-----------------------------------------------------------------------------
UPDATE SAKAI_USER SET PW='' WHERE USER_ID='postmaster' AND PW='ISMvKXpXpadDiUoOSoAf'
;