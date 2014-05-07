
    create table lesson_builder_comments (
        id bigint not null auto_increment,
        itemId bigint not null,
        pageId bigint not null,
        timePosted datetime not null,
        author varchar(99) not null,
        commenttext longtext,
        UUID varchar(36) not null,
        html bit not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_groups (
        id bigint not null auto_increment,
        itemId varchar(255) not null,
        groupId varchar(99) not null,
        groups longtext,
        siteId varchar(99),
        primary key (id)
    );

    create table lesson_builder_items (
        id bigint not null auto_increment,
        pageId bigint not null,
        sequence integer not null,
        type integer not null,
        sakaiId varchar(250),
        name varchar(100),
        html longtext,
        description longtext,
        height varchar(8),
        width varchar(8),
        alt longtext,
        nextPage bit,
        format varchar(255),
        required bit,
        alternate bit,
        prerequisite bit,
        subrequirement bit,
        requirementText varchar(20),
        sameWindow bit,
        groups longtext,
        anonymous bit,
        showComments bit,
        forcedCommentsAnonymous bit,
        showPeerEval bit,
        gradebookId varchar(100),
        gradebookPoints integer,
        gradebookTitle varchar(200),
        altGradebook varchar(100),
        altPoints integer,
        altGradebookTitle varchar(200),
        groupOwned bit,
        ownerGroups longtext,
        attributeString longtext,
        primary key (id)
    );

    create table lesson_builder_log (
        id bigint not null auto_increment,
        lastViewed datetime not null,
        itemId bigint not null,
        userId varchar(99) not null,
        firstViewed datetime not null,
        complete bit not null,
        dummy bit not null,
        path varchar(255),
        toolId varchar(99),
        studentPageId bigint,
        primary key (id)
    );

    create table lesson_builder_p_eval_results (
        PEER_EVAL_RESULT_ID bigint not null auto_increment,
        PAGE_ID bigint not null,
        TIME_POSTED datetime,
        GRADER varchar(99) not null,
        GRADEE varchar(99) not null,
        ROW_TEXT varchar(255) not null,
        COLUMN_VALUE integer not null,
        SELECTED bit,
        primary key (PEER_EVAL_RESULT_ID)
    );

    create table lesson_builder_pages (
        pageId bigint not null auto_increment,
        toolId varchar(99) not null,
        siteId varchar(99) not null,
        title varchar(100) not null,
        parent bigint,
        topParent bigint,
        hidden bit,
        releaseDate datetime,
        gradebookPoints double precision,
        owner varchar(99),
        groupOwned bit,
        groupid varchar(99),
        cssSheet varchar(250),
        primary key (pageId)
    );

    create table lesson_builder_properties (
        id bigint not null auto_increment,
        attribute varchar(255) not null unique,
        value longtext,
        primary key (id)
    );

    create table lesson_builder_q_responses (
        id bigint not null auto_increment,
        timeAnswered datetime not null,
        questionId bigint not null,
        userId varchar(99) not null,
        correct bit not null,
        shortanswer longtext,
        multipleChoiceId bigint,
        originalText longtext,
        overridden bit not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_qr_totals (
        id bigint not null auto_increment,
        questionId bigint,
        responseId bigint,
        respcount bigint,
        primary key (id)
    );

    create table lesson_builder_student_pages (
        id bigint not null auto_increment,
        lastUpdated datetime not null,
        itemId bigint not null,
        pageId bigint not null,
        title varchar(100) not null,
        owner varchar(99) not null,
        groupOwned bit not null,
        groupid varchar(99),
        commentsSection bigint,
        lastCommentChange datetime,
        deleted bit,
        points double precision,
        primary key (id)
    );
