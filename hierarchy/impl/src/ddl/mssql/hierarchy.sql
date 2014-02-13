
    create table HIERARCHY_NODE (
        ID numeric(19,0) identity not null,
        directParentIds varchar(2000) null,
        parentIds varchar(4000) null,
        directChildIds varchar(2000) null,
        childIds varchar(4000) null,
        primary key (ID)
    );

    create table HIERARCHY_NODE_META (
        ID numeric(19,0) identity not null,
        hierarchyId varchar(255) null,
        isRootNode tinyint not null,
        ownerId varchar(255) null,
        title varchar(255) null,
        description text null,
        permToken varchar(255) null,
        isDisabled tinyint not null,
        primary key (ID)
    );

    create table HIERARCHY_PERMS (
        ID numeric(19,0) identity not null,
        createdOn datetime not null,
        lastModified datetime not null,
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
