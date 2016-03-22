
    alter table OpinionSummary_opinionCitations 
        drop 
        foreign key FK_7taix86q3mc0xl8tqvm4cs777;
    alter table OpinionSummary_referringOpinions 
        drop 
        foreign key FK_o7oesopyeys7rmvos6brdm1ps;
    alter table OpinionSummary_statuteCitations 
        drop 
        foreign key FK_mt3ehxifatmti8pt8ul1rctfx;
    alter table SlipOpinion_opinionCitations 
        drop 
        foreign key FK_nfsnm7y4d1rt4u5jxmcombor8;
    alter table SlipOpinion_referringOpinions 
        drop 
        foreign key FK_3e5akqak78f37yjuwlct63unh;
    alter table SlipOpinion_statuteCitations 
        drop 
        foreign key FK_1hg79w4owc4lebrdrh57calf4;
    alter table StatuteCitation_referringOpinionCount 
        drop 
        foreign key FK_n7io7ja2sk6bo74057n0786hu;
    alter table user_role 
        drop 
        foreign key FK_5k3dviices5fr7560hvc81x4r;
    alter table user_role 
        drop 
        foreign key FK_apcc8lxk2xnug8377fatvbn04;
    drop table if exists OpinionSummary;
    drop table if exists OpinionSummary_opinionCitations;
    drop table if exists OpinionSummary_referringOpinions;
    drop table if exists OpinionSummary_statuteCitations;
    drop table if exists SlipOpinion;
    drop table if exists SlipOpinion_opinionCitations;
    drop table if exists SlipOpinion_referringOpinions;
    drop table if exists SlipOpinion_statuteCitations;
    drop table if exists StatuteCitation;
    drop table if exists StatuteCitation_referringOpinionCount;
    drop table if exists role;
    drop table if exists user;
    drop table if exists user_role;
    create table OpinionSummary (
        id bigint not null auto_increment,
        countReferringOpinions integer not null,
        court TEXT,
        opinionDate date,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        title TEXT,
        primary key (id)
    ) ENGINE=InnoDB;
    create table OpinionSummary_opinionCitations (
        OpinionSummary_id bigint not null,
        page bigint not null,
        volume integer not null,
        vset integer not null
    ) ENGINE=InnoDB;
    create table OpinionSummary_referringOpinions (
        OpinionSummary_id bigint not null,
        page bigint not null,
        volume integer not null,
        vset integer not null
    ) ENGINE=InnoDB;
    create table OpinionSummary_statuteCitations (
        OpinionSummary_id bigint not null,
        code varchar(255),
        sectionNumber varchar(255)
    ) ENGINE=InnoDB;
    create table SlipOpinion (
        id bigint not null auto_increment,
        countReferringOpinions integer not null,
        court TEXT,
        opinionDate date,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        title TEXT,
        disposition TEXT,
        fileName TEXT,
        summary TEXT,
        primary key (id)
    ) ENGINE=InnoDB;
    create table SlipOpinion_opinionCitations (
        SlipOpinion_id bigint not null,
        page bigint not null,
        volume integer not null,
        vset integer not null
    ) ENGINE=InnoDB;
    create table SlipOpinion_referringOpinions (
        SlipOpinion_id bigint not null,
        page bigint not null,
        volume integer not null,
        vset integer not null
    ) ENGINE=InnoDB;
    create table SlipOpinion_statuteCitations (
        SlipOpinion_id bigint not null,
        code varchar(255),
        sectionNumber varchar(255)
    ) ENGINE=InnoDB;
    create table StatuteCitation (
        id bigint not null auto_increment,
        designated bit not null,
        code varchar(255),
        sectionNumber varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;
    create table StatuteCitation_referringOpinionCount (
        StatuteCitation_id bigint not null,
        referringOpinionCount integer,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (StatuteCitation_id, page, volume, vset)
    ) ENGINE=InnoDB;
    create table role (
        id bigint not null auto_increment,
        role varchar(255) not null,
        primary key (id)
    ) ENGINE=InnoDB;
    create table user (
        id bigint not null auto_increment,
        codes tinyblob,
        createDate datetime,
        email varchar(255),
        emailUpdates bit not null,
        firstName varchar(255),
        lastName varchar(255),
        locale varchar(255),
        password varchar(255),
        updateDate datetime,
        verified bit not null,
        verifyCount integer not null,
        verifyErrors integer not null,
        verifyKey varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;
    create table user_role (
        user_id bigint not null,
        roles_id bigint not null
    ) ENGINE=InnoDB;
    create index UK_64jpr2briakrjulgpgcjmis6g on OpinionSummary (volume, vset, page);
    create index UK_9pp4c9r1gw6o0yt1j75n7fvrw on SlipOpinion (volume, vset, page);
    create index UK_tpaoq400m551d2ckksm65mlor on StatuteCitation (code, sectionNumber);
    alter table role 
        add constraint UK_bjxn5ii7v7ygwx39et0wawu0q  unique (role);
    alter table user 
        add constraint UK_ob8kqyqqgmefl0aco34akdtpe  unique (email);
    alter table OpinionSummary_opinionCitations 
        add constraint FK_7taix86q3mc0xl8tqvm4cs777 
        foreign key (OpinionSummary_id) 
        references OpinionSummary (id);
    alter table OpinionSummary_referringOpinions 
        add constraint FK_o7oesopyeys7rmvos6brdm1ps 
        foreign key (OpinionSummary_id) 
        references OpinionSummary (id);
    alter table OpinionSummary_statuteCitations 
        add constraint FK_mt3ehxifatmti8pt8ul1rctfx 
        foreign key (OpinionSummary_id) 
        references OpinionSummary (id);
    alter table SlipOpinion_opinionCitations 
        add constraint FK_nfsnm7y4d1rt4u5jxmcombor8 
        foreign key (SlipOpinion_id) 
        references SlipOpinion (id);
    alter table SlipOpinion_referringOpinions 
        add constraint FK_3e5akqak78f37yjuwlct63unh 
        foreign key (SlipOpinion_id) 
        references SlipOpinion (id);
    alter table SlipOpinion_statuteCitations 
        add constraint FK_1hg79w4owc4lebrdrh57calf4 
        foreign key (SlipOpinion_id) 
        references SlipOpinion (id);
    alter table StatuteCitation_referringOpinionCount 
        add constraint FK_n7io7ja2sk6bo74057n0786hu 
        foreign key (StatuteCitation_id) 
        references StatuteCitation (id);
    alter table user_role 
        add constraint FK_5k3dviices5fr7560hvc81x4r 
        foreign key (roles_id) 
        references role (id);
    alter table user_role 
        add constraint FK_apcc8lxk2xnug8377fatvbn04 
        foreign key (user_id) 
        references user (id);