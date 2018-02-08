--liquibase formatted sql

--changeset akirilchev:crs-1.0-VTBCRS-262-adhoc-table logicalFilePath:crs-1.0-VTBCRS-262-adhoc-table endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tables where table_name = 'USERS'
create table users (
    id_user number,
    is_ad_hoc_table number,
    constraint users_pk primary key(id_user)
)
/

--changeset svaliev:crs-1.0-VTBCRS-43 logicalFilePath:crs-1.0-VTBCRS-43 endDelimiter:/
create table crs_sys_h_attribute
(
    id   number not null,
    key  varchar2(30) not null,
    ldts date not null
)
/
comment on table crs_sys_h_attribute is 'Entity attribute hub'
/
comment on column crs_sys_h_attribute.id is 'Identifier'
/
comment on column crs_sys_h_attribute.key is 'Attribute key'
/
comment on column crs_sys_h_attribute.ldts is 'Load date'
/
create unique index crs_sys_h_attribute_uk01 on crs_sys_h_attribute (key) tablespace spoindx
/
alter table crs_sys_h_attribute
  add constraint crs_sys_h_attribute_pk primary key (id)
/

create table crs_sys_h_atype
(
    id   number not null,
    key  varchar2(100) not null,
    ldts date not null
)
/
comment on table crs_sys_h_atype is 'Entity attribute type hub'
/
comment on column crs_sys_h_atype.id is 'Identifier'
/
comment on column crs_sys_h_atype.key is 'Attribute type key'
/
comment on column crs_sys_h_atype.ldts is 'Load date'
/
create index crs_sys_h_atype_uk01 on crs_sys_h_atype (key)
/
alter table crs_sys_h_atype
  add constraint crs_sys_h_atype_pk primary key (id)
/

create table crs_sys_h_entity
(
    id   number not null,
    key  varchar2(19) not null,
    ldts date not null
)
/
comment on table crs_sys_h_entity is 'Entity metadata hub'
/
comment on column crs_sys_h_entity.id is 'Identifier'
/
comment on column crs_sys_h_entity.key is 'Entity name'
/
comment on column crs_sys_h_entity.ldts is 'Load date'
/
create unique index crs_sys_h_entity_uk01 on crs_sys_h_entity (key) tablespace spoindx
/
alter table crs_sys_h_entity
  add constraint crs_sys_h_entity_pk primary key (id)
/

create table crs_sys_h_etype
(
    id   number not null,
    key  varchar2(100) not null,
    ldts date not null
)
/
comment on table crs_sys_h_etype is 'Entity type hub'
/
comment on column crs_sys_h_etype.id is 'Identifier'
/
comment on column crs_sys_h_etype.key is 'Entity type key'
/
comment on column crs_sys_h_etype.ldts is 'Load date'
/
create unique index crs_sys_h_etype_uk01 on crs_sys_h_etype (key) tablespace spoindx
/
alter table crs_sys_h_etype
  add constraint crs_sys_h_etype_pk primary key (id)
/

create table crs_sys_h_localization
(
    id   number not null,
    key  varchar2(100) not null,
    ldts date not null
)
/
comment on table crs_sys_h_localization is 'Entity localization hub'
/
comment on column crs_sys_h_localization.id is 'Identifier'
/
comment on column crs_sys_h_localization.key is 'localization key'
/
comment on column crs_sys_h_localization.ldts is 'Load date'
/
create unique index crs_sys_h_localization_uk01 on crs_sys_h_localization (key) tablespace spoindx
/
alter table crs_sys_h_localization
  add constraint crs_sys_h_localization_pk primary key (id)
/

create table crs_sys_l_attribute_atype
(
    id           number not null,
    attribute_id number not null,
    atype_id     number not null,
    ldts         date not null
)
/
comment on table crs_sys_l_attribute_atype is 'Link between attribute and attribute type'
/
comment on column crs_sys_l_attribute_atype.id is 'Identifier'
/
comment on column crs_sys_l_attribute_atype.attribute_id is 'Reference to entity attribute hub'
/
comment on column crs_sys_l_attribute_atype.atype_id is 'Reference to attribute type hub'
/
comment on column crs_sys_l_attribute_atype.ldts is 'Load date'
/
create index crs_sys_l_attribute_atype_i01 on crs_sys_l_attribute_atype (attribute_id) tablespace spoindx
/
create index crs_sys_l_attribute_atype_i02 on crs_sys_l_attribute_atype (atype_id) tablespace spoindx
/
alter table crs_sys_l_attribute_atype
  add constraint crs_sys_l_attribute_atype_pk primary key (id)
/
alter table crs_sys_l_attribute_atype
  add constraint crs_sys_l_attribute_atype_fk01 foreign key (attribute_id)
  references crs_sys_h_attribute (id)
/
alter table crs_sys_l_attribute_atype
  add constraint crs_sys_l_attribute_atype_fk02 foreign key (atype_id)
  references crs_sys_h_atype (id)
/

create table crs_sys_l_attribute_loc
(
    id              number not null,
    attribute_id    number not null,
    localization_id number not null,
    ldts            date not null
)
/
comment on table crs_sys_l_attribute_loc is 'Link between entity attribute and localization'
/
comment on column crs_sys_l_attribute_loc.id is 'Identifier'
/
comment on column crs_sys_l_attribute_loc.attribute_id is 'Reference to entity attribute hub'
/
comment on column crs_sys_l_attribute_loc.localization_id is 'Reference to localization hub'
/
comment on column crs_sys_l_attribute_loc.ldts is 'Load date'
/
create index crs_sys_l_attribute_loc_i01 on crs_sys_l_attribute_loc (attribute_id) tablespace spoindx
/
create index crs_sys_l_attribute_loc_i02 on crs_sys_l_attribute_loc (localization_id) tablespace spoindx
/
alter table crs_sys_l_attribute_loc
  add constraint crs_sys_l_attribute_loc_pk primary key (id)
/
alter table crs_sys_l_attribute_loc
  add constraint crs_sys_l_attribute_loc_fk01 foreign key (attribute_id)
  references crs_sys_h_attribute (id)
/
alter table crs_sys_l_attribute_loc
  add constraint crs_sys_l_attribute_loc_fk02 foreign key (localization_id)
  references crs_sys_h_localization (id)
/

create table crs_sys_l_atype_loc
(
    id              number not null,
    atype_id        number not null,
    localization_id number not null,
    ldts            date not null
)
/
comment on table crs_sys_l_atype_loc is 'Link between attribute type and localization'
/
comment on column crs_sys_l_atype_loc.id is 'Identifier'
/
comment on column crs_sys_l_atype_loc.atype_id is 'Reference to attribute type hub'
/
comment on column crs_sys_l_atype_loc.localization_id is 'Reference to localization hub'
/
comment on column crs_sys_l_atype_loc.ldts is 'Load date'
/
create index crs_sys_l_atype_loc_i01 on crs_sys_l_atype_loc (atype_id) tablespace spoindx
/
create index crs_sys_l_atype_loc_i02 on crs_sys_l_atype_loc (localization_id) tablespace spoindx
/
alter table crs_sys_l_atype_loc
  add constraint crs_sys_l_atype_loc_pk primary key (id)
/
alter table crs_sys_l_atype_loc
  add constraint crs_sys_l_atype_loc_fk01 foreign key (atype_id)
  references crs_sys_h_atype (id)
/
alter table crs_sys_l_atype_loc
  add constraint crs_sys_l_atype_loc_fk02 foreign key (localization_id)
  references crs_sys_h_localization (id)
/

create table crs_sys_l_entity_attribute
(
    id           number not null,
    entity_id    number not null,
    attribute_id number not null,
    ldts         date not null
)
/
comment on table crs_sys_l_entity_attribute is 'Link betwwen entity metadata and entity attribute'
/
comment on column crs_sys_l_entity_attribute.id is 'Identifier'
/
comment on column crs_sys_l_entity_attribute.entity_id is 'Reference to entity metadata hub'
/
comment on column crs_sys_l_entity_attribute.attribute_id is 'Reference to entity attribute hub'
/
comment on column crs_sys_l_entity_attribute.ldts is 'Load date'
/
create index crs_sys_l_entity_attr_i01 on crs_sys_l_entity_attribute (entity_id) tablespace spoindx
/
create index crs_sys_l_entity_attr_i02 on crs_sys_l_entity_attribute (attribute_id) tablespace spoindx
/
alter table crs_sys_l_entity_attribute
  add constraint crs_sys_l_entity_attr_pk primary key (id)
/
alter table crs_sys_l_entity_attribute
  add constraint crs_sys_l_entity_attr_fk01 foreign key (entity_id)
  references crs_sys_h_entity (id)
/
alter table crs_sys_l_entity_attribute
  add constraint crs_sys_l_entity_attr_fk02 foreign key (attribute_id)
  references crs_sys_h_attribute (id)
/

create table crs_sys_l_entity_etype
(
    id        number not null,
    etype_id  number not null,
    entity_id number not null,
    ldts      date not null
)
/
comment on table crs_sys_l_entity_etype is 'Link between entity metadata and entity type'
/
comment on column crs_sys_l_entity_etype.id is 'Identifier'
/
comment on column crs_sys_l_entity_etype.etype_id is 'Reference to entity type hub'
/
comment on column crs_sys_l_entity_etype.entity_id is 'Reference to entity metadata hub'
/
comment on column crs_sys_l_entity_etype.ldts is 'Load date'
/
create index crs_sys_l_entity_etype_i01 on crs_sys_l_entity_etype (etype_id) tablespace spoindx
/
create index crs_sys_l_entity_etype_i02 on crs_sys_l_entity_etype (entity_id) tablespace spoindx
/
alter table crs_sys_l_entity_etype
  add constraint crs_sys_l_entity_etype_pk primary key (id)
/
alter table crs_sys_l_entity_etype
  add constraint crs_sys_l_entity_etype_fk01 foreign key (etype_id)
  references crs_sys_h_etype (id)
/
alter table crs_sys_l_entity_etype
  add constraint crs_sys_l_entity_etype_fk02 foreign key (entity_id)
  references crs_sys_h_entity (id)
/

create table crs_sys_l_entity_loc
(
    id              number not null,
    entity_id       number not null,
    localization_id number not null,
    ldts            date not null
)
/
comment on table crs_sys_l_entity_loc is 'Link between entity metadata and localization'
/
comment on column crs_sys_l_entity_loc.id is 'Identifier'
/
comment on column crs_sys_l_entity_loc.entity_id is 'Reference to entity metadata hub'
/
comment on column crs_sys_l_entity_loc.localization_id is 'Reference to localization hub'
/
comment on column crs_sys_l_entity_loc.ldts is 'Load date'
/
create index crs_sys_l_entity_loc_i01 on crs_sys_l_entity_loc (entity_id) tablespace spoindx
/
create index crs_sys_l_entity_loc_i02 on crs_sys_l_entity_loc (localization_id) tablespace spoindx
/
alter table crs_sys_l_entity_loc
  add constraint crs_sys_l_entity_loc_pk primary key (id)
/
alter table crs_sys_l_entity_loc
  add constraint crs_sys_l_entity_loc_fk01 foreign key (entity_id)
  references crs_sys_h_entity (id)
/
alter table crs_sys_l_entity_loc
  add constraint crs_sys_l_entity_loc_fk02 foreign key (localization_id)
  references crs_sys_h_localization (id)
/

create table crs_sys_s_attribute
(
    id               number not null,
    h_id             number not null,
    ldts             date not null,
    edts             date,
    view_order       number(38) default 0 not null,
    nullable         number(1) default 1 not null,
    multilang        number(1) default 0 not null,
    link_table       varchar2(30),
    satelitte_column varchar2(30),
    link             as (nvl2(link_table, 1, 0))
)
/
comment on table crs_sys_s_attribute is 'Entity attribute satellite'
/
comment on column crs_sys_s_attribute.id is 'Identifier'
/
comment on column crs_sys_s_attribute.h_id is 'Reference to hub'
/
comment on column crs_sys_s_attribute.ldts is 'Load date'
/
comment on column crs_sys_s_attribute.edts is 'End date'
/
comment on column crs_sys_s_attribute.view_order is 'View order'
/
comment on column crs_sys_s_attribute.nullable is 'Nullable'
/
comment on column crs_sys_s_attribute.multilang is 'Multilang'
/
comment on column crs_sys_s_attribute.link_table is 'Link table'
/
comment on column crs_sys_s_attribute.satelitte_column is 'Satelitte column for display'
/
comment on column crs_sys_s_attribute.link is 'Flag of the link presence'
/
create index crs_sys_s_attribute_i01 on crs_sys_s_attribute (h_id) tablespace spoindx
/
create index crs_sys_s_attribute_i02 on crs_sys_s_attribute (edts) tablespace spoindx
/
create unique index crs_sys_s_attribute_uk01 on crs_sys_s_attribute (ldts, h_id) tablespace spoindx
/
alter table crs_sys_s_attribute
  add constraint crs_sys_s_attribute_pk primary key (id)
/
alter table crs_sys_s_attribute
  add constraint crs_sys_s_attribute_fk01 foreign key (h_id)
  references crs_sys_h_attribute (id)
/
alter table crs_sys_s_attribute
  add constraint crs_sys_s_attribute_ck01
check (nullable in (0, 1))
/
alter table crs_sys_s_attribute
  add constraint crs_sys_s_attribute_ck02
check (multilang in (0, 1))
/

create table crs_sys_s_entity
(
    id   number not null,
    h_id number not null,
    ldts date not null,
    edts date,
    form varchar2(100)
)
/
comment on table crs_sys_s_entity is 'Entity metadata satellite'
/
comment on column crs_sys_s_entity.id is 'Identifier'
/
comment on column crs_sys_s_entity.h_id is 'Reference to hub'
/
comment on column crs_sys_s_entity.ldts is 'Load date'
/
comment on column crs_sys_s_entity.edts is 'End date'
/
comment on column crs_sys_s_entity.form is 'UI form'
/
create index crs_sys_s_table_meta_i01 on crs_sys_s_entity (h_id) tablespace spoindx
/
create index crs_sys_s_table_meta_i02 on crs_sys_s_entity (edts) tablespace spoindx
/
create unique index crs_sys_s_table_meta_uk01 on crs_sys_s_entity (ldts, h_id) tablespace spoindx
/
alter table crs_sys_s_entity
  add constraint crs_sys_s_entity_pk primary key (id)
/
alter table crs_sys_s_entity
  add constraint crs_sys_s_entity_fk01 foreign key (h_id)
  references crs_sys_h_entity (id)
/

create table crs_sys_s_localization
(
    id      number not null,
    h_id    number not null,
    ldts    date not null,
    name_ru varchar2(4000) not null,
    name_en varchar2(4000) not null,
    edts    date
)
/
comment on table crs_sys_s_localization is 'Entity localization satellite'
/
comment on column crs_sys_s_localization.id is 'Identifier'
/
comment on column crs_sys_s_localization.h_id is 'Reference to hub'
/
comment on column crs_sys_s_localization.ldts is 'Load date'
/
comment on column crs_sys_s_localization.name_ru is 'Name (ru)'
/
comment on column crs_sys_s_localization.name_en is 'Name (en)'
/
comment on column crs_sys_s_localization.edts is 'End date'
/
create index crs_sys_s_localization_i01 on crs_sys_s_localization (h_id) tablespace spoindx
/
create index crs_sys_s_localization_i02 on crs_sys_s_localization (edts) tablespace spoindx
/
create unique index crs_sys_s_localization_uk01 on crs_sys_s_localization (ldts, h_id) tablespace spoindx
/
alter table crs_sys_s_localization
  add constraint crs_sys_s_localization_pk primary key (id)
/
alter table crs_sys_s_localization
  add constraint crs_sys_s_localization_fk01 foreign key (h_id)
  references crs_sys_h_localization (id)
/

create sequence crs_sys_h_attribute_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_h_atype_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_h_entity_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_h_etype_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_h_localization_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_l_attribute_atype_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_l_attribute_loc_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_l_atype_loc_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_l_entity_attribute_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_l_entity_etype_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_l_entity_loc_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_s_attribute_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_s_entity_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_sys_s_localization_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset svaliev:crs-1.0-VTBCRS-45-structure-refactoring logicalFilePath:crs-1.0-VTBCRS-45-structure-refactoring endDelimiter:/
drop table crs_sys_l_attribute_loc
/
drop table crs_sys_l_atype_loc
/
drop table crs_sys_l_entity_loc
/
drop table crs_sys_s_localization
/
drop table crs_sys_h_localization
/
drop sequence crs_sys_l_attribute_loc_seq
/
drop sequence crs_sys_l_atype_loc_seq
/
drop sequence crs_sys_l_entity_loc_seq
/
drop sequence crs_sys_s_localization_seq
/
drop sequence crs_sys_h_localization_seq
/

alter table crs_sys_s_attribute add name_ru varchar2(4000) not null
/
alter table crs_sys_s_attribute add name_en varchar2(4000) not null
/
comment on column crs_sys_s_attribute.name_ru is 'Name (ru)'
/
comment on column crs_sys_s_attribute.name_en is 'Name (en)'
/

alter table crs_sys_s_entity add name_ru varchar2(4000) not null
/
alter table crs_sys_s_entity add name_en varchar2(4000) not null
/
comment on column crs_sys_s_entity.name_ru is 'Name (ru)'
/
comment on column crs_sys_s_entity.name_en is 'Name (en)'
/

create table crs_sys_s_atype
(
    id      number not null,
    h_id    number not null,
    ldts    date not null,
    edts    date,
    name_ru varchar2(4000) not null,
    name_en varchar2(4000) not null
)
/
comment on table crs_sys_s_atype is 'Entity attribute type satellite'
/
comment on column crs_sys_s_atype.id is 'Identifier'
/
comment on column crs_sys_s_atype.h_id is 'Reference to hub'
/
comment on column crs_sys_s_atype.ldts is 'Load date'
/
comment on column crs_sys_s_atype.edts is 'End date'
/
comment on column crs_sys_s_atype.name_ru is 'Name (ru)'
/
comment on column crs_sys_s_atype.name_en is 'Name (en)'
/
create index crs_sys_s_atype_i01 on crs_sys_s_atype (h_id) tablespace spoindx
/
create index crs_sys_s_atype_i02 on crs_sys_s_atype (edts) tablespace spoindx
/
create unique index crs_sys_s_atype_uk01 on crs_sys_s_atype (ldts, h_id) tablespace spoindx
/
alter table crs_sys_s_atype add constraint crs_sys_s_atype_pk primary key (id)
/
alter table crs_sys_s_atype
  add constraint crs_sys_s_atype_fk01 foreign key (h_id)
  references crs_sys_h_atype (id)
/

create sequence crs_sys_s_atype_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset svaliev:crs-1.0-VTBCRS-45 logicalFilePath:crs-1.0-VTBCRS-45 endDelimiter:/
declare
    v_ldts date := sysdate;
begin
    insert into crs_sys_h_etype (id, key, ldts)
    values (crs_sys_h_etype_seq.nextval, 'DICTIONARY', v_ldts);

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'BOOLEAN', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Булевый тип', 'Boolean');

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'STRING', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Строка', 'String');

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'TEXT', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Текст', 'Text');

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'FILE', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Файл', 'File');

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'NUMBER', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Число', 'Number');

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'DATE', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Дата', 'Date');

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'DATETIME', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Дата и время', 'DateTime');

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'REFERENCE', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Связь', 'Reference');

    insert into crs_sys_h_atype (id, key, ldts)
    values (crs_sys_h_atype_seq.nextval, 'SELF_REFERENCE', v_ldts);
    insert into crs_sys_s_atype (id, h_id, ldts, name_ru, name_en)
    values (crs_sys_s_atype_seq.nextval, crs_sys_h_atype_seq.currval, v_ldts, 'Иерархическая связь', 'Hierarhy reference');
end;
/

--changeset svaliev:crs-1.0-VTBCRS-46 logicalFilePath:crs-1.0-VTBCRS-46 endDelimiter:/
alter table crs_sys_s_attribute add scale number(38)
/
comment on column crs_sys_s_attribute.scale is 'Type scale'
/

--changeset svaliev:crs-1.0-VTBCRS-47 logicalFilePath:crs-1.0-VTBCRS-47 endDelimiter:/
create table crs_h_localization
(
    id   number not null,
    key  varchar2(100) not null,
    ldts date not null
)
/
comment on table crs_h_localization is 'Data localization hub'
/
comment on column crs_h_localization.id is 'Identifier'
/
comment on column crs_h_localization.key is 'Attribute key'
/
comment on column crs_h_localization.ldts is 'Load date'
/
create unique index crs_h_localization_uk01 on crs_h_localization (key) tablespace spoindx
/
alter table crs_h_localization
  add constraint crs_h_localization_pk primary key (id)
/

create table crs_s_localization
(
    id        number not null,
    h_id      number not null,
    ldts      date not null,
    edts      date,
    string_ru varchar2(4000),
    string_en varchar2(4000),
    text_ru   clob,
    text_en   clob
)
/
comment on table crs_s_localization is 'Data localization satellite'
/
comment on column crs_s_localization.id is 'Identifier'
/
comment on column crs_s_localization.h_id is 'Reference to hub'
/
comment on column crs_s_localization.ldts is 'Load date'
/
comment on column crs_s_localization.edts is 'End date'
/
comment on column crs_s_localization.string_ru is 'String value (ru)'
/
comment on column crs_s_localization.string_en is 'String value (en)'
/
comment on column crs_s_localization.text_ru is 'Text value (ru)'
/
comment on column crs_s_localization.text_en is 'Text value (en)'
/
create index crs_s_localization_i01 on crs_s_localization (h_id) tablespace spoindx
/
create index crs_s_localization_i02 on crs_s_localization (edts) tablespace spoindx
/
create unique index crs_s_localization_uk01 on crs_s_localization (ldts, h_id) tablespace spoindx
/
alter table crs_s_localization
  add constraint crs_s_localization_pk primary key (id)
/
alter table crs_s_localization
  add constraint crs_s_localization_fk01 foreign key (h_id)
  references crs_h_localization (id)
/

create sequence crs_h_localization_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_s_localization_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_h_file_storage
(
    id   number not null,
    key  varchar2(100) not null,
    ldts date not null
)
/
comment on table crs_h_file_storage is 'File storage hub'
/
comment on column crs_h_file_storage.id is 'Identifier'
/
comment on column crs_h_file_storage.key is 'Attribute key'
/
comment on column crs_h_file_storage.ldts is 'Load date'
/
create unique index crs_h_file_storage_uk01 on crs_h_file_storage (key) tablespace spoindx
/
alter table crs_h_file_storage
  add constraint crs_h_file_storage_pk primary key (id)
/
create table crs_s_file_storage
(
    id        number not null,
    h_id      number not null,
    ldts      date not null,
    edts      date,
    mime_type varchar2(100),
    name      varchar2(500),
    data      blob
)
/
comment on table crs_s_file_storage is 'File storage satellite'
/
comment on column crs_s_file_storage.id is 'Identifier'
/
comment on column crs_s_file_storage.h_id is 'Reference to hub'
/
comment on column crs_s_file_storage.ldts is 'Load date'
/
comment on column crs_s_file_storage.edts is 'End date'
/
comment on column crs_s_file_storage.mime_type is 'mime type'
/
comment on column crs_s_file_storage.name is 'Filename'
/
comment on column crs_s_file_storage.data is 'Filedata'
/
create index crs_s_file_storage_i01 on crs_s_file_storage (h_id) tablespace spoindx
/
create index crs_s_file_storage_i02 on crs_s_file_storage (edts) tablespace spoindx
/
create unique index crs_s_file_storage_uk01 on crs_s_file_storage (ldts, h_id) tablespace spoindx
/
alter table crs_s_file_storage
  add constraint crs_s_file_storage_pk primary key (id)
/
alter table crs_s_file_storage
  add constraint crs_s_file_storage_fk01 foreign key (h_id)
  references crs_h_file_storage (id)
/

create sequence crs_h_file_storage_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/
create sequence crs_s_file_storage_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

alter table crs_sys_s_attribute add filter_available number(1) default 0 not null
/
comment on column crs_sys_s_attribute.filter_available is 'If column filtering is available'
/

create index crs_sys_h_attribute_i01 on crs_sys_h_attribute (ldts) tablespace spoindx
/
create index crs_sys_h_atype_i01 on crs_sys_h_atype (ldts) tablespace spoindx
/
create index crs_sys_h_entity_i01 on crs_sys_h_entity (ldts) tablespace spoindx
/
create index crs_sys_h_etype_i01 on crs_sys_h_etype (ldts) tablespace spoindx
/

create index crs_sys_l_attribute_atype_i03 on crs_sys_l_attribute_atype (ldts) tablespace spoindx
/
create index crs_sys_l_entity_attribute_i03 on crs_sys_l_entity_attribute (ldts) tablespace spoindx
/
create index crs_sys_l_entity_etype_i03 on crs_sys_l_entity_etype (ldts) tablespace spoindx
/

--changeset svaliev:crs-1.0-attribute-key-length logicalFilePath:crs-1.0-attribute-key-length endDelimiter:/
alter table crs_h_file_storage modify key varchar2(30)
/
alter table crs_h_localization modify key varchar2(30)
/

--changeset svaliev:crs-1.0-ldts-indexes logicalFilePath:crs-1.0-ldts-indexes endDelimiter:/
create index crs_h_file_storage_i01 on crs_h_file_storage (ldts) tablespace spoindx
/
create index crs_h_localization_i01 on crs_h_localization (ldts) tablespace spoindx
/

--changeset svaliev:crs-1.0-ldts-date-to-timestamp logicalFilePath:crs-1.0-ldts-date-to-timestamp endDelimiter:/
alter table crs_h_file_storage modify ldts timestamp
/
alter table crs_h_localization modify ldts timestamp
/
alter table crs_sys_h_attribute modify ldts timestamp
/
alter table crs_sys_h_atype modify ldts timestamp
/
alter table crs_sys_h_entity modify ldts timestamp
/
alter table crs_sys_h_etype modify ldts timestamp
/
alter table crs_sys_l_attribute_atype modify ldts timestamp
/
alter table crs_sys_l_entity_attribute modify ldts timestamp
/
alter table crs_sys_l_entity_etype modify ldts timestamp
/
alter table crs_sys_s_attribute modify ldts timestamp
/
alter table crs_sys_s_atype modify ldts timestamp
/
alter table crs_sys_s_entity modify ldts timestamp
/
alter table crs_s_file_storage modify ldts timestamp
/
alter table crs_s_localization modify ldts timestamp
/
alter table crs_sys_s_attribute modify edts timestamp
/
alter table crs_sys_s_atype modify edts timestamp
/
alter table crs_sys_s_entity modify edts timestamp
/
alter table crs_s_file_storage modify edts timestamp
/
alter table crs_s_localization modify edts timestamp
/

--changeset svaliev:crs-1.0-add-unique-keys-to-hubs logicalFilePath:crs-1.0-add-unique-keys-to-hubs endDelimiter:/
drop index crs_h_file_storage_uk01
/
drop index crs_h_localization_uk01
/
drop index crs_sys_h_attribute_uk01
/
drop index crs_sys_h_atype_uk01
/
drop index crs_sys_h_entity_uk01
/
drop index crs_sys_h_etype_uk01
/
alter table crs_h_file_storage add constraint crs_h_file_storage_uk01 unique (key)
/
alter table crs_h_localization add constraint crs_h_localization_uk01 unique (key)
/
alter table crs_sys_h_attribute add constraint crs_sys_h_attribute_uk01 unique (key)
/
alter table crs_sys_h_atype add constraint crs_sys_h_atype_uk01 unique (key)
/
alter table crs_sys_h_entity add constraint crs_sys_h_entity_uk01 unique (key)
/
alter table crs_sys_h_etype add constraint crs_sys_h_etype_uk01 unique (key)
/

--changeset svaliev:crs-1.0-attribute-meta-refactor logicalFilePath:crs-1.0-attribute-meta-refactor endDelimiter:/
alter table crs_sys_s_attribute drop column scale
/
alter table crs_sys_s_attribute rename column satelitte_column to attribute_key
/
comment on column crs_sys_s_attribute.attribute_key is 'Satellite column for display'
/
alter table crs_sys_s_attribute add entity_key varchar2(19)
/
comment on column crs_sys_s_attribute.entity_key is 'Link to entity'
/
alter table crs_sys_s_attribute
  add constraint crs_sys_s_attribute_fk02 foreign key (entity_key)
  references crs_sys_h_entity (key)
/
alter table crs_sys_s_attribute
  add constraint crs_sys_s_attribute_fk03 foreign key (attribute_key)
  references crs_sys_h_attribute (key)
/
create index crs_sys_s_attribute_i03 on crs_sys_s_attribute (entity_key) tablespace spoindx
/
create index crs_sys_s_attribute_i04 on crs_sys_s_attribute (attribute_key) tablespace spoindx
/

--changeset svaliev:crs-1.0-file-storage-refactor logicalFilePath:crs-1.0-file-storage-refactor endDelimiter:/
alter table crs_s_file_storage drop column data
/

create table crs_s_file_storage_data
(
    id        number not null,
    h_id      number not null,
    ldts      date not null,
    edts      date,
    data      blob
)
/
comment on table crs_s_file_storage_data is 'File storage satellite'
/
comment on column crs_s_file_storage_data.id is 'Identifier'
/
comment on column crs_s_file_storage_data.h_id is 'Reference to hub'
/
comment on column crs_s_file_storage_data.ldts is 'Load date'
/
comment on column crs_s_file_storage_data.edts is 'End date'
/
comment on column crs_s_file_storage_data.data is 'Filedata'
/
create index crs_s_file_storage_data_i01 on crs_s_file_storage_data (h_id) tablespace spoindx
/
create index crs_s_file_storage_data_i02 on crs_s_file_storage_data (edts) tablespace spoindx
/
create unique index crs_s_file_storage_data_uk01 on crs_s_file_storage_data (ldts, h_id) tablespace spoindx
/
alter table crs_s_file_storage_data
  add constraint crs_s_file_storage_data_pk primary key (id)
/
alter table crs_s_file_storage_data
  add constraint crs_s_file_storage_data_fk01 foreign key (h_id)
  references crs_h_file_storage (id)
/
create sequence crs_s_file_storage_data_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset svaliev:crs-1.0-entity-satellite-refactor logicalFilePath:crs-1.0-entity-satellite-refactor endDelimiter:/
alter table crs_sys_s_entity add link_table varchar2(30)
/
alter table crs_sys_s_entity add attribute_key varchar2(30)
/
alter table crs_sys_s_entity add hierarchical number(1) default 0 not null
/
comment on column crs_sys_s_entity.link_table is 'Link table'
/
comment on column crs_sys_s_entity.attribute_key is 'Satellite column for display'
/
comment on column crs_sys_s_entity.hierarchical is 'Hierarchical flag'
/
create index crs_sys_s_entity_i03 on crs_sys_s_entity (attribute_key) tablespace spoindx
/
alter table crs_sys_s_entity
  add constraint crs_sys_s_entity_fk02 foreign key (attribute_key)
  references crs_sys_h_attribute (key)
/
alter table crs_sys_s_entity
  add constraint crs_sys_s_entity_ck01
check (hierarchical in (0, 1))
/

--changeset svaliev:crs-1.0-remove-edts logicalFilePath:crs-1.0-remove-edts endDelimiter:/
alter table crs_sys_l_entity_attribute add removed number(1) default 0 not null
/
comment on column crs_sys_l_entity_attribute.removed is 'Removed flag'
/
alter table crs_sys_l_entity_attribute
  add constraint crs_sys_l_entity_attr_ck01
check (removed in (0, 1))
/

alter table crs_sys_l_entity_etype add removed number(1) default 0 not null
/
comment on column crs_sys_l_entity_etype.removed is 'Removed flag'
/
alter table crs_sys_l_entity_etype
  add constraint crs_sys_l_entity_etype_ck01
check (removed in (0, 1))
/

alter table crs_sys_s_attribute add removed number(1) default 0 not null
/
comment on column crs_sys_s_attribute.removed is 'Removed flag'
/
alter table crs_sys_s_attribute
  add constraint crs_sys_s_attribute_ck03
check (removed in (0, 1))
/
drop index crs_sys_s_attribute_i02
/
alter table crs_sys_s_attribute drop column edts
/
alter index crs_sys_s_attribute_i03 rename to crs_sys_s_attribute_i02
/
alter index crs_sys_s_attribute_i04 rename to crs_sys_s_attribute_i03
/

alter table crs_sys_s_atype add removed number(1) default 0 not null
/
comment on column crs_sys_s_atype.removed is 'Removed flag'
/
alter table crs_sys_s_atype
  add constraint crs_sys_s_atype_ck01
check (removed in (0, 1))
/
drop index crs_sys_s_atype_i02
/
alter table crs_sys_s_atype drop column edts
/

alter table crs_sys_s_entity add removed number(1) default 0 not null
/
comment on column crs_sys_s_entity.removed is 'Removed flag'
/
alter table crs_sys_s_entity
  add constraint crs_sys_s_entity_ck02
check (removed in (0, 1))
/
drop index crs_sys_s_table_meta_i02
/
alter table crs_sys_s_entity drop column edts
/
alter index crs_sys_s_entity_i03 rename to crs_sys_s_entity_i02
/
alter index crs_sys_s_table_meta_i01 rename to crs_sys_s_entity_i01
/
alter index crs_sys_s_table_meta_uk01 rename to crs_sys_s_entity_uk01
/

alter table crs_s_file_storage add removed number(1) default 0 not null
/
comment on column crs_s_file_storage.removed is 'Removed flag'
/
alter table crs_s_file_storage
  add constraint crs_s_file_storage_ck01
check (removed in (0, 1))
/
drop index crs_s_file_storage_i02
/
alter table crs_s_file_storage drop column edts
/

alter table crs_s_file_storage_data add removed number(1) default 0 not null
/
comment on column crs_s_file_storage_data.removed is 'Removed flag'
/
alter table crs_s_file_storage_data
  add constraint crs_s_file_storage_data_ck01
check (removed in (0, 1))
/
drop index crs_s_file_storage_data_i02
/
alter table crs_s_file_storage_data drop column edts
/

alter table crs_s_localization add removed number(1) default 0 not null
/
comment on column crs_s_localization.removed is 'Removed flag'
/
alter table crs_s_localization
  add constraint crs_s_localization_ck01
check (removed in (0, 1))
/
drop index crs_s_localization_i02
/
alter table crs_s_localization drop column edts
/

--changeset svaliev:crs-1.0-VTBCRS-49 logicalFilePath:crs-1.0-VTBCRS-49 endDelimiter:/
comment on column crs_sys_h_entity.key is 'Entity key'
/
alter table crs_sys_h_entity add constraint crs_sys_h_entity_ck01 check (key = upper(trim(key)))
/
alter table crs_sys_h_attribute add constraint crs_sys_h_attribute_ck01 check (key = upper(trim(key)))
/
alter table crs_h_localization add constraint crs_h_localization_ck01 check (key = upper(trim(key)))
/
alter table crs_h_file_storage add constraint crs_h_file_storage_ck01 check (key = upper(trim(key)))
/

delete crs_sys_s_atype ats
 where ats.h_id = (select at.id
                     from crs_sys_h_atype at
                    where at.key = 'SELF_REFERENCE')
/
delete crs_sys_h_atype at
 where at.key = 'SELF_REFERENCE'
/

alter table crs_sys_s_entity modify link_table not null
/
comment on column crs_sys_s_entity.link_table is 'Link table for hierarchy support'
/

--changeset svaliev:crs-1.0-satellites-unique-constraints logicalFilePath:crs-1.0-satellites-unique-constraints endDelimiter:/
drop index crs_sys_s_attribute_uk01
/
alter table crs_sys_s_attribute add constraint crs_sys_s_attribute_uk01 unique (ldts, h_id)
/
drop index crs_sys_s_atype_uk01
/
alter table crs_sys_s_atype add constraint crs_sys_s_atype_uk01 unique (ldts, h_id)
/
drop index crs_sys_s_entity_uk01
/
alter table crs_sys_s_entity add constraint crs_sys_s_entity_uk01 unique (ldts, h_id)
/
drop index crs_s_file_storage_uk01
/
alter table crs_s_file_storage add constraint crs_s_file_storage_uk01 unique (ldts, h_id)
/
drop index crs_s_file_storage_data_uk01
/
alter table crs_s_file_storage_data add constraint crs_s_file_storage_data_uk01 unique (ldts, h_id)
/
drop index crs_s_localization_uk01
/
alter table crs_s_localization add constraint crs_s_localization_uk01 unique (ldts, h_id)
/

--changeset svaliev:crs-1.0-VTBCRS-65 logicalFilePath:crs-1.0-VTBCRS-65 endDelimiter:/
comment on table crs_sys_l_entity_attribute is 'Link between entity metadata and entity attribute'
/
alter index crs_sys_l_entity_attribute_i03 rename to crs_sys_l_entity_attr_i03
/

create table crs_h_calc_model
(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null
)
/
comment on table crs_h_calc_model is 'Calculation model hub'
/
comment on column crs_h_calc_model.id is 'Identifier'
/
comment on column crs_h_calc_model.key is 'Key'
/
comment on column crs_h_calc_model.ldts is 'Load date'
/
create unique index crs_h_calc_model_uk01 on crs_h_calc_model (key) tablespace spoindx
/
alter table crs_h_calc_model
  add constraint crs_h_calc_model_pk primary key (id)
/
alter table crs_h_calc_model add constraint crs_h_calc_model_ck01 check (key = upper(trim(key)))
/
create index crs_h_calc_model_i01 on crs_h_calc_model (ldts) tablespace spoindx
/
create sequence crs_h_calc_model_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_s_calc_model
(
    id number not null,
    h_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null,
    digest varchar2(100) not null,
    name_ru varchar2(4000) not null,
    name_en varchar2(4000) not null,
    published number(1) default 0 not null,
    actuality timestamp not null,
    version number(38),
    comment_ru clob,
    comment_en clob
)
/
comment on table crs_s_calc_model is 'Calculation model satellite'
/
comment on column crs_s_calc_model.id is 'Identifier'
/
comment on column crs_s_calc_model.h_id is 'Reference to hub'
/
comment on column crs_s_calc_model.ldts is 'Load date'
/
comment on column crs_s_calc_model.removed is 'Removed flag'
/
comment on column crs_s_calc_model.digest is 'Row digest'
/
comment on column crs_s_calc_model.name_ru is 'Name (ru)'
/
comment on column crs_s_calc_model.name_en is 'Name (en)'
/
comment on column crs_s_calc_model.published is 'Published model'
/
comment on column crs_s_calc_model.actuality is 'Point in time the current model'
/
comment on column crs_s_calc_model.version is 'Version'
/
comment on column crs_s_calc_model.comment_ru is 'Comment (ru)'
/
comment on column crs_s_calc_model.comment_en is 'Comment (en)'
/
create index crs_s_calc_model_i01 on crs_s_calc_model (h_id) tablespace spoindx
/
alter table crs_s_calc_model
  add constraint crs_s_calc_model_pk primary key (id)
/
alter table crs_s_calc_model
  add constraint crs_s_calc_model_fk01 foreign key (h_id)
  references crs_h_calc_model (id)
/
alter table crs_s_calc_model
  add constraint crs_s_calc_model_uk01 unique (ldts, h_id)
/
alter table crs_s_calc_model add constraint crs_s_calc_model_ck01 check (removed in (0, 1))
/
alter table crs_s_calc_model add constraint crs_s_calc_model_ck02 check (published in (0, 1))
/
create sequence crs_s_calc_model_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_h_calc_formula
(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null
)
/
comment on table crs_h_calc_formula is 'Calculation formula hub'
/
comment on column crs_h_calc_formula.id is 'Identifier'
/
comment on column crs_h_calc_formula.key is 'Key'
/
comment on column crs_h_calc_formula.ldts is 'Load date'
/
create unique index crs_h_calc_formula_uk01 on crs_h_calc_formula (key) tablespace spoindx
/
alter table crs_h_calc_formula
  add constraint crs_h_calc_formula_pk primary key (id)
/
alter table crs_h_calc_formula add constraint crs_h_calc_formula_ck01 check (key = upper(trim(key)))
/
create index crs_h_calc_formula_i01 on crs_h_calc_formula (ldts) tablespace spoindx
/
create sequence crs_h_calc_formula_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_s_calc_formula_desc
(
    id number not null,
    h_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null,
    digest varchar2(100) not null,
    name_ru varchar2(4000) not null,
    name_en varchar2(4000) not null,
    comment_ru clob,
    comment_en clob
)
/
comment on table crs_s_calc_formula_desc is 'Calculation formula description satellite'
/
comment on column crs_s_calc_formula_desc.id is 'Identifier'
/
comment on column crs_s_calc_formula_desc.h_id is 'Reference to hub'
/
comment on column crs_s_calc_formula_desc.ldts is 'Load date'
/
comment on column crs_s_calc_formula_desc.removed is 'Removed flag'
/
comment on column crs_s_calc_formula_desc.digest is 'Row digest'
/
comment on column crs_s_calc_formula_desc.name_ru is 'Name (ru)'
/
comment on column crs_s_calc_formula_desc.name_en is 'Name (en)'
/
comment on column crs_s_calc_formula_desc.comment_ru is 'Comment (ru)'
/
comment on column crs_s_calc_formula_desc.comment_en is 'Comment (en)'
/
create index crs_s_calc_formula_desc_i01 on crs_s_calc_formula_desc (h_id) tablespace spoindx
/
alter table crs_s_calc_formula_desc
  add constraint crs_s_calc_formula_desc_pk primary key (id)
/
alter table crs_s_calc_formula_desc
  add constraint crs_s_calc_formula_desc_fk01 foreign key (h_id)
  references crs_h_calc_formula (id)
/
alter table crs_s_calc_formula_desc
  add constraint crs_s_calc_formula_desc_uk01 unique (ldts, h_id)
/
alter table crs_s_calc_formula_desc add constraint crs_s_calc_formula_desc_ck01 check (removed in (0, 1))
/
create sequence crs_s_calc_formula_desc_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_s_calc_formula
(
    id number not null,
    h_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null,
    digest varchar2(100) not null,
    formula clob not null
)
/
comment on table crs_s_calc_formula is 'Calculation formula satellite'
/
comment on column crs_s_calc_formula.id is 'Identifier'
/
comment on column crs_s_calc_formula.h_id is 'Reference to hub'
/
comment on column crs_s_calc_formula.ldts is 'Load date'
/
comment on column crs_s_calc_formula.removed is 'Removed flag'
/
comment on column crs_s_calc_formula.digest is 'Row digest'
/
comment on column crs_s_calc_formula.formula is 'Formula data'
/
create index crs_s_calc_formula_i01 on crs_s_calc_formula (h_id) tablespace spoindx
/
alter table crs_s_calc_formula
  add constraint crs_s_calc_formula_pk primary key (id)
/
alter table crs_s_calc_formula
  add constraint crs_s_calc_formula_fk01 foreign key (h_id)
references crs_h_calc_formula (id)
/
alter table crs_s_calc_formula
  add constraint crs_s_calc_formula_uk01 unique (ldts, h_id)
/
alter table crs_s_calc_formula add constraint crs_s_calc_formula_ck01 check (removed in (0, 1))
/
create sequence crs_s_calc_formula_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_l_calc_formula
(
    id number not null,
    formula_id number not null,
    formula_parent_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null
)
/
comment on table crs_l_calc_formula is 'Calculation formula self reference'
/
comment on column crs_l_calc_formula.id is 'Identifier'
/
comment on column crs_l_calc_formula.formula_id is 'Reference to calculation formula'
/
comment on column crs_l_calc_formula.formula_parent_id is 'Reference to parent calculation formula'
/
comment on column crs_l_calc_formula.ldts is 'Load date'
/
comment on column crs_l_calc_formula.removed is 'Removed flag'
/
create index crs_l_calc_formula_i01 on crs_l_calc_formula (formula_id) tablespace spoindx
/
create index crs_l_calc_formula_i02 on crs_l_calc_formula (formula_parent_id) tablespace spoindx
/
create index crs_l_calc_formula_i03 on crs_l_calc_formula (ldts) tablespace spoindx
/
alter table crs_l_calc_formula add constraint crs_l_calc_formula_pk primary key (id)
/
alter table crs_l_calc_formula
  add constraint crs_l_calc_formula_fk01 foreign key (formula_id)
  references crs_h_calc_formula (id)
/
alter table crs_l_calc_formula
  add constraint crs_l_calc_formula_fk02 foreign key (formula_parent_id)
  references crs_h_calc_formula (id)
/
alter table crs_l_calc_formula add constraint crs_l_calc_formula_ck01 check (removed in (0, 1))
/
create sequence crs_l_calc_formula_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_l_calc_model_formula
(
    id number not null,
    model_id number not null,
    formula_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null
)
/
comment on table crs_l_calc_model_formula is 'Link between calculation model and formula'
/
comment on column crs_l_calc_model_formula.id is 'Identifier'
/
comment on column crs_l_calc_model_formula.model_id is 'Reference to calculation model'
/
comment on column crs_l_calc_model_formula.formula_id is 'Reference to calculation formula'
/
comment on column crs_l_calc_model_formula.ldts is 'Load date'
/
comment on column crs_l_calc_model_formula.removed is 'Removed flag'
/
create index crs_l_calc_model_formula_i01 on crs_l_calc_model_formula (model_id) tablespace spoindx
/
create index crs_l_calc_model_formula_i02 on crs_l_calc_model_formula (formula_id) tablespace spoindx
/
create index crs_l_calc_model_formula_i03 on crs_l_calc_model_formula (ldts) tablespace spoindx
/
alter table crs_l_calc_model_formula add constraint crs_l_calc_model_formula_pk primary key (id)
/
alter table crs_l_calc_model_formula
  add constraint crs_l_calc_model_formula_fk01 foreign key (model_id)
  references crs_h_calc_model (id)
/
alter table crs_l_calc_model_formula
  add constraint crs_l_calc_model_formula_fk02 foreign key (formula_id)
  references crs_h_calc_formula (id)
/
alter table crs_l_calc_model_formula add constraint crs_l_calc_model_formula_ck01 check (removed in (0, 1))
/
create sequence crs_l_calc_model_formula_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset svaliev:crs-1.0-VTBCRS-65-using-unique-check logicalFilePath:crs-1.0-VTBCRS-65-using-unique-check endDelimiter:/
drop index crs_h_calc_model_uk01
/
alter table crs_h_calc_model add constraint crs_h_calc_model_uk01 unique (key)
/
drop index crs_h_calc_formula_uk01
/
alter table crs_h_calc_formula add constraint crs_h_calc_formula_uk01 unique (key)
/

--changeset achalov:crs-1.0-VTBCRS-71 logicalFilePath:crs-1.0-VTBCRS-71 endDelimiter:/
alter table crs_s_calc_formula_desc add library number(1) default 0 not null
/
alter table crs_s_calc_formula_desc add eval_lang varchar(100) not null
/
alter table crs_s_calc_formula_desc
  add constraint crs_s_calc_formula_desc_ck02
check (library in (0, 1))
/

--changeset svaliev:crs-1.0-VTBCRS-75 logicalFilePath:crs-1.0-VTBCRS-75 endDelimiter:/
delete crs_sys_l_entity_attribute
/
delete crs_sys_l_entity_etype
/
delete crs_sys_l_attribute_atype
/
delete crs_sys_s_entity
/
delete crs_sys_s_attribute
/
delete crs_sys_h_entity
/
delete crs_sys_h_attribute
/

alter table crs_sys_s_attribute add type varchar2(100) not null
/
comment on column crs_sys_s_attribute.type is 'Attribute type'
/

rename crs_sys_h_etype to crs_sys_h_entity_type
/
alter index crs_sys_h_etype_i01 rename to crs_sys_h_entity_type_i01
/
alter index crs_sys_h_etype_pk rename to crs_sys_h_entity_type_pk
/
alter index crs_sys_h_etype_uk01 rename to crs_sys_h_entity_type_uk01
/
alter table crs_sys_h_entity_type rename constraint crs_sys_h_etype_pk to crs_sys_h_entity_type_pk
/
alter table crs_sys_h_entity_type rename constraint crs_sys_h_etype_uk01 to crs_sys_h_entity_type_uk01
/

rename crs_sys_l_entity_etype to crs_sys_l_entity_type
/
alter table crs_sys_l_entity_type rename column etype_id to type_id
/
alter index crs_sys_l_entity_etype_i02 rename to crs_sys_l_entity_type_i02
/
alter index crs_sys_l_entity_etype_i03 rename to crs_sys_l_entity_type_i03
/
alter index crs_sys_l_entity_etype_pk rename to crs_sys_l_entity_type_pk
/
drop index crs_sys_l_entity_etype_i01
/
create index crs_sys_l_entity_type_i01 on crs_sys_l_entity_type (type_id) tablespace spoindx
/
alter table crs_sys_l_entity_type rename constraint crs_sys_l_entity_etype_pk to crs_sys_l_entity_type_pk
/
alter table crs_sys_l_entity_type rename constraint crs_sys_l_entity_etype_fk02 to crs_sys_l_entity_type_fk02
/
alter table crs_sys_l_entity_type drop constraint crs_sys_l_entity_etype_fk01
/
alter table crs_sys_l_entity_type
  add constraint crs_sys_l_entity_type_fk01 foreign key (type_id)
references crs_sys_h_entity_type (id)
/
alter table crs_sys_l_entity_type rename constraint crs_sys_l_entity_etype_ck01 to crs_sys_l_entity_type_ck01
/

rename crs_sys_h_etype_seq to crs_sys_h_entity_type_seq
/
rename crs_sys_l_entity_etype_seq to crs_sys_l_entity_type_seq
/

drop table crs_sys_l_attribute_atype
/
drop table crs_sys_s_atype
/
drop table crs_sys_h_atype
/
drop sequence crs_sys_h_atype_seq
/
drop sequence crs_sys_l_attribute_atype_seq
/
drop sequence crs_sys_s_atype_seq
/

--changeset svaliev:crs-1.0-preactions-VTBCRS-72 logicalFilePath:crs-1.0-VTBCRS-preactions-72 endDelimiter:/
delete crs_l_calc_model_formula
/
delete crs_s_calc_formula_desc
/
delete crs_s_calc_formula
/
delete crs_h_calc_formula
/

--changeset svaliev:crs-1.0-VTBCRS-72 logicalFilePath:crs-1.0-VTBCRS-72 endDelimiter:/
create table crs_h_calc
(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null
)
/
comment on table crs_h_calc is 'Calculation hub'
/
comment on column crs_h_calc.id is 'Identifier'
/
comment on column crs_h_calc.key is 'Key'
/
comment on column crs_h_calc.ldts is 'Load date'
/
create index crs_h_calc_i01 on crs_h_calc (ldts) tablespace spoindx
/
alter table crs_h_calc
  add constraint crs_h_calc_pk primary key (id)
/
alter table crs_h_calc
  add constraint crs_h_calc_uk01 unique (key)
/
alter table crs_h_calc
  add constraint crs_h_calc_ck01
check (key = upper(trim(key)))
/
create sequence crs_h_calc_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_s_calc
(
    id                  number not null,
    h_id                number not null,
    ldts                timestamp not null,
    removed             number(1) not null,
    digest              varchar2(100) not null,
    name                varchar2(4000) not null,
    actuality           timestamp not null,
    published           number(1) default 0 not null,
    calculated          number(1) default 0 not null
)
/
comment on table crs_s_calc is 'Calculation satellite'
/
comment on column crs_s_calc.id is 'Identifier'
/
comment on column crs_s_calc.h_id is 'Reference to hub'
/
comment on column crs_s_calc.ldts is 'Load date'
/
comment on column crs_s_calc.removed is 'Removed flag'
/
comment on column crs_s_calc.digest is 'Row digest'
/
comment on column crs_s_calc.name is 'Name'
/
comment on column crs_s_calc.actuality is 'Actual date'
/
comment on column crs_s_calc.published is 'Published calculation'
/
comment on column crs_s_calc.calculated is 'Сalculated flag'
/
create index crs_s_calc_i01 on crs_s_calc (h_id) tablespace spoindx
/
alter table crs_s_calc
  add constraint crs_s_calc_pk primary key (id)
/
alter table crs_s_calc
  add constraint crs_s_calc_uk01 unique (ldts, h_id)
/
alter table crs_s_calc
  add constraint crs_s_calc_fk01 foreign key (h_id)
references crs_h_calc (id)
/
alter table crs_s_calc
  add constraint crs_s_calc_ck01
check (removed in (0, 1))
/
alter table crs_s_calc
  add constraint crs_s_calc_ck02
check (published in (0, 1))
/
alter table crs_s_calc
    add constraint crs_s_calc_ck03
check (calculated in (0, 1))
/
create sequence crs_s_calc_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_l_calc_user
(
    id           number not null,
    ldts         timestamp not null,
    removed      number(1) default 0 not null,
    calc_id number not null,
    user_id      number not null
)
/
comment on table crs_l_calc_user is 'Link between calculation and user'
/
comment on column crs_l_calc_user.id is 'Identifier'
/
comment on column crs_l_calc_user.ldts is 'Load date'
/
comment on column crs_l_calc_user.removed is 'Removed flag'
/
comment on column crs_l_calc_user.calc_id is 'Reference to calculation'
/
comment on column crs_l_calc_user.user_id is 'Reference to user'
/
create index crs_l_calc_user_i01 on crs_l_calc_user (calc_id) tablespace spoindx
/
create index crs_l_calc_user_i02 on crs_l_calc_user (user_id) tablespace spoindx
/
create index crs_l_calc_user_i03 on crs_l_calc_user (ldts) tablespace spoindx
/
alter table crs_l_calc_user
  add constraint crs_l_calc_user_pk primary key (id)
/
alter table crs_l_calc_user
  add constraint crs_l_calc_user_fk01 foreign key (calc_id)
references crs_h_calc (id)
/
alter table crs_l_calc_user
  add constraint crs_l_calc_user_fk02 foreign key (user_id)
references users (id_user)
/
alter table crs_l_calc_user
  add constraint crs_l_calc_user_ck01
check (removed in (0, 1))
/
create sequence crs_l_calc_user_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_l_calc_model
(
    id           number not null,
    ldts         timestamp not null,
    removed      number(1) default 0 not null,
    calc_id      number not null,
    model_id     number not null
)
/
comment on table crs_l_calc_model is 'Link between calculation and mode'
/
comment on column crs_l_calc_model.id is 'Identifier'
/
comment on column crs_l_calc_model.ldts is 'Load date'
/
comment on column crs_l_calc_model.removed is 'Removed flag'
/
comment on column crs_l_calc_model.calc_id is 'Reference to calculation'
/
comment on column crs_l_calc_model.model_id is 'Reference to calculation model'
/
create index crs_l_calc_model_i01 on crs_l_calc_model (ldts) tablespace spoindx
/
create index crs_l_calc_model_i02 on crs_l_calc_model (calc_id) tablespace spoindx
/
create index crs_l_calc_model_i03 on crs_l_calc_model (model_id) tablespace spoindx
/
alter table crs_l_calc_model
  add constraint crs_l_calc_model_pk primary key (id)
/
alter table crs_l_calc_model
  add constraint crs_l_calc_model_fk01 foreign key (calc_id)
references crs_h_calc (id)
/
alter table crs_l_calc_model
  add constraint crs_l_calc_model_fk02 foreign key (model_id)
references crs_h_calc_model (id)
/
alter table crs_l_calc_model
  add constraint crs_l_calc_model_ck01
check (removed in (0, 1))
/
create sequence crs_l_calc_model_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_h_calc_formula_result
(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null
)
/
comment on table crs_h_calc_formula_result is 'Calculation formula result hub'
/
comment on column crs_h_calc_formula_result.id is 'Identifier'
/
comment on column crs_h_calc_formula_result.key is 'Key'
/
comment on column crs_h_calc_formula_result.ldts is 'Load date'
/
create index crs_h_calc_formula_result_i01 on crs_h_calc_formula_result (ldts) tablespace spoindx
/
alter table crs_h_calc_formula_result
  add constraint crs_h_calc_formula_result_pk primary key (id)
/
alter table crs_h_calc_formula_result
  add constraint crs_h_calc_formula_result_uk01 unique (key)
/
alter table crs_h_calc_formula_result
  add constraint crs_h_calc_formula_result_ck01
check (key = upper(trim(key)))
/
create sequence crs_h_calc_formula_result_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_s_calc_formula_result
(
    id                  number not null,
    h_id                number not null,
    ldts                timestamp not null,
    removed             number(1) default 0 not null,
    number_result       number,
    string_result       varchar2(4000)
)
/
comment on table crs_s_calc_formula_result is 'Calculation formula result satellite'
/
comment on column crs_s_calc_formula_result.id is 'Identifier'
/
comment on column crs_s_calc_formula_result.h_id is 'Reference to hub'
/
comment on column crs_s_calc_formula_result.ldts is 'Load date'
/
comment on column crs_s_calc_formula_result.removed is 'Removed flag'
/
comment on column crs_s_calc_formula_result.number_result is 'Result as number'
/
comment on column crs_s_calc_formula_result.string_result is 'Result as string'
/
create index crs_s_calc_formula_result_i01 on crs_s_calc_formula_result (h_id) tablespace spoindx
/
alter table crs_s_calc_formula_result
  add constraint crs_s_calc_formula_result_pk primary key (id)
/
alter table crs_s_calc_formula_result
  add constraint crs_s_calc_formula_result_uk01 unique (ldts, h_id)
/
alter table crs_s_calc_formula_result
  add constraint crs_s_calc_formula_result_fk01 foreign key (h_id)
references crs_h_calc (id)
/
alter table crs_s_calc_formula_result
  add constraint crs_s_calc_formula_result_ck01
check (removed in (0, 1))
/
create sequence crs_s_calc_formula_result_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

create table crs_l_calc_formula_result
(
    id                  number not null,
    ldts                timestamp not null,
    removed             number(1) default 0 not null,
    calc_id             number not null,
    formula_id          number not null,
    formula_result_id   number not null
)
/
comment on table crs_l_calc_formula_result is 'Link between calculation, formula and formula result'
/
comment on column crs_l_calc_formula_result.id is 'Identifier'
/
comment on column crs_l_calc_formula_result.ldts is 'Load date'
/
comment on column crs_l_calc_formula_result.removed is 'Removed flag'
/
comment on column crs_l_calc_formula_result.calc_id is 'Reference to calculation'
/
comment on column crs_l_calc_formula_result.formula_id is 'Reference to calculation formula'
/
comment on column crs_l_calc_formula_result.formula_result_id is 'Reference to calculation formula result'
/
create index crs_l_calc_formula_result_i01 on crs_l_calc_formula_result (calc_id) tablespace spoindx
/
create index crs_l_calc_formula_result_i02 on crs_l_calc_formula_result (formula_id) tablespace spoindx
/
create index crs_l_calc_formula_result_i03 on crs_l_calc_formula_result (formula_result_id) tablespace spoindx
/
alter table crs_l_calc_formula_result
  add constraint crs_l_calc_formula_result_pk primary key (id)
/
alter table crs_l_calc_formula_result
  add constraint crs_l_calc_formula_result_fk01 foreign key (calc_id)
references crs_h_calc (id)
/
alter table crs_l_calc_formula_result
  add constraint crs_l_calc_formula_result_fk02 foreign key (formula_id)
references crs_h_calc_formula (id)
/
alter table crs_l_calc_formula_result
  add constraint crs_l_calc_formula_result_fk03 foreign key (formula_result_id)
references crs_h_calc_formula_result (id)
/
alter table crs_l_calc_formula_result
  add constraint crs_l_calc_formula_result_ck01
check (removed in (0, 1))
/
create sequence crs_l_calc_formula_result_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

comment on column crs_s_calc_formula_desc.library is 'Functions library flag'
/
comment on column crs_s_calc_formula_desc.eval_lang is 'Evaluation language'
/
alter table crs_s_calc_formula_desc add result_type varchar2(100) not null
/
comment on column crs_s_calc_formula_desc.result_type is 'Result type'
/

--changeset achalov:crs-1.0-VTBCRS-77 logicalFilePath:crs-1.0-VTBCRS-77 endDelimiter:/
create global temporary table crs_calc_formula_l_error(
    formula_id number not null,
    constraint crs_calc_formula_l_error_pk primary key (formula_id)
) on commit preserve rows
/
create type crs_number_table as table of number
/

--changeset svaliev:crs-1.0-VTBCRS-80 logicalFilePath:crs-1.0-VTBCRS-80 endDelimiter:/
comment on table crs_l_calc_model is 'Link between calculation and model'
/

create table crs_l_calc_model_entity
(
    id           number not null,
    ldts         timestamp not null,
    removed      number(1) default 0 not null,
    model_id     number not null,
    entity_id    number not null
)
/
comment on table crs_l_calc_model_entity is 'Link between model and entity'
/
comment on column crs_l_calc_model_entity.id is 'Identifier'
/
comment on column crs_l_calc_model_entity.ldts is 'Load date'
/
comment on column crs_l_calc_model_entity.removed is 'Removed flag'
/
comment on column crs_l_calc_model_entity.model_id is 'Reference to calculation model'
/
comment on column crs_l_calc_model_entity.entity_id is 'Reference to entity metadata'
/
create index crs_l_calc_model_entity_i01 on crs_l_calc_model_entity (ldts) tablespace spoindx
/
create index crs_l_calc_model_entity_i02 on crs_l_calc_model_entity (model_id) tablespace spoindx
/
create index crs_l_calc_model_entity_i03 on crs_l_calc_model_entity (entity_id) tablespace spoindx
/
alter table crs_l_calc_model_entity
    add constraint crs_l_calc_model_entity_pk primary key (id)
/
alter table crs_l_calc_model_entity
    add constraint crs_l_calc_model_entity_fk01 foreign key (model_id)
references crs_h_calc_model (id)
/
alter table crs_l_calc_model_entity
    add constraint crs_l_calc_model_entity_fk02 foreign key (entity_id)
references crs_sys_h_entity (id)
/
alter table crs_l_calc_model_entity
    add constraint crs_l_calc_model_entity_ck01
check (removed in (0, 1))
/
create sequence crs_l_calc_model_entity_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset svaliev:crs-1.0-VTBCRS-81 logicalFilePath:crs-1.0-VTBCRS-81 endDelimiter:/
create table crs_l_calc
(
    id              number not null,
    ldts            timestamp not null,
    removed         number(1) default 0 not null,
    origin_calc_id  number not null,
    copied_calc_id  number not null
)
/
comment on table crs_l_calc is 'Link between origin calculation and copied calculation'
/
comment on column crs_l_calc.id is 'Identifier'
/
comment on column crs_l_calc.ldts is 'Load date'
/
comment on column crs_l_calc.removed is 'Removed flag'
/
comment on column crs_l_calc.origin_calc_id is 'Reference to origin calculation'
/
comment on column crs_l_calc.copied_calc_id is 'Reference to copied calculation'
/
create index crs_l_calc_i01 on crs_l_calc (ldts) tablespace spoindx
/
create index crs_l_calc_i02 on crs_l_calc (origin_calc_id) tablespace spoindx
/
create index crs_l_calc_i03 on crs_l_calc (copied_calc_id) tablespace spoindx
/
alter table crs_l_calc
    add constraint crs_l_calc_pk primary key (id)
/
alter table crs_l_calc
    add constraint crs_l_calc_fk01 foreign key (origin_calc_id)
references crs_h_calc (id)
/
alter table crs_l_calc
    add constraint crs_l_calc_fk02 foreign key (copied_calc_id)
references crs_h_calc (id)
/
alter table crs_l_calc
    add constraint crs_l_calc_ck01
check (removed in (0, 1))
/
create sequence crs_l_calc_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset svaliev:crs-1.0-VTBCRS-77-temporary-table-modification logicalFilePath:crs-1.0-VTBCRS-77-temporary-table-modification endDelimiter:/
drop table crs_calc_formula_l_error
/
create global temporary table crs_calc_formula_error(
    formula_id number not null,
    constraint crs_calc_formula_error_pk primary key (formula_id)
) on commit preserve rows
/
comment on table crs_calc_formula_error is 'Calculation formulas that contain cyclic dependencies'
/
comment on column crs_calc_formula_error.formula_id is 'Reference to calculation formula'
/

--changeset svaliev:crs-1.0-add-exception-to-formula-result logicalFilePath:crs-1.0-add-exception-to-formula-result endDelimiter:/
alter table crs_s_calc_formula_result add exception clob
/
comment on column crs_s_calc_formula_result.exception is 'Exception log'
/

--changeset achalov:crs-1.0-VTBCRS-86 logicalFilePath:crs-1.0-VTBCRS-86 endDelimiter:/
create table crs_s_l_calc_formula(
    id                number        not null,
    formula_id        number        not null,
    formula_parent_id number        not null,
    ldts              timestamp     not null,
    attribute_name    varchar2(100) not null,
    constraint crs_s_l_calc_formula_pk   primary key(id),
    constraint crs_s_l_calc_formula_fk01 foreign key(formula_id)        references crs_h_calc_formula(id),
    constraint crs_s_l_calc_formula_fk02 foreign key(formula_parent_id) references crs_h_calc_formula(id)
)
/
comment on table crs_s_l_calc_formula is 'Calculation formula satellite for crs_l_calc_formula link table'
/
comment on column crs_s_l_calc_formula.id is 'Identifier'
/
comment on column crs_s_l_calc_formula.formula_id is 'Reference to calculation formula'
/
comment on column crs_s_l_calc_formula.formula_parent_id is 'Reference to parent calculation formula'
/
comment on column crs_s_l_calc_formula.ldts is 'Load date'
/
comment on column crs_s_l_calc_formula.attribute_name is 'Context attribute name to pass from formula to parent one'
/
create index crs_s_l_calc_formula_i01 on crs_s_l_calc_formula(formula_id) tablespace spoindx
/
create index crs_s_l_calc_formula_i02 on crs_s_l_calc_formula(formula_parent_id) tablespace spoindx
/
create index crs_s_l_calc_formula_i03 on crs_s_l_calc_formula(ldts) tablespace spoindx
/
create sequence crs_s_l_calc_formula_seq nocycle order nocache
/

--changeset pmasalov:crs-1.0-VTBCRS-57 logicalFilePath:crs-1.0-VTBCRS-57 endDelimiter:/
alter table crs_s_file_storage add digest varchar2(100) default 'none' not null
/
alter table crs_s_file_storage modify digest default null
/
alter table crs_s_file_storage_data add digest varchar2(100) default 'none' not null
/
alter table crs_s_file_storage_data modify digest default null
/
alter table crs_s_localization add digest varchar2(100) default 'none' not null
/
alter table crs_s_localization modify digest default null
/

--changeset pmasalov:crs-1.0-VTBCRS-85 logicalFilePath:crs-1.0-VTBCRS-85 endDelimiter:/
create or replace type crs_pair_number force as object (n1 number, n2 number)
/
create or replace type crs_pair_number_a as table of crs_pair_number
/

--changeset pmasalov:crs-1.0-VTBCRS-107 logicalFilePath:crs-1.0-VTBCRS-107 endDelimiter:/
create table crs_h_contractor_type(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_contractor_type_pk primary key (id) using index tablespace spoindx,
    constraint crs_h_contractor_type_uk1 unique (key) using index tablespace spoindx
)
/
comment on table crs_h_contractor_type is 'Contractor type reference hub'
/
comment on column crs_h_contractor_type.id is 'Identifier'
/
comment on column crs_h_contractor_type.key is 'Key'
/
comment on column crs_h_contractor_type.ldts is 'Load date'
/

--changeset pmasalov:crs-1.0-VTBCRS-107-1 logicalFilePath:crs-1.0-VTBCRS-107-1 endDelimiter:/
create table crs_s_contractor_type (
    id      number not null,
    h_id    number not null,
    ldts    timestamp not null,
    removed number(1) default 0 not null,
    digest  varchar2(100) not null,
    constraint crs_s_contractor_type_pk primary key (id) using index tablespace spoindx,
    constraint crs_s_contractor_type_uk1 unique (ldts, h_id) using index tablespace spoindx,
    constraint crs_s_contractor_type_fk01 foreign key (h_id) references crs_h_contractor_type(id)
)
/
comment on table crs_s_contractor_type is 'Contractor type reference satellite'
/
comment on column crs_s_contractor_type.id is 'Identifier'
/
comment on column crs_s_contractor_type.h_id is 'Reference to hub'
/
comment on column crs_s_contractor_type.ldts is 'Load date'
/
comment on column crs_s_contractor_type.removed is 'Removed flag'
/
comment on column crs_s_contractor_type.digest is 'Row digest'
/

--changeset pmasalov:crs-1.0-VTBCRS-107-2 logicalFilePath:crs-1.0-VTBCRS-107-2 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_H_CONTRACTOR_TYPE_I01')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_h_contractor_type_i01 on crs_h_contractor_type (ldts) tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-107-3 logicalFilePath:crs-1.0-VTBCRS-107-3 endDelimiter:/
create table crs_h_contractor(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_contractor_pk primary key (id) using index tablespace spoindx,
    constraint crs_h_contractor_uk1 unique (key) using index tablespace spoindx
)
/
comment on table crs_h_contractor is 'Contractor hub'
/
comment on column crs_h_contractor.id is 'Identifier'
/
comment on column crs_h_contractor.key is 'Key'
/
comment on column crs_h_contractor.ldts is 'Load date'
/

--changeset pmasalov:crs-1.0-VTBCRS-107-4 logicalFilePath:crs-1.0-VTBCRS-107-4 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_H_CONTRACTOR_I01')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_h_contractor_i01 on crs_h_contractor (ldts) tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-107-5 logicalFilePath:crs-1.0-VTBCRS-107-5 endDelimiter:/
create table crs_s_contractor(
    id                 number not null,
    h_id               number not null,
    ldts               timestamp not null,
    removed            number(1) default 0 not null,
    digest             varchar2(100) not null,
    short_name         varchar2(256),
    full_name          varchar2(2000),
    workphone          varchar2(32),
    fax                varchar2(32),
    inn                varchar2(12),
    kpp                varchar2(9),
    client_category    varchar2(80),
    division           varchar2(64),
    registration_date  date,
    type               varchar2(64),
    department         varchar2(64),
    reg_direction      varchar2(64),
    branch             varchar2(255),
    description        varchar2(255),
    afc                number(1),
    constraint crs_s_contractor_pk primary key (id) using index tablespace spoindx,
    constraint crs_s_contractor_uk1 unique (ldts, h_id) using index tablespace spoindx,
    constraint crs_s_contractor_fk01 foreign key (h_id) references crs_h_contractor(id)
)
/
--changeset pmasalov:crs-1.0-VTBCRS-107-5-1 logicalFilePath:crs-1.0-VTBCRS-107-5-1 endDelimiter:/
comment on table crs_s_contractor is 'Contractor satellite'
/
comment on column crs_s_contractor.id is 'Identifier'
/
comment on column crs_s_contractor.h_id is 'Reference to hub'
/
comment on column crs_s_contractor.ldts is 'Load date'
/
comment on column crs_s_contractor.removed is 'Removed flag'
/
comment on column crs_s_contractor.digest is 'Row digest'
/
comment on column crs_s_contractor.short_name is 'Short name'
/
comment on column crs_s_contractor.full_name is 'Long name'
/
comment on column crs_s_contractor.workphone is 'Work phone'
/
comment on column crs_s_contractor.fax is 'Fax'
/
comment on column crs_s_contractor.inn is 'INN'
/
comment on column crs_s_contractor.kpp is 'KPP'
/
comment on column crs_s_contractor.client_category is 'Client category'
/
comment on column crs_s_contractor.division is 'Division'
/
comment on column crs_s_contractor.registration_date is 'Registration date'
/
comment on column crs_s_contractor.type is 'Contractor type'
/
comment on column crs_s_contractor.department is 'Department'
/
comment on column crs_s_contractor.reg_direction is 'Reqistration direction'
/
comment on column crs_s_contractor.branch is 'Branch'
/
comment on column crs_s_contractor.description is 'Description'
/
comment on column crs_s_contractor.afc is 'Affiliated financial company'
/

--changeset pmasalov:crs-1.0-VTBCRS-107-6 logicalFilePath:crs-1.0-VTBCRS-107-6 endDelimiter:/
create table crs_l_contractor_type (
    id                 number not null,
    ldts               timestamp not null,
    removed            number(1) not null,
    contractor_id      number not null,
    contractor_type_id number not null,
    constraint crs_l_contractor_type_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_contractor_type_fk01 foreign key(contractor_id) references crs_h_contractor(id),
    constraint crs_l_contractor_type_fk02 foreign key(contractor_type_id) references crs_h_contractor_type(id)
)
/
comment on table crs_l_contractor_type is 'Contractor to contractor type link'
/
comment on column crs_l_contractor_type.id is 'Identifier'
/
comment on column crs_l_contractor_type.contractor_id is 'Reference to contractor'
/
comment on column crs_l_contractor_type.contractor_type_id is 'Reference to contractor type'
/
comment on column crs_l_contractor_type.ldts is 'Load date'
/

--changeset pmasalov:crs-1.0-VTBCRS-107-7 logicalFilePath:crs-1.0-VTBCRS-107-7 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_L_CONTRACTOR_TYPE_I01', 'CRS_L_CONTRACTOR_TYPE_I02', 'CRS_L_CONTRACTOR_TYPE_I03')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_l_contractor_type_i01 on crs_l_contractor_type(contractor_id) compress 1 tablespace spoindx
/
create index crs_l_contractor_type_i02 on crs_l_contractor_type(contractor_type_id) compress 1 tablespace spoindx
/
create index crs_l_contractor_type_i03 on crs_l_contractor_type(ldts) tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-107-8 logicalFilePath:crs-1.0-VTBCRS-107-8 endDelimiter:/
create table crs_l_contractor_content (
    id              number not null,
    ldts            timestamp not null,
    removed         number(1) not null,
    contractor_id   number not null,
    contractor_p_id number not null,
    constraint crs_l_contractor_content_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_contractor_content_fk01 foreign key(contractor_id) references crs_h_contractor(id),
    constraint crs_l_contractor_content_fk02 foreign key(contractor_p_id) references crs_h_contractor(id)
)
/
comment on table crs_l_contractor_type is 'Contractor to contractor link'
/
comment on column crs_l_contractor_type.id is 'Identifier'
/
comment on column crs_l_contractor_type.contractor_id is 'Reference to contractor'
/
comment on column crs_l_contractor_type.contractor_type_id is 'Reference to parent contractor'
/
comment on column crs_l_contractor_type.ldts is 'Load date'
/

--changeset pmasalov:crs-1.0-VTBCRS-107-9 logicalFilePath:crs-1.0-VTBCRS-107-9 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_L_CONTRACTOR_CONTENT_I01', 'CRS_L_CONTRACTOR_CONTENT_I02', 'CRS_L_CONTRACTOR_CONTENT_I03')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_l_contractor_content_i01 on crs_l_contractor_content(contractor_id) compress 1 tablespace spoindx
/
create index crs_l_contractor_content_i02 on crs_l_contractor_content(contractor_p_id) compress 1 tablespace spoindx
/
create index crs_l_contractor_content_i03 on crs_l_contractor_content(ldts) tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-107-10 logicalFilePath:crs-1.0-VTBCRS-107-10 endDelimiter:/
begin
    for r in (select * from user_sequences where sequence_name in ('CRS_L_CONTRACTOR_CONTENT_SEQ',
                                                                   'CRS_L_CONTRACTOR_TYPE_SEQ',
                                                                   'CRS_S_CONTRACTOR_SEQ',
                                                                   'CRS_H_CONTRACTOR_SEQ',
                                                                   'CRS_S_CONTRACTOR_TYPE_SEQ',
                                                                   'CRS_H_CONTRACTOR_TYPE_SEQ')) loop
        execute immediate 'drop sequence '||r.sequence_name;
    end loop;
end;
/
create sequence crs_l_contractor_content_seq
/
create sequence crs_l_contractor_type_seq
/
create sequence crs_s_contractor_seq
/
create sequence crs_h_contractor_seq
/
create sequence crs_s_contractor_type_seq nocycle nocache
/
create sequence crs_h_contractor_type_seq nocycle nocache
/

-- changeset pmasalov:crs-1.0-VTBCRS-107-10-2 logicalFilePath:crs-1.0-VTBCRS-107-10-2 endDelimiter:/
alter table crs_sys_s_attribute add native_column varchar2(30)
/
comment on column crs_sys_s_attribute.native_column is 'Database column name stores intable attribute value'
/

--changeset pmasalov:crs-1.0-VTBCRS-107-11 logicalFilePath:crs-1.0-VTBCRS-107-11 endDelimiter:/
insert into crs_sys_h_entity_type (id, key, ldts)
values (crs_sys_h_entity_type_seq.nextval, 'PREDEFINED_DICTIONARY', systimestamp)
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12 logicalFilePath:crs-1.0-VTBCRS-107-12 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    -- entity CONTRACTOR_TYPE
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CONTRACTOR_TYPE', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Тип контрагента',
            'Contractor type',
            'null',
            null,
            1,
            0
    );

    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval,
            v_entity_type_id,
            v_h_entity_id,
            v_ldts,
            0);

    -- multilang attribute NAME
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CONTRACTOR_TYPE#NAME', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            null,
            null,
            'Наименование типа',
            'Type name',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-1 logicalFilePath:crs-1.0-VTBCRS-107-12-1 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    -- entity CONTRACTOR
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CONTRACTOR', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Контрагент',
            'Contractor',
            'CRS_L_CONTRACTOR_CONTENT',
            null,
            1,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);

    -- link attribute
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#CONTRACTOR_TYPE_ID',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CONTRACTOR_TYPE',
            'CONTRACTOR_TYPE#NAME',
            'Тип контрагента',
            'Contractor type',
            1,
            'CONTRACTOR_TYPE',
            0,
            'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-2 logicalFilePath:crs-1.0-VTBCRS-107-12-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute SHORT_NAME
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#SHORT_NAME',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Краткое наименование контрагента',
            'Contractor short name',
            1,
            null,
            0,
            'STRING',
            'SHORT_NAME');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-3 logicalFilePath:crs-1.0-VTBCRS-107-12-3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute FULL_NAME
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#FULL_NAME',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Полное наименование контрагента',
            'Contractor full name',
            1,
            null,
            0,
            'STRING',
            'FULL_NAME');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-4 logicalFilePath:crs-1.0-VTBCRS-107-12-4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute WORKPHONE
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#WORKPHONE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Рабочий телефон',
            'Work phone',
            1,
            null,
            0,
            'STRING',
            'WORKPHONE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-5 logicalFilePath:crs-1.0-VTBCRS-107-12-5 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute FAX
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#FAX',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Факс',
            'Fax',
            1,
            null,
            0,
            'STRING',
            'FAX');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-6 logicalFilePath:crs-1.0-VTBCRS-107-12-6 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute INN
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#INN',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'ИНН',
            'INN',
            1,
            null,
            0,
            'STRING',
            'INN');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-7 logicalFilePath:crs-1.0-VTBCRS-107-12-7 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute KPP
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#KPP',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'КПП',
            'KPP',
            1,
            null,
            0,
            'STRING',
            'KPP');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-8 logicalFilePath:crs-1.0-VTBCRS-107-12-8 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute CLIENT_CATEGORY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#CLIENT_CATEGORY',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Категория клиента',
            'Client category',
            1,
            null,
            0,
            'STRING',
            'CLIENT_CATEGORY');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-9 logicalFilePath:crs-1.0-VTBCRS-107-12-9 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute DIVISION
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#DIVISION',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Подразделение',
            'Division',
            1,
            null,
            0,
            'STRING',
            'DIVISION');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-10 logicalFilePath:crs-1.0-VTBCRS-107-12-10 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute REGISTRATION_DATE
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#REGISTRATION_DATE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Дата регистрации',
            'Date of registration',
            1,
            null,
            0,
            'DATE',
            'REGISTRATION_DATE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-11 logicalFilePath:crs-1.0-VTBCRS-107-12-11 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute TYPE
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#TYPE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Тип клиента',
            'Client type',
            1,
            null,
            0,
            'STRING',
            'TYPE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-12 logicalFilePath:crs-1.0-VTBCRS-107-12-12 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute DEPARTMENT
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#DEPARTMENT',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Департамент',
            'Department',
            1,
            null,
            0,
            'STRING',
            'DEPARTMENT');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-13 logicalFilePath:crs-1.0-VTBCRS-107-12-13 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute REG_DIRECTION
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#REG_DIRECTION',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Регистрация',
            'Registration direction',
            1,
            null,
            0,
            'STRING',
            'REG_DIRECTION');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-14 logicalFilePath:crs-1.0-VTBCRS-107-12-14 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute BRANCH
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#BRANCH',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Филиал',
            'Branch',
            1,
            null,
            0,
            'STRING',
            'BRANCH');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-15 logicalFilePath:crs-1.0-VTBCRS-107-12-15 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute DESCRIPTION
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#DESCRIPTION',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Описание',
            'Description',
            1,
            null,
            0,
            'STRING',
            'DESCRIPTION');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-107-12-16 logicalFilePath:crs-1.0-VTBCRS-107-12-16 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    -- intable attribute AFC
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CONTRACTOR#AFC',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Дочерняя финансовая компания',
            'Affiliated financial company',
            1,
            null,
            0,
            'BOOLEAN',
            'AFC');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-116 logicalFilePath:crs-1.0-VTBCRS-116 endDelimiter:/
create table crs_l_contractor_type_name (
    id                 number not null,
    ldts               timestamp not null,
    removed            number(1) not null,
    contractor_type_id number not null,
    localization_id    number not null,
    constraint crs_l_contractor_type_name_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_contractor_type_name_fk1 foreign key (contractor_type_id) references crs_h_contractor_type(id),
    constraint crs_l_contractor_type_name_fk2 foreign key (localization_id) references crs_h_localization(id)
)
/
comment on table crs_l_contractor_type_name is 'Contractor type NAME multilanguage link'
/
comment on column crs_l_contractor_type_name.id is 'Identifier'
/
comment on column crs_l_contractor_type_name.contractor_type_id is 'Reference to contractor type'
/
comment on column crs_l_contractor_type_name.localization_id is 'Reference to multilanguage'
/
comment on column crs_l_contractor_type_name.ldts is 'Load date'
/

--changeset pmasalov:crs-1.0-VTBCRS-116-1 logicalFilePath:crs-1.0-VTBCRS-116-1 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_L_CONTRACTOR_TYPE_NAME_I01', 'CRS_L_CONTRACTOR_TYPE_NAME_I02', 'CRS_L_CONTRACTOR_TYPE_NAME_I03')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_l_contractor_type_name_i01 on crs_l_contractor_type_name(contractor_type_id) compress 1 tablespace spoindx
/
create index crs_l_contractor_type_name_i02 on crs_l_contractor_type_name(localization_id) compress 1 tablespace spoindx
/
create index crs_l_contractor_type_name_i03 on crs_l_contractor_type_name(ldts) tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-116-2 logicalFilePath:crs-1.0-VTBCRS-116-2 endDelimiter:/
update crs_sys_s_attribute set link_table = 'CRS_L_CONTRACTOR_TYPE_NAME'
 where h_id = (select id from crs_sys_h_attribute where key = 'CONTRACTOR_TYPE#NAME')
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-116-3 logicalFilePath:crs-1.0-VTBCRS-116-3 endDelimiter:/
begin
    for r in (select * from user_sequences where sequence_name in ('CRS_L_CONTRACTOR_TYPE_NAME_SEQ')) loop
        execute immediate 'drop sequence '||r.sequence_name;
    end loop;
end;
/
create sequence crs_l_contractor_type_name_seq nocycle nocache
/

--changeset pmasalov:crs-1.0-VTBCRS-116-4-1 logicalFilePath:crs-1.0-VTBCRS-116-4-1 endDelimiter:/
declare
    v_h_id number;
    v_ml_h_id number;
    v_ldts timestamp := systimestamp;

begin
    insert into crs_h_contractor_type (id, key, ldts)
    values (crs_h_contractor_type_seq.nextval, 'PROJECT', v_ldts)
    returning id into v_h_id;
    insert into crs_s_contractor_type (id, h_id, ldts, removed, digest)
    values (crs_s_contractor_type_seq.nextval, v_h_id, v_ldts, 0, 'n');

    insert into crs_h_localization (id, key, ldts)
    values (crs_h_localization_seq.nextval, crs_h_localization_seq.currval, v_ldts)
    returning id into v_ml_h_id;
    insert into crs_s_localization (id, h_id, ldts, string_ru, string_en, removed, digest)
    values (crs_s_localization_seq.nextval, v_ml_h_id,v_ldts, 'Проект', 'Project', 0, 'n');

    insert into crs_l_contractor_type_name (id, ldts, removed, contractor_type_id, localization_id)
    values (crs_l_contractor_type_name_seq.nextval, v_ldts, 0, v_h_id, v_ml_h_id);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-116-4-2 logicalFilePath:crs-1.0-VTBCRS-116-4-2 endDelimiter:/
declare
    v_h_id number;
    v_ml_h_id number;
    v_ldts timestamp := systimestamp;

begin
    insert into crs_h_contractor_type (id, key, ldts)
    values (crs_h_contractor_type_seq.nextval, 'GROUP', v_ldts)
    returning id into v_h_id;
    insert into crs_s_contractor_type (id, h_id, ldts, removed, digest)
    values (crs_s_contractor_type_seq.nextval, v_h_id, v_ldts, 0, 'n');

    insert into crs_h_localization (id, key, ldts)
    values (crs_h_localization_seq.nextval, crs_h_localization_seq.currval, v_ldts)
    returning id into v_ml_h_id;
    insert into crs_s_localization (id, h_id, ldts, string_ru, string_en, removed, digest)
    values (crs_s_localization_seq.nextval, v_ml_h_id, v_ldts, 'Группа', 'Group', 0, 'n');

    insert into crs_l_contractor_type_name (id, ldts, removed, contractor_type_id, localization_id)
    values (crs_l_contractor_type_name_seq.nextval, v_ldts, 0, v_h_id, v_ml_h_id);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-116-4-3 logicalFilePath:crs-1.0-VTBCRS-116-4-3 endDelimiter:/
declare
    v_h_id number;
    v_ml_h_id number;
    v_ldts timestamp := systimestamp;

begin
    insert into crs_h_contractor_type (id, key, ldts)
    values (crs_h_contractor_type_seq.nextval, 'ORGANIZATION', v_ldts)
    returning id into v_h_id;
    insert into crs_s_contractor_type (id, h_id, ldts, removed, digest)
    values (crs_s_contractor_type_seq.nextval, v_h_id, v_ldts, 0, 'n');

    insert into crs_h_localization (id, key, ldts)
    values (crs_h_localization_seq.nextval, crs_h_localization_seq.currval, v_ldts)
    returning id into v_ml_h_id;
    insert into crs_s_localization (id, h_id, ldts, string_ru, string_en, removed, digest)
    values (crs_s_localization_seq.nextval, v_ml_h_id, v_ldts, 'Клиентская запись', 'Organization', 0, 'n');

    insert into crs_l_contractor_type_name (id, ldts, removed, contractor_type_id, localization_id)
    values (crs_l_contractor_type_name_seq.nextval, v_ldts, 0, v_h_id, v_ml_h_id);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-116-4-4 logicalFilePath:crs-1.0-VTBCRS-116-4-4 endDelimiter:/
declare
    v_h_id number;
    v_ml_h_id number;
    v_ldts timestamp := systimestamp;

begin
    insert into crs_h_contractor_type (id, key, ldts)
    values (crs_h_contractor_type_seq.nextval, 'UNITED_CLIENT', v_ldts)
    returning id into v_h_id;
    insert into crs_s_contractor_type (id, h_id, ldts, removed, digest)
    values (crs_s_contractor_type_seq.nextval, v_h_id, v_ldts, 0, 'n');

    insert into crs_h_localization (id, key, ldts)
    values (crs_h_localization_seq.nextval, crs_h_localization_seq.currval, v_ldts)
    returning id into v_ml_h_id;
    insert into crs_s_localization (id, h_id, ldts, string_ru, string_en, removed, digest)
    values (crs_s_localization_seq.nextval, v_ml_h_id, v_ldts, 'Единый клиент', 'United client', 0, 'n');

    insert into crs_l_contractor_type_name (id, ldts, removed, contractor_type_id, localization_id)
    values (crs_l_contractor_type_name_seq.nextval, v_ldts, 0, v_h_id, v_ml_h_id);
end;
/

--changeset svaliev:crs-1.0-VTBCRS-119 logicalFilePath:crs-1.0-VTBCRS-119 endDelimiter:/
update crs_sys_s_attribute s
   set s.native_column = (select h.key
                            from crs_sys_h_attribute h
                           where h.id = s.h_id)
 where s.native_column is null
   and s.type not in ('REFERENCE', 'FILE')
   and s.multilang = 0
/

alter table crs_sys_h_attribute modify key varchar2(100)
/

--changeset pmasalov:crs-1.0-VTBCRS-102 logicalFilePath:crs-1.0-VTBCRS-102 endDelimiter:/
alter table crs_s_calc_formula_desc add master number(1) default 0 not null
/
comment on column crs_s_calc_formula_desc.master is 'Master script flag'
/

--changeset pmasalov:crs-1.0-VTBCRS-102-2 logicalFilePath:crs-1.0-VTBCRS-102-2 endDelimiter:/
alter table crs_s_calc_formula_desc add constraint crs_s_calc_formula_desc_ck03 check (master in (0,1))
/
alter table crs_s_calc_formula_desc add constraint crs_s_calc_formula_desc_ck04 check (not (master = 1 and library = 1))
/

--changeset achalov:crs-1.0-VTBCRS-70 logicalFilePath:crs-1.0-VTBCRS-70 endDelimiter:/
alter table crs_s_l_calc_formula add removed number(1) default 0 not null
/
comment on column crs_s_l_calc_formula.removed is 'Removed flag'
/
alter table crs_s_l_calc_formula
  add constraint crs_s_l_calc_formula_ck01
check (removed in (0, 1))
/

--changeset svaliev:crs-1.0-rename-calc-tables logicalFilePath:crs-1.0-rename-calc-tables endDelimiter:/
rename crs_calc_formula_error to crs_calc_formula_error_t
/
alter index crs_calc_formula_error_pk rename to crs_calc_formula_error_t_pk
/
alter table crs_calc_formula_error_t rename constraint crs_calc_formula_error_pk to crs_calc_formula_error_t_pk
/

rename crs_s_l_calc_formula to crs_l_s_calc_formula
/
alter index crs_s_l_calc_formula_pk rename to crs_l_s_calc_formula_pk
/
alter index crs_s_l_calc_formula_i01 rename to crs_l_s_calc_formula_i01
/
alter index crs_s_l_calc_formula_i02 rename to crs_l_s_calc_formula_i02
/
alter index crs_s_l_calc_formula_i03 rename to crs_l_s_calc_formula_i03
/
alter table crs_l_s_calc_formula rename constraint crs_s_l_calc_formula_ck01 to crs_l_s_calc_formula_ck01
/
alter table crs_l_s_calc_formula rename constraint crs_s_l_calc_formula_pk to crs_l_s_calc_formula_pk
/
alter table crs_l_s_calc_formula rename constraint crs_s_l_calc_formula_fk01 to crs_l_s_calc_formula_fk01
/
alter table crs_l_s_calc_formula rename constraint crs_s_l_calc_formula_fk02 to crs_l_s_calc_formula_fk02
/
rename crs_s_l_calc_formula_seq to crs_l_s_calc_formula_seq
/

--changeset achalov:crs-1.0-VTBCRS-105 logicalFilePath:crs-1.0-VTBCRS-105 endDelimiter:/
create global temporary table crs_calc_formula_filter_t(
    formula_id number not null,
    constraint crs_calc_formula_filter_t_pk primary key (formula_id)
)
/
create global temporary table crs_calc_formula_tree_t(
    id        number         not null,
    hub_id    number         not null,
    c_id      number,
    c_hub_id  number,
    name_ru   varchar2(4000) not null,
    name_en   varchar2(4000) not null,
    library   number(1)      not null,
    master    number(1)      not null
) on commit preserve rows
/
comment on table crs_calc_formula_filter_t is 'Filtered formula hub identifiers'
/
comment on column crs_calc_formula_filter_t.formula_id is 'Formula hub identifier'
/
comment on table crs_calc_formula_tree_t is 'Flattened filtered formula trees'
/
comment on column crs_calc_formula_tree_t.id is 'Formula identifier'
/
comment on column crs_calc_formula_tree_t.hub_id is 'Formula hub identifier'
/
comment on column crs_calc_formula_tree_t.c_id is 'Child formula identifier'
/
comment on column crs_calc_formula_tree_t.c_hub_id is 'Child formula hub identifier'
/
comment on column crs_calc_formula_tree_t.name_ru is 'Name (ru)'
/
comment on column crs_calc_formula_tree_t.name_en is 'Name (en)'
/
comment on column crs_calc_formula_tree_t.library is 'Library flag'
/
comment on column crs_calc_formula_tree_t.master is 'Master flag'
/

--changeset svaliev:crs-1.0-entity-types logicalFilePath:crs-1.0-entity-types endDelimiter:/
insert into crs_sys_h_entity_type (id, key, ldts)
values (crs_sys_h_entity_type_seq.nextval, 'INPUT_FORM', systimestamp)
/
insert into crs_sys_h_entity_type (id, key, ldts)
values (crs_sys_h_entity_type_seq.nextval, 'SYSTEM_OBJECT', systimestamp)
/
commit
/

alter table crs_sys_s_entity modify link_table null
/
update crs_sys_s_entity e
   set e.link_table = null
 where e.link_table = 'null'
/

--changeset pmasalov:crs-1.0-VTBCRS-165 logicalFilePath:crs-1.0-VTBCRS-165 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'SYSTEM_OBJECT';

    -- entity CALC_FORMULA
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CALC_FORMULA', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Показатель формулы',
            'Calc formula',
            'crs_l_calc_formula',
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-165-2 logicalFilePath:crs-1.0-VTBCRS-165-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute NAME_RU
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#NAME_RU',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Название',
            'Name',
            1,
            null,
            0,
            'STRING',
            'NAME_RU');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-165-3 logicalFilePath:crs-1.0-VTBCRS-165-3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute NAME_EN
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#NAME_EN',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Название',
            'Name',
            1,
            null,
            0,
            'STRING',
            'NAME_EN');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-165-4 logicalFilePath:crs-1.0-VTBCRS-165-4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute COMMENT_RU
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#COMMENT_RU',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Комментарий',
            'Comment',
            1,
            null,
            0,
            'STRING',
            'COMMENT_RU');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-165-5 logicalFilePath:crs-1.0-VTBCRS-165-5 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute COMMENT_RU
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#COMMENT_EN',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Комментарий',
            'Comment',
            1,
            null,
            0,
            'STRING',
            'COMMENT_EN');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-165-6 logicalFilePath:crs-1.0-VTBCRS-165-6 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute LIBRARY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#LIBRARY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Библиотека функций',
            'Functions library flag',
            1,
            null,
            0,
            'BOOLEAN',
            'LIBRARY');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-165-7 logicalFilePath:crs-1.0-VTBCRS-165-7 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute EVAL_LANG
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#EVAL_LANG',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Язык программирования',
            'Evaluation language',
            1,
            null,
            0,
            'STRING',
            'EVAL_LANG');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-165-9 logicalFilePath:crs-1.0-VTBCRS-165-9 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute RESULT_TYPE
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#RESULT_TYPE',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Тип результирующего значения',
            'Result type',
            1,
            null,
            0,
            'STRING',
            'RESULT_TYPE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-165-10 logicalFilePath:crs-1.0-VTBCRS-165-10 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute MASTER
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#MASTER',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Признак главной функции',
            'Master script flag',
            1,
            null,
            0,
            'BOOLEAN',
            'MASTER');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset mkuzmin:crs-1.0-VTBCRS-173 logicalFilePath:crs-1.0-VTBCRS-173 endDelimiter:/
alter table crs_calc_formula_tree_t add key varchar2(100) not null
/
comment on column crs_calc_formula_tree_t.key is 'Key'
/

--changeset svaliev:crs-1.0-VTBCRS-155 logicalFilePath:crs-1.0-VTBCRS-155 endDelimiter:/
alter index crs_h_file_storage_i01 rename to crs_sys_h_storage_i01
/
alter index crs_h_file_storage_pk rename to crs_sys_h_storage_pk
/
alter index crs_h_file_storage_uk01 rename to crs_sys_h_storage_uk01
/
alter table crs_h_file_storage rename constraint crs_h_file_storage_pk to crs_sys_h_storage_pk
/
alter table crs_h_file_storage rename constraint crs_h_file_storage_uk01 to crs_sys_h_storage_uk01
/
alter table crs_h_file_storage rename constraint crs_h_file_storage_ck01 to crs_sys_h_storage_ck01
/
rename crs_h_file_storage to crs_sys_h_storage
/
rename crs_h_file_storage_seq to crs_sys_h_storage_seq
/

alter index crs_s_file_storage_i01 rename to crs_sys_s_storage_desc_i01
/
alter index crs_s_file_storage_pk rename to crs_sys_s_storage_desc_pk
/
alter index crs_s_file_storage_uk01 rename to crs_sys_s_storage_desc_uk01
/
alter table crs_s_file_storage rename constraint crs_s_file_storage_pk to crs_sys_s_storage_desc_pk
/
alter table crs_s_file_storage rename constraint crs_s_file_storage_uk01 to crs_sys_s_storage_desc_uk01
/
alter table crs_s_file_storage rename constraint crs_s_file_storage_fk01 to crs_sys_s_storage_desc_fk01
/
alter table crs_s_file_storage rename constraint crs_s_file_storage_ck01 to crs_sys_s_storage_desc_ck01
/
rename crs_s_file_storage to crs_sys_s_storage_desc
/
rename crs_s_file_storage_seq to crs_sys_s_storage_desc_seq
/

alter index crs_s_file_storage_data_i01 rename to crs_sys_s_storage_i01
/
alter index crs_s_file_storage_data_pk rename to crs_sys_s_storage_pk
/
alter index crs_s_file_storage_data_uk01 rename to crs_sys_s_storage_uk01
/
alter table crs_s_file_storage_data rename constraint crs_s_file_storage_data_pk to crs_sys_s_storage_pk
/
alter table crs_s_file_storage_data rename constraint crs_s_file_storage_data_uk01 to crs_sys_s_storage_uk01
/
alter table crs_s_file_storage_data rename constraint crs_s_file_storage_data_fk01 to crs_sys_s_storage_fk01
/
alter table crs_s_file_storage_data rename constraint crs_s_file_storage_data_ck01 to crs_sys_s_storage_ck01
/
rename crs_s_file_storage_data to crs_sys_s_storage
/
rename crs_s_file_storage_data_seq to crs_sys_s_storage_seq
/

alter index crs_h_localization_i01 rename to crs_sys_h_localization_i01
/
alter index crs_h_localization_pk rename to crs_sys_h_localization_pk
/
alter index crs_h_localization_uk01 rename to crs_sys_h_localization_uk01
/
alter table crs_h_localization rename constraint crs_h_localization_pk to crs_sys_h_localization_pk
/
alter table crs_h_localization rename constraint crs_h_localization_uk01 to crs_sys_h_localization_uk01
/
alter table crs_h_localization rename constraint crs_h_localization_ck01 to crs_sys_h_localization_ck01
/
rename crs_h_localization to crs_sys_h_localization
/
rename crs_h_localization_seq to crs_sys_h_localization_seq
/

alter index crs_s_localization_i01 rename to crs_sys_s_localization_i01
/
alter index crs_s_localization_pk rename to crs_sys_s_localization_pk
/
alter index crs_s_localization_uk01 rename to crs_sys_s_localization_uk01
/
alter table crs_s_localization rename constraint crs_s_localization_pk to crs_sys_s_localization_pk
/
alter table crs_s_localization rename constraint crs_s_localization_uk01 to crs_sys_s_localization_uk01
/
alter table crs_s_localization rename constraint crs_s_localization_fk01 to crs_sys_s_localization_fk01
/
alter table crs_s_localization rename constraint crs_s_localization_ck01 to crs_sys_s_localization_ck01
/
rename crs_s_localization to crs_sys_s_localization
/
rename crs_s_localization_seq to crs_sys_s_localization_seq
/

--changeset svaliev:crs-1.0-VTBCRS-155-migration-fix logicalFilePath:crs-1.0-VTBCRS-155-migration-fix endDelimiter:/
begin
    for t in (select tc.table_name
                from user_tab_cols tc
               where tc.table_name like 'CRS_L_%'
                 and tc.column_name = 'FILE_STORAGE_ID') loop
        execute immediate 'alter table ' || t.table_name || ' rename column file_storage_id to storage_id';
    end loop;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88 logicalFilePath:crs-1.0-VTBCRS-88 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'SYSTEM_OBJECT';

    -- entity ENTITY
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'ENTITY', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Сущность',
            'Entity',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-2 logicalFilePath:crs-1.0-VTBCRS-88-2 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'SYSTEM_OBJECT';

    -- entity CALC_FORMULA
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CALC_MODEL', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Методика',
            'Calculation model',
            'null',
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-2-1 logicalFilePath:crs-1.0-VTBCRS-88-2-1 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute NAME_RU
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#NAME_RU',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Наименование методики',
            'Model name',
            1,
            null,
            0,
            'STRING',
            'NAME_RU');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-3 logicalFilePath:crs-1.0-VTBCRS-88-3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute NAME_EN
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#NAME_EN',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Наименование методики',
            'Model name',
            1,
            null,
            0,
            'STRING',
            'NAME_EN');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-4 logicalFilePath:crs-1.0-VTBCRS-88-4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute PUBLISHED
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#PUBLISHED',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Признак публикации',
            'Published flag',
            1,
            null,
            0,
            'BOOLEAN',
            'PUBLISHED');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-5-0 logicalFilePath:crs-1.0-VTBCRS-88-5-0 endDelimiter:/
alter table crs_s_calc_model modify actuality date
/

--changeset pmasalov:crs-1.0-VTBCRS-88-5 logicalFilePath:crs-1.0-VTBCRS-88-5 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute ACTUALITY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#ACTUALITY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Дата актуальности',
            'Point in time the current model',
            1,
            null,
            0,
            'DATETIME',
            'ACTUALITY');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-6 logicalFilePath:crs-1.0-VTBCRS-88-6 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute VERSION
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#VERSION',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Версия',
            'Version',
            1,
            null,
            0,
            'NUMBER',
            'VERSION');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-7 logicalFilePath:crs-1.0-VTBCRS-88-7 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute COMMENT_RU
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#COMMENT_RU',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Комментарий',
            'Comment',
            1,
            null,
            0,
            'TEXT',
            'COMMENT_RU');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-8 logicalFilePath:crs-1.0-VTBCRS-88-8 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute COMMENT_EN
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#COMMENT_EN',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Комментарий',
            'Comment',
            1,
            null,
            0,
            'TEXT',
            'COMMENT_EN');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-9 logicalFilePath:crs-1.0-VTBCRS-88-9 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute FORMULAS
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#FORMULAS',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CALC_MODEL_FORMULA',
            null,
            'Показатели методики',
            'Model''s formulas',
            1,
            'CALC_FORMULA',
            0,
            'REFERENCE',
            'FORMULAS');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-10 logicalFilePath:crs-1.0-VTBCRS-88-10 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute INPUT_FORMS
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#INPUT_FORMS',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CALC_MODEL_ENTITY',
            null,
            'Формы методики',
            'Model''s forms',
            1,
            'ENTITY',
            0,
            'REFERENCE',
            'INPUT_FORMS');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-88-11 logicalFilePath:crs-1.0-VTBCRS-88-11 endDelimiter:/
-- Add/modify columns
alter table crs_l_calc_model_formula rename column model_id to calc_model_id
/
alter table crs_l_calc_model_formula rename column formula_id to calc_formula_id
/
-- create/recreate indexes
drop index crs_l_calc_model_formula_i01
/
create index crs_l_calc_model_formula_i01 on crs_l_calc_model_formula (calc_model_id) tablespace spoindx
/
drop index crs_l_calc_model_formula_i02
/
create index crs_l_calc_model_formula_i02 on crs_l_calc_model_formula (calc_formula_id) tablespace spoindx
/
-- create/recreate primary, unique and foreign key constraints
alter table crs_l_calc_model_formula drop constraint crs_l_calc_model_formula_fk01
/
alter table crs_l_calc_model_formula
  add constraint crs_l_calc_model_formula_fk01 foreign key (calc_model_id)
references crs_h_calc_model (id)
/
alter table crs_l_calc_model_formula
    drop constraint crs_l_calc_model_formula_fk02
/
alter table crs_l_calc_model_formula
  add constraint crs_l_calc_model_formula_fk02 foreign key (calc_formula_id)
references crs_h_calc_formula (id)
/

--changeset pmasalov:crs-1.0-VTBCRS-88-12 logicalFilePath:crs-1.0-VTBCRS-88-12 endDelimiter:/
-- Add/modify columns
alter table crs_l_calc_model_entity rename column model_id to calc_model_id
/
-- create/recreate indexes
drop index crs_l_calc_model_entity_i02
/
create index crs_l_calc_model_entity_i02 on crs_l_calc_model_entity (calc_model_id) tablespace spoindx
/
-- create/recreate primary, unique and foreign key constraints
alter table crs_l_calc_model_entity drop constraint crs_l_calc_model_entity_fk01
/
alter table crs_l_calc_model_entity
  add constraint crs_l_calc_model_entity_fk01 foreign key (calc_model_id)
references crs_h_calc_model (id)
/

--changeset svaliev:crs-1.0-VTBCRS-88-embedded-object-type logicalFilePath:crs-1.0-VTBCRS-88-embedded-object-type endDelimiter:/
update crs_sys_h_entity_type et
   set et.key = 'EMBEDDED_OBJECT'
 where et.key = 'SYSTEM_OBJECT'
/

--changeset svaliev:crs-1.0-VTBCRS-88-calc-formula-metadata-desc logicalFilePath:crs-1.0-VTBCRS-88-calc-formula-metadata-desc endDelimiter:/
update crs_sys_s_entity e
   set e.name_ru = 'Показатель', e.name_en = 'Calculation formula'
 where exists (select 1
                 from crs_sys_h_entity eh
                where eh.key = 'CALC_FORMULA'
                  and eh.id = e.h_id)
/
update crs_sys_s_attribute a
   set a.name_ru = 'Наименование'
 where exists (select 1
                 from crs_sys_h_attribute ah
                where ah.key = 'CALC_FORMULA#NAME_RU'
                  and ah.id = a.h_id)
/
update crs_sys_s_attribute a
set a.name_ru = 'Наименование'
where exists (select 1
                from crs_sys_h_attribute ah
               where ah.key = 'CALC_FORMULA#NAME_EN'
                 and ah.id = a.h_id)
/
update crs_sys_s_attribute a
set a.name_ru = 'Мастер показатель', a.name_en = 'Master flag'
where exists (select 1
                from crs_sys_h_attribute ah
               where ah.key = 'CALC_FORMULA#MASTER'
                 and ah.id = a.h_id)
/

--changeset svaliev:crs-1.0-fix-fk-formula-result-satellite logicalFilePath:crs-1.0-fix-fk-formula-result-satellite endDelimiter:/
alter table crs_s_calc_formula_result drop constraint crs_s_calc_formula_result_fk01
/
alter table crs_s_calc_formula_result add constraint crs_s_calc_formula_result_fk01 foreign key (h_id) references crs_h_calc_formula_result (id)
/

--changeset svaliev:crs-1.0-VTBCRS-169 logicalFilePath:crs-1.0-VTBCRS-169 endDelimiter:/
begin
    for t in (select tc.*
                from user_tab_cols tc
               where tc.table_name like 'CRS_H_%'
                 and tc.column_name = 'KEY'
                 and not exists (select 1
                                   from user_constraints uc
                                  where uc.table_name = tc.table_name
                                    and uc.constraint_name like '%_CK01')) loop
        execute immediate 'update ' || t.table_name ||
                            ' set key = upper(trim(key))';
        execute immediate 'alter table ' || t.table_name || ' add constraint ' || t.table_name || '_ck01 check (key = upper(trim(key)))';
    end loop;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89 logicalFilePath:crs-1.0-VTBCRS-89 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    -- entity CALC
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CALC', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Расчет',
            'Calculation',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-2 logicalFilePath:crs-1.0-VTBCRS-89-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    -- intable attribute NAME
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC#NAME',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Наименование',
            'Name',
            1,
            null,
            0,
            'STRING',
            'NAME');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-3 logicalFilePath:crs-1.0-VTBCRS-89-3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    -- intable attribute ACTUALITY
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC#ACTUALITY',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Дата актуализации',
            'Actual date',
            1,
            null,
            0,
            'DATETIME',
            'ACTUALITY');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-4 logicalFilePath:crs-1.0-VTBCRS-89-4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    -- intable attribute PUBLISHED
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC#PUBLISHED',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Расчет опубликован',
            'Published calculation',
            1,
            null,
            0,
            'BOOLEAN',
            'PUBLISHED');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-5 logicalFilePath:crs-1.0-VTBCRS-89-5 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    -- intable attribute CALCULATED
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC#CALCULATED',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Расчет выполнен',
            'Сalculated flag',
            1,
            null,
            0,
            'BOOLEAN',
            'CALCULATED');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-6 logicalFilePath:crs-1.0-VTBCRS-89-6 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    -- entity USER
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'USER', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Пользователь',
            'User',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-7 logicalFilePath:crs-1.0-VTBCRS-89-7 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    -- intable attribute AUTHOR
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#AUTHOR',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'crs_l_calc_user',
            null,
            'Автор расчета',
            'Calculation''s author',
            0,
            'USER',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-8 logicalFilePath:crs-1.0-VTBCRS-89-8 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    -- intable attribute MODEL
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#MODEL',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CALC_MODEL',
            null,
            'Методика вычисления',
            'Calculation model',
            0,
            'CALC_MODEL',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-9 logicalFilePath:crs-1.0-VTBCRS-89-9 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    -- entity CALC_FORMULA_RESULT
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CALC_FORMULA_RESULT', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Результат вычисления показателя',
            'Calculation formula result',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-89-11 logicalFilePath:crs-1.0-VTBCRS-89-11 endDelimiter:/
-- Add/modify columns
alter table crs_l_calc_formula_result rename column formula_result_id to calc_formula_result_id
/
-- Create/Recreate indexes
drop index crs_l_calc_formula_result_i03
/
create index crs_l_calc_formula_result_i03 on crs_l_calc_formula_result (calc_formula_result_id) tablespace spoindx
/
-- Create/Recreate primary, unique and foreign key constraints
alter table crs_l_calc_formula_result drop constraint crs_l_calc_formula_result_fk03
/
alter table crs_l_calc_formula_result
  add constraint crs_l_calc_formula_result_fk03 foreign key (calc_formula_result_id)
references crs_h_calc_formula_result (id)
/

--changeset pmasalov:crs-1.0-VTBCRS-89-12 logicalFilePath:crs-1.0-VTBCRS-89-12 endDelimiter:/
-- Add/modify columns
alter table crs_l_calc_model rename column model_id to calc_model_id
/
-- Create/Recreate indexes
drop index crs_l_calc_model_i03
/
create index crs_l_calc_model_i03 on crs_l_calc_model (calc_model_id) tablespace spoindx
/
-- Create/Recreate primary, unique and foreign key constraints
alter table crs_l_calc_model drop constraint crs_l_calc_model_fk02
/
alter table crs_l_calc_model
  add constraint crs_l_calc_model_fk02 foreign key (calc_model_id)
references crs_h_calc_model (id)
/

--changeset mkuzmin:crs-1.0-VTBCRS-199 logicalFilePath:crs-1.0-VTBCRS-199 endDelimiter:/
alter table crs_calc_formula_tree_t add eval_lang varchar2(100) not null
/
comment on column crs_calc_formula_tree_t.eval_lang is 'Evaluation language'
/

--changeset svaliev:crs-1.0-add-classifier-type logicalFilePath:crs-1.0-add-classifier-type endDelimiter:/
insert into crs_sys_h_entity_type (id, key, ldts)
values (crs_sys_h_entity_type_seq.nextval, 'CLASSIFIER', systimestamp)
/

--changeset pmasalov:crs-1.0-VTBCRS-87 logicalFilePath:crs-1.0-VTBCRS-87 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA_RESULT';

    -- intable attribute NUMBER_RESULT
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA_RESULT#NUMBER_RESULT',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Числовой результат',
            'Result as number',
            1,
            null,
            0,
            'NUMBER',
            'NUMBER_RESULT');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-87-2 logicalFilePath:crs-1.0-VTBCRS-87-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA_RESULT';

    -- intable attribute STRING_RESULT
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA_RESULT#STRING_RESULT',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Строковый результат',
            'Result as string',
            1,
            null,
            0,
            'STRING',
            'STRING_RESULT');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-87-3 logicalFilePath:crs-1.0-VTBCRS-87-3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA_RESULT';

    -- intable attribute EXCEPTION
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA_RESULT#EXCEPTION',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Протокол ошибки',
            'Exception log',
            1,
            null,
            0,
            'TEXT',
            'EXCEPTION');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-87-4 logicalFilePath:crs-1.0-VTBCRS-87-4 endDelimiter:/
-- set options for good parallel IUD operations
alter table crs_l_calc_formula_result initrans 8
/
alter index crs_l_calc_formula_result_pk initrans 8
/
alter index crs_l_calc_formula_result_i01 initrans 8
/
alter index crs_l_calc_formula_result_i02 initrans 8
/
alter index crs_l_calc_formula_result_i03 initrans 8
/

alter table crs_s_calc_formula_result initrans 8
/
alter index crs_s_calc_formula_result_i01 initrans 8
/
alter index crs_s_calc_formula_result_pk initrans 8
/
alter index crs_s_calc_formula_result_uk01 initrans 8
/

alter table crs_h_calc_formula_result initrans 8
/
alter index crs_h_calc_formula_result_i01 initrans 8
/
alter index crs_h_calc_formula_result_pk initrans 8
/
alter index crs_h_calc_formula_result_uk01 initrans 8
/

--changeset pmasalov:crs-1.0-VTBCRS-87-5 logicalFilePath:crs-1.0-VTBCRS-87-5 endDelimiter:/
-- add DIGEST column
alter table crs_s_calc_formula_result add digest varchar2(100) default 'null' not null
/
comment on column crs_s_calc_formula_result.digest is 'Row digest'
/
alter table crs_s_calc_formula_result modify digest default null
/

--changeset mkuzmin:crs-1.0-VTBCRS-111 logicalFilePath:crs-1.0-VTBCRS-111 endDelimiter:/
update crs_sys_s_attribute
   set name_ru = 'Наименование модели'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#NAME_RU')
/
update crs_sys_s_attribute
   set name_ru = 'Наименование модели'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#NAME_EN')
/
update crs_sys_s_attribute
   set name_ru = 'Показатели модели'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#FORMULAS')
/
update crs_sys_s_attribute
   set name_ru = 'Формы модели'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#INPUT_FORMS')
/

--changeset mkuzmin:crs-1.0-VTBCRS-111-2 logicalFilePath:crs-1.0-VTBCRS-111-2 endDelimiter:/
update crs_sys_s_attribute
   set name_ru = 'Наименование модели (RU)',
       name_en = 'Model name (RU)',
       view_order = 1
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#NAME_RU')
/
update crs_sys_s_attribute
   set name_ru = 'Наименование модели (EN)',
       name_en = 'Model name (EN)',
       view_order = 2
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#NAME_EN')
/
update crs_sys_s_attribute
   set view_order = 3
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#VERSION')
/
update crs_sys_s_attribute
   set view_order = 4
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#ACTUALITY')
/
update crs_sys_s_attribute
   set view_order = 5
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#PUBLISHED')
/

--changeset mkuzmin:crs-1.0-VTBCRS-111-3 logicalFilePath:crs-1.0-VTBCRS-111-3 endDelimiter:/
update crs_sys_s_attribute
   set name_ru = 'Комментарий (RU)',
       name_en = 'Comment (RU)',
       view_order = 6
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#COMMENT_RU')
/
update crs_sys_s_attribute
   set name_ru = 'Комментарий (EN)',
       name_en = 'Comment (EN)',
       view_order = 7
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#COMMENT_EN')
/

--changeset pmasalov:crs-1.0-VTBCRS-206-0 logicalFilePath:crs-1.0-VTBCRS-206-0 endDelimiter:/
create sequence crs_l_calc_model_classfr_seq nocache
/

--changeset pmasalov:crs-1.0-VTBCRS-206 logicalFilePath:crs-1.0-VTBCRS-206 endDelimiter:/
create table crs_l_calc_model_classfr (
    id                 number not null,
    ldts               timestamp not null,
    removed            number(1) not null,
    calc_model_id      number not null,
    entity_id          number not null,
    constraint crs_l_calc_model_classfr_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_calc_model_classfr_fk01 foreign key (calc_model_id) references crs_h_calc_model(id),
    constraint crs_l_calc_model_classfr_fk02 foreign key (entity_id) references crs_sys_h_entity(id)
)
/
comment on table crs_l_calc_model_classfr is 'Model to classifier link'
/
comment on column crs_l_calc_model_classfr.id is 'Identifier'
/
comment on column crs_l_calc_model_classfr.calc_model_id is 'Reference to model'
/
comment on column crs_l_calc_model_classfr.entity_id is 'Reference to classifier'
/
comment on column crs_l_calc_model_classfr.ldts is 'Load date'
/

--changeset pmasalov:crs-1.0-VTBCRS-206-1 logicalFilePath:crs-1.0-VTBCRS-206-1 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_L_CALC_MODEL_CLASSFR_I01', 'CRS_L_CALC_MODEL_CLASSFR_I02', 'CRS_L_CALC_MODEL_CLASSFR_I03')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_l_calc_model_classfr_i01 on crs_l_calc_model_classfr(calc_model_id) compress 1 tablespace spoindx
/
create index crs_l_calc_model_classfr_i02 on crs_l_calc_model_classfr(entity_id) compress 1 tablespace spoindx
/
create index crs_l_calc_model_classfr_i03 on crs_l_calc_model_classfr(ldts) tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-206-1-1 logicalFilePath:crs-1.0-VTBCRS-206-1-1 endDelimiter:/
alter table crs_l_calc_model_classfr
  add constraint crs_l_calc_model_classfr_ck01
check (removed in (0, 1))
/

--changeset pmasalov:crs-1.0-VTBCRS-206-2 logicalFilePath:crs-1.0-VTBCRS-206-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- reference attribute FORMULAS
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#CLASSIFIERS',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CALC_MODEL_CLASSFR',
            null,
            'Классификаторы',
            'Model''s classifiers',
            1,
            'ENTITY',
            0,
            'REFERENCE',
            'CLASSIFIERS');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-206-3 logicalFilePath:crs-1.0-VTBCRS-206-3 endDelimiter:/
alter table crs_l_calc_model_entity rename to crs_l_calc_model_form
/

--changeset pmasalov:crs-1.0-VTBCRS-206-4 logicalFilePath:crs-1.0-VTBCRS-206-4 endDelimiter:/
rename crs_l_calc_model_entity_seq to crs_l_calc_model_form_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-206-5 logicalFilePath:crs-1.0-VTBCRS-206-5 endDelimiter:/
alter table crs_l_calc_model_form
     rename constraint crs_l_calc_model_entity_ck01 to crs_l_calc_model_form_ck01
/
--changeset pmasalov:crs-1.0-VTBCRS-206-6 logicalFilePath:crs-1.0-VTBCRS-206-6 endDelimiter:/
alter index crs_l_calc_model_entity_pk rename to crs_l_calc_model_form_pk
/
--changeset pmasalov:crs-1.0-VTBCRS-206-7 logicalFilePath:crs-1.0-VTBCRS-206-7 endDelimiter:/
alter table crs_l_calc_model_form
     rename constraint crs_l_calc_model_entity_pk to crs_l_calc_model_form_pk
/
--changeset pmasalov:crs-1.0-VTBCRS-206-8 logicalFilePath:crs-1.0-VTBCRS-206-8 endDelimiter:/
alter table crs_l_calc_model_form
     rename constraint crs_l_calc_model_entity_fk01 to crs_l_calc_model_form_fk01
/
--changeset pmasalov:crs-1.0-VTBCRS-206-9 logicalFilePath:crs-1.0-VTBCRS-206-9 endDelimiter:/
alter table crs_l_calc_model_form
     rename constraint crs_l_calc_model_entity_fk02 to crs_l_calc_model_form_fk02
/
--changeset pmasalov:crs-1.0-VTBCRS-206-10 logicalFilePath:crs-1.0-VTBCRS-206-10 endDelimiter:/
alter index crs_l_calc_model_entity_i01 rename to crs_l_calc_model_form_i01
/
--changeset pmasalov:crs-1.0-VTBCRS-206-11 logicalFilePath:crs-1.0-VTBCRS-206-11 endDelimiter:/
alter index crs_l_calc_model_entity_i02 rename to crs_l_calc_model_form_i02
/
--changeset pmasalov:crs-1.0-VTBCRS-206-12 logicalFilePath:crs-1.0-VTBCRS-206-12 endDelimiter:/
alter index crs_l_calc_model_entity_i03 rename to crs_l_calc_model_form_i03
/

--changeset pmasalov:crs-1.0-VTBCRS-206-13 logicalFilePath:crs-1.0-VTBCRS-206-13 endDelimiter:/
update crs_sys_s_attribute set link_table = 'CRS_L_CALC_MODEL_FORM' where link_table = 'CRS_L_CALC_MODEL_ENTITY'
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-154 logicalFilePath:crs-1.0-VTBCRS-154 endDelimiter:/
alter table crs_l_s_calc_formula add link_id number
/
comment on column crs_l_s_calc_formula.link_id is 'Link table identifier'
/

--changeset pmasalov:crs-1.0-VTBCRS-154-2 logicalFilePath:crs-1.0-VTBCRS-154-2 endDelimiter:/
update crs_l_s_calc_formula s
   set (link_id,ldts) = (select max(id), max(ldts)
                           from crs_l_calc_formula l
                          where l.formula_id = s.formula_id
                            and l.formula_parent_id = s.formula_parent_id
                            and s.ldts >= l.ldts)
 where s.link_id is null
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-154-3 logicalFilePath:crs-1.0-VTBCRS-154-3 endDelimiter:/
alter table crs_l_s_calc_formula modify link_id not null
/

--changeset pmasalov:crs-1.0-VTBCRS-154-4 logicalFilePath:crs-1.0-VTBCRS-154-4 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_L_S_CALC_FORMULA_I01', 'CRS_L_S_CALC_FORMULA_I02', 'CRS_L_S_CALC_FORMULA_I03')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-154-5 logicalFilePath:crs-1.0-VTBCRS-154-5 endDelimiter:/
alter table crs_l_s_calc_formula
  add constraint crs_l_s_calc_formula_uk01 unique (link_id, ldts) using index compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-154-6 logicalFilePath:crs-1.0-VTBCRS-154-6 endDelimiter:/
alter table crs_l_s_calc_formula drop constraint crs_l_s_calc_formula_fk01
/
alter table crs_l_s_calc_formula drop constraint crs_l_s_calc_formula_fk02
/
--changeset pmasalov:crs-1.0-VTBCRS-154-7 logicalFilePath:crs-1.0-VTBCRS-154-7 endDelimiter:/
alter table crs_l_s_calc_formula drop column formula_id
/
alter table crs_l_s_calc_formula drop column formula_parent_id
/

--changeset pmasalov:crs-1.0-VTBCRS-238 logicalFilePath:crs-1.0-VTBCRS-238 endDelimiter:/
create or replace synonym crs_h_entity for crs_sys_h_entity
/
create or replace synonym crs_s_entity for crs_sys_s_entity
/

--changeset pmasalov:crs-1.0-VTBCRS-238-1 logicalFilePath:crs-1.0-VTBCRS-238-1 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY#FORM',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Форма отображения',
            'UI form',
            1,
            null,
            0,
            'STRING',
            'FORM');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-238-2 logicalFilePath:crs-1.0-VTBCRS-238-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY#NAME_RU',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Наименование (ru)',
            'Name (ru)',
            1,
            null,
            0,
            'STRING',
            'NAME_RU');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-238-3 logicalFilePath:crs-1.0-VTBCRS-238-3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY#NAME_EN',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Наименование (en)',
            'Name (en)',
            1,
            null,
            0,
            'STRING',
            'NAME_EN');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-238-4 logicalFilePath:crs-1.0-VTBCRS-238-4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY#LINK_TABLE',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Таблица связи для организации иерархии',
            'Link table for hierarchy support',
            1,
            null,
            0,
            'STRING',
            'LINK_TABLE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-238-6 logicalFilePath:crs-1.0-VTBCRS-238-6 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY#ATTRIBUTE_KEY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Атрибут для отображения',
            'Satellite attribute to display',
            1,
            null,
            0,
            'STRING',
            'ATTRIBUTE_KEY');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/
comment on column crs_sys_s_entity.attribute_key is 'Satellite attribute to display'
/

--changeset pmasalov:crs-1.0-VTBCRS-238-7 logicalFilePath:crs-1.0-VTBCRS-238-7 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY#HIERARCHICAL',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Признак иерархии',
            'Hierarchical flag',
            1,
            null,
            0,
            'BOOLEAN',
            'HIERARCHICAL');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-208 logicalFilePath:crs-1.0-VTBCRS-208 endDelimiter:/
alter table crs_sys_s_entity add comment_ru clob
/
comment on column crs_sys_s_entity.comment_ru is 'Comment (ru)'
/
alter table crs_sys_s_entity add comment_en clob
/
comment on column crs_sys_s_entity.comment_en is 'Comment (en)'
/

--changeset akirilchev:crs-1.0-VTBCRS-257 logicalFilePath:crs-1.0-VTBCRS-257 endDelimiter:/
alter table crs_s_calc_formula_desc modify result_type null
/
update crs_s_calc_formula_desc
   set result_type = null
 where library = 1
/

--changeset akirilchev:crs-1.0-VTBCRS-220 logicalFilePath:crs-1.0-VTBCRS-220 endDelimiter:/
update crs_sys_s_attribute
   set name_ru = 'Модель',
       name_en = 'Model'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC#MODEL')
/
update crs_sys_s_entity
   set name_ru = 'Модель',
       name_en = 'Model'
 where h_id = (select id from crs_sys_h_entity where key = 'CALC_MODEL')
/

--changeset emelnikov:crs-1.0-VTBCRS-245 logicalFilePath:crs-1.0-VTBCRS-245 endDelimiter:/
create table crs_h_form_template(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_form_template_pk primary key (id),
    constraint crs_h_form_template_uk1 unique (key)
)
/
comment on table crs_h_form_template is 'Form template hub'
/
comment on column crs_h_form_template.id is 'Identifier'
/
comment on column crs_h_form_template.key is 'Key'
/
comment on column crs_h_form_template.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-245-1 logicalFilePath:crs-1.0-VTBCRS-245-1 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_H_FORM_TEMPLATE_I01')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_h_form_template_i01 on crs_h_form_template (ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-245-2 logicalFilePath:crs-1.0-VTBCRS-245-2 endDelimiter:/
create table crs_s_form_template(
    id                 number not null,
    h_id               number not null,
    ldts               timestamp not null,
    removed            number(1) default 0 not null,
    digest             varchar2(100) not null,
    type               varchar2(20) not null,
    format             varchar2(20) not null,
    constraint crs_s_form_template_pk primary key (id),
    constraint crs_s_form_template_uk1 unique (ldts, h_id),
    constraint crs_s_form_template_fk01 foreign key (h_id) references crs_h_form_template(id)
)
/

--changeset emelnikov:crs-1.0-VTBCRS-245-3 logicalFilePath:crs-1.0-VTBCRS-245-3 endDelimiter:/
comment on table crs_s_form_template is 'Form template satellite'
/
comment on column crs_s_form_template.id is 'Identifier'
/
comment on column crs_s_form_template.h_id is 'Reference to hub'
/
comment on column crs_s_form_template.ldts is 'Load date'
/
comment on column crs_s_form_template.removed is 'Removed flag'
/
comment on column crs_s_form_template.digest is 'Row digest'
/
comment on column crs_s_form_template.type is 'Type'
/
comment on column crs_s_form_template.format is 'Format'
/

--changeset emelnikov:crs-1.0-VTBCRS-245-4 logicalFilePath:crs-1.0-VTBCRS-245-4 endDelimiter:/
create table crs_l_form_template_name (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    form_template_id      number not null,
    form_template_name_id number not null,
    constraint crs_l_form_template_name_pk primary key(id),
    constraint crs_l_form_template_name_fk01 foreign key(form_template_id) references crs_h_form_template(id),
    constraint crs_l_form_template_name_fk02 foreign key(form_template_name_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_form_template_name is 'Form template to multilang name type link'
/
comment on column crs_l_form_template_name.id is 'Identifier'
/
comment on column crs_l_form_template_name.form_template_id is 'Reference to form template'
/
comment on column crs_l_form_template_name.form_template_name_id is 'Reference to form template multilang'
/
comment on column crs_l_form_template_name.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-245-5 logicalFilePath:crs-1.0-VTBCRS-245-5 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_L_FORM_TEMPLATE_NAME_I01', 'CRS_L_FORM_TEMPLATE_NAME_I02', 'CRS_L_FORM_TEMPLATE_NAME_I03')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_l_form_template_name_i01 on crs_l_form_template_name(form_template_id) compress 1 tablespace spoindx
/
create index crs_l_form_template_name_i02 on crs_l_form_template_name(form_template_name_id) compress 1 tablespace spoindx
/
create index crs_l_form_template_name_i03 on crs_l_form_template_name(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-245-6 logicalFilePath:crs-1.0-VTBCRS-245-6 endDelimiter:/
create table crs_l_form_template_data (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    form_template_id      number not null,
    form_template_data_id number not null,
    constraint crs_l_form_template_data_pk primary key(id),
    constraint crs_l_form_template_data_fk01 foreign key(form_template_id) references crs_h_form_template(id),
    constraint crs_l_form_template_data_fk02 foreign key(form_template_data_id) references crs_sys_h_storage(id)
)
/
comment on table crs_l_form_template_data is 'Form template to data object type link'
/
comment on column crs_l_form_template_data.id is 'Identifier'
/
comment on column crs_l_form_template_data.form_template_id is 'Reference to form template'
/
comment on column crs_l_form_template_data.form_template_data_id is 'Reference to form template data object'
/
comment on column crs_l_form_template_data.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-245-7 logicalFilePath:crs-1.0-VTBCRS-245-7 endDelimiter:/
begin
    for r in (select * from user_indexes where index_name in ('CRS_L_FORM_TEMPLATE_DATA_I01', 'CRS_L_FORM_TEMPLATE_DATA_I02', 'CRS_L_FORM_TEMPLATE_DATA_I03')) loop
        execute immediate 'drop index '||r.index_name;
    end loop;
end;
/
create index crs_l_form_template_data_i01 on crs_l_form_template_data(form_template_id) compress 1 tablespace spoindx
/
create index crs_l_form_template_data_i02 on crs_l_form_template_data(form_template_data_id) compress 1 tablespace spoindx
/
create index crs_l_form_template_data_i03 on crs_l_form_template_data(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-245-8 logicalFilePath:crs-1.0-VTBCRS-245-8 endDelimiter:/
begin
    for r in (select * from user_sequences where sequence_name in ('CRS_L_FORM_TEMPLATE_DATA_SEQ',
                                                                   'CRS_L_FORM_TEMPLATE_NAME_SEQ',
                                                                   'CRS_S_FORM_TEMPLATE_SEQ',
                                                                   'CRS_H_FORM_TEMPLATE_SEQ')) loop
        execute immediate 'drop sequence '||r.sequence_name;
    end loop;
end;
/
create sequence crs_l_form_template_data_seq
/
create sequence crs_l_form_template_name_seq
/
create sequence crs_s_form_template_seq
/
create sequence crs_h_form_template_seq
/

--changeset emelnikov:crs-1.0-VTBCRS-245-9 logicalFilePath:crs-1.0-VTBCRS-245-9 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('1111-JAN-01 00:00:00', 'YYYY-MON-DD HH24:MI:SS');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    -- entity FORM_TEMPLATE
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'FORM_TEMPLATE', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Шаблон импорта/экспорта',
            'Form template',
            'null',
            null,
            1,
            0
    );

    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval,
            v_entity_type_id,
            v_h_entity_id,
            v_ldts,
            0);

    -- multilang attribute NAME
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'FORM_TEMPLATE#NAME', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_FORM_TEMPLATE_NAME',
            null,
            'Наименование шаблона',
            'Template name',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- data attribute
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'FORM_TEMPLATE#OBJECT', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_FORM_TEMPLATE_DATA',
            null,
            'Данные шаблона',
            'Template data',
            0,
            null,
            0,
            'FILE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset emelnikov:crs-1.0-VTBCRS-245-10 logicalFilePath:crs-1.0-VTBCRS-245-10 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := TO_DATE('1111-JAN-01 00:00:00', 'YYYY-MON-DD HH24:MI:SS');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FORM_TEMPLATE';

    -- intable attribute format
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FORM_TEMPLATE#FORMAT',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Формат шаблона',
            'Template format',
            1,
            null,
            0,
            'STRING',
            'FORMAT');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset emelnikov:crs-1.0-VTBCRS-245-11 logicalFilePath:crs-1.0-VTBCRS-245-11 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := TO_DATE('1111-JAN-01 00:00:00', 'YYYY-MON-DD HH24:MI:SS');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FORM_TEMPLATE';

    -- intable attribute format
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FORM_TEMPLATE#TYPE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Тип шаблона',
            'Template type',
            1,
            null,
            0,
            'STRING',
            'TYPE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset svaliev:crs-1.0-increase-attribute-key-length logicalFilePath:crs-1.0-increase-attribute-key-length endDelimiter:/
alter table crs_sys_s_attribute modify attribute_key varchar2(100)
/

--changeset akirilchev:crs-1.0-VTBCRS-239-add-desc-type logicalFilePath:crs-1.0-VTBCRS-239-add-desc-type endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tab_cols where table_name = 'CRS_S_CALC_FORMULA_DESC' and column_name = 'TYPE'
alter table crs_s_calc_formula_desc add type varchar2(30)
/
comment on column crs_s_calc_formula_desc.type is 'formula type'
/

--changeset akirilchev:crs-1.0-VTBCRS-add-tree-type logicalFilePath:crs-1.0-VTBCRS-239-add-tree-type endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tab_cols where table_name = 'CRS_CALC_FORMULA_TREE_T' and column_name = 'TYPE'
alter table crs_calc_formula_tree_t add type varchar2(30)
/
comment on column crs_calc_formula_tree_t.type is 'formula type'
/

--changeset akirilchev:crs-1.0-VTBCRS-239-fill logicalFilePath:crs-1.0-VTBCRS-239-fill endDelimiter:/
update crs_s_calc_formula_desc s
   set type = case when s.library = 1
                   then 'LIBRARY'
                   when s.master = 1
                   then 'MASTER_FORMULA'
                   else 'FORMULA'
              end
/
update crs_calc_formula_tree_t s
   set type = case when s.library = 1
                   then 'LIBRARY'
                   when s.master = 1
                   then 'MASTER_FORMULA'
                   else 'FORMULA'
              end
/

--changeset akirilchev:crs-1.0-VTBCRS-239-delete logicalFilePath:crs-1.0-VTBCRS-239-delete endDelimiter:/
delete crs_sys_l_entity_attribute where attribute_id in (select id from crs_sys_h_attribute where key in ('CALC_FORMULA#LIBRARY', 'CALC_FORMULA#MASTER'))
/
delete crs_sys_s_attribute
 where h_id in (select id from crs_sys_h_attribute where key in ('CALC_FORMULA#LIBRARY', 'CALC_FORMULA#MASTER'))
    or attribute_key in ('CALC_FORMULA#LIBRARY', 'CALC_FORMULA#MASTER')
/
delete crs_sys_h_attribute where key in ('CALC_FORMULA#LIBRARY', 'CALC_FORMULA#MASTER')
/

--changeset akirilchev:crs-1.0-VTBCRS-239-insert logicalFilePath:crs-1.0-VTBCRS-239-insert endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA';

    -- intable attribute TYPE
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA#TYPE',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Тип показателя',
            'Formula type',
            1,
            null,
            0,
            'STRING',
            'TYPE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-239-drop logicalFilePath:crs-1.0-VTBCRS-239-drop endDelimiter:/
alter table crs_s_calc_formula_desc drop constraint crs_s_calc_formula_desc_ck04
/
alter table crs_s_calc_formula_desc drop column library
/
alter table crs_s_calc_formula_desc drop column master
/
alter table crs_calc_formula_tree_t drop column library
/
alter table crs_calc_formula_tree_t drop column master
/

--changeset akirilchev:crs-1.0-VTBCRS-239-type-not-null logicalFilePath:crs-1.0-VTBCRS-239-type-not-null endDelimiter:/
alter table crs_s_calc_formula_desc modify type not null
/

--changeset svaliev:crs-1.0-VTBCRS-154-satellite-fk logicalFilePath:crs-1.0-VTBCRS-154-satellite-fk endDelimiter:/
alter table crs_l_s_calc_formula add constraint crs_l_s_calc_formula_fk01 foreign key (link_id) references crs_l_calc_formula (id)
/

--changeset akirilchev:crs-1.0-VTBCRS-262 logicalFilePath:crs-1.0-VTBCRS-262 endDelimiter:/
create table crs_h_user(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_user_pk primary key (id),
    constraint crs_h_user_uk1 unique (key)
)
/
comment on table crs_h_user is 'User hub'
/
comment on column crs_h_user.id is 'Identifier'
/
comment on column crs_h_user.key is 'Key'
/
comment on column crs_h_user.ldts is 'Load date'
/
create table crs_s_user(
    id         number not null,
    h_id       number not null,
    ldts       timestamp not null,
    removed    number(1) default 0 not null,
    digest     varchar2(100) not null,
    surname    varchar2(100),
    name       varchar2(100),
    patronymic varchar2(100),
    full_name  generated always as (rtrim(((case when surname is not null then surname ||' ' end) || (case when name is not null then name ||' ' end )) || patronymic)) virtual,
    constraint crs_s_user_pk primary key (id),
    constraint crs_s_user_uk1 unique (ldts, h_id),
    constraint crs_s_user_fk01 foreign key (h_id) references crs_h_user(id)
)
/
comment on table crs_s_user is 'User satellite'
/
comment on column crs_s_user.id is 'Identifier'
/
comment on column crs_s_user.h_id is 'Reference to hub'
/
comment on column crs_s_user.ldts is 'Load date'
/
comment on column crs_s_user.removed is 'Removed flag'
/
comment on column crs_s_user.digest is 'Row digest'
/
comment on column crs_s_user.surname is 'Surname'
/
comment on column crs_s_user.name is 'Name'
/
comment on column crs_s_user.patronymic is 'Patronymic'
/
comment on column crs_s_user.full_name is 'Full name'
/
create sequence crs_h_user_seq nocycle nocache
/
create sequence crs_s_user_seq nocycle nocache
/

--changeset akirilchev:crs-1.0-VTBCRS-262-fk-drop logicalFilePath:crs-1.0-VTBCRS-262-fk-drop endDelimiter:/
alter table crs_l_calc_user
 drop constraint crs_l_calc_user_fk02
/

--changeset akirilchev:crs-1.0-VTBCRS-262-migrate logicalFilePath:crs-1.0-VTBCRS-262-migrate endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:1 select case when exists(select 1 from user_tables where table_name = 'USERS') and not exists(select 1 from user_tab_cols where table_name = 'USERS' and column_name = 'IS_AD_HOC_TABLE') then 1 else 0 end from dual
insert into crs_h_user(id, key, ldts)
select id_user, login, to_date('011111', 'mmyyyy')
  from users
/
insert into crs_s_user(id, h_id, ldts, removed, digest, surname, name, patronymic)
select crs_s_user_seq.nextval, h.id, to_date('011111', 'mmyyyy'), 0, 'no_digest', surname, name, patronymic
  from users u join crs_h_user h on h.key = u.login
 where u.is_active = 1
/

--changeset akirilchev:crs-1.0-VTBCRS-262-reset-sequence logicalFilePath:crs-1.0-VTBCRS-262-reset-sequence endDelimiter:/
declare
    procedure recreate_sequence_for_table(p_table_name varchar2, p_column_name varchar2, p_sequence_name varchar2) is
        v_exists number := 0;
        v_current_id number := 0;
        v_sequence_value number;
    begin
        select count(*) into v_exists from user_objects where object_name = upper(p_sequence_name);
        if v_exists = 0 then
            execute immediate 'create sequence '|| p_sequence_name ||' nocycle nocache';
        end if;
        execute immediate 'select max('|| p_column_name ||') + 1 from '|| p_table_name into v_sequence_value;
        execute immediate 'select ' || p_sequence_name || '.nextval + 1 from dual' into v_current_id;
        if v_current_id <> v_sequence_value then
            execute immediate 'alter sequence ' || p_sequence_name || ' increment by ' || to_char(v_sequence_value - v_current_id);
            execute immediate 'select ' || p_sequence_name || '.nextval from dual' into v_current_id;
            execute immediate 'alter sequence ' || p_sequence_name || ' increment by 1';
        end if;
    exception
        when others then
            dbms_output.enable(null);
            dbms_output.put_line(dbms_utility.format_error_backtrace());
            raise_application_error(-20001, 'recreate_sequence_for_table error: "' ||p_table_name||'.'|| p_column_name || '", sequence name "' || p_sequence_name || '": Error is ' || sqlerrm);
    end recreate_sequence_for_table;
begin
    recreate_sequence_for_table('crs_h_user', 'id', 'crs_h_user_seq');
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-262-attr logicalFilePath:crs-1.0-VTBCRS-262-attr endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'USER';

    -- intable attribute FULL_NAME
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'USER#FULL_NAME',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key,
                                     name_ru, name_en,
                                     filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval, v_h_attribute_id, v_ldts, 0, 0, 0, null, null,
            'ФИО', 'Full name',
            1, null, 0, 'STRING', 'FULL_NAME');

    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-262-fk-create logicalFilePath:crs-1.0-VTBCRS-262-fk-create endDelimiter:/
alter table crs_l_calc_user
  add constraint crs_l_calc_user_fk02 foreign key (user_id)
  references crs_h_user(id)
/

--changeset akirilchev:crs-1.0-VTBCRS-262-attr-del logicalFilePath:crs-1.0-VTBCRS-262-attr-del endDelimiter:/
begin
    for r_h_attribute_id in (select id
                               from crs_sys_h_attribute
                              where key = 'USER#FULL_NAME')
    loop
        delete crs_sys_l_entity_attribute
         where entity_id = (select id
                              from crs_sys_h_entity
                             where key = 'USER')
           and attribute_id = r_h_attribute_id.id;

        delete crs_sys_s_attribute
         where h_id = r_h_attribute_id.id;

        delete crs_sys_h_attribute
         where id = r_h_attribute_id.id;
    end loop;
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-262-attr-2 logicalFilePath:crs-1.0-VTBCRS-262-attr-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'USER';

    -- intable attribute SURNAME
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'USER#SURNAME',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key,
                                     name_ru, name_en,
                                     filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval, v_h_attribute_id, v_ldts, 0, 0, 0, null, null,
            'Фамилия', 'Surname',
            1, null, 0, 'STRING', 'SURNAME');

    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-262-attr-3 logicalFilePath:crs-1.0-VTBCRS-262-attr-3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'USER';

    -- intable attribute NAME
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'USER#NAME',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key,
                                     name_ru, name_en,
                                     filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval, v_h_attribute_id, v_ldts, 0, 0, 0, null, null,
            'Имя', 'Name',
            1, null, 0, 'STRING', 'NAME');

    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-262-attr-4 logicalFilePath:crs-1.0-VTBCRS-262-attr-4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'USER';

    -- intable attribute PATRONYMIC
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'USER#PATRONYMIC',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key,
                                     name_ru, name_en,
                                     filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval, v_h_attribute_id, v_ldts, 0, 0, 0, null, null,
            'Отчество', 'Patronymic',
            1, null, 0, 'STRING', 'PATRONYMIC');

    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-277-actuality logicalFilePath:crs-1.0-VTBCRS-277-actuality endDelimiter:/
alter table crs_s_calc add data_actuality timestamp
/
comment on column crs_s_calc.data_actuality is 'Data actuality date'
/
update crs_s_calc set data_actuality = ldts
/
alter table crs_s_calc modify data_actuality not null
/

--changeset akirilchev:crs-1.0-VTBCRS-277 logicalFilePath:crs-1.0-VTBCRS-277 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    -- intable attribute DATA_ACTUALITY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CALC#DATA_ACTUALITY', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key,
                                     name_ru, name_en,
                                     filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval, v_h_attribute_id, v_ldts, 0, 0, 0, null, null,
            'Расчетная дата', 'Data actuality date',
            1, null, 0, 'DATETIME', 'DATA_ACTUALITY');

    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-277-date-upd logicalFilePath:crs-1.0-VTBCRS-277-date-upd endDelimiter:/
declare
    v_entity_key varchar2(30) := 'CALC';
    v_attr_key varchar2(30) := 'CALC#DATA_ACTUALITY';
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    update crs_sys_h_attribute
       set ldts = v_ldts
     where key = v_attr_key;

    update crs_sys_s_attribute
       set ldts = v_ldts
     where h_id in (select id
                      from crs_sys_h_attribute
                     where key = v_attr_key);

    update crs_sys_l_entity_attribute
       set ldts = v_ldts
     where entity_id in (select id
                           from crs_sys_h_entity
                          where key = v_entity_key)
       and attribute_id in (select id
                              from crs_sys_h_attribute
                             where key = v_attr_key);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-281 logicalFilePath:crs-1.0-VTBCRS-281 endDelimiter:/
declare
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    for r_entity in (select e.*
                       from crs_sys_h_entity e
                      where key in (select e.key
                                      from crs_sys_l_entity_type l
                                      join crs_sys_h_entity e on l.entity_id = e.id
                                     where type_id in (select id
                                                         from crs_sys_h_entity_type
                                                        where key in ('EMBEDDED_OBJECT',
                                                                      'PREDEFINED_DICTIONARY')))
                      order by e.key)
    loop
        update crs_sys_h_attribute a
           set a.ldts = v_ldts
         where a.id in (select l.attribute_id
                          from crs_sys_l_entity_attribute l
                         where l.entity_id = r_entity.id);

        update crs_sys_s_attribute a
           set a.ldts = v_ldts
         where a.h_id in (select l.attribute_id
                            from crs_sys_l_entity_attribute l
                           where l.entity_id = r_entity.id);

        update crs_sys_l_entity_attribute
           set ldts = v_ldts
         where entity_id = r_entity.id;

        update crs_sys_h_entity
           set ldts = v_ldts
         where id = r_entity.id;

        update crs_sys_s_entity
           set ldts = v_ldts
         where h_id = r_entity.id;

        update crs_sys_l_entity_type
           set ldts = v_ldts
         where entity_id = r_entity.id;
    end loop;

    update crs_sys_h_entity_type
       set ldts = v_ldts;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-232 logicalFilePath:crs-1.0-VTBCRS-232 endDelimiter:/
create table crs_sys_l_ref_attribute
(
    id             number not null,
    ldts           timestamp not null,
    removed        number(1) default 0 not null,
    attribute_id   number not null,
    attr_attribute_id number not null
)
/

comment on table crs_sys_l_ref_attribute is 'Link between REFERENCE type attribute and it link attributes'
/
comment on column crs_sys_l_ref_attribute.id is 'Identifier'
/
comment on column crs_sys_l_ref_attribute.ldts is 'Load date'
/
comment on column crs_sys_l_ref_attribute.removed is 'Removed flag'
/
comment on column crs_sys_l_ref_attribute.attribute_id is 'REFERENCE attribute primary id'
/
comment on column crs_sys_l_ref_attribute.attr_attribute_id is 'Attribute of REFERENCE attribute'
/

alter table crs_sys_l_ref_attribute
    add constraint crs_sys_l_ref_attribute_pk primary key (ID)
    using index
    tablespace spoindx
/
create unique index crs_sys_l_ref_attribute_i01 on crs_sys_l_ref_attribute (attribute_id, ldts)
tablespace spoindx
/
create unique index crs_sys_l_ref_attribute_i02 on crs_sys_l_ref_attribute (attr_attribute_id, ldts)
tablespace spoindx
/
alter table crs_sys_l_ref_attribute
    add constraint crs_sys_l_ref_attribute_fk01 foreign key (attribute_id)
    references crs_sys_h_attribute (id)
/
alter table crs_sys_l_ref_attribute
    add constraint crs_sys_l_ref_attribute_fk02 foreign key (attr_attribute_id)
    references crs_sys_h_attribute (id)
/
alter table crs_sys_l_ref_attribute
    add constraint crs_sys_l_ref_attribute_ck01
    check (removed in (0, 1))
/

--changeset pmasalov:crs-1.0-VTBCRS-232-1 logicalFilePath:crs-1.0-VTBCRS-232-1 endDelimiter:/
create sequence crs_sys_l_ref_attribute_seq nocache
/

--changeset pmasalov:crs-1.0-VTBCRS-232-2 logicalFilePath:crs-1.0-VTBCRS-232-2 endDelimiter:/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'CALC#MODEL';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#MODEL#VERSION',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_attr_attr_id,
            v_ldts,
            0,
            0,
            'Версия связанной модели',
            'Linked model version',
            1,
            0,
            'NUMBER',
            'VERSION');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-232-3 logicalFilePath:crs-1.0-VTBCRS-232-3 endDelimiter:/
create table crs_l_s_calc_model
(
    id number not null,
    link_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null,
    digest varchar2(100) not null,
    version number
)
/
comment on table crs_l_s_calc_model is 'Calculation satellite for link table'
/
comment on column crs_l_s_calc_model.id is 'Identifier'
/
comment on column crs_l_s_calc_model.ldts is 'Load date'
/
comment on column crs_l_s_calc_model.version is 'Version of model used at link'
/
comment on column crs_l_s_calc_model.removed is 'Removed flag'
/
comment on column crs_l_s_calc_model.link_id is 'Link table identifier'
/
comment on column crs_l_s_calc_model.digest is 'Row digest'
/
alter table crs_l_s_calc_model
    add constraint crs_l_s_calc_model_pk primary key (ID)
    using index
    tablespace spoindx
/
alter table crs_l_s_calc_model
    add constraint crs_l_s_calc_model_uk01 unique (LINK_ID, LDTS)
    using index
    tablespace SPOINDX
/
alter table crs_l_s_calc_model
    add constraint crs_l_s_calc_model_ck01
check (removed in (0, 1))
/
alter table crs_l_s_calc_model
    add constraint crs_l_s_calc_model_fk01 foreign key (link_id)
references crs_l_calc_model (id)
/

--changeset pmasalov:crs-1.0-VTBCRS-232-4 logicalFilePath:crs-1.0-VTBCRS-232-4 endDelimiter:/
create sequence crs_l_s_calc_model_seq
/

--changeset emelnikov:crs-1.0-VTBCRS-268 logicalFilePath:crs-1.0-VTBCRS-268 endDelimiter:/
create table crs_h_client(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_client_pk primary key (id),
    constraint crs_h_client_uk1 unique (key)
)
/
comment on table crs_h_client is 'Client hub'
/
comment on column crs_h_client.id is 'Identifier'
/
comment on column crs_h_client.key is 'Key'
/
comment on column crs_h_client.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-1 logicalFilePath:crs-1.0-VTBCRS-268-1 endDelimiter:/
create index crs_h_client_i01 on crs_h_client (ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-2 logicalFilePath:crs-1.0-VTBCRS-268-2 endDelimiter:/
create table crs_s_client(
    id                 number not null,
    h_id               number not null,
    ldts               timestamp not null,
    removed            number(1) default 0 not null,
    digest             varchar2(100) not null,
    reg_countrycode    varchar2(10),
    ogrn               varchar2(15),
    constraint crs_s_client_pk primary key (id),
    constraint crs_s_client_uk1 unique (ldts, h_id),
    constraint crs_s_client_fk01 foreign key (h_id) references crs_h_client(id)
)
/

--changeset emelnikov:crs-1.0-VTBCRS-268-3 logicalFilePath:crs-1.0-VTBCRS-268-3 endDelimiter:/
comment on table crs_s_client is 'Client satellite'
/
comment on column crs_s_client.id is 'Identifier'
/
comment on column crs_s_client.h_id is 'Reference to hub'
/
comment on column crs_s_client.ldts is 'Load date'
/
comment on column crs_s_client.removed is 'Removed flag'
/
comment on column crs_s_client.digest is 'Row digest'
/
comment on column crs_s_client.reg_countrycode is 'Register country code'
/
comment on column crs_s_client.ogrn is 'OGRN'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-4 logicalFilePath:crs-1.0-VTBCRS-268-4 endDelimiter:/
create table crs_h_client_type(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_client_type_pk primary key (id),
    constraint crs_h_client_type_uk1 unique (key)
)
/
comment on table crs_h_client_type is 'Client type hub'
/
comment on column crs_h_client_type.id is 'Identifier'
/
comment on column crs_h_client_type.key is 'Key'
/
comment on column crs_h_client_type.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-5 logicalFilePath:crs-1.0-VTBCRS-268-5 endDelimiter:/
create index crs_h_client_type_i01 on crs_h_client_type (ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-6 logicalFilePath:crs-1.0-VTBCRS-268-6 endDelimiter:/
create table crs_s_client_type(
    id                 number not null,
    h_id               number not null,
    ldts               timestamp not null,
    removed            number(1) default 0 not null,
    digest             varchar2(100) not null,
    constraint crs_s_client_type_pk primary key (id),
    constraint crs_s_client_type_uk1 unique (ldts, h_id),
    constraint crs_s_client_type_fk01 foreign key (h_id) references crs_h_client_type(id)
)
/

--changeset emelnikov:crs-1.0-VTBCRS-268-7 logicalFilePath:crs-1.0-VTBCRS-268-7 endDelimiter:/
comment on table crs_s_client_type is 'Client type satellite'
/
comment on column crs_s_client_type.id is 'Identifier'
/
comment on column crs_s_client_type.h_id is 'Reference to hub'
/
comment on column crs_s_client_type.ldts is 'Load date'
/
comment on column crs_s_client_type.removed is 'Removed flag'
/
comment on column crs_s_client_type.digest is 'Row digest'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-8 logicalFilePath:crs-1.0-VTBCRS-268-8 endDelimiter:/
create table crs_l_client_type (
    id                 number not null,
    ldts               timestamp not null,
    removed            number(1) not null,
    client_id          number not null,
    client_type_id     number not null,
    constraint crs_l_client_type_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_type_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_type_fk02 foreign key(client_type_id) references crs_h_client_type(id)
)
/
comment on table crs_l_client_type is 'Client to client type link'
/
comment on column crs_l_client_type.id is 'Identifier'
/
comment on column crs_l_client_type.client_id is 'Reference to client'
/
comment on column crs_l_client_type.client_type_id is 'Reference to client type'
/
comment on column crs_l_client_type.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-9 logicalFilePath:crs-1.0-VTBCRS-268-9 endDelimiter:/
create index crs_l_client_type_i01 on crs_l_client_type(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_type_i02 on crs_l_client_type(client_type_id) compress 1 tablespace spoindx
/
create index crs_l_client_type_i03 on crs_l_client_type(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-10 logicalFilePath:crs-1.0-VTBCRS-268-10 endDelimiter:/
create table crs_h_client_group(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_client_group_pk primary key (id),
    constraint crs_h_client_group_uk1 unique (key)
)
/
comment on table crs_h_client_group is 'Client group hub'
/
comment on column crs_h_client_group.id is 'Identifier'
/
comment on column crs_h_client_group.key is 'Key'
/
comment on column crs_h_client_group.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-11 logicalFilePath:crs-1.0-VTBCRS-268-11 endDelimiter:/
create index crs_h_client_group_i01 on crs_h_client_group (ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-12 logicalFilePath:crs-1.0-VTBCRS-268-12 endDelimiter:/
create table crs_s_client_group(
    id                 number not null,
    h_id               number not null,
    ldts               timestamp not null,
    removed            number(1) default 0 not null,
    digest             varchar2(100) not null,
    vtb_daughter       number(1),
    reg_countrycode    varchar2(10),
    constraint crs_s_client_group_pk primary key (id),
    constraint crs_s_client_group_uk1 unique (ldts, h_id),
    constraint crs_s_client_group_fk01 foreign key (h_id) references crs_h_client_group(id)
)
/

--changeset emelnikov:crs-1.0-VTBCRS-268-13 logicalFilePath:crs-1.0-VTBCRS-268-13 endDelimiter:/
comment on table crs_s_client_group is 'Client group satellite'
/
comment on column crs_s_client_group.id is 'Identifier'
/
comment on column crs_s_client_group.h_id is 'Reference to hub'
/
comment on column crs_s_client_group.ldts is 'Load date'
/
comment on column crs_s_client_group.removed is 'Removed flag'
/
comment on column crs_s_client_group.digest is 'Row digest'
/
comment on column crs_s_client_group.vtb_daughter is 'VTB branch flag'
/
comment on column crs_s_client_group.reg_countrycode is 'Register country code'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-14 logicalFilePath:crs-1.0-VTBCRS-268-14 endDelimiter:/
create table crs_l_client_group (
    id                 number not null,
    ldts               timestamp not null,
    removed            number(1) not null,
    client_id          number not null,
    client_group_id    number not null,
    constraint crs_l_client_group_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_group_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_group_fk02 foreign key(client_group_id) references crs_h_client_group(id)
)
/
comment on table crs_l_client_group is 'Client to client group link'
/
comment on column crs_l_client_group.id is 'Identifier'
/
comment on column crs_l_client_group.client_id is 'Reference to client'
/
comment on column crs_l_client_group.client_group_id is 'Reference to client group'
/
comment on column crs_l_client_group.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-15 logicalFilePath:crs-1.0-VTBCRS-268-15 endDelimiter:/
create index crs_l_client_group_i01 on crs_l_client_group(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_group_i02 on crs_l_client_group(client_group_id) compress 1 tablespace spoindx
/
create index crs_l_client_group_i03 on crs_l_client_group(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-16 logicalFilePath:crs-1.0-VTBCRS-268-16 endDelimiter:/
create table crs_h_client_inn(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_client_inn_pk primary key (id),
    constraint crs_h_client_inn_uk1 unique (key)
)
/
comment on table crs_h_client_inn is 'Client INN hub'
/
comment on column crs_h_client_inn.id is 'Identifier'
/
comment on column crs_h_client_inn.key is 'Key'
/
comment on column crs_h_client_inn.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-17 logicalFilePath:crs-1.0-VTBCRS-268-17 endDelimiter:/
create index crs_h_client_inn_i01 on crs_h_client_inn (ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-18 logicalFilePath:crs-1.0-VTBCRS-268-18 endDelimiter:/
create table crs_s_client_inn(
    id                 number not null,
    h_id               number not null,
    ldts               timestamp not null,
    removed            number(1) default 0 not null,
    digest             varchar2(100) not null,
    tax_id             varchar2(32),
    tax_id_countrycode varchar2(10),
    constraint crs_s_client_inn_pk primary key (id),
    constraint crs_s_client_inn_uk1 unique (ldts, h_id),
    constraint crs_s_client_inn_fk01 foreign key (h_id) references crs_h_client_inn(id)
)
/

--changeset emelnikov:crs-1.0-VTBCRS-268-19 logicalFilePath:crs-1.0-VTBCRS-268-19 endDelimiter:/
comment on table crs_s_client_inn is 'Client INN satellite'
/
comment on column crs_s_client_inn.id is 'Identifier'
/
comment on column crs_s_client_inn.h_id is 'Reference to hub'
/
comment on column crs_s_client_inn.ldts is 'Load date'
/
comment on column crs_s_client_inn.removed is 'Removed flag'
/
comment on column crs_s_client_inn.digest is 'Row digest'
/
comment on column crs_s_client_inn.tax_id is 'Tax id'
/
comment on column crs_s_client_inn.tax_id_countrycode is 'Country code'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-20 logicalFilePath:crs-1.0-VTBCRS-268-20 endDelimiter:/
create table crs_l_client_inn (
    id                 number not null,
    ldts               timestamp not null,
    removed            number(1) not null,
    client_id          number not null,
    client_inn_id      number not null,
    constraint crs_l_client_inn_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_inn_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_inn_fk02 foreign key(client_inn_id) references crs_h_client_inn(id)
)
/
comment on table crs_l_client_inn is 'Client to client inn link'
/
comment on column crs_l_client_inn.id is 'Identifier'
/
comment on column crs_l_client_inn.client_id is 'Reference to client'
/
comment on column crs_l_client_inn.client_inn_id is 'Reference to client inn'
/
comment on column crs_l_client_inn.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-21 logicalFilePath:crs-1.0-VTBCRS-268-21 endDelimiter:/
create index crs_l_client_inn_i01 on crs_l_client_inn(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_inn_i02 on crs_l_client_inn(client_inn_id) compress 1 tablespace spoindx
/
create index crs_l_client_inn_i03 on crs_l_client_inn(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-22 logicalFilePath:crs-1.0-VTBCRS-268-22 endDelimiter:/
create table crs_l_client_full_name (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_id             number not null,
    localization_id       number not null,
    constraint crs_l_client_full_name_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_full_name_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_full_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_full_name is 'Client to multilang client full name link'
/
comment on column crs_l_client_full_name.id is 'Identifier'
/
comment on column crs_l_client_full_name.client_id is 'Reference to client'
/
comment on column crs_l_client_full_name.localization_id is 'Reference to client full name multilang'
/
comment on column crs_l_client_full_name.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-23 logicalFilePath:crs-1.0-VTBCRS-268-23 endDelimiter:/
create index crs_l_client_full_name_i01 on crs_l_client_full_name(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_full_name_i02 on crs_l_client_full_name(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_full_name_i03 on crs_l_client_full_name(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-24 logicalFilePath:crs-1.0-VTBCRS-268-24 endDelimiter:/
create table crs_l_client_name (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_id             number not null,
    localization_id       number not null,
    constraint crs_l_client_name_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_name_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_name is 'Client to multilang client name link'
/
comment on column crs_l_client_name.id is 'Identifier'
/
comment on column crs_l_client_name.client_id is 'Reference to client'
/
comment on column crs_l_client_name.localization_id is 'Reference to client name multilang'
/
comment on column crs_l_client_name.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-25 logicalFilePath:crs-1.0-VTBCRS-268-25 endDelimiter:/
create index crs_l_client_name_i01 on crs_l_client_name(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_name_i02 on crs_l_client_name(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_name_i03 on crs_l_client_name(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-26 logicalFilePath:crs-1.0-VTBCRS-268-26 endDelimiter:/
create table crs_l_client_opf (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_id             number not null,
    localization_id       number not null,
    constraint crs_l_client_opf_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_opf_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_opf_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_opf is 'Client to multilang client opf link'
/
comment on column crs_l_client_opf.id is 'Identifier'
/
comment on column crs_l_client_opf.client_id is 'Reference to client'
/
comment on column crs_l_client_opf.localization_id is 'Reference to client opf multilang'
/
comment on column crs_l_client_opf.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-27 logicalFilePath:crs-1.0-VTBCRS-268-27 endDelimiter:/
create index crs_l_client_opf_i01 on crs_l_client_opf(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_opf_i02 on crs_l_client_opf(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_opf_i03 on crs_l_client_opf(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-28 logicalFilePath:crs-1.0-VTBCRS-268-28 endDelimiter:/
create table crs_l_client_reg_country (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_id             number not null,
    localization_id       number not null,
    constraint crs_l_client_reg_country_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_reg_country_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_reg_country_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_reg_country is 'Client to multilang client reg country link'
/
comment on column crs_l_client_reg_country.id is 'Identifier'
/
comment on column crs_l_client_reg_country.client_id is 'Reference to client'
/
comment on column crs_l_client_reg_country.localization_id is 'Reference to client reg country multilang'
/
comment on column crs_l_client_reg_country.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-29 logicalFilePath:crs-1.0-VTBCRS-268-29 endDelimiter:/
create index crs_l_client_reg_country_i01 on crs_l_client_reg_country(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_reg_country_i02 on crs_l_client_reg_country(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_reg_country_i03 on crs_l_client_reg_country(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-30 logicalFilePath:crs-1.0-VTBCRS-268-30 endDelimiter:/
create table crs_l_client_category (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_id             number not null,
    localization_id       number not null,
    constraint crs_l_client_category_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_category_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_category_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_category is 'Client to multilang client category link'
/
comment on column crs_l_client_category.id is 'Identifier'
/
comment on column crs_l_client_category.client_id is 'Reference to client'
/
comment on column crs_l_client_category.localization_id is 'Reference to client category multilang'
/
comment on column crs_l_client_category.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-31 logicalFilePath:crs-1.0-VTBCRS-268-31 endDelimiter:/
create index crs_l_client_category_i01 on crs_l_client_category(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_category_i02 on crs_l_client_category(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_category_i03 on crs_l_client_category(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-32 logicalFilePath:crs-1.0-VTBCRS-268-32 endDelimiter:/
create table crs_l_client_segment (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_id             number not null,
    localization_id       number not null,
    constraint crs_l_client_segment_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_segment_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_segment_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_segment is 'Client to multilang client segment link'
/
comment on column crs_l_client_segment.id is 'Identifier'
/
comment on column crs_l_client_segment.client_id is 'Reference to client'
/
comment on column crs_l_client_segment.localization_id is 'Reference to client segment multilang'
/
comment on column crs_l_client_segment.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-33 logicalFilePath:crs-1.0-VTBCRS-268-33 endDelimiter:/
create index crs_l_client_segment_i01 on crs_l_client_segment(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_segment_i02 on crs_l_client_segment(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_segment_i03 on crs_l_client_segment(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-34 logicalFilePath:crs-1.0-VTBCRS-268-34 endDelimiter:/
create table crs_l_client_industry (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_id             number not null,
    localization_id       number not null,
    constraint crs_l_client_industry_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_industry_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_industry_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_industry is 'Client to multilang client industry link'
/
comment on column crs_l_client_industry.id is 'Identifier'
/
comment on column crs_l_client_industry.client_id is 'Reference to client'
/
comment on column crs_l_client_industry.localization_id is 'Reference to client industry multilang'
/
comment on column crs_l_client_industry.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-35 logicalFilePath:crs-1.0-VTBCRS-268-35 endDelimiter:/
create index crs_l_client_industry_i01 on crs_l_client_industry(client_id) compress 1 tablespace spoindx
/
create index crs_l_client_industry_i02 on crs_l_client_industry(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_industry_i03 on crs_l_client_industry(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-36 logicalFilePath:crs-1.0-VTBCRS-268-36 endDelimiter:/
create table crs_l_client_type_name (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_type_id        number not null,
    localization_id       number not null,
    constraint crs_l_client_type_name_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_type_name_fk01 foreign key(client_type_id) references crs_h_client_type(id),
    constraint crs_l_client_type_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_type_name is 'Client type to multilang client type name link'
/
comment on column crs_l_client_type_name.id is 'Identifier'
/
comment on column crs_l_client_type_name.client_type_id is 'Reference to client type'
/
comment on column crs_l_client_type_name.localization_id is 'Reference to client type name multilang'
/
comment on column crs_l_client_type_name.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-37 logicalFilePath:crs-1.0-VTBCRS-268-37 endDelimiter:/
create index crs_l_client_type_name_i01 on crs_l_client_type_name(client_type_id) compress 1 tablespace spoindx
/
create index crs_l_client_type_name_i02 on crs_l_client_type_name(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_type_name_i03 on crs_l_client_type_name(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-38 logicalFilePath:crs-1.0-VTBCRS-268-38 endDelimiter:/
create table crs_l_client_group_name (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_group_id       number not null,
    localization_id       number not null,
    constraint crs_l_client_group_name_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_group_name_fk01 foreign key(client_group_id) references crs_h_client_group(id),
    constraint crs_l_client_group_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_group_name is 'Client group to multilang client group name link'
/
comment on column crs_l_client_group_name.id is 'Identifier'
/
comment on column crs_l_client_group_name.client_group_id is 'Reference to client group'
/
comment on column crs_l_client_group_name.localization_id is 'Reference to client group multilang name'
/
comment on column crs_l_client_group_name.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-39 logicalFilePath:crs-1.0-VTBCRS-268-39 endDelimiter:/
create index crs_l_client_group_name_i01 on crs_l_client_group_name(client_group_id) compress 1 tablespace spoindx
/
create index crs_l_client_group_name_i02 on crs_l_client_group_name(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_group_name_i03 on crs_l_client_group_name(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-40 logicalFilePath:crs-1.0-VTBCRS-268-40 endDelimiter:/
create table crs_l_client_grp_fullname (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_group_id       number not null,
    localization_id       number not null,
    constraint crs_l_client_grp_fullname_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_grp_fullname_fk01 foreign key(client_group_id) references crs_h_client_group(id),
    constraint crs_l_client_grp_fullname_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_grp_fullname is 'Client group to multilang client group full name link'
/
comment on column crs_l_client_grp_fullname.id is 'Identifier'
/
comment on column crs_l_client_grp_fullname.client_group_id is 'Reference to client group'
/
comment on column crs_l_client_grp_fullname.localization_id is 'Reference to client group multilang full name'
/
comment on column crs_l_client_grp_fullname.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-41 logicalFilePath:crs-1.0-VTBCRS-268-41 endDelimiter:/
create index crs_l_client_grp_fullname_i01 on crs_l_client_grp_fullname(client_group_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_fullname_i02 on crs_l_client_grp_fullname(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_fullname_i03 on crs_l_client_grp_fullname(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-42 logicalFilePath:crs-1.0-VTBCRS-268-42 endDelimiter:/
create table crs_l_client_grp_dscrp (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_group_id       number not null,
    localization_id       number not null,
    constraint crs_l_client_grp_dscrp_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_grp_dscrp_fk01 foreign key(client_group_id) references crs_h_client_group(id),
    constraint crs_l_client_grp_dscrp_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_grp_dscrp is 'Client group to multilang client group description link'
/
comment on column crs_l_client_grp_dscrp.id is 'Identifier'
/
comment on column crs_l_client_grp_dscrp.client_group_id is 'Reference to client group'
/
comment on column crs_l_client_grp_dscrp.localization_id is 'Reference to client group multilang description'
/
comment on column crs_l_client_grp_dscrp.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-43 logicalFilePath:crs-1.0-VTBCRS-268-43 endDelimiter:/
create index crs_l_client_grp_dscrp_i01 on crs_l_client_grp_dscrp(client_group_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_dscrp_i02 on crs_l_client_grp_dscrp(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_dscrp_i03 on crs_l_client_grp_dscrp(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-44 logicalFilePath:crs-1.0-VTBCRS-268-44 endDelimiter:/
create table crs_l_client_grp_segment (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_group_id       number not null,
    localization_id       number not null,
    constraint crs_l_client_grp_segment_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_grp_segment_fk01 foreign key(client_group_id) references crs_h_client_group(id),
    constraint crs_l_client_grp_segment_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_grp_segment is 'Client group to multilang client group segment link'
/
comment on column crs_l_client_grp_segment.id is 'Identifier'
/
comment on column crs_l_client_grp_segment.client_group_id is 'Reference to client group'
/
comment on column crs_l_client_grp_segment.localization_id is 'Reference to client group multilang segment'
/
comment on column crs_l_client_grp_segment.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-45 logicalFilePath:crs-1.0-VTBCRS-268-45 endDelimiter:/
create index crs_l_client_grp_segment_i01 on crs_l_client_grp_segment(client_group_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_segment_i02 on crs_l_client_grp_segment(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_segment_i03 on crs_l_client_grp_segment(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-46 logicalFilePath:crs-1.0-VTBCRS-268-46 endDelimiter:/
create table crs_l_client_grp_industry (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_group_id       number not null,
    localization_id       number not null,
    constraint crs_l_client_grp_industry_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_grp_industry_fk01 foreign key(client_group_id) references crs_h_client_group(id),
    constraint crs_l_client_grp_industry_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_grp_industry is 'Client group to multilang client group industry link'
/
comment on column crs_l_client_grp_industry.id is 'Identifier'
/
comment on column crs_l_client_grp_industry.client_group_id is 'Reference to client group'
/
comment on column crs_l_client_grp_industry.localization_id is 'Reference to client group multilang industry'
/
comment on column crs_l_client_grp_industry.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-47 logicalFilePath:crs-1.0-VTBCRS-268-47 endDelimiter:/
create index crs_l_client_grp_industry_i01 on crs_l_client_grp_industry(client_group_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_industry_i02 on crs_l_client_grp_industry(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_industry_i03 on crs_l_client_grp_industry(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-48 logicalFilePath:crs-1.0-VTBCRS-268-48 endDelimiter:/
create table crs_l_client_grp_reg_cntr (
    id                    number not null,
    ldts                  timestamp not null,
    removed               number(1) not null,
    client_group_id       number not null,
    localization_id       number not null,
    constraint crs_l_client_grp_reg_cntr_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_grp_reg_cntr_fk01 foreign key(client_group_id) references crs_h_client_group(id),
    constraint crs_l_client_grp_reg_cntr_fk02 foreign key(localization_id) references crs_sys_h_localization(id)
)
/
comment on table crs_l_client_grp_reg_cntr is 'Client group to multilang client group reg country link'
/
comment on column crs_l_client_grp_reg_cntr.id is 'Identifier'
/
comment on column crs_l_client_grp_reg_cntr.client_group_id is 'Reference to client group'
/
comment on column crs_l_client_grp_reg_cntr.localization_id is 'Reference to client group multilang reg country'
/
comment on column crs_l_client_grp_reg_cntr.ldts is 'Load date'
/

--changeset emelnikov:crs-1.0-VTBCRS-268-49 logicalFilePath:crs-1.0-VTBCRS-268-49 endDelimiter:/
create index crs_l_client_grp_reg_cntr_i01 on crs_l_client_grp_reg_cntr(client_group_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_reg_cntr_i02 on crs_l_client_grp_reg_cntr(localization_id) compress 1 tablespace spoindx
/
create index crs_l_client_grp_reg_cntr_i03 on crs_l_client_grp_reg_cntr(ldts) tablespace spoindx
/

--changeset emelnikov:crs-1.0-VTBCRS-268-50 logicalFilePath:crs-1.0-VTBCRS-268-50 endDelimiter:/
create sequence crs_h_client_inn_seq
/
create sequence crs_s_client_inn_seq
/
create sequence crs_s_client_seq
/
create sequence crs_h_client_seq
/
create sequence crs_s_client_type_seq
/
create sequence crs_h_client_type_seq
/
create sequence crs_s_client_group_seq
/
create sequence crs_h_client_group_seq
/
create sequence crs_l_client_inn_seq
/
create sequence crs_l_client_type_seq
/
create sequence crs_l_client_group_seq
/
create sequence crs_l_client_full_name_seq
/
create sequence crs_l_client_name_seq
/
create sequence crs_l_client_opf_seq
/
create sequence crs_l_client_reg_country_seq
/
create sequence crs_l_client_category_seq
/
create sequence crs_l_client_segment_seq
/
create sequence crs_l_client_industry_seq
/
create sequence crs_l_client_type_name_seq
/
create sequence crs_l_client_group_name_seq
/
create sequence crs_l_client_grp_fullname_seq
/
create sequence crs_l_client_grp_dscrp_seq
/
create sequence crs_l_client_grp_segment_seq
/
create sequence crs_l_client_grp_industry_seq
/
create sequence crs_l_client_grp_reg_cntr_seq
/

--changeset emelnikov:crs-1.0-VTBCRS-268-51 logicalFilePath:crs-1.0-VTBCRS-269-51 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    -- entity CLIENT_TYPE
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CLIENT_TYPE', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Тип клиента',
            'Client type',
            'null',
            null,
            1,
            0
    );

    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval,
            v_entity_type_id,
            v_h_entity_id,
            v_ldts,
            0);

    -- multilang attribute NAME
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT_TYPE#NAME', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_CLIENT_TYPE_NAME',
            null,
            'Наименование',
            'Name',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset emelnikov:crs-1.0-VTBCRS-268-52 logicalFilePath:crs-1.0-VTBCRS-269-52 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    -- entity GROUP
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CLIENT_GROUP', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Группа клиента',
            'Client group',
            'null',
            null,
            1,
            0
    );

    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval,
            v_entity_type_id,
            v_h_entity_id,
            v_ldts,
            0);

    -- multilang attribute NAME
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT_GROUP#NAME', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_CLIENT_GROUP_NAME',
            null,
            'Наименование',
            'Name',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute FULL NAME
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT_GROUP#FULL_NAME', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_CLIENT_GRP_FULLNAME',
            null,
            'Полное наименование',
            'Full name',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute DESCRIPTION
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT_GROUP#DESCRIPTION', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_CLIENT_GRP_DSCRP',
            null,
            'Описание',
            'Description',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute SEGMENT
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT_GROUP#SEGMENT', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            1,
            'CRS_L_CLIENT_GRP_SEGMENT',
            null,
            'Сегмент ',
            'Segment',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute INDUSTRY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT_GROUP#INDUSTRY', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            1,
            'CRS_L_CLIENT_GRP_INDUSTRY',
            null,
            'Отрасль ',
            'Industry',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute REG_COUNTRY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT_GROUP#REG_COUNTRY', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_CLIENT_GRP_REG_CNTR',
            null,
            'Страна регистрации',
            'Reg country',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT_GROUP';

    -- intable attribute VTB_DAUGHTER
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT_GROUP#VTB_DAUGHTER',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Дочерняя структура ВТБ',
            'VTB branch',
            1,
            null,
            0,
            'NUMBER',
            'VTB_DAUGHTER');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- intable attribute REG_COUNTRYCODE
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT_GROUP#REG_COUNTRYCODE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Код страны регистрации',
            'Reg country code',
            1,
            null,
            0,
            'STRING',
            'REG_COUNTRYCODE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset emelnikov:crs-1.0-VTBCRS-268-53 logicalFilePath:crs-1.0-VTBCRS-269-53 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    -- entity CLIENT_INN
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CLIENT_INN', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'ИНН клиента',
            'Client INN',
            'null',
            null,
            1,
            0
    );

    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval,
            v_entity_type_id,
            v_h_entity_id,
            v_ldts,
            0);

    -- intable attribute TAX_ID
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT_INN#TAX_ID',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Налоговый идентификатор',
            'Tax identifier',
            1,
            null,
            0,
            'STRING',
            'TAX_ID');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- intable attribute TAX_ID_COUNTRYCODE
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT_INN#TAX_ID_COUNTRYCODE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Страна налогового идентификатора',
            'Country code',
            1,
            null,
            0,
            'STRING',
            'TAX_ID_COUNTRYCODE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset emelnikov:crs-1.0-VTBCRS-268-54 logicalFilePath:crs-1.0-VTBCRS-269-54 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    -- entity CLIENT
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CLIENT', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Клиент',
            'Client',
            'null',
            null,
            1,
            0
    );

    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval,
            v_entity_type_id,
            v_h_entity_id,
            v_ldts,
            0);

    -- multilang attribute FULL_NAME
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT#FULL_NAME', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_CLIENT_FULL_NAME',
            null,
            'Полное наименование',
            'Full name',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute NAME
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT#NAME', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_CLIENT_NAME',
            null,
            'Наименование',
            'Name',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute OPF
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT#OPF', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            1,
            'CRS_L_CLIENT_OPF',
            null,
            'ОПФ',
            'OPF',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute REG_COUNTRY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT#REG_COUNTRY', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            'CRS_L_CLIENT_REG_COUNTRY',
            null,
            'Страна регистрации',
            'Reg country',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute CATEGORY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT#CATEGORY', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            1,
            'CRS_L_CLIENT_CATEGORY',
            null,
            'Категория клиента',
            'Client category',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute SEGMENT
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT#SEGMENT', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            1,
            'CRS_L_CLIENT_SEGMENT',
            null,
            'Сегмент ',
            'Segment',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- multilang attribute INDUSTRY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT#INDUSTRY', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            1,
            'CRS_L_CLIENT_INDUSTRY',
            null,
            'Отрасль ',
            'Industry',
            0,
            null,
            0,
            'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- intable attribute REG_COUNTRYCODE
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#REG_COUNTRYCODE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Код страны регистрации',
            'Reg country code',
            1,
            null,
            0,
            'STRING',
            'REG_COUNTRYCODE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- intable attribute OGRN
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#OGRN',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'ОГРН',
            'OGRN',
            1,
            null,
            0,
            'STRING',
            'OGRN');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- link attribute client to client_type
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#CLIENT_TYPE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CLIENT_TYPE',
            'CLIENT_TYPE#NAME',
            'Тип',
            'Type',
            1,
            'CLIENT_TYPE',
            0,
            'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- link attribute client to group
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#CLIENT_GROUP',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CLIENT_GROUP',
            'CLIENT_GROUP#NAME',
            'Группа клиента',
            'Client group',
            1,
            'CLIENT_GROUP',
            0,
            'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    -- link attribute client to client_inn
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval, 'CLIENT#CLIENT_INN', v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CLIENT_INN',
            'CLIENT_INN#TAX_ID',
            'ИНН клиента',
            'Client INN',
            1,
            'CLIENT_INN',
            0,
            'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset emelnikov:crs-1.0-VTBCRS-268-drops-seq-ind logicalFilePath:crs-1.0-VTBCRS-268-drops-seq-ind endDelimiter:/
drop sequence crs_l_contractor_content_seq
/
drop sequence crs_l_contractor_type_seq
/
drop sequence crs_s_contractor_seq
/
drop sequence crs_h_contractor_seq
/
drop sequence crs_s_contractor_type_seq
/
drop sequence crs_h_contractor_type_seq
/
drop index crs_l_contractor_content_i01
/
drop index crs_l_contractor_content_i02
/
drop index crs_l_contractor_content_i03
/
drop index crs_l_contractor_type_i01
/
drop index crs_l_contractor_type_i02
/
drop index crs_l_contractor_type_i03
/
drop index crs_h_contractor_i01
/
drop index crs_h_contractor_type_i01
/
drop index crs_l_contractor_type_name_i01
/
drop index crs_l_contractor_type_name_i02
/
drop index crs_l_contractor_type_name_i03
/
drop sequence crs_l_contractor_type_name_seq
/

--changeset emelnikov:crs-1.0-VTBCRS-268-drops-entities logicalFilePath:crs-1.0-VTBCRS-268-drops-entities endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
begin
    --delete client type meta
    for r in (select localization_id, id from crs_l_contractor_type_name) loop
        execute immediate 'delete from crs_l_contractor_type_name where id = '||r.id;
        execute immediate 'delete from crs_sys_s_localization where h_id = '||r.localization_id;
        execute immediate 'delete from crs_sys_h_localization where id = '||r.localization_id;
    end loop;

    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR_TYPE';

    select id into v_h_attribute_id from crs_sys_h_attribute where key = 'CONTRACTOR_TYPE#NAME';

    delete from crs_sys_l_entity_attribute where attribute_id = v_h_attribute_id;
    delete from crs_sys_s_attribute where attribute_key like '%CONTRACTOR_TYPE%';
    delete from crs_sys_s_attribute where h_id = v_h_attribute_id;
    delete from crs_sys_h_attribute where id = v_h_attribute_id;

    delete from crs_sys_l_entity_type where entity_id = v_h_entity_id;
    delete from crs_sys_s_entity where h_id = v_h_entity_id;
    delete from crs_sys_h_entity where id = v_h_entity_id;

    --delete client meta
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CONTRACTOR';

    for r in (select id from crs_sys_h_attribute where key in ( 'CONTRACTOR#CONTRACTOR_TYPE_ID',
                                                                'CONTRACTOR#SHORT_NAME',
                                                                'CONTRACTOR#FULL_NAME',
                                                                'CONTRACTOR#WORKPHONE',
                                                                'CONTRACTOR#FAX',
                                                                'CONTRACTOR#INN',
                                                                'CONTRACTOR#KPP',
                                                                'CONTRACTOR#CLIENT_CATEGORY',
                                                                'CONTRACTOR#DIVISION',
                                                                'CONTRACTOR#REGISTRATION_DATE',
                                                                'CONTRACTOR#TYPE',
                                                                'CONTRACTOR#DEPARTMENT',
                                                                'CONTRACTOR#REG_DIRECTION',
                                                                'CONTRACTOR#BRANCH',
                                                                'CONTRACTOR#DESCRIPTION',
                                                                'CONTRACTOR#AFC')) loop
        execute immediate 'delete from crs_sys_l_entity_attribute where attribute_id = '||r.id;
        execute immediate 'delete from crs_sys_s_attribute where h_id = '||r.id;
        execute immediate 'delete from crs_sys_h_attribute where id = '||r.id;
    end loop;

    delete from crs_sys_l_entity_type where entity_id = v_h_entity_id;
    delete from crs_sys_s_entity where h_id = v_h_entity_id;
    delete from crs_sys_h_entity where id = v_h_entity_id;

    commit;
end;
/

--changeset emelnikov:crs-1.0-VTBCRS-268-drops-tables logicalFilePath:crs-1.0-VTBCRS-268-drops-tables endDelimiter:/
drop table crs_l_contractor_type
/
drop table crs_l_contractor_content
/
drop table crs_s_contractor_type
/
drop table crs_l_contractor_type_name
/
drop table crs_h_contractor_type
/
drop table crs_s_contractor
/
drop table crs_h_contractor
/

--changeset akirilchev:crs-1.0-VTBCRS-262-login-upper logicalFilePath:crs-1.0-VTBCRS-262-login-upper endDelimiter:/
update crs_h_user
   set key = upper(trim(key))
/
alter table crs_h_user add constraint crs_h_user_ck01 check (key = upper(trim(key)))
/

--changeset akirilchev:crs-1.0-VTBCRS-84 logicalFilePath:crs-1.0-VTBCRS-84 endDelimiter:/
alter table crs_sys_s_storage modify ldts timestamp
/

--changeset pmasalov:crs-1.0-VTBCRS-236 logicalFilePath:crs-1.0-VTBCRS-236 endDelimiter:/
alter table crs_sys_s_entity add key_name_en varchar2(4000) default 'Key' not null
/
alter table crs_sys_s_entity add key_name_ru varchar2(4000) default 'Ключ' not null
/
comment on column crs_sys_s_entity.key_name_en is 'En name for entity KEY attribute'
/
comment on column crs_sys_s_entity.key_name_ru is 'Ru name for entity KEY attribute'
/

--changeset pmasalov:crs-1.0-VTBCRS-236-2 logicalFilePath:crs-1.0-VTBCRS-236-2 endDelimiter:/
update crs_sys_s_entity
   set key_name_ru = 'Логин', key_name_en = 'Login'
 where h_id = (select id from crs_sys_h_entity where key = 'USER')
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-334 logicalFilePath:crs-1.0-VTBCRS-334 endDelimiter:/
begin
    for r in (select index_name
                from user_indexes
               where index_name in ('CRS_H_CALC_I01',
                                    'CRS_H_CALC_FORMULA_I01',
                                    'CRS_H_CALC_FORMULA_RESULT_I01',
                                    'CRS_H_CALC_MODEL_I01',
                                    'CRS_H_CLIENT_I01',
                                    'CRS_H_CLIENT_GROUP_I01',
                                    'CRS_H_CLIENT_INN_I01',
                                    'CRS_H_CLIENT_TYPE_I01',
                                    'CRS_H_FORM_TEMPLATE_I01',
                                    'CRS_H_USER_I01'))
    loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/
--changeset pmasalov:crs-1.0-VTBCRS-334-2 logicalFilePath:crs-1.0-VTBCRS-334-2 endDelimiter:/
begin
    for r in (select index_name
                from user_indexes
               where index_name in ('CRS_L_CALC_I01',
                                    'CRS_L_CALC_FORMULA_I03',
                                    'CRS_L_CALC_MODEL_I01',
                                    'CRS_L_CALC_MODEL_CLASSFR_I03',
                                    'CRS_L_CALC_MODEL_FORM_I01',
                                    'CRS_L_CALC_MODEL_FORMULA_I03',
                                    'CRS_L_CALC_USER_I03',
                                    'CRS_L_CLIENT_CATEGORY_I03',
                                    'CRS_L_CLIENT_FULL_NAME_I03',
                                    'CRS_L_CLIENT_GROUP_I03',
                                    'CRS_L_CLIENT_GROUP_NAME_I03',
                                    'CRS_L_CLIENT_GRP_DSCRP_I03',
                                    'CRS_L_CLIENT_GRP_FULLNAME_I03',
                                    'CRS_L_CLIENT_GRP_INDUSTRY_I03',
                                    'CRS_L_CLIENT_GRP_REG_CNTR_I03',
                                    'CRS_L_CLIENT_GRP_SEGMENT_I03',
                                    'CRS_L_CLIENT_INDUSTRY_I03',
                                    'CRS_L_CLIENT_INN_I03',
                                    'CRS_L_CLIENT_NAME_I03',
                                    'CRS_L_CLIENT_OPF_I03',
                                    'CRS_L_CLIENT_REG_COUNTRY_I03',
                                    'CRS_L_CLIENT_SEGMENT_I03',
                                    'CRS_L_CLIENT_TYPE_I03',
                                    'CRS_L_CLIENT_TYPE_NAME_I03',
                                    'CRS_L_FORM_TEMPLATE_DATA_I03',
                                    'CRS_L_FORM_TEMPLATE_NAME_I03'))
    loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/
--changeset pmasalov:crs-1.0-VTBCRS-334-3 logicalFilePath:crs-1.0-VTBCRS-334-3 endDelimiter:/
begin
    for r in (select index_name
                from user_indexes
               where index_name in ('CRS_SYS_H_ATTRIBUTE_I01',
                                    'CRS_SYS_H_ENTITY_I01',
                                    'CRS_SYS_H_ENTITY_TYPE_I01',
                                    'CRS_SYS_H_LOCALIZATION_I01',
                                    'CRS_SYS_H_STORAGE_I01',
                                    'CRS_SYS_L_ENTITY_ATTR_I03',
                                    'CRS_SYS_L_ENTITY_TYPE_I03'))
    loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/
--changeset pmasalov:crs-1.0-VTBCRS-334-4 logicalFilePath:crs-1.0-VTBCRS-334-4 endDelimiter:/
declare
    procedure reindex(p_table varchar2, p_index varchar2, p_column varchar2) is
        begin
            for r in (select index_name from user_indexes where index_name = upper(p_index)) loop
                execute immediate 'drop index ' || r.index_name;
            end loop;
            if p_column = 'LOCALIZATION_ID' then
                execute immediate 'create index ' || p_index || ' on ' || p_table || ' (' || p_column || ') compress 1 tablespace spoindx';
            else
                execute immediate 'create index ' || p_index || ' on ' || p_table || ' (' || p_column || ',ldts) compress 1 tablespace spoindx';
            end if;
        end;
begin
    reindex('CRS_L_CALC', 'CRS_L_CALC_I02', 'ORIGIN_CALC_ID');
    reindex('CRS_L_CALC', 'CRS_L_CALC_I03', 'COPIED_CALC_ID');
    reindex('CRS_L_CALC_FORMULA', 'CRS_L_CALC_FORMULA_I01', 'FORMULA_ID');
    reindex('CRS_L_CALC_FORMULA', 'CRS_L_CALC_FORMULA_I02', 'FORMULA_PARENT_ID');
    reindex('CRS_L_CALC_FORMULA_RESULT', 'CRS_L_CALC_FORMULA_RESULT_I01', 'CALC_ID');
    reindex('CRS_L_CALC_FORMULA_RESULT', 'CRS_L_CALC_FORMULA_RESULT_I02', 'FORMULA_ID');
    reindex('CRS_L_CALC_FORMULA_RESULT', 'CRS_L_CALC_FORMULA_RESULT_I03', 'CALC_FORMULA_RESULT_ID');
    reindex('CRS_L_CALC_MODEL', 'CRS_L_CALC_MODEL_I02', 'CALC_ID');
    reindex('CRS_L_CALC_MODEL', 'CRS_L_CALC_MODEL_I03', 'CALC_MODEL_ID');
    reindex('CRS_L_CALC_MODEL_CLASSFR', 'CRS_L_CALC_MODEL_CLASSFR_I01', 'CALC_MODEL_ID');
    reindex('CRS_L_CALC_MODEL_CLASSFR', 'CRS_L_CALC_MODEL_CLASSFR_I02', 'ENTITY_ID');
    reindex('CRS_L_FORM_TEMPLATE_NAME', 'CRS_L_FORM_TEMPLATE_NAME_I01', 'FORM_TEMPLATE_ID');
    reindex('CRS_L_FORM_TEMPLATE_NAME', 'CRS_L_FORM_TEMPLATE_NAME_I02', 'FORM_TEMPLATE_NAME_ID');
    reindex('CRS_L_FORM_TEMPLATE_DATA', 'CRS_L_FORM_TEMPLATE_DATA_I01', 'FORM_TEMPLATE_ID');
    reindex('CRS_L_FORM_TEMPLATE_DATA', 'CRS_L_FORM_TEMPLATE_DATA_I02', 'FORM_TEMPLATE_DATA_ID');
    reindex('CRS_L_CLIENT_TYPE_NAME', 'CRS_L_CLIENT_TYPE_NAME_I01', 'CLIENT_TYPE_ID');
    reindex('CRS_L_CLIENT_TYPE_NAME', 'CRS_L_CLIENT_TYPE_NAME_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_TYPE', 'CRS_L_CLIENT_TYPE_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_TYPE', 'CRS_L_CLIENT_TYPE_I02', 'CLIENT_TYPE_ID');
    reindex('CRS_L_CLIENT_SEGMENT', 'CRS_L_CLIENT_SEGMENT_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_SEGMENT', 'CRS_L_CLIENT_SEGMENT_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_REG_COUNTRY', 'CRS_L_CLIENT_REG_COUNTRY_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_REG_COUNTRY', 'CRS_L_CLIENT_REG_COUNTRY_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_OPF', 'CRS_L_CLIENT_OPF_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_OPF', 'CRS_L_CLIENT_OPF_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_NAME', 'CRS_L_CLIENT_NAME_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_NAME', 'CRS_L_CLIENT_NAME_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_INN', 'CRS_L_CLIENT_INN_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_INN', 'CRS_L_CLIENT_INN_I02', 'CLIENT_INN_ID');
    reindex('CRS_L_CLIENT_INDUSTRY', 'CRS_L_CLIENT_INDUSTRY_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_INDUSTRY', 'CRS_L_CLIENT_INDUSTRY_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_GRP_SEGMENT', 'CRS_L_CLIENT_GRP_SEGMENT_I01', 'CLIENT_GROUP_ID');
    reindex('CRS_L_CLIENT_GRP_SEGMENT', 'CRS_L_CLIENT_GRP_SEGMENT_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_GRP_REG_CNTR', 'CRS_L_CLIENT_GRP_REG_CNTR_I01', 'CLIENT_GROUP_ID');
    reindex('CRS_L_CLIENT_GRP_REG_CNTR', 'CRS_L_CLIENT_GRP_REG_CNTR_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_GRP_INDUSTRY', 'CRS_L_CLIENT_GRP_INDUSTRY_I01', 'CLIENT_GROUP_ID');
    reindex('CRS_L_CLIENT_GRP_INDUSTRY', 'CRS_L_CLIENT_GRP_INDUSTRY_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_GRP_FULLNAME', 'CRS_L_CLIENT_GRP_FULLNAME_I01', 'CLIENT_GROUP_ID');
    reindex('CRS_L_CLIENT_GRP_FULLNAME', 'CRS_L_CLIENT_GRP_FULLNAME_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_GRP_DSCRP', 'CRS_L_CLIENT_GRP_DSCRP_I01', 'CLIENT_GROUP_ID');
    reindex('CRS_L_CLIENT_GRP_DSCRP', 'CRS_L_CLIENT_GRP_DSCRP_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_GROUP_NAME', 'CRS_L_CLIENT_GROUP_NAME_I01', 'CLIENT_GROUP_ID');
    reindex('CRS_L_CLIENT_GROUP_NAME', 'CRS_L_CLIENT_GROUP_NAME_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_GROUP', 'CRS_L_CLIENT_GROUP_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_GROUP', 'CRS_L_CLIENT_GROUP_I02', 'CLIENT_GROUP_ID');
    reindex('CRS_L_CLIENT_FULL_NAME', 'CRS_L_CLIENT_FULL_NAME_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_FULL_NAME', 'CRS_L_CLIENT_FULL_NAME_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CLIENT_CATEGORY', 'CRS_L_CLIENT_CATEGORY_I01', 'CLIENT_ID');
    reindex('CRS_L_CLIENT_CATEGORY', 'CRS_L_CLIENT_CATEGORY_I02', 'LOCALIZATION_ID');
    reindex('CRS_L_CALC_USER', 'CRS_L_CALC_USER_I01', 'CALC_ID');
    reindex('CRS_L_CALC_USER', 'CRS_L_CALC_USER_I02', 'USER_ID');
    reindex('CRS_L_CALC_MODEL_FORMULA', 'CRS_L_CALC_MODEL_FORMULA_I01', 'CALC_MODEL_ID');
    reindex('CRS_L_CALC_MODEL_FORMULA', 'CRS_L_CALC_MODEL_FORMULA_I02', 'CALC_FORMULA_ID');
    reindex('CRS_L_CALC_MODEL_FORM', 'CRS_L_CALC_MODEL_FORM_I02', 'CALC_MODEL_ID');
    reindex('CRS_L_CALC_MODEL_FORM', 'CRS_L_CALC_MODEL_FORM_I03', 'ENTITY_ID');
    reindex('CRS_SYS_L_ENTITY_ATTRIBUTE', 'CRS_SYS_L_ENTITY_ATTR_I01', 'ENTITY_ID');
    reindex('CRS_SYS_L_ENTITY_ATTRIBUTE', 'CRS_SYS_L_ENTITY_ATTR_I02', 'ATTRIBUTE_ID');
    reindex('CRS_SYS_L_REF_ATTRIBUTE', 'CRS_SYS_L_REF_ATTRIBUTE_I01', 'ATTRIBUTE_ID');
    reindex('CRS_SYS_L_REF_ATTRIBUTE', 'CRS_SYS_L_REF_ATTRIBUTE_I02', 'ATTR_ATTRIBUTE_ID');
    reindex('CRS_SYS_L_ENTITY_TYPE', 'CRS_SYS_L_ENTITY_TYPE_I01', 'TYPE_ID');
    reindex('CRS_SYS_L_ENTITY_TYPE', 'CRS_SYS_L_ENTITY_TYPE_I02', 'ENTITY_ID');
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-334-5 logicalFilePath:crs-1.0-VTBCRS-334-5 endDelimiter:/
declare
    procedure reindex(p_table varchar2, p_ukey varchar2) is
        begin
            for r in (select constraint_name from user_constraints c where c.constraint_name = upper(p_ukey)) loop
                execute immediate 'alter table ' || p_table || ' drop constraint ' || p_ukey || ' cascade';
            end loop;
            for r in (select index_name from user_indexes where index_name = upper(p_ukey)) loop
                execute immediate 'drop index ' || r.index_name;
            end loop;
            execute immediate 'alter table '|| p_table ||' add constraint '|| p_ukey ||' unique (H_ID,LDTS) using index compress 1 tablespace spoindx';
        end;
begin
    reindex('CRS_SYS_S_ATTRIBUTE','CRS_SYS_S_ATTRIBUTE_UK01');
    reindex('CRS_S_USER','CRS_S_USER_UK1');
    reindex('CRS_S_FORM_TEMPLATE','CRS_S_FORM_TEMPLATE_UK1');
    reindex('CRS_S_CLIENT_TYPE','CRS_S_CLIENT_TYPE_UK1');
    reindex('CRS_S_CLIENT_INN','CRS_S_CLIENT_INN_UK1');
    reindex('CRS_S_CLIENT_GROUP','CRS_S_CLIENT_GROUP_UK1');
    reindex('CRS_S_CLIENT','CRS_S_CLIENT_UK1');
    reindex('CRS_S_CALC_MODEL','CRS_S_CALC_MODEL_UK01');
    reindex('CRS_S_CALC_FORMULA_RESULT','CRS_S_CALC_FORMULA_RESULT_UK01');
    reindex('CRS_S_CALC_FORMULA_DESC','CRS_S_CALC_FORMULA_DESC_UK01');
    reindex('CRS_S_CALC_FORMULA','CRS_S_CALC_FORMULA_UK01');
    reindex('CRS_S_CALC','CRS_S_CALC_UK01');
    reindex('CRS_SYS_S_STORAGE_DESC','CRS_SYS_S_STORAGE_DESC_UK01');
    reindex('CRS_SYS_S_STORAGE','CRS_SYS_S_STORAGE_UK01');
    reindex('CRS_SYS_S_LOCALIZATION','CRS_SYS_S_LOCALIZATION_UK01');
    reindex('CRS_SYS_S_ENTITY','CRS_SYS_S_ENTITY_UK01');
end;
/

--changeset imatushak:crs-1.0-VTBCRS-263 logicalFilePath:crs-1.0-VTBCRS-263 endDelimiter:/
update crs_sys_s_attribute s
   set s.view_order = 1
 where exists (select 1
                 from crs_sys_h_attribute h
                where h.key = 'USER#NAME'
                  and h.id = s.h_id)
/
update crs_sys_s_attribute s
   set s.view_order = 2
 where exists (select 1
                 from crs_sys_h_attribute h
                where h.key = 'USER#PATRONYMIC'
                  and h.id = s.h_id)
/

--changeset emelnikov:crs-1.0-VTBCRS-288 logicalFilePath:crs-1.0-VTBCRS-288 endDelimiter:/
alter table crs_s_calc_formula_result add output clob
/
comment on column crs_s_calc_formula_result.output is 'Output formula data'
/

--changeset emelnikov:crs-1.0-VTBCRS-288-1 logicalFilePath:crs-1.0-VTBCRS-288-1 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_FORMULA_RESULT';

    -- intable attribute OUTPUT
    insert into crs_sys_h_attribute (id, key, ldts) values (crs_sys_h_attribute_seq.nextval,'CALC_FORMULA_RESULT#OUTPUT',v_ldts) returning  id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available, entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Вывод',
            'Output',
            1,
            null,
            0,
            'TEXT',
            'OUTPUT');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/
--changeset pmasalov:crs-1.0-VTBCRS-256 logicalFilePath:crs-1.0-VTBCRS-256 endDelimiter:/
create or replace type crs_pair_nts force as object (n number, ts timestamp)
/
create or replace type crs_pair_nts_a as table of crs_pair_nts
/

--changeset pmasalov:crs-1.0-VTBCRS-100 logicalFilePath:crs-1.0-VTBCRS-100 endDelimiter:/
update crs_sys_s_entity s
   set s.hierarchical = 0
 where s.h_id = (select id
                   from crs_sys_h_entity h
                  where h.key = 'CLIENT')
/
commit
/

--changeset svaliev:crs-1.0-VTBCRS-268-constraint-on-removed logicalFilePath:crs-1.0-VTBCRS-268-constraint-on-removed endDelimiter:/
begin
    for t in (select ut.table_name tn
                from user_tables ut
               where ut.table_name like 'CRS_S_CLIENT%'
                  or ut.table_name like 'CRS_L_CLIENT%')
    loop
        execute immediate 'alter table ' || t.tn || ' modify removed default 0';
        execute immediate 'alter table ' || t.tn || ' add constraint ' || t.tn || '_CK01 check (removed in (0, 1))';
        execute immediate 'comment on column ' || t.tn || '.removed is ''Removed flag''';
    end loop;
end;
/

--changeset imatushak:crs-1.0-VTBCRS-315 logicalFilePath:crs-1.0-VTBCRS-315 endDelimiter:/
update crs_sys_s_entity s
   set s.hierarchical = 0
 where s.h_id = (select id
                   from crs_sys_h_entity h
                  where h.key = 'CLIENT_GROUP')
/

update crs_sys_s_entity s
   set s.hierarchical = 0
 where s.h_id = (select id
                   from crs_sys_h_entity h
                  where h.key = 'CLIENT_INN')
/

update crs_sys_s_entity s
   set s.hierarchical = 0
 where s.h_id = (select id
                   from crs_sys_h_entity h
                  where h.key = 'CLIENT_TYPE')
/

--changeset pmasalov:crs-1.0-VTBCRS-322 logicalFilePath:crs-1.0-VTBCRS-322 endDelimiter:/
update crs_sys_h_attribute
   set key = 'FORM_TEMPLATE#BOOK'
 where key = 'FORM_TEMPLATE#OBJECT'
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-322-2 logicalFilePath:crs-1.0-VTBCRS-322-2 endDelimiter:/
alter table crs_s_form_template add draft number(1) default 1 not null
/
alter table crs_s_form_template add mapper clob
/
comment on column crs_s_form_template.draft is 'Draft template'
/
comment on column crs_s_form_template.mapper is 'JSON representation of Mapper object'
/

--changeset pmasalov:crs-1.0-VTBCRS-322-3 logicalFilePath:crs-1.0-VTBCRS-322-3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('1111-JAN-01 00:00:00', 'YYYY-MON-DD HH24:MI:SS');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FORM_TEMPLATE';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FORM_TEMPLATE#DRAFT',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            null,
            'Черновик шаблона',
            'Draft template',
            1,
            null,
            0,
            'NUMBER',
            'DRAFT');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-322-4 logicalFilePath:crs-1.0-VTBCRS-322-4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('1111-JAN-01 00:00:00', 'YYYY-MON-DD HH24:MI:SS');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FORM_TEMPLATE';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FORM_TEMPLATE#MAPPER',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'JSON представление обьекта Mapper',
            'JSON representation of Mapper object',
            1,
            null,
            0,
            'TEXT',
            'MAPPER');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-322-5 logicalFilePath:crs-1.0-VTBCRS-322-5 endDelimiter:/
update crs_sys_h_attribute
   set key = 'FORM_TEMPLATE#MAPPER_CONFIG'
 where key = 'FORM_TEMPLATE#MAPPER'
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-322-6 logicalFilePath:crs-1.0-VTBCRS-322-6 endDelimiter:/
update crs_sys_s_attribute
  set type = 'BOOLEAN'
 where h_id = (select id
                 from crs_sys_h_attribute
                where key = 'FORM_TEMPLATE#DRAFT')
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-322-7 logicalFilePath:crs-1.0-VTBCRS-322-7 endDelimiter:/
alter table crs_l_form_template_data rename column form_template_data_id to storage_id
/
drop index crs_l_form_template_data_i02
/
create index crs_l_form_template_data_i02 on crs_l_form_template_data (storage_id, ldts)
tablespace spoindx compress 1
/
alter table crs_l_form_template_data drop constraint crs_l_form_template_data_fk02
/
alter table crs_l_form_template_data
  add constraint crs_l_form_template_data_fk02 foreign key (storage_id)
references crs_sys_h_storage (id)
/

--changeset pmasalov:crs-1.0-VTBCRS-322-8 logicalFilePath:crs-1.0-VTBCRS-322-8 endDelimiter:/
alter table crs_l_form_template_name rename column form_template_name_id to localization_id
/
drop index crs_l_form_template_name_i02
/
create index crs_l_form_template_name_i02 on crs_l_form_template_name (localization_id, ldts)
tablespace spoindx compress 1
/
alter table crs_l_form_template_name drop constraint crs_l_form_template_name_fk02
/
alter table crs_l_form_template_name
  add constraint crs_l_form_template_name_fk02 foreign key (localization_id)
references crs_sys_h_localization (id)
/

--changeset pmasalov:crs-1.0-VTBCRS-322-9 logicalFilePath:crs-1.0-VTBCRS-322-9 endDelimiter:/
update crs_sys_s_entity s
   set s.hierarchical = 0
 where s.h_id = (select id
                   from crs_sys_h_entity h
                  where h.key = 'FORM_TEMPLATE')
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-379 logicalFilePath:crs-1.0-VTBCRS-379 endDelimiter:/
create table crs_l_calc_model_f_tmplt
(
    id            number not null,
    ldts          timestamp(6) not null,
    removed       number(1) not null,
    calc_model_id number not null,
    form_template_id     number not null
)/
comment on table crs_l_calc_model_f_tmplt is 'Model to classifier link'
/
comment on column crs_l_calc_model_f_tmplt.id is 'Identifier'
/
comment on column crs_l_calc_model_f_tmplt.ldts is 'Load date'
/
comment on column crs_l_calc_model_f_tmplt.calc_model_id is 'Reference to model'
/
comment on column crs_l_calc_model_f_tmplt.form_template_id is 'Reference to form template'
/

create index crs_l_calc_model_f_tmplt_i01 on crs_l_calc_model_f_tmplt (calc_model_id, ldts)
tablespace spoindx compress 1
/
create index crs_l_calc_model_f_tmplt_i02 on crs_l_calc_model_f_tmplt (form_template_id, ldts)
tablespace spoindx compress 1
/

alter table crs_l_calc_model_f_tmplt
      add constraint crs_l_calc_model_f_tmplt_pk primary key (id)
      using index
      tablespace spoindx
/
alter table crs_l_calc_model_f_tmplt
  add constraint crs_l_calc_model_f_tmplt_fk01 foreign key (calc_model_id)
references crs_h_calc_model (id)
/
alter table crs_l_calc_model_f_tmplt
  add constraint crs_l_calc_model_f_tmplt_fk02 foreign key (form_template_id)
references crs_h_form_template (id)
/

alter table crs_l_calc_model_f_tmplt
  add constraint crs_l_calc_model_f_tmplt_ck01
check (removed in (0, 1))
/

--changeset pmasalov:crs-1.0-VTBCRS-379-2 logicalFilePath:crs-1.0-VTBCRS-379-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_MODEL';

    -- intable attribute INPUT_FORMS
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#FORM_TEMPLATE',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CALC_MODEL_F_TMPLT',
            null,
            'Шаблоны форм',
            'Model''s form templates',
            1,
            'ENTITY',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-379-3 logicalFilePath:crs-1.0-VTBCRS-379-3 endDelimiter:/
update crs_sys_h_attribute
   set key = 'CALC_MODEL#FORM_TEMPLATES'
 where key = 'CALC_MODEL#FORM_TEMPLATE'
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-379-4 logicalFilePath:crs-1.0-VTBCRS-379-4 endDelimiter:/
update crs_sys_s_attribute
   set entity_key = 'FORM_TEMPLATE'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_MODEL#FORM_TEMPLATES')
/
commit
/

--changeset pmasalov:crs-1.0-VTBCRS-379-5 logicalFilePath:crs-1.0-VTBCRS-379-5 endDelimiter:/
create sequence crs_l_calc_model_f_tmplt_seq
/

--changeset akirilchev:crs-1.0-VTBCRS-374 logicalFilePath:crs-1.0-VTBCRS-374 endDelimiter:/
drop table crs_calc_formula_filter_t
/

--changeset svaliev:crs-1.0-correcting-ldts logicalFilePath:crs-1.0-correcting-ldts endDelimiter:/
update crs_sys_h_attribute
   set ldts = to_date('011111', 'mmyyyy')
 where key = 'CALC_MODEL#FORM_TEMPLATES'
/
update crs_sys_s_attribute
       set ldts = to_date('011111', 'mmyyyy')
     where h_id in (select id
                      from crs_sys_h_attribute
                     where key = 'CALC_MODEL#FORM_TEMPLATES')
/

update crs_sys_h_attribute
   set ldts = to_date('011111', 'mmyyyy')
 where key = 'CALC#MODEL#VERSION'
/
update crs_sys_s_attribute
       set ldts = to_date('011111', 'mmyyyy')
     where h_id in (select id
                      from crs_sys_h_attribute
                     where key = 'CALC#MODEL#VERSION')
/

--changeset akirilchev:crs-1.0-VTBCRS-343-structure logicalFilePath:crs-1.0-VTBCRS-343-structure endDelimiter:/
create table crs_h_favorites
(
    id   number not null,
    key  varchar2(100) not null,
    ldts timestamp not null,
    constraint crs_h_favorites_pk primary key (id),
    constraint crs_h_favorites_uk01 unique (key)
)
/
comment on table crs_h_favorites is 'Favorites hub'
/
comment on column crs_h_favorites.id is 'Identifier'
/
comment on column crs_h_favorites.key is 'Key'
/
comment on column crs_h_favorites.ldts is 'Load date'
/
create index crs_h_favorites_i01 on crs_h_favorites (ldts) tablespace spoindx compress 1
/
alter table crs_h_favorites
  add constraint crs_h_favorites_ck01
check (key = upper(trim(key)))
/
create sequence crs_h_favorites_seq nocache
/
create table crs_s_favorites
(
    id                  number not null,
    h_id                number not null,
    ldts                timestamp not null,
    removed             number(1) not null,
    digest              varchar2(100) not null,
    constraint crs_s_favorites_pk primary key (id),
    constraint crs_s_favorites_uk01 unique (ldts, h_id)
)
/
comment on table crs_s_favorites is 'Favorites satellite'
/
comment on column crs_s_favorites.id is 'Identifier'
/
comment on column crs_s_favorites.h_id is 'Reference to hub'
/
comment on column crs_s_favorites.ldts is 'Load date'
/
comment on column crs_s_favorites.removed is 'Removed flag'
/
comment on column crs_s_favorites.digest is 'Row digest'
/
create index crs_s_favorites_i01 on crs_s_favorites (h_id) tablespace spoindx compress 1
/
alter table crs_s_favorites
  add constraint crs_s_favorites_fk01 foreign key (h_id)
references crs_h_favorites (id)
/
alter table crs_s_favorites
  add constraint crs_s_favorites_ck01
check (removed in (0, 1))
/
create sequence crs_s_favorites_seq nocache
/

create table crs_l_user_favorites
(
    id           number not null,
    ldts         timestamp not null,
    removed      number(1) default 0 not null,
    favorites_id number not null,
    user_id      number not null
)
/
comment on table crs_l_user_favorites is 'Link between user and favorites'
/
comment on column crs_l_user_favorites.id is 'Identifier'
/
comment on column crs_l_user_favorites.ldts is 'Load date'
/
comment on column crs_l_user_favorites.removed is 'Removed flag'
/
comment on column crs_l_user_favorites.favorites_id is 'Reference to favorites'
/
comment on column crs_l_user_favorites.user_id is 'Reference to user'
/
create index crs_l_user_favorites_i01 on crs_l_user_favorites (favorites_id) tablespace spoindx compress 1
/
create index crs_l_user_favorites_i02 on crs_l_user_favorites (user_id) tablespace spoindx compress 1
/
create index crs_l_user_favorites_i03 on crs_l_user_favorites (ldts) tablespace spoindx compress 1
/
alter table crs_l_user_favorites
  add constraint crs_l_user_favorites_pk primary key (id)
/
alter table crs_l_user_favorites
  add constraint crs_l_user_favorites_fk01 foreign key (favorites_id)
references crs_h_favorites (id)
/
alter table crs_l_user_favorites
  add constraint crs_l_user_favorites_fk02 foreign key (user_id)
references crs_h_user (id)
/
alter table crs_l_user_favorites
  add constraint crs_l_user_favorites_ck01
check (removed in (0, 1))
/
create sequence crs_l_user_favorites_seq nocache
/

create table crs_l_fav_user
(
    id           number not null,
    ldts         timestamp not null,
    removed      number(1) default 0 not null,
    favorites_id number not null,
    user_id      number not null
)
/
comment on table crs_l_fav_user is 'Link between favorites and user'
/
comment on column crs_l_fav_user.id is 'Identifier'
/
comment on column crs_l_fav_user.ldts is 'Load date'
/
comment on column crs_l_fav_user.removed is 'Removed flag'
/
comment on column crs_l_fav_user.favorites_id is 'Reference to favorites'
/
comment on column crs_l_fav_user.user_id is 'Reference to user'
/
create index crs_l_fav_user_i01 on crs_l_fav_user (favorites_id) tablespace spoindx compress 1
/
create index crs_l_fav_user_i02 on crs_l_fav_user (user_id) tablespace spoindx compress 1
/
create index crs_l_fav_user_i03 on crs_l_fav_user (ldts) tablespace spoindx compress 1
/
alter table crs_l_fav_user
  add constraint crs_l_fav_user_pk primary key (id)
/
alter table crs_l_fav_user
  add constraint crs_l_fav_user_fk01 foreign key (favorites_id)
references crs_h_favorites (id)
/
alter table crs_l_fav_user
  add constraint crs_l_fav_user_fk02 foreign key (user_id)
references crs_h_user (id)
/
alter table crs_l_fav_user
  add constraint crs_l_fav_user_ck01
check (removed in (0, 1))
/
create sequence crs_l_fav_user_seq nocache
/

create table crs_l_fav_client
(
    id           number not null,
    ldts         timestamp not null,
    removed      number(1) default 0 not null,
    favorites_id number not null,
    client_id    number not null
)
/
comment on table crs_l_fav_client is 'Link between favorites and client'
/
comment on column crs_l_fav_client.id is 'Identifier'
/
comment on column crs_l_fav_client.ldts is 'Load date'
/
comment on column crs_l_fav_client.removed is 'Removed flag'
/
comment on column crs_l_fav_client.favorites_id is 'Reference to favorites'
/
comment on column crs_l_fav_client.client_id is 'Reference to client'
/
create index crs_l_fav_client_i01 on crs_l_fav_client (favorites_id) tablespace spoindx compress 1
/
create index crs_l_fav_client_i02 on crs_l_fav_client (client_id) tablespace spoindx compress 1
/
create index crs_l_fav_client_i03 on crs_l_fav_client (ldts) tablespace spoindx compress 1
/
alter table crs_l_fav_client
  add constraint crs_l_fav_client_pk primary key (id)
/
alter table crs_l_fav_client
  add constraint crs_l_fav_client_fk01 foreign key (favorites_id)
references crs_h_favorites (id)
/
alter table crs_l_fav_client
  add constraint crs_l_fav_client_fk02 foreign key (client_id)
references crs_h_client (id)
/
alter table crs_l_fav_client
  add constraint crs_l_fav_client_ck01
check (removed in (0, 1))
/
create sequence crs_l_fav_client_seq nocache
/

create table crs_l_fav_client_group
(
    id              number not null,
    ldts            timestamp not null,
    removed         number(1) default 0 not null,
    favorites_id    number not null,
    client_group_id number not null
)
/
comment on table crs_l_fav_client_group is 'Link between favorites and client_group'
/
comment on column crs_l_fav_client_group.id is 'Identifier'
/
comment on column crs_l_fav_client_group.ldts is 'Load date'
/
comment on column crs_l_fav_client_group.removed is 'Removed flag'
/
comment on column crs_l_fav_client_group.favorites_id is 'Reference to favorites'
/
comment on column crs_l_fav_client_group.client_group_id is 'Reference to client_group'
/
create index crs_l_fav_client_group_i01 on crs_l_fav_client_group (favorites_id) tablespace spoindx compress 1
/
create index crs_l_fav_client_group_i02 on crs_l_fav_client_group (client_group_id) tablespace spoindx compress 1
/
create index crs_l_fav_client_group_i03 on crs_l_fav_client_group (ldts) tablespace spoindx compress 1
/
alter table crs_l_fav_client_group
  add constraint crs_l_fav_client_group_pk primary key (id)
/
alter table crs_l_fav_client_group
  add constraint crs_l_fav_client_group_fk01 foreign key (favorites_id)
references crs_h_favorites (id)
/
alter table crs_l_fav_client_group
  add constraint crs_l_fav_client_group_fk02 foreign key (client_group_id)
references crs_h_client_group (id)
/
alter table crs_l_fav_client_group
  add constraint crs_l_fav_client_group_ck01
check (removed in (0, 1))
/
create sequence crs_l_fav_client_group_seq nocache
/

create table crs_l_fav_calc_model
(
    id           number not null,
    ldts          timestamp not null,
    removed       number(1) default 0 not null,
    favorites_id  number not null,
    calc_model_id number not null
)
/
comment on table crs_l_fav_calc_model is 'Link between favorites and calc_model'
/
comment on column crs_l_fav_calc_model.id is 'Identifier'
/
comment on column crs_l_fav_calc_model.ldts is 'Load date'
/
comment on column crs_l_fav_calc_model.removed is 'Removed flag'
/
comment on column crs_l_fav_calc_model.favorites_id is 'Reference to favorites'
/
comment on column crs_l_fav_calc_model.calc_model_id is 'Reference to calc_model'
/
create index crs_l_fav_calc_model_i01 on crs_l_fav_calc_model (favorites_id) tablespace spoindx compress 1
/
create index crs_l_fav_calc_model_i02 on crs_l_fav_calc_model (calc_model_id) tablespace spoindx compress 1
/
create index crs_l_fav_calc_model_i03 on crs_l_fav_calc_model (ldts) tablespace spoindx compress 1
/
alter table crs_l_fav_calc_model
  add constraint crs_l_fav_calc_model_pk primary key (id)
/
alter table crs_l_fav_calc_model
  add constraint crs_l_fav_calc_model_fk01 foreign key (favorites_id)
references crs_h_favorites (id)
/
alter table crs_l_fav_calc_model
  add constraint crs_l_fav_calc_model_fk02 foreign key (calc_model_id)
references crs_h_calc_model (id)
/
alter table crs_l_fav_calc_model
  add constraint crs_l_fav_calc_model_ck01
check (removed in (0, 1))
/
create sequence crs_l_fav_calc_model_seq nocache
/

create table crs_l_fav_calc
(
    id           number not null,
    ldts         timestamp not null,
    removed      number(1) default 0 not null,
    favorites_id number not null,
    calc_id      number not null
)
/
comment on table crs_l_fav_calc is 'Link between favorites and calc'
/
comment on column crs_l_fav_calc.id is 'Identifier'
/
comment on column crs_l_fav_calc.ldts is 'Load date'
/
comment on column crs_l_fav_calc.removed is 'Removed flag'
/
comment on column crs_l_fav_calc.favorites_id is 'Reference to favorites'
/
comment on column crs_l_fav_calc.calc_id is 'Reference to calc'
/
create index crs_l_fav_calc_i01 on crs_l_fav_calc (favorites_id) tablespace spoindx compress 1
/
create index crs_l_fav_calc_i02 on crs_l_fav_calc (calc_id) tablespace spoindx compress 1
/
create index crs_l_fav_calc_i03 on crs_l_fav_calc (ldts) tablespace spoindx compress 1
/
alter table crs_l_fav_calc
  add constraint crs_l_fav_calc_pk primary key (id)
/
alter table crs_l_fav_calc
  add constraint crs_l_fav_calc_fk01 foreign key (favorites_id)
references crs_h_favorites (id)
/
alter table crs_l_fav_calc
  add constraint crs_l_fav_calc_fk02 foreign key (calc_id)
references crs_h_calc (id)
/
alter table crs_l_fav_calc
  add constraint crs_l_fav_calc_ck01
check (removed in (0, 1))
/
create sequence crs_l_fav_calc_seq nocache
/

--changeset akirilchev:crs-1.0-VTBCRS-343-attr logicalFilePath:crs-1.0-VTBCRS-343-attr endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    -- entity FAVORITES
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'FAVORITES', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Избранные',
            'Favorites',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'USER';

    -- reference attribute USER#FAVORITES
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'USER#FAVORITES',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_USER_FAVORITES', 'Избранные', 'Favorites',
            'FAVORITES', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FAVORITES';

    -- reference attribute FAVORITES#USER
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FAVORITES#USER',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_FAV_USER', 'Пользователь', 'User',
            'USER', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FAVORITES';

    -- reference attribute FAVORITES#CLIENT_GROUP
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FAVORITES#CLIENT_GROUP',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_FAV_CLIENT_GROUP', 'Группа компаний', 'Client group',
            'CLIENT_GROUP', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FAVORITES';

    -- reference attribute FAVORITES#CLIENT
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FAVORITES#CLIENT',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_FAV_CLIENT', 'Клиент', 'Client',
            'CLIENT', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FAVORITES';

    -- reference attribute FAVORITES#CALC_MODEL
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FAVORITES#CALC_MODEL',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_FAV_CALC_MODEL', 'Модель', 'Model',
            'CALC_MODEL', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'FAVORITES';

    -- reference attribute FAVORITES#CALC
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'FAVORITES#CALC',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_FAV_CALC', 'Расчет', 'Calculation',
            'CALC', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-382 logicalFilePath:crs-1.0-VTBCRS-382 endDelimiter:/
create sequence crs_l_calc_client_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-382-1 logicalFilePath:crs-1.0-VTBCRS-382-1 endDelimiter:/
create table crs_l_calc_client
(
    id            number not null,
    ldts          timestamp(6) not null,
    removed       number(1) not null,
    calc_id number not null,
    client_id number not null
)/
comment on table crs_l_calc_client is 'Calculation to client link'
/
comment on column crs_l_calc_client.id is 'Identifier'
/
comment on column crs_l_calc_client.ldts is 'Load date'
/
comment on column crs_l_calc_client.calc_id is 'Reference to calculation'
/
comment on column crs_l_calc_client.client_id is 'Reference to client'
/

create index crs_l_calc_client_i01 on crs_l_calc_client (calc_id, ldts)
tablespace spoindx compress 1
/
create index crs_l_calc_client_i02 on crs_l_calc_client (client_id, ldts)
tablespace spoindx compress 1
/

alter table crs_l_calc_client
    add constraint crs_l_calc_client_pk primary key (id)
    using index
    tablespace spoindx
/
alter table crs_l_calc_client
    add constraint crs_l_calc_client_fk01 foreign key (calc_id)
references crs_h_calc (id)
/
alter table crs_l_calc_client
    add constraint crs_l_calc_client_fk02 foreign key (client_id)
references crs_h_client (id)
/

alter table crs_l_calc_client
    add constraint crs_l_calc_client_ck01
check (removed in (0, 1))
/

--changeset pmasalov:crs-1.0-VTBCRS-382-2 logicalFilePath:crs-1.0-VTBCRS-382-2 endDelimiter:/
create sequence crs_l_calc_client_group_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-382-3 logicalFilePath:crs-1.0-VTBCRS-382-3 endDelimiter:/
create table crs_l_calc_client_group
(
    id            number not null,
    ldts          timestamp(6) not null,
    removed       number(1) not null,
    calc_id number not null,
    client_group_id number not null
)/
comment on table crs_l_calc_client_group is 'Calculation to client link'
/
comment on column crs_l_calc_client_group.id is 'Identifier'
/
comment on column crs_l_calc_client_group.ldts is 'Load date'
/
comment on column crs_l_calc_client_group.calc_id is 'Reference to calculation'
/
comment on column crs_l_calc_client_group.client_group_id is 'Reference to client group'
/

create index crs_l_calc_client_group_i01 on crs_l_calc_client_group (calc_id, ldts)
tablespace spoindx compress 1
/
create index crs_l_calc_client_group_i02 on crs_l_calc_client_group (client_group_id, ldts)
tablespace spoindx compress 1
/

alter table crs_l_calc_client_group
    add constraint crs_l_calc_client_group_pk primary key (id)
    using index
    tablespace spoindx
/
alter table crs_l_calc_client_group
    add constraint crs_l_calc_client_group_fk01 foreign key (calc_id)
references crs_h_calc (id)
/
alter table crs_l_calc_client_group
    add constraint crs_l_calc_client_group_fk02 foreign key (client_group_id)
references crs_h_client_group (id)
/

alter table crs_l_calc_client_group
    add constraint crs_l_calc_client_group_ck01
check (removed in (0, 1))
/

--changeset pmasalov:crs-1.0-VTBCRS-382-4 logicalFilePath:crs-1.0-VTBCRS-382-4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#CLIENT',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CALC_CLIENT',
            'CLIENT#NAME',
            'Клиент',
            'Client',
            1,
            'CLIENT',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-382-5 logicalFilePath:crs-1.0-VTBCRS-382-5 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#CLIENT_GROUP',v_ldts)
    returning id into v_h_attribute_id;

    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'CRS_L_CALC_CLIENT_GROUP',
            'CLIENT_GROUP#NAME',
            'Группа клиента',
            'Client group',
            1,
            'CLIENT',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-382-6 logicalFilePath:crs-1.0-VTBCRS-382-6 endDelimiter:/
update crs_sys_s_attribute
   set entity_key = 'CLIENT_GROUP'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC#CLIENT_GROUP')
/
commit
/

--changeset imatushak:crs-1.0-VTBCRS-405 logicalFilePath:crs-1.0-VTBCRS-405 endDelimiter:/
update crs_sys_s_attribute
   set filter_available = 1
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#AUTHOR')
/

update crs_sys_s_attribute
   set filter_available = 1
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#MODEL')
/

--changeset imatushak:crs-1.0-VTBCRS-405-1 logicalFilePath:crs-1.0-VTBCRS-405-1 endDelimiter:/
update crs_sys_s_attribute
   set view_order = 2
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#NAME')
/

update crs_sys_s_attribute
   set view_order = 5
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#AUTHOR')
/

update crs_sys_s_attribute
   set view_order = 6
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#PUBLISHED')
/

update crs_sys_s_attribute
   set view_order = 7
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#CALCULATED')
/

update crs_sys_s_attribute
   set view_order = 9
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#CLIENT_GROUP')
/

update crs_sys_s_attribute
   set view_order = 0
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#MODEL')
/

update crs_sys_s_attribute
   set view_order = 1
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#MODEL#VERSION')
/

update crs_sys_s_attribute
   set view_order = 4
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#DATA_ACTUALITY')
/

update crs_sys_s_attribute
   set view_order = 8
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#CLIENT')
/

update crs_sys_s_attribute
   set view_order = 3
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#ACTUALITY')
/

--changeset emelnikov:crs-1.0-VTBCRS-341 logicalFilePath:crs-1.0-VTBCRS-341 endDelimiter:/
alter table crs_sys_s_storage_desc add description clob
/
comment on column crs_sys_s_storage_desc.description is 'Description file storage'
/

--changeset mkuzmin:crs-1.0-VTBCRS-369 logicalFilePath:crs-1.0-VTBCRS-369 endDelimiter:/
create table crs_audit_log
(
    id            number not null,
    metadata_key  varchar2(100) not null,
    entity_key    varchar2(100) not null,
    entity_satellite_id number not null,
    entity_ldts   timestamp(6) not null,
    executor_id   number not null,
    action        varchar2(100) not null,
    constraint crs_audit_log_pk primary key(id)
)
/
comment on table crs_audit_log is 'Audit log'
/
comment on column crs_audit_log.id is 'Identifier'
/
comment on column crs_audit_log.metadata_key is 'Entity meta key'
/
comment on column crs_audit_log.entity_key is 'Entity key'
/
comment on column crs_audit_log.entity_satellite_id is 'Entity id'
/
comment on column crs_audit_log.entity_ldts is 'Load date'
/
comment on column crs_audit_log.executor_id is 'User id'
/
comment on column crs_audit_log.action is 'Action enum'
/
alter table crs_audit_log
  add constraint crs_audit_log_fk01 foreign key (metadata_key)
references crs_sys_h_entity (key)
/
create sequence crs_audit_log_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset mkuzmin:crs-1.0-VTBCRS-369-1 logicalFilePath:crs-1.0-VTBCRS-369-1 endDelimiter:/
alter table crs_audit_log
  add constraint crs_audit_log_fk02 foreign key (executor_id)
references crs_h_user (id)
/

--changeset mkuzmin:crs-1.0-VTBCRS-369-2 logicalFilePath:crs-1.0-VTBCRS-369-2 endDelimiter:/ runOnChange:true
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tab_cols where table_name = 'CRS_AUDIT_LOG' and column_name = 'RECORD_TIMESTAMP'
alter table crs_audit_log add record_timestamp timestamp not null
/

--changeset pmasalov:crs-1.0-VTBCRS-433-0 logicalFilePath:crs-1.0-VTBCRS-433-0 endDelimiter:/
begin
    for r in (select index_name from user_indexes where index_name in ('CRS_S_CALC_FORMULA_I01','CRS_S_CALC_I01',
                                                                       'CRS_SYS_S_STORAGE_DESC_I01',
                                                                       'CRS_SYS_S_STORAGE_I01',
                                                                       'CRS_SYS_S_LOCALIZATION_I01',
                                                                       'CRS_SYS_S_LOCALIZATION_I01',
                                                                       'CRS_SYS_S_ENTITY_I01',
                                                                       'CRS_SYS_S_ATTRIBUTE_I01',
                                                                       'CRS_S_CALC_MODEL_I01',
                                                                       'CRS_S_CALC_FORMULA_RESULT_I01',
                                                                       'CRS_S_CALC_FORMULA_DESC_I01',
                                                                       'CRS_S_FAVORITES_I01')) loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-433 logicalFilePath:crs-1.0-VTBCRS-433 endDelimiter:/
create table crs_h_calc_profile
(
    id number not null,
    key varchar2(100 char) not null,
    ldts timestamp not null,
    constraint crs_h_calc_profile_ck01 check (key = upper(trim(key))),
    constraint crs_h_calc_profile_pk primary key (id),
    constraint crs_h_calc_profile_uk01 unique (key) using index tablespace spoindx
)
/
comment on table crs_h_calc_profile is 'Calculation profile hub'
/
comment on column crs_h_calc_profile.id is 'Identifier'
/
comment on column crs_h_calc_profile.key is 'Key'
/
comment on column crs_h_calc_profile.ldts is 'Load date'
/

--changeset pmasalov:crs-1.0-VTBCRS-433-2 logicalFilePath:crs-1.0-VTBCRS-433-2 endDelimiter:/
create table crs_s_calc_profile
(
    id number not null,
    h_id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) not null,
    digest varchar2(100 char) not null,
    constraint crs_s_calc_profile_pk primary key (id) using index tablespace spoindx,
    constraint crs_s_calc_profile_ck01 check (removed in (0, 1)),
    constraint crs_s_calc_profile_uk01 unique (h_id, ldts) using index compress 1 tablespace spoindx,
    constraint crs_s_calc_profile_fk01 foreign key (h_id) references crs_h_calc_profile (id)
)
/
comment on table crs_s_calc_profile is 'Calculation profile satellite'
/
comment on column crs_s_calc_profile.id is 'Identifier'
/
comment on column crs_s_calc_profile.h_id is 'Reference to hub'
/
comment on column crs_s_calc_profile.ldts is 'Load date'
/
comment on column crs_s_calc_profile.removed is 'Removed flag'
/
comment on column crs_s_calc_profile.digest is 'Row digest'
/

--changeset pmasalov:crs-1.0-VTBCRS-433-3 logicalFilePath:crs-1.0-VTBCRS-433-3 endDelimiter:/
create sequence crs_h_calc_profile_seq
/
create sequence crs_s_calc_profile_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-433-4 logicalFilePath:crs-1.0-VTBCRS-433-4 endDelimiter:/
create table crs_l_calc_profile_name
(
    id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) default 0 not null,
    calc_profile_id number not null,
    localization_id number not null,
    constraint crs_l_calc_profile_name_pk primary key (id) using index tablespace spoindx,
    constraint crs_l_calc_profile_name_ck01 check (removed in (0, 1)),
    constraint crs_l_calc_profile_name_fk01 foreign key (calc_profile_id) references crs_h_calc_profile (id),
    constraint crs_l_calc_profile_name_fk02 foreign key (localization_id) references crs_sys_h_localization (id)
)
/
comment on table crs_l_calc_profile_name is 'Calculation profile to multilang profile name link'
/
comment on column crs_l_calc_profile_name.id is 'Identifier'
/
comment on column crs_l_calc_profile_name.ldts is 'Load date'
/
comment on column crs_l_calc_profile_name.removed is 'Removed flag'
/
comment on column crs_l_calc_profile_name.calc_profile_id is 'Reference to calculation profile'
/
comment on column crs_l_calc_profile_name.localization_id is 'Reference to calculation profile multilang name'
/
create index crs_l_calc_profile_name_i01 on crs_l_calc_profile_name (calc_profile_id, ldts)
compress 1 tablespace spoindx
/
create index crs_l_calc_profile_name_i02 on crs_l_calc_profile_name (localization_id)
compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-433-5 logicalFilePath:crs-1.0-VTBCRS-433-5 endDelimiter:/
create sequence crs_l_calc_profile_name_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-433-6 logicalFilePath:crs-1.0-VTBCRS-433-6 endDelimiter:/
declare
    v_h_id number;
    v_localization_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    insert into crs_h_calc_profile (id, key, ldts)
    values (crs_h_calc_profile_seq.nextval, 'RATED', v_ldts) returning id into v_h_id;
    insert into crs_s_calc_profile (id, h_id, ldts, removed, digest)
    values (crs_s_calc_profile_seq.nextval, v_h_id, v_ldts, 0, 'no-digest');
    insert into crs_sys_h_localization (id, key, ldts)
    values (crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, v_ldts) returning id into v_localization_id;
    insert into crs_sys_s_localization(id, h_id, ldts, string_ru, string_en, removed, digest)
    values (crs_sys_s_localization_seq.nextval, v_localization_id, v_ldts, 'Расчетный', 'Rated', 0, 'no-digest');
    insert into crs_l_calc_profile_name(id, ldts, removed, calc_profile_id, localization_id)
    values (crs_l_calc_profile_name_seq.nextval, v_ldts, 0, v_h_id, v_localization_id);

    insert into crs_h_calc_profile (id, key, ldts)
    values (crs_h_calc_profile_seq.nextval, 'EXPERT', v_ldts) returning id into v_h_id;
    insert into crs_s_calc_profile (id, h_id, ldts, removed, digest)
    values (crs_s_calc_profile_seq.nextval, v_h_id, v_ldts, 0, 'no-digest');
    insert into crs_sys_h_localization (id, key, ldts)
    values (crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, v_ldts) returning id into v_localization_id;
    insert into crs_sys_s_localization(id, h_id, ldts, string_ru, string_en, removed, digest)
    values (crs_sys_s_localization_seq.nextval, v_localization_id, v_ldts, 'Экспертный', 'Expert', 0, 'no-digest');
    insert into crs_l_calc_profile_name(id, ldts, removed, calc_profile_id, localization_id)
    values (crs_l_calc_profile_name_seq.nextval, v_ldts, 0, v_h_id, v_localization_id);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-433-7 logicalFilePath:crs-1.0-VTBCRS-433-7 endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'CALC_PROFILE', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Профиль расчёта',
            'Calculation profile',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
/

declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC_PROFILE';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_PROFILE#NAME',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 1, null, 0, 0, null,
            null, 'Наименование', 'Name',
            null, 'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-433-8 logicalFilePath:crs-1.0-VTBCRS-433-8 endDelimiter:/
update crs_sys_s_attribute
   set link_table = 'crs_l_calc_profile_name'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC_PROFILE#NAME')
/

--changeset pmasalov:crs-1.0-VTBCRS-434 logicalFilePath:crs-1.0-VTBCRS-434 endDelimiter:/
create table crs_l_calc_profile
(
    id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) default 0 not null,
    calc_id number not null,
    calc_profile_id number not null,
    constraint crs_l_calc_profile_pk primary key (id) using index tablespace spoindx,
    constraint crs_l_calc_profile_ck01 check (removed in (0, 1)),
    constraint crs_l_calc_profile_fk02 foreign key (calc_profile_id) references crs_h_calc_profile (id),
    constraint crs_l_calc_profile_fk01 foreign key (calc_id) references crs_h_calc (id)
)
/
comment on table crs_l_calc_profile is 'Calculation to calculation profile link'
/
comment on column crs_l_calc_profile.id is 'Identifier'
/
comment on column crs_l_calc_profile.ldts is 'Load date'
/
comment on column crs_l_calc_profile.removed is 'Removed flag'
/
comment on column crs_l_calc_profile.calc_profile_id is 'Reference to calculation profile'
/
comment on column crs_l_calc_profile.calc_id is 'Reference to calculation'
/
create index crs_l_calc_profile_i01 on crs_l_calc_profile (calc_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_calc_profile_i02 on crs_l_calc_profile (calc_profile_id) compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-434-1 logicalFilePath:crs-1.0-VTBCRS-434-1 endDelimiter:/
create sequence crs_l_calc_profile_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-434-2 logicalFilePath:crs-1.0-VTBCRS-434-2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CALC';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#CALC_PROFILE',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'crs_l_calc_profile',
            'CALC_PROFILE#NAME',
            'Профиль расчёта',
            'Calculation profile',
            0,
            'CALC_PROFILE',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);

    commit;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-434-3 logicalFilePath:crs-1.0-VTBCRS-434-3 endDelimiter:/
declare
    v_rated_h_id number;
begin
    select id
      into v_rated_h_id
      from crs_h_calc_profile
     where key = 'RATED';

    insert into crs_l_calc_profile (id, ldts, removed, calc_id, calc_profile_id)
    select crs_l_calc_profile_seq.nextval, h.ldts, 0, h.id, v_rated_h_id
      from crs_h_calc h
     where not exists(select 1
                        from crs_l_calc_profile ch
                       where ch.calc_id = h.id
                         and ch.calc_profile_id = v_rated_h_id);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-435 logicalFilePath:crs-1.0-VTBCRS-435 endDelimiter:/
declare
    G_LDTS constant timestamp := to_date('011111', 'mmyyyy');
    v_link_table_name varchar2(100);

    function generate_link_table_name return varchar2 is
        v_link_table_prefix varchar2(6) := 'crs_l_';
        v_result varchar2(23);
        v_link_table_exists number(1);
    begin
        loop
            select v_link_table_prefix || to_char(dbms_random.value(0,99999999999999999), 'FM00000000000000000')
              into v_result
              from dual;

            select nvl2((select ut.table_name
                           from user_tables ut
                          where ut.table_name = upper(v_result)), 1, 0)
              into v_link_table_exists
              from dual;

            exit when v_link_table_exists = 0;
        end loop;
        return v_result;
    end generate_link_table_name;

    procedure exec_ddl(p_key varchar2, p_table varchar2, p_sql_tempate varchar2) is
        pragma autonomous_transaction;
        v_sql varchar2(32767);
    begin
        v_sql := replace(replace(p_sql_tempate, '<p_table>', p_table), '<p_key>', p_key);
        execute immediate v_sql;
    end;

    procedure create_link_table(p_key varchar2, p_table varchar2) is
    begin
        exec_ddl(p_key, p_table,
                 'create table <p_table>
                 (
                     id number not null,
                     ldts timestamp(6) not null,
                     removed number(1, 0) default 0 not null,
                     <p_key>_id number not null,
                     calc_profile_id number not null, constraint <p_table>_pk primary key (id) using index tablespace spoindx,
                     constraint <p_table>_ck01 check (removed in (0, 1)),
                     constraint <p_table>_fk02 foreign key (calc_profile_id) references crs_h_calc_profile (id),
                     constraint <p_table>_fk01 foreign key (<p_key>_id) references crs_h_<p_key> (id)
                 )');
        exec_ddl(p_key, p_table,
                 'create index <p_table>_i01 on <p_table> (<p_key>_id, ldts) compress 1 tablespace spoindx');
        exec_ddl(p_key, p_table,
                 'create index <p_table>_i02 on <p_table> (calc_profile_id) compress 1 tablespace spoindx');
        exec_ddl(p_key, p_table,
                 'create sequence <p_table>_seq');
    end;

    procedure drop_link_table(p_table varchar2) is
    begin
        for r in (select 1 from user_tables where table_name = upper(p_table)) loop
            exec_ddl('', p_table, 'drop table <p_table>');
        end loop;
        for r in (select 1 from user_sequences where sequence_name = upper(p_table||'_seq')) loop
            exec_ddl('', p_table, 'drop sequence <p_table>_seq');
        end loop;
    end;

    procedure setup_reference_attribute(p_key varchar2, p_h_entity_id number, p_table varchar2) is
        v_h_attribute_id number;
    begin
        insert into crs_sys_h_attribute (id, key, ldts)
        values (crs_sys_h_attribute_seq.nextval, p_key || '#CALC_PROFILE', G_LDTS)
        returning id into v_h_attribute_id;
        insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                         entity_key, removed, type, native_column)
        values (crs_sys_s_attribute_seq.nextval,
                v_h_attribute_id,
                G_LDTS,
                0,
                0,
                0,
                p_table,
                'CALC_PROFILE#NAME',
                'Профиль расчёта',
                'Calculation profile',
                0,
                'CALC_PROFILE',
                0,
                'REFERENCE',
                null);
        insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
        values (crs_sys_l_entity_attribute_seq.nextval, p_h_entity_id, v_h_attribute_id, G_LDTS, 0);
    end;

begin
    for r in (select h.key, h.id h_id
                from crs_sys_h_entity h join crs_sys_l_entity_type l on l.entity_id = h.id
                                        join crs_sys_h_entity_type t on t.id = l.type_id
                                                                    and t.key in ('CLASSIFIER', 'INPUT_FORM')
               where not exists(select 1
                                  from crs_sys_l_entity_attribute lea join crs_sys_h_attribute ha on lea.attribute_id = ha.id
                                                                                                 and ha.key = h.key || '#CALC_PROFILE'
                                 where lea.entity_id = h.id)
    )
    loop
        v_link_table_name := generate_link_table_name;
        create_link_table(r.key, v_link_table_name);
        setup_reference_attribute(r.key, r.h_id, v_link_table_name);
        commit;
    end loop;

exception when others then
    drop_link_table(v_link_table_name);
    rollback;
    raise;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-435-insert-rated logicalFilePath:crs-1.0-VTBCRS-435-insert-rated endDelimiter:/
declare
    v_rated_h_id number;

    procedure exec_sql(p_table varchar2, p_key varchar2, p_profile_h_id number, p_sql_tamplate varchar2) is
        v_sql varchar2(32767);
    begin
        v_sql := replace(replace(replace(p_sql_tamplate, '<table>', p_table),'<key>',p_key), '<profile_h_id>', p_profile_h_id);
        execute immediate v_sql;
    end;

begin
    select id into v_rated_h_id from crs_h_calc_profile where key = 'RATED';

    for r in (select h.key, sa.link_table
                from crs_sys_h_entity h join crs_sys_l_entity_type l on l.entity_id = h.id
                                        join crs_sys_h_entity_type t on t.id = l.type_id and t.key in ('CLASSIFIER', 'INPUT_FORM')
                                        join crs_sys_l_entity_attribute lea on lea.entity_id = h.id
                                        join crs_sys_h_attribute ha on lea.attribute_id = ha.id and ha.key = h.key || '#CALC_PROFILE'
                                        join crs_sys_s_attribute sa on sa.h_id = ha.id
    ) loop
        exec_sql(r.link_table, r.key, v_rated_h_id,
                 'insert into <table> (id, ldts, removed, <key>_id, calc_profile_id)
                  select <table>_seq.nextval, h.ldts, 0, h.id, <profile_h_id>
                    from crs_h_<key> h
                   where not exists(select 1 from <table> ch where ch.<key>_id = h.id and ch.calc_profile_id = <profile_h_id>)');
    end loop;
end;

--changeset pmasalov:crs-1.0-VTBCRS-457 logicalFilePath:crs-1.0-VTBCRS-457 endDelimiter:/
update crs_sys_l_entity_attribute l set ldts = to_date('011111', 'mmyyyy')
where attribute_id in (select id
                         from crs_sys_h_attribute
                        where key = 'CALC_MODEL#FORM_TEMPLATES')
/

--changeset achalov:crs-1.0-VTBCRS-393 logicalFilePath:crs-1.0-VTBCRS-393 endDelimiter:/
alter table crs_s_client drop column reg_countrycode
/
alter table crs_s_client drop column ogrn
/
alter table crs_s_client_group drop column reg_countrycode
/
alter table crs_s_client_inn drop column tax_id_countrycode
/
alter table crs_l_client_opf drop column localization_id
/
alter table crs_l_client_reg_country drop column localization_id
/
alter table crs_l_client_category drop column localization_id
/
alter table crs_l_client_segment drop column localization_id
/
alter table crs_l_client_industry drop column localization_id
/
alter table crs_l_client_grp_reg_cntr drop column localization_id
/
alter table crs_l_client_grp_segment drop column localization_id
/
alter table crs_l_client_grp_industry drop column localization_id
/
delete
  from crs_sys_l_entity_attribute
where attribute_id in (
  select id from crs_sys_h_attribute where key in (
      'CLIENT_GROUP#SEGMENT', 'CLIENT_GROUP#INDUSTRY', 'CLIENT_GROUP#REG_COUNTRY', 'CLIENT_GROUP#REG_COUNTRYCODE',
      'CLIENT#OPF', 'CLIENT#REG_COUNTRY', 'CLIENT#CATEGORY', 'CLIENT#SEGMENT', 'CLIENT#INDUSTRY', 'CLIENT#REG_COUNTRYCODE', 'CLIENT#OGRN',
      'CLIENT_INN#TAX_ID_COUNTRYCODE'
  )
)
/
delete
  from crs_sys_s_attribute
where h_id in (
  select id from crs_sys_h_attribute where key in (
      'CLIENT_GROUP#SEGMENT', 'CLIENT_GROUP#INDUSTRY', 'CLIENT_GROUP#REG_COUNTRY', 'CLIENT_GROUP#REG_COUNTRYCODE',
      'CLIENT#OPF', 'CLIENT#REG_COUNTRY', 'CLIENT#CATEGORY', 'CLIENT#SEGMENT', 'CLIENT#INDUSTRY', 'CLIENT#REG_COUNTRYCODE', 'CLIENT#OGRN',
      'CLIENT_INN#TAX_ID_COUNTRYCODE'
  )
)
/
delete from crs_sys_h_attribute
 where key in (
      'CLIENT_GROUP#SEGMENT', 'CLIENT_GROUP#INDUSTRY', 'CLIENT_GROUP#REG_COUNTRY', 'CLIENT_GROUP#REG_COUNTRYCODE',
      'CLIENT#OPF', 'CLIENT#REG_COUNTRY', 'CLIENT#CATEGORY', 'CLIENT#SEGMENT', 'CLIENT#INDUSTRY', 'CLIENT#REG_COUNTRYCODE', 'CLIENT#OGRN',
      'CLIENT_INN#TAX_ID_COUNTRYCODE'
  )
/
delete from crs_l_client_opf
/
delete from crs_l_client_reg_country
/
delete from crs_l_client_category
/
delete from crs_l_client_segment
/
delete from crs_l_client_industry
/
delete from crs_l_client_grp_reg_cntr
/
delete from crs_l_client_grp_segment
/
delete from crs_l_client_grp_industry
/

--changeset achalov:crs-1.0-VTBCRS-397 logicalFilePath:crs-1.0-VTBCRS-397 endDelimiter:/
create table crs_h_client_category(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_category_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_category_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_category_seq
/
comment on table crs_h_client_category is 'Client category hub (origin: CPI)'
/
comment on column crs_h_client_category.id is 'Identifier'
/
comment on column crs_h_client_category.key is 'Key, coincides with client portal identifier'
/
comment on column crs_h_client_category.ldts is 'Load date'
/
create table crs_s_client_category(
    id      number              not null,
    h_id    number              not null,
    digest  varchar2(100)       not null,
    removed number(1) default 0 not null,
    ldts    timestamp           not null,
    constraint crs_s_client_category_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_category_ck01 check(removed in (0, 1)),
    constraint crs_s_client_category_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_client_category_fk01 foreign key(h_id) references crs_h_client_category(id)
)
/
create index crs_s_client_category_i01 on crs_s_client_category(h_id) tablespace spoindx
/
create index crs_s_client_category_i02 on crs_s_client_category(ldts) tablespace spoindx
/
create sequence crs_s_client_category_seq
/
comment on table crs_s_client_category is 'Client category satellite (origin: CPI)'
/
comment on column crs_s_client_category.id is 'Identifier'
/
comment on column crs_s_client_category.h_id is 'Reference to hub'
/
comment on column crs_s_client_category.digest is 'Row digest'
/
comment on column crs_s_client_category.ldts is 'Load date'
/
comment on column crs_s_client_category.removed is 'Removed flag'
/
create table crs_l_client_cat_name(
    id                 number              not null,
    client_category_id number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_client_cat_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_cat_name_fk01 foreign key(client_category_id) references crs_h_client_category(id),
    constraint crs_l_client_cat_name_fk02 foreign key(localization_id)    references crs_sys_h_localization(id),
    constraint crs_l_client_cat_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_cat_name_i01 on crs_l_client_cat_name(client_category_id) tablespace spoindx
/
create index crs_l_client_cat_name_i02 on crs_l_client_cat_name(localization_id) tablespace spoindx
/
create index crs_l_client_cat_name_i03 on crs_l_client_cat_name(ldts) tablespace spoindx
/
create sequence crs_l_client_cat_name_seq
/
comment on table crs_l_client_cat_name is 'Client category to multilanguage name link'
/
comment on column crs_l_client_cat_name.client_category_id is 'Reference to client category hub (origin: CPI)'
/
comment on column crs_l_client_cat_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_cat_name.removed is 'Removed flag'
/
comment on column crs_l_client_cat_name.ldts is 'Load date'
/


create table crs_h_client_currency(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_currency_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_currency_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_currency_seq
/
comment on table crs_h_client_currency is 'Currency hub (origin: CPI)'
/
comment on column crs_h_client_currency.id is 'Identifier'
/
comment on column crs_h_client_currency.key is 'Key, coincides with client portal identifier'
/
comment on column crs_h_client_currency.ldts is 'Load date'
/
create table crs_s_client_currency(
    id       number              not null,
    h_id     number              not null,
    code     varchar(6),
    code_num number,
    digest   varchar2(100)       not null,
    removed  number(1) default 0 not null,
    ldts     timestamp           not null,
    constraint crs_s_client_currency_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_currency_ck01 check(removed in (0, 1)),
    constraint crs_s_client_currency_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_client_currency_fk01 foreign key(h_id) references crs_h_client_currency(id)
)
/
create index crs_s_client_currency_i01 on crs_s_client_currency(h_id) tablespace spoindx
/
create index crs_s_client_currency_i02 on crs_s_client_currency(ldts) tablespace spoindx
/
create sequence crs_s_client_currency_seq
/
comment on table crs_s_client_currency is 'Currency satellite (origin: CPI)'
/
comment on column crs_s_client_currency.id is 'Identifier'
/
comment on column crs_s_client_currency.h_id is 'Reference to hub'
/
comment on column crs_s_client_currency.digest is 'Row digest'
/
comment on column crs_s_client_currency.code is 'Character code'
/
comment on column crs_s_client_currency.code_num is 'Numeric code'
/
comment on column crs_s_client_currency.ldts is 'Load date'
/
comment on column crs_s_client_currency.removed is 'Removed flag'
/
create table crs_l_client_cur_name(
    id              number              not null,
    currency_id     number              not null,
    localization_id number              not null,
    removed         number(1) default 0 not null,
    ldts            timestamp           not null,
    constraint crs_l_currency_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_currency_name_fk01 foreign key(currency_id)     references crs_h_client_currency(id),
    constraint crs_l_currency_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_currency_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_cur_name_i01 on crs_l_client_cur_name(currency_id) tablespace spoindx
/
create index crs_l_client_cur_name_i02 on crs_l_client_cur_name(localization_id) tablespace spoindx
/
create index crs_l_client_cur_name_i03 on crs_l_client_cur_name(ldts) tablespace spoindx
/
create sequence crs_l_client_cur_name_seq
/
comment on table crs_l_client_cur_name is 'Currency to multilanguage name link'
/
comment on column crs_l_client_cur_name.currency_id is 'Reference to currency hub  (origin: CPI)'
/
comment on column crs_l_client_cur_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_cur_name.removed is 'Removed flag'
/
comment on column crs_l_client_cur_name.ldts is 'Load date'
/


create table crs_h_client_country(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_country_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_country_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_country_seq
/
comment on table crs_h_client_country is 'Country hub (origin: CPI)'
/
comment on column crs_h_client_country.id is 'Identifier'
/
comment on column crs_h_client_country.key is 'Key, coincides with client portal identifier'
/
comment on column crs_h_client_country.ldts is 'Load date'
/
create table crs_s_client_country(
    id       number              not null,
    h_id     number              not null,
    code_a2  varchar2(10),
    code_a3  varchar2(10),
    code_num number,
    digest   varchar2(100)       not null,
    removed  number(1) default 0 not null,
    ldts     timestamp           not null,
    constraint crs_s_client_country_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_country_ck01 check(removed in (0, 1)),
    constraint crs_s_client_country_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_client_country_fk01 foreign key(h_id) references crs_h_client_country(id)
)
/
create index crs_s_client_country_i01 on crs_s_client_country(h_id) tablespace spoindx
/
create index crs_s_client_country_i02 on crs_s_client_country(ldts) tablespace spoindx
/
create sequence crs_s_client_country_seq
/
comment on table crs_s_client_country is 'Country satellite (origin: CPI)'
/
comment on column crs_s_client_country.id is 'Identifier'
/
comment on column crs_s_client_country.h_id is 'Reference to hub'
/
comment on column crs_s_client_country.code_a2 is 'A2 code'
/
comment on column crs_s_client_country.code_a3 is 'A3 code'
/
comment on column crs_s_client_country.code_num is 'ISO numeric code'
/
comment on column crs_s_client_country.digest is 'Row digest'
/
comment on column crs_s_client_country.ldts is 'Load date'
/
comment on column crs_s_client_country.removed is 'Removed flag'
/
create table crs_l_client_cnt_name(
    id                 number              not null,
    client_country_id  number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_client_cnt_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_cnt_name_fk01 foreign key(client_country_id) references crs_h_client_country(id),
    constraint crs_l_client_cnt_name_fk02 foreign key(localization_id)   references crs_sys_h_localization(id),
    constraint crs_l_client_cnt_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_cnt_name_i01 on crs_l_client_cnt_name(client_country_id) tablespace spoindx
/
create index crs_l_client_cnt_name_i02 on crs_l_client_cnt_name(localization_id) tablespace spoindx
/
create index crs_l_client_cnt_name_i03 on crs_l_client_cnt_name(ldts) tablespace spoindx
/
create sequence crs_l_client_cnt_name_seq
/
comment on table crs_l_client_cnt_name is 'Country to multilanguage name link'
/
comment on column crs_l_client_cnt_name.client_country_id is 'Reference to country hub (origin: CPI)'
/
comment on column crs_l_client_cnt_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_cnt_name.removed is 'Removed flag'
/
comment on column crs_l_client_cnt_name.ldts is 'Load date'
/


create table crs_h_client_indstry(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_indstry_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_indstry_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_indstry_seq
/
comment on table crs_h_client_indstry is 'Industry hub (origin: CPI)'
/
comment on column crs_h_client_indstry.id is 'Identifier'
/
comment on column crs_h_client_indstry.key is 'Key, coincides with client portal identifier'
/
comment on column crs_h_client_indstry.ldts is 'Load date'
/
create table crs_s_client_indstry(
    id       number              not null,
    h_id     number              not null,
    digest   varchar2(100)       not null,
    removed  number(1) default 0 not null,
    ldts     timestamp           not null,
    constraint crs_s_client_indstry_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_indstry_ck01 check(removed in (0, 1)),
    constraint crs_s_client_indstry_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_client_indstry_fk01 foreign key(h_id) references crs_h_client_indstry(id)
)
/
create index crs_s_client_indstry_i01 on crs_s_client_indstry(h_id) tablespace spoindx
/
create index crs_s_client_indstry_i02 on crs_s_client_indstry(ldts) tablespace spoindx
/
create sequence crs_s_client_indstry_seq
/
comment on table crs_s_client_indstry is 'Industry satellite (origin: CPI)'
/
comment on column crs_s_client_indstry.id is 'Identifier'
/
comment on column crs_s_client_indstry.h_id is 'Reference to hub'
/
comment on column crs_s_client_indstry.digest is 'Row digest'
/
comment on column crs_s_client_indstry.ldts is 'Load date'
/
comment on column crs_s_client_indstry.removed is 'Removed flag'
/
create table crs_l_client_ind_name(
    id                 number              not null,
    client_indstry_id  number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_client_ind_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_ind_name_fk01 foreign key(client_indstry_id) references crs_h_client_indstry(id),
    constraint crs_l_client_ind_name_fk02 foreign key(localization_id)    references crs_sys_h_localization(id),
    constraint crs_l_client_ind_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_ind_name_i01 on crs_l_client_ind_name(client_indstry_id) tablespace spoindx
/
create index crs_l_client_ind_name_i02 on crs_l_client_ind_name(localization_id) tablespace spoindx
/
create index crs_l_client_ind_name_i03 on crs_l_client_ind_name(ldts) tablespace spoindx
/
create sequence crs_l_client_ind_name_seq
/
comment on table crs_l_client_ind_name is 'Industry to multilanguage name link'
/
comment on column crs_l_client_ind_name.client_indstry_id is 'Reference to industry hub (origin: CPI)'
/
comment on column crs_l_client_ind_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_ind_name.removed is 'Removed flag'
/
comment on column crs_l_client_ind_name.ldts is 'Load date'
/


create table crs_h_client_opf(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_opf_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_opf_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_opf_seq
/
comment on table crs_h_client_opf is 'OPF hub (origin: CPI)'
/
comment on column crs_h_client_opf.id is 'Identifier'
/
comment on column crs_h_client_opf.key is 'Key, coincides with client portal identifier'
/
comment on column crs_h_client_opf.ldts is 'Load date'
/
create table crs_s_client_opf(
    id       number              not null,
    h_id     number              not null,
    digest   varchar2(100)       not null,
    removed  number(1) default 0 not null,
    ldts     timestamp           not null,
    constraint crs_s_client_opf_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_opf_ck01 check(removed in (0, 1)),
    constraint crs_s_client_opf_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_client_opf_fk01 foreign key(h_id) references crs_h_client_opf(id)
)
/
create index crs_s_client_opf_i01 on crs_s_client_opf(h_id) tablespace spoindx
/
create index crs_s_client_opf_i02 on crs_s_client_opf(ldts) tablespace spoindx
/
create sequence crs_s_client_opf_seq
/
comment on table crs_s_client_opf is 'OPF satellite (origin: CPI)'
/
comment on column crs_s_client_opf.id is 'Identifier'
/
comment on column crs_s_client_opf.h_id is 'Reference to hub'
/
comment on column crs_s_client_opf.digest is 'Row digest'
/
comment on column crs_s_client_opf.ldts is 'Load date'
/
comment on column crs_s_client_opf.removed is 'Removed flag'
/
create table crs_l_client_opf_name(
    id                 number              not null,
    client_opf_id      number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_client_opf_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_opf_name_fk01 foreign key(client_opf_id) references crs_h_client_opf(id),
    constraint crs_l_client_opf_name_fk02 foreign key(localization_id)    references crs_sys_h_localization(id),
    constraint crs_l_client_opf_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_opf_name_i01 on crs_l_client_opf_name(client_opf_id) tablespace spoindx
/
create index crs_l_client_opf_name_i02 on crs_l_client_opf_name(localization_id) tablespace spoindx
/
create index crs_l_client_opf_name_i03 on crs_l_client_opf_name(ldts) tablespace spoindx
/
create sequence crs_l_client_opf_name_seq
/
comment on table crs_l_client_opf_name is 'OPF to multilanguage name link'
/
comment on column crs_l_client_opf_name.client_opf_id is 'Reference to OPF hub (origin: CPI)'
/
comment on column crs_l_client_opf_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_opf_name.removed is 'Removed flag'
/
comment on column crs_l_client_opf_name.ldts is 'Load date'
/
create table crs_l_client_opf_country(
    id            number              not null,
    client_opf_id number              not null,
    country_id    number              not null,
    removed       number(1) default 0 not null,
    ldts          timestamp           not null,
    constraint crs_l_client_opf_country_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_opf_country_fk01 foreign key(client_opf_id) references crs_h_client_opf(id),
    constraint crs_l_client_opf_country_fk02 foreign key(country_id)    references crs_h_client_country(id),
    constraint crs_l_client_opf_country_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_opf_country_i01 on crs_l_client_opf_country(client_opf_id) tablespace spoindx
/
create index crs_l_client_opf_country_i02 on crs_l_client_opf_country(country_id) tablespace spoindx
/
create index crs_l_client_opf_country_i03 on crs_l_client_opf_country(ldts) tablespace spoindx
/
create sequence crs_l_client_opf_country_seq
/
comment on table crs_l_client_opf_country is 'OPF to country link'
/
comment on column crs_l_client_opf_country.client_opf_id is 'Reference to OPF hub (origin: CPI)'
/
comment on column crs_l_client_opf_country.country_id is 'Reference to country hub'
/
comment on column crs_l_client_opf_country.removed is 'Removed flag'
/
comment on column crs_l_client_opf_country.ldts is 'Load date'
/


create table crs_h_client_ogrn(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_ogrn_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_ogrn_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_ogrn_seq
/
comment on table crs_h_client_ogrn is 'OGRN hub (origin: CPI)'
/
comment on column crs_h_client_ogrn.id is 'Identifier'
/
comment on column crs_h_client_ogrn.key is 'Key'
/
comment on column crs_h_client_ogrn.ldts is 'Load date'
/
create table crs_s_client_ogrn(
    id       number              not null,
    h_id     number              not null,
    reg_num  varchar2(64)        not null,
    digest   varchar2(100)       not null,
    removed  number(1) default 0 not null,
    ldts     timestamp           not null,
    constraint crs_s_client_ogrn_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_ogrn_ck01 check(removed in (0, 1)),
    constraint crs_s_client_ogrn_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_client_ogrn_fk01 foreign key(h_id) references crs_h_client_ogrn(id)
)
/
create index crs_s_client_ogrn_i01 on crs_s_client_ogrn(h_id) tablespace spoindx
/
create index crs_s_client_ogrn_i02 on crs_s_client_ogrn(ldts) tablespace spoindx
/
create sequence crs_s_client_ogrn_seq
/
comment on table crs_s_client_ogrn is 'OGRN satellite (origin: CPI)'
/
comment on column crs_s_client_ogrn.id is 'Identifier'
/
comment on column crs_s_client_ogrn.h_id is 'Reference to hub'
/
comment on column crs_s_client_ogrn.reg_num is 'Registration number'
/
comment on column crs_s_client_ogrn.digest is 'Row digest'
/
comment on column crs_s_client_ogrn.ldts is 'Load date'
/
comment on column crs_s_client_ogrn.removed is 'Removed flag'
/
create table crs_l_client_ogrn_country(
    id             number              not null,
    client_ogrn_id number              not null,
    country_id     number              not null,
    removed        number(1) default 0 not null,
    ldts           timestamp           not null,
    constraint crs_l_client_ogrn_country_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_ogrn_country_fk01 foreign key(client_ogrn_id) references crs_h_client_ogrn(id),
    constraint crs_l_client_ogrn_country_fk02 foreign key(country_id)     references crs_h_client_country(id),
    constraint crs_l_client_ogrn_country_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_ogrn_country_i01 on crs_l_client_ogrn_country(client_ogrn_id) tablespace spoindx
/
create index crs_l_client_ogrn_country_i02 on crs_l_client_ogrn_country(country_id) tablespace spoindx
/
create index crs_l_client_ogrn_country_i03 on crs_l_client_ogrn_country(ldts) tablespace spoindx
/
create sequence crs_l_client_ogrn_country_seq
/
comment on table crs_l_client_ogrn_country is 'OGRN to country link'
/
comment on column crs_l_client_ogrn_country.client_ogrn_id is 'Reference to OGRN hub (origin: CPI)'
/
comment on column crs_l_client_ogrn_country.country_id is 'Reference to country hub'
/
comment on column crs_l_client_ogrn_country.removed is 'Removed flag'
/
comment on column crs_l_client_ogrn_country.ldts is 'Load date'
/


create table crs_h_client_segm(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_segm_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_segm_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_segm_seq
/
comment on table crs_h_client_segm is 'Segment hub (origin: CPI)'
/
comment on column crs_h_client_segm.id is 'Identifier'
/
comment on column crs_h_client_segm.key is 'Key, coincides with client portal identifier'
/
comment on column crs_h_client_segm.ldts is 'Load date'
/
create table crs_s_client_segm(
    id          number              not null,
    h_id        number              not null,
    revenue_min number,
    revenue_max number,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    constraint crs_s_client_segm_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_segm_ck01 check(removed in (0, 1)),
    constraint crs_s_client_segm_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_client_segm_fk01 foreign key(h_id) references crs_h_client_segm(id)
)
/
create index crs_s_client_segm_i01 on crs_s_client_segm(h_id) tablespace spoindx
/
create index crs_s_client_segm_i02 on crs_s_client_segm(ldts) tablespace spoindx
/
create sequence crs_s_client_segm_seq
/
comment on table crs_s_client_segm is 'Segment satellite (origin: CPI)'
/
comment on column crs_s_client_segm.id is 'Identifier'
/
comment on column crs_s_client_segm.h_id is 'Reference to hub'
/
comment on column crs_s_client_segm.revenue_min is 'Minimum revenue value'
/
comment on column crs_s_client_segm.revenue_max is 'Maximum revenue value'
/
comment on column crs_s_client_segm.digest is 'Row digest'
/
comment on column crs_s_client_segm.ldts is 'Load date'
/
comment on column crs_s_client_segm.removed is 'Removed flag'
/
create table crs_l_client_segm_name(
    id                 number              not null,
    client_segm_id     number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_client_segm_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_segm_name_fk01 foreign key(client_segm_id) references crs_h_client_segm(id),
    constraint crs_l_client_segm_name_fk02 foreign key(localization_id)    references crs_sys_h_localization(id),
    constraint crs_l_client_segm_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_segm_name_i01 on crs_l_client_segm_name(client_segm_id) tablespace spoindx
/
create index crs_l_client_segm_name_i02 on crs_l_client_segm_name(localization_id) tablespace spoindx
/
create index crs_l_client_segm_name_i03 on crs_l_client_segm_name(ldts) tablespace spoindx
/
create sequence crs_l_client_segm_name_seq
/
comment on table crs_l_client_segm_name is 'Segment to multilanguage name link'
/
comment on column crs_l_client_segm_name.client_segm_id is 'Reference to segment hub (origin: CPI)'
/
comment on column crs_l_client_segm_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_segm_name.removed is 'Removed flag'
/
comment on column crs_l_client_segm_name.ldts is 'Load date'
/
create table crs_l_client_segm_curr(
    id             number              not null,
    client_segm_id number              not null,
    currency_id    number              not null,
    removed        number(1) default 0 not null,
    ldts           timestamp           not null,
    constraint crs_l_client_segm_curr_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_segm_curr_fk01 foreign key(client_segm_id) references crs_h_client_segm(id),
    constraint crs_l_client_segm_curr_fk02 foreign key(currency_id)    references crs_h_client_currency(id),
    constraint crs_l_client_segm_curr_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_segm_curr_i01 on crs_l_client_segm_curr(client_segm_id) tablespace spoindx
/
create index crs_l_client_segm_curr_i02 on crs_l_client_segm_curr(currency_id) tablespace spoindx
/
create index crs_l_client_segm_curr_i03 on crs_l_client_segm_curr(ldts) tablespace spoindx
/
create sequence crs_l_client_segm_curr_seq
/
comment on table crs_l_client_segm_curr is 'Segment to currency link'
/
comment on column crs_l_client_segm_curr.client_segm_id is 'Reference to segment hub (origin: CPI)'
/
comment on column crs_l_client_segm_curr.currency_id is 'Reference to currency hub'
/
comment on column crs_l_client_segm_curr.removed is 'Removed flag'
/
comment on column crs_l_client_segm_curr.ldts is 'Load date'
/


drop table crs_l_client_type_name
/
drop table crs_l_client_type
/
drop table crs_s_client_type
/
drop table crs_h_client_type
/
drop sequence crs_l_client_type_name_seq
/
drop sequence crs_l_client_type_seq
/
drop sequence crs_s_client_type_seq
/
drop sequence crs_h_client_type_seq
/


create table crs_h_client_type(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_type_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_type_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_type_seq
/
comment on table crs_h_client_type is 'Client type hub (origin: CPI)'
/
comment on column crs_h_client_type.id is 'Identifier'
/
comment on column crs_h_client_type.key is 'Key, coincides with client portal identifier'
/
comment on column crs_h_client_type.ldts is 'Load date'
/
create table crs_s_client_type(
    id          number              not null,
    h_id        number              not null,
    priority    number,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    constraint crs_s_client_type_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_type_ck01 check(removed in (0, 1)),
    constraint crs_s_client_type_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_client_type_fk01 foreign key(h_id) references crs_h_client_type(id)
)
/
create index crs_s_client_type_i01 on crs_s_client_type(h_id) tablespace spoindx
/
create index crs_s_client_type_i02 on crs_s_client_type(ldts) tablespace spoindx
/
create sequence crs_s_client_type_seq
/
comment on table crs_s_client_type is 'Client type satellite (origin: CPI)'
/
comment on column crs_s_client_type.id is 'Identifier'
/
comment on column crs_s_client_type.h_id is 'Reference to hub'
/
comment on column crs_s_client_type.priority is 'UI sort priority'
/
comment on column crs_s_client_type.digest is 'Row digest'
/
comment on column crs_s_client_type.ldts is 'Load date'
/
comment on column crs_s_client_type.removed is 'Removed flag'
/
create table crs_l_client_type_name(
    id                 number              not null,
    client_type_id     number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_client_type_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_type_name_fk01 foreign key(client_type_id) references crs_h_client_type(id),
    constraint crs_l_client_type_name_fk02 foreign key(localization_id)    references crs_sys_h_localization(id),
    constraint crs_l_client_type_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_type_name_i01 on crs_l_client_type_name(client_type_id) tablespace spoindx
/
create index crs_l_client_type_name_i02 on crs_l_client_type_name(localization_id) tablespace spoindx
/
create index crs_l_client_type_name_i03 on crs_l_client_type_name(ldts) tablespace spoindx
/
create sequence crs_l_client_type_name_seq
/
comment on table crs_l_client_type_name is 'Client type to multilanguage name link'
/
comment on column crs_l_client_type_name.client_type_id is 'Reference to client type hub (origin: CPI)'
/
comment on column crs_l_client_type_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_type_name.removed is 'Removed flag'
/
comment on column crs_l_client_type_name.ldts is 'Load date'
/
create table crs_l_client_type (
    id                 number not null,
    ldts               timestamp not null,
    removed            number(1) not null,
    client_id          number not null,
    client_type_id     number not null,
    constraint crs_l_client_type_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_type_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_type_fk02 foreign key(client_type_id) references crs_h_client_type(id)
)
/
create index crs_l_client_type_i01 on crs_l_client_type(client_id) tablespace spoindx
/
create index crs_l_client_type_i02 on crs_l_client_type(client_type_id)  tablespace spoindx
/
create index crs_l_client_type_i03 on crs_l_client_type(ldts) tablespace spoindx
/
create sequence crs_l_client_type_seq
/
comment on table crs_l_client_type is 'Client to client type link'
/
comment on column crs_l_client_type.id is 'Identifier'
/
comment on column crs_l_client_type.client_id is 'Reference to client hub (origin: CPI)'
/
comment on column crs_l_client_type.client_type_id is 'Reference to client type (origin: CPI)'
/
comment on column crs_l_client_type.ldts is 'Load date'
/

--changeset achalov:crs-1.0-VTBCRS-397-meta logicalFilePath:crs-1.0-VTBCRS-397-meta endDelimiter:/
declare
    v_entity_type_id number;
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    function insert_entity(
        p_entity_type_id crs_sys_h_entity.id%type,
        p_entity_key     crs_sys_h_entity.key%type,
        p_entity_name_ru crs_sys_s_entity.name_ru%type,
        p_entity_name_en crs_sys_s_entity.name_en%type,
        p_ldts           crs_sys_h_entity.ldts%type
    ) return number is
    begin
        insert into crs_sys_h_entity(id, key, ldts)
        values(crs_sys_h_entity_seq.nextval, p_entity_key, p_ldts);

        insert into crs_sys_s_entity(id, h_id, ldts, name_ru, name_en)
        values (crs_sys_s_entity_seq.nextval, crs_sys_h_entity_seq.currval, p_ldts, p_entity_name_ru, p_entity_name_en);

        insert into crs_sys_l_entity_type(id, type_id, entity_id, ldts)
        values(crs_sys_l_entity_type_seq.nextval, p_entity_type_id, crs_sys_h_entity_seq.currval, p_ldts);

        return crs_sys_h_entity_seq.currval;
    end;

    procedure insert_entity_attribute(
        p_entity_hub_id     crs_sys_h_entity.id%type,
        p_entity_key        crs_sys_h_entity.key%type,
        p_attribute_key     varchar2,
        p_attribute_name_ru crs_sys_s_attribute.name_ru%type,
        p_attribute_name_en crs_sys_s_attribute.name_en%type,
        p_multilang         crs_sys_s_attribute.multilang%type,
        p_link_table        crs_sys_s_attribute.link_table%type,
        p_attribute_type    crs_sys_s_attribute.type%type,
        p_native_column     crs_sys_s_attribute.native_column%type,
        p_ref_entity_key    crs_sys_s_attribute.entity_key%type,
        p_ref_attribute_key crs_sys_s_attribute.attribute_key%type,
        p_ldts              crs_sys_h_attribute.ldts%type
    ) is
    begin
        insert into crs_sys_h_attribute(id, key, ldts)
        values(crs_sys_h_attribute_seq.nextval, p_entity_key || '#' || p_attribute_key, p_ldts);

        insert into crs_sys_s_attribute(
            id, h_id, ldts, multilang, link_table,
            name_ru, name_en, type, native_column, entity_key,
            attribute_key
        )
        values(
            crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_multilang, p_link_table,
            p_attribute_name_ru, p_attribute_name_en, p_attribute_type, p_native_column, p_ref_entity_key,
            case
                 when p_ref_entity_key is not null and p_ref_attribute_key is not null
                      then p_ref_entity_key || '#' || p_ref_attribute_key
                 else
                      null
            end
        );

        insert into crs_sys_l_entity_attribute(id, entity_id, attribute_id, ldts)
        values(crs_sys_l_entity_attribute_seq.nextval, p_entity_hub_id, crs_sys_h_attribute_seq.currval, p_ldts);
    end;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    --client category
    v_entity_key := 'CLIENT_CATEGORY';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Категория клиента', 'Client category', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 1, 'CRS_L_CLIENT_CAT_NAME', 'STRING', null, null, null, v_ldts);

    --client currency
    v_entity_key := 'CLIENT_CURRENCY';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Валюта', 'Currency', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 1, 'CRS_L_CLIENT_CUR_NAME', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CODE', 'Буквенный код', 'Literal code', 0, null, 'STRING', 'CODE', null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CODE_NUM', 'Цифровой код', 'Numeric code', 0, null, 'NUMBER', 'CODE_NUM', null, null, v_ldts);

    --client country
    v_entity_key := 'CLIENT_COUNTRY';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Страна', 'Country', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 1, 'CRS_L_CLIENT_CNT_NAME', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CODE_A2', 'A2 код', 'A2 code', 0, null, 'STRING', 'CODE_A2', null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CODE_A3', 'A3 код', 'A3 code', 0, null, 'STRING', 'CODE_A3', null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CODE_NUM', 'Цифровой код', 'Numeric code', 0, null, 'NUMBER', 'CODE_NUM', null, null, v_ldts);

    --client industry
    v_entity_key := 'CLIENT_INDSTRY';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Отрасль', 'Industry', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 1, 'CRS_L_CLIENT_IND_NAME', 'STRING', null, null, null, v_ldts);

    --client legal form of business
    v_entity_key := 'CLIENT_OPF';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Организационно правовая форма', 'Legal form of business', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 1, 'CRS_L_CLIENT_OPF_NAME', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'COUNTRY', 'Страна', 'Country', 0, 'CRS_L_CLIENT_OPF_COUNTRY', 'STRING', null, 'CLIENT_COUNTRY', 'NAME', v_ldts);

    --client registration number
    v_entity_key := 'CLIENT_OGRN';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Основной государственный регистрационный номер', 'Primary state registration number', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'REG_NUM', 'Регистрационный номер', 'Registration number', 0, null, 'STRING', 'REG_NUM', null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'COUNTRY', 'Страна', 'Country', 0, 'CRS_L_CLIENT_OGRN_COUNTRY', 'STRING', null, 'CLIENT_COUNTRY', 'NAME', v_ldts);

    --client segment
    v_entity_key := 'CLIENT_SEGM';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Сегмент', 'Segment', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 1, 'CRS_L_CLIENT_SEGM_NAME', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'REVENUE_MIN', 'Минимальное значение выручки', 'Minimum revenue value', 0, null, 'NUMBER', 'REVENUE_MIN', null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'REVENUE_MAX', 'Максимальное значение выручки', 'Maximum revenue value', 0, null, 'NUMBER', 'REVENUE_MAX', null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CURRENCY', 'Валюта', 'Currency', 0, 'CRS_L_CLIENT_SEGM_CURR', 'STRING', null, 'CLIENT_CURRENCY', 'NAME', v_ldts);

    --client type
    v_entity_key := 'CLIENT_TYPE';
    select id into v_entity_hub_id
      from crs_sys_h_entity
     where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'PRIORITY', 'Приоритет сортировки на интерфейсе', 'User interface sort priority', 0, null, 'NUMBER', 'PRIORITY', null, null, v_ldts);
end;
/

--changeset achalov:crs-1.0-VTBCRS-398-rename logicalFilePath:crs-1.0-VTBCRS-398-rename endDelimiter:/
alter table crs_l_client_cur_name rename column currency_id to client_currency_id
/
alter table crs_l_client_segm_curr rename column currency_id to client_currency_id
/
alter table crs_l_client_opf_country rename column country_id to client_country_id
/
alter table crs_l_client_ogrn_country rename column country_id to client_country_id
/
create table crs_l_client_inn_country(
    id                number              not null,
    client_inn_id     number              not null,
    client_country_id number              not null,
    removed           number(1) default 0 not null,
    ldts              timestamp           not null,
    constraint crs_l_client_inn_country_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_inn_country_fk01 foreign key(client_inn_id)     references crs_h_client_inn(id),
    constraint crs_l_client_inn_country_fk02 foreign key(client_country_id) references crs_h_client_country(id),
    constraint crs_l_client_inn_country_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_inn_country_i01 on crs_l_client_inn_country(client_inn_id) tablespace spoindx
/
create index crs_l_client_inn_country_i02 on crs_l_client_inn_country(client_country_id) tablespace spoindx
/
create index crs_l_client_inn_country_i03 on crs_l_client_inn_country(ldts) tablespace spoindx
/
create sequence crs_l_client_inn_country_seq
/
comment on table crs_l_client_inn_country is 'INN to country link'
/
comment on column crs_l_client_inn_country.client_inn_id is 'Reference to INN hub (origin: CPI)'
/
comment on column crs_l_client_inn_country.client_country_id is 'Reference to country hub'
/
comment on column crs_l_client_inn_country.removed is 'Removed flag'
/
comment on column crs_l_client_inn_country.ldts is 'Load date'
/
insert into crs_sys_h_attribute(id, key, ldts)
values(crs_sys_h_attribute_seq.nextval, 'CLIENT_INN#COUNTRY', to_timestamp('011111', 'mmyyyy'))
/
insert into crs_sys_s_attribute(id, h_id, link_table, attribute_key, name_ru, name_en, entity_key, type, ldts)
values(crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, 'CRS_L_CLIENT_INN_COUNTRY', 'CLIENT_COUNTRY#NAME',
       'Страна', 'Country', 'CLIENT_COUNTRY', 'REFERENCE', to_timestamp('011111', 'mmyyyy'))
/
insert into crs_sys_l_entity_attribute(id, entity_id, attribute_id, ldts)
select crs_sys_l_entity_attribute_seq.nextval, e.id, a.id, to_timestamp('011111', 'mmyyyy')
  from crs_sys_h_entity e, crs_sys_h_attribute a
 where e.key = 'CLIENT_INN'
   and a.key = 'CLIENT_INN#COUNTRY'
/
update crs_sys_s_attribute
   set type = 'REFERENCE'
 where link_table in ('CRS_L_CLIENT_OPF_COUNTRY', 'CRS_L_CLIENT_OGRN_COUNTRY', 'CRS_L_CLIENT_SEGM_CURR', 'CRS_L_CLIENT_INN_COUNTRY')
/

--changeset akirilchev:crs-1.0-VTBCRS-399-rename-industry logicalFilePath:crs-1.0-VTBCRS-399-rename-industry endDelimiter:/
update crs_sys_h_entity set key  = 'CLIENT_INDUSTRY' where key = 'CLIENT_INDSTRY'
/
update crs_sys_h_attribute set key  = 'CLIENT_INDUSTRY#NAME' where key = 'CLIENT_INDSTRY#NAME'
/
rename crs_h_client_indstry to crs_h_client_industry
/
rename crs_s_client_indstry to crs_s_client_industry
/
rename crs_h_client_indstry_seq to crs_h_client_industry_seq
/
rename crs_s_client_indstry_seq to crs_s_client_industry_seq
/
alter index crs_s_client_indstry_i01 rename to crs_s_client_industry_i01
/
alter index crs_s_client_indstry_i02 rename to crs_s_client_industry_i02
/
alter table crs_h_client_industry rename constraint crs_h_client_indstry_pk to crs_h_client_industry_pk
/
alter table crs_h_client_industry rename constraint crs_h_client_indstry_uk01 to crs_h_client_industry_uk01
/
alter table crs_s_client_industry rename constraint crs_s_client_indstry_pk to crs_s_client_industry_pk
/
alter table crs_s_client_industry rename constraint crs_s_client_indstry_ck01 to crs_s_client_industry_ck01
/
alter table crs_s_client_industry rename constraint crs_s_client_indstry_uk01 to crs_s_client_industry_uk01
/
alter table crs_s_client_industry rename constraint crs_s_client_indstry_fk01 to crs_s_client_industry_fk01
/
alter table crs_l_client_ind_name rename column client_indstry_id to client_industry_id
/

--changeset akirilchev:crs-1.0-VTBCRS-399-rename-segment logicalFilePath:crs-1.0-VTBCRS-399-rename-segment endDelimiter:/
update crs_sys_h_entity set key = 'CLIENT_SEGMENT' where key = 'CLIENT_SEGM'
/
update crs_sys_h_attribute set key = 'CLIENT_SEGMENT#NAME' where key = 'CLIENT_SEGM#NAME'
/
update crs_sys_h_attribute set key = 'CLIENT_SEGMENT#REVENUE_MIN' where key = 'CLIENT_SEGM#REVENUE_MIN'
/
update crs_sys_h_attribute set key = 'CLIENT_SEGMENT#REVENUE_MAX' where key = 'CLIENT_SEGM#REVENUE_MAX'
/
update crs_sys_h_attribute set key = 'CLIENT_SEGMENT#CURRENCY' where key = 'CLIENT_SEGM#CURRENCY'
/
rename crs_h_client_segm to crs_h_client_segment
/
rename crs_s_client_segm to crs_s_client_segment
/
rename crs_h_client_segm_seq to crs_h_client_segment_seq
/
rename crs_s_client_segm_seq to crs_s_client_segment_seq
/
alter table crs_h_client_segment rename constraint crs_h_client_segm_pk to crs_h_client_segment_pk
/
alter table crs_h_client_segment rename constraint crs_h_client_segm_uk01 to crs_h_client_segment_uk01
/
alter table crs_s_client_segment rename constraint crs_s_client_segm_pk to crs_s_client_segment_pk
/
alter table crs_s_client_segment rename constraint crs_s_client_segm_ck01 to crs_s_client_segment_ck01
/
alter table crs_s_client_segment rename constraint crs_s_client_segm_uk01 to crs_s_client_segment_uk01
/
alter table crs_s_client_segment rename constraint crs_s_client_segm_fk01 to crs_s_client_segment_fk01
/
alter index crs_s_client_segm_i01 rename to crs_s_client_segment_i01
/
alter index crs_s_client_segm_i02 rename to crs_s_client_segment_i02
/
alter table crs_l_client_segm_name rename column client_segm_id to client_segment_id
/
alter table crs_l_client_segm_curr rename column client_segm_id to client_segment_id
/

--changeset akirilchev:crs-1.0-VTBCRS-399 logicalFilePath:crs-1.0-VTBCRS-399 endDelimiter:/
alter table crs_l_client_grp_segment add client_segment_id number not null
/
comment on column crs_l_client_grp_segment.client_segment_id is 'Reference to segment hub'
/
alter table crs_l_client_grp_segment add constraint crs_l_client_grp_segment_fk02 foreign key (client_segment_id) references crs_h_client_segment(id)
/
create index crs_l_client_grp_segment_i02 on crs_l_client_grp_segment(client_segment_id) tablespace spoindx
/
alter table crs_l_client_grp_industry add client_industry_id number not null
/
comment on column crs_l_client_grp_industry.client_industry_id is 'Reference to industry hub'
/
alter table crs_l_client_grp_industry add constraint crs_l_client_grp_industry_fk02 foreign key (client_industry_id) references crs_h_client_industry(id)
/
create index crs_l_client_grp_industry_i02 on crs_l_client_grp_industry(client_industry_id) tablespace spoindx
/
rename crs_l_client_grp_reg_cntr to crs_l_client_grp_country
/
alter table crs_l_client_grp_country add client_country_id number not null
/
comment on column crs_l_client_grp_country.client_country_id is 'Reference to country hub'
/
alter table crs_l_client_grp_country add constraint crs_l_client_grp_country_fk02 foreign key(client_country_id) references crs_h_client_country(id)
/
create index crs_l_client_grp_country_i02 on crs_l_client_grp_country(client_country_id) tablespace spoindx
/
rename crs_l_client_reg_country_seq to crs_l_client_grp_country_seq
/

--changeset akirilchev:crs-1.0-VTBCRS-399-attr logicalFilePath:crs-1.0-VTBCRS-399-attr endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT_GROUP';

    -- reference attribute CLIENT_GROUP#SEGMENT
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT_GROUP#SEGMENT',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_GRP_SEGMENT', 'Сегмент', 'Segment',
            'CLIENT_SEGMENT', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT_GROUP';

    -- reference attribute CLIENT_GROUP#INDUSTRY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT_GROUP#INDUSTRY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_GRP_INDUSTRY', 'Отрасль', 'Industry',
            'CLIENT_INDUSTRY', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT_GROUP';

    -- reference attribute CLIENT_GROUP#COUNTRY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT_GROUP#COUNTRY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_GRP_COUNTRY', 'Страна', 'Country',
            'CLIENT_COUNTRY', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-399-link-table-attr logicalFilePath:crs-1.0-VTBCRS-399-link-table-attr endDelimiter:/
update crs_sys_s_entity e
   set e.link_table = null
 where e.link_table = 'null'
/

--changeset akirilchev:crs-1.0-VTBCRS-400 logicalFilePath:crs-1.0-VTBCRS-400 endDelimiter:/
create table crs_l_client_ogrn (
    id                 number not null,
    ldts               timestamp not null,
    removed            number(1) not null,
    client_id          number not null,
    client_ogrn_id     number not null,
    constraint crs_l_client_ogrn_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_client_ogrn_fk01 foreign key(client_id) references crs_h_client(id),
    constraint crs_l_client_ogrn_fk02 foreign key(client_ogrn_id) references crs_h_client_ogrn(id)
)
/
create index crs_l_client_ogrn_i01 on crs_l_client_ogrn(client_id) tablespace spoindx
/
create index crs_l_client_ogrn_i02 on crs_l_client_ogrn(client_ogrn_id)  tablespace spoindx
/
create index crs_l_client_ogrn_i03 on crs_l_client_ogrn(ldts) tablespace spoindx
/
create sequence crs_l_client_ogrn_seq
/
comment on table crs_l_client_ogrn is 'Client to client ogrn link'
/
comment on column crs_l_client_ogrn.id is 'Identifier'
/
comment on column crs_l_client_ogrn.client_id is 'Reference to client hub'
/
comment on column crs_l_client_ogrn.client_ogrn_id is 'Reference to client ogrn hub'
/
comment on column crs_l_client_ogrn.ldts is 'Load date'
/

--changeset akirilchev:crs-1.0-VTBCRS-400-ref-attr logicalFilePath:crs-1.0-VTBCRS-400-ref-attr endDelimiter:/
delete crs_l_client_opf
/
delete crs_l_client_category
/
delete crs_l_client_segment
/
delete crs_l_client_industry
/
alter table crs_l_client_opf add client_opf_id number not null
/
comment on column crs_l_client_opf.client_opf_id is 'Reference to opf'
/
alter table crs_l_client_opf add constraint crs_l_client_opf_fk02 foreign key(client_opf_id) references crs_h_client_opf(id)
/
alter table crs_l_client_category add client_category_id number not null
/
comment on column crs_l_client_category.client_category_id is 'Reference to category'
/
alter table crs_l_client_category add constraint crs_l_client_category_fk02 foreign key(client_category_id) references crs_h_client_category(id)
/
alter table crs_l_client_segment add client_segment_id number not null
/
comment on column crs_l_client_segment.client_segment_id is 'Reference to segment'
/
alter table crs_l_client_segment add constraint crs_l_client_segment_fk02 foreign key(client_segment_id) references crs_h_client_segment(id)
/
alter table crs_l_client_industry add client_industry_id number not null
/
comment on column crs_l_client_industry.client_industry_id is 'Reference to industry'
/
alter table crs_l_client_industry add constraint crs_l_client_industry_fk02 foreign key(client_industry_id) references crs_h_client_industry(id)
/
rename crs_l_client_reg_country to crs_l_client_country
/
create sequence crs_l_client_country_seq
/
delete crs_l_client_country
/
alter index crs_l_client_reg_country_pk rename to crs_l_client_country_pk
/
alter table crs_l_client_country rename constraint crs_l_client_reg_country_pk to crs_l_client_country_pk
/
alter table crs_l_client_country rename constraint crs_l_client_reg_country_fk01 to crs_l_client_country_fk01
/
alter table crs_l_client_country add client_country_id number not null
/
alter table crs_l_client_country add constraint crs_l_client_country_fk02 foreign key (client_country_id) references crs_h_client_country (id)
/
comment on column crs_l_client_country.client_country_id is 'Reference to country'
/
create index crs_l_client_opf_i02 on crs_l_client_opf(client_opf_id) tablespace spoindx
/
create index crs_l_client_category_i02 on crs_l_client_category(client_category_id) tablespace spoindx
/
create index crs_l_client_segment_i02 on crs_l_client_segment(client_segment_id) tablespace spoindx
/
create index crs_l_client_industry_i02 on crs_l_client_industry(client_industry_id) tablespace spoindx
/
create index crs_l_client_country_i02 on crs_l_client_country(client_country_id) tablespace spoindx
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT';

    -- reference attribute CLIENT#OGRN
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#OGRN',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_OGRN', 'ОГРН', 'PSRN',
            'CLIENT_OGRN', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT';

    -- reference attribute CLIENT#OPF
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#OPF',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_OPF', 'ОПФ', 'Type of business',
            'CLIENT_OPF', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT';

    -- reference attribute CLIENT#CATEGORY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#CATEGORY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_CATEGORY', 'Категория клиента', 'Client category',
            'CLIENT_CATEGORY', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT';

    -- reference attribute CLIENT#SEGMENT
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#SEGMENT',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_SEGMENT', 'Сегмент', 'Segment',
            'CLIENT_SEGMENT', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT';

    -- reference attribute CLIENT#INDUSTRY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#INDUSTRY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_INDUSTRY', 'Отрасль', 'Industry',
            'CLIENT_INDUSTRY', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT';

    -- reference attribute CLIENT#COUNTRY
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT#COUNTRY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_COUNTRY', 'Страна', 'Country',
            'CLIENT_COUNTRY', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-462 logicalFilePath:crs-1.0-VTBCRS-462 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'CLIENT_GROUP';

    -- reference attribute CLIENT_GROUP#CLIENT
    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CLIENT_GROUP#CLIENT',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, null,
            'CRS_L_CLIENT_GROUP', 'Клиент', 'Client',
            'CLIENT', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-436 logicalFilePath:crs-1.0-VTBCRS-436 endDelimiter:/
alter table crs_l_calc_formula_result add calc_profile_id number
/
comment on column crs_l_calc_formula_result.calc_profile_id is 'Reference to calculation profile'
/

--changeset pmasalov:crs-1.0-VTBCRS-436-update-profile logicalFilePath:crs-1.0-VTBCRS-436-update-profile endDelimiter:/
declare
    v_rated_h_id number;
begin
    select id into v_rated_h_id from crs_h_calc_profile where key = 'RATED';
    update crs_l_calc_formula_result set calc_profile_id = v_rated_h_id where calc_profile_id is null;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-436-add-constraint logicalFilePath:crs-1.0-VTBCRS-436-add-constraint endDelimiter:/
alter table crs_l_calc_formula_result modify calc_profile_id not null
/

create index crs_l_calc_formula_result_i04 on crs_l_calc_formula_result (calc_profile_id, ldts) tablespace spoindx
/

alter table crs_l_calc_formula_result
  add constraint crs_l_calc_formula_result_fk04 foreign key (calc_profile_id)
references crs_h_calc_profile (id)
/

--changeset pmasalov:crs-1.0-VTBCRS-436-triple-object logicalFilePath:crs-1.0-VTBCRS-436-triple-object endDelimiter:/
create or replace type crs_triple_number force as object (n1 number, n2 number, n3 number)
/
create or replace type crs_triple_number_a as table of crs_triple_number
/

--changeset svaliev:crs-1.0-type_id-to-entity_type_id logicalFilePath:crs-1.0-type_id-to-entity_type_id endDelimiter:/
alter table crs_sys_l_entity_type rename column type_id to entity_type_id
/

--changeset akirilchev:crs-1.0-VTBCRS-462-nullable logicalFilePath:crs-1.0-VTBCRS-462-nullable endDelimiter:/
alter table crs_s_client_ogrn modify reg_num null
/

--changeset akirilchev:crs-1.0-VTBCRS-462-attr-text logicalFilePath:crs-1.0-VTBCRS-462-attr-text endDelimiter:/
update crs_sys_s_attribute
   set type = 'TEXT'
where h_id in (select id
                 from crs_sys_h_attribute
                where key = 'CLIENT_GROUP#DESCRIPTION')
/
--changeset pmasalov:crs-1.0-VTBCRS-406 logicalFilePath:crs-1.0-VTBCRS-406 endDelimiter:/
create table crs_sys_h_entity_group
(
    id number not null,
    key varchar2(100 char) not null,
    ldts timestamp not null,
    constraint crs_sys_h_entity_group_ck01 check (key = upper(trim(key))),
    constraint crs_sys_h_entity_group_pk primary key (id) using index tablespace spoindx,
    constraint crs_sys_h_entity_group_uk01 unique (key) using index tablespace spoindx
)
/
comment on table crs_sys_h_entity_group is 'Entity group hub'
/
comment on column crs_sys_h_entity_group.id is 'Identifier'
/
comment on column crs_sys_h_entity_group.key is 'Key'
/
comment on column crs_sys_h_entity_group.ldts is 'Load date'
/

--changeset pmasalov:crs-1.0-VTBCRS-406-satellite logicalFilePath:crs-1.0-VTBCRS-406-satellite endDelimiter:/
create table crs_sys_s_entity_group
(
    id number not null,
    h_id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) not null,
    digest varchar2(100 char) not null,
    name_ru varchar2(4000) not null,
    name_en varchar2(4000) not null,
    view_order number default 0 not null,
    constraint crs_sys_s_entity_group_pk primary key (id) using index tablespace spoindx,
    constraint crs_sys_s_entity_group_ck01 check (removed in (0, 1)),
    constraint crs_sys_s_entity_group_uk01 unique (h_id, ldts) using index compress 1 tablespace spoindx,
    constraint crs_sys_s_entity_group_fk01 foreign key (h_id) references crs_sys_h_entity_group (id)
)
/
comment on table crs_sys_s_entity_group is 'Entity group satellite'
/
comment on column crs_sys_s_entity_group.id is 'Identifier'
/
comment on column crs_sys_s_entity_group.h_id is 'Reference to hub'
/
comment on column crs_sys_s_entity_group.ldts is 'Load date'
/
comment on column crs_sys_s_entity_group.removed is 'Removed flag'
/
comment on column crs_sys_s_entity_group.digest is 'Row digest'
/
comment on column crs_sys_s_entity_group.name_ru is 'Entity group russian name'
/
comment on column crs_sys_s_entity_group.name_en is 'Entity group english name'
/
comment on column crs_sys_s_entity_group.view_order is 'Entity group sort order to view in list'
/

--changeset pmasalov:crs-1.0-VTBCRS-406-eg-sequences logicalFilePath:crs-1.0-VTBCRS-406-eg-sequences endDelimiter:/
create sequence crs_sys_h_entity_group_seq
/
create sequence crs_sys_s_entity_group_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-406-eg-type logicalFilePath:crs-1.0-VTBCRS-406-eg-type endDelimiter:/
create table crs_l_entity_group_type
(
    id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) default 0 not null,
    entity_group_id number not null,
    entity_type_id number not null,
    constraint crs_l_entity_group_type_pk primary key (id) using index tablespace spoindx,
    constraint crs_l_entity_group_type_ck01 check (removed in (0, 1)),
    constraint crs_l_entity_group_type_fk01 foreign key (entity_group_id) references crs_sys_h_entity_group (id),
    constraint crs_l_entity_group_type_fk02 foreign key (entity_type_id) references crs_sys_h_entity_type (id)
)
/
comment on table crs_l_entity_group_type is 'Entity group to entity type link'
/
comment on column crs_l_entity_group_type.id is 'Identifier'
/
comment on column crs_l_entity_group_type.ldts is 'Load date'
/
comment on column crs_l_entity_group_type.removed is 'Removed flag'
/
comment on column crs_l_entity_group_type.entity_group_id is 'Reference to entity group'
/
comment on column crs_l_entity_group_type.entity_type_id is 'Reference to entity type'
/
create index crs_l_entity_group_type_i01 on crs_l_entity_group_type (entity_group_id, ldts)
compress 1 tablespace spoindx
/
create index crs_l_entity_group_type_i02 on crs_l_entity_group_type (entity_type_id, ldts)
compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-406-eg-type-seq logicalFilePath:crs-1.0-VTBCRS-406-eg-type-seq endDelimiter:/
create sequence crs_l_entity_group_type_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-406-eg-entity logicalFilePath:crs-1.0-VTBCRS-406-eg-entity endDelimiter:/
create table crs_l_entity_group_entity
(
    id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) default 0 not null,
    entity_group_id number not null,
    entity_id number not null,
    constraint crs_l_entity_group_entity_pk primary key (id) using index tablespace spoindx,
    constraint crs_l_entity_group_entity_ck01 check (removed in (0, 1)),
    constraint crs_l_entity_group_entity_fk01 foreign key (entity_group_id) references crs_sys_h_entity_group (id),
    constraint crs_l_entity_group_entity_fk02 foreign key (entity_id) references crs_sys_h_entity (id)
)
/
comment on table crs_l_entity_group_entity is 'Entity group to entity link'
/
comment on column crs_l_entity_group_entity.id is 'Identifier'
/
comment on column crs_l_entity_group_entity.ldts is 'Load date'
/
comment on column crs_l_entity_group_entity.removed is 'Removed flag'
/
comment on column crs_l_entity_group_entity.entity_group_id is 'Reference to entity group'
/
comment on column crs_l_entity_group_entity.entity_id is 'Reference to entity'
/
create index crs_l_entity_group_entity_i01 on crs_l_entity_group_entity (entity_group_id, ldts)
compress 1 tablespace spoindx
/
create index crs_l_entity_group_entity_i02 on crs_l_entity_group_entity (entity_id, ldts)
compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-406-eg-entity-seq logicalFilePath:crs-1.0-VTBCRS-406-eg-entity-seq endDelimiter:/
create sequence crs_l_entity_group_entity_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-type-meta logicalFilePath:crs-1.0-VTBCRS-406-entity-type-meta endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'ENTITY_TYPE', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Тип сущности',
            'Entity type',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, entity_type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-type-attr logicalFilePath:crs-1.0-VTBCRS-406-entity-type-attr endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY_TYPE';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY_TYPE#NAME_RU',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, 'NAME_RU',
            null, 'Наименование (ru)', 'Name (ru)',
            null, 'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
--changeset pmasalov:crs-1.0-VTBCRS-406-entity-type-attr2 logicalFilePath:crs-1.0-VTBCRS-406-entity-type-attr2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY_TYPE';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY_TYPE#NAME_EN',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, 'NAME_EN',
            null, 'Наименование (en)', 'Name (en)',
            null, 'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-group-meta logicalFilePath:crs-1.0-VTBCRS-406-entity-group-meta endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'ENTITY_GROUP', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Группа сущностей',
            'entity group',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, entity_type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-group-attr-meta logicalFilePath:crs-1.0-VTBCRS-406-entity-group-attr-meta endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY_GROUP';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY_GROUP#NAME_RU',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, 'NAME_RU',
            null, 'Наименование (ru)', 'Name (ru)',
            null, 'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
--changeset pmasalov:crs-1.0-VTBCRS-406-entity-group-attr-meta2 logicalFilePath:crs-1.0-VTBCRS-406-entity-group-attr-meta2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY_GROUP';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY_GROUP#NAME_EN',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, 'NAME_EN',
            null, 'Наименование (en)', 'Name (en)',
            null, 'STRING');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
--changeset pmasalov:crs-1.0-VTBCRS-406-entity-group-attr-meta3 logicalFilePath:crs-1.0-VTBCRS-406-entity-group-attr-meta3 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY_GROUP';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY_GROUP#VIEW_ORDER',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 0, 0, 'VIEW_ORDER',
            null, 'Порядок отображения', 'View order',
            null, 'NUMBER');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-group-attr-meta4 logicalFilePath:crs-1.0-VTBCRS-406-entity-group-attr-meta4 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY_GROUP';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY_GROUP#ENTITY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 1, 0, null,
            'crs_l_entity_group_entity', 'сущность', 'Entity',
            'ENTITY', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-group-attr-meta5 logicalFilePath:crs-1.0-VTBCRS-406-entity-group-attr-meta5 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY_GROUP';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY_GROUP#ENTITY_TYPE',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, attribute_key, filter_available, removed, native_column,
                                     link_table, name_ru, name_en, entity_key, type)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id, v_ldts, 0, 0, 0, null, 1, 0, null,
            'crs_l_entity_group_type', 'Тип сущности', 'Entity type',
            'ENTITY_TYPE', 'REFERENCE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-type-satellite logicalFilePath:crs-1.0-VTBCRS-406-entity-type-satellite endDelimiter:/
create table crs_sys_s_entity_type
(
    id number not null,
    h_id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) not null,
    digest varchar2(100 char) not null,
    name_ru varchar2(4000) not null,
    name_en varchar2(4000) not null,
    constraint crs_sys_s_entity_type_pk primary key (id) using index tablespace spoindx,
    constraint crs_sys_s_entity_type_ck01 check (removed in (0, 1)),
    constraint crs_sys_s_entity_type_uk01 unique (h_id, ldts) using index compress 1 tablespace spoindx,
    constraint crs_sys_s_entity_type_fk01 foreign key (h_id) references crs_sys_h_entity_type (id)
)
/
comment on table crs_sys_s_entity_type is 'Entity type satellite'
/
comment on column crs_sys_s_entity_type.id is 'Identifier'
/
comment on column crs_sys_s_entity_type.h_id is 'Reference to hub'
/
comment on column crs_sys_s_entity_type.ldts is 'Load date'
/
comment on column crs_sys_s_entity_type.removed is 'Removed flag'
/
comment on column crs_sys_s_entity_type.digest is 'Row digest'
/
comment on column crs_sys_s_entity_type.name_ru is 'Entity type russian name'
/
comment on column crs_sys_s_entity_type.name_en is 'Entity type english name'
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-type-satellite-seq logicalFilePath:crs-1.0-VTBCRS-406-entity-type-satellite-seq endDelimiter:/
create sequence crs_sys_s_entity_type_seq
/

--changeset pmasalov:crs-1.0-VTBCRS-406-entity-type-satellite-data logicalFilePath:crs-1.0-VTBCRS-406-entity-type-satellite-dsts endDelimiter:/
insert into crs_sys_s_entity_type(id,
                                  h_id,
                                  ldts,
                                  removed,
                                  digest,
                                  name_ru,
                                  name_en)
    select crs_sys_s_entity_type_seq.nextval, id, ldts, 0, 'no-digest', 'Справочник', 'Dictionary'
    from crs_sys_h_entity_type where key = 'DICTIONARY'
/
insert into crs_sys_s_entity_type(id,
                                  h_id,
                                  ldts,
                                  removed,
                                  digest,
                                  name_ru,
                                  name_en)
    select crs_sys_s_entity_type_seq.nextval, id, ldts, 0, 'no-digest', 'Формы ввода', 'Input form'
    from crs_sys_h_entity_type where key = 'INPUT_FORM'
/
insert into crs_sys_s_entity_type(id,
                                  h_id,
                                  ldts,
                                  removed,
                                  digest,
                                  name_ru,
                                  name_en)
    select crs_sys_s_entity_type_seq.nextval, id, ldts, 0, 'no-digest', 'Встроенный обьект', 'Embedded object'
    from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT'
/
insert into crs_sys_s_entity_type(id,
                                  h_id,
                                  ldts,
                                  removed,
                                  digest,
                                  name_ru,
                                  name_en)
    select crs_sys_s_entity_type_seq.nextval, id, ldts, 0, 'no-digest', 'Предопределённый справочник', 'Predefined dictionary'
    from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY'
/
insert into crs_sys_s_entity_type(id,
                                  h_id,
                                  ldts,
                                  removed,
                                  digest,
                                  name_ru,
                                  name_en)
    select crs_sys_s_entity_type_seq.nextval, id, ldts, 0, 'no-digest', 'Классификатор', 'Classifier'
    from crs_sys_h_entity_type where key = 'CLASSIFIER'
/

--changeset akirilchev:crs-1.0-VTBCRS-406-default-group logicalFilePath:crs-1.0-VTBCRS-406-default-group endDelimiter:/
declare
    v_group_id number;
    v_time timestamp := to_date('011111', 'mmyyyy');
begin
    insert into crs_sys_h_entity_group(id, key, ldts)
    values(crs_sys_h_entity_group_seq.nextval, 'DEFAULT_DICTIONARY_GROUP', v_time)
    returning id into v_group_id;

    insert into crs_sys_s_entity_group(id, h_id, ldts, removed, digest, name_ru, name_en, view_order)
    values(crs_sys_s_entity_group_seq.nextval, v_group_id, v_time, 0, 'no digest', 'Неклассифицированные', 'Not classified', 0);

    insert into crs_l_entity_group_type(id, ldts, removed, entity_group_id, entity_type_id)
    values(crs_l_entity_group_type_seq.nextval, v_time, 0, v_group_id,
          (select t.id
             from crs_sys_h_entity_type t
            where t.key = 'DICTIONARY'));
end;
/
declare
    v_group_id number;
    v_time timestamp := to_date('011111', 'mmyyyy');
begin
    insert into crs_sys_h_entity_group(id, key, ldts)
    values(crs_sys_h_entity_group_seq.nextval, 'DEFAULT_INPUT_FORM_GROUP', v_time)
    returning id into v_group_id;

    insert into crs_sys_s_entity_group(id, h_id, ldts, removed, digest, name_ru, name_en, view_order)
    values(crs_sys_s_entity_group_seq.nextval, v_group_id, v_time, 0, 'no digest', 'Неклассифицированные', 'Not classified', 0);

    insert into crs_l_entity_group_type(id, ldts, removed, entity_group_id, entity_type_id)
    values(crs_l_entity_group_type_seq.nextval, v_time, 0, v_group_id,
          (select t.id
             from crs_sys_h_entity_type t
            where t.key = 'INPUT_FORM'));
end;
/
declare
    v_group_id number;
    v_time timestamp := to_date('011111', 'mmyyyy');
begin
    insert into crs_sys_h_entity_group(id, key, ldts)
    values(crs_sys_h_entity_group_seq.nextval, 'DEFAULT_CLASSIFIER_GROUP', v_time)
    returning id into v_group_id;

    insert into crs_sys_s_entity_group(id, h_id, ldts, removed, digest, name_ru, name_en, view_order)
    values(crs_sys_s_entity_group_seq.nextval, v_group_id, v_time, 0, 'no digest', 'Неклассифицированные', 'Not classified', 0);

    insert into crs_l_entity_group_type(id, ldts, removed, entity_group_id, entity_type_id)
    values(crs_l_entity_group_type_seq.nextval, v_time, 0, v_group_id,
          (select t.id
             from crs_sys_h_entity_type t
            where t.key = 'CLASSIFIER'));
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-406-default-group-data logicalFilePath:crs-1.0-VTBCRS-406-default-group-data endDelimiter:/
insert into crs_l_entity_group_entity(id, ldts, removed, entity_group_id, entity_id)
select crs_l_entity_group_entity_seq.nextval, systimestamp, 0,
       t.entity_group_id,
       let.entity_id
  from crs_sys_l_entity_type let join (select et.id entity_type_id, eg.id entity_group_id
                                        from (select 'DEFAULT_DICTIONARY_GROUP' entity_group_key, 'DICTIONARY' entity_type_key from dual union all
                                              select 'DEFAULT_INPUT_FORM_GROUP' entity_group_key, 'INPUT_FORM' entity_type_key from dual union all
                                              select 'DEFAULT_CLASSIFIER_GROUP' entity_group_key, 'CLASSIFIER' entity_type_key from dual
                                             ) z join crs_sys_h_entity_type et on et.key = z.entity_type_key
                                                 join crs_sys_h_entity_group eg on eg.key = z.entity_group_key
                                      ) t on t.entity_type_id = let.entity_type_id
/

--changeset emelnikov:crs-1.0-VTBCRS-309 logicalFilePath:crs-1.0-VTBCRS-309 endDelimiter:/
alter table crs_sys_s_attribute add default_value varchar2(4000)
/
comment on column crs_sys_s_attribute.default_value is 'Default value'
/

--changeset akirilchev:crs-1.0-vtbcrs-406-table-rename logicalfilepath:crs-1.0-vtbcrs-406-table-rename enddelimiter:/
rename crs_l_entity_group_entity to crs_sys_l_entity_group
/
rename crs_l_entity_group_entity_seq to crs_sys_l_entity_group_seq
/
rename crs_l_entity_group_type to crs_sys_l_e_group_type
/
rename crs_l_entity_group_type_seq to crs_sys_l_e_group_type_seq
/
alter index crs_l_entity_group_type_i01 rename to crs_sys_l_e_group_type_i01
/
alter index crs_l_entity_group_type_i02 rename to crs_sys_l_e_group_type_i02
/
alter index crs_l_entity_group_type_pk rename to crs_sys_l_e_group_type_pk
/
alter table crs_sys_l_e_group_type rename constraint crs_l_entity_group_type_pk to crs_sys_l_e_group_type_pk
/
alter table crs_sys_l_e_group_type rename constraint crs_l_entity_group_type_fk01 to crs_sys_l_e_group_type_fk01
/
alter table crs_sys_l_e_group_type rename constraint crs_l_entity_group_type_fk02 to crs_sys_l_e_group_type_fk02
/
alter table crs_sys_l_e_group_type rename constraint crs_l_entity_group_type_ck01 to crs_sys_l_e_group_type_ck01
/
alter index crs_l_entity_group_entity_i01 rename to crs_sys_l_entity_group_i01
/
alter index crs_l_entity_group_entity_i02 rename to crs_sys_l_entity_group_i02
/
alter index crs_l_entity_group_entity_pk rename to crs_sys_l_entity_group_pk
/
alter table crs_sys_l_entity_group rename constraint crs_l_entity_group_entity_pk to crs_sys_l_entity_group_pk
/
alter table crs_sys_l_entity_group rename constraint crs_l_entity_group_entity_fk01 to crs_sys_l_entity_group_fk01
/
alter table crs_sys_l_entity_group rename constraint crs_l_entity_group_entity_fk02 to crs_sys_l_entity_group_fk02
/
alter table crs_sys_l_entity_group rename constraint crs_l_entity_group_entity_ck01 to crs_sys_l_entity_group_ck01
/
update crs_sys_s_attribute
   set link_table = 'crs_sys_l_entity_group'
 where link_table = 'crs_l_entity_group_entity'
/
update crs_sys_s_attribute
   set link_table = 'crs_sys_l_e_group_type'
 where link_table = 'crs_l_entity_group_type'
/

--changeset akirilchev:crs-1.0-VTBCRS-406-default-group-data2 logicalFilePath:crs-1.0-VTBCRS-406-default-group-data2 endDelimiter:/
delete crs_sys_l_entity_group
/
insert into crs_sys_l_entity_group(id, ldts, removed, entity_group_id, entity_id)
select crs_sys_l_entity_group_seq.nextval, to_date('011111', 'mmyyyy'), 0,
       t.entity_group_id,
       let.entity_id
  from crs_sys_l_entity_type let join (select et.id entity_type_id, eg.id entity_group_id
                                        from (select 'DEFAULT_DICTIONARY_GROUP' entity_group_key, 'DICTIONARY' entity_type_key from dual union all
                                              select 'DEFAULT_INPUT_FORM_GROUP' entity_group_key, 'INPUT_FORM' entity_type_key from dual union all
                                              select 'DEFAULT_CLASSIFIER_GROUP' entity_group_key, 'CLASSIFIER' entity_type_key from dual
                                             ) z join crs_sys_h_entity_type et on et.key = z.entity_type_key
                                                 join crs_sys_h_entity_group eg on eg.key = z.entity_group_key
                                      ) t on t.entity_type_id = let.entity_type_id
/


--changeset svaliev:crs-1.0-update-calc-date-actuality logicalFilePath:crs-1.0-update-calc-date-actuality endDelimiter:/
update crs_sys_s_attribute
   set name_ru = 'Отчетная дата'
 where h_id = (select id from crs_sys_h_attribute where key = 'CALC#ACTUALITY')
/

--changeset akirilchev:crs-1.0-VTBCRS-426-dep-table logicalFilePath:crs-1.0-VTBCRS-426-table endDelimiter:/
create table crs_h_department(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_department_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_department_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_department_seq
/
comment on table crs_h_department is 'Department hub'
/
comment on column crs_h_department.id is 'Identifier'
/
comment on column crs_h_department.key is 'Key'
/
comment on column crs_h_department.ldts is 'Load date'
/
create table crs_s_department(
    id          number              not null,
    h_id        number              not null,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    constraint crs_s_department_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_department_ck01 check(removed in (0, 1)),
    constraint crs_s_department_uk01 unique(h_id, ldts) using index compress 1 tablespace spoindx,
    constraint crs_s_department_fk01 foreign key(h_id) references crs_h_department(id)
)
/
create sequence crs_s_department_seq
/
comment on table crs_s_department is 'Department satellite'
/
comment on column crs_s_department.id is 'Identifier'
/
comment on column crs_s_department.h_id is 'Reference to hub'
/
comment on column crs_s_department.digest is 'Row digest'
/
comment on column crs_s_department.ldts is 'Load date'
/
comment on column crs_s_department.removed is 'Removed flag'
/
create table crs_l_user_department
(
    id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) default 0 not null,
    user_id number not null,
    department_id number not null,
    constraint crs_l_user_department_pk primary key (id) using index tablespace spoindx,
    constraint crs_l_user_department_ck01 check (removed in (0, 1)),
    constraint crs_l_user_department_fk01 foreign key (user_id) references crs_h_user(id),
    constraint crs_l_user_department_fk02 foreign key (department_id) references crs_h_department(id)
)
/
comment on table crs_l_user_department is 'User to department link'
/
comment on column crs_l_user_department.id is 'Identifier'
/
comment on column crs_l_user_department.ldts is 'Load date'
/
comment on column crs_l_user_department.removed is 'Removed flag'
/
comment on column crs_l_user_department.user_id is 'Reference to user'
/
comment on column crs_l_user_department.department_id is 'Reference to department'
/
create index crs_l_user_department_i01 on crs_l_user_department (user_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_user_department_i02 on crs_l_user_department (department_id, ldts) compress 1 tablespace spoindx
/
create sequence crs_l_user_department_seq
/
create table crs_l_client_department
(
    id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) default 0 not null,
    client_id number not null,
    department_id number not null,
    constraint crs_l_client_department_pk primary key (id) using index tablespace spoindx,
    constraint crs_l_client_department_ck01 check (removed in (0, 1)),
    constraint crs_l_client_department_fk01 foreign key (client_id) references crs_h_client(id),
    constraint crs_l_client_department_fk02 foreign key (department_id) references crs_h_department(id)
)
/
comment on table crs_l_client_department is 'Client to department link'
/
comment on column crs_l_client_department.id is 'Identifier'
/
comment on column crs_l_client_department.ldts is 'Load date'
/
comment on column crs_l_client_department.removed is 'Removed flag'
/
comment on column crs_l_client_department.client_id is 'Reference to client'
/
comment on column crs_l_client_department.department_id is 'Reference to department'
/
create index crs_l_client_department_i01 on crs_l_client_department (client_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_department_i02 on crs_l_client_department (department_id, ldts) compress 1 tablespace spoindx
/
create sequence crs_l_client_department_seq
/
create table crs_l_department_name(
    id                 number              not null,
    department_id      number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_department_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_department_name_fk01 foreign key(department_id) references crs_h_department(id),
    constraint crs_l_department_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_department_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_department_name_i01 on crs_l_department_name(department_id, ldts) tablespace spoindx
/
create index crs_l_department_name_i02 on crs_l_department_name(localization_id, ldts) tablespace spoindx
/
create index crs_l_department_name_i03 on crs_l_department_name(ldts) tablespace spoindx
/
create sequence crs_l_department_name_seq
/
comment on table crs_l_department_name is 'Department to multilanguage name link'
/
comment on column crs_l_department_name.department_id is 'Reference to department hub'
/
comment on column crs_l_department_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_department_name.removed is 'Removed flag'
/
comment on column crs_l_department_name.ldts is 'Load date'
/
create table crs_l_department_fullname(
    id                 number              not null,
    department_id      number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_department_fullname_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_department_fullname_fk01 foreign key(department_id) references crs_h_department(id),
    constraint crs_l_department_fullname_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_department_fullname_ck01 check(removed in (0, 1))
)
/
create index crs_l_department_fullname_i01 on crs_l_department_fullname(department_id, ldts) tablespace spoindx
/
create index crs_l_department_fullname_i02 on crs_l_department_fullname(localization_id, ldts) tablespace spoindx
/
create index crs_l_department_fullname_i03 on crs_l_department_fullname(ldts) tablespace spoindx
/
create sequence crs_l_department_fullname_seq
/
comment on table crs_l_department_fullname is 'Department to multilanguage full name link'
/
comment on column crs_l_department_fullname.department_id is 'Reference to department hub'
/
comment on column crs_l_department_fullname.localization_id is 'Reference to localization hub'
/
comment on column crs_l_department_fullname.removed is 'Removed flag'
/
comment on column crs_l_department_fullname.ldts is 'Load date'
/
create table crs_l_department_comment(
    id                 number              not null,
    department_id      number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_department_comment_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_department_comment_fk01 foreign key(department_id) references crs_h_department(id),
    constraint crs_l_department_comment_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_department_comment_ck01 check(removed in (0, 1))
)
/
create index crs_l_department_comment_i01 on crs_l_department_comment(department_id, ldts) tablespace spoindx
/
create index crs_l_department_comment_i02 on crs_l_department_comment(localization_id, ldts) tablespace spoindx
/
create index crs_l_department_comment_i03 on crs_l_department_comment(ldts) tablespace spoindx
/
create sequence crs_l_department_comment_seq
/
comment on table crs_l_department_comment is 'Department to multilanguage comment link'
/
comment on column crs_l_department_comment.department_id is 'Reference to department hub'
/
comment on column crs_l_department_comment.localization_id is 'Reference to localization hub'
/
comment on column crs_l_department_comment.removed is 'Removed flag'
/
comment on column crs_l_department_comment.ldts is 'Load date'
/

--changeset akirilchev:crs-1.0-VTBCRS-426-dep-self-link logicalFilePath:crs-1.0-VTBCRS-426-dep-self-link endDelimiter:/
create table crs_l_department (
    id              number not null,
    ldts            timestamp not null,
    removed         number(1) not null,
    department_id   number not null,
    department_p_id number not null,
    constraint crs_l_department_pk primary key(id) using index tablespace spoindx,
    constraint crs_l_department_ck01 check (removed in (0, 1)),
    constraint crs_l_department_fk01 foreign key(department_id) references crs_h_department(id),
    constraint crs_l_department_fk02 foreign key(department_p_id) references crs_h_department(id)
)
/
comment on table crs_l_department is 'Department to department link'
/
comment on column crs_l_department.id is 'Identifier'
/
comment on column crs_l_department.department_id is 'Reference to department'
/
comment on column crs_l_department.department_p_id is 'Reference to parent department'
/
comment on column crs_l_department.ldts is 'Load date'
/
create index crs_l_department_i01 on crs_l_department (department_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_department_i02 on crs_l_department (department_p_id, ldts) compress 1 tablespace spoindx
/
create sequence crs_l_department_seq
/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'DEPARTMENT', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Подразделение',
            'Department',
            'CRS_L_DEPARTMENT',
            null,
            1,
            0);
    insert into crs_sys_l_entity_type (id, entity_type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-426-attr logicalFilePath:crs-1.0-VTBCRS-426-attr endDelimiter:/
declare
    v_entity_type_id number;
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    function insert_entity(
        p_entity_type_id crs_sys_h_entity.id%type,
        p_entity_key     crs_sys_h_entity.key%type,
        p_entity_name_ru crs_sys_s_entity.name_ru%type,
        p_entity_name_en crs_sys_s_entity.name_en%type,
        p_ldts           crs_sys_h_entity.ldts%type
    ) return number is
    begin
        insert into crs_sys_h_entity(id, key, ldts)
        values(crs_sys_h_entity_seq.nextval, p_entity_key, p_ldts);

        insert into crs_sys_s_entity(id, h_id, ldts, name_ru, name_en)
        values (crs_sys_s_entity_seq.nextval, crs_sys_h_entity_seq.currval, p_ldts, p_entity_name_ru, p_entity_name_en);

        insert into crs_sys_l_entity_type(id, entity_type_id, entity_id, ldts)
        values(crs_sys_l_entity_type_seq.nextval, p_entity_type_id, crs_sys_h_entity_seq.currval, p_ldts);

        return crs_sys_h_entity_seq.currval;
    end;

    procedure insert_entity_attribute(
        p_entity_hub_id     crs_sys_h_entity.id%type,
        p_entity_key        crs_sys_h_entity.key%type,
        p_attribute_key     varchar2,
        p_attribute_name_ru crs_sys_s_attribute.name_ru%type,
        p_attribute_name_en crs_sys_s_attribute.name_en%type,
        p_multilang         crs_sys_s_attribute.multilang%type,
        p_link_table        crs_sys_s_attribute.link_table%type,
        p_attribute_type    crs_sys_s_attribute.type%type,
        p_native_column     crs_sys_s_attribute.native_column%type,
        p_ref_entity_key    crs_sys_s_attribute.entity_key%type,
        p_ref_attribute_key crs_sys_s_attribute.attribute_key%type,
        p_ldts              crs_sys_h_attribute.ldts%type
    ) is
    begin
        insert into crs_sys_h_attribute(id, key, ldts)
        values(crs_sys_h_attribute_seq.nextval, p_entity_key || '#' || p_attribute_key, p_ldts);

        insert into crs_sys_s_attribute(id, h_id, ldts, multilang, link_table,
                                        name_ru, name_en, type, native_column, entity_key,
                                        attribute_key)
        values(crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_multilang, p_link_table,
               p_attribute_name_ru, p_attribute_name_en, p_attribute_type, p_native_column, p_ref_entity_key,
               case when p_ref_entity_key is not null and p_ref_attribute_key is not null
                    then p_ref_entity_key || '#' || p_ref_attribute_key
                    else null
               end);

        insert into crs_sys_l_entity_attribute(id, entity_id, attribute_id, ldts)
        values(crs_sys_l_entity_attribute_seq.nextval, p_entity_hub_id, crs_sys_h_attribute_seq.currval, p_ldts);
    end;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    v_entity_key := 'DEPARTMENT';
    select id into v_entity_hub_id
      from crs_sys_h_entity
     where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CLIENT', 'Клиент', 'Client', 0, 'crs_l_client_department', 'REFERENCE', null, 'CLIENT', null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'USER', 'Пользователь', 'User', 0, 'crs_l_user_department', 'REFERENCE', null, 'USER', null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 1, 'crs_l_department_name', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'FULL_NAME', 'Полное наименование', 'Full name', 1, 'crs_l_department_fullname', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'COMMENT', 'Комментарий', 'Comment', 1, 'crs_l_department_comment', 'STRING', null, null, null, v_ldts);

    v_entity_key := 'USER';
    select id into v_entity_hub_id
      from crs_sys_h_entity
     where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'DEPARTMENT', 'Подразделение', 'Department', 0, 'crs_l_user_department', 'REFERENCE', null, 'DEPARTMENT', null, v_ldts);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-426-dep-migrate logicalFilePath:crs-1.0-VTBCRS-426-dep-migrate endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:1 select count(*) from user_tab_cols where (table_name, column_name) in (select 'DEPARTMENTS', 'FULLNAME' from dual)
declare
    v_date timestamp := to_date('011111', 'mmyyyy');
    v_hub_id number;
    v_localization_hub_id crs_sys_h_localization.id%type;

    function insert_string_localization(
        p_string_ru      crs_sys_s_localization.string_ru%type,
        p_string_en      crs_sys_s_localization.string_en%type,
        p_current_run_ts timestamp
    ) return number is
    begin
        insert into crs_sys_h_localization(id, key, ldts)
        values(crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_current_run_ts);

        insert into crs_sys_s_localization(id, h_id, string_ru, string_en, digest, ldts)
        values(crs_sys_s_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_string_ru, p_string_en, 'NO DIGEST', p_current_run_ts);

        return crs_sys_h_localization_seq.currval;
    end;
begin
    for rec in (select id_department, fullname, shortname, is_active from departments order by id_department) loop
        insert into crs_h_department(id, key, ldts)
        values (crs_h_department_seq.nextval, rec.id_department, v_date)
        returning id into v_hub_id;

        insert into crs_s_department(id, h_id, digest, removed, ldts)
        select crs_s_department_seq.nextval, v_hub_id, 'NO DIGEST',
               case when rec.is_active = 1 then 0 else 1 end removed,
               v_date
          from dual;

        v_localization_hub_id := insert_string_localization(rec.shortname, rec.shortname, v_date);
        insert into crs_l_department_name(id, department_id, localization_id, ldts, removed)
        values(crs_l_department_name_seq.nextval, v_hub_id, v_localization_hub_id, v_date, 0);

        v_localization_hub_id := insert_string_localization(rec.fullname, rec.fullname, v_date);
        insert into crs_l_department_fullname(id, department_id, localization_id, ldts, removed)
        values(crs_l_department_fullname_seq.nextval, v_hub_id, v_localization_hub_id, v_date, 0);
    end loop;
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-552 logicalFilePath:crs-1.0-VTBCRS-552 endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:1 select count(*) from user_tab_cols where (table_name, column_name) in (select 'USERS', 'SURNAME' from dual)
declare
    v_hub_id number;
begin
    for rec in (select id_user, login, to_date('011111', 'mmyyyy') ldts,
                       surname, name, patronymic, is_active
                  from users u
                 where not exists(select 1
                                    from crs_h_user h
                                   where upper(h.key) = upper(u.login)) )
    loop
        insert into crs_h_user(id, key, ldts)
        values (crs_h_user_seq.nextval, upper(rec.login), rec.ldts)
        returning id into v_hub_id;

        insert into crs_s_user(id, h_id, ldts, removed, digest, surname, name, patronymic)
        values(crs_s_user_seq.nextval, v_hub_id, to_date('011111', 'mmyyyy'),
              case when rec.is_active = 1 then 0 else 1 end,
              'no_digest', rec.surname, rec.name, rec.patronymic);
    end loop;
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-552-dep-user-migrate logicalFilePath:crs-1.0-VTBCRS-552-dep-user-migrate endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:2 select count(*) from user_tab_cols where (table_name, column_name) in (select 'DEPARTMENTS', 'FULLNAME' from dual union all select 'USERS', 'SURNAME' from dual)
delete crs_l_user_department
/
insert into crs_l_user_department(id, ldts, removed, user_id, department_id)
select crs_l_user_department_seq.nextval, to_date('011111', 'mmyyyy'), 0,
       (select t.id
          from crs_h_user t
         where t.key = upper(u.login)) user_hub_id,
       (select t.id
          from crs_h_department t
         where t.key = to_char(u.id_department)) department_hub_id
  from users u
/

--changeset akirilchev:crs-1.0-VTBCRS-426-dep-hierarchy-migrate-2 logicalFilePath:crs-1.0-VTBCRS-426-dep-hierarchy-migrate-2 endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:2 select count(*) from user_tab_cols where (table_name, column_name) in (select 'DEPARTMENTS', 'FULLNAME' from dual union all select 'DEPARTMENTS_PAR', 'ID_DEPARTMENT_PAR' from dual)
delete crs_l_department
/
insert into crs_l_department(id, ldts, removed, department_id, department_p_id)
select crs_l_department_seq.nextval, to_date('011111', 'mmyyyy'), 0,
       t.id, t2.id
  from departments_par dp left join crs_h_department t on t.key = to_char(dp.id_department_child)
                          left join crs_h_department t2 on t2.key = to_char(dp.id_department_par)
/

--changeset akirilchev:crs-1.0-VTBCRS-492 logicalFilePath:crs-1.0-VTBCRS-492 endDelimiter:/
alter table crs_s_calc_model add periodicity varchar2(30)
/
update crs_s_calc_model set periodicity = 'QUARTER'
/
comment on column crs_s_calc_model.periodicity is 'Periodicity'
/
declare
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    procedure insert_entity_attribute(
        p_entity_hub_id     crs_sys_h_entity.id%type,
        p_entity_key        crs_sys_h_entity.key%type,
        p_attribute_key     varchar2,
        p_attribute_name_ru crs_sys_s_attribute.name_ru%type,
        p_attribute_name_en crs_sys_s_attribute.name_en%type,
        p_multilang         crs_sys_s_attribute.multilang%type,
        p_link_table        crs_sys_s_attribute.link_table%type,
        p_attribute_type    crs_sys_s_attribute.type%type,
        p_native_column     crs_sys_s_attribute.native_column%type,
        p_ref_entity_key    crs_sys_s_attribute.entity_key%type,
        p_ref_attribute_key crs_sys_s_attribute.attribute_key%type,
        p_ldts              crs_sys_h_attribute.ldts%type
    ) is
    begin
        insert into crs_sys_h_attribute(id, key, ldts)
        values(crs_sys_h_attribute_seq.nextval, p_entity_key || '#' || p_attribute_key, p_ldts);

        insert into crs_sys_s_attribute(id, h_id, ldts, multilang, link_table,
                                        name_ru, name_en, type, native_column, entity_key,
                                        attribute_key)
        values(crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_multilang, p_link_table,
               p_attribute_name_ru, p_attribute_name_en, p_attribute_type, p_native_column, p_ref_entity_key,
               case when p_ref_entity_key is not null and p_ref_attribute_key is not null
                    then p_ref_entity_key || '#' || p_ref_attribute_key
                    else null
               end);

        insert into crs_sys_l_entity_attribute(id, entity_id, attribute_id, ldts)
        values(crs_sys_l_entity_attribute_seq.nextval, p_entity_hub_id, crs_sys_h_attribute_seq.currval, p_ldts);
    end;
begin
    v_entity_key := 'CALC_MODEL';
    select id into v_entity_hub_id
      from crs_sys_h_entity
     where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'PERIODICITY', 'Периодичность', 'Periodicity', 0, null, 'STRING', 'PERIODICITY', null, null, v_ldts);
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-496 logicalFilePath:crs-1.0-VTBCRS-496 endDelimiter:/
alter table crs_l_calc rename column origin_calc_id to calc_id
/
alter table crs_l_calc rename column copied_calc_id to calc_p_id
/
update crs_sys_s_entity
   set hierarchical = 1,
       link_table = 'CRS_L_CALC'
where h_id in (select id
                 from crs_sys_h_entity
                where key = 'CALC')
/

--changeset pmasalov:crs-1.0-VTBCRS-467 logicalFilePath::crs-1.0-VTBCRS-467 endDelimiter:/
begin
    for r in (select index_name from user_indexes where index_name in ('CRS_S_CLIENT_GROUP_I01',
                                                                       'CRS_S_CLIENT_I01',
                                                                       'CRS_SYS_S_LOCALIZATION_I01',
                                                                       'CRS_SYS_S_LOCALIZATION_I02')) loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-467-new-index logicalFilePath::crs-1.0-VTBCRS-467-new-index endDelimiter:/
begin
    for r in (select index_name from user_indexes where index_name in ('CRS_S_CLIENT_INN_I01',
                                                                       'CRS_SYS_S_LOCALIZATION_I01',
                                                                       'CRS_SYS_S_LOCALIZATION_I02',
                                                                       'CRS_L_CLIENT_NAME_I02',
                                                                       'CRS_L_CLIENT_GROUP_NAME_I02',
                                                                       'CRS_L_CLIENT_INN_I02',
                                                                       'CRS_L_CLIENT_NAME_I01',
                                                                       'CRS_L_CLIENT_INN_I01')) loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/
create index crs_sys_s_localization_i01 on crs_sys_s_localization (upper(string_ru)) tablespace spoindx
/
create index crs_sys_s_localization_i02 on crs_sys_s_localization (upper(string_en)) tablespace spoindx
/
create index crs_s_client_inn_i01 on crs_s_client_inn (upper(tax_id)) tablespace spoindx
/
create index crs_l_client_name_i02 on crs_l_client_name (localization_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_name_i01 on crs_l_client_name (client_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_group_name_i02 on crs_l_client_group_name (localization_id, ldts) compress 1  tablespace spoindx
/
create index crs_l_client_inn_i02 on crs_l_client_inn (client_inn_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_inn_i01 on crs_l_client_inn (client_id, ldts) compress 1 tablespace spoindx
/

--changeset achalov:crs-1.0-VTBCRS-511 logicalFilePath:crs-1.0-VTBCRS-511 endDelimiter:/
create table crs_h_role(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_role_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_role_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_role_seq
/
comment on table crs_h_role is 'Role hub'
/
comment on column crs_h_role.id is 'Identifier'
/
comment on column crs_h_role.key is 'Key'
/
comment on column crs_h_role.ldts is 'Load date'
/
create table crs_s_role(
    id          number              not null,
    h_id        number              not null,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    constraint crs_s_role_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_role_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_role_fk01 foreign key(h_id)  references crs_h_role(id),
    constraint crs_s_role_ck01 check(removed in (0, 1))
)
/
create sequence crs_s_role_seq
/
comment on table crs_s_role is 'Role satellite'
/
comment on column crs_s_role.id is 'Identifier'
/
comment on column crs_s_role.h_id is 'Reference to hub'
/
comment on column crs_s_role.digest is 'Row digest'
/
comment on column crs_s_role.removed is 'Removed flag'
/
comment on column crs_s_role.ldts is 'Load date'
/
create table crs_l_role_name(
    id              number              not null,
    role_id         number              not null,
    localization_id number              not null,
    removed         number(1) default 0 not null,
    ldts            timestamp           not null,
    constraint crs_l_role_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_role_name_fk01 foreign key(role_id)         references crs_h_role(id),
    constraint crs_l_role_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_role_name_ck01 check(removed in (0, 1))
)
/
create sequence crs_l_role_name_seq
/
comment on table crs_l_role_name is 'Role to multilanguage name link'
/
comment on column crs_l_role_name.role_id is 'Reference to role hub'
/
comment on column crs_l_role_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_role_name.removed is 'Removed flag'
/
comment on column crs_l_role_name.ldts is 'Load date'
/
create index crs_l_role_name_i01 on crs_l_role_name(role_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_role_name_i02 on crs_l_role_name(localization_id, ldts) compress 1 tablespace spoindx
/
create table crs_l_role_desc(
    id              number              not null,
    role_id         number              not null,
    localization_id number              not null,
    removed         number(1) default 0 not null,
    ldts            timestamp           not null,
    constraint crs_l_role_desc_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_role_desc_fk01 foreign key(role_id)         references crs_h_role(id),
    constraint crs_l_role_desc_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_role_desc_ck01 check(removed in (0, 1))
)
/
create sequence crs_l_role_desc_seq
/
comment on table crs_l_role_desc is 'Role to multilanguage description link'
/
comment on column crs_l_role_desc.role_id is 'Reference to role hub'
/
comment on column crs_l_role_desc.localization_id is 'Reference to localization hub'
/
comment on column crs_l_role_desc.removed is 'Removed flag'
/
comment on column crs_l_role_desc.ldts is 'Load date'
/
create index crs_l_role_desc_i01 on crs_l_role_desc(role_id, ldts) tablespace spoindx
/
create index crs_l_role_desc_i02 on crs_l_role_desc(localization_id, ldts) tablespace spoindx
/
create table crs_l_user_role(
    id      number       not null,
    user_id number       not null,
    role_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_l_user_role_pk   primary key (id) using index tablespace spoindx,
    constraint crs_l_user_role_fk01 foreign key (user_id) references crs_h_user(id),
    constraint crs_l_user_role_fk02 foreign key (role_id) references crs_h_role(id),
    constraint crs_l_user_role_ck01 check (removed in (0, 1))
)
/
create sequence crs_l_user_role_seq
/
comment on table crs_l_user_role is 'User to role link'
/
comment on column crs_l_user_role.id is 'Identifier'
/
comment on column crs_l_user_role.ldts is 'Load date'
/
comment on column crs_l_user_role.removed is 'Removed flag'
/
comment on column crs_l_user_role.user_id is 'Reference to user hub'
/
comment on column crs_l_user_role.role_id is 'Reference to role hub'
/
create index crs_l_user_role_i01 on crs_l_user_role(user_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_user_role_i02 on crs_l_user_role(role_id, ldts) compress 1 tablespace spoindx
/
declare
    v_entity_type_id number;
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    function insert_entity(
        p_entity_type_id crs_sys_h_entity.id%type,
        p_entity_key     crs_sys_h_entity.key%type,
        p_entity_name_ru crs_sys_s_entity.name_ru%type,
        p_entity_name_en crs_sys_s_entity.name_en%type,
        p_ldts           crs_sys_h_entity.ldts%type
    ) return number is
    begin
        insert into crs_sys_h_entity(id, key, ldts)
        values(crs_sys_h_entity_seq.nextval, p_entity_key, p_ldts);

        insert into crs_sys_s_entity(id, h_id, ldts, name_ru, name_en)
        values (crs_sys_s_entity_seq.nextval, crs_sys_h_entity_seq.currval, p_ldts, p_entity_name_ru, p_entity_name_en);

        insert into crs_sys_l_entity_type(id, entity_type_id, entity_id, ldts)
        values(crs_sys_l_entity_type_seq.nextval, p_entity_type_id, crs_sys_h_entity_seq.currval, p_ldts);

        return crs_sys_h_entity_seq.currval;
    end;

    procedure insert_entity_attribute(
        p_entity_hub_id     crs_sys_h_entity.id%type,
        p_entity_key        crs_sys_h_entity.key%type,
        p_attribute_key     varchar2,
        p_attribute_name_ru crs_sys_s_attribute.name_ru%type,
        p_attribute_name_en crs_sys_s_attribute.name_en%type,
        p_multilang         crs_sys_s_attribute.multilang%type,
        p_link_table        crs_sys_s_attribute.link_table%type,
        p_attribute_type    crs_sys_s_attribute.type%type,
        p_native_column     crs_sys_s_attribute.native_column%type,
        p_ref_entity_key    crs_sys_s_attribute.entity_key%type,
        p_ref_attribute_key crs_sys_s_attribute.attribute_key%type,
        p_ldts              crs_sys_h_attribute.ldts%type
    ) is
    begin
        insert into crs_sys_h_attribute(id, key, ldts)
        values(crs_sys_h_attribute_seq.nextval, p_entity_key || '#' || p_attribute_key, p_ldts);

        insert into crs_sys_s_attribute(
            id, h_id, ldts, multilang, link_table,
            name_ru, name_en, type, native_column, entity_key,
            attribute_key
        )
        values(
            crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_multilang, p_link_table,
            p_attribute_name_ru, p_attribute_name_en, p_attribute_type, p_native_column, p_ref_entity_key,
            case
                 when p_ref_entity_key is not null and p_ref_attribute_key is not null
                      then p_ref_entity_key || '#' || p_ref_attribute_key
                 else
                      null
            end
        );

        insert into crs_sys_l_entity_attribute(id, entity_id, attribute_id, ldts)
        values(crs_sys_l_entity_attribute_seq.nextval, p_entity_hub_id, crs_sys_h_attribute_seq.currval, p_ldts);
    end;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    --role
    v_entity_key := 'ROLE';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Роль', 'Role', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 1, 'CRS_L_ROLE_NAME', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'DESCRIPTION', 'Описание', 'Description', 1, 'CRS_L_ROLE_DESC', 'TEXT', null, null, null, v_ldts);

    --user to role link
    v_entity_key := 'USER';
    select id into v_entity_hub_id
      from crs_h_entity where KEY = 'USER';
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'ROLES', 'Роли', 'Roles', 0, 'CRS_L_USER_ROLE', 'REFERENCE', null, 'ROLE', null, v_ldts);
end;
/
declare
    v_ldts timestamp := systimestamp;

    procedure insert_role(
        p_role_key     crs_h_role.key%type,
        p_role_name_ru crs_sys_s_localization.string_ru%type,
        p_role_name_en crs_sys_s_localization.string_en%type,
        p_ldts         timestamp
    ) is
    begin
        insert into crs_h_role(id, key, ldts)
        values(crs_h_role_seq.nextval, p_role_key, p_ldts);
        insert into crs_s_role(id, h_id, digest, ldts)
        values(crs_s_role_seq.nextval, crs_h_role_seq.currval, 'NO_DIGEST', p_ldts);

        insert into crs_sys_h_localization(id, key, ldts)
        values(crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_ldts);
        insert into crs_sys_s_localization(id, h_id, ldts, string_ru, string_en, digest)
        values(crs_sys_s_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_ldts, p_role_name_ru, p_role_name_en, 'NO DIGEST');
        insert into crs_l_role_name(id, role_id, localization_id, ldts)
        values(crs_l_role_name_seq.nextval, crs_h_role_seq.currval, crs_sys_h_localization_seq.currval, p_ldts);
    end;
begin
    insert_role('КМ', 'Клиентский менеджер', 'Client manager', v_ldts);
    insert_role('КИ', 'Кредитный инспектор', 'Credit inspector', v_ldts);
    insert_role('МЭ', 'Менеджер эксперт', 'Expert manager', v_ldts);
    insert_role('КЭ', 'Кредитный эксперт', 'Credit expert', v_ldts);
    insert_role('КЭ (СБ)', 'Кредитный эксперт-средний бизнес', 'Medium-sized business credit expert', v_ldts);
    insert_role('КЭ (КБ)', 'Кредитный эксперт-крупный бизнес', 'Large business credit expert', v_ldts);
    insert_role('КЭ (СЗРЦ)', 'Кредитный эксперт СЗРЦ', 'NWRC credit expert', v_ldts);
    insert_role('КЭ (ДФК)', 'Кредитный эксперт ДФК', 'CFC credit expert', v_ldts);
    insert_role('РА', 'Риск-аналитик', 'Risk analyst', v_ldts);
    insert_role('РА (СЗРЦ)', 'Риск-аналитик СЗРЦ', 'NWRC risk analyst', v_ldts);
    insert_role('РА (ДФК)', 'Риск-аналитик ДФК ', 'CFC risk analyst', v_ldts);
    insert_role('БА', 'Бизнес-администратор', 'Business administrator', v_ldts);
    insert_role('БА (СЗРЦ)', 'Бизнес-администратор СЗРЦ', 'NWRC business administrator', v_ldts);
    insert_role('БА (ДФК)', 'Бизнес-администратор ДФК', 'CFC business administrator', v_ldts);
    insert_role('АУД', 'Аудитор', 'Auditor', v_ldts);
    insert_role('АУД (СЗРЦ)', 'Аудитор СЗРЦ', 'NWRC auditor', v_ldts);
    insert_role('АУД (ДФК)', 'Аудитор ДФК', 'CFC auditor', v_ldts);
    insert_role('АДМ', 'Администратор системы', 'System administrator', v_ldts);
    insert_role('АДМ_ПОЛЬЗ', 'Администратор пользователей', 'User administrator', v_ldts);
    insert_role('АДМ_РОЛИ', 'Администратор полномочий', 'Credentials administrator', v_ldts);
    insert_role('АДМ_ВИЗ', 'Администратор, визирующий действия Администратора полномочий', 'Visa credentials administrator', v_ldts);
    insert_role('АУД (ИБ)', 'Аудитор ИБ', 'Security auditor', v_ldts);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-525 logicalFilePath:crs-1.0-VTBCRS-525 endDelimiter:/
begin
    for r in (select index_name from user_indexes where index_name in ('CRS_SYS_S_LOCALIZATION_I01',
                                                                       'CRS_SYS_S_LOCALIZATION_I02',
                                                                       'CRS_L_CLIENT_OGRN_I03',
                                                                       'CRS_L_CLIENT_OGRN_I01',
                                                                       'CRS_L_CLIENT_OGRN_I02')) loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/
alter table crs_s_client_inn
    drop constraint crs_s_client_inn_uk1 cascade
/
alter table crs_s_client_inn
    add constraint crs_s_client_inn_uk1 unique (h_id, ldts)
    using index compress 1
    tablespace spoindx
/
alter table crs_sys_s_localization
    drop constraint crs_sys_s_localization_uk01 cascade
/
alter table crs_sys_s_localization
    add constraint crs_sys_s_localization_uk01 unique (h_id, ldts)
    using index compress 1
    tablespace spoindx
/
create index crs_l_client_ogrn_i01 on crs_l_client_ogrn (client_id, ldts) compress 1
tablespace spoindx
/
create index crs_l_client_ogrn_i02 on crs_l_client_ogrn (client_ogrn_id, ldts) compress 1
tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-525-client-idx-proc logicalFilePath:crs-1.0-VTBCRS-525-client-idx-proc endDelimiter:/
-- install simple first version
create or replace procedure crs_client_key_data_vc( p_rowid in rowid, p_idx_data in out varchar2)
is
    v_element varchar2(32767);
    v_key crs_h_client.key%type;
    v_h_id crs_h_client.id%type;
    v_ldts timestamp := systimestamp;

    procedure i_writeAppend(p_add varchar2) is
    begin
        p_idx_data := p_idx_data || p_add;
    end;

begin
    begin
        select key, h_id into v_key, v_h_id
        from (select eh.key,
                  es.h_id,
                  es.removed,
                  row_number() over(partition by es.h_id order by es.ldts desc) rn
              from crs_h_CLIENT eh
                  join crs_s_CLIENT es on eh.id = es.h_id
                                          and es.ldts <= v_ldts
              where eh.rowid = p_rowid
             ) e
        where e.rn = 1
              and e.removed = 0;
        v_element := 'SLX:' || v_key || ':SLX';
        i_writeAppend( v_element||' ');
    exception when no_data_found then
        return;
    end;
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-525-client-idx-pref logicalFilePath:crs-1.0-VTBCRS-525-client-idx-pref endDelimiter:/
begin
    for r in (select pre_name from ctx_preferences where pre_owner = user and pre_name in ('CLIENT_SEARCH_DATASTORE','CLIENT_SEARCH_LEXER')) loop
        ctx_ddl.drop_preference(r.pre_name);
    end loop;
end;
/
begin
    ctx_ddl.create_preference('CLIENT_SEARCH_DATASTORE', 'user_datastore');
    ctx_ddl.set_attribute('CLIENT_SEARCH_DATASTORE', 'procedure', 'crs_client_key_data_vc');
    ctx_ddl.set_attribute('CLIENT_SEARCH_DATASTORE', 'output_type', 'varchar2');

    ctx_ddl.create_preference('CLIENT_SEARCH_LEXER','BASIC_LEXER');
    ctx_ddl.set_attribute('CLIENT_SEARCH_LEXER','MIXED_CASE','NO');
    ctx_ddl.set_attribute('CLIENT_SEARCH_LEXER', 'skipjoins', '-');
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-525-client-idx logicalFilePath:crs-1.0-VTBCRS-525-client-idx endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(1) from user_indexes where index_name = 'CRS_H_CLIENT_I01'
-- do not recreate index if it exists
create index crs_h_client_i01 on crs_h_client (key)
indextype is ctxsys.context
parameters ('LEXER CLIENT_SEARCH_LEXER DATASTORE CLIENT_SEARCH_DATASTORE FILTER CTXSYS.NULL_FILTER')
/

--changeset pmasalov:crs-1.0-VTBCRS-525-client-g-idx-proc logicalFilePath:crs-1.0-VTBCRS-525-client-g-idx-proc endDelimiter:/
-- install simple first version
create or replace procedure crs_client_g_key_data_vc( p_rowid in rowid, p_idx_data in out varchar2)
is
    v_element varchar2(32767);
    v_key crs_h_client.key%type;
    v_h_id crs_h_client.id%type;
    v_ldts timestamp := systimestamp;

    procedure i_writeAppend(p_add varchar2) is
    begin
        p_idx_data := p_idx_data || p_add;
    end;

begin
    begin
        select key, h_id into v_key, v_h_id
          from (select eh.key,
                       es.h_id,
                       es.removed,
                       row_number() over(partition by es.h_id order by es.ldts desc) rn
                  from crs_h_client_group eh
                  join crs_s_client_group es on eh.id = es.h_id
                                      and es.ldts <= v_ldts
                 where eh.rowid = p_rowid
                ) e
         where e.rn = 1
           and e.removed = 0;
        v_element := 'SLX:' || v_key || ':SLX';
        i_writeAppend( v_element||' ');
    exception when no_data_found then
        return;
    end;

end crs_client_g_key_data_vc;
/

--changeset pmasalov:crs-1.0-VTBCRS-525-client-g-idx-pref logicalFilePath:crs-1.0-VTBCRS-525-client-g-idx-pref endDelimiter:/
begin
    for r in (select pre_name from ctx_preferences where pre_owner = user and pre_name in ('CLIENT_G_SEARCH_DATASTORE','CLIENT_G_SEARCH_LEXER')) loop
        ctx_ddl.drop_preference(r.pre_name);
    end loop;
end;
/
begin
    ctx_ddl.create_preference('CLIENT_G_SEARCH_DATASTORE', 'user_datastore');
    ctx_ddl.set_attribute('CLIENT_G_SEARCH_DATASTORE', 'procedure', 'crs_client_g_key_data_vc');
    ctx_ddl.set_attribute('CLIENT_G_SEARCH_DATASTORE', 'output_type', 'varchar2');

    ctx_ddl.create_preference('CLIENT_G_SEARCH_LEXER','BASIC_LEXER');
    ctx_ddl.set_attribute('CLIENT_G_SEARCH_LEXER','MIXED_CASE','NO');
    ctx_ddl.set_attribute('CLIENT_G_SEARCH_LEXER', 'skipjoins', '-');
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-525-client-g-idx logicalFilePath:crs-1.0-VTBCRS-525-client-g-idx endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(1) from user_indexes where index_name = 'CRS_H_CLIENT_GROUP_I01'
-- do not recreate index if it exists
create index crs_h_client_group_i01 on crs_h_client_group (key)
indextype is ctxsys.context
parameters ('LEXER CLIENT_G_SEARCH_LEXER DATASTORE CLIENT_G_SEARCH_DATASTORE FILTER CTXSYS.NULL_FILTER')
/


--changeset akirilchev:crs-1.0-VTBCRS-429-dep-sequence logicalFilePath:crs-1.0-VTBCRS-429-dep-sequence endDelimiter:/
declare
    v_greatest number;

    procedure recreate_sequence(p_sequence_name varchar2, p_sequence_value number) is
        v_exists number := 0;
        v_current_id number := 0;
    begin

        execute immediate 'select ' || p_sequence_name || '.nextval + 1 from dual' into v_current_id;
        if v_current_id <> p_sequence_value then
            execute immediate 'alter sequence ' || p_sequence_name || ' increment by ' || to_char(p_sequence_value - v_current_id);
            execute immediate 'select ' || p_sequence_name || '.nextval from dual' into v_current_id;
            execute immediate 'alter sequence ' || p_sequence_name || ' increment by 1';
        end if;
    exception
        when others then
            dbms_output.enable(null);
            dbms_output.put_line(dbms_utility.format_error_backtrace());
            raise_application_error(-20001, 'recreate_sequence. sequence name "' || p_sequence_name || ', sequence value "'|| to_char(p_sequence_value) ||'": Error is ' || sqlerrm);
    end recreate_sequence;
begin
    select nvl(greatest(max(to_number(h.key)), max(h.id)), 1)
      into v_greatest
      from crs_h_department h;
    recreate_sequence('crs_h_department_seq', v_greatest + 20000);
end;
/

--changeset imatushak:crs-1.0-VTBCRS-429-dep-view-order logicalFilePath:crs-1.0-VTBCRS-429-dep-view-order endDelimiter:/
update crs_sys_s_attribute
   set view_order = 1
 where h_id = (select id from crs_sys_h_attribute where key = 'DEPARTMENT#NAME')
/
update crs_sys_s_attribute
   set view_order = 2
 where h_id = (select id from crs_sys_h_attribute where key = 'DEPARTMENT#FULL_NAME')
/
update crs_sys_s_attribute
  set view_order = 3
where h_id = (select id from crs_sys_h_attribute where key = 'DEPARTMENT#COMMENT')
/

--changeset akirilchev:crs-1.0-VTBCRS-429-user-view-order logicalFilePath:crs-1.0-VTBCRS-429-user-view-order endDelimiter:/
update crs_sys_s_attribute
   set view_order = 3
 where h_id = (select id from crs_sys_h_attribute where key = 'USER#DEPARTMENT')
/
update crs_sys_s_attribute
   set view_order = 4
 where h_id = (select id from crs_sys_h_attribute where key = 'USER#ROLE')
/
update crs_sys_s_attribute
   set view_order = 5
 where h_id = (select id from crs_sys_h_attribute where key = 'USER#FAVORITES')
/

--changeset akirilchev:crs-1.0-VTBCRS-429-dep-name-attr-key logicalFilePath:crs-1.0-VTBCRS-429-dep-name-attr-key endDelimiter:/
update crs_sys_s_attribute
   set attribute_key = 'DEPARTMENT#NAME'
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'USER#DEPARTMENT')
/

--changeset akirilchev:crs-1.0-VTBCRS-521 logicalFilePath:crs-1.0-VTBCRS-521 endDelimiter:/
update crs_sys_s_attribute
   set type = 'DATE'
 where h_id in (select id
                  from crs_sys_h_attribute
                 where key = 'CALC#ACTUALITY')
/
update crs_s_calc
   set actuality = trunc(actuality)
/
alter table crs_s_calc add constraint crs_s_calc_ck04 check (actuality = trunc(actuality))
/
alter table crs_s_calc modify actuality date
/

--changeset akirilchev:crs-1.0-VTBCRS-521-client-dep logicalFilePath:crs-1.0-VTBCRS-521-client-dep endDelimiter:/
declare
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    procedure insert_entity_attribute(
        p_entity_hub_id     crs_sys_h_entity.id%type,
        p_entity_key        crs_sys_h_entity.key%type,
        p_attribute_key     varchar2,
        p_attribute_name_ru crs_sys_s_attribute.name_ru%type,
        p_attribute_name_en crs_sys_s_attribute.name_en%type,
        p_multilang         crs_sys_s_attribute.multilang%type,
        p_link_table        crs_sys_s_attribute.link_table%type,
        p_attribute_type    crs_sys_s_attribute.type%type,
        p_native_column     crs_sys_s_attribute.native_column%type,
        p_ref_entity_key    crs_sys_s_attribute.entity_key%type,
        p_ref_attribute_key crs_sys_s_attribute.attribute_key%type,
        p_ldts              crs_sys_h_attribute.ldts%type
    ) is
    begin
        insert into crs_sys_h_attribute(id, key, ldts)
        values(crs_sys_h_attribute_seq.nextval, p_entity_key || '#' || p_attribute_key, p_ldts);

        insert into crs_sys_s_attribute(id, h_id, ldts, multilang, link_table,
                                        name_ru, name_en, type, native_column, entity_key,
                                        attribute_key)
        values(crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_multilang, p_link_table,
               p_attribute_name_ru, p_attribute_name_en, p_attribute_type, p_native_column, p_ref_entity_key,
               case when p_ref_entity_key is not null and p_ref_attribute_key is not null
                    then p_ref_entity_key || '#' || p_ref_attribute_key
                    else null
               end);

        insert into crs_sys_l_entity_attribute(id, entity_id, attribute_id, ldts)
        values(crs_sys_l_entity_attribute_seq.nextval, p_entity_hub_id, crs_sys_h_attribute_seq.currval, p_ldts);
    end;
begin
    v_entity_key := 'CLIENT';
    select id into v_entity_hub_id
      from crs_sys_h_entity
     where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'DEPARTMENT', 'Подразделение', 'Department', 0, 'crs_l_client_department', 'REFERENCE', null, 'DEPARTMENT', 'NAME', v_ldts);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-537-role-embedded logicalFilePath:crs-1.0-VTBCRS-537-role-embedded endDelimiter:/
alter table crs_s_role add embedded number(1) default 0 not null
/
comment on column crs_s_role.embedded is 'Embedded role'
/
alter table crs_s_role add constraint crs_s_role_ck02 check (embedded in (0,1))
/

--changeset pmasalov:crs-1.0-VTBCRS-537-role-embedded-meta logicalFilePath:crs-1.0-VTBCRS-537-role-embedded-meta endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ROLE';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ROLE#EMBEDDED',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Встроенная роль',
            'Embedded role',
            1,
            null,
            0,
            'BOOLEAN',
            'EMBEDDED');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-537-role-embedded-data logicalFilePath:crs-1.0-VTBCRS-537-role-embedded-data endDelimiter:/
update crs_s_role set embedded = 1 where h_id in (select id from crs_h_role where key in ('АДМ', 'АДМ_ПОЛЬЗ', 'АДМ_РОЛИ'))
/
--changeset pmasalov:crs-1.0-VTBCRS-530-allowed-actions logicalFilePath:crs-1.0-VTBCRS-530-allowed-actions endDelimiter:/
create table crs_sys_h_business_action(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_sys_h_business_action_pk   primary key(id) using index tablespace spoindx,
    constraint crs_sys_h_business_action_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_sys_h_business_action_seq
/
comment on table crs_sys_h_business_action is 'Business action hub'
/
comment on column crs_sys_h_business_action.id is 'Identifier'
/
comment on column crs_sys_h_business_action.key is 'Key'
/
comment on column crs_sys_h_business_action.ldts is 'Load date'
/
create table crs_sys_s_business_action(
    id          number              not null,
    h_id        number              not null,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    constraint crs_sys_s_business_action_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_sys_s_business_action_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_sys_s_business_action_fk01 foreign key(h_id)  references crs_sys_h_business_action(id),
    constraint crs_sys_s_business_action_ck01 check(removed in (0, 1))
)
/
create sequence crs_sys_s_business_action_seq
/
comment on table crs_sys_s_business_action is 'Business action satellite'
/
comment on column crs_sys_s_business_action.id is 'Identifier'
/
comment on column crs_sys_s_business_action.h_id is 'Reference to hub'
/
comment on column crs_sys_s_business_action.digest is 'Row digest'
/
comment on column crs_sys_s_business_action.removed is 'Removed flag'
/
comment on column crs_sys_s_business_action.ldts is 'Load date'
/
create table crs_sys_l_business_action_name(
    id              number              not null,
    business_action_id number           not null,
    localization_id number              not null,
    removed         number(1) default 0 not null,
    ldts            timestamp           not null,
    constraint crs_sys_l_business_action_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_sys_l_business_action_name_fk01 foreign key(business_action_id) references crs_sys_h_business_action(id),
    constraint crs_sys_l_business_action_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_sys_l_business_action_name_ck01 check(removed in (0, 1))
)
/
create sequence crs_sys_l_business_action_name_seq
/
comment on table crs_sys_l_business_action_name is 'Business action to multilanguage name link'
/
comment on column crs_sys_l_business_action_name.business_action_id is 'Reference to business action hub'
/
comment on column crs_sys_l_business_action_name.localization_id is 'Reference to localization hub'
/
comment on column crs_sys_l_business_action_name.removed is 'Removed flag'
/
comment on column crs_sys_l_business_action_name.ldts is 'Load date'
/
create index crs_sys_l_business_action_name_i01 on crs_sys_l_business_action_name(business_action_id, ldts) compress 1 tablespace spoindx
/
create index crs_sys_l_business_action_name_i02 on crs_sys_l_business_action_name(localization_id, ldts) compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-530-allowed-actions-dict-data logicalFilePath:crs-1.0-VTBCRS-530-allowed-actions-dict-data endDelimiter:/
declare
    v_ldts timestamp := systimestamp;

    procedure insert_business_action(
        p_business_action_key     crs_sys_h_business_action.key%type,
        p_business_action_name_ru crs_sys_s_localization.string_ru%type,
        p_business_action_name_en crs_sys_s_localization.string_en%type,
        p_ldts         timestamp
    ) is
        begin
            insert into crs_sys_h_business_action(id, key, ldts)
            values(crs_sys_h_business_action_seq.nextval, p_business_action_key, p_ldts);
            insert into crs_sys_s_business_action(id, h_id, digest, ldts)
            values(crs_sys_s_business_action_seq.nextval, crs_sys_h_business_action_seq.currval, 'NO_DIGEST', p_ldts);

            insert into crs_sys_h_localization(id, key, ldts)
            values(crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_ldts);
            insert into crs_sys_s_localization(id, h_id, ldts, string_ru, string_en, digest)
            values(crs_sys_s_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_ldts, p_business_action_name_ru, p_business_action_name_en, 'NO DIGEST');
            insert into crs_sys_l_business_action_name(id, business_action_id, localization_id, ldts)
            values(crs_sys_l_business_action_name_seq.nextval, crs_sys_h_business_action_seq.currval, crs_sys_h_localization_seq.currval, p_ldts);
        end;
begin
    insert_business_action('CREATE_NEW', 'Создание', 'Create New', v_ldts);
    insert_business_action('SEARCH', 'Поиск', 'Search', v_ldts);
    insert_business_action('LOOK_UP', 'Просмотр', 'Look up', v_ldts);
    insert_business_action('EDIT', 'Редактирование', 'Edit', v_ldts);
    insert_business_action('REMOVE', 'Удаление', 'Remove', v_ldts);
    insert_business_action('EXECUTE', 'Выполнение', 'Execute', v_ldts);
    insert_business_action('PUBLISH', 'Публикация', 'Publish', v_ldts);
    insert_business_action('CREATE_COPY', 'Создание копии', 'Create Copy', v_ldts);
    insert_business_action('USE_AT_CALC', 'Использование в Расчете', 'Use at Calcutation', v_ldts);
end;
/


--changeset pmasalov:crs-1.0-VTBCRS-530-allowed-actions-link logicalFilePath:crs-1.0-VTBCRS-530-allowed-actions-link endDelimiter:/
create table crs_sys_l_entity_business_action(
    id      number       not null,
    entity_id number       not null,
    business_action_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_sys_l_entity_business_action_pk   primary key (id) using index tablespace spoindx,
    constraint crs_sys_l_entity_business_action_fk01 foreign key (entity_id) references crs_sys_h_entity(id),
    constraint crs_sys_l_entity_business_action_fk02 foreign key (business_action_id) references crs_sys_h_business_action(id),
    constraint crs_sys_l_entity_business_action_ck01 check (removed in (0, 1))
)
/
create sequence crs_sys_l_entity_business_action_seq
/
comment on table crs_sys_l_entity_business_action is 'Entity meta to business action link'
/
comment on column crs_sys_l_entity_business_action.id is 'Identifier'
/
comment on column crs_sys_l_entity_business_action.ldts is 'Load date'
/
comment on column crs_sys_l_entity_business_action.removed is 'Removed flag'
/
comment on column crs_sys_l_entity_business_action.entity_id is 'Reference to entity meta hub'
/
comment on column crs_sys_l_entity_business_action.business_action_id is 'Reference to business action_id hub'
/
create index crs_sys_l_entity_business_action_i01 on crs_sys_l_entity_business_action(entity_id, ldts) compress 1 tablespace spoindx
/
create index crs_sys_l_entity_business_action_i02 on crs_sys_l_entity_business_action(business_action_id, ldts) compress 1 tablespace spoindx
/

create table crs_sys_l_entity_type_business_action(
    id      number       not null,
    entity_type_id number       not null,
    business_action_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_sys_l_entity_type_business_action_pk   primary key (id) using index tablespace spoindx,
    constraint crs_sys_l_entity_type_business_action_fk01 foreign key (entity_type_id) references crs_sys_h_entity_type(id),
    constraint crs_sys_l_entity_type_business_action_fk02 foreign key (business_action_id) references crs_sys_h_business_action(id),
    constraint crs_sys_l_entity_type_business_action_ck01 check (removed in (0, 1))
)
/
create sequence crs_sys_l_entity_type_business_action_seq
/
comment on table crs_sys_l_entity_type_business_action is 'Entity type to business action link'
/
comment on column crs_sys_l_entity_type_business_action.id is 'Identifier'
/
comment on column crs_sys_l_entity_type_business_action.ldts is 'Load date'
/
comment on column crs_sys_l_entity_type_business_action.removed is 'Removed flag'
/
comment on column crs_sys_l_entity_type_business_action.entity_type_id is 'Reference to entity type hub'
/
comment on column crs_sys_l_entity_type_business_action.business_action_id is 'Reference to business action hub'
/
create index crs_sys_l_entity_type_business_action_i01 on crs_sys_l_entity_type_business_action(entity_type_id, ldts) compress 1 tablespace spoindx
/
create index crs_sys_l_entity_type_business_action_i02 on crs_sys_l_entity_type_business_action(business_action_id, ldts) compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-530-business-actions-metadata logicalFilePath:crs-1.0-VTBCRS-530-business-actions-metadata endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'BUSINESS_ACTION', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Производственное действите',
            'Business action',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, entity_type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'BUSINESS_ACTION';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'BUSINESS_ACTION#NAME',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            1,
            null,
            null,
            'Наименование',
            'Name',
            1,
            null,
            0,
            'STRING',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-530-allowed-actions-link-metadata logicalFilePath:crs-1.0-VTBCRS-530-allowed-actions-link-metadata endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY#BUSINESS_ACTION',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            'BUSINESS_ACTION#NAME',
            'Допустимые операции',
            'Allowed operations',
            0,
            'BUSINESS_ACTION',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'ENTITY_TYPE';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY_TYPE#BUSINESS_ACTION',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            null,
            'BUSINESS_ACTION#NAME',
            'Допустимые операции',
            'Allowed operations',
            0,
            'BUSINESS_ACTION',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-530-allowed-actions-link-metadata-upd0 logicalFilePath:crs-1.0-VTBCRS-530-allowed-actions-link-metadata-upd0 endDelimiter:/
alter table crs_sys_s_attribute modify link as (1)
/
alter table crs_sys_s_attribute modify link_table varchar2(128)
/
alter table crs_sys_s_attribute modify link as (nvl2(link_table,1,0))
/

--changeset pmasalov:crs-1.0-VTBCRS-530-allowed-actions-link-metadata-upd logicalFilePath:crs-1.0-VTBCRS-530-allowed-actions-link-metadata-upd endDelimiter:/
update crs_sys_s_attribute set link_table = 'crs_sys_l_entity_type_business_action'
where h_id = (select id from crs_sys_h_attribute where key = 'ENTITY_TYPE#BUSINESS_ACTION')
/
update crs_sys_s_attribute set link_table = 'crs_sys_l_entity_business_action'
where h_id = (select id from crs_sys_h_attribute where key = 'ENTITY#BUSINESS_ACTION')
/

--changeset pmasalov:crs-1.0-VTBCRS-530-business-actions-metadata-upd logicalFilePath:crs-1.0-VTBCRS-530-business-actions-metadata-upd endDelimiter:/
update crs_sys_s_attribute set link_table = 'crs_sys_l_business_action_name'
where h_id = (select id from crs_sys_h_attribute where key = 'BUSINESS_ACTION#NAME')
/

--changeset pmasalov:crs-1.0-VTBCRS-530-allowed-actions-link-data logicalFilePath:crs-1.0-VTBCRS-530-allowed-actions-link-data endDelimiter:/
declare
    v_ldts timestamp := to_date('011111', 'mmyyyy');

    procedure link_entity_type(p_entity_type_key varchar2, p_business_action_key varchar2) is
    begin
        insert into crs_sys_l_entity_type_business_action (id, entity_type_id, business_action_id, ldts)
        values (crs_sys_l_entity_type_business_action_seq.nextval,
                (select id from crs_sys_h_entity_type where key = p_entity_type_key),
                (select id from crs_sys_h_business_action where key = p_business_action_key),
                v_ldts);
    end;

    procedure link_entity(p_entity_key varchar2, p_business_action_key varchar2) is
    begin
        insert into crs_sys_l_entity_business_action(id, entity_id, business_action_id, ldts)
        values (crs_sys_l_entity_business_action_seq.nextval,
                (select id from crs_sys_h_entity where key = p_entity_key),
                (select id from crs_sys_h_business_action where key = p_business_action_key),
                v_ldts);

    end;

begin
    -- Справочник -> Просмотр   Редактирование   Удаление    Использование в Расчете
    link_entity_type('DICTIONARY', 'SEARCH');
    link_entity_type('DICTIONARY', 'LOOK_UP');
    link_entity_type('DICTIONARY', 'EDIT');
    link_entity_type('DICTIONARY', 'REMOVE');
    link_entity_type('DICTIONARY', 'USE_AT_CALC');

    -- Форма ввода данных ->  Создание	Просмотр	Редактирование	Удаление     Публикация    Использование в Расчете
    link_entity_type('INPUT_FORM', 'CREATE_NEW');
    link_entity_type('INPUT_FORM', 'SEARCH');
    link_entity_type('INPUT_FORM', 'LOOK_UP');
    link_entity_type('INPUT_FORM', 'EDIT');
    link_entity_type('INPUT_FORM', 'REMOVE');
    link_entity_type('INPUT_FORM', 'USE_AT_CALC');

    -- Классификаторы  -> Создание	Просмотр	Редактирование	Удаление   Использование в Расчете
    link_entity_type('CLASSIFIER', 'CREATE_NEW');
    link_entity_type('CLASSIFIER', 'SEARCH');
    link_entity_type('CLASSIFIER', 'LOOK_UP');
    link_entity_type('CLASSIFIER', 'EDIT');
    link_entity_type('CLASSIFIER', 'REMOVE');
    link_entity_type('CLASSIFIER', 'USE_AT_CALC');

    -- Клиент ->  Просмотр  Использование в Расчете
    link_entity('CLIENT', 'SEARCH');
    link_entity('CLIENT', 'LOOK_UP');
    link_entity('CLIENT', 'USE_AT_CALC');

    -- Расчет ->  Создание	Просмотр	Редактирование	Удаление	Выполнение	Публикация	Создание копии
    link_entity('CALC', 'CREATE_NEW');
    link_entity('CALC', 'SEARCH');
    link_entity('CALC', 'LOOK_UP');
    link_entity('CALC', 'EDIT');
    link_entity('CALC', 'REMOVE');
    link_entity('CALC', 'EXECUTE');
    link_entity('CALC', 'PUBLISH');
    link_entity('CALC', 'CREATE_COPY');
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-530-permission logicalFilePath:crs-1.0-VTBCRS-530-permission endDelimiter:/
create table crs_h_permission(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_permission_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_permission_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_permission_seq
/
comment on table crs_h_permission is 'Role permission hub'
/
comment on column crs_h_permission.id is 'Identifier'
/
comment on column crs_h_permission.key is 'Key'
/
comment on column crs_h_permission.ldts is 'Load date'
/
create table crs_s_permission(
    id          number              not null,
    h_id        number              not null,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    permit      number(1),
    constraint crs_s_permission_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_permission_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_permission_fk01 foreign key(h_id)  references crs_h_permission(id),
    constraint crs_s_permission_ck01 check(removed in (0, 1)),
    constraint crs_s_permission_ck02 check(removed in (0, 1))
)
/
create sequence crs_s_permission_seq
/
comment on table crs_s_permission is 'Role permission satellite'
/
comment on column crs_s_permission.id is 'Identifier'
/
comment on column crs_s_permission.h_id is 'Reference to hub'
/
comment on column crs_s_permission.digest is 'Row digest'
/
comment on column crs_s_permission.removed is 'Removed flag'
/
comment on column crs_s_permission.ldts is 'Load date'
/
comment on column crs_s_permission.permit is 'Permission flag'
/

create table crs_l_permission_role(
    id      number       not null,
    permission_id number       not null,
    role_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_l_permission_role_pk   primary key (id) using index tablespace spoindx,
    constraint crs_l_permission_role_fk01 foreign key (permission_id) references crs_h_permission(id),
    constraint crs_l_permission_role_fk02 foreign key (role_id) references crs_h_role(id),
    constraint crs_l_permission_role_ck01 check (removed in (0, 1))
)
/
create sequence crs_l_permission_role_seq
/
comment on table crs_l_permission_role is 'Role permission to role link'
/
comment on column crs_l_permission_role.id is 'Identifier'
/
comment on column crs_l_permission_role.ldts is 'Load date'
/
comment on column crs_l_permission_role.removed is 'Removed flag'
/
comment on column crs_l_permission_role.permission_id is 'Reference to role permission hub'
/
comment on column crs_l_permission_role.role_id is 'Reference to role hub'
/
create index crs_l_permission_role_i01 on crs_l_permission_role(permission_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_permission_role_i02 on crs_l_permission_role(role_id, ldts) compress 1 tablespace spoindx
/

create table crs_l_permission_entity(
    id number not null,
    permission_id number not null,
    entity_id number not null,
    removed number(1, 0) default 0 not null,
    ldts timestamp not null,
    constraint crs_l_permission_entity_pk   primary key (id) using index tablespace spoindx,
    constraint crs_l_permission_entity_fk01 foreign key (permission_id) references crs_h_permission(id),
    constraint crs_l_permission_entity_fk02 foreign key (entity_id) references crs_sys_h_entity(id),
    constraint crs_l_permission_entity_ck01 check (removed in (0, 1))
)
/
create sequence crs_l_permission_entity_seq
/
comment on table crs_l_permission_entity is 'Role permission to entity link'
/
comment on column crs_l_permission_entity.id is 'Identifier'
/
comment on column crs_l_permission_entity.ldts is 'Load date'
/
comment on column crs_l_permission_entity.removed is 'Removed flag'
/
comment on column crs_l_permission_entity.permission_id is 'Reference to role permission hub'
/
comment on column crs_l_permission_entity.entity_id is 'Reference to entity hub'
/
create index crs_l_permission_entity_i01 on crs_l_permission_entity(permission_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_permission_entity_i02 on crs_l_permission_entity(entity_id, ldts) compress 1 tablespace spoindx
/

create table crs_l_permission_business_action(
    id      number       not null,
    permission_id number       not null,
    business_action_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_l_permission_business_action_pk   primary key (id) using index tablespace spoindx,
    constraint crs_l_permission_business_action_fk01 foreign key (permission_id) references crs_h_permission(id),
    constraint crs_l_permission_business_action_fk02 foreign key (business_action_id) references crs_sys_h_business_action(id),
    constraint crs_l_permission_business_action_ck01 check (removed in (0, 1))
)
/
create sequence crs_l_permission_business_action_seq
/
comment on table crs_l_permission_business_action is 'Role permission to business action link'
/
comment on column crs_l_permission_business_action.id is 'Identifier'
/
comment on column crs_l_permission_business_action.ldts is 'Load date'
/
comment on column crs_l_permission_business_action.removed is 'Removed flag'
/
comment on column crs_l_permission_business_action.permission_id is 'Reference to role permission hub'
/
comment on column crs_l_permission_business_action.business_action_id is 'Reference to business action hub'
/
create index crs_l_permission_business_action_i01 on crs_l_permission_business_action(permission_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_permission_business_action_i02 on crs_l_permission_business_action(business_action_id, ldts) compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.0-VTBCRS-530-permission-meta logicalFilePath:crs-1.0-VTBCRS-530-permission-meta endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'PERMISSION', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Права доступа',
            'Permission',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, entity_type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'PERMISSION';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'PERMISSION#BUSINESS_ACTION',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            0,
            0,
            'crs_l_permission_business_action',
            'BUSINESS_ACTION#NAME',
            'Действие',
            'Action',
            0,
            'BUSINESS_ACTION',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'PERMISSION';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'PERMISSION#ENTITY',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            'crs_l_permission_entity',
            null,
            'Сущьность',
            'Entity',
            0,
            'ENTITY',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'PERMISSION';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'PERMISSION#ROLE',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            'crs_l_permission_role',
            'ROLE#NAME',
            'Роль',
            'Role',
            0,
            'ROLE',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-530-permission-meta2 logicalFilePath:crs-1.0-VTBCRS-530-permission-meta2 endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'PERMISSION';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'PERMISSION#PERMIT',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_h_attribute_id,
            v_ldts,
            0,
            1,
            0,
            null,
            null,
            'Разрешение',
            'Permit',
            0,
            null,
            0,
            'BOOLEAN',
            'PERMIT');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.0-VTBCRS-530-alter-entity logicalFilePath:crs-1.0-VTBCRS-530-alter-entity endDelimiter:/
alter table crs_sys_s_entity add digest varchar2(100)
/
comment on column crs_sys_s_entity.digest is 'Digest of row. For compatability'
/

--changeset pmasalov:crs-1.0-VTBCRS-530-alter-permission logicalFilePath:crs-1.0-VTBCRS-530-alter-permission endDelimiter:/
update crs_sys_s_attribute
set nullable = 0
where h_id in (select id
               from crs_sys_h_attribute
               where key = 'PERMISSION#PERMIT')
/
alter table crs_s_permission modify permit not null
/

--changeset pmasalov:crs-1.0-VTBCRS-530-permission-data logicalFilePath:crs-1.0-VTBCRS-530-permission-data endDelimiter:/
declare
    v_ldts timestamp := to_date('011111', 'mmyyyy');

    procedure insert_permission(p_role varchar2, p_entity varchar2, p_action varchar2, p_permit number default 1) is
        begin
            insert into crs_h_permission (id, key, ldts) values (crs_h_permission_seq.nextval, crs_h_permission_seq.currval, v_ldts);
            insert into crs_s_permission (id, h_id, digest, removed, ldts, permit)
            values (crs_s_permission_seq.nextval, crs_h_permission_seq.currval, 'NO_DIGEST', 0, v_ldts, p_permit);
            insert into crs_l_permission_role (id, permission_id, role_id, removed, ldts)
            values (crs_l_permission_role_seq.nextval, crs_h_permission_seq.currval, (select id from crs_h_role where key = p_role), 0, v_ldts);
            insert into crs_l_permission_entity (id, permission_id, entity_id, removed, ldts)
            values (crs_l_permission_entity_seq.nextval, crs_h_permission_seq.currval, (select id from crs_sys_h_entity where key = p_entity), 0, v_ldts);
            insert into crs_l_permission_business_action (id, permission_id, business_action_id, removed, ldts)
            values (crs_l_permission_business_action_seq.nextval, crs_h_permission_seq.currval, (select id from crs_sys_h_business_action where key = p_action), 0, v_ldts);
        end;

begin
    insert_permission('АДМ', 'CLIENT', 'SEARCH');
    insert_permission('АДМ', 'CLIENT', 'LOOK_UP');
    insert_permission('АДМ', 'CLIENT_GROUP', 'SEARCH');
    insert_permission('АДМ', 'CLIENT_GROUP', 'LOOK_UP');

    insert_permission('АДМ', 'CALC', 'CREATE_NEW');
    insert_permission('АДМ', 'CALC', 'SEARCH');
    insert_permission('АДМ', 'CALC', 'LOOK_UP');
    insert_permission('АДМ', 'CALC', 'EDIT');
    insert_permission('АДМ', 'CALC', 'REMOVE');
    insert_permission('АДМ', 'CALC', 'EXECUTE');
    insert_permission('АДМ', 'CALC', 'PUBLISH');
    insert_permission('АДМ', 'CALC', 'CREATE_COPY');

    insert_permission('АДМ', 'CALC_MODEL', 'SEARCH');
    insert_permission('АДМ', 'CALC_MODEL', 'LOOK_UP');

    insert_permission('АДМ', 'ENTITY', 'SEARCH');
    insert_permission('АДМ', 'ENTITY', 'LOOK_UP');
    insert_permission('АДМ', 'ENTITY_GROUP', 'SEARCH');
    insert_permission('АДМ', 'ENTITY_GROUP', 'LOOK_UP');

    insert_permission('АДМ_ПОЛЬЗ', 'USER', 'CREATE_NEW');
    insert_permission('АДМ_ПОЛЬЗ', 'USER', 'SEARCH');
    insert_permission('АДМ_ПОЛЬЗ', 'USER', 'LOOK_UP');
    insert_permission('АДМ_ПОЛЬЗ', 'USER', 'EDIT');
    insert_permission('АДМ_ПОЛЬЗ', 'USER', 'REMOVE');

    insert_permission('АДМ_РОЛИ', 'ROLE', 'CREATE_NEW');
    insert_permission('АДМ_РОЛИ', 'ROLE', 'SEARCH');
    insert_permission('АДМ_РОЛИ', 'ROLE', 'LOOK_UP');
    insert_permission('АДМ_РОЛИ', 'ROLE', 'EDIT');
    insert_permission('АДМ_РОЛИ', 'ROLE', 'REMOVE');

    insert_permission('АДМ_РОЛИ', 'PERMISSION', 'CREATE_NEW');
    insert_permission('АДМ_РОЛИ', 'PERMISSION', 'SEARCH');
    insert_permission('АДМ_РОЛИ', 'PERMISSION', 'LOOK_UP');
    insert_permission('АДМ_РОЛИ', 'PERMISSION', 'EDIT');
    insert_permission('АДМ_РОЛИ', 'PERMISSION', 'REMOVE');

    insert_permission('АДМ_РОЛИ', 'USER', 'CREATE_NEW', 0);
    insert_permission('АДМ_РОЛИ', 'USER', 'SEARCH');
    insert_permission('АДМ_РОЛИ', 'USER', 'LOOK_UP');
    insert_permission('АДМ_РОЛИ', 'USER', 'EDIT', 0);
    insert_permission('АДМ_РОЛИ', 'USER', 'REMOVE', 0);

end;
/

