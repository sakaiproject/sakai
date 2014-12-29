
    create table HIERARCHY_NODE (
        ID number(19,0) not null,
        directParentIds varchar2(2000),
        parentIds varchar2(4000),
        directChildIds clob,
        childIds clob,
        primary key (ID)
    );

    create table HIERARCHY_NODE_META (
        ID number(19,0) not null,
        hierarchyId varchar2(255),
        isRootNode number(1,0) not null,
        ownerId varchar2(99),
        title varchar2(255),
        description clob,
        permToken varchar2(255),
        isDisabled number(1,0) not null,
        primary key (ID)
    );

    create table HIERARCHY_PERMS (
        ID number(19,0) not null,
        createdOn date not null,
        lastModified date not null,
        userId varchar2(255) not null,
        nodeId varchar2(255) not null,
        permission varchar2(255) not null,
        primary key (ID)
    );

    create index HIERARCHY_PERMTOKEN on HIERARCHY_NODE_META (permToken);

    create index HIERARCHY_HID on HIERARCHY_NODE_META (hierarchyId);

    create index HIER_PERM_USER on HIERARCHY_PERMS (userId);

    create index HIER_PERM_NODE on HIERARCHY_PERMS (nodeId);

    create index HIER_PERM_PERM on HIERARCHY_PERMS (permission);

    create sequence HIERARCHY_META_ID_SEQ;

    create sequence HIERARCHY_NODE_ID_SEQ;

    create sequence HIERARCHY_PERM_ID_SEQ;
