
    create table lesson_builder_comments (
        id bigint not null auto_increment,
        itemId bigint not null,
        pageId bigint not null,
        timePosted datetime not null,
        author varchar(36) not null,
        commenttext text,
        UUID varchar(36) not null,
        html bit not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_groups (
        id bigint not null auto_increment,
        itemId varchar(255) not null,
        groupId varchar(255) not null,
        groups text,
        siteId varchar(250),
        primary key (id)
    );

    create table lesson_builder_items (
        id bigint not null auto_increment,
        pageId bigint not null,
        sequence integer not null,
        type integer not null,
        sakaiId varchar(250),
        name varchar(100),
        html mediumtext,
        description text,
        height varchar(8),
        width varchar(8),
        alt text,
        nextPage bit,
        format varchar(255),
        required bit,
        alternate bit,
        prerequisite bit,
        subrequirement bit,
        requirementText varchar(20),
        sameWindow bit,
        groups text,
        anonymous bit,
        showComments bit,
        forcedCommentsAnonymous bit,
        gradebookId varchar(35),
        gradebookPoints integer,
        gradebookTitle varchar(200),
        altGradebook varchar(35),
        altPoints integer,
        altGradebookTitle varchar(200),
        primary key (id)
    );

    create table lesson_builder_log (
        id bigint not null auto_increment,
        lastViewed datetime not null,
        itemId bigint not null,
        userId varchar(255) not null,
        firstViewed datetime not null,
        complete bit not null,
        dummy bit not null,
        path varchar(255),
        toolId varchar(250),
        studentPageId bigint,
        primary key (id)
    );

    create table lesson_builder_pages (
        pageId bigint not null auto_increment,
        toolId varchar(250) not null,
        siteId varchar(250) not null,
        title varchar(100) not null,
        parent bigint,
        topParent bigint,
        hidden bit,
        releaseDate datetime,
        gradebookPoints double precision,
        owner varchar(36),
        groupOwned bit,
        cssSheet varchar(250),
        primary key (pageId)
    );

    create table lesson_builder_student_pages (
        id bigint not null auto_increment,
        lastUpdated datetime not null,
        itemId bigint not null,
        pageId bigint not null,
        title varchar(100) not null,
        owner varchar(36) not null,
        groupOwned bit not null,
        commentsSection bigint,
        lastCommentChange datetime,
        deleted bit,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_properties (
        id bigint not null auto_increment,
        attribute varchar(255) not null unique,
        value longtext;
        primary key (id),
    );
