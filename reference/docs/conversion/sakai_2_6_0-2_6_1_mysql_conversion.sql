--SAK-16548 - Incorrect internationalization showing the grade NO GRADE

-- Note these are all mostly hex replacements as I could not find a better way to do it in mysql like in oracle.
--assignment_zh_CN.properties
update assignment_submission set xml = unhex(replace(hex(xml),'E697A0E8AF84E58886',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_ar.properties
update assignment_submission set xml = unhex(replace(hex(xml),'D984D8A720D8AAD988D8ACD8AF20D8A3D98A20D8AFD8B1D8ACD8A92E',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_pt_BR.properties
update assignment_submission set xml = unhex(replace(hex(xml),'4E656E68756D61204176616C6961C3A7C3A36F',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_es.properties 
update assignment_submission set xml = replace(xml,'No hay calificación','gen.nograd') where xml like '%graded="true"%';    
--assignment_ko.properties
update assignment_submission set xml = unhex(replace(hex(xml),'ED9599ECA09020EC9786EC9D8C',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_eu.propertie
update assignment_submission set xml = replace(xml,'Kalifikatu gabe',hex('gen.nograd')) where xml like '%graded="true"%';    
--assignment_nl.properties
update assignment_submission set xml = replace(xml,'Zonder beoordeling',hex('gen.nograd')) where xml like '%graded="true"%';    
--assignment_fr_CA.properties
update assignment_submission set xml = replace(xml,'Aucune note',hex('gen.nograd')) where xml like '%graded="true"%';    
--assignment_en_GB.properties
update assignment_submission set xml = replace(xml,'No Mark',hex('gen.nograd')) where xml like '%graded="true"%';    
--assignment.properties
update assignment_submission set xml = replace(xml,'No Grade',hex('gen.nograd')) where xml like '%graded="true"%';    
--assignment_ca.properties
update assignment_submission set xml = unhex(replace(hex(xml),'4E6F206861792063616C69666963616369C3B36E',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_pt_PT.properties
update assignment_submission set xml = unhex(replace(hex(xml),'53656D206176616C6961C3A7C3A36F',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_ru.properties
update assignment_submission set xml = unhex(replace(hex(xml),'D091D0B5D0B720D0BED186D0B5D0BDD0BAD0B8',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_sv.properties
update assignment_submission set xml = unhex(replace(hex(xml),'426574796773C3A474747320656A',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_ja.properties
update assignment_submission set xml = unhex(replace(hex(xml),'E68EA1E782B9E38197E381AAE38184',hex('gen.nograd'))) where xml like '%graded="true"%';    
--assignment_zh_TW.properties
update assignment_submission set xml = unhex(replace(hex(xml),'E6B292E69C89E8A995E58886',hex('gen.nograd'))) where xml like '%graded="true"%';    

-- SAK-16847  asn.share.drafts permission should be added into 2.6.1 conversion script
-- This might have been added with the 2.6.0 conversion but was added after release

-- Don't do anything if the function already exists 
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'asn.share.drafts') on duplicate key update function_name=function_name;

-- Two recent Jira's (http://jira.sakaiproject.org/browse/SAK-17061) and (http://jira.sakaiproject.org/browse/PRFL-97) 
-- have uncovered that when the field 'locked' was added to the SAKAI_PERSON_T a while ago 
-- (before 2.5.0 was cut), there was no DB upgrade script added to upgrade existing entries. 

-- Here is the Jira that added the locked field: http://jira.sakaiproject.org/browse SAK-10512

--As such, this field is null for old profiles. Its set correctly for any new profiles but all old entries need to be converted.

update SAKAI_PERSON_T set locked=false where locked=null; 
