
    create table lesson_builder_comments (
        id numeric(19,0) identity not null,
        itemId numeric(19,0) not null,
        pageId numeric(19,0) not null,
        timePosted datetime not null,
        author varchar(36) not null,
        commenttext text null,
        UUID varchar(36) not null,
        html tinyint not null,
        points double precision null,
        primary key (id)
    );

    create table lesson_builder_groups (
        id numeric(19,0) identity not null,
        itemId varchar(255) not null,
        groupId varchar(255) not null,
        groups varchar(500) null,
        primary key (id)
    );

    create table lesson_builder_items (
        id numeric(19,0) identity not null,
        pageId numeric(19,0) not null,
        sequence int not null,
        type int not null,
        sakaiId varchar(250) null,
        name varchar(100) null,
        html text null,
        description varchar(500) null,
        height varchar(8) null,
        width varchar(8) null,
        alt varchar(500) null,
        nextPage tinyint null,
        format varchar(255) null,
        required tinyint null,
        alternate tinyint null,
        prerequisite tinyint null,
        subrequirement tinyint null,
        requirementText varchar(20) null,
        sameWindow tinyint null,
        groups varchar(500) null,
        anonymous tinyint null,
        showComments tinyint null,
        forcedCommentsAnonymous tinyint null,
        gradebookId varchar(35) null,
        gradebookPoints int null,
        gradebookTitle varchar(200) null,
        altGradebook varchar(35) null,
        altPoints int null,
        altGradebookTitle varchar(200) null,
        primary key (id)
    );

    create table lesson_builder_log (
        id numeric(19,0) identity not null,
        lastViewed datetime not null,
        itemId numeric(19,0) not null,
        userId varchar(255) not null,
        firstViewed datetime not null,
        complete tinyint not null,
        dummy tinyint not null,
        path varchar(255) null,
        toolId varchar(250) null,
        studentPageId numeric(19,0) null,
        primary key (id)
    );

    create table lesson_builder_pages (
        pageId numeric(19,0) identity not null,
        toolId varchar(250) not null,
        siteId varchar(250) not null,
        title varchar(100) not null,
        parent numeric(19,0) null,
        topParent numeric(19,0) null,
        hidden tinyint null,
        releaseDate datetime null,
        gradebookPoints double precision null,
        owner varchar(36) null,
        groupOwned tinyint null,
        cssSheet varchar(250) null,
        primary key (pageId)
    );

    create table lesson_builder_student_pages (
        id numeric(19,0) identity not null,
        lastUpdated datetime not null,
        itemId numeric(19,0) not null,
        pageId numeric(19,0) not null,
        title varchar(100) not null,
        owner varchar(36) not null,
        groupOwned tinyint not null,
        commentsSection numeric(19,0) null,
        lastCommentChange datetime null,
        deleted tinyint null,
        points double precision null,
        primary key (id)
    );
