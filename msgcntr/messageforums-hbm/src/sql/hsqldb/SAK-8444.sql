-- SAK-8444
-- create a unique constraint on AREA table columns context id and type uuid
-- ---------------------------------------------------------------------------

alter table MFR_AREA_T add constraint MFR_AREA_CONTEXT_ID_TYPE_UUID_UNIQUE unique (CONTEXT_ID, TYPE_UUID);