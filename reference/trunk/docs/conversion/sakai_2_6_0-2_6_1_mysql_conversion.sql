-- SAK-16847  asn.share.drafts permission should be added into 2.6.1 conversion script
-- This might have been added with the 2.6.0 conversion but was added after release

-- Don't do anything if the function already exists 
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'asn.share.drafts') on duplicate key update function_name=function_name;

-- Two recent Jira's (http://jira.sakaiproject.org/browse/SAK-17061) and (http://jira.sakaiproject.org/browse/PRFL-97) 
-- have uncovered that when the field 'locked' was added to the SAKAI_PERSON_T a while ago 
-- (before 2.5.0 was cut), there was no DB upgrade script added to upgrade existing entries. 

-- Here is the Jira that added the locked field: http://jira.sakaiproject.org/browse SAK-10512

-- As such, this field is null for old profiles. Its set correctly for any new profiles but all old entries need to be converted.

update SAKAI_PERSON_T set locked=false where locked=null; 

-- SAK-16668
-- Note: If you upgraded to sakai 2.6.0 PRIOR TO September 1st 2009 you'll need to run this column conversion.
-- You should probably check your columns as they should be converted from varchar to text
-- (Or just run them anyway as it is safe to rerun, which is why they are uncommented)
ALTER TABLE ASN_MA_ITEM_T CHANGE TEXT TEXT TEXT;
ALTER TABLE ASN_NOTE_ITEM_T CHANGE NOTE NOTE TEXT;

-- SAK-16548 - Incorrect internationalization showing the grade NO GRADE (Speed improved version)

-- I would recommend making a backup of the ASSIGNMENT_SUBMISSION table prior to running this. I've tested it quite a bit in development, 
-- but we only ran the Oracle version in production at Michigan.

-- Note these are all mostly hex replacements as I could not find a better way to do it in mysql like in oracle.
-- Also mysql does not have a nice regular expression replace like oracle either so the replacement remains specific.
-- If you want, you can run Step 1 prior to a roll, and run Step 2 afterward. It will likely only match 1% or less of submissions. 
-- You don't need to bring down the server to run this, it can be run live. (The UI will just display a blank area instead of "No Grade" until the update finishes)

-- Step 1: Create table with the ids that will need to be updated
create table ASSIGNMENT_SUBMISSION_ID_TEMP (select submission_id from ASSIGNMENT_SUBMISSION where graded='true' and (
-- assignment.properties
	instr(xml,'"No Grade"') != 0 or
-- assignment_zh_CN.properties
	instr(hex(xml),'22E697A0E8AF84E5888622') != 0 or
-- assignment_ar.properties
	instr(hex(xml),'22D984D8A720D8AAD988D8ACD8AF20D8A3D98A20D8AFD8B1D8ACD8A92E22') != 0 or
-- assignment_pt_BR.properties
	instr(hex(xml),'224E656E68756D61204176616C6961C3A7C3A36F22') != 0 or
-- assignment_es.properties 
	instr(hex(xml),'224E6F206861792063616C69666963616369C3B36E22') != 0 or
-- assignment_ko.properties
	instr(hex(xml),'22ED9599ECA09020EC9786EC9D8C22') !=0 or 
-- assignment_eu.properties
	instr(xml,'"Kalifikatu gabe"') != 0 or
-- assignment_nl.properties
	instr(xml,'"Zonder beoordeling"') != 0 or
-- assignment_fr_CA.properties
	instr(xml,'"Aucune note"') != 0 or
-- assignment_en_GB.properties
	instr(xml,'"No Mark"') != 0 or
-- assignment_ca.properties
	instr(hex(xml),'224E6F206861792063616C69666963616369C3B36E22') != 0 or
-- assignment_pt_PT.properties
	instr(hex(xml),'2253656D206176616C6961C3A7C3A36F22') != 0 or
-- assignment_ru.properties
	instr(hex(xml),'22D091D0B5D0B720D0BED186D0B5D0BDD0BAD0B822') != 0 or
-- assignment_sv.properties
	instr(hex(xml),'22426574796773C3A474747320656A22') != 0 or
-- assignment_ja.properties
	instr(hex(xml),'22E68EA1E782B9E38197E381AAE3818422') != 0 or
-- assignment_zh_TW.properties
	instr(hex(xml),'22E6B292E69C89E8A995E5888622') != 0 
));

-- Step 2: Update all of the found fields using the table as a significant search limiter. This reduces the number of matches to very few in most instances.
-- This is only replacing on the previously filtered values so is faster than doing full table scans for the values and safer because less rows will be locked.
-- Ideally, the select statement would know which replacement it would need to do for each case, but that seemed difficult to write. In oracle this is accomplished with a 
-- regular expression. The update for Michigan with 2 million rows on Oracle was still minimal.
update ASSIGNMENT_SUBMISSION set 
-- assignment.properties
    xml = replace(xml,'"No Grade"','"gen.nograd"'), 
-- assignment_zh_CN.properties
    xml = unhex(replace(hex(xml),'22E697A0E8AF84E5888622',hex('"gen.nograd"'))), 
-- assignment_ar.properties
    xml = unhex(replace(hex(xml),'22D984D8A720D8AAD988D8ACD8AF20D8A3D98A20D8AFD8B1D8ACD8A92E22',hex('"gen.nograd"'))), 
-- assignment_pt_BR.properties
    xml = unhex(replace(hex(xml),'224E656E68756D61204176616C6961C3A7C3A36F22',hex('"gen.nograd"'))),
-- assignment_es.properties 
    xml = unhex(replace(hex(xml),'224E6F206861792063616C69666963616369C3B36E22',hex('"gen.nograd"'))),
-- assignment_ko.properties
    xml = unhex(replace(hex(xml),'22ED9599ECA09020EC9786EC9D8C22',hex('"gen.nograd"'))),
-- assignment_eu.properties
    xml = replace(xml,'"Kalifikatu gabe"','"gen.nograd"'),
-- assignment_eu.properties
    xml = replace(xml,'"Zonder beoordeling"','"gen.nograd"'),
-- assignment_fr_CA.properties
    xml = replace(xml,'"Aucune note"','"gen.nograd"'),
-- assignment_en_GB.properties
    xml = replace(xml,'"No Mark"','"gen.nograd"'),
-- assignment_ca.properties
    xml = unhex(replace(hex(xml),'224E6F206861792063616C69666963616369C3B36E22',hex('"gen.nograd"'))),
-- assignment_ru.properties
    xml = unhex(replace(hex(xml),'22D091D0B5D0B720D0BED186D0B5D0BDD0BAD0B822',hex('"gen.nograd"'))),
-- assignment_sv.properties
    xml = unhex(replace(hex(xml),'22426574796773C3A474747320656A22',hex('"gen.nograd"'))),
-- assignment_ja.properties
    xml = unhex(replace(hex(xml),'22E68EA1E782B9E38197E381AAE3818422',hex('"gen.nograd"'))),
-- assignment_zh_TW.properties
    xml = unhex(replace(hex(xml),'22E6B292E69C89E8A995E5888622',hex('"gen.nograd"')))
where submission_id in (select submission_id from ASSIGNMENT_SUBMISSION_ID_TEMP);

-- Step 3: Drop the temp table
drop table ASSIGNMENT_SUBMISSION_ID_TEMP;


