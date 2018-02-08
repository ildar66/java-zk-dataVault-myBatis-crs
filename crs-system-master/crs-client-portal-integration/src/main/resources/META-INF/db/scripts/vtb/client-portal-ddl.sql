create table v_crs_category(
    categoryid  number not null,
    last_update timestamp(6) not null,
    name        varchar2(128 char) null,
    name_en     varchar2(128 char) null
)
/
create table v_crs_client(
    vtb_legalentityid   varchar2(12 char) not null,
    full_name           varchar2(512 char) null,
    full_name_en        varchar2(512 char) null,
    name                varchar2(255 char) null,
    name_en             varchar2(255 char) null,
    opfid               number null,
    countryid           number null,
    categoryid          number null,
    segmentid           number null,
    industryid          number null,
    last_update         timestamp(6) null
)
/
create table v_crs_client_inn(
    cl_global_prid      integer not null,
    vtb_legalentityid   varchar2(12 char) null,
    taxid               varchar2(64 char) null,
    countryid           number null,
    last_update         timestamp(6) null
)
/
create table v_crs_client_ogrn(
    cl_global_prid      integer not null,
    vtb_legalentityid   varchar2(12 char) null,
    reg_num             varchar2(64 char) null,
    countryid           number null,
    last_update         timestamp(6) null
)
/
create table v_crs_client_type(
    vtb_legalentityid   varchar2(12 char) null,
    client_typeid       number not null,
    orgid               number not null,
    last_update         timestamp(6) null
)
/
create table v_crs_client_type_dict(
    client_typeid   number not null,
    last_update     timestamp(6) not null,
    name            varchar2(128 char) null,
    name_en         varchar2(128 char) null,
    priority        number null
)
/
create table v_crs_country(
    countryid   number not null,
    last_update timestamp(6) not null,
    code_a2     varchar2(10 char) null,
    code_a3     varchar2(10 char) null,
    num_code    number null,
    name        varchar2(128 char) null,
    name_en     varchar2(128 char) null
)
/
create table v_crs_currency(
    currencyid  number not null,
    last_update timestamp(6) not null,
    code_num    number null,
    code        varchar2(6 char) null,
    name        varchar2(128 char) null,
    name_en     varchar2(128 char) null
)
/
create table v_crs_dep_cl_hierarchy(
    departmentid    number null,
    parentid        number null,
    last_update     timestamp(6) not null
)
/
create table v_crs_dep_reg_hierarchy(
    departmentid    number null,
    parentid        number null,
    last_update     timestamp(6) not null
)
/
create table v_crs_department(
    departmentid        number not null,
    dep_name            varchar2(255 char) null,
    dep_name_en         varchar2(255 char) null,
    dep_full_name       varchar2(255 char) null,
    dep_full_name_en    varchar2(255 char) null,
    parent_departmentid number null,
    last_update         timestamp(6) null
)
/
create table v_crs_group(
    group_id        varchar2(12 char) null,
    name            varchar2(255 char) null,
    name_en         varchar2(255 char) null,
    full_name       varchar2(512 char) null,
    full_name_en    varchar2(512 char) null,
    description     clob null,
    description_en  clob null,
    is_vtb_daughter varchar2(512 char) null,
    segmentid       number null,
    industryid      number null,
    countryid       number null,
    last_update     timestamp(6) null
)
/
create table v_crs_group_participant(
    group_id            varchar2(12 char) null,
    vtb_legalentityid   varchar2(12 char) null,
    last_update         timestamp(6) null
)
/
create table v_crs_industry(
    industryid  number not null,
    last_update timestamp(6) not null,
    name        varchar2(512 char) null,
    name_en     varchar2(512 char) null
)
/
create table v_crs_lock(
    vtb_legalentityid   varchar2(12 char) null,
    departmentid        number not null,
    lock_typeid         number not null,
    last_update         timestamp(6) not null
)
/
create table v_crs_opf(
    opfid       number not null,
    countryid   number null,
    name        varchar2(128 char) null,
    name_en     varchar2(128 char) null,
    last_update timestamp(6) not null
)
/
create table v_crs_segment(
    segmentid   number not null,
    last_update timestamp(6) not null,
    name        varchar2(128 char) null,
    name_en     varchar2(128 char) null,
    diap_min    number null,
    diap_max    number null,
    currencyid  number not null
)
/
create table v_crs_vle_deleted(
    vtb_legalentityid   varchar2(12 char) null,
    del_date            timestamp(6) not null
)
/
