
    create table lesson_builder_comments (
        id number(19,0) not null,
        itemId number(19,0) not null,
        pageId number(19,0) not null,
        timePosted timestamp not null,
        author varchar2(36 char) not null,
        commenttext clob,
        UUID varchar2(36 char) not null,
        html number(1,0) not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_groups (
        id number(19,0) not null,
        itemId varchar2(255 char) not null,
        groupId varchar2(255 char) not null,
        groups varchar2(500 char),
        primary key (id)
    );

    create table lesson_builder_items (
        id number(19,0) not null,
        pageId number(19,0) not null,
        sequence number(10,0) not null,
        type number(10,0) not null,
        sakaiId varchar2(250 char),
        name varchar2(100 char),
        html clob,
        description varchar2(500 char),
        height varchar2(8 char),
        width varchar2(8 char),
        alt varchar2(500 char),
        nextPage number(1,0),
        format varchar2(255 char),
        required number(1,0),
        alternate number(1,0),
        prerequisite number(1,0),
        subrequirement number(1,0),
        requirementText varchar2(20 char),
        sameWindow number(1,0),
        groups varchar2(500 char),
        anonymous number(1,0),
        showComments number(1,0),
        forcedCommentsAnonymous number(1,0),
        gradebookId varchar2(35 char),
        gradebookPoints number(10,0),
        gradebookTitle varchar2(200 char),
        altGradebook varchar2(35 char),
        altPoints number(10,0),
        altGradebookTitle varchar2(200 char),
        primary key (id)
    );

    create table lesson_builder_log (
        id number(19,0) not null,
        lastViewed timestamp not null,
        itemId number(19,0) not null,
        userId varchar2(255 char) not null,
        firstViewed timestamp not null,
        complete number(1,0) not null,
        dummy number(1,0) not null,
        path varchar2(255 char),
        toolId varchar2(250 char),
        studentPageId number(19,0),
        primary key (id)
    );

    create table lesson_builder_pages (
        pageId number(19,0) not null,
        toolId varchar2(250 char) not null,
        siteId varchar2(250 char) not null,
        title varchar2(100 char) not null,
        parent number(19,0),
        topParent number(19,0),
        hidden number(1,0),
        releaseDate timestamp,
        gradebookPoints double precision,
        owner varchar2(36 char),
        groupOwned number(1,0),
        cssSheet varchar2(250 char),
        primary key (pageId)
    );

    create table lesson_builder_student_pages (
        id number(19,0) not null,
        lastUpdated timestamp not null,
        itemId number(19,0) not null,
        pageId number(19,0) not null,
        title varchar2(100 char) not null,
        owner varchar2(36 char) not null,
        groupOwned number(1,0) not null,
        commentsSection number(19,0),
        lastCommentChange timestamp,
        deleted number(1,0),
        points double precision,
        primary key (id)
    );

    create sequence hibernate_sequence;
