
    create table lesson_builder_comments (
        id number(19,0) not null,
        itemId number(19,0) not null,
        pageId number(19,0) not null,
        timePosted date not null,
        author varchar2(36) not null,
        commenttext clob,
        UUID varchar2(36) not null,
        html number(1,0) not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_groups (
        id number(19,0) not null,
        itemId varchar2(255) not null,
        groupId varchar2(255) not null,
        groups clob,
	siteId varchar2(250 char),
        primary key (id)
    );

    create table lesson_builder_items (
        id number(19,0) not null,
        pageId number(19,0) not null,
        sequence number(10,0) not null,
        type number(10,0) not null,
        sakaiId varchar2(250),
        name varchar2(100),
        html clob,
        description clob,
        height varchar2(8),
        width varchar2(8),
        alt varchar2(500),
        nextPage number(1,0),
        format varchar2(255),
        required number(1,0),
        alternate number(1,0),
        prerequisite number(1,0),
        subrequirement number(1,0),
        requirementText varchar2(20),
        sameWindow number(1,0),
        groups clob,
        anonymous number(1,0),
        showComments number(1,0),
        forcedCommentsAnonymous number(1,0),
        gradebookId varchar2(35),
        gradebookPoints number(10,0),
        gradebookTitle varchar2(200),
        altGradebook varchar2(35),
        altPoints number(10,0),
        altGradebookTitle varchar2(200),
        primary key (id)
    );

    create table lesson_builder_log (
        id number(19,0) not null,
        lastViewed date not null,
        itemId number(19,0) not null,
        userId varchar2(255) not null,
        firstViewed date not null,
        complete number(1,0) not null,
        dummy number(1,0) not null,
        path varchar2(255),
        toolId varchar2(250),
        studentPageId number(19,0),
        primary key (id)
    );

    create table lesson_builder_pages (
        pageId number(19,0) not null,
        toolId varchar2(250) not null,
        siteId varchar2(250) not null,
        title varchar2(100) not null,
        parent number(19,0),
        topParent number(19,0),
        hidden number(1,0),
        releaseDate date,
        gradebookPoints double precision,
        owner varchar2(36),
        groupOwned number(1,0),
        cssSheet varchar2(250),
        primary key (pageId)
    );

    create table lesson_builder_student_pages (
        id number(19,0) not null,
        lastUpdated date not null,
        itemId number(19,0) not null,
        pageId number(19,0) not null,
        title varchar2(100) not null,
        owner varchar2(36) not null,
        groupOwned number(1,0) not null,
        commentsSection number(19,0),
        lastCommentChange date,
        deleted number(1,0),
        points double precision,
        primary key (id)
    );

    create table lesson_builder_properties (
        id number(19,0) not null,
        attribute varchar2(255 char) not null unique,
        value clob,
        primary key (id)
    );

    create sequence hibernate_sequence;
