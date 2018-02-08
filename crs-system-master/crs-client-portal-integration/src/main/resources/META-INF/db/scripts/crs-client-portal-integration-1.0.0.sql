--liquibase formatted sql

--changeset achalov:crs-1.0-VTBCRS-316 logicalFilePath:crs-1.0-VTBCRS-316 endDelimiter:/
create table crs_cpi_setting(
    mnemo         varchar2(32)  not null,
    setting_value varchar2(188),
    primary key(mnemo)
)
/
insert into crs_cpi_setting(mnemo, setting_value) values ('CRON', '0+0+0+*+*+?')
/
insert into crs_cpi_setting(mnemo, setting_value) values ('PREVIOUS_RUN_TIMESTAMP', null)
/
insert into crs_cpi_setting(mnemo, setting_value) values ('TIMESTAMP_FORMAT', 'dd.MM.yyyy HH24:MI:SSxFF6')
/

--changeset achalov:crs-1.0-VTBCRS-316-syn logicalFilePath:crs-1.0-VTBCRS-316-syn endDelimiter:/
create or replace synonym crs_cpi_client for v_crs_client@crs_client_portal_link
/
create or replace synonym crs_cpi_client_deleted for v_crs_vle_deleted@crs_client_portal_link
/
create or replace synonym crs_cpi_client_inn for v_crs_client_inn@crs_client_portal_link
/

--changeset achalov:crs-1.0-VTBCRS-398 logicalFilePath:crs-1.0-VTBCRS-398 endDelimiter:/
create or replace synonym crs_cpi_client_category for v_crs_category@crs_client_portal_link
/
create or replace synonym crs_cpi_client_currency for v_crs_currency@crs_client_portal_link
/
create or replace synonym crs_cpi_client_country for v_crs_country@crs_client_portal_link
/
create or replace synonym crs_cpi_client_indstry for v_crs_industry@crs_client_portal_link
/
create or replace synonym crs_cpi_client_opf for v_crs_opf@crs_client_portal_link
/
create or replace synonym crs_cpi_client_ogrn for v_crs_client_ogrn@crs_client_portal_link
/
create or replace synonym crs_cpi_client_segm for v_crs_segment@crs_client_portal_link
/
create or replace synonym crs_cpi_client_type_dict for v_crs_client_type_dict@crs_client_portal_link
/
update crs_cpi_setting
   set setting_value = 'dd.MM.yyyy HH24:MI:SS.FF6'
 where mnemo = 'TIMESTAMP_FORMAT'
/
update crs_cpi_setting
   set setting_value = null
 where mnemo = 'PREVIOUS_RUN_TIMESTAMP'
/

--changeset akirilchev:crs-1.0-VTBCRS-399-synonym logicalFilePath:crs-1.0-VTBCRS-399-synonym endDelimiter:/
create or replace synonym crs_cpi_client_group for v_crs_group@crs_client_portal_link
/
rename crs_cpi_client_indstry to crs_cpi_client_industry
/
rename crs_cpi_client_segm to crs_cpi_client_segment
/

--changeset akirilchev:crs-1.0-VTBCRS-400-synonym logicalFilePath:crs-1.0-VTBCRS-400-synonym endDelimiter:/
create or replace synonym crs_cpi_client_type for v_crs_client_type@crs_client_portal_link
/
create or replace synonym crs_cpi_group_participant for v_crs_group_participant@crs_client_portal_link
/

--changeset akirilchev:crs-1.0-VTBCRS-400-log-table logicalFilePath:crs-1.0-VTBCRS-400-log-table endDelimiter:/
create table crs_cpi_log
(
    id_log           number not null,
    last_sync_date   timestamp,
    portal_view_name varchar2(30),
    error_text       varchar2(4000),
    constraint crs_cpi_log_pk primary key (id_log)
)
/
comment on table crs_cpi_log is 'CRS client portal integration log'
/
comment on column crs_cpi_log.id_log is 'Primary key'
/
comment on column crs_cpi_log.last_sync_date is 'Last sync date'
/
comment on column crs_cpi_log.portal_view_name is 'Client portal view name'
/
comment on column crs_cpi_log.error_text is 'Error text'
/
create sequence crs_cpi_log_seq
/
create table crs_cpi_last_sync (
    id             varchar2(30),
    last_sync_date timestamp,
    constraint crs_cpi_last_sync_pk primary key (id)
)
/
comment on table crs_cpi_last_sync is 'Client integration last sync table'
/
comment on column crs_cpi_last_sync.id is 'Primary key. Client portal view name'
/
comment on column crs_cpi_last_sync.last_sync_date is 'Last sync date'
/

--changeset akirilchev:crs-1.0-VTBCRS-400-remove-setting logicalFilePath:crs-1.0-VTBCRS-400-remove-setting endDelimiter:/
delete crs_cpi_setting
 where mnemo in ('PREVIOUS_RUN_TIMESTAMP', 'TIMESTAMP_FORMAT')
/

--changeset akirilchev:crs-1.0-VTBCRS-462-cpi-reset-data logicalFilePath:crs-1.0-VTBCRS-462-cpi-reset-data endDelimiter:/
begin
    for rec in (select * from user_tables where table_name like 'CRS\_L%\_CLIENT%' escape '\') loop
        execute immediate 'delete '|| rec.table_name;
    end loop;
    for rec in (select * from user_tables where table_name like 'CRS\_S%\_CLIENT%' escape '\') loop
        execute immediate 'delete '|| rec.table_name;
    end loop;
    for rec in (select * from user_tables where table_name like 'CRS\_H%\_CLIENT%' escape '\') loop
        execute immediate 'delete '|| rec.table_name;
    end loop;
    delete crs_cpi_last_sync;
    delete crs_cpi_log;
end;
/

--changeset akirilchev:crs-1.0-VTBCRS-462-group-temp logicalFilePath:crs-1.0-VTBCRS-462-group-temp endDelimiter:/
create global temporary table crs_cpi_client_group_t
(
    group_id        varchar2(12),
    name            varchar2(1024),
    name_en         varchar2(1024),
    full_name       varchar2(1024),
    full_name_en    varchar2(1024),
    description     clob,
    description_en  clob,
    is_vtb_daughter varchar2(512),
    segmentid       number,
    industryid      number,
    countryid       number,
    last_update     timestamp
) on commit delete rows
/

--changeset akirilchev:crs-1.0-VTBCRS-427-synonym logicalFilePath:crs-1.0-VTBCRS-427-synonym endDelimiter:/
create or replace synonym crs_cpi_client_department for v_crs_lock@crs_client_portal_link
/
create or replace synonym crs_cpi_department for v_crs_department@crs_client_portal_link
/

--changeset akirilchev:crs-1.0-VTBCRS-427-dep-temp logicalFilePath:crs-1.0-VTBCRS-427-dep-temp endDelimiter:/
create global temporary table crs_cpi_department_t
(
    departmentid        number not null,
    dep_name            varchar2(255 char) null,
    dep_name_en         varchar2(255 char) null,
    dep_full_name       varchar2(255 char) null,
    dep_full_name_en    varchar2(255 char) null,
    parent_departmentid number null,
    last_update         timestamp(6) null
) on commit delete rows
/

--changeset akirilchev:crs-1.0-VTBCRS-427-client-dep-temp-2 logicalFilePath:crs-1.0-VTBCRS-427-client-dep-temp-2 endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tables where table_name = 'CRS_CPI_CLIENT_DEPARTMENT_T'
create global temporary table crs_cpi_client_department_t
(
    vtb_legalentityid   varchar2(12) null,
    departmentid        number not null,
    lock_typeid         number not null,
    last_update         timestamp(6) not null
) on commit delete rows
/

--changeset akirilchev:crs-1.0-VTBCRS-427-client-dep-temp-2-index logicalFilePath:crs-1.0-VTBCRS-427-client-dep-temp-2-index endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select case when exists(select 1 from user_ind_columns where table_name = 'CRS_CPI_CLIENT_DEPARTMENT_T' and column_name in ('DEPARTMENTID', 'VTB_LEGALENTITYID') group by index_name having count(*) = 2) then 1 else 0 end from dual
create index crs_cpi_client_dep_t_i01 on crs_cpi_client_department_t(departmentid, vtb_legalentityid)
/

--changeset akirilchev:crs-1.0-VTBCRS-462-group-temp-index logicalFilePath:crs-1.0-VTBCRS-462-group-temp-index endDelimiter:/
create index crs_cpi_client_group_t_i01 on crs_cpi_client_group_t(last_update)
/

--changeset akirilchev:crs-1.0-VTBCRS-462-dep-name-table logicalFilePath:crs-1.0-VTBCRS-462-dep-name-table endDelimiter:/
create global temporary table crs_cpi_department_name_t
(
    departmentid        number not null,
    crs_departmentid     number not null
) on commit delete rows
/
create index crs_cpi_department_name_t_i01 on crs_cpi_department_name_t(departmentid, crs_departmentid)
/

--changeset akirilchev:crs-1.0-VTBCRS-480-client-temp logicalFilePath:crs-1.0-VTBCRS-480-client-temp endDelimiter:/
create global temporary table crs_cpi_client_t (
    vtb_legalentityid   varchar2(12) not null,
    full_name           varchar2(512) null,
    full_name_en        varchar2(512) null,
    name                varchar2(255) null,
    name_en             varchar2(255) null,
    opfid               number null,
    countryid           number null,
    categoryid          number null,
    segmentid           number null,
    industryid          number null,
    last_update         timestamp(6) null
) on commit delete rows
/
create index crs_cpi_client_t_i01 on crs_cpi_client_t(last_update)
/

--changeset akirilchev:crs-1.0-VTBCRS-480-client-inn-temp logicalFilePath:crs-1.0-VTBCRS-480-client-inn-temp endDelimiter:/
create global temporary table crs_cpi_client_inn_t (
    cl_global_prid      integer not null,
    vtb_legalentityid   varchar2(12) null,
    taxid               varchar2(64) null,
    countryid           number null,
    last_update         timestamp(6) null
) on commit delete rows
/
create index crs_cpi_client_inn_t_i01 on crs_cpi_client_inn_t(last_update)
/

--changeset akirilchev:crs-1.0-VTBCRS-480-client-ogrn-temp logicalFilePath:crs-1.0-VTBCRS-480-client-ogrn-temp endDelimiter:/
create global temporary table crs_cpi_client_ogrn_t (
    cl_global_prid      integer not null,
    vtb_legalentityid   varchar2(12) null,
    reg_num             varchar2(64) null,
    countryid           number null,
    last_update         timestamp(6) null
) on commit delete rows
/
create index crs_cpi_client_ogrn_t_i01 on crs_cpi_client_ogrn_t(last_update)
/

--changeset akirilchev:crs-1.0-VTBCRS-480-client-segment-temp logicalFilePath:crs-1.0-VTBCRS-480-client-segment-temp endDelimiter:/
create global temporary table crs_cpi_client_segment_t (
    segmentid   number not null,
    last_update timestamp(6) not null,
    name        varchar2(300) null,
    name_en     varchar2(300) null,
    diap_min    number null,
    diap_max    number null,
    currencyid  number not null
) on commit delete rows
/
create index crs_cpi_client_segment_t_i01 on crs_cpi_client_segment_t(last_update)
/

--changeset akirilchev:crs-1.0-VTBCRS-480-client-opf-temp logicalFilePath:crs-1.0-VTBCRS-480-client-opf-temp endDelimiter:/
create global temporary table crs_cpi_client_opf_t (
    opfid       number not null,
    countryid   number null,
    name        varchar2(300) null,
    name_en     varchar2(300) null,
    last_update timestamp(6) not null
) on commit delete rows
/
create index crs_cpi_client_opf_t_i01 on crs_cpi_client_opf_t(last_update)
/

--changeset pmasalov:crs-1.0-VTBCRS-509-temp-l-client-department logicalFilePath:crs-1.0-VTBCRS-509-temp-l-client-department endDelimiter:/
create global temporary table crs_cpi_l_client_department_t
(
    id            number not null,
    ldts          timestamp(6) not null,
    removed       number(1) default 0 not null,
    client_id     number not null,
    department_id number not null
) on commit delete rows
/

--changeset akamordin:crs-1.0.0-VTBCRS-620-Mapping logicalFilePath:crs-1.0.0-VTBCRS-620-Mapping endDelimiter:/ runOnChange:true
create table crs_cpi_dep_mapping
(
    crs_department_id number not null,
    cp_department_id  number not null,
    constraint crs_cpi_dep_mapping_pk primary key (crs_department_id, cp_department_id),
    constraint crs_cpi_dep_mapping_fk01 foreign key (crs_department_id) references crs_h_department(id)
)
organization index
compress 1
tablespace spoindx
/

comment on table crs_cpi_dep_mapping is 'Mapping for departments'
/
comment on column crs_cpi_dep_mapping.crs_department_id is 'Reference to departments (crs_h_department.id)'
/
comment on column crs_cpi_dep_mapping.cp_department_id is 'Client portal departmenid (crs_cpi_department.departmentid)'
/
create index crs_cpi_dep_mapping_i01 on crs_cpi_dep_mapping(cp_department_id) tablespace spoindx
/
