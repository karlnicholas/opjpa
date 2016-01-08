
    alter table OpinionSummary_opinionCitations 
        drop constraint FK_7taix86q3mc0xl8tqvm4cs777;
    alter table OpinionSummary_referringOpinions 
        drop constraint FK_o7oesopyeys7rmvos6brdm1ps;
    alter table OpinionSummary_statuteCitations 
        drop constraint FK_mt3ehxifatmti8pt8ul1rctfx;
    alter table SlipOpinion_opinionCitations 
        drop constraint FK_nfsnm7y4d1rt4u5jxmcombor8;
    alter table SlipOpinion_referringOpinions 
        drop constraint FK_3e5akqak78f37yjuwlct63unh;
    alter table SlipOpinion_statuteCitations 
        drop constraint FK_1hg79w4owc4lebrdrh57calf4;
    alter table StatuteCitation_referringOpinionCount 
        drop constraint FK_n7io7ja2sk6bo74057n0786hu;
    drop table if exists OpinionSummary cascade;
    drop table if exists OpinionSummary_opinionCitations cascade;
    drop table if exists OpinionSummary_referringOpinions cascade;
    drop table if exists OpinionSummary_statuteCitations cascade;
    drop table if exists SlipOpinion cascade;
    drop table if exists SlipO`pinion_opinionCitations cascade;
    drop table if exists SlipOpinion_referringOpinions cascade;
    drop table if exists SlipOpinion_statuteCitations cascade;
    drop table if exists StatuteCitation cascade;
    drop table if exists StatuteCitation_referringOpinionCount cascade;
    drop sequence hibernate_sequence;
    create table OpinionSummary (
        id int8 not null,
        countReferringOpinions int4 not null,
        court TEXT,
        opinionDate date,
        page int8 not null,
        volume int4 not null,
        vset int4 not null,
        title TEXT,
        primary key (id)
    );
    create table OpinionSummary_opinionCitations (
        OpinionSummary_id int8 not null,
        page int8 not null,
        volume int4 not null,
        vset int4 not null
    );
    create table OpinionSummary_referringOpinions (
        OpinionSummary_id int8 not null,
        page int8 not null,
        volume int4 not null,
        vset int4 not null
    );
    create table OpinionSummary_statuteCitations (
        OpinionSummary_id int8 not null,
        code TEXT,
        sectionNumber TEXT
    );
    create table SlipOpinion (
        id int8 not null,
        countReferringOpinions int4 not null,
        court TEXT,
        opinionDate date,
        page int8 not null,
        volume int4 not null,
        vset int4 not null,
        title TEXT,
        disposition TEXT,
        fileName TEXT,
        summary TEXT,
        primary key (id)
    );
    create table SlipOpinion_opinionCitations (
        SlipOpinion_id int8 not null,
        page int8 not null,
        volume int4 not null,
        vset int4 not null
    );
    create table SlipOpinion_referringOpinions (
        SlipOpinion_id int8 not null,
        page int8 not null,
        volume int4 not null,
        vset int4 not null
    );
    create table SlipOpinion_statuteCitations (
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
    create table StatuteCitation_referringOpinionCount (
        StatuteCitation_id int8 not null,
        referringOpinionCount int4,
        page int8 not null,
        volume int4 not null,
        vset int4 not null,
        primary key (StatuteCitation_id, page, volume, vset)
    );
    create index UK_64jpr2briakrjulgpgcjmis6g on OpinionSummary (volume, vset, page);
    create index UK_9pp4c9r1gw6o0yt1j75n7fvrw on SlipOpinion (volume, vset, page);
    create index UK_tpaoq400m551d2ckksm65mlor on StatuteCitation (code, sectionNumber);
    alter table OpinionSummary_opinionCitations 
        add constraint FK_7taix86q3mc0xl8tqvm4cs777 
        foreign key (OpinionSummary_id) 
        references OpinionSummary;
    alter table OpinionSummary_referringOpinions 
        add constraint FK_o7oesopyeys7rmvos6brdm1ps 
        foreign key (OpinionSummary_id) 
        references OpinionSummary;
    alter table OpinionSummary_statuteCitations 
        add constraint FK_mt3ehxifatmti8pt8ul1rctfx 
        foreign key (OpinionSummary_id) 
        references OpinionSummary;
    alter table SlipOpinion_opinionCitations 
        add constraint FK_nfsnm7y4d1rt4u5jxmcombor8 
        foreign key (SlipOpinion_id) 
        references SlipOpinion;
    alter table SlipOpinion_referringOpinions 
        add constraint FK_3e5akqak78f37yjuwlct63unh 
        foreign key (SlipOpinion_id) 
        references SlipOpinion;
    alter table SlipOpinion_statuteCitations 
        add constraint FK_1hg79w4owc4lebrdrh57calf4 
        foreign key (SlipOpinion_id) 
        references SlipOpinion;
    alter table StatuteCitation_referringOpinionCount 
        add constraint FK_n7io7ja2sk6bo74057n0786hu 
        foreign key (StatuteCitation_id) 
        references StatuteCitation;
    create sequence hibernate_sequence;