create table `users`(
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

create table `nonces`(
    nonceValue varchar(255) primary key,
    userId varchar(64),
    projectId int,
    time timestamp
);
create index `index_ndate` on nonces(time);

create table `pwdata`(
    userId varchar(64),
    email varchar(255),
    time timestamp
);
create index `index_pdate` on pwdata(time);

create table `rendezvous`(
    rkey varchar(64) primary key,
    ipaddr varchar(64),
    time timestamp
);

create table `groups`(
    groupId int primary key auto_increment,
    name varchar(255)
);

create table `gusers`(
    groupId int,
    userId varchar(64),
    primary key(groupId, userId)
);

create table `backpack`(
    backpackId varchar(255) primary key,
    content text
);

create table `build_status`(
    userId varchar(64),
    projectId int,
    progress tinyint,
    primary key(userId, projectId)
);