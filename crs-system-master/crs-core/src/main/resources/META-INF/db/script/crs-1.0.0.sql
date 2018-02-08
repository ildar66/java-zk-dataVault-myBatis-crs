--liquibase formatted sql

--changeset svaliev:crs-1.0-VTBCRS-4 logicalFilePath:crs-1.0-VTBCRS-4 endDelimiter:/
create table crs_localized_name_meta (
    localized_name_meta_id number not null,
    name_ru                varchar2(4000) not null,
    name_en                varchar2(4000) not null
)
/
comment on table crs_localized_name_meta is 'Metadata localization'
/
comment on column crs_localized_name_meta.localized_name_meta_id is 'Identifier'
/
comment on column crs_localized_name_meta.name_ru is 'Name (ru)'
/
comment on column crs_localized_name_meta.name_en is 'Name (en)'
/
alter table crs_localized_name_meta
  add constraint crs_localized_name_meta_pk primary key (localized_name_meta_id)
/
create sequence crs_localized_name_meta_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/


create table crs_column_type_meta
(
    column_type_meta_id    number not null,
    key                    varchar2(100) not null,
    localized_name_meta_id number not null
)
/
comment on table crs_column_type_meta is 'Column types metadata'
/
comment on column crs_column_type_meta.column_type_meta_id is 'Identifier'
/
comment on column crs_column_type_meta.key is 'Column type key'
/
comment on column crs_column_type_meta.localized_name_meta_id is 'Localized name reference'
/
create index crs_column_type_meta_i01 on crs_column_type_meta (localized_name_meta_id) tablespace spoindx
/
create unique index crs_column_type_meta_uk01 on crs_column_type_meta (key) tablespace spoindx
/
alter table crs_column_type_meta
  add constraint crs_column_type_meta_pk primary key (column_type_meta_id)
/
alter table crs_column_type_meta
  add constraint crs_column_type_meta_fk01 foreign key (localized_name_meta_id)
  references crs_localized_name_meta (localized_name_meta_id)
/
create sequence crs_column_type_meta_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/


create table crs_table_meta
(
    table_meta_id          number not null,
    key                    varchar2(30) not null,
    hierarchy              number(1) default 0 not null,
    versioning             number(1) default 0 not null,
    localized_name_meta_id number,
    relationship_table     number(1) default 0 not null,
    ref_table_meta_id      number,
    type                   varchar2(100),
    form                   varchar2(100)
)
/
comment on table crs_table_meta is 'Tables metadata'
/
comment on column crs_table_meta.table_meta_id is 'Identifier'
/
comment on column crs_table_meta.key is 'Database table name'
/
comment on column crs_table_meta.hierarchy is 'Hierarchy flag'
/
comment on column crs_table_meta.versioning is 'Versioning flag'
/
comment on column crs_table_meta.localized_name_meta_id is 'Localized name reference'
/
comment on column crs_table_meta.relationship_table is 'Many-to-many relationship table flag'
/
comment on column crs_table_meta.ref_table_meta_id is 'Reference to relationship table'
/
comment on column crs_table_meta.type is 'Table type'
/
comment on column crs_table_meta.form is 'Form name'
/
create index crs_table_meta_i01 on crs_table_meta (localized_name_meta_id) tablespace spoindx
/
create index crs_table_meta_i02 on crs_table_meta (ref_table_meta_id) tablespace spoindx
/
create unique index crs_table_meta_uk01 on crs_table_meta (key) tablespace spoindx
/
alter table crs_table_meta
  add constraint crs_table_meta_pk primary key (table_meta_id)
/
alter table crs_table_meta
  add constraint crs_table_meta_fk01 foreign key (localized_name_meta_id)
  references crs_localized_name_meta (localized_name_meta_id)
/
alter table crs_table_meta
  add constraint crs_table_meta_fk02 foreign key (ref_table_meta_id)
  references crs_table_meta (table_meta_id)
/
alter table crs_table_meta
  add constraint crs_table_meta_ck01
  check (hierarchy in (0, 1))
/
alter table crs_table_meta
  add constraint crs_table_meta_ck02
  check (versioning in (0, 1))
/
alter table crs_table_meta
  add constraint crs_table_meta_ck03
  check (relationship_table in (0, 1))
/
alter table crs_table_meta
  add constraint crs_table_meta_ck04
  check ((localized_name_meta_id is null and relationship_table = 1 and type is null)
         or (localized_name_meta_id is not null and relationship_table = 0 and type is not null))
/
create sequence crs_table_meta_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/


create table crs_column_meta
(
    column_meta_id         number not null,
    key                    varchar2(30) not null,
    table_meta_id          number not null,
    multilang              number(1) default 0 not null,
    localized_name_meta_id number not null,
    column_type_meta_id    number not null,
    view_order             number not null,
    ref_column_meta_id     number,
    ref_table              varchar2(30),
    ref_column             varchar2(30)
)
/
comment on table crs_column_meta is 'Columns metadata'
/
comment on column crs_column_meta.column_meta_id is 'Identifier'
/
comment on column crs_column_meta.key is 'Table column name'
/
comment on column crs_column_meta.table_meta_id is 'Table meta reference'
/
comment on column crs_column_meta.multilang is 'Multilang column'
/
comment on column crs_column_meta.localized_name_meta_id is 'Localized name reference'
/
comment on column crs_column_meta.column_type_meta_id is 'Column type reference'
/
comment on column crs_column_meta.view_order is 'Column view order'
/
comment on column crs_column_meta.ref_column_meta_id is 'Column meta identifier reference'
/
comment on column crs_column_meta.ref_table is 'Table reference'
/
comment on column crs_column_meta.ref_column is 'Table column reference'
/
create index crs_column_meta_i01 on crs_column_meta (table_meta_id) tablespace spoindx
/
create index crs_column_meta_i02 on crs_column_meta (localized_name_meta_id) tablespace spoindx
/
create index crs_column_meta_i03 on crs_column_meta (column_type_meta_id) tablespace spoindx
/
create index crs_column_meta_i04 on crs_column_meta (ref_column_meta_id) tablespace spoindx
/
create unique index crs_column_meta_uk01 on crs_column_meta (key, table_meta_id) tablespace spoindx
/
alter table crs_column_meta
  add constraint crs_column_meta_pk primary key (column_meta_id)
/
alter table crs_column_meta
  add constraint crs_column_meta_fk01 foreign key (table_meta_id)
  references crs_table_meta (table_meta_id)
/
alter table crs_column_meta
  add constraint crs_column_meta_fk02 foreign key (localized_name_meta_id)
  references crs_localized_name_meta (localized_name_meta_id)
/
alter table crs_column_meta
  add constraint crs_column_meta_fk03 foreign key (column_type_meta_id)
  references crs_column_type_meta (column_type_meta_id)
/
alter table crs_column_meta
  add constraint crs_column_meta_fk04 foreign key (ref_column_meta_id)
  references crs_column_meta (column_meta_id)
/
alter table crs_column_meta
  add constraint crs_column_meta_ck01
  check (multilang in (0, 1))
/
alter table crs_column_meta
  add constraint crs_column_meta_ck03
  check ((ref_column_meta_id is not null and ref_table is null and ref_column is null)
         or (ref_column_meta_id is null and ref_table is not null and ref_column is not null))
/
create sequence crs_column_meta_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset svaliev:crs-1.0-VTBCRS-4-metadata logicalFilePath:crs-1.0-VTBCRS-4-metadata endDelimiter:/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Булевый тип', 'Boolean')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'BOOLEAN', crs_localized_name_meta_seq.currval)
/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Строка', 'String')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'STRING', crs_localized_name_meta_seq.currval)
/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Текст', 'Text')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'TEXT', crs_localized_name_meta_seq.currval)
/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Файл', 'File')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'FILE', crs_localized_name_meta_seq.currval)
/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Число', 'Number')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'NUMBER', crs_localized_name_meta_seq.currval)
/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Дата', 'Date')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'DATE', crs_localized_name_meta_seq.currval)
/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Дата и время', 'DateTime')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'DATETIME', crs_localized_name_meta_seq.currval)
/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Ссылка на значение', 'Reference value')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'REFERENCE_VALUE', crs_localized_name_meta_seq.currval)
/
insert into crs_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_localized_name_meta_seq.nextval, 'Ссылка на список значений', 'Reference list')
/
insert into crs_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_column_type_meta_seq.nextval, 'REFERENCE_LIST', crs_localized_name_meta_seq.currval)
/

--changeset svaliev:crs-1.0-VTBCRS-5 logicalFilePath:crs-1.0-VTBCRS-5 endDelimiter:/
alter table crs_table_meta add external number(1) default 0
/
comment on column crs_table_meta.external is 'External table'
/
alter table crs_table_meta add constraint crs_table_meta_ck05 check (external in (0, 1))
/
alter table crs_column_meta rename constraint crs_column_meta_ck03 to crs_column_meta_ck02
/

--changeset svaliev:crs-1.0-drop-structure logicalFilePath:crs-1.0-drop-structure endDelimiter:/
drop table crs_column_meta
/
drop table crs_table_meta
/
drop table crs_column_type_meta
/
drop table crs_localized_name_meta
/
drop sequence crs_column_meta_seq
/
drop sequence crs_table_meta_seq
/
drop sequence crs_column_type_meta_seq
/
drop sequence crs_localized_name_meta_seq
/

--changeset svaliev:crs-1.0-add-SYS-prefix logicalFilePath:crs-1.0-add-SYS-prefix endDelimiter:/
create table crs_sys_localized_name_meta (
    localized_name_meta_id number not null,
    name_ru                varchar2(4000) not null,
    name_en                varchar2(4000) not null
)
/
comment on table crs_sys_localized_name_meta is 'Metadata localization'
/
comment on column crs_sys_localized_name_meta.localized_name_meta_id is 'Identifier'
/
comment on column crs_sys_localized_name_meta.name_ru is 'Name (ru)'
/
comment on column crs_sys_localized_name_meta.name_en is 'Name (en)'
/
alter table crs_sys_localized_name_meta
    add constraint crs_sys_localized_name_meta_pk primary key (localized_name_meta_id)
/
create sequence crs_sys_localized_nm_meta_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/


create table crs_sys_column_type_meta
(
    column_type_meta_id    number not null,
    key                    varchar2(100) not null,
    localized_name_meta_id number not null
)
/
comment on table crs_sys_column_type_meta is 'Column types metadata'
/
comment on column crs_sys_column_type_meta.column_type_meta_id is 'Identifier'
/
comment on column crs_sys_column_type_meta.key is 'Column type key'
/
comment on column crs_sys_column_type_meta.localized_name_meta_id is 'Localized name reference'
/
create index crs_sys_column_type_meta_i01 on crs_sys_column_type_meta (localized_name_meta_id) tablespace spoindx
/
create unique index crs_sys_column_type_meta_uk01 on crs_sys_column_type_meta (key) tablespace spoindx
/
alter table crs_sys_column_type_meta
    add constraint crs_sys_column_type_meta_pk primary key (column_type_meta_id)
/
alter table crs_sys_column_type_meta
    add constraint crs_sys_column_type_meta_fk01 foreign key (localized_name_meta_id)
references crs_sys_localized_name_meta (localized_name_meta_id)
/
create sequence crs_sys_column_type_meta_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/


create table crs_sys_table_meta
(
    table_meta_id          number not null,
    key                    varchar2(30) not null,
    hierarchy              number(1) default 0 not null,
    versioning             number(1) default 0 not null,
    localized_name_meta_id number,
    relationship_table     number(1) default 0 not null,
    ref_table_meta_id      number,
    type                   varchar2(100),
    form                   varchar2(100),
    external               number(1) default 0 not null
)
/
comment on table crs_sys_table_meta is 'Tables metadata'
/
comment on column crs_sys_table_meta.table_meta_id is 'Identifier'
/
comment on column crs_sys_table_meta.key is 'Database table name'
/
comment on column crs_sys_table_meta.hierarchy is 'Hierarchy flag'
/
comment on column crs_sys_table_meta.versioning is 'Versioning flag'
/
comment on column crs_sys_table_meta.localized_name_meta_id is 'Localized name reference'
/
comment on column crs_sys_table_meta.relationship_table is 'Many-to-many relationship table flag'
/
comment on column crs_sys_table_meta.ref_table_meta_id is 'Reference to relationship table'
/
comment on column crs_sys_table_meta.type is 'Table type'
/
comment on column crs_sys_table_meta.form is 'Form name'
/
comment on column crs_sys_table_meta.external is 'External table'
/
create index crs_sys_table_meta_i01 on crs_sys_table_meta (localized_name_meta_id) tablespace spoindx
/
create index crs_sys_table_meta_i02 on crs_sys_table_meta (ref_table_meta_id) tablespace spoindx
/
create unique index crs_sys_table_meta_uk01 on crs_sys_table_meta (key) tablespace spoindx
/
alter table crs_sys_table_meta
    add constraint crs_sys_table_meta_pk primary key (table_meta_id)
/
alter table crs_sys_table_meta
    add constraint crs_sys_table_meta_fk01 foreign key (localized_name_meta_id)
references crs_sys_localized_name_meta (localized_name_meta_id)
/
alter table crs_sys_table_meta
    add constraint crs_sys_table_meta_fk02 foreign key (ref_table_meta_id)
references crs_sys_table_meta (table_meta_id)
/
alter table crs_sys_table_meta
    add constraint crs_sys_table_meta_ck01
check (hierarchy in (0, 1))
/
alter table crs_sys_table_meta
    add constraint crs_sys_table_meta_ck02
check (versioning in (0, 1))
/
alter table crs_sys_table_meta
    add constraint crs_sys_table_meta_ck03
check (relationship_table in (0, 1))
/
alter table crs_sys_table_meta
    add constraint crs_sys_table_meta_ck04
check ((localized_name_meta_id is null and relationship_table = 1 and type is null)
       or (localized_name_meta_id is not null and relationship_table = 0 and type is not null))
/
alter table crs_sys_table_meta add constraint crs_sys_table_meta_ck05 check (external in (0, 1))
/
create sequence crs_sys_table_meta_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/


create table crs_sys_column_meta
(
    column_meta_id         number not null,
    key                    varchar2(30) not null,
    table_meta_id          number not null,
    multilang              number(1) default 0 not null,
    localized_name_meta_id number not null,
    column_type_meta_id    number not null,
    view_order             number not null,
    ref_column_meta_id     number,
    ref_table              varchar2(30),
    ref_column             varchar2(30)
)
/
comment on table crs_sys_column_meta is 'Columns metadata'
/
comment on column crs_sys_column_meta.column_meta_id is 'Identifier'
/
comment on column crs_sys_column_meta.key is 'Table column name'
/
comment on column crs_sys_column_meta.table_meta_id is 'Table meta reference'
/
comment on column crs_sys_column_meta.multilang is 'Multilang column'
/
comment on column crs_sys_column_meta.localized_name_meta_id is 'Localized name reference'
/
comment on column crs_sys_column_meta.column_type_meta_id is 'Column type reference'
/
comment on column crs_sys_column_meta.view_order is 'Column view order'
/
comment on column crs_sys_column_meta.ref_column_meta_id is 'Column meta identifier reference'
/
comment on column crs_sys_column_meta.ref_table is 'Table reference'
/
comment on column crs_sys_column_meta.ref_column is 'Table column reference'
/
create index crs_sys_column_meta_i01 on crs_sys_column_meta (table_meta_id) tablespace spoindx
/
create index crs_sys_column_meta_i02 on crs_sys_column_meta (localized_name_meta_id) tablespace spoindx
/
create index crs_sys_column_meta_i03 on crs_sys_column_meta (column_type_meta_id) tablespace spoindx
/
create index crs_sys_column_meta_i04 on crs_sys_column_meta (ref_column_meta_id) tablespace spoindx
/
create unique index crs_sys_column_meta_uk01 on crs_sys_column_meta (key, table_meta_id) tablespace spoindx
/
alter table crs_sys_column_meta
    add constraint crs_sys_column_meta_pk primary key (column_meta_id)
/
alter table crs_sys_column_meta
    add constraint crs_sys_column_meta_fk01 foreign key (table_meta_id)
references crs_sys_table_meta (table_meta_id)
/
alter table crs_sys_column_meta
    add constraint crs_sys_column_meta_fk02 foreign key (localized_name_meta_id)
references crs_sys_localized_name_meta (localized_name_meta_id)
/
alter table crs_sys_column_meta
    add constraint crs_sys_column_meta_fk03 foreign key (column_type_meta_id)
references crs_sys_column_type_meta (column_type_meta_id)
/
alter table crs_sys_column_meta
    add constraint crs_sys_column_meta_fk04 foreign key (ref_column_meta_id)
references crs_sys_column_meta (column_meta_id)
/
alter table crs_sys_column_meta
    add constraint crs_sys_column_meta_ck01
check (multilang in (0, 1))
/
alter table crs_sys_column_meta
    add constraint crs_sys_column_meta_ck02
check ((ref_column_meta_id is not null and ref_table is null and ref_column is null)
       or (ref_column_meta_id is null and ref_table is not null and ref_column is not null))
/
create sequence crs_sys_column_meta_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Булевый тип', 'Boolean')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'BOOLEAN', crs_sys_localized_nm_meta_seq.currval)
/
insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Строка', 'String')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'STRING', crs_sys_localized_nm_meta_seq.currval)
/
insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Текст', 'Text')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'TEXT', crs_sys_localized_nm_meta_seq.currval)
/
insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Файл', 'File')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'FILE', crs_sys_localized_nm_meta_seq.currval)
/
insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Число', 'Number')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'NUMBER', crs_sys_localized_nm_meta_seq.currval)
/
insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Дата', 'Date')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'DATE', crs_sys_localized_nm_meta_seq.currval)
/
insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Дата и время', 'DateTime')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'DATETIME', crs_sys_localized_nm_meta_seq.currval)
/
insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Ссылка на значение', 'Reference value')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'REFERENCE_VALUE', crs_sys_localized_nm_meta_seq.currval)
/
insert into crs_sys_localized_name_meta (localized_name_meta_id, name_ru, name_en)
values (crs_sys_localized_nm_meta_seq.nextval, 'Ссылка на список значений', 'Reference list')
/
insert into crs_sys_column_type_meta (column_type_meta_id, key, localized_name_meta_id)
values (crs_sys_column_type_meta_seq.nextval, 'REFERENCE_LIST', crs_sys_localized_nm_meta_seq.currval)
/

--changeset svaliev:crs-1.0-VTBCRS-9 logicalFilePath:crs-1.0-VTBCRS-9 endDelimiter:/
create table crs_sys_localized_value
(
    localized_value_id number not null,
    string_ru          varchar2(4000),
    string_en          varchar2(4000),
    text_ru            clob,
    text_en            clob
)
/
comment on table crs_sys_localized_value is 'Values localization'
/
comment on column crs_sys_localized_value.localized_value_id is 'Identifier'
/
comment on column crs_sys_localized_value.string_ru is 'String value (ru)'
/
comment on column crs_sys_localized_value.string_en is 'String value (en)'
/
comment on column crs_sys_localized_value.text_ru is 'Text value (ru)'
/
comment on column crs_sys_localized_value.text_en is 'Text value (en)'
/
alter table crs_sys_localized_value
  add constraint crs_sys_localized_value_pk primary key (localized_value_id)
/
alter table crs_sys_localized_value
  add constraint crs_sys_localized_value_ck01
  check ((string_ru is not null and string_en is not null and text_ru is null and text_en is null)
         or (string_ru is null and string_en is null and text_ru is not null and text_en is not null))
/
create sequence crs_sys_localized_value_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/


create table crs_sys_file_storage
(
    file_storage_id number not null,
    mime_type       varchar2(100) not null,
    name            varchar2(500) not null,
    data            blob not null
)
/
comment on table crs_sys_file_storage is 'File storage'
/
comment on column crs_sys_file_storage.file_storage_id is 'Identifier'
/
comment on column crs_sys_file_storage.mime_type is 'Mime type'
/
comment on column crs_sys_file_storage.name is 'Filename'
/
comment on column crs_sys_file_storage.data is 'Filedata'
/
alter table crs_sys_file_storage
  add constraint crs_sys_file_storage_pk primary key (file_storage_id)
/
create sequence crs_sys_file_storage_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/


create table crs_sys_dictionary_nav
(
    dictionary_nav_id     number not null,
    table_meta_id         number not null,
    ref_dictionary_nav_id number
)
/
comment on table crs_sys_dictionary_nav is 'Dictionary navigation tree'
/
comment on column crs_sys_dictionary_nav.dictionary_nav_id is 'Identifier'
/
comment on column crs_sys_dictionary_nav.table_meta_id is 'Table meta identifier reference'
/
comment on column crs_sys_dictionary_nav.ref_dictionary_nav_id is 'Dictionary navigation reference'
/
create index crs_sys_dictionary_nav_i01 on crs_sys_dictionary_nav (ref_dictionary_nav_id) tablespace spoindx
/
create unique index crs_sys_dictionary_nav_uk01 on crs_sys_dictionary_nav (table_meta_id) tablespace spoindx
/
alter table crs_sys_dictionary_nav
  add constraint crs_sys_dictionary_nav_pk primary key (dictionary_nav_id)
/
alter table crs_sys_dictionary_nav
  add constraint crs_sys_dictionary_nav_fk01 foreign key (ref_dictionary_nav_id)
  references crs_sys_dictionary_nav (dictionary_nav_id)
/
create sequence crs_sys_dictionary_nav_seq increment by 1 start with 0 minvalue 0 nocycle nocache
/

--changeset svaliev:crs-1.0-VTBCRS-14 logicalFilePath:crs-1.0-VTBCRS-14 endDelimiter:/
alter table crs_sys_table_meta drop column ref_table_meta_id
/

--changeset svaliev:crs-1.0-localized_name_meta_id_unique logicalFilePath:crs-1.0-localized_name_meta_id_unique endDelimiter:/
drop index crs_sys_column_meta_i02
/
create unique index crs_sys_column_meta_uk02 on crs_sys_column_meta (localized_name_meta_id) tablespace spoindx
/
alter index crs_sys_column_meta_i03 rename to crs_sys_column_meta_i02
/
alter index crs_sys_column_meta_i04 rename to crs_sys_column_meta_i03
/

drop index crs_sys_column_type_meta_i01
/
create unique index crs_sys_column_type_meta_uk02 on crs_sys_column_type_meta (localized_name_meta_id) tablespace spoindx
/

drop index crs_sys_table_meta_i01
/
create unique index crs_sys_table_meta_uk02 on crs_sys_table_meta (localized_name_meta_id) tablespace spoindx
/

--changeset svaliev:crs-1.0-VTBCRS-17 logicalFilePath:crs-1.0-VTBCRS-17 endDelimiter:/
alter table crs_sys_column_meta
 drop constraint crs_sys_column_meta_ck02
/
alter table crs_sys_column_meta
  add constraint crs_sys_column_meta_ck02
  check ((ref_column_meta_id is not null and ref_table is null and ref_column is null)
         or (ref_column_meta_id is null and ref_table is not null and ref_column is not null)
         or (ref_column_meta_id is null and ref_table is null and ref_column is null))
/
alter table crs_sys_table_meta modify type not null
/

--changeset svaliev:crs-1.0-VTBCRS-26 logicalFilePath:crs-1.0-VTBCRS-26 endDelimiter:/
alter table crs_sys_dictionary_nav modify table_meta_id null
/
alter table crs_sys_dictionary_nav add localized_name_meta_id number
/
comment on column crs_sys_dictionary_nav.localized_name_meta_id is 'Localized name reference'
/
drop index crs_sys_dictionary_nav_uk01
/
create index crs_sys_dictionary_nav_i02 on crs_sys_dictionary_nav (localized_name_meta_id) tablespace spoindx
/
alter table crs_sys_dictionary_nav
  add constraint crs_sys_dictionary_nav_fk02 foreign key (localized_name_meta_id)
  references crs_sys_localized_name_meta (localized_name_meta_id)
/
alter table crs_sys_dictionary_nav
  add constraint crs_sys_dictionary_nav_ck01
check ((table_meta_id is not null and localized_name_meta_id is null)
       or (table_meta_id is null and localized_name_meta_id is not null))
/

--changeset svaliev:crs-1.0-clean-db logicalFilePath:crs-1.0-clean-db endDelimiter:/
drop table crs_sys_column_meta
/
drop table crs_sys_column_type_meta
/
drop table crs_sys_dictionary_nav
/
drop table crs_sys_file_storage
/
drop table crs_sys_table_meta
/
drop table crs_sys_localized_name_meta
/
drop table crs_sys_localized_value
/
drop sequence crs_sys_column_meta_seq
/
drop sequence crs_sys_column_type_meta_seq
/
drop sequence crs_sys_dictionary_nav_seq
/
drop sequence crs_sys_file_storage_seq
/
drop sequence crs_sys_localized_nm_meta_seq
/
drop sequence crs_sys_localized_value_seq
/
drop sequence crs_sys_table_meta_seq
/
