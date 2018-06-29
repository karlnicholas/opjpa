alter table opinionbase_opinionbase drop foreign key FKv9vgyvlfwws091112o975tdf;
alter table opinionbase_opinionbase drop foreign key FKta2uodkexsd423u05p76th66o;
alter table opinionstatutecitation drop foreign key FKj4m7y817yxpioeb629x56h8u6;
alter table opinionstatutecitation drop foreign key FKfeousb71h1ayu6myk7wp7mgfc;
alter table slipproperties drop foreign key FK97edwcyxhia5mmhb1qqgury8o;
alter table user_role drop foreign key FKeog8p06nu33ihk13roqnrp1y6;
alter table user_role drop foreign key FK859n2jvi8ivhui0rl0esws6o;
drop table if exists opinionbase;
drop table if exists opinionbase_opinionbase;
drop table if exists opinionstatutecitation;
drop table if exists role;
drop table if exists slipproperties;
drop table if exists statutecitation;
drop table if exists user;
drop table if exists user_role;
create table opinionbase (dtype integer not null, id integer not null auto_increment, countreferringopinions integer not null, opiniondate date, page integer not null, volume integer not null, vset integer not null, title varchar(127), primary key (id)) ENGINE=InnoDB;
create table opinionbase_opinionbase (referringopinions_id integer not null, opinioncitations_id integer not null, primary key (referringopinions_id, opinioncitations_id)) ENGINE=InnoDB;
create table opinionstatutecitation (countreferences integer not null, opinionbase_id integer not null, statutecitation_id integer not null, primary key (opinionbase_id, statutecitation_id)) ENGINE=InnoDB;
create table role (id bigint not null auto_increment, role varchar(255) not null, primary key (id)) ENGINE=InnoDB;
create table slipproperties (court varchar(15), disposition varchar(31), fileextension varchar(7), filename varchar(31), summary varchar(4007), slipopinion_id integer not null, primary key (slipopinion_id)) ENGINE=InnoDB;
create table statutecitation (id integer not null auto_increment, designated bit not null, sectionnumber char(32), title char(3), primary key (id)) ENGINE=InnoDB;
create table user (id bigint not null auto_increment, createdate datetime, email varchar(255), emailupdates bit not null, firstname varchar(255), lastname varchar(255), locale varchar(255), optout bit not null, optoutkey varchar(255), password varchar(255), titles tinyblob, updatedate datetime, verified bit not null, verifycount integer not null, verifyerrors integer not null, verifykey varchar(255), welcomeerrors integer not null, welcomed bit not null, primary key (id)) ENGINE=InnoDB;
create table user_role (user_id bigint not null, roles_id bigint not null) ENGINE=InnoDB;
create index IDXd587qslmmirn7juop20is6gwt on opinionbase (vset, volume, page);
alter table role add constraint UK_bjxn5ii7v7ygwx39et0wawu0q unique (role);
create index IDXa0xjansqjx14py55e14jnar89 on statutecitation (title, sectionnumber);
alter table user add constraint UKob8kqyqqgmefl0aco34akdtpe unique (email);
alter table opinionbase_opinionbase add constraint FKv9vgyvlfwws091112o975tdf foreign key (opinioncitations_id) references opinionbase (id);
alter table opinionbase_opinionbase add constraint FKta2uodkexsd423u05p76th66o foreign key (referringopinions_id) references opinionbase (id);
alter table opinionstatutecitation add constraint FKj4m7y817yxpioeb629x56h8u6 foreign key (opinionbase_id) references opinionbase (id);
alter table opinionstatutecitation add constraint FKfeousb71h1ayu6myk7wp7mgfc foreign key (statutecitation_id) references statutecitation (id);
alter table slipproperties add constraint FK97edwcyxhia5mmhb1qqgury8o foreign key (slipopinion_id) references opinionbase (id);
alter table user_role add constraint FKeog8p06nu33ihk13roqnrp1y6 foreign key (roles_id) references role (id);
alter table user_role add constraint FK859n2jvi8ivhui0rl0esws6o foreign key (user_id) references user (id);
alter table opinionbase_opinionbase drop foreign key FKv9vgyvlfwws091112o975tdf;
alter table opinionbase_opinionbase drop foreign key FKta2uodkexsd423u05p76th66o;
alter table opinionstatutecitation drop foreign key FKj4m7y817yxpioeb629x56h8u6;
alter table opinionstatutecitation drop foreign key FKfeousb71h1ayu6myk7wp7mgfc;
alter table slipproperties drop foreign key FK97edwcyxhia5mmhb1qqgury8o;
alter table user_role drop foreign key FKeog8p06nu33ihk13roqnrp1y6;
alter table user_role drop foreign key FK859n2jvi8ivhui0rl0esws6o;
drop table if exists opinionbase;
drop table if exists opinionbase_opinionbase;
drop table if exists opinionstatutecitation;
drop table if exists role;
drop table if exists slipproperties;
drop table if exists statutecitation;
drop table if exists user;
drop table if exists user_role;
create table opinionbase (dtype integer not null, id integer not null auto_increment, countreferringopinions integer not null, opiniondate date, page integer not null, volume integer not null, vset integer not null, title varchar(127), primary key (id)) ENGINE=InnoDB;
create table opinionbase_opinionbase (referringopinions_id integer not null, opinioncitations_id integer not null, primary key (referringopinions_id, opinioncitations_id)) ENGINE=InnoDB;
create table opinionstatutecitation (countreferences integer not null, opinionbase_id integer not null, statutecitation_id integer not null, primary key (opinionbase_id, statutecitation_id)) ENGINE=InnoDB;
create table role (id bigint not null auto_increment, role varchar(255) not null, primary key (id)) ENGINE=InnoDB;
create table slipproperties (court varchar(15), disposition varchar(31), fileextension varchar(7), filename varchar(31), summary varchar(4007), slipopinion_id integer not null, primary key (slipopinion_id)) ENGINE=InnoDB;
create table statutecitation (id integer not null auto_increment, designated bit not null, sectionnumber char(32), title char(4), primary key (id)) ENGINE=InnoDB;
create table user (id bigint not null auto_increment, createdate datetime, email varchar(255), emailupdates bit not null, firstname varchar(255), lastname varchar(255), locale varchar(255), optout bit not null, optoutkey varchar(255), password varchar(255), titles tinyblob, updatedate datetime, verified bit not null, verifycount integer not null, verifyerrors integer not null, verifykey varchar(255), welcomeerrors integer not null, welcomed bit not null, primary key (id)) ENGINE=InnoDB;
create table user_role (user_id bigint not null, roles_id bigint not null) ENGINE=InnoDB;
create index IDXd587qslmmirn7juop20is6gwt on opinionbase (vset, volume, page);
alter table role add constraint UK_bjxn5ii7v7ygwx39et0wawu0q unique (role);
create index IDXa0xjansqjx14py55e14jnar89 on statutecitation (title, sectionnumber);
alter table user add constraint UKob8kqyqqgmefl0aco34akdtpe unique (email);
alter table opinionbase_opinionbase add constraint FKv9vgyvlfwws091112o975tdf foreign key (opinioncitations_id) references opinionbase (id);
alter table opinionbase_opinionbase add constraint FKta2uodkexsd423u05p76th66o foreign key (referringopinions_id) references opinionbase (id);
alter table opinionstatutecitation add constraint FKj4m7y817yxpioeb629x56h8u6 foreign key (opinionbase_id) references opinionbase (id);
alter table opinionstatutecitation add constraint FKfeousb71h1ayu6myk7wp7mgfc foreign key (statutecitation_id) references statutecitation (id);
alter table slipproperties add constraint FK97edwcyxhia5mmhb1qqgury8o foreign key (slipopinion_id) references opinionbase (id);
alter table user_role add constraint FKeog8p06nu33ihk13roqnrp1y6 foreign key (roles_id) references role (id);
alter table user_role add constraint FK859n2jvi8ivhui0rl0esws6o foreign key (user_id) references user (id);
