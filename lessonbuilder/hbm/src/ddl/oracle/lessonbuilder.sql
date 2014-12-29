
    create table lesson_builder_comments (
        id number(19,0) not null,
        itemId number(19,0) not null,
        pageId number(19,0) not null,
        timePosted timestamp not null,
        author varchar2(99 char) not null,
        commenttext clob,
        UUID varchar2(36 char) not null,
        html number(1,0) not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_groups (
        id number(19,0) not null,
        itemId varchar2(255 char) not null,
        groupId varchar2(99 char) not null,
        groups clob,
        siteId varchar2(99 char),
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
        description clob,
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
        groups clob,
        anonymous number(1,0),
        showComments number(1,0),
        forcedCommentsAnonymous number(1,0),
        showPeerEval number(1,0),
        gradebookId varchar2(100 char),
        gradebookPoints number(10,0),
        gradebookTitle varchar2(200 char),
        altGradebook varchar2(100 char),
        altPoints number(10,0),
        altGradebookTitle varchar2(200 char),
        groupOwned number(1,0),
        ownerGroups clob,
        attributeString clob,
        primary key (id)
    );

    create table lesson_builder_log (
        id number(19,0) not null,
        lastViewed timestamp not null,
        itemId number(19,0) not null,
        userId varchar2(99 char) not null,
        firstViewed timestamp not null,
        complete number(1,0) not null,
        dummy number(1,0) not null,
        path varchar2(255 char),
        toolId varchar2(99 char),
        studentPageId number(19,0),
        primary key (id)
    );

    create table lesson_builder_p_eval_results (
        PEER_EVAL_RESULT_ID number(19,0) not null,
        PAGE_ID number(19,0) not null,
        TIME_POSTED timestamp,
        GRADER varchar2(99 char) not null,
        GRADEE varchar2(99 char) not null,
        ROW_TEXT varchar2(255 char) not null,
        COLUMN_VALUE number(10,0) not null,
        SELECTED number(1,0),
        primary key (PEER_EVAL_RESULT_ID)
    );

    create table lesson_builder_pages (
        pageId number(19,0) not null,
        toolId varchar2(99 char) not null,
        siteId varchar2(99 char) not null,
        title varchar2(100 char) not null,
        parent number(19,0),
        topParent number(19,0),
        hidden number(1,0),
        releaseDate timestamp,
        gradebookPoints double precision,
        owner varchar2(99 char),
        groupOwned number(1,0),
        groupid varchar2(99 char),
        cssSheet varchar2(250 char),
        primary key (pageId)
    );

    create table lesson_builder_properties (
        id number(19,0) not null,
        attribute varchar2(255 char) not null unique,
        value clob,
        primary key (id)
    );

    create table lesson_builder_q_responses (
        id number(19,0) not null,
        timeAnswered timestamp not null,
        questionId number(19,0) not null,
        userId varchar2(99 char) not null,
        correct number(1,0) not null,
        shortanswer clob,
        multipleChoiceId number(19,0),
        originalText clob,
        overridden number(1,0) not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_qr_totals (
        id number(19,0) not null,
        questionId number(19,0),
        responseId number(19,0),
        respcount number(19,0),
        primary key (id)
    );

    create table lesson_builder_student_pages (
        id number(19,0) not null,
        lastUpdated timestamp not null,
        itemId number(19,0) not null,
        pageId number(19,0) not null,
        title varchar2(100 char) not null,
        owner varchar2(99 char) not null,
        groupOwned number(1,0) not null,
        groupid varchar2(99 char),
        commentsSection number(19,0),
        lastCommentChange timestamp,
        deleted number(1,0),
        points double precision,
        primary key (id)
    );

    create sequence LB_PEER_EVAL_RESULT_S;

    create sequence hibernate_sequence;
