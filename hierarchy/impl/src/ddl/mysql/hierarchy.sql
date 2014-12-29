
    create table HIERARCHY_NODE (
        ID bigint not null auto_increment,
        directParentIds text,
        parentIds text,
        directChildIds blob,
        childIds blob,
        primary key (ID)
    );

    create table HIERARCHY_NODE_META (
        ID bigint not null auto_increment,
        hierarchyId varchar(255),
        isRootNode bit not null,
        ownerId varchar(99),
        title varchar(255),
        description text,
        permToken varchar(255),
        isDisabled bit not null,
        primary key (ID)
    );

    create table HIERARCHY_PERMS (
        ID bigint not null auto_increment,
        createdOn datetime not null,
        lastModified datetime not null,
        userId varchar(99) not null,
        nodeId varchar(255) not null,
        permission varchar(255) not null,
        primary key (ID)
    );

    create index HIERARCHY_PERMTOKEN on HIERARCHY_NODE_META (permToken);

    create index HIERARCHY_HID on HIERARCHY_NODE_META (hierarchyId);

    create index HIER_PERM_USER on HIERARCHY_PERMS (userId);

    create index HIER_PERM_NODE on HIERARCHY_PERMS (nodeId);

    create index HIER_PERM_PERM on HIERARCHY_PERMS (permission);
