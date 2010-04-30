# SAK-18306 - Adding additional grading scales to existing gradebooks
# Replace the XXXXX in the sql with the GB_GRADING_SCALE_T.ID for your new grading scale 
#

insert into GB_GRADE_MAP_T (OBJECT_TYPE_ID, VERSION, GRADEBOOK_ID, GB_GRADING_SCALE_T)
select distinct 0,0,GB_GRADEBOOK_T.ID,GB_GRADING_SCALE_T.ID
from GB_GRADEBOOK_T, GB_GRADING_SCALE_T
where (GB_GRADEBOOK_T.ID, GB_GRADING_SCALE_T.ID) NOT in (select distinct GRADEBOOK_ID, GB_GRADING_SCALE_T from GB_GRADE_MAP_T where GB_GRADING_SCALE_T=XXXXX)
AND GB_GRADING_SCALE_T.ID = XXXXX 