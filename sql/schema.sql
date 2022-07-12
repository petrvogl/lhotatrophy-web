
-- USER TABLE

create table `user` (
       id bigint not null auto_increment,
        active bit not null,
        created bigint not null,
        email varchar(255) not null,
        password varchar(255) not null,
        privileged bit not null,
        properties TEXT,
        primary key (id)
) engine=InnoDB

alter table `user`
       add constraint UK_ob8kqyqqgmefl0aco34akdtpe unique (email)


-- TEAM TABLE

create table team (
       id bigint not null auto_increment,
        active bit not null,
        name varchar(255) not null,
        owner_id bigint not null,
        primary key (id)
) engine=InnoDB

alter table team 
       add constraint UK_frjxgaag9ypwhjyf7y3p5o3of unique (owner_id)

alter table team 
       add constraint UK_g2l9qqsoeuynt4r5ofdt1x2td unique (name)

alter table team 
       add constraint FK27p5xb3sfn8v7o34ve0b0wy2k 
       foreign key (owner_id) 
       references `user` (id)


-- TEAM_MEMBER TABLE

create table team_member (
       id bigint not null auto_increment,
        name varchar(255) not null,
        primary key (id)
) engine=InnoDB




