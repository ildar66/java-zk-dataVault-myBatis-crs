--liquibase formatted sql

--changeset akirilchev:crs-1.1.0-VTBCRS-540 logicalFilePath:crs-1.1.0-VTBCRS-540 endDelimiter:/
alter table crs_sys_s_entity add view_order number default 0 not null
/
comment on column crs_sys_s_entity.view_order is 'view order'
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
    v_entity_key := 'ENTITY';
    select id into v_entity_hub_id
    from crs_sys_h_entity
    where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'VIEW_ORDER', 'Порядок отображения', 'View order', 0, null, 'NUMBER', 'VIEW_ORDER', null, null, v_ldts);
end;
/

--changeset akirilchev:crs-1.1.0-VTBCRS-540-update logicalFilePath:crs-1.1.0-VTBCRS-540-update endDelimiter:/
merge into crs_sys_s_entity t using (
                                        select s.id,
                                            s.view_order,
                                            s.name_ru,
                                            row_number() over(order by s.name_ru, s.ldts) new_view_order
                                        from crs_sys_s_entity s
                                    ) z on (t.id = z.id)
when matched then update set t.view_order = z.new_view_order
/

--changeset akirilchev:crs-1.1.0-VTBCRS-540-remove-odd-group logicalFilePath:crs-1.1.0-VTBCRS-540-remove-odd-group endDelimiter:/
insert into crs_sys_l_entity_group(id, ldts, removed, entity_group_id, entity_id)
    select crs_sys_l_entity_group_seq.nextval, sysdate, 1, z.min_entity_group_id, z.entity_id
    from (select leg.entity_id, min(entity_group_id) min_entity_group_id
          from (select leg.*,
                    row_number() over(partition by leg.entity_id, leg.entity_group_id order by leg.ldts desc) rn
                from crs_sys_l_entity_group leg) leg join (select g.*,
                                                               row_number() over(partition by g.h_id order by g.ldts) rn
                                                           from crs_sys_s_entity_group g) g on leg.entity_group_id = g.h_id
          where g.rn = 1
                and g.removed = 0
                and leg.rn = 1
                and leg.removed = 0
          group by leg.entity_id
          having count(*) > 1) z
    where z.min_entity_group_id in (select eg.id
                                    from crs_sys_h_entity_group eg
                                    where eg.key in ('DEFAULT_DICTIONARY_GROUP', 'DEFAULT_INPUT_FORM_GROUP', 'DEFAULT_CLASSIFIER_GROUP'))
/


--changeset pmasalov:crs-1.1.0-VTBCRS-508-rename-action logicalFilePath:crs-1.1.0-VTBCRS-508-rename-action endDelimiter:/
update crs_sys_h_business_action set key = 'VIEW' where key = 'LOOK_UP'
/
update crs_sys_h_business_action set key = 'VIEW_LIST' where key = 'SEARCH'
/

--changeset akirilchev:crs-1.1.0-VTBCRS-583 logicalfilepath:crs-1.1.0-VTBCRS-583 enddelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tables where table_name = 'CRS_L_S_CALC_MODEL_FORM'
create table crs_l_s_calc_model_form
(
    id number not null,
    link_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null,
    digest varchar2(100) not null,
    input_form_date_attr_key varchar2(100),
    period_count number
)
/
comment on table crs_l_s_calc_model_form is 'Model input form satellite for link table'
/
comment on column crs_l_s_calc_model_form.id is 'Identifier'
/
comment on column crs_l_s_calc_model_form.ldts is 'Load date'
/
comment on column crs_l_s_calc_model_form.input_form_date_attr_key is 'Date field of model input form'
/
comment on column crs_l_s_calc_model_form.period_count is 'Period count of model input form'
/
comment on column crs_l_s_calc_model_form.removed is 'Removed flag'
/
comment on column crs_l_s_calc_model_form.link_id is 'Link table identifier'
/
comment on column crs_l_s_calc_model_form.digest is 'Row digest'
/
alter table crs_l_s_calc_model_form
    add constraint crs_l_s_calc_model_form_pk primary key (id)
    using index
    tablespace spoindx
/
alter table crs_l_s_calc_model_form
    add constraint crs_l_s_calc_model_form_uk01 unique (link_id, ldts)
    using index
    tablespace spoindx
/
alter table crs_l_s_calc_model_form
    add constraint crs_l_s_calc_model_form_ck01
check (removed in (0, 1))
/
alter table crs_l_s_calc_model_form
    add constraint crs_l_s_calc_model_form_fk01 foreign key (link_id)
references crs_l_calc_model_form (id)
/
create sequence crs_l_s_calc_model_form_seq
/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'CALC_MODEL#INPUT_FORMS';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#INPUT_FORMS#PERIOD_COUNT',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
        v_attr_attr_id,
        v_ldts,
        0,
        0,
        'Количество периодов',
        'Number of periods',
        1,
        0,
        'NUMBER',
        'PERIOD_COUNT');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);
end;
/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'CALC_MODEL#INPUT_FORMS';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC_MODEL#INPUT_FORMS#INPUT_FORM_DATE_ATTR_KEY',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
        v_attr_attr_id,
        v_ldts,
        0,
        0,
        'Поле с датой',
        'Date field',
        1,
        0,
        'STRING',
        'INPUT_FORM_DATE_ATTR_KEY');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.1.0-VTBCRS-564 logicalfilepath:crs-1.1.0-VTBCRS-564 enddelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tables where table_name = 'CRS_L_S_CALC_CLIENT'
create table crs_l_s_calc_client
(
    id number not null,
    link_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null,
    digest varchar2(100) not null,
    participant number(1) default 0 not null,
    status varchar2(100) not null,
    text clob
)
/
comment on table crs_l_s_calc_client is 'Model input form satellite for link table'
/
comment on column crs_l_s_calc_client.id is 'Identifier'
/
comment on column crs_l_s_calc_client.ldts is 'Load date'
/
comment on column crs_l_s_calc_client.removed is 'Removed flag'
/
comment on column crs_l_s_calc_client.link_id is 'Link table identifier'
/
comment on column crs_l_s_calc_client.digest is 'Row digest'
/
comment on column crs_l_s_calc_client.participant is 'Participant of calculation client'
/
comment on column crs_l_s_calc_client.status is 'Status of calculation client'
/
comment on column crs_l_s_calc_client.text is 'Comment of calculation client'
/
alter table crs_l_s_calc_client
    add constraint crs_l_s_calc_client_pk primary key (id)
    using index
    tablespace spoindx
/
alter table crs_l_s_calc_client
    add constraint crs_l_s_calc_client_uk01 unique (link_id, ldts)
    using index
    tablespace spoindx
/
alter table crs_l_s_calc_client
    add constraint crs_l_s_calc_client_ck01
check (removed in (0, 1))
/
alter table crs_l_s_calc_client
    add constraint crs_l_s_calc_client_fk01 foreign key (link_id)
references crs_l_calc_client (id)
/
create sequence crs_l_s_calc_client_seq
/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'CALC#CLIENT';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#CLIENT#PARTICIPANT',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
        v_attr_attr_id,
        v_ldts,
        0,
        0,
        'Участие контрагента в расчете лимита',
        'Client limit participation',
        1,
        0,
        'BOOLEAN',
        'PARTICIPANT');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);
end;
/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'CALC#CLIENT';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#CLIENT#STATUS',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
        v_attr_attr_id,
        v_ldts,
        0,
        0,
        'Статус контрагента в группе',
        'Client status in group',
        1,
        0,
        'STRING',
        'STATUS');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);
end;
/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'CALC#CLIENT';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#CLIENT#COMMENT',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
        v_attr_attr_id,
        v_ldts,
        0,
        0,
        'Комментарий',
        'Comment',
        1,
        0,
        'TEXT',
        'TEXT');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);
end;
/

--changeset akirilchev:crs-1.1.0-VTBCRS-566 logicalfilepath:crs-1.1.0-VTBCRS-566 enddelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tab_cols where table_name = 'CRS_L_S_CALC_CLIENT' and column_name = 'EXCLUDED'
alter table crs_l_s_calc_client add excluded number(1) default 0 not null
/
comment on column crs_l_s_calc_client.excluded is 'Is client excluded from calculation client group'
/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'CALC#CLIENT';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'CALC#CLIENT#EXCLUDED',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
        v_attr_attr_id,
        v_ldts,
        0,
        0,
        'Исключен из состава группы',
        'Ecluded from client group',
        1,
        0,
        'BOOLEAN',
        'EXCLUDED');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-509-remove-view-list logicalFilePath:crs-1.1.0-VTBCRS-509-remove-view-list endDelimiter:/
delete from crs_l_permission_business_action where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST')
/
delete from crs_sys_l_entity_business_action where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST')
/
delete from crs_sys_l_entity_type_business_action where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST')
/
delete from crs_sys_s_business_action where h_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST')
/
declare
    v_localization_id number;
begin
    select localization_id into v_localization_id from crs_sys_l_business_action_name
    where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST');

    delete from crs_sys_s_localization where h_id = v_localization_id;

    delete from crs_sys_l_business_action_name
    where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST');

    delete from crs_sys_h_localization where id = v_localization_id;

    exception when no_data_found then
    return;
end;
/
delete from crs_sys_h_business_action where key = 'VIEW_LIST'
/

--changeset pmasalov:crs-1.1.0-VTBCRS-586-permission-add-link logicalFilePath:crs-1.1.0-VTBCRS-586-permission-add-link endDelimiter:/
create table crs_l_permission_entity_type (
    id      number       not null,
    permission_id number       not null,
    entity_type_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_l_permission_entity_type_pk   primary key (id) using index tablespace spoindx,
    constraint crs_l_permission_entity_type_fk01 foreign key (permission_id) references crs_h_permission(id),
    constraint crs_l_permission_entity_type_fk02 foreign key (entity_type_id) references crs_sys_h_entity_type(id),
    constraint crs_l_permission_entity_type_ck01 check (removed in (0, 1))
)
/
create sequence crs_l_permission_entity_type_seq
/
comment on table crs_l_permission_entity_type is 'Role permission to entity type link'
/
comment on column crs_l_permission_entity_type.id is 'Identifier'
/
comment on column crs_l_permission_entity_type.ldts is 'Load date'
/
comment on column crs_l_permission_entity_type.removed is 'Removed flag'
/
comment on column crs_l_permission_entity_type.permission_id is 'Reference to role permission hub'
/
comment on column crs_l_permission_entity_type.entity_type_id is 'Reference to entity type hub'
/
create index crs_l_permission_entity_type_i01 on crs_l_permission_entity_type(permission_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_permission_entity_type_i02 on crs_l_permission_entity_type(entity_type_id, ldts) compress 1 tablespace spoindx
/

create table crs_l_permission_calc_model (
    id      number       not null,
    permission_id number       not null,
    calc_model_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_l_permission_calc_model_pk   primary key (id) using index tablespace spoindx,
    constraint crs_l_permission_calc_model_fk01 foreign key (permission_id) references crs_h_permission(id),
    constraint crs_l_permission_calc_model_fk02 foreign key (calc_model_id) references crs_h_calc_model(id),
    constraint crs_l_permission_calc_model_ck01 check (removed in (0, 1))
)
/
create sequence crs_l_permission_calc_model_seq
/
comment on table crs_l_permission_calc_model is 'Role permission to model link'
/
comment on column crs_l_permission_calc_model.id is 'Identifier'
/
comment on column crs_l_permission_calc_model.ldts is 'Load date'
/
comment on column crs_l_permission_calc_model.removed is 'Removed flag'
/
comment on column crs_l_permission_calc_model.permission_id is 'Reference to role permission hub'
/
comment on column crs_l_permission_calc_model.calc_model_id is 'Reference to model hub'
/
create index crs_l_permission_calc_model_i01 on crs_l_permission_calc_model(permission_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_permission_calc_model_i02 on crs_l_permission_calc_model(calc_model_id, ldts) compress 1 tablespace spoindx
/

--changeset pmasalov:crs-1.1.0-VTBCRS-586-permission-add-link-meta logicalFilePath:crs-1.1.0-VTBCRS-586-permission-add-link-meta endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'PERMISSION';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'PERMISSION#ENTITY_TYPE',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
        v_h_attribute_id,
        v_ldts,
        0,
        0,
        0,
        'crs_l_permission_entity_type',
        'ENTITY_TYPE#NAME_EN',
        'Тип сущьности',
        'Entity type',
        0,
            'ENTITY_TYPE',
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
    values (crs_sys_h_attribute_seq.nextval,'PERMISSION#CALC_MODEL',v_ldts)
    returning id into v_h_attribute_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, multilang, link_table, attribute_key, name_ru, name_en, filter_available,
                                     entity_key, removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
        v_h_attribute_id,
        v_ldts,
        0,
        0,
        0,
        'crs_l_permission_calc_model',
        'CALC_MODEL#NAME_EN',
        'Модель',
        'Model',
        0,
            'CALC_MODEL',
            0,
            'REFERENCE',
            null);
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-509-alter-calc logicalFilePath:crs-1.1.0-VTBCRS-509-alter-calc endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from user_tab_cols where table_name = 'CRS_H_CALC' and column_name = 'VIRTUAL_SECURE_TAG'
alter table crs_h_calc add virtual_secure_tag varchar2(1)
/
comment on column crs_h_calc.virtual_secure_tag is 'Secure tag index column'
/

--changeset pmasalov:crs-1.1.0-VTBCRS-509-dummy-index-proc logicalFilePath:crs-1.1.0-VTBCRS-509-dummy-index-proc endDelimiter:/
-- temp version of procedure. full version builds in separate procedure script
create or replace procedure crs_calc_secure_target_src( p_rowid in rowid, p_idx_data in out clob)
is
    begin
        null;
    end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-509-create-index logicalFilePath:crs-1.1.0-VTBCRS-509-create-index endDelimiter:/
begin
    for r in (select index_name from user_indexes where index_name = 'CRS_H_CALC_I01') loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/
begin
    for r in (select pre_name from ctx_preferences where pre_owner = user and pre_name in ('CALC_SECURE_DATASTORE')) loop
        ctx_ddl.drop_preference(r.pre_name);
    end loop;
end;
/
begin
    ctx_ddl.create_preference('CALC_SECURE_DATASTORE', 'user_datastore');
    ctx_ddl.set_attribute('CALC_SECURE_DATASTORE', 'procedure', 'crs_calc_secure_target_src');
    ctx_ddl.set_attribute('CALC_SECURE_DATASTORE', 'output_type', 'clob');
end;
/
begin
    for r in (select sgp_name from ctx_section_groups where sgp_owner = user and sgp_name = 'SECURE_SECTION_GROUP') loop
        ctx_ddl.drop_section_group(r.sgp_name);
    end loop;
end;
/
begin
    ctx_ddl.create_section_group('SECURE_SECTION_GROUP', 'PATH_SECTION_GROUP');
end;
/
create index crs_h_calc_i01 on crs_h_calc (virtual_secure_tag)
indextype is ctxsys.context
parameters ('DATASTORE CALC_SECURE_DATASTORE FILTER CTXSYS.NULL_FILTER section group SECURE_SECTION_GROUP')
/

--changeset pmasalov:crs-1.1.0-VTBCRS-509-remove-view-list logicalFilePath:crs-1.1.0-VTBCRS-509-remove-view-list endDelimiter:/
delete from crs_l_permission_business_action where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST')
/
delete from crs_sys_l_entity_business_action where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST')
/
delete from crs_sys_l_entity_type_business_action where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST')
/
delete from crs_sys_s_business_action where h_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST')
/
declare
    v_localization_id number;
begin
    select localization_id into v_localization_id from crs_sys_l_business_action_name
    where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST');

    delete from crs_sys_s_localization where h_id = v_localization_id;

    delete from crs_sys_l_business_action_name
    where business_action_id = (select id from crs_sys_h_business_action where key = 'VIEW_LIST');

    delete from crs_sys_h_localization where id = v_localization_id;
    exception when no_data_found then
    return;
end;
/
delete from crs_sys_h_business_action where key = 'VIEW_LIST'
/

--changeset achalov:crs-1.1.0-VTBCRS-500 logicalFilePath:crs-1.1.0-VTBCRS-500 endDelimiter:/
create table crs_setting(
    mnemo         varchar2(64)  not null,
    setting_value varchar2(256),
    primary key(mnemo)
)
/

--changeset achalov:crs-1.1.0-VTBCRS-500-insert-default-data logicalFilePath:crs-1.1.0-VTBCRS-500-insert-default-data endDelimiter:/
drop table crs_setting
/
create table crs_setting(
    mnemo         varchar2(64)  not null,
    setting_value varchar2(256),
    constraint crs_setting_pk primary key(mnemo) using index tablespace spoindx
)
/

insert into crs_setting(mnemo, setting_value)
values ('INTEGRATION_LOAD_BALANCER_URL', 'http://localhost:9080/crs-integration-web/')
/

--changeset svaliev:crs-1.1.0-VTBCRS-500-rename-mnemo logicalFilePath:crs-1.1.0-VTBCRS-500-rename-mnemo endDelimiter:/
update crs_setting t
set t.mnemo = 'INTEGRATION_MODULE_URL'
where t.mnemo = 'INTEGRATION_LOAD_BALANCER_URL'
/

--changeset svaliev:crs-1.1.0-permission-entity-type-name logicalFilePath:crs-1.1.0-permission-entity-type-name endDelimiter:/
update crs_sys_s_attribute a
set a.name_ru = 'Тип сущности'
where a.h_id = (select id
                from crs_sys_h_attribute
                where key = 'PERMISSION#ENTITY_TYPE')
/

--changeset akamordin:crs-1.1.0-VTBCRS-599-add-cp_department-infrastructure logicalFilePath:crs-1.1.0-VTBCRS-599-add-cp_department-infrastructure endDelimiter:/
create table crs_h_cp_department(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_cp_department_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_cp_department_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_cp_department_seq
/
comment on table crs_h_cp_department is 'Client portal departments hub'
/
comment on column crs_h_cp_department.id is 'Identifier'
/
comment on column crs_h_cp_department.key is 'Key'
/
comment on column crs_h_cp_department.ldts is 'Load date'
/
create table crs_s_cp_department(
    id          number              not null,
    h_id        number              not null,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    constraint crs_s_cp_department_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_cp_department_ck01 check(removed in (0, 1)),
    constraint crs_s_cp_department_uk01 unique(h_id, ldts) using index compress 1 tablespace spoindx,
    constraint crs_s_cp_department_fk01 foreign key(h_id) references crs_h_cp_department(id)
)
/
create sequence crs_s_cp_department_seq
/
comment on table crs_s_cp_department is 'Client portal departments satellite'
/
comment on column crs_s_cp_department.id is 'Identifier'
/
comment on column crs_s_cp_department.h_id is 'Reference to hub'
/
comment on column crs_s_cp_department.digest is 'Row digest'
/
comment on column crs_s_cp_department.ldts is 'Load date'
/
comment on column crs_s_cp_department.removed is 'Removed flag'
/

create table crs_l_cp_department_name(
    id                 number              not null,
    cp_department_id   number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_cp_department_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_cp_department_name_fk01 foreign key(cp_department_id) references crs_h_cp_department(id),
    constraint crs_l_cp_department_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_cp_department_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_cp_department_name_i01 on crs_l_cp_department_name(cp_department_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_cp_department_name_i02 on crs_l_cp_department_name(localization_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_cp_department_name_i03 on crs_l_cp_department_name(ldts) tablespace spoindx
/
create sequence crs_l_cp_department_name_seq
/
comment on table crs_l_cp_department_name is 'Client portal departments to multilanguage name link'
/
comment on column crs_l_cp_department_name.cp_department_id is 'Reference to client portal departments hub'
/
comment on column crs_l_cp_department_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_cp_department_name.removed is 'Removed flag'
/
comment on column crs_l_cp_department_name.ldts is 'Load date'
/
create table crs_l_cp_department_fullname(
    id                 number              not null,
    cp_department_id   number              not null,
    localization_id    number              not null,
    removed            number(1) default 0 not null,
    ldts               timestamp           not null,
    constraint crs_l_cp_department_fullname_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_cp_department_fullname_fk01 foreign key(cp_department_id) references crs_h_cp_department(id),
    constraint crs_l_cp_department_fullname_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_cp_department_fullname_ck01 check(removed in (0, 1))
)
/
create index crs_l_cp_department_fullname_i01 on crs_l_cp_department_fullname(cp_department_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_cp_department_fullname_i02 on crs_l_cp_department_fullname(localization_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_cp_department_fullname_i03 on crs_l_cp_department_fullname(ldts) tablespace spoindx
/
create sequence crs_l_cp_department_fullname_seq
/
comment on table crs_l_cp_department_fullname is 'Client portal departments to multilanguage full name link'
/
comment on column crs_l_cp_department_fullname.cp_department_id is 'Reference to client portal departments hub'
/
comment on column crs_l_cp_department_fullname.localization_id is 'Reference to localization hub'
/
comment on column crs_l_cp_department_fullname.removed is 'Removed flag'
/
comment on column crs_l_cp_department_fullname.ldts is 'Load date'
/

create table crs_l_client_cp_department
(
    id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) default 0 not null,
    client_id number not null,
    cp_department_id number not null,
    constraint crs_l_client_cp_department_pk primary key (id) using index tablespace spoindx,
    constraint crs_l_client_cp_department_ck01 check (removed in (0, 1)),
    constraint crs_l_client_cp_department_fk01 foreign key (client_id) references crs_h_client(id),
    constraint crs_l_client_cp_department_fk02 foreign key (cp_department_id) references crs_h_cp_department(id)
)
/
comment on table crs_l_client_cp_department is 'Client to client portal departments link'
/
comment on column crs_l_client_cp_department.id is 'Identifier'
/
comment on column crs_l_client_cp_department.ldts is 'Load date'
/
comment on column crs_l_client_cp_department.removed is 'Removed flag'
/
comment on column crs_l_client_cp_department.client_id is 'Reference to client'
/
comment on column crs_l_client_cp_department.cp_department_id is 'Reference to client portal departments'
/
create index crs_l_client_cp_department_i01 on crs_l_client_cp_department (client_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_cp_department_i02 on crs_l_client_cp_department (cp_department_id, ldts) compress 1 tablespace spoindx
/
create sequence crs_l_client_cp_department_seq
/

declare
    v_entity_type_id number;
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    procedure delete_meta_data(p_entity_key in crs_sys_h_entity.key%type) is
        begin
            for r in (select *
                      from crs_sys_h_entity e
                      where e.key = p_entity_key) loop
                for j in (select *
                          from crs_sys_l_entity_attribute lea
                          where lea.entity_id = r.id) loop

                    delete from crs_sys_l_entity_attribute lea
                    where lea.attribute_id = j.attribute_id;
                    delete from crs_sys_s_attribute sa
                    where sa.h_id = j.attribute_id;
                    delete from crs_sys_h_attribute ha
                    where ha.id = j.attribute_id;
                end loop;

                delete from crs_sys_l_entity_type let
                where let.entity_id = r.id;
                delete from crs_sys_s_entity se
                where se.h_id = r.id;
                delete from crs_sys_h_entity he
                where he.id = r.id;
            end loop;
        end delete_meta_data;

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
        p_view_order        crs_sys_s_attribute.view_order%type,
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
                id, h_id, ldts, view_order, multilang, link_table,
                name_ru, name_en, type, native_column, entity_key,
                attribute_key
            )
            values(
                crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_view_order, p_multilang, p_link_table,
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

    function get_client_hub_id_by_key(p_entity_key in crs_sys_h_entity.key%type
    ) return crs_sys_h_entity.id%type is
        v_hub_id number;
        begin
            for r in (select c.id from crs_sys_h_entity c where c.key = p_entity_key) loop
                v_hub_id := r.id;
            end loop;
            return v_hub_id;
        end get_client_hub_id_by_key;

begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    --CP_DEPARTMENT
    v_entity_key := 'CP_DEPARTMENT';
    delete_meta_data(v_entity_key);
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Подразделение из КП', 'Department from CP', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 0, 1, 'CRS_L_CP_DEPARTMENT_NAME', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'FULL_NAME', 'Полное наименование', 'Full Name', 1, 1, 'CRS_L_CP_DEPARTMENT_FULL_NAME', 'STRING', null, null, null, v_ldts);

    --CLIENT
    v_entity_key := 'CLIENT';
    v_entity_hub_id := get_client_hub_id_by_key(v_entity_key);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CP_DEPARTMENT', 'Подразделение из КП', 'Department from CP', 0, 0, 'CRS_L_CLIENT_CP_DEPARTMENT', 'REFERENCE', null, 'CP_DEPARTMENT', 'NAME', v_ldts);
end;
/

--changeset svaliev:crs-1.1.0-VTBCRS-599-remove-cp-department logicalFilePath:crs-1.1.0-VTBCRS-599-remove-cp-department endDelimiter:/
drop table crs_l_cp_department_name
/
drop table crs_l_cp_department_fullname
/
drop table crs_l_client_cp_department
/
drop table crs_s_cp_department
/
drop table crs_h_cp_department
/

drop sequence crs_h_cp_department_seq
/
drop sequence crs_s_cp_department_seq
/
drop sequence crs_l_cp_department_name_seq
/
drop sequence crs_l_cp_department_fullname_seq
/
drop sequence crs_l_client_cp_department_seq
/

--changeset svaliev:crs-1.1.0-VTBCRS-599-recreate logicalFilePath:crs-1.1.0-VTBCRS-599-recreate endDelimiter:/
create table crs_h_client_department(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_client_department_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_client_department_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_client_department_seq
/
comment on table crs_h_client_department is 'Client portal departments hub'
/
comment on column crs_h_client_department.id is 'Identifier'
/
comment on column crs_h_client_department.key is 'Key'
/
comment on column crs_h_client_department.ldts is 'Load date'
/
create table crs_s_client_department(
    id          number              not null,
    h_id        number              not null,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    constraint crs_s_client_department_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_client_department_ck01 check(removed in (0, 1)),
    constraint crs_s_client_department_uk01 unique(h_id, ldts) using index compress 1 tablespace spoindx,
    constraint crs_s_client_department_fk01 foreign key(h_id) references crs_h_client_department(id)
)
/
create sequence crs_s_client_department_seq
/
comment on table crs_s_client_department is 'Client portal departments satellite'
/
comment on column crs_s_client_department.id is 'Identifier'
/
comment on column crs_s_client_department.h_id is 'Reference to hub'
/
comment on column crs_s_client_department.digest is 'Row digest'
/
comment on column crs_s_client_department.ldts is 'Load date'
/
comment on column crs_s_client_department.removed is 'Removed flag'
/

create table crs_l_client_department_name(
    id                     number              not null,
    client_department_id   number              not null,
    localization_id        number              not null,
    removed                number(1) default 0 not null,
    ldts                   timestamp           not null,
    constraint crs_l_client_department_name_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_department_name_fk01 foreign key(client_department_id) references crs_h_client_department(id),
    constraint crs_l_client_department_name_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_client_department_name_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_department_name_i01 on crs_l_client_department_name(client_department_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_department_name_i02 on crs_l_client_department_name(localization_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_department_name_i03 on crs_l_client_department_name(ldts) tablespace spoindx
/
create sequence crs_l_client_department_name_seq
/
comment on table crs_l_client_department_name is 'Client portal departments to multilanguage name link'
/
comment on column crs_l_client_department_name.client_department_id is 'Reference to client portal departments hub'
/
comment on column crs_l_client_department_name.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_department_name.removed is 'Removed flag'
/
comment on column crs_l_client_department_name.ldts is 'Load date'
/
create table crs_l_client_department_fullname(
    id                     number              not null,
    client_department_id   number              not null,
    localization_id        number              not null,
    removed                number(1) default 0 not null,
    ldts                   timestamp           not null,
    constraint crs_l_client_department_fullname_pk   primary key(id) using index tablespace spoindx,
    constraint crs_l_client_department_fullname_fk01 foreign key(client_department_id) references crs_h_client_department(id),
    constraint crs_l_client_department_fullname_fk02 foreign key(localization_id) references crs_sys_h_localization(id),
    constraint crs_l_client_department_fullname_ck01 check(removed in (0, 1))
)
/
create index crs_l_client_department_fullname_i01 on crs_l_client_department_fullname(client_department_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_department_fullname_i02 on crs_l_client_department_fullname(localization_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_department_fullname_i03 on crs_l_client_department_fullname(ldts) tablespace spoindx
/
create sequence crs_l_client_department_fullname_seq
/
comment on table crs_l_client_department_fullname is 'Client portal departments to multilanguage full name link'
/
comment on column crs_l_client_department_fullname.client_department_id is 'Reference to client portal departments hub'
/
comment on column crs_l_client_department_fullname.localization_id is 'Reference to localization hub'
/
comment on column crs_l_client_department_fullname.removed is 'Removed flag'
/
comment on column crs_l_client_department_fullname.ldts is 'Load date'
/

create table crs_l_client_cp_department
(
    id number not null,
    ldts timestamp(6) not null,
    removed number(1, 0) default 0 not null,
    client_id number not null,
    client_department_id number not null,
    constraint crs_l_client_cp_department_pk primary key (id) using index tablespace spoindx,
    constraint crs_l_client_cp_department_ck01 check (removed in (0, 1)),
    constraint crs_l_client_cp_department_fk01 foreign key (client_id) references crs_h_client(id),
    constraint crs_l_client_cp_department_fk02 foreign key (client_department_id) references crs_h_client_department(id)
)
/
comment on table crs_l_client_cp_department is 'Client to client portal departments link'
/
comment on column crs_l_client_cp_department.id is 'Identifier'
/
comment on column crs_l_client_cp_department.ldts is 'Load date'
/
comment on column crs_l_client_cp_department.removed is 'Removed flag'
/
comment on column crs_l_client_cp_department.client_id is 'Reference to client'
/
comment on column crs_l_client_cp_department.client_department_id is 'Reference to client portal departments'
/
create index crs_l_client_cp_department_i01 on crs_l_client_cp_department (client_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_client_cp_department_i02 on crs_l_client_cp_department (client_department_id, ldts) compress 1 tablespace spoindx
/
create sequence crs_l_client_cp_department_seq
/

declare
    v_entity_type_id number;
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    procedure delete_meta_data(p_entity_key in crs_sys_h_entity.key%type) is
        begin
            for r in (select *
                      from crs_sys_h_entity e
                      where e.key = p_entity_key) loop
                for j in (select *
                          from crs_sys_l_entity_attribute lea
                          where lea.entity_id = r.id) loop

                    delete from crs_sys_l_entity_attribute lea
                    where lea.attribute_id = j.attribute_id;
                    delete from crs_sys_s_attribute sa
                    where sa.h_id = j.attribute_id;
                    delete from crs_sys_h_attribute ha
                    where ha.id = j.attribute_id;
                end loop;

                delete from crs_sys_l_entity_type let
                where let.entity_id = r.id;
                delete from crs_sys_s_entity se
                where se.h_id = r.id;
                delete from crs_sys_h_entity he
                where he.id = r.id;
            end loop;
        end delete_meta_data;

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
        p_view_order        crs_sys_s_attribute.view_order%type,
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
                id, h_id, ldts, view_order, multilang, link_table,
                name_ru, name_en, type, native_column, entity_key,
                attribute_key
            )
            values(
                crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_view_order, p_multilang, p_link_table,
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

    function get_client_hub_id_by_key(p_entity_key in crs_sys_h_entity.key%type
    ) return crs_sys_h_entity.id%type is
        v_hub_id number;
        begin
            for r in (select c.id from crs_sys_h_entity c where c.key = p_entity_key) loop
                v_hub_id := r.id;
            end loop;
            return v_hub_id;
        end get_client_hub_id_by_key;

begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'PREDEFINED_DICTIONARY';

    delete from crs_sys_l_entity_attribute eal
    where eal.attribute_id = (select ah.id
                              from crs_sys_h_attribute ah
                              where ah.key = 'CLIENT#CP_DEPARTMENT');

    delete from crs_sys_s_attribute a
    where a.h_id = (select ah.id
                    from crs_sys_h_attribute ah
                    where ah.key = 'CLIENT#CP_DEPARTMENT');

    delete from crs_sys_h_attribute ah
    where ah.key = 'CLIENT#CP_DEPARTMENT';

    delete_meta_data('CP_DEPARTMENT');

    --CLIENT_DEPARTMENT
    v_entity_key := 'CLIENT_DEPARTMENT';
    v_entity_hub_id := insert_entity(v_entity_type_id, v_entity_key, 'Подразделение из КП', 'Department from CP', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'NAME', 'Наименование', 'Name', 0, 1, 'CRS_L_CLIENT_DEPARTMENT_NAME', 'STRING', null, null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'FULLNAME', 'Полное наименование', 'Full Name', 1, 1, 'CRS_L_CLIENT_DEPARTMENT_FULLNAME', 'STRING', null, null, null, v_ldts);

    --CLIENT
    v_entity_key := 'CLIENT';
    v_entity_hub_id := get_client_hub_id_by_key(v_entity_key);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'CLIENT_DEPARTMENT', 'Подразделение из КП', 'Department from CP', 0, 0, 'CRS_L_CLIENT_CP_DEPARTMENT', 'REFERENCE', null, 'CLIENT_DEPARTMENT', 'NAME', v_ldts);
end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-622-secure-index-online logicalFilePath:crs-1.1.0-VTBCRS-622-secure-index-online endDelimiter:/
begin
    for r in (select index_name from user_indexes where index_name = 'CRS_H_CALC_I01') loop
        execute immediate 'drop index ' || r.index_name;
    end loop;
end;
/
begin
    for r in (select pre_name from ctx_preferences where pre_owner = user and pre_name in ('SECURE_STORAGE')) loop
        ctx_ddl.drop_preference(r.pre_name);
    end loop;
end;
/
begin
    ctx_ddl.create_preference('SECURE_STORAGE', 'BASIC_STORAGE');
    ctx_ddl.set_attribute('SECURE_STORAGE', 'save_copy', 'FILTERED');
    ctx_ddl.set_attribute('SECURE_STORAGE', 'single_byte', 'TRUE');
end;
/
create index crs_h_calc_i01 on crs_h_calc (virtual_secure_tag)
indextype is ctxsys.context
parameters ('DATASTORE CALC_SECURE_DATASTORE FILTER CTXSYS.NULL_FILTER section group SECURE_SECTION_GROUP storage SECURE_STORAGE sync (on commit)')
/

--changeset akirilchev:crs-1.1.0-VTBCRS-590-tel logicalFilePath:crs-1.1.0-VTBCRS-590-tel endDelimiter:/
create table crs_h_user_tel(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_user_tel_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_user_tel_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_user_tel_seq
/
comment on table crs_h_user_tel is 'User telephone hub'
/
comment on column crs_h_user_tel.id is 'Identifier'
/
comment on column crs_h_user_tel.key is 'Key'
/
comment on column crs_h_user_tel.ldts is 'Load date'
/
create table crs_s_user_tel(
    id          number              not null,
    h_id        number              not null,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    tel_number  varchar2(100),
    constraint crs_s_user_tel_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_user_tel_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_user_tel_fk01 foreign key(h_id)  references crs_h_user_tel(id),
    constraint crs_s_user_tel_ck01 check(removed in (0, 1))
)
/
create sequence crs_s_user_tel_seq
/
comment on table crs_s_user_tel is 'User telephone satellite'
/
comment on column crs_s_user_tel.id is 'Identifier'
/
comment on column crs_s_user_tel.h_id is 'Reference to hub'
/
comment on column crs_s_user_tel.digest is 'Row digest'
/
comment on column crs_s_user_tel.removed is 'Removed flag'
/
comment on column crs_s_user_tel.ldts is 'Load date'
/
comment on column crs_s_user_tel.tel_number is 'Telephone number'
/
create table crs_l_user_tel(
    id      number       not null,
    user_tel_id number       not null,
    user_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_l_user_tel_pk   primary key (id) using index tablespace spoindx,
    constraint crs_l_user_tel_fk01 foreign key (user_tel_id) references crs_h_user_tel(id),
    constraint crs_l_user_tel_fk02 foreign key (user_id) references crs_h_user(id),
    constraint crs_l_user_tel_ck01 check (removed in (0, 1))
)
/
create sequence crs_l_user_tel_seq
/
comment on table crs_l_user_tel is 'User telephone to user link'
/
comment on column crs_l_user_tel.id is 'Identifier'
/
comment on column crs_l_user_tel.ldts is 'Load date'
/
comment on column crs_l_user_tel.removed is 'Removed flag'
/
comment on column crs_l_user_tel.user_tel_id is 'Reference to user telephone hub'
/
comment on column crs_l_user_tel.user_id is 'Reference to user hub'
/
create index crs_l_user_tel_i01 on crs_l_user_tel(user_tel_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_user_tel_i02 on crs_l_user_tel(user_id, ldts) compress 1 tablespace spoindx
/

--changeset akirilchev:crs-1.1.0-VTBCRS-590-email logicalFilePath:crs-1.1.0-VTBCRS-590-email endDelimiter:/
create table crs_h_user_email(
    id   number        not null,
    key  varchar2(100) not null,
    ldts timestamp     not null,
    constraint crs_h_user_email_pk   primary key(id) using index tablespace spoindx,
    constraint crs_h_user_email_uk01 unique(key)     using index tablespace spoindx
)
/
create sequence crs_h_user_email_seq
/
comment on table crs_h_user_email is 'User email hub'
/
comment on column crs_h_user_email.id is 'Identifier'
/
comment on column crs_h_user_email.key is 'Key'
/
comment on column crs_h_user_email.ldts is 'Load date'
/
create table crs_s_user_email(
    id          number              not null,
    h_id        number              not null,
    digest      varchar2(100)       not null,
    removed     number(1) default 0 not null,
    ldts        timestamp           not null,
    email       varchar2(100),
    constraint crs_s_user_email_pk   primary key(id)    using index tablespace spoindx,
    constraint crs_s_user_email_uk01 unique(h_id, ldts) using index tablespace spoindx,
    constraint crs_s_user_email_fk01 foreign key(h_id)  references crs_h_user_email(id),
    constraint crs_s_user_email_ck01 check(removed in (0, 1))
)
/
create sequence crs_s_user_email_seq
/
comment on table crs_s_user_email is 'User email satellite'
/
comment on column crs_s_user_email.id is 'Identifier'
/
comment on column crs_s_user_email.h_id is 'Reference to hub'
/
comment on column crs_s_user_email.digest is 'Row digest'
/
comment on column crs_s_user_email.removed is 'Removed flag'
/
comment on column crs_s_user_email.ldts is 'Load date'
/
comment on column crs_s_user_email.email is 'User email'
/
create table crs_l_user_email(
    id      number       not null,
    user_email_id number not null,
    user_id number       not null,
    removed number(1, 0) default 0 not null,
    ldts    timestamp    not null,
    constraint crs_l_user_email_pk   primary key (id) using index tablespace spoindx,
    constraint crs_l_user_email_fk01 foreign key (user_email_id) references crs_h_user_email(id),
    constraint crs_l_user_email_fk02 foreign key (user_id) references crs_h_user(id),
    constraint crs_l_user_email_ck01 check (removed in (0, 1))
)
/
create sequence crs_l_user_email_seq
/
comment on table crs_l_user_email is 'User emailephone to user link'
/
comment on column crs_l_user_email.id is 'Identifier'
/
comment on column crs_l_user_email.ldts is 'Load date'
/
comment on column crs_l_user_email.removed is 'Removed flag'
/
comment on column crs_l_user_email.user_email_id is 'Reference to user email hub'
/
comment on column crs_l_user_email.user_id is 'Reference to user hub'
/
create index crs_l_user_email_i01 on crs_l_user_email(user_email_id, ldts) compress 1 tablespace spoindx
/
create index crs_l_user_email_i02 on crs_l_user_email(user_id, ldts) compress 1 tablespace spoindx
/

--changeset akirilchev:crs-1.1.0-VTBCRS-590-attr logicalFilePath:crs-1.1.0-VTBCRS-590-attr endDelimiter:/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    -- entity USER
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'USER_TEL', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Телефон пользователя',
            'User tel',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, entity_type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
/
declare
    v_entity_type_id number;
    v_h_entity_id number;
    v_ldts timestamp := systimestamp;
begin
    select id into v_entity_type_id from crs_sys_h_entity_type where key = 'EMBEDDED_OBJECT';

    -- entity USER
    insert into crs_sys_h_entity (id, key, ldts)
    values (crs_sys_h_entity_seq.nextval, 'USER_EMAIL', v_ldts)
    returning id into v_h_entity_id;

    insert into crs_sys_s_entity (id, h_id, ldts, form, name_ru, name_en, link_table, attribute_key, hierarchical, removed)
    values (crs_sys_s_entity_seq.nextval,
            v_h_entity_id,
            v_ldts,
            null,
            'Email пользователя',
            'User email',
            null,
            null,
            0,
            0);
    insert into crs_sys_l_entity_type (id, entity_type_id, entity_id, ldts, removed)
    values (crs_sys_l_entity_type_seq.nextval, v_entity_type_id, v_h_entity_id, v_ldts, 0);
end;
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
    v_entity_key := 'USER_TEL';
    select id into v_entity_hub_id
      from crs_sys_h_entity
     where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'TEL_NUMBER', 'Телефон пользователя', 'User tel', 0, null, 'STRING', 'TEL_NUMBER', null, null, v_ldts);

    v_entity_key := 'USER_EMAIL';
    select id into v_entity_hub_id
      from crs_sys_h_entity
     where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'EMAIL', 'Email пользователя', 'User email', 0, null, 'STRING', 'EMAIL', null, null, v_ldts);

    v_entity_key := 'USER';
    select id into v_entity_hub_id
      from crs_sys_h_entity
     where key = v_entity_key;
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'TEL_NUMBER', 'Телефон пользователя', 'User tel', 0, 'crs_l_user_tel', 'REFERENCE', null, 'USER_TEL', 'TEL_NUMBER', v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'EMAIL', 'Email пользователя', 'User email', 0, 'crs_l_user_email', 'REFERENCE', null, 'USER_EMAIL', 'EMAIL', v_ldts);
end;
/

--changeset akirilchev:crs-1.1.0-VTBCRS-590-migrate-tel logicalFilePath:crs-1.1.0-VTBCRS-590-migrate-tel endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:1 select count(*) from user_tab_cols where (table_name, column_name) in (select 'USERS', 'CELL_PHONE' from dual)
declare
    v_ldts timestamp := to_date('011111', 'mmyyyy');
    v_hub_id number;
begin
    for rec in (select * from users where cell_phone is not null) loop
        insert into crs_h_user_tel(id, key, ldts)
        values(crs_h_user_tel_seq.nextval, crs_h_user_tel_seq.nextval, v_ldts)
        returning id into v_hub_id;

        insert into crs_s_user_tel(id, h_id, digest, removed, ldts, tel_number)
        values(crs_s_user_tel_seq.nextval, v_hub_id, 'no digest', 0, v_ldts, rec.cell_phone);

        insert into crs_l_user_tel(id, user_tel_id, user_id, removed, ldts)
        values(crs_l_user_tel_seq.nextval, v_hub_id, (select id
                                                        from crs_h_user
                                                       where key = upper(to_char(rec.login))),
               0, v_ldts);
    end loop;
end;
/

--changeset akirilchev:crs-1.1.0-VTBCRS-590-migrate-email logicalFilePath:crs-1.1.0-VTBCRS-590-migrate-email endDelimiter:/
--preconditions onFail:CONTINUE onError:HALT
--precondition-sql-check expectedResult:2 select count(*) from user_tab_cols where (table_name, column_name) in (select 'USERS', 'SURNAME' from dual union all select 'USER_EMAIL', 'EMAIL' from dual)
declare
    v_ldts timestamp := to_date('011111', 'mmyyyy');
    v_hub_id number;
begin
    for rec in (select distinct e.id_user, e.email, u.login from user_email e join users u on u.id_user = e.id_user) loop
        insert into crs_h_user_email(id, key, ldts)
        values(crs_h_user_email_seq.nextval, crs_h_user_email_seq.nextval, v_ldts)
        returning id into v_hub_id;

        insert into crs_s_user_email(id, h_id, digest, removed, ldts, email)
        values(crs_s_user_email_seq.nextval, v_hub_id, 'no digest', 0, v_ldts, rec.email);

        insert into crs_l_user_email(id, user_email_id, user_id, removed, ldts)
        values(crs_l_user_email_seq.nextval, v_hub_id, (select id
                                                          from crs_h_user
                                                         where key = upper(to_char(rec.login))),
               0, v_ldts);
    end loop;
end;
/

--changeset akirilchev:crs-1.1.0-VTBCRS-581-client-group-client-del logicalFilePath:crs-1.1.0-VTBCRS-581-client-group-client-del endDelimiter:/
delete crs_sys_l_entity_attribute where attribute_id in (select id from crs_sys_h_attribute where key in ('CLIENT_GROUP#CLIENT'))
/
delete crs_sys_s_attribute
 where h_id in (select id from crs_sys_h_attribute where key in ('CLIENT_GROUP#CLIENT'))
    or attribute_key in ('CLIENT_GROUP#CLIENT')
/
delete crs_sys_h_attribute where key in ('CLIENT_GROUP#CLIENT')
/

--changeset akirilchev:crs-1.1.0-VTBCRS-581-dep-attr-del logicalFilePath:crs-1.1.0-VTBCRS-581-dep-attr-del endDelimiter:/
delete crs_sys_l_entity_attribute where attribute_id in (select id from crs_sys_h_attribute where key in ('DEPARTMENT#CLIENT', 'DEPARTMENT#USER'))
/
delete crs_sys_s_attribute
 where h_id in (select id from crs_sys_h_attribute where key in ('DEPARTMENT#CLIENT', 'DEPARTMENT#USER'))
    or attribute_key in ('DEPARTMENT#CLIENT', 'DEPARTMENT#USER')
/
delete crs_sys_h_attribute where key in ('DEPARTMENT#CLIENT', 'DEPARTMENT#USER')
/

--changeset akirilchev:crs-1.1.0-VTBCRS-581-index logicalFilePath:crs-1.1.0-VTBCRS-581-index endDelimiter:/
create index crs_s_client_group_i01 on crs_s_client_group(ldts, h_id, removed, id) compute statistics
/

--changeset pmasalov:crs-1.1.0-VTBCRS-538-use-to-role-view-attribute logicalFilePath:crs-1.1.0-VTBCRS-538-use-to-role-view-attribute endDelimiter:/
update crs_sys_s_attribute set attribute_key = 'ROLE#NAME'
 where h_id = (select id from crs_sys_h_attribute where key = 'USER#ROLES')
/

--changeset pmasalov:crs-1.1.0-VTBCRS-538-allowed-actions-link-data logicalFilePath:crs-1.1.0-VTBCRS-538-allowed-actions-link-data endDelimiter:/
declare
    v_ldts timestamp := to_date('011111', 'mmyyyy');

    procedure link_entity(p_entity_key varchar2, p_business_action_key varchar2) is
        begin
            insert into crs_sys_l_entity_business_action(id, entity_id, business_action_id, ldts)
            values (crs_sys_l_entity_business_action_seq.nextval,
                    (select id from crs_sys_h_entity where key = p_entity_key),
                    (select id from crs_sys_h_business_action where key = p_business_action_key),
                    v_ldts);

        end;

begin
    -- Шаблон -> Создание	Просмотр	Редактирование	Удаление	Выполнение	Использование в Расчете
    link_entity('FORM_TEMPLATE', 'CREATE_NEW');
    link_entity('FORM_TEMPLATE', 'VIEW');
    link_entity('FORM_TEMPLATE', 'EDIT');
    link_entity('FORM_TEMPLATE', 'REMOVE');
    link_entity('FORM_TEMPLATE', 'EXECUTE');
    link_entity('FORM_TEMPLATE', 'USE_AT_CALC');

    -- Модель расчета     ->   Создание	Просмотр	Редактирование	Удаление	Выполнение	Публикация	Использование в Расчете
    link_entity('CALC_MODEL', 'CREATE_NEW');
    link_entity('CALC_MODEL', 'VIEW');
    link_entity('CALC_MODEL', 'EDIT');
    link_entity('CALC_MODEL', 'REMOVE');
    link_entity('CALC_MODEL', 'EXECUTE');
    link_entity('CALC_MODEL', 'PUBLISH');
    link_entity('CALC_MODEL', 'USE_AT_CALC');

    -- Клиент  --> Просмотр    Использование в Расчете
    link_entity('CLIENT', 'VIEW');
    link_entity('CLIENT', 'USE_AT_CALC');
    -- Группа  --> Просмотр   Редактирование   Использование в Расчете
    link_entity('CLIENT', 'VIEW');
    link_entity('CLIENT', 'EDIT');
    link_entity('CLIENT', 'USE_AT_CALC');

    -- Показатель  -->    Создание	Просмотр	Редактирование	Удаление	Использование в Расчете
    link_entity('CALC_FORMULA', 'CREATE_NEW');
    link_entity('CALC_FORMULA', 'VIEW');
    link_entity('CALC_FORMULA', 'EDIT');
    link_entity('CALC_FORMULA', 'REMOVE');
    link_entity('CALC_FORMULA', 'USE_AT_CALC');
end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-538-allowed-actions-entity-meta logicalFilePath:crs-1.1.0-VTBCRS-538-allowed-actions-entity-meta endDelimiter:/
declare
    v_ldts timestamp := to_date('011111', 'mmyyyy');

    procedure link_entity(p_entity_key varchar2, p_business_action_key varchar2) is
        begin
            insert into crs_sys_l_entity_business_action(id, entity_id, business_action_id, ldts)
            values (crs_sys_l_entity_business_action_seq.nextval,
                    (select id from crs_sys_h_entity where key = p_entity_key),
                    (select id from crs_sys_h_business_action where key = p_business_action_key),
                    v_ldts);

        end;

begin
    -- Справочник(структура/метаданные) -> Создание	Просмотр	Редактирование
    link_entity('ENTITY', 'CREATE_NEW');
    link_entity('ENTITY', 'VIEW');
    link_entity('ENTITY', 'EDIT');
end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-538-allowed-actions-entity-meta2 logicalFilePath:crs-1.1.0-VTBCRS-538-allowed-actions-entity-meta2 endDelimiter:/
declare
    v_ldts timestamp := to_date('011111', 'mmyyyy');

    procedure link_entity(p_entity_key varchar2, p_business_action_key varchar2) is
        begin
            insert into crs_sys_l_entity_business_action(id, entity_id, business_action_id, ldts)
            values (crs_sys_l_entity_business_action_seq.nextval,
                    (select id from crs_sys_h_entity where key = p_entity_key),
                    (select id from crs_sys_h_business_action where key = p_business_action_key),
                    v_ldts);

        end;

begin
    -- Справочник(структура/метаданные) -> Удаление
    link_entity('ENTITY', 'REMOVE');
end;
/

--changeset imatushak:crs-1.1.0-VTBCRS-538 logicalFilePath:crs-1.1.0-VTBCRS-538 endDelimiter:/
update crs_sys_s_attribute
   set view_order = 4
 where h_id = (select id from crs_sys_h_attribute where key = 'USER#ROLES')
/

--changeset imatushak:crs-1.1.0-VTBCRS-538-1 logicalFilePath:crs-1.1.0-VTBCRS-538-1 endDelimiter:/
update crs_sys_s_attribute
   set name_ru = 'Сокращенное наименование'
 where h_id = (select id from crs_sys_h_attribute where key = 'ROLE#NAME')
/
update crs_sys_s_attribute
   set name_en = 'Short name'
 where h_id = (select id from crs_sys_h_attribute where key = 'ROLE#NAME')
/
update crs_sys_s_attribute
   set name_ru = 'Полное наименование'
 where h_id = (select id from crs_sys_h_attribute where key = 'ROLE#DESCRIPTION')
/
update crs_sys_s_attribute
   set name_en = 'Full name'
 where h_id = (select id from crs_sys_h_attribute where key = 'ROLE#DESCRIPTION')
/

--changeset pmasalov:crs-1.1.0-VTBCRS-538-rename-roles logicalFilePath:crs-1.1.0-VTBCRS-538-rename-roles endDelimiter:/
declare
    v_ldts timestamp := systimestamp;

    procedure rename_role(
        p_role_key     crs_h_role.key%type,
        p_role_name_ru crs_sys_s_localization.string_ru%type,
        p_role_name_en crs_sys_s_localization.string_en%type,
        p_role_desc_ru crs_sys_s_localization.string_ru%type,
        p_role_desc_en crs_sys_s_localization.string_en%type,
        p_ldts         timestamp
    ) is
        v_ids crs_number_table;
        v_role_id number;
        begin
            select id into v_role_id
              from crs_h_role
             where key = p_role_key;
            select ld.localization_id
              bulk collect into v_ids
              from crs_h_role h join crs_s_role s on s.h_id = h.id
                                join crs_l_role_desc ld on ld.role_id = h.id
                                left join crs_sys_s_localization ds on ds.h_id = ld.localization_id
             where h.key = p_role_key;

            forall i in 1..v_ids.count
                delete from crs_l_role_desc where localization_id = v_ids(i);
            forall i in 1..v_ids.count
                delete from crs_sys_s_localization where h_id = v_ids(i);
            forall i in 1..v_ids.count
                delete from crs_sys_h_localization where id = v_ids(i);

            select ld.localization_id
              bulk collect into v_ids
              from crs_h_role h join crs_s_role s on s.h_id = h.id
                                join crs_l_role_name ld on ld.role_id = h.id
                                left outer join crs_sys_s_localization ds on ds.h_id = ld.localization_id
             where h.key = p_role_key;

            forall i in 1..v_ids.count
                delete from crs_l_role_name where localization_id = v_ids(i);
            forall i in 1..v_ids.count
                delete from crs_sys_s_localization where h_id = v_ids(i);
            forall i in 1..v_ids.count
                delete from crs_sys_h_localization where id = v_ids(i);

            insert into crs_sys_h_localization(id, key, ldts)
            values (crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_ldts);
            insert into crs_sys_s_localization(id, h_id, ldts, string_ru, string_en, digest)
            values (crs_sys_s_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_ldts, p_role_name_ru, p_role_name_en, 'NO DIGEST');
            insert into crs_l_role_name(id, role_id, localization_id, ldts)
            values (crs_l_role_name_seq.nextval, v_role_id, crs_sys_h_localization_seq.currval, p_ldts);

            insert into crs_sys_h_localization(id, key, ldts)
            values (crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_ldts);
            insert into crs_sys_s_localization(id, h_id, ldts, text_ru, text_en, digest)
            values (crs_sys_s_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_ldts, p_role_desc_ru, p_role_desc_en, 'NO DIGEST');
            insert into crs_l_role_desc(id, role_id, localization_id, ldts)
            values (crs_l_role_desc_seq.nextval, v_role_id, crs_sys_h_localization_seq.currval, p_ldts);
        end;
begin
    rename_role('КМ', 'КМ', 'CM', 'Клиентский менеджер', 'Client manager', v_ldts);
    rename_role('КИ', 'КИ', 'CI', 'Кредитный инспектор', 'Credit inspector', v_ldts);
    rename_role('МЭ', 'МЭ', 'EM', 'Менеджер эксперт', 'Expert manager', v_ldts);
    rename_role('КЭ', 'КЭ', 'CE', 'Кредитный эксперт', 'Credit expert', v_ldts);
    rename_role('КЭ (СБ)', 'КЭ (СБ)', 'MBCE', 'Кредитный эксперт-средний бизнес', 'Medium-sized business credit expert', v_ldts);
    rename_role('КЭ (КБ)', 'КЭ (КБ)', 'LBCE', 'Кредитный эксперт-крупный бизнес', 'Large business credit expert', v_ldts);
    rename_role('КЭ (СЗРЦ)', 'КЭ (СЗРЦ)', 'NWRC CE', 'Кредитный эксперт СЗРЦ', 'NWRC credit expert', v_ldts);
    rename_role('КЭ (ДФК)', 'КЭ (ДФК)', 'CFC CE', 'Кредитный эксперт ДФК', 'CFC credit expert', v_ldts);
    rename_role('РА', 'РА', 'RA', 'Риск-аналитик', 'Risk analyst', v_ldts);
    rename_role('РА (СЗРЦ)', 'РА (СЗРЦ)', 'NWRC RA', 'Риск-аналитик СЗРЦ', 'NWRC risk analyst', v_ldts);
    rename_role('РА (ДФК)', 'РА (ДФК)', 'CFC RA', 'Риск-аналитик ДФК ', 'CFC risk analyst', v_ldts);
    rename_role('БА', 'БА', 'BA', 'Бизнес-администратор', 'Business administrator', v_ldts);
    rename_role('БА (СЗРЦ)', 'БА (СЗРЦ)', 'NERC BA', 'Бизнес-администратор СЗРЦ', 'NWRC business administrator', v_ldts);
    rename_role('БА (ДФК)', 'БА (ДФК)', 'CFC BA', 'Бизнес-администратор ДФК', 'CFC business administrator', v_ldts);
    rename_role('АУД', 'АУД', 'AUD', 'Аудитор', 'Auditor', v_ldts);
    rename_role('АУД (СЗРЦ)', 'АУД (СЗРЦ)', 'NWRC AUD', 'Аудитор СЗРЦ', 'NWRC auditor', v_ldts);
    rename_role('АУД (ДФК)', 'АУД (ДФК)', 'CFC AUD', 'Аудитор ДФК', 'CFC auditor', v_ldts);
    rename_role('АДМ', 'АДМ', 'SA', 'Администратор системы', 'System administrator', v_ldts);
    rename_role('АДМ_ПОЛЬЗ', 'АДМ_ПОЛЬЗ', 'UA', 'Администратор пользователей', 'User administrator', v_ldts);
    rename_role('АДМ_РОЛИ', 'АДМ_РОЛИ', 'CA', 'Администратор полномочий', 'Credentials administrator', v_ldts);
    rename_role('АДМ_ВИЗ', 'АДМ_ВИЗ', 'VCA', 'Администратор, визирующий действия Администратора полномочий', 'Visa credentials administrator', v_ldts);
    rename_role('АУД (ИБ)', 'АУД (ИБ)', 'SA', 'Аудитор ИБ', 'Security auditor', v_ldts);
end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-610-entity-action-satellite logicalFilePath:crs-1.1.0-VTBCRS-610-entity-action-satellite endDelimiter:/
create table crs_sys_l_s_entity_business_action
(
    id number not null,
    link_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null,
    digest varchar2(100) not null,
    cancel_secext_rule_available number
)
/
comment on table crs_sys_l_s_entity_business_action is 'Satellite for available actions for entity link table'
/
comment on column crs_sys_l_s_entity_business_action.id is 'Identifier'
/
comment on column crs_sys_l_s_entity_business_action.ldts is 'Load date'
/
comment on column crs_sys_l_s_entity_business_action.cancel_secext_rule_available is 'Cancellation of extended security rule are available'
/
comment on column crs_sys_l_s_entity_business_action.removed is 'Removed flag'
/
comment on column crs_sys_l_s_entity_business_action.link_id is 'Link table identifier'
/
comment on column crs_sys_l_s_entity_business_action.digest is 'Row digest'
/
alter table crs_sys_l_s_entity_business_action
    add constraint crs_sys_l_s_entity_business_action_pk primary key (id)
    using index
    tablespace spoindx
/
alter table crs_sys_l_s_entity_business_action
    add constraint crs_sys_l_s_entity_business_action_uk01 unique (link_id, ldts)
    using index
    tablespace spoindx
/
alter table crs_sys_l_s_entity_business_action
    add constraint crs_sys_l_s_entity_business_action_ck01
check (removed in (0, 1))
/
alter table crs_sys_l_s_entity_business_action
    add constraint crs_sys_l_s_entity_business_action_fk01 foreign key (link_id)
references crs_sys_l_entity_business_action (id)
/
create sequence crs_sys_l_s_entity_business_action_seq
/

--changeset pmasalov:crs-1.1.0-VTBCRS-610-entity-action-satellite-meta logicalFilePath:crs-1.1.0-VTBCRS-610-entity-action-satellite-meta endDelimiter:/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'ENTITY#BUSINESS_ACTION';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'ENTITY#BUSINESS_ACTION#CANCEL_SECEXT_RULE_AVAILABLE',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_attr_attr_id,
            v_ldts,
            0,
            0,
            'Доступность отмены расширенного правила безопасности',
            'Security extended rule cancellation availability',
            1,
            0,
            'BOOLEAN',
            'CANCEL_SECEXT_RULE_AVAILABLE');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-610-entity-action-satellite-data logicalFilePath:crs-1.1.0-VTBCRS-610-entity-action-satellite-data endDelimiter:/
insert into crs_sys_l_s_entity_business_action(id,
                                               link_id,
                                               ldts,
                                               removed,
                                               digest,
                                               cancel_secext_rule_available)
  with l as (select leba.id link_id, leba.entity_id, leba.business_action_id
               from crs_sys_l_entity_business_action leba join crs_sys_h_entity he on he.id = leba.entity_id
                                                                               and he.key = 'CALC'
                                                          join crs_sys_h_business_action hba on hba.id = leba.business_action_id
                                                                                         and hba.key = 'VIEW')
select crs_sys_l_s_entity_business_action_seq.nextval, link_id, to_date('011111', 'mmyyyy'), 0, 'NO_DIGEST', 1
  from l
 where not exists(select 1 from crs_sys_l_s_entity_business_action lseba where lseba.link_id = l.link_id)
/

--changeset pmasalov:crs-1.1.0-VTBCRS-610-entity-permission-alter logicalFilePath:crs-1.1.0-VTBCRS-610-entity-permission-alter endDelimiter:/
alter table crs_s_permission add cancel_secext_rule number(1) default 0 not null
/
comment on column crs_s_permission.cancel_secext_rule is 'Security extended rule cancel flag'
/
alter table crs_s_permission drop constraint crs_s_permission_ck02
/
alter table crs_s_permission add constraint crs_s_permission_ck02 check (cancel_secext_rule in (0, 1))
/

--changeset pmasalov:crs-1.1.0-VTBCRS-610-entity-permission-meta logicalFilePath:crs-1.1.0-VTBCRS-610-entity-permission-meta endDelimiter:/
declare
    v_h_entity_id number;
    v_h_attribute_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_h_entity_id from crs_sys_h_entity where key = 'PERMISSION';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'PERMISSION#CANCEL_SECEXT_RULE',v_ldts)
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
            'Отмена расширенных правил безопасности',
            'Cancel security extended rule',
            0,
            null,
            0,
            'BOOLEAN',
            'CANCEL_SECEXT_RULE');
    insert into crs_sys_l_entity_attribute (id, entity_id, attribute_id, ldts, removed)
    values (crs_sys_l_entity_attribute_seq.nextval, v_h_entity_id, v_h_attribute_id, v_ldts, 0);
end;
/


--changeset akamordin:crs-1.1.0-VTBCRS-624-add-department-columns logicalFilePath:crs-1.1.0-VTBCRS-624-add-department-columns endDelimiter:/
declare
    v_entity_type_id number;
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    function get_hub_id_by_key(p_entity_key in crs_sys_h_entity.key%type
    ) return crs_sys_h_entity.id%type is
        v_hub_id number;
        begin
            for r in (select c.id from crs_sys_h_entity c where c.key = p_entity_key) loop
                v_hub_id := r.id;
            end loop;
            return v_hub_id;
        end get_hub_id_by_key;

    procedure insert_entity_attribute(
        p_entity_hub_id     crs_sys_h_entity.id%type,
        p_entity_key        crs_sys_h_entity.key%type,
        p_attribute_key     varchar2,
        p_attribute_name_ru crs_sys_s_attribute.name_ru%type,
        p_attribute_name_en crs_sys_s_attribute.name_en%type,
        p_view_order        crs_sys_s_attribute.view_order%type,
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
                id, h_id, ldts, view_order, multilang, link_table,
                name_ru, name_en, type, native_column, entity_key,
                attribute_key
            )
            values(
                crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_view_order, p_multilang, p_link_table,
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
        end insert_entity_attribute;
begin
    --DEPARTMENT
    v_entity_key := 'DEPARTMENT';
    v_entity_hub_id := get_hub_id_by_key(v_entity_key);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'BRANCH', 'Дочерняя компания (ДФК)', 'Is child company', 4, 0, null, 'BOOLEAN', 'BRANCH', null, null, v_ldts);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'ORG_UNIT', 'Структурное подразделение', 'Structural unit', 5, 0, null, 'BOOLEAN', 'ORG_UNIT', null, null, v_ldts);
end;
/

alter table crs_s_department add branch number default 0 not null
/
alter table crs_s_department add org_unit number default 0 not null
/

--changeset akirilchev:crs-1.1.0-VTBCRS-501-attr-vieworder logicalFilePath:crs-1.1.0-VTBCRS-501-attr-vieworder endDelimiter:/
update crs_sys_s_attribute
   set view_order = 6, name_ru = 'Email', name_en = 'Email'
 where h_id in (select id from crs_sys_h_attribute where key = 'USER#EMAIL')
/
update crs_sys_s_attribute
   set view_order = 7, name_ru = 'Телефон', name_en = 'Cell phone'
 where h_id in (select id from crs_sys_h_attribute where key = 'USER#TEL_NUMBER')
/

--changeset akamordin:crs-1.1.0-VTBCRS-636-alter-column logicalFilePath:crs-1.1.0-VTBCRS-636-alter-column endDelimiter:/
update crs_sys_s_localization s
   set s.string_ru = dbms_lob.substr(s.text_ru, 4000, 1),
       s.string_en = dbms_lob.substr(s.text_en, 4000, 1),
       s.text_ru   = null,
       s.text_en   = null
 where s.h_id in (
   select l.localization_id
     from crs_l_role_desc l
)
   and s.string_ru is null
   and s.string_en is null
/

begin
  for r in (
    select ha.key, sa.*
      from crs_sys_s_entity se
      join crs_sys_h_entity he on he.id = se.h_id and he.key = 'ROLE'
      join crs_sys_l_entity_attribute lea on lea.entity_id = he.id
      join crs_sys_h_attribute ha on ha.id = lea.attribute_id
      join crs_sys_s_attribute sa on sa.h_id = ha.id
  )
  loop
    if substr(r.key, instr(r.key, '#') + 1) = 'NAME' then
      update crs_sys_s_attribute sa
         set sa.view_order = 1
       where sa.id = r.id;
    elsif substr(r.key, instr(r.key, '#') + 1) = 'DESCRIPTION' then
      update crs_sys_s_attribute sa
         set sa.view_order = 2, sa.type = 'STRING'
       where sa.id = r.id;
    elsif substr(r.key, instr(r.key, '#') + 1) = 'EMBEDDED' then
      update crs_sys_s_attribute sa
         set sa.view_order = 3
       where sa.id = r.id;
    end if;
  end loop;
end;
/

--changeset svaliev:crs-1.1.0-VTBCRS-662 logicalFilePath:crs-1.1.0-VTBCRS-662 endDelimiter:/
alter table crs_s_user drop column full_name
/
alter table crs_s_user modify surname varchar2(4000)
/
alter table crs_s_user modify name varchar2(4000)
/
alter table crs_s_user modify patronymic varchar2(4000)
/
alter table crs_s_user add full_name generated always as (rtrim(((case when surname is not null then surname ||' ' end) || (case when name is not null then name ||' ' end )) || patronymic)) virtual
/

--changeset akamordin:crs-1.1.0-VTBCRS-647-add-role-column logicalFilePath:crs-1.1.0-VTBCRS-647-add-role-column endDelimiter:/
declare
    v_entity_type_id number;
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    function get_hub_id_by_key(p_entity_key in crs_sys_h_entity.key%type
    ) return crs_sys_h_entity.id%type is
        v_hub_id number;
        begin
            for r in (select c.id from crs_sys_h_entity c where c.key = p_entity_key) loop
                v_hub_id := r.id;
            end loop;
            return v_hub_id;
        end get_hub_id_by_key;

    procedure insert_entity_attribute(
        p_entity_hub_id     crs_sys_h_entity.id%type,
        p_entity_key        crs_sys_h_entity.key%type,
        p_attribute_key     varchar2,
        p_attribute_name_ru crs_sys_s_attribute.name_ru%type,
        p_attribute_name_en crs_sys_s_attribute.name_en%type,
        p_view_order        crs_sys_s_attribute.view_order%type,
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
                id, h_id, ldts, view_order, multilang, link_table,
                name_ru, name_en, type, native_column, entity_key,
                attribute_key
            )
            values(
                crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_view_order, p_multilang, p_link_table,
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
        end insert_entity_attribute;
begin
    --ROLE
    v_entity_key := 'ROLE';
    v_entity_hub_id := get_hub_id_by_key(v_entity_key);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'BRANCH', 'Дочерняя компания (ДФК)', 'Is child company', 4, 0, null, 'BOOLEAN', 'BRANCH', null, null, v_ldts);
end;
/

alter table crs_s_role add branch number default 0 not null
/

--changeset akamordin:crs-1.1.0-VTBCRS-647-rename-role-key logicalFilePath:crs-1.1.0-VTBCRS-647-rename-role-key endDelimiter:/
declare
  v_key  number;
  v_exit boolean;
begin
  for r in (
    select hr.id h_id, hr.key, l.id l_id, replace(l.string_en, ' ', '_') as new_key
      from crs_h_role hr
      join crs_l_role_name lr on lr.role_id = hr.id
      join crs_sys_s_localization l on l.h_id = lr.localization_id
     where hr.key <> nvl(replace(l.string_en, ' ', '_'), '~!~')
     order by hr.id
  )
  loop
    if r.new_key is not null then
      v_exit := false;
      loop
        exit when v_exit;
        begin
          update crs_h_role hr
             set hr.key = r.new_key||to_char(v_key)
           where hr.id = r.h_id;
          v_exit := true;
        exception
          when dup_val_on_index then
            v_key := nvl(v_key, 0) + 1;
        end;
      end loop;
    else
      update crs_sys_s_localization l
         set l.string_en = r.key
       where l.id = r.l_id;
    end if;
  end loop;
end;
/

--changeset svaliev:crs-1.1.0-VTBCRS-591-expand-fields logicalFilePath:crs-1.1.0-VTBCRS-591-expand-fields endDelimiter:/
alter table crs_s_user_tel modify tel_number varchar2(4000)
/
alter table crs_s_user_email modify email varchar2(4000)
/

--changeset akamordin:crs-1.1.0-VTBCRS-653-add-role-column logicalFilePath:crs-1.1.0-VTBCRS-653-add-role-column endDelimiter:/
declare
    v_entity_type_id number;
    v_entity_key     varchar2(32);
    v_ldts           timestamp := to_timestamp('011111', 'mmyyyy');
    v_entity_hub_id  number;

    function get_hub_id_by_key(p_entity_key in crs_sys_h_entity.key%type
    ) return crs_sys_h_entity.id%type is
        v_hub_id number;
        begin
            for r in (select c.id from crs_sys_h_entity c where c.key = p_entity_key) loop
                v_hub_id := r.id;
            end loop;
            return v_hub_id;
        end get_hub_id_by_key;

    procedure insert_entity_attribute(
        p_entity_hub_id     crs_sys_h_entity.id%type,
        p_entity_key        crs_sys_h_entity.key%type,
        p_attribute_key     varchar2,
        p_attribute_name_ru crs_sys_s_attribute.name_ru%type,
        p_attribute_name_en crs_sys_s_attribute.name_en%type,
        p_view_order        crs_sys_s_attribute.view_order%type,
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
                id, h_id, ldts, view_order, multilang, link_table,
                name_ru, name_en, type, native_column, entity_key,
                attribute_key
            )
            values(
                crs_sys_s_attribute_seq.nextval, crs_sys_h_attribute_seq.currval, p_ldts, p_view_order, p_multilang, p_link_table,
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
        end insert_entity_attribute;
begin
    --ROLE
    v_entity_key := 'ROLE';
    v_entity_hub_id := get_hub_id_by_key(v_entity_key);
    insert_entity_attribute(v_entity_hub_id, v_entity_key, 'APPROVED', 'Утверждена', 'Is role approved', 5, 0, null, 'BOOLEAN', 'APPROVED', null, null, v_ldts);
end;
/

alter table crs_s_role add approved number default 0 not null
/

--changeset akamordin:crs-1.1.0-VTBCRS-657-add-link-approved logicalFilePath:crs-1.1.0-VTBCRS-657-add-link-approved endDelimiter:/
create table crs_l_s_user_role
(
    id number not null,
    link_id number not null,
    ldts timestamp not null,
    removed number(1) default 0 not null,
    digest varchar2(100) not null,
    approved number(1) default 0 not null
)
/
comment on table crs_l_s_user_role is 'Satellite for link table crs_l_user_role'
/
comment on column crs_l_s_user_role.id is 'Identifier'
/
comment on column crs_l_s_user_role.ldts is 'Load date'
/
comment on column crs_l_s_user_role.removed is 'Removed flag'
/
comment on column crs_l_s_user_role.link_id is 'Link table identifier'
/
comment on column crs_l_s_user_role.digest is 'Row digest'
/
comment on column crs_l_s_user_role.approved is 'Is link approved'
/

alter table crs_l_s_user_role
    add constraint crs_l_s_user_role_pk primary key (id)
    using index
    tablespace spoindx
/
alter table crs_l_s_user_role
    add constraint crs_l_s_user_role_uk01 unique (link_id, ldts)
    using index
    tablespace spoindx
/
alter table crs_l_s_user_role
    add constraint crs_l_s_user_role_ck01
check (removed in (0, 1))
/
alter table crs_l_s_user_role
    add constraint crs_l_s_user_role_fk01 foreign key (link_id)
references crs_l_user_role (id)
/
create sequence crs_l_s_user_role_seq
/
declare
    v_ref_attr_id number;
    v_attr_attr_id number;
    v_ldts timestamp := to_date('011111', 'mmyyyy');
begin
    select id into v_ref_attr_id from crs_sys_h_attribute where key = 'USER#ROLES';

    insert into crs_sys_h_attribute (id, key, ldts)
    values (crs_sys_h_attribute_seq.nextval,'USER#ROLES#APPROVED',v_ldts)
    returning id into v_attr_attr_id;
    insert into crs_sys_s_attribute (id, h_id, ldts, view_order, nullable, name_ru, name_en, filter_available,
                                     removed, type, native_column)
    values (crs_sys_s_attribute_seq.nextval,
            v_attr_attr_id,
            v_ldts,
            0,
            0,
            'Роль пользователя утверждена',
            'User role approved',
            1,
            0,
            'BOOLEAN',
            'APPROVED');
    insert into crs_sys_l_ref_attribute (id, attribute_id, attr_attribute_id, ldts, removed)
    values (crs_sys_l_ref_attribute_seq.nextval, v_ref_attr_id, v_attr_attr_id, v_ldts, 0);
end;
/

--changeset pmasalov:crs-1.1.0-VTBCRS-609-update-setup logicalFilePath:crs-1.1.0-VTBCRS-609-update-setup endDelimiter:/
delete from crs_sys_l_entity_business_action where id in (
    select leba.id from crs_sys_l_entity_business_action leba
                   join crs_sys_h_entity he on he.id = leba.entity_id and he.key in ( 'CALC_MODEL','FORM_TEMPLATE' )
                   join crs_sys_h_business_action hba on hba.id = leba.business_action_id and hba.key = 'EXECUTE')
/

update crs_sys_s_entity_type set name_ru = 'Справочник (данные)', name_en = 'Dictionary (data)'
where h_id = (select id from crs_sys_h_entity_type where key = 'DICTIONARY')
/

update crs_sys_s_entity set name_ru = 'Справочник (структура)', name_en = 'Dictionary (structure)'
where h_id = (select id from crs_sys_h_entity where key = 'ENTITY')
/

--changeset pmasalov:crs-1.1.0-VTBCRS-678-remove-allowed-action logicalFilePath:crs-1.1.0-VTBCRS-678-remove-allowed-action endDelimiter:/
delete from crs_sys_l_entity_type_business_action
 where entity_type_id = (select id from crs_sys_h_entity_type where key = 'DICTIONARY')
   and business_action_id = (select id from crs_sys_h_business_action where key = 'USE_AT_CALC')
/
