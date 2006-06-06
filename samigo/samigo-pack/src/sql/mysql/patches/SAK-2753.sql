-- SAK-2753 this patch is for those who uses mySQL DB and mySQL ConnectorJ  3.1.x. 

-- updating label and hint fields for True and False questions, from NULL to "". 

update SAM_ANSWER_T answer set answer.label="" where  answer.itemid in (select itemid from SAM_ITEM_T item where item.itemid = answer.itemid and item.typeid =4  );

update SAM_ITEM_T item set item.hint="" where  item.typeid =4 ;


update SAM_PUBLISHEDANSWER_T answer set answer.label="" where  answer.itemid in (select itemid from SAM_PUBLISHEDITEM_T item where item.itemid = answer.itemid and item.typeid =4  );


update SAM_PUBLISHEDITEM_T item set item.hint="" where  item.typeid =4 ;
