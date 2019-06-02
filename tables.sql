create database if not exists `appinventor`;
use appinventor;

create table if not exists `users`(
    userId varchar(64) primary key,
    email varchar(255), name varchar(255),
    visited timestamp,
    settings text,
    tosAccepted tinyint(1),
    isAdmin tinyint(1),
    sessionId varchar(255),
    password varchar(255)
);
create index `index_email` on users(email);

create table if not exists `nonces`(
    nonceValue varchar(255) primary key,
    userId varchar(64),
    projectId int,
    time timestamp
);
create index `index_ndate` on nonces(time);

create table if not exists `pwdata`(
    userId varchar(64),
    email varchar(255),
    time timestamp
);
create index `index_pdate` on pwdata(time);

create table if not exists `rendezvous`(
    rkey varchar(64) primary key,
    ipaddr varchar(64),
    time timestamp
);

create table if not exists `groups`(
    groupId int primary key auto_increment,
    name varchar(255)
);

create table if not exists `gusers`(
    groupId int,
    userId varchar(64),
    primary key(groupId, userId)
);

create table if not exists `backpack`(
    backpackId varchar(255) primary key,
    content text
);

create table if not exists `build_status`(
    userId varchar(64),
    projectId int,
    progress tinyint,
    primary key(userId, projectId)
);

create table if not exists courses
(
    courseId   int auto_increment primary key,
    courseName varchar(64) not null,
    adminId    varchar(64) not null,
    constraint course_users_userId_fk
        foreign key (adminId) references users (userId)
            on update cascade on delete cascade
);

create table if not exists classes
(
    courseId int         not null,
    userId   varchar(64) not null,
    primary key (courseId, userId),
    constraint class_course_courseId_fk
        foreign key (courseId) references courses (courseId)
            on update cascade on delete cascade,
    constraint class_users_userId_fk
        foreign key (userId) references users (userId)
            on update cascade on delete cascade
);

create table if not exists scores
(
    adminId     varchar(64)                             not null,
    projectId   int                                     not null,
    submitterId varchar(64)                             not null,
    courseId    int                                     not null,
    submitTime  timestamp default CURRENT_TIMESTAMP     null,
    score       int       default -1                    null,
    scoredTime  timestamp default '1970-01-01 08:00:01' null,
    similarity  float     default -1                    not null,
    primary key (adminId, projectId),
    constraint scores_courses_courseId_fk
        foreign key (courseId) references courses (courseId)
            on update cascade on delete cascade,
    constraint scores_users_userId_fk
        foreign key (adminId) references users (userId)
            on update cascade on delete cascade,
    constraint scores_users_userId_fk_2
        foreign key (submitterId) references users (userId)
            on update cascade on delete cascade
);
