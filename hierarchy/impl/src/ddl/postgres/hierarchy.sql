
    create table HIERARCHY_NODE (
        ID int8 not null,
        directParentIds varchar(2000),
        parentIds varchar(4000),
        directChildIds varchar(2000),
        childIds varchar(4000),
        primary key (ID)
    );

    create table HIERARCHY_NODE_META (
        ID int8 not null,
        hierarchyId varchar(255),
        isRootNode bool not null,
        ownerId varchar(255),
        title varchar(255),
        description text,
        permToken varchar(255),
        isDisabled bool not null,
        primary key (ID)
    );

    create table HIERARCHY_PERMS (
        ID int8 not null,
        createdOn timestamp not null,
        lastModified timestamp not null,
        userId varchar(255) not null,
        nodeId varchar(255) not null,
        permission varchar(255) not null,
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
