
alter table searchbuilderitem add index isearchbuilderitem_name (name);
alter table searchbuilderitem add index isearchbuilderitem_ctx (context);
alter table searchbuilderitem add index isearchbuilderitem_act (searchaction);
alter table searchbuilderitem add index isearchbuilderitem_sta (searchstate);

