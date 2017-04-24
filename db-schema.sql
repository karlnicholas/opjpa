
    alter table articlecomment 
        drop 
        foreign key FK6mxverhrr5rsel5msd57e9as0;

    alter table articlecomment 
        drop 
        foreign key FK2y4u22wnwi60je4wpokuprt0l;

    alter table opinionsummary_opinioncitations 
        drop 
        foreign key FKqchg5ejilb0t4r6r64ruidiaj;

    alter table opinionsummary_referringopinions 
        drop 
        foreign key FKgr6a36p9kayr58yupn4qftbk;

    alter table opinionsummary_statutecitations 
        drop 
        foreign key FKd53i6cfoqiw69uua5prh29ffd;

    alter table slipopinion_opinioncitations 
        drop 
        foreign key FKnv3p7ky3646y8i4iixa1dtrw8;

    alter table slipopinion_referringopinions 
        drop 
        foreign key FK61efa3433v7d0q6fnrxcvoi5g;

    alter table slipopinion_statutecitations 
        drop 
        foreign key FK1m20fklftdbqpg352amvl8198;

    alter table statutecitation_referringopinioncount 
        drop 
        foreign key FKqiuk8lfq5bt48wm3b54imm2du;

    alter table user_role 
        drop 
        foreign key FKeog8p06nu33ihk13roqnrp1y6;

    alter table user_role 
        drop 
        foreign key FK859n2jvi8ivhui0rl0esws6o;

    drop table if exists article;

    drop table if exists articlecomment;

    drop table if exists opinionsummary;

    drop table if exists opinionsummary_opinioncitations;

    drop table if exists opinionsummary_referringopinions;

    drop table if exists opinionsummary_statutecitations;

    drop table if exists role;

    drop table if exists slipopinion;

    drop table if exists slipopinion_opinioncitations;

    drop table if exists slipopinion_referringopinions;

    drop table if exists slipopinion_statutecitations;

    drop table if exists statutecitation;

    drop table if exists statutecitation_referringopinioncount;

    drop table if exists user;

    drop table if exists user_role;

    create table article (
        id bigint not null auto_increment,
        contents varchar(255),
        date datetime,
        title varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table articlecomment (
        id bigint not null auto_increment,
        comment varchar(255),
        importance integer not null,
        article_id bigint,
        user_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table opinionsummary (
        page bigint not null,
        volume integer not null,
        vset integer not null,
        countreferringopinions integer not null,
        court TEXT,
        opiniondate date,
        title TEXT,
        primary key (page, volume, vset)
    ) ENGINE=InnoDB;

    create table opinionsummary_opinioncitations (
        opinionsummary_page bigint not null,
        opinionsummary_volume integer not null,
        opinionsummary_vset integer not null,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (opinionsummary_page, opinionsummary_volume, opinionsummary_vset, page, volume, vset)
    ) ENGINE=InnoDB;

    create table opinionsummary_referringopinions (
        opinionsummary_page bigint not null,
        opinionsummary_volume integer not null,
        opinionsummary_vset integer not null,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (opinionsummary_page, opinionsummary_volume, opinionsummary_vset, page, volume, vset)
    ) ENGINE=InnoDB;

    create table opinionsummary_statutecitations (
        opinionsummary_page bigint not null,
        opinionsummary_volume integer not null,
        opinionsummary_vset integer not null,
        code varchar(255),
        sectionnumber varchar(255)
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
        countreferringopinions integer not null,
        court TEXT,
        opiniondate date,
        title TEXT,
        disposition TEXT,
        fileextension TEXT,
        filename TEXT,
        summary TEXT,
        primary key (page, volume, vset)
    ) ENGINE=InnoDB;

    create table slipopinion_opinioncitations (
        slipopinion_page bigint not null,
        slipopinion_volume integer not null,
        slipopinion_vset integer not null,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (slipopinion_page, slipopinion_volume, slipopinion_vset, page, volume, vset)
    ) ENGINE=InnoDB;

    create table slipopinion_referringopinions (
        slipopinion_page bigint not null,
        slipopinion_volume integer not null,
        slipopinion_vset integer not null,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (slipopinion_page, slipopinion_volume, slipopinion_vset, page, volume, vset)
    ) ENGINE=InnoDB;

    create table slipopinion_statutecitations (
        slipopinion_page bigint not null,
        slipopinion_volume integer not null,
        slipopinion_vset integer not null,
        code varchar(255),
        sectionnumber varchar(255)
    ) ENGINE=InnoDB;

    create table statutecitation (
        id bigint not null auto_increment,
        designated bit not null,
        code varchar(255),
        sectionnumber varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table statutecitation_referringopinioncount (
        statutecitation_id bigint not null,
        referringopinioncount integer,
        page bigint not null,
        volume integer not null,
        vset integer not null,
        primary key (statutecitation_id, page, volume, vset)
    ) ENGINE=InnoDB;

    create table user (
        id bigint not null auto_increment,
        codes tinyblob,
        createdate datetime,
        email varchar(255),
        emailupdates bit not null,
        firstname varchar(255),
        lastname varchar(255),
        locale varchar(255),
        optout bit not null,
        optoutkey varchar(255),
        password varchar(255),
        updatedate datetime,
        verified bit not null,
        verifycount integer not null,
        verifyerrors integer not null,
        verifykey varchar(255),
        welcomeerrors integer not null,
        welcomed bit not null,
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

    create index IDXcl6khfhqdvkpxsqhhiegkp9fd on statutecitation (code, sectionnumber);

    alter table user 
        add constraint UKob8kqyqqgmefl0aco34akdtpe unique (email);

    alter table articlecomment 
        add constraint FK6mxverhrr5rsel5msd57e9as0 
        foreign key (article_id) 
        references article (id);

    alter table articlecomment 
        add constraint FK2y4u22wnwi60je4wpokuprt0l 
        foreign key (user_id) 
        references user (id);

    alter table opinionsummary_opinioncitations 
        add constraint FKqchg5ejilb0t4r6r64ruidiaj 
        foreign key (opinionsummary_page, opinionsummary_volume, opinionsummary_vset) 
        references opinionsummary (page, volume, vset);

    alter table opinionsummary_referringopinions 
        add constraint FKgr6a36p9kayr58yupn4qftbk 
        foreign key (opinionsummary_page, opinionsummary_volume, opinionsummary_vset) 
        references opinionsummary (page, volume, vset);

    alter table opinionsummary_statutecitations 
        add constraint FKd53i6cfoqiw69uua5prh29ffd 
        foreign key (opinionsummary_page, opinionsummary_volume, opinionsummary_vset) 
        references opinionsummary (page, volume, vset);

    alter table slipopinion_opinioncitations 
        add constraint FKnv3p7ky3646y8i4iixa1dtrw8 
        foreign key (slipopinion_page, slipopinion_volume, slipopinion_vset) 
        references slipopinion (page, volume, vset);

    alter table slipopinion_referringopinions 
        add constraint FK61efa3433v7d0q6fnrxcvoi5g 
        foreign key (slipopinion_page, slipopinion_volume, slipopinion_vset) 
        references slipopinion (page, volume, vset);

    alter table slipopinion_statutecitations 
        add constraint FK1m20fklftdbqpg352amvl8198 
        foreign key (slipopinion_page, slipopinion_volume, slipopinion_vset) 
        references slipopinion (page, volume, vset);

    alter table statutecitation_referringopinioncount 
        add constraint FKqiuk8lfq5bt48wm3b54imm2du 
        foreign key (statutecitation_id) 
        references statutecitation (id);

    alter table user_role 
        add constraint FKeog8p06nu33ihk13roqnrp1y6 
        foreign key (roles_id) 
        references role (id);

    alter table user_role 
        add constraint FK859n2jvi8ivhui0rl0esws6o 
        foreign key (user_id) 
        references user (id);
