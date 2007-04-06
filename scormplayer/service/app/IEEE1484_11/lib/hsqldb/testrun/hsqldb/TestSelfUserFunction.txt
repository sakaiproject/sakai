--
-- TestSelfUserFunction.txt
--
-- This test checks if the USER() function works correctly when used within
-- prepared statements such as VIEW's and constraints in TABLE's

-- Setup tables and views
DROP TABLE USER_PROFILE IF EXISTS
CREATE TABLE USER_PROFILE(NAME VARCHAR(10), PROFILE INT, CHECK(USER() = NAME))
DROP VIEW USER_SECURITY_PROFILE_VIEW IF EXISTS
CREATE VIEW USER_SECURITY_PROFILE_VIEW AS SELECT * FROM USER_PROFILE WHERE Name = USER()

-- Create user for test
CREATE USER MATT PASSWORD MATT ADMIN

-- This checks that you are allowed to insert a row as long as your username matches the NAME field
/*u1*/ INSERT INTO USER_PROFILE(NAME, PROFILE) VALUES('SA',10)

-- This checks that you aren't allowed to insert a row as a different user
/*e*/  INSERT INTO USER_PROFILE(NAME, PROFILE) VALUES('MATT',100)

-- Connect as MATT
CONNECT USER MATT PASSWORD MATT

-- Insert a row as the connected user
/*u1*/ INSERT INTO USER_PROFILE(NAME, PROFILE) VALUES('MATT',20)

-- There are two rows in the table but the select should only return the row
-- associated with the connected user i.e. one row
/*c1*/ SELECT COUNT(*) FROM USER_SECURITY_PROFILE_VIEW

