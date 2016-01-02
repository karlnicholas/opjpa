
    alter table OpinionSummary_opinionCitationKeys 
        drop constraint FK_8ycg7rdhrg5do1fgtldrfijn8;
    alter table OpinionSummary_opinionsReferredFrom 
        drop constraint FK_mrj5i06k2t1y2x7erymouw7em;
    alter table OpinionSummary_statuteCitationKeys 
        drop constraint FK_buf7gufi4w7x4shj5dltoyly;
    alter table SlipOpinion_opinionCitationKeys 
        drop constraint FK_rgla4u9lffkvokqt72jljxm5u;
    alter table SlipOpinion_opinionsReferredFrom 
        drop constraint FK_lyuim0bu7lwrxfg13s5rewno7;
    alter table SlipOpinion_statuteCitationKeys 
        drop constraint FK_6hlbsk6raw3yk4s02gqj26gox;
    alter table StatuteCitation_referringCaseMap 
        drop constraint FK_95d4ya2l1b0ox0tbprvtgy2rg;
    drop table if exists OpinionSummary cascade;
    drop table if exists OpinionSummary_opinionCitationKeys cascade;
    drop table if exists OpinionSummary_opinionsReferredFrom cascade;
    drop table if exists OpinionSummary_statuteCitationKeys cascade;
    drop table if exists SlipOpinion cascade;
    drop table if exists SlipOpinion_opinionCitationKeys cascade;
    drop table if exists SlipOpinion_opinionsReferredFrom cascade;
    drop table if exists SlipOpinion_statuteCitationKeys cascade;
    drop table if exists StatuteCitation cascade;
    drop table if exists StatuteCitation_referringCaseMap cascade;
    drop sequence hibernate_sequence;
    create table OpinionSummary (
        id int8 not null,
        court TEXT,
        opinionDate date,
        page int8 not null,
        volume int4 not null,
        vset int4 not null,
        publishDate date,
        title TEXT,
        primary key (id)
    );
    create table OpinionSummary_opinionCitationKeys (
        OpinionSummary_id int8 not null,
        page int8 not null,
        volume int4 not null,
        vset int4 not null
    );
    create table OpinionSummary_opinionsReferredFrom (
        OpinionSummary_id int8 not null,
        page int8 not null,
        volume int4 not null,
        vset int4 not null
    );
    create table OpinionSummary_statuteCitationKeys (
        OpinionSummary_id int8 not null,
        code TEXT,
        sectionNumber TEXT
    );
    create table SlipOpinion (
        id int8 not null,
        court TEXT,
        opinionDate date,
        page int8 not null,
        volume int4 not null,
        vset int4 not null,
        publishDate date,
        title TEXT,
        disposition TEXT,
        fileName TEXT,
        summary TEXT,
        primary key (id)
    );
    create table SlipOpinion_opinionCitationKeys (
        SlipOpinion_id int8 not null,
        page int8 not null,
        volume int4 not null,
        vset int4 not null
    );
    create table SlipOpinion_opinionsReferredFrom (
        SlipOpinion_id int8 not null,
        page int8 not null,
        volume int4 not null,
        vset int4 not null
    );
    create table SlipOpinion_statuteCitationKeys (
        SlipOpinion_id int8 not null,
        code TEXT,
        sectionNumber TEXT
    );
    create table StatuteCitation (
        id int8 not null,
        designated boolean not null,
        code TEXT,
        sectionNumber TEXT,
        primary key (id)
    );
    create table StatuteCitation_referringCaseMap (
        StatuteCitation_id int8 not null,
        referringCaseMap int4,
        page int8 not null,
        volume int4 not null,
        vset int4 not null,
        primary key (StatuteCitation_id, page, volume, vset)
    );
    create index UK_64jpr2briakrjulgpgcjmis6g on OpinionSummary (volume, vset, page);
    create index UK_9pp4c9r1gw6o0yt1j75n7fvrw on SlipOpinion (volume, vset, page);
    create index UK_tpaoq400m551d2ckksm65mlor on StatuteCitation (code, sectionNumber);
    alter table OpinionSummary_opinionCitationKeys 
        add constraint FK_8ycg7rdhrg5do1fgtldrfijn8 
        foreign key (OpinionSummary_id) 
        references OpinionSummary;
    alter table OpinionSummary_opinionsReferredFrom 
        add constraint FK_mrj5i06k2t1y2x7erymouw7em 
        foreign key (OpinionSummary_id) 
        references OpinionSummary;
    alter table OpinionSummary_statuteCitationKeys 
        add constraint FK_buf7gufi4w7x4shj5dltoyly 
        foreign key (OpinionSummary_id) 
        references OpinionSummary;
    alter table SlipOpinion_opinionCitationKeys 
        add constraint FK_rgla4u9lffkvokqt72jljxm5u 
        foreign key (SlipOpinion_id) 
        references SlipOpinion;
    alter table SlipOpinion_opinionsReferredFrom 
        add constraint FK_lyuim0bu7lwrxfg13s5rewno7 
        foreign key (SlipOpinion_id) 
        references SlipOpinion;
    alter table SlipOpinion_statuteCitationKeys 
        add constraint FK_6hlbsk6raw3yk4s02gqj26gox 
        foreign key (SlipOpinion_id) 
        references SlipOpinion;
    alter table StatuteCitation_referringCaseMap 
        add constraint FK_95d4ya2l1b0ox0tbprvtgy2rg 
        foreign key (StatuteCitation_id) 
        references StatuteCitation;
    create sequence hibernate_sequence;