create index SAM_ANSWERFEED_ANSWERID_I on SAM_ANSWERFEEDBACK_T (ANSWERID);
create index SAM_ANSWER_ITEMTEXTID_I on SAM_ANSWER_T (ITEMTEXTID);
create index SAM_ITEMFEED_ITEMID_I on SAM_ITEMFEEDBACK_T (ITEMID);
create index SAM_ITEMMETADATA_ITEMID_I on SAM_ITEMMETADATA_T (ITEMID);
create index SAM_ITEM_SECTIONID_I on SAM_ITEM_T (SECTIONID);
create index SAM_QPOOL_OWNER_I on SAM_QUESTIONPOOL_T (OWNERID);
