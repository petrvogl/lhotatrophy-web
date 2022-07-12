
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

alter table `user`
       add constraint UK_ob8kqyqqgmefl0aco34akdtpe unique (email);


-- TEAM TABLE

create table team (
       id bigint not null auto_increment,
        active bit not null,
        name varchar(255) not null,
        owner_id bigint not null,
        primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

alter table team 
       add constraint UK_frjxgaag9ypwhjyf7y3p5o3of unique (owner_id);

alter table team 
       add constraint UK_g2l9qqsoeuynt4r5ofdt1x2td unique (name);

alter table team 
       add constraint FK27p5xb3sfn8v7o34ve0b0wy2k 
       foreign key (owner_id) 
       references `user` (id);


-- TEAM_MEMBER TABLE

create table team_member (
       id bigint not null auto_increment,
        name varchar(255) not null,
        properties TEXT,
        team_id bigint not null,
        primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

alter table team_member
       add constraint FK9ubp79ei4tv4crd0r9n7u5i6e
       foreign key (team_id)
       references team (id);


