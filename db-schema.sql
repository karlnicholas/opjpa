
    alter table opinion_summary_opinion_citations 
        drop 
        foreign key FK3w3c8oo02ponml8aur0fbg1sp;

    alter table opinion_summary_referring_opinions 
        drop 
        foreign key FK9imu4k9kam4qqsnsswk9ktt04;

    alter table opinion_summary_statute_citations 
        drop 
        foreign key FK6cwwovu1jgtb2k07pk75ywh7;

    alter table slip_opinion_opinion_citations 
        drop 
        foreign key FKi8i9j3sodfma7kd59noxi2p8w;

    alter table slip_opinion_referring_opinions 
        drop 
        foreign key FKlvqab3cace1e5hgncuj57yg42;

    alter table slip_opinion_statute_citations 
        drop 
        foreign key FKjpa8m0b5ycy0d2t4ya66v4xvo;

    alter table statute_citation_referring_opinion_count 
        drop 
        foreign key FKcmhoh1rpnya4u6f2v00u4emm8;

    alter table user_role 
        drop 
        foreign key FKeog8p06nu33ihk13roqnrp1y6;

    alter table user_role 
        drop 
        foreign key FK859n2jvi8ivhui0rl0esws6o;

    drop table if exists opinionsummary;

    drop table if exists opinion_summary_opinion_citations;

    drop table if exists opinion_summary_referring_opinions;

    drop table if exists opinion_summary_statute_citations;

    drop table if exists role;

    drop table if exists slipopinion;

    drop table if exists slip_opinion_opinion_citations;

    drop table if exists slip_opinion_referring_opinions;

    drop table if exists slip_opinion_statute_citations;

    drop table if exists statutecitation;

    drop table if exists statute_citation_referring_opinion_count;

    drop table if exists user;

    drop table if exists user_role;

    create table opinionsummary (
        page bigint not null,
        volume integer not null,
        vset integer not null,
        count_referring_opinions integer not null,
        court TEXT,
        opinion_date date,
        title TEXT,
        primary key (page, volume, vset)
    ) ENGINE=InnoDB;

    create table opinion_summary_opinion_citations (
        opinion_summary_page bigint not null,
        opinion_summary_volume integer not null,
        opinion_summary_vset integer not null,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (opinion_summary_page, opinion_summary_volume, opinion_summary_vset, page, volume, vset)
    ) ENGINE=InnoDB;

    create table opinion_summary_referring_opinions (
        opinion_summary_page bigint not null,
        opinion_summary_volume integer not null,
        opinion_summary_vset integer not null,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (opinion_summary_page, opinion_summary_volume, opinion_summary_vset, page, volume, vset)
    ) ENGINE=InnoDB;

    create table opinion_summary_statute_citations (
        opinion_summary_page bigint not null,
        opinion_summary_volume integer not null,
        opinion_summary_vset integer not null,
        code varchar(255),
        section_number varchar(255)
    ) ENGINE=InnoDB;

    create table role (
        id bigint not null auto_increment,
        role varchar(255) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table slipopinion (
        page bigint not null,
        volume integer not null,
        vset integer not null,
        count_referring_opinions integer not null,
        court TEXT,
        opinion_date date,
        title TEXT,
        disposition TEXT,
        file_name TEXT,
        summary TEXT,
        primary key (page, volume, vset)
    ) ENGINE=InnoDB;

    create table slip_opinion_opinion_citations (
        slip_opinion_page bigint not null,
        slip_opinion_volume integer not null,
        slip_opinion_vset integer not null,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (slip_opinion_page, slip_opinion_volume, slip_opinion_vset, page, volume, vset)
    ) ENGINE=InnoDB;

    create table slip_opinion_referring_opinions (
        slip_opinion_page bigint not null,
        slip_opinion_volume integer not null,
        slip_opinion_vset integer not null,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (slip_opinion_page, slip_opinion_volume, slip_opinion_vset, page, volume, vset)
    ) ENGINE=InnoDB;

    create table slip_opinion_statute_citations (
        slip_opinion_page bigint not null,
        slip_opinion_volume integer not null,
        slip_opinion_vset integer not null,
        code varchar(255),
        section_number varchar(255)
    ) ENGINE=InnoDB;

    create table statutecitation (
        id bigint not null auto_increment,
        designated bit not null,
        code varchar(255),
        section_number varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table statute_citation_referring_opinion_count (
        statute_citation_id bigint not null,
        referring_opinion_count integer,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (statute_citation_id, page, volume, vset)
    ) ENGINE=InnoDB;

    create table user (
        id bigint not null auto_increment,
        codes tinyblob,
        create_date datetime,
        email varchar(255),
        email_updates bit not null,
        first_name varchar(255),
        last_name varchar(255),
        locale varchar(255),
        password varchar(255),
        update_date datetime,
        verified bit not null,
        verify_count integer not null,
        verify_errors integer not null,
        verify_key varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table user_role (
        user_id bigint not null,
        roles_id bigint not null
    ) ENGINE=InnoDB;

    create index IDXk0cudkkkxkpgggrmq197iurmp on opinionsummary (volume, vset, page);

    alter table role 
        add constraint UK_bjxn5ii7v7ygwx39et0wawu0q unique (role);

    create index IDXawik8fxrew20mjriq10fitgi9 on slipopinion (volume, vset, page);

    create index IDXcl6khfhqdvkpxsqhhiegkp9fd on statutecitation (code, section_number);

    alter table user 
        add constraint UKob8kqyqqgmefl0aco34akdtpe unique (email);

    alter table opinion_summary_opinion_citations 
        add constraint FK3w3c8oo02ponml8aur0fbg1sp 
        foreign key (opinion_summary_page, opinion_summary_volume, opinion_summary_vset) 
        references opinionsummary (page, volume, vset);

    alter table opinion_summary_referring_opinions 
        add constraint FK9imu4k9kam4qqsnsswk9ktt04 
        foreign key (opinion_summary_page, opinion_summary_volume, opinion_summary_vset) 
        references opinionsummary (page, volume, vset);

    alter table opinion_summary_statute_citations 
        add constraint FK6cwwovu1jgtb2k07pk75ywh7 
        foreign key (opinion_summary_page, opinion_summary_volume, opinion_summary_vset) 
        references opinionsummary (page, volume, vset);

    alter table slip_opinion_opinion_citations 
        add constraint FKi8i9j3sodfma7kd59noxi2p8w 
        foreign key (slip_opinion_page, slip_opinion_volume, slip_opinion_vset) 
        references slipopinion (page, volume, vset);

    alter table slip_opinion_referring_opinions 
        add constraint FKlvqab3cace1e5hgncuj57yg42 
        foreign key (slip_opinion_page, slip_opinion_volume, slip_opinion_vset) 
        references slipopinion (page, volume, vset);

    alter table slip_opinion_statute_citations 
        add constraint FKjpa8m0b5ycy0d2t4ya66v4xvo 
        foreign key (slip_opinion_page, slip_opinion_volume, slip_opinion_vset) 
        references slipopinion (page, volume, vset);

    alter table statute_citation_referring_opinion_count 
        add constraint FKcmhoh1rpnya4u6f2v00u4emm8 
        foreign key (statute_citation_id) 
        references statutecitation (id);

    alter table user_role 
        add constraint FKeog8p06nu33ihk13roqnrp1y6 
        foreign key (roles_id) 
        references role (id);

    alter table user_role 
        add constraint FK859n2jvi8ivhui0rl0esws6o 
        foreign key (user_id) 
        references user (id);
