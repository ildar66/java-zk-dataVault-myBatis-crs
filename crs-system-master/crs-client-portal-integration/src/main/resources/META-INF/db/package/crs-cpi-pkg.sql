--liquibase formatted sql

--changeset achalov:crs-cpi-pkg logicalFilePath:crs-1.0-crs-cpi-pkg endDelimiter:\n/ runOnChange:true
create or replace package crs_cpi_pkg as

    --default value for inserted digest
    DEFAULT_DIGEST constant crs_s_client.digest%type := 'NO DIGEST';
    NOT_EMPTY_VALUE constant varchar2(30)            := 'NOTEMPTYVALUE';
    NOT_EMPTY_NUMBER constant integer                := -1111;
    MICROSECOND constant interval day(9) to second(9):= numToDSInterval(1, 'second')/1000000;

    --zero timestamp
    ZERO_TIMESTAMP constant timestamp                := to_date('011111', 'mmyyyy');

    procedure synchronize_data;
end crs_cpi_pkg;
/
create or replace package body crs_cpi_pkg as

    procedure commit_and_lock(p_portal_view_name varchar2, p_current_run_ts timestamp) is
        v_result number;
        v_lockhandle varchar2(200);
        v_lock_name varchar2(200);
    begin
        update crs_cpi_last_sync s
           set s.last_sync_date = p_current_run_ts
         where s.id = p_portal_view_name
           and p_portal_view_name is not null;

        commit;

        v_lock_name := user || '_cpi_lock';
        dbms_lock.allocate_unique(lockname => v_lock_name,
                                  lockhandle => v_lockhandle);
        v_result := dbms_lock.request(lockhandle => v_lockhandle,
                                    lockmode => dbms_lock.x_mode,
                                    timeout => 0,
                                    release_on_commit => true);
        if v_result != 0 then
          raise_application_error(-20001, 'Client portal integration already running');
        end if;
    end commit_and_lock;

    procedure log_rollback_and_lock(p_portal_view_name varchar2, p_current_run_ts timestamp) is
        v_error_message varchar2(4000);
    begin
        rollback;
        v_error_message := sqlerrm|| chr(10) || chr(13) || dbms_utility.format_error_backtrace();

        insert into crs_cpi_log(id_log, last_sync_date, portal_view_name, error_text)
        values(crs_cpi_log_seq.nextval, p_current_run_ts, p_portal_view_name, v_error_message);

        commit_and_lock(null, p_current_run_ts);
    end log_rollback_and_lock;

    function insert_string_localization(
        p_string_ru      crs_sys_s_localization.string_ru%type,
        p_string_en      crs_sys_s_localization.string_en%type,
        p_current_run_ts timestamp
    ) return number is
    begin
        insert into crs_sys_h_localization(id, key, ldts)
        values(crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_current_run_ts);

        insert into crs_sys_s_localization(id, h_id, string_ru, string_en, digest, ldts)
        values(crs_sys_s_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_string_ru, p_string_en, DEFAULT_DIGEST, p_current_run_ts);

        return crs_sys_h_localization_seq.currval;
    end;

    function insert_text_localization(
        p_text_ru      crs_sys_s_localization.text_ru%type,
        p_text_en      crs_sys_s_localization.text_en%type,
        p_current_run_ts timestamp
    ) return number is
    begin
        insert into crs_sys_h_localization(id, key, ldts)
        values(crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_current_run_ts);

        insert into crs_sys_s_localization(id, h_id, text_ru, text_en, digest, ldts)
        values(crs_sys_s_localization_seq.nextval, crs_sys_h_localization_seq.currval, p_text_ru, p_text_en, DEFAULT_DIGEST, p_current_run_ts);

        return crs_sys_h_localization_seq.currval;
    end;

    -- read previous run timestamp
    function get_previous_run_ts(p_portal_view_name varchar2) return timestamp is
        v_previous_run_ts timestamp;
    begin
        begin
            select s.last_sync_date
              into v_previous_run_ts
              from crs_cpi_last_sync s
             where s.id = p_portal_view_name;
        exception
            when no_data_found then
                insert into crs_cpi_last_sync(id, last_sync_date)
                values(p_portal_view_name, crs_cpi_pkg.ZERO_TIMESTAMP)
                returning last_sync_date into v_previous_run_ts;
        end;
        return v_previous_run_ts;
    end get_previous_run_ts;

    procedure sync_client_category(
        p_current_run_ts  timestamp
    ) is
        v_hub_id              number;
        v_localization_hub_id crs_sys_h_localization.id%type;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_CATEGORY';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        for rec in (
            select categoryid, name, name_en
              from crs_cpi_client_category
             where last_update between v_previous_run_ts and p_current_run_ts
        ) loop
            begin
                select id into v_hub_id
                  from crs_h_client_category
                 where key = to_char(rec.categoryid);
            exception when no_data_found then
                insert into crs_h_client_category(id, key, ldts)
                values(crs_h_client_category_seq.nextval, to_char(rec.categoryid), p_current_run_ts);
                v_hub_id := crs_h_client_category_seq.currval;

                insert into crs_s_client_category(id, h_id, digest, ldts)
                values(crs_s_client_category_seq.nextval, v_hub_id, DEFAULT_DIGEST, p_current_run_ts);
            end;

            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_cat_name(id, client_category_id, localization_id, ldts, removed)
            values(crs_l_client_cat_name_seq.nextval, v_hub_id, v_localization_hub_id, p_current_run_ts, 0);
        end loop;

        --insert remove row
        insert into crs_s_client_category(id, h_id, digest, ldts, removed)
        select crs_s_client_category_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_category s) s join crs_h_client_category h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_number(h.key) not in (select t.categoryid
                                          from crs_cpi_client_category t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_currency(
        p_current_run_ts  timestamp
    ) is
        v_hub_id              number;
        v_localization_hub_id crs_sys_h_localization.id%type;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_CURRENCY';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        for rec in (
            select currencyid, code, code_num, name, name_en
              from crs_cpi_client_currency
             where last_update between v_previous_run_ts and p_current_run_ts
        ) loop
            begin
                select id into v_hub_id
                  from crs_h_client_currency
                 where key = to_char(rec.currencyid);
            exception when no_data_found then
                insert into crs_h_client_currency(id, key, ldts)
                values(crs_h_client_currency_seq.nextval, to_char(rec.currencyid), p_current_run_ts);
                v_hub_id := crs_h_client_currency_seq.currval;
            end;

            insert into crs_s_client_currency(id, h_id, code, code_num, digest, ldts)
            values(crs_s_client_currency_seq.nextval, v_hub_id, rec.code, rec.code_num, DEFAULT_DIGEST, p_current_run_ts);

            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_cur_name(id, client_currency_id, localization_id, ldts, removed)
            values(crs_l_client_cur_name_seq.nextval, v_hub_id, v_localization_hub_id, p_current_run_ts, 0);
        end loop;

        --insert remove row
        insert into crs_s_client_currency(id, h_id, digest, ldts, removed, code, code_num)
        select crs_s_client_currency_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1, s.code, s.code_num
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_currency s) s join crs_h_client_currency h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_number(h.key) not in (select t.currencyid
                                          from crs_cpi_client_currency t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_country(
        p_current_run_ts  timestamp
    ) is
        v_hub_id              number;
        v_localization_hub_id crs_sys_h_localization.id%type;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_COUNTRY';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        for rec in (
            select countryid, code_a2, code_a3, num_code, name, name_en
              from crs_cpi_client_country
             where last_update between v_previous_run_ts and p_current_run_ts
        ) loop
            begin
                select id into v_hub_id
                  from crs_h_client_country
                 where key = to_char(rec.countryid);
            exception when no_data_found then
                insert into crs_h_client_country(id, key, ldts)
                values(crs_h_client_country_seq.nextval, to_char(rec.countryid), p_current_run_ts);
                v_hub_id := crs_h_client_country_seq.currval;
            end;

            insert into crs_s_client_country(id, h_id, code_a2, code_a3, code_num, digest, ldts)
            values(crs_s_client_country_seq.nextval, v_hub_id, rec.code_a2, rec.code_a3, rec.num_code, DEFAULT_DIGEST, p_current_run_ts);

            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_cnt_name(id, client_country_id, localization_id, ldts, removed)
            values(crs_l_client_cnt_name_seq.nextval, v_hub_id, v_localization_hub_id, p_current_run_ts, 0);
        end loop;

        --insert remove row
        insert into crs_s_client_country(id, h_id, digest, ldts, removed, code_a2, code_a3, code_num)
        select crs_s_client_country_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1, s.code_a2, s.code_a3, s.code_num
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_country s) s join crs_h_client_country h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_number(h.key) not in (select t.countryid
                                          from crs_cpi_client_country t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_industry(
        p_current_run_ts  timestamp
    ) is
        v_hub_id              number;
        v_localization_hub_id crs_sys_h_localization.id%type;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_INDUSTRY';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        for rec in (
            select industryid, name, name_en
              from crs_cpi_client_industry
             where last_update between v_previous_run_ts and p_current_run_ts
        ) loop
            begin
                select id into v_hub_id
                  from crs_h_client_industry
                 where key = to_char(rec.industryid);
            exception when no_data_found then
                insert into crs_h_client_industry(id, key, ldts)
                values(crs_h_client_industry_seq.nextval, to_char(rec.industryid), p_current_run_ts);
                v_hub_id := crs_h_client_industry_seq.currval;

                insert into crs_s_client_industry(id, h_id, digest, ldts)
                values(crs_s_client_industry_seq.nextval, v_hub_id, DEFAULT_DIGEST, p_current_run_ts);
            end;

            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_ind_name(id, client_industry_id, localization_id, ldts, removed)
            values(crs_l_client_ind_name_seq.nextval, v_hub_id, v_localization_hub_id, p_current_run_ts, 0);
        end loop;

        --insert remove row
        insert into crs_s_client_industry(id, h_id, digest, ldts, removed)
        select crs_s_client_industry_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_industry s) s join crs_h_client_industry h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_number(h.key) not in (select t.industryid
                                          from crs_cpi_client_industry t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_opf(
        p_current_run_ts  timestamp
    ) is
        v_hub_id              number;
        v_localization_hub_id crs_sys_h_localization.id%type;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_OPF';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_cpi_client_opf_t (opfid, countryid, name, name_en, last_update)
        select opfid, countryid, name, name_en, last_update
          from crs_cpi_client_opf
         where last_update between v_previous_run_ts and p_current_run_ts;

        for rec in (
            select opfid, countryid, name, name_en
              from crs_cpi_client_opf_t
        ) loop
            begin
                select id into v_hub_id
                  from crs_h_client_opf
                 where key = to_char(rec.opfid);
            exception when no_data_found then
                insert into crs_h_client_opf(id, key, ldts)
                values(crs_h_client_opf_seq.nextval, to_char(rec.opfid), p_current_run_ts);
                v_hub_id := crs_h_client_opf_seq.currval;

                insert into crs_s_client_opf(id, h_id, digest, ldts)
                values(crs_s_client_opf_seq.nextval, v_hub_id, DEFAULT_DIGEST, p_current_run_ts);
            end;

            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_opf_name(id, client_opf_id, localization_id, ldts, removed)
            values(crs_l_client_opf_name_seq.nextval, v_hub_id, v_localization_hub_id, p_current_run_ts, 0);
        end loop;

        insert into crs_l_client_opf_country(id, ldts, client_opf_id, client_country_id, removed)
        select crs_l_client_opf_country_seq.nextval, dup.run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_country_id else z.client_country_id end client_country_id,
               dup.removed
          from (select h.id hub_id, prtl.countryid, ent.country_key, ent.client_country_id,
                       (select t.id
                          from crs_h_client_country t
                         where t.key = to_char(prtl.countryid)) new_client_country_id,
                       case when prtl.countryid is not null then 1 else 0 end row_has_value,
                       case when ent.client_country_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_opf_t prtl join crs_h_client_opf h on h.key = prtl.opfid
                                                 left join ( select ent.client_country_id, ent.client_opf_id, h.key country_key
                                                               from (select ent.*, row_number() over (partition by ent.client_opf_id, ent.client_country_id order by ent.ldts desc) rn
                                                                       from crs_l_client_opf_country ent) ent join crs_h_client_country h on h.id = ent.client_country_id
                                                                      where ent.rn = 1
                                                                        and ent.removed = 0) ent on ent.client_opf_id = h.id
                 where 0 = decode(prtl.countryid, ent.country_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        --insert remove row
        insert into crs_s_client_opf(id, h_id, digest, ldts, removed)
        select crs_s_client_opf_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_opf s) s join crs_h_client_opf h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_number(h.key) not in (select t.opfid
                                          from crs_cpi_client_opf t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_ogrn(
        p_current_run_ts  timestamp
    ) is
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_CLIENT_OGRN';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_cpi_client_ogrn_t(cl_global_prid, reg_num, countryid, vtb_legalentityid, last_update)
        select cl_global_prid, reg_num, countryid, vtb_legalentityid, last_update
          from crs_cpi_client_ogrn
         where last_update between v_previous_run_ts and p_current_run_ts;

        -- mark client for reindex
        update crs_h_client set key = key
        where key in ( select c.vtb_legalentityid
                         from crs_cpi_client_ogrn_t c);

        insert into crs_h_client_ogrn(id, key, ldts)
        select crs_h_client_ogrn_seq.nextval, to_char(t.cl_global_prid), p_current_run_ts
          from crs_cpi_client_ogrn_t t
         where not exists (select 1
                             from crs_h_client_ogrn t2
                            where t2.key = to_char(t.cl_global_prid));

        insert into crs_s_client_ogrn(id, h_id, reg_num, digest, ldts)
        select crs_s_client_ogrn_seq.nextval, h.id, t.reg_num, DEFAULT_DIGEST, p_current_run_ts
          from crs_cpi_client_ogrn_t t left join crs_h_client_ogrn h on h.key = to_char(t.cl_global_prid)
                                      left join ( select s.*
                                                    from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                                                            from crs_s_client_ogrn s) s
                                                   where s.rn = 1
                                                     and s.removed = 0) s on s.h_id = h.id
         where 0 = decode(t.reg_num, s.reg_num, 1, 0)
            or s.id is null;

        insert into crs_l_client_ogrn(id, ldts, client_ogrn_id, client_id, removed)
        select crs_l_client_ogrn_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_id else z.client_id end client_id,
               dup.removed
          from (select h.id hub_id, prtl.vtb_legalentityid, ent.client_key, ent.client_id,
                       (select t.id
                          from crs_h_client t
                         where t.key = prtl.vtb_legalentityid) new_client_id,
                       case when prtl.vtb_legalentityid is not null then 1 else 0 end row_has_value,
                       case when ent.client_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_ogrn_t prtl join crs_h_client_ogrn h on h.key = to_char(prtl.cl_global_prid)
                                                  left join ( select ent.client_id, ent.client_ogrn_id, h.key client_key
                                                               from (select ent.*, row_number() over (partition by ent.client_ogrn_id, ent.client_id order by ent.ldts desc) rn
                                                                       from crs_l_client_ogrn ent) ent join crs_h_client h on h.id = ent.client_id
                                                                      where ent.rn = 1
                                                                        and ent.removed = 0) ent on ent.client_ogrn_id = h.id
                 where 0 = decode(prtl.vtb_legalentityid, ent.client_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        insert into crs_l_client_ogrn_country(id, ldts, client_ogrn_id, client_country_id, removed)
        select crs_l_client_ogrn_country_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_country_id else z.client_country_id end client_country_id,
               dup.removed
          from (select h.id hub_id, prtl.countryid, ent.country_key, ent.client_country_id,
                       (select t.id
                          from crs_h_client_country t
                         where t.key = to_char(prtl.countryid)) new_client_country_id,
                       case when prtl.countryid is not null then 1 else 0 end row_has_value,
                       case when ent.client_country_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_ogrn_t prtl join crs_h_client_ogrn h on h.key = to_char(prtl.cl_global_prid)
                                                  left join ( select ent.client_country_id, ent.client_ogrn_id, h.key country_key
                                                                from (select ent.*, row_number() over (partition by ent.client_ogrn_id, ent.client_country_id order by ent.ldts desc) rn
                                                                        from crs_l_client_ogrn_country ent) ent join crs_h_client_country h on h.id = ent.client_country_id
                                                                       where ent.rn = 1
                                                                         and ent.removed = 0) ent on ent.client_ogrn_id = h.id
                 where 0 = decode(prtl.countryid, ent.country_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        --insert remove row
        insert into crs_s_client_ogrn(id, h_id, digest, ldts, removed, reg_num)
        select crs_s_client_ogrn_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1, s.reg_num
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_ogrn s) s join crs_h_client_ogrn h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_number(h.key) not in (select t.cl_global_prid
                                          from crs_cpi_client_ogrn t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_inn(
        p_current_run_ts  timestamp
    ) is
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_CLIENT_INN';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_cpi_client_inn_t(cl_global_prid, taxid, countryid, vtb_legalentityid, last_update)
        select cl_global_prid, taxid, countryid, vtb_legalentityid, last_update
          from crs_cpi_client_inn
         where last_update between v_previous_run_ts and p_current_run_ts;

        -- mark client for reindex
        update crs_h_client set key = key
        where key in ( select c.vtb_legalentityid
                         from crs_cpi_client_inn_t c);

        insert into crs_h_client_inn(id, key, ldts)
        select crs_h_client_inn_seq.nextval, to_char(t.cl_global_prid), p_current_run_ts
          from crs_cpi_client_inn_t t
         where not exists (select 1
                             from crs_h_client_inn t2
                            where t2.key = to_char(t.cl_global_prid));

        insert into crs_s_client_inn(id, h_id, tax_id, digest, ldts)
        select crs_s_client_inn_seq.nextval, h.id, t.taxid, DEFAULT_DIGEST, p_current_run_ts
          from crs_cpi_client_inn_t t left join crs_h_client_inn h on h.key = to_char(t.cl_global_prid)
                                      left join ( select s.*
                                                    from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                                                            from crs_s_client_inn s) s
                                                   where s.rn = 1
                                                     and s.removed = 0) s on s.h_id = h.id
         where 0 = decode(t.taxid, s.tax_id, 1, 0)
            or s.id is null;

        insert into crs_l_client_inn(id, ldts, client_inn_id, client_id, removed)
        select crs_l_client_inn_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_id else z.client_id end client_id,
               dup.removed
          from (select h.id hub_id, prtl.vtb_legalentityid, ent.client_key, ent.client_id,
                       (select t.id
                          from crs_h_client t
                         where t.key = prtl.vtb_legalentityid) new_client_id,
                       case when prtl.vtb_legalentityid is not null then 1 else 0 end row_has_value,
                       case when ent.client_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_inn_t prtl join crs_h_client_inn h on h.key = to_char(prtl.cl_global_prid)
                                                 left join ( select ent.client_id, ent.client_inn_id, h.key client_key
                                                               from (select ent.*, row_number() over (partition by ent.client_inn_id, ent.client_id order by ent.ldts desc) rn
                                                                       from crs_l_client_inn ent) ent join crs_h_client h on h.id = ent.client_id
                                                                      where ent.rn = 1
                                                                        and ent.removed = 0) ent on ent.client_inn_id = h.id
                 where 0 = decode(prtl.vtb_legalentityid, ent.client_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        insert into crs_l_client_inn_country(id, ldts, client_inn_id, client_country_id, removed)
        select crs_l_client_inn_country_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_country_id else z.client_country_id end client_country_id,
               dup.removed
          from (select h.id hub_id, prtl.countryid, ent.country_key, ent.client_country_id,
                       (select t.id
                          from crs_h_client_country t
                         where t.key = to_char(prtl.countryid)) new_client_country_id,
                       case when prtl.countryid is not null then 1 else 0 end row_has_value,
                       case when ent.client_country_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_inn_t prtl join crs_h_client_inn h on h.key = to_char(prtl.cl_global_prid)
                                                 left join ( select ent.client_country_id, ent.client_inn_id, h.key country_key
                                                               from (select ent.*, row_number() over (partition by ent.client_inn_id, ent.client_country_id order by ent.ldts desc) rn
                                                                       from crs_l_client_inn_country ent) ent join crs_h_client_country h on h.id = ent.client_country_id
                                                                      where ent.rn = 1
                                                                        and ent.removed = 0) ent on ent.client_inn_id = h.id
                 where 0 = decode(prtl.countryid, ent.country_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        --insert remove row
        insert into crs_s_client_inn(id, h_id, digest, ldts, removed, tax_id)
        select crs_s_client_inn_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1, s.tax_id
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_inn s) s join crs_h_client_inn h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_char(h.key) not in (select to_char(t.cl_global_prid)
                                        from crs_cpi_client_inn t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_segment(
        p_current_run_ts  timestamp
    ) is
        v_hub_id number;
        v_localization_hub_id crs_sys_h_localization.id%type;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_SEGMENT';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_cpi_client_segment_t (segmentid, name, name_en, diap_min, diap_max, currencyid, last_update)
        select segmentid, name, name_en, diap_min, diap_max, currencyid, last_update
          from crs_cpi_client_segment
         where last_update between v_previous_run_ts and p_current_run_ts;

        for rec in (
            select segmentid, name, name_en, diap_min, diap_max, currencyid
              from crs_cpi_client_segment_t
        ) loop
            begin
                select id into v_hub_id
                  from crs_h_client_segment
                 where key = to_char(rec.segmentid);
            exception when no_data_found then
                insert into crs_h_client_segment(id, key, ldts)
                values(crs_h_client_segment_seq.nextval, to_char(rec.segmentid), p_current_run_ts);
                v_hub_id := crs_h_client_segment_seq.currval;
            end;

            insert into crs_s_client_segment(id, h_id, revenue_min, revenue_max, digest, ldts)
            values(crs_s_client_segment_seq.nextval, v_hub_id, rec.diap_min, rec.diap_max, DEFAULT_DIGEST, p_current_run_ts);

            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_segm_name(id, client_segment_id, localization_id, ldts, removed)
            values(crs_l_client_segm_name_seq.nextval, v_hub_id, v_localization_hub_id, p_current_run_ts, 0);
        end loop;

        insert into crs_l_client_segm_curr(id, ldts, client_segment_id, client_currency_id, removed)
        select crs_l_client_segm_curr_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_currency_id else z.client_currency_id end client_currency_id,
               dup.removed
          from (select h.id hub_id, prtl.currencyid, ent.currency_key, ent.client_currency_id,
                       (select t.id
                          from crs_h_client_currency t
                         where t.key = to_char(prtl.currencyid)) new_client_currency_id,
                       case when prtl.currencyid is not null then 1 else 0 end row_has_value,
                       case when ent.client_currency_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_segment_t prtl join crs_h_client_segment h on h.key = to_char(prtl.segmentid)
                                                     left join ( select ent.client_currency_id, ent.client_segment_id, h.key currency_key
                                                                   from (select ent.*, row_number() over (partition by ent.client_segment_id, ent.client_currency_id order by ent.ldts desc) rn
                                                                           from crs_l_client_segm_curr ent) ent join crs_h_client_currency h on h.id = ent.client_currency_id
                                                                          where ent.rn = 1
                                                                            and ent.removed = 0) ent on ent.client_segment_id = h.id
                 where 0 = decode(prtl.currencyid, ent.currency_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        --insert remove row
        insert into crs_s_client_segment(id, h_id, digest, ldts, removed, revenue_min, revenue_max)
        select crs_s_client_segment_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1, s.revenue_min, s.revenue_max
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_segment s) s join crs_h_client_segment h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_number(h.key) not in (select t.segmentid
                                          from crs_cpi_client_segment t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_type_dict(
        p_current_run_ts  timestamp
    ) is
        v_hub_id              number;
        v_localization_hub_id crs_sys_h_localization.id%type;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_CLIENT_TYPE_DICT';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        for rec in (
            select client_typeid, name, name_en, priority
              from crs_cpi_client_type_dict
             where last_update between v_previous_run_ts and p_current_run_ts
        ) loop
            begin
                select id into v_hub_id
                  from crs_h_client_type
                 where key = to_char(rec.client_typeid);
            exception when no_data_found then
                insert into crs_h_client_type(id, key, ldts)
                values(crs_h_client_type_seq.nextval, to_char(rec.client_typeid), p_current_run_ts);
                v_hub_id := crs_h_client_type_seq.currval;
            end;

            insert into crs_s_client_type(id, h_id, priority, digest, ldts)
            values(crs_s_client_type_seq.nextval, v_hub_id, rec.priority, DEFAULT_DIGEST, p_current_run_ts);

            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_type_name(id, client_type_id, localization_id, ldts, removed)
            values(crs_l_client_type_name_seq.nextval, v_hub_id, v_localization_hub_id, p_current_run_ts, 0);
        end loop;

        --insert remove row
        insert into crs_s_client_type(id, h_id, digest, ldts, removed, priority)
        select crs_s_client_type_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1, s.priority
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_type s) s join crs_h_client_type h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and to_number(h.key) not in (select t.client_typeid
                                          from crs_cpi_client_type_dict t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_type(
        p_current_run_ts  timestamp
    ) is
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_CLIENT_TYPE';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_l_client_type(id, ldts, removed, client_type_id, client_id)
        select crs_l_client_type_seq.nextval, p_current_run_ts, 0,
               (select ct.id
                  from crs_h_client_type ct
                 where ct.key = to_char(t.client_typeid)),
               (select c.id
                  from crs_h_client c
                 where c.key = t.vtb_legalentityid)
        from crs_cpi_client_type t
        where last_update between v_previous_run_ts and p_current_run_ts;

        --insert remove row
        insert into crs_l_client_type(id, ldts, removed, client_type_id, client_id)
        select crs_l_client_type_seq.nextval, (p_current_run_ts - MICROSECOND), 1, l.client_type_id, l.client_id
          from (select l.*, row_number() over(partition by l.client_type_id, l.client_id order by l.ldts desc) rn
                  from crs_l_client_type l) l join crs_h_client_type ct on ct.id = l.client_type_id
                                              join crs_h_client c on c.id = l.client_id
         where l.removed = 0
           and l.rn = 1
           and (to_number(ct.key), c.key) not in (select nvl(t.client_typeid, NOT_EMPTY_NUMBER), nvl(t.vtb_legalentityid, NOT_EMPTY_VALUE)
                                                    from crs_cpi_client_type t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_group(
        p_current_run_ts  timestamp
    ) is
        v_localization_hub_id number;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_GROUP';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_cpi_client_group_t (group_id, name, name_en, full_name, full_name_en, description, description_en, is_vtb_daughter, segmentid,
                                            industryid, countryid, last_update)
        select group_id, name, name_en, full_name, full_name_en, description, description_en, is_vtb_daughter, segmentid, industryid, countryid, last_update
          from crs_cpi_client_group
         where last_update between v_previous_run_ts and p_current_run_ts;

        -- mark records to update index
        update crs_h_client_group set key = key
         where key in (select group_id from crs_cpi_client_group_t);

        insert into crs_h_client_group(id, key, ldts)
        select crs_h_client_group_seq.nextval, t.group_id, p_current_run_ts
          from crs_cpi_client_group_t t
         where not exists (select 1
                             from crs_h_client_group t2
                            where t2.key = t.group_id);

        insert into crs_s_client_group(id, h_id, digest, ldts, vtb_daughter)
        select crs_s_client_group_seq.nextval, h.id, DEFAULT_DIGEST, p_current_run_ts, t.is_vtb_daughter
          from (select t.group_id, t.name, t.name_en, t.full_name, t.full_name_en, t.description, t.description_en, t.segmentid,
                       t.industryid, t.countryid, t.last_update,
                       case when t.is_vtb_daughter = 'T' then 1 else 0 end is_vtb_daughter
                  from crs_cpi_client_group_t t) t left join crs_h_client_group h on h.key = t.group_id
                                                   left join ( select s.*
                                                                 from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                                                                         from crs_s_client_group s) s
                                                                where s.rn = 1
                                                                  and s.removed = 0) s on s.h_id = h.id
         where s.id is null
            or 0 = decode(t.is_vtb_daughter, s.vtb_daughter, 1, 0);

        for rec in (
            select t.group_id, t.name, t.name_en, t.full_name, t.full_name_en,
                   t.description, t.description_en,
                   g.id hub_id
              from crs_cpi_client_group_t t left join crs_h_client_group g on g.key = t.group_id
        ) loop
            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_group_name(id, client_group_id, localization_id, ldts, removed)
            values(crs_l_client_group_name_seq.nextval, rec.hub_id, v_localization_hub_id, p_current_run_ts, 0);

            v_localization_hub_id := insert_string_localization(rec.full_name, rec.full_name_en, p_current_run_ts);
            insert into crs_l_client_grp_fullname(id, client_group_id, localization_id, ldts, removed)
            values(crs_l_client_grp_fullname_seq.nextval, rec.hub_id, v_localization_hub_id, p_current_run_ts, 0);

            v_localization_hub_id := insert_text_localization(rec.description, rec.description_en, p_current_run_ts);
            insert into crs_l_client_grp_dscrp(id, client_group_id, localization_id, ldts, removed)
            values(crs_l_client_grp_dscrp_seq.nextval, rec.hub_id, v_localization_hub_id, p_current_run_ts, 0);
        end loop;

        insert into crs_l_client_grp_segment(id, ldts, client_group_id, client_segment_id, removed)
        select crs_l_client_grp_segment_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_segment_id else z.client_segment_id end client_segment_id,
               dup.removed
          from (select h.id hub_id, prtl.segmentid, ent.segment_key, ent.client_segment_id,
                       (select t.id
                          from crs_h_client_segment t
                         where t.key = to_char(prtl.segmentid)) new_client_segment_id,
                       case when prtl.segmentid is not null then 1 else 0 end row_has_value,
                       case when ent.client_segment_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_group_t prtl join crs_h_client_group h on h.key = prtl.group_id
                                              left join ( select ent.client_segment_id, ent.client_group_id, h.key segment_key
                                                            from (select ent.*, row_number() over (partition by ent.client_group_id, ent.client_segment_id order by ent.ldts desc) rn
                                                                    from crs_l_client_grp_segment ent) ent join crs_h_client_segment h on h.id = ent.client_segment_id
                                                                   where ent.rn = 1
                                                                     and ent.removed = 0) ent on ent.client_group_id = h.id
                 where 0 = decode(prtl.segmentid, ent.segment_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                         union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        insert into crs_l_client_grp_industry(id, ldts, client_group_id, client_industry_id, removed)
        select crs_l_client_grp_industry_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_industry_id else z.client_industry_id end client_industry_id,
               dup.removed
          from (select h.id hub_id, prtl.industryid, ent.industry_key, ent.client_industry_id,
                       (select t.id
                          from crs_h_client_industry t
                         where t.key = to_char(prtl.industryid)) new_client_industry_id,
                       case when prtl.industryid is not null then 1 else 0 end row_has_value,
                       case when ent.client_industry_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_group_t prtl join crs_h_client_group h on h.key = prtl.group_id
                                                   left join ( select ent.client_industry_id, ent.client_group_id, h.key industry_key
                                                                 from (select ent.*, row_number() over (partition by ent.client_group_id, ent.client_industry_id order by ent.ldts desc) rn
                                                                         from crs_l_client_grp_industry ent) ent join crs_h_client_industry h on h.id = ent.client_industry_id
                                                                        where ent.rn = 1
                                                                          and ent.removed = 0) ent on ent.client_group_id = h.id
                 where 0 = decode(prtl.industryid, ent.industry_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        insert into crs_l_client_grp_country(id, ldts, client_group_id, client_country_id, removed)
        select crs_l_client_grp_country_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_country_id else z.client_country_id end client_country_id,
               dup.removed
          from (select h.id hub_id, prtl.countryid, ent.country_key, ent.client_country_id,
                       (select t.id
                          from crs_h_client_country t
                         where t.key = to_char(prtl.countryid)) new_client_country_id,
                       case when prtl.countryid is not null then 1 else 0 end row_has_value,
                       case when ent.client_country_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_group_t prtl join crs_h_client_group h on h.key = prtl.group_id
                                                   left join ( select ent.client_country_id, ent.client_group_id, h.key country_key
                                                                 from (select ent.*, row_number() over (partition by ent.client_group_id, ent.client_country_id order by ent.ldts desc) rn
                                                                         from crs_l_client_grp_country ent) ent join crs_h_client_country h on h.id = ent.client_country_id
                                                                        where ent.rn = 1
                                                                          and ent.removed = 0) ent on ent.client_group_id = h.id
                 where 0 = decode(prtl.countryid, ent.country_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        --insert remove row
        insert into crs_s_client_group(id, h_id, digest, ldts, removed, vtb_daughter)
        select crs_s_client_group_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1, s.vtb_daughter
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client_group s) s join crs_h_client_group h on h.id = s.h_id
         where s.removed = 0
           and s.rn = 1
           and h.key not in (select nvl(t.group_id, NOT_EMPTY_VALUE)
                               from crs_cpi_client_group t);

        -- mark records to update index
        update crs_h_client_group set key = key
         where key not in (select t.group_id
                             from crs_cpi_client_group t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_group_participant(
        p_current_run_ts  timestamp
    ) is
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_GROUP_PARTICIPANT';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_l_client_group(id, ldts, removed, client_group_id, client_id)
        select crs_l_client_group_seq.nextval, p_current_run_ts, 0,
               (select cg.id
                  from crs_h_client_group cg
                 where cg.key = t.group_id),
               (select c.id
                  from crs_h_client c
                 where c.key = t.vtb_legalentityid)
          from crs_cpi_group_participant t
         where last_update between v_previous_run_ts and p_current_run_ts
           and exists(select 1
                        from crs_cpi_client c
                       where c.vtb_legalentityid = t.vtb_legalentityid);

        --insert remove row
        insert into crs_l_client_group(id, ldts, removed, client_id, client_group_id)
        select crs_l_client_group_seq.nextval, (p_current_run_ts - MICROSECOND), 1, l.client_id, l.client_group_id
          from (select l.*, row_number() over(partition by l.client_id, l.client_group_id order by l.ldts desc) rn
                  from crs_l_client_group l) l join crs_h_client c on c.id = l.client_id
                                               join crs_h_client_group cg on cg.id = l.client_group_id
         where l.removed = 0
           and l.rn = 1
           and (c.key, cg.key) not in (select nvl(t.vtb_legalentityid, NOT_EMPTY_VALUE),
                                              nvl(t.group_id, NOT_EMPTY_VALUE)
                                         from crs_cpi_group_participant t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client(
        p_current_run_ts  timestamp
    ) is
        v_localization_hub_id number;
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_CLIENT';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_cpi_client_t(vtb_legalentityid,
                                     name, name_en,
                                     full_name, full_name_en,
                                     opfid, countryid, categoryid, segmentid, industryid, last_update)
        select c.vtb_legalentityid,
               c.name, c.name_en,
               c.full_name, c.full_name_en,
               c.opfid, c.countryid, c.categoryid, c.segmentid, c.industryid, c.last_update
          from crs_cpi_client c
         where c.last_update between v_previous_run_ts and p_current_run_ts;

        -- mark client for reindex
        update crs_h_client set key = key
        where key in ( select c.vtb_legalentityid
                         from crs_cpi_client_t c);

        insert into crs_h_client(id, key, ldts)
        select crs_h_client_seq.nextval, t.vtb_legalentityid, p_current_run_ts
          from crs_cpi_client_t t
         where not exists (select 1
                             from crs_h_client t2
                            where t2.key = t.vtb_legalentityid);

        insert into crs_s_client(id, h_id, digest, ldts)
        select crs_s_client_seq.nextval, h.id, DEFAULT_DIGEST, p_current_run_ts
          from crs_cpi_client_t t left join crs_h_client h on h.key = t.vtb_legalentityid
                                  left join ( select s.*
                                                from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                                                        from crs_s_client s) s
                                               where s.rn = 1
                                                 and s.removed = 0) s on s.h_id = h.id
         where s.id is null;

        for rec in (select c.vtb_legalentityid,
                           c.name, c.name_en,
                           c.full_name, c.full_name_en,
                           cl.id hub_id
                      from crs_cpi_client_t c left join crs_h_client cl on cl.key = c.vtb_legalentityid)
        loop
            v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
            insert into crs_l_client_name(id, ldts, removed, client_id, localization_id)
            values(crs_l_client_name_seq.nextval, p_current_run_ts, 0, rec.hub_id, v_localization_hub_id);

            v_localization_hub_id := insert_string_localization(rec.full_name, rec.full_name_en, p_current_run_ts);
            insert into crs_l_client_full_name(id, ldts, removed, client_id, localization_id)
            values(crs_l_client_full_name_seq.nextval, p_current_run_ts, 0, rec.hub_id, v_localization_hub_id);
        end loop;

        insert into crs_l_client_opf(id, ldts, client_id, client_opf_id, removed)
        select crs_l_client_opf_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_opf_id else z.client_opf_id end client_opf_id,
               dup.removed
          from (select h.id hub_id, prtl.opfid, ent.opf_key, ent.client_opf_id,
                       (select t.id
                          from crs_h_client_opf t
                         where t.key = to_char(prtl.opfid)) new_client_opf_id,
                       case when prtl.opfid is not null then 1 else 0 end row_has_value,
                       case when ent.client_opf_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_t prtl join crs_h_client h on h.key = prtl.vtb_legalentityid
                                             left join ( select ent.client_opf_id, ent.client_id, h.key opf_key
                                                           from (select ent.*, row_number() over (partition by ent.client_id, ent.client_opf_id order by ent.ldts desc) rn
                                                                   from crs_l_client_opf ent) ent join crs_h_client_opf h on h.id = ent.client_opf_id
                                                                  where ent.rn = 1
                                                                    and ent.removed = 0) ent on ent.client_id = h.id
                 where 0 = decode(prtl.opfid, ent.opf_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        insert into crs_l_client_country(id, ldts, client_id, client_country_id, removed)
        select crs_l_client_country_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_country_id else z.client_country_id end client_country_id,
               dup.removed
          from (select h.id hub_id, prtl.countryid, ent.country_key, ent.client_country_id,
                       (select t.id
                          from crs_h_client_country t
                         where t.key = to_char(prtl.countryid)) new_client_country_id,
                       case when prtl.countryid is not null then 1 else 0 end row_has_value,
                       case when ent.client_country_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_t prtl join crs_h_client h on h.key = prtl.vtb_legalentityid
                                             left join ( select ent.client_country_id, ent.client_id, h.key country_key
                                                           from (select ent.*, row_number() over (partition by ent.client_id, ent.client_country_id order by ent.ldts desc) rn
                                                                   from crs_l_client_country ent) ent join crs_h_client_country h on h.id = ent.client_country_id
                                                                  where ent.rn = 1
                                                                    and ent.removed = 0) ent on ent.client_id = h.id
                 where 0 = decode(prtl.countryid, ent.country_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        insert into crs_l_client_category(id, ldts, client_id, client_category_id, removed)
        select crs_l_client_category_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_category_id else z.client_category_id end client_category_id,
               dup.removed
          from (select h.id hub_id, prtl.categoryid, ent.category_key, ent.client_category_id,
                       (select t.id
                          from crs_h_client_category t
                         where t.key = to_char(prtl.categoryid)) new_client_category_id,
                       case when prtl.categoryid is not null then 1 else 0 end row_has_value,
                       case when ent.client_category_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_t prtl join crs_h_client h on h.key = prtl.vtb_legalentityid
                                             left join ( select ent.client_category_id, ent.client_id, h.key category_key
                                                           from (select ent.*, row_number() over (partition by ent.client_id, ent.client_category_id order by ent.ldts desc) rn
                                                                   from crs_l_client_category ent) ent join crs_h_client_category h on h.id = ent.client_category_id
                                                                  where ent.rn = 1
                                                                    and ent.removed = 0) ent on ent.client_id = h.id
                 where 0 = decode(prtl.categoryid, ent.category_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        insert into crs_l_client_segment(id, ldts, client_id, client_segment_id, removed)
        select crs_l_client_segment_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_segment_id else z.client_segment_id end client_segment_id,
               dup.removed
          from (select h.id hub_id, prtl.segmentid, ent.segment_key, ent.client_segment_id,
                       (select t.id
                          from crs_h_client_segment t
                         where t.key = to_char(prtl.segmentid)) new_client_segment_id,
                       case when prtl.segmentid is not null then 1 else 0 end row_has_value,
                       case when ent.client_segment_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_t prtl join crs_h_client h on h.key = prtl.vtb_legalentityid
                                             left join ( select ent.client_segment_id, ent.client_id, h.key segment_key
                                                           from (select ent.*, row_number() over (partition by ent.client_id, ent.client_segment_id order by ent.ldts desc) rn
                                                                   from crs_l_client_segment ent) ent join crs_h_client_segment h on h.id = ent.client_segment_id
                                                                  where ent.rn = 1
                                                                    and ent.removed = 0) ent on ent.client_id = h.id
                 where 0 = decode(prtl.segmentid, ent.segment_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        insert into crs_l_client_industry(id, ldts, client_id, client_industry_id, removed)
        select crs_l_client_industry_seq.nextval, run_ts, z.hub_id,
               case when dup.row_has_value = 1 then z.new_client_industry_id else z.client_industry_id end client_industry_id,
               dup.removed
          from (select h.id hub_id, prtl.industryid, ent.industry_key, ent.client_industry_id,
                       (select t.id
                          from crs_h_client_industry t
                         where t.key = to_char(prtl.industryid)) new_client_industry_id,
                       case when prtl.industryid is not null then 1 else 0 end row_has_value,
                       case when ent.client_industry_id is not null then 1 else 0 end remove_prev_row
                  from crs_cpi_client_t prtl join crs_h_client h on h.key = prtl.vtb_legalentityid
                                             left join ( select ent.client_industry_id, ent.client_id, h.key industry_key
                                                           from (select ent.*, row_number() over (partition by ent.client_id, ent.client_industry_id order by ent.ldts desc) rn
                                                                   from crs_l_client_industry ent) ent join crs_h_client_industry h on h.id = ent.client_industry_id
                                                                  where ent.rn = 1
                                                                    and ent.removed = 0) ent on ent.client_id = h.id
                 where 0 = decode(prtl.industryid, ent.industry_key, 1, 0)
               ) z join (select 1 row_has_value, null remove_prev_row, 0 removed, p_current_run_ts run_ts from dual
                          union all
                         select 0 row_has_value, 1 remove_prev_row, 1 removed, (p_current_run_ts - MICROSECOND) run_ts from dual
                        ) dup on z.row_has_value = dup.row_has_value
                              or z.remove_prev_row = dup.remove_prev_row;

        --insert not remove row
        insert into crs_s_client(id, h_id, digest, ldts, removed)
        select crs_s_client_seq.nextval, s.h_id, DEFAULT_DIGEST, p_current_run_ts, 0
          from (select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client s) s join crs_h_client h on h.id = s.h_id
         where s.removed = 1
           and s.rn = 1
           and h.key in (select c.vtb_legalentityid
                           from crs_cpi_client c
                          where c.last_update between v_previous_run_ts and p_current_run_ts)
           and h.key not in (select nvl(t.vtb_legalentityid, NOT_EMPTY_VALUE)
                               from crs_cpi_client_deleted t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_deleted(
        p_current_run_ts  timestamp
    ) is
        v_previous_run_ts timestamp;
        v_portal_view_name varchar2(30) := 'V_CRS_VLE_DELETED';
    begin
        v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

        insert into crs_s_client (id, h_id, ldts, digest, removed)
        select crs_s_client_seq.nextval, z.h_id, p_current_run_ts, z.digest, 1
          from (select s.*,
                       row_number() over(partition by s.h_id order by s.ldts desc) rn
                  from crs_s_client s join crs_h_client h on s.h_id = h.id
                 where h.key in ( select c.vtb_legalentityid
                                    from crs_cpi_client_deleted c
                                   where c.del_date between v_previous_run_ts and p_current_run_ts)
               ) z
         where z.removed = 0
           and z.rn = 1;

        -- mark client for reindex
        update crs_h_client set key = key
        where key in ( select t.vtb_legalentityid
                         from crs_cpi_client_deleted t);

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_department(
        p_current_run_ts  timestamp
    ) is
        v_portal_view_name varchar2(30) := 'V_CRS_LOCK';
    begin
        insert into crs_cpi_department_t(departmentid, dep_name, dep_name_en, dep_full_name, dep_full_name_en, parent_departmentid, last_update)
        select departmentid, dep_name, dep_name_en, dep_full_name, dep_full_name_en, parent_departmentid, last_update
          from crs_cpi_department;

        insert into crs_cpi_department_name_t(departmentid, crs_departmentid)
        select m.cp_department_id, m.crs_department_id
          from crs_cpi_department_t t
          join crs_cpi_dep_mapping m on m.cp_department_id = t.departmentid;

        insert into crs_cpi_department_name_t(departmentid, crs_departmentid)
        select t.departmentid, t2.h_id
          from (select t.departmentid, trim(upper(dep_name)) dep_name, trim(upper(dep_name_en)) dep_name_en,
                       trim(upper(dep_full_name)) dep_full_name, trim(upper(dep_full_name_en)) dep_full_name_en
                  from crs_cpi_department_t t left join crs_cpi_dep_mapping m on m.cp_department_id = t.departmentid
                 where m.crs_department_id is null) t join (select s.h_id, trim(upper(lc.string_ru)) short_ru, trim(upper(lc.string_en)) short_en,
                                                                   trim(upper(lc2.string_ru)) full_ru, trim(upper(lc2.string_en)) full_en
                                                              from (select s.h_id
                                                                      from (select s.h_id, s.removed, row_number() over(partition by s.h_id order by s.ldts desc) rn
                                                                              from crs_s_department s) s
                                                                      where s.rn = 1
                                                                        and s.removed = 0
                                                                   ) s join (select l.department_id, l.localization_id
                                                                              from (select l.*, row_number() over(partition by l.department_id order by l.ldts desc) rn
                                                                                      from crs_l_department_name l ) l
                                                                             where l.rn = 1
                                                                               and l.removed = 0
                                                                            ) l on l.department_id = s.h_id
                                                                       join (select l2.department_id, l2.localization_id
                                                                               from (select l2.*, row_number() over(partition by l2.department_id order by l2.ldts desc) rn
                                                                                       from crs_l_department_fullname l2 ) l2
                                                                              where l2.rn = 1
                                                                                and l2.removed = 0
                                                                            ) l2 on l2.department_id = s.h_id
                                                                       join (select lc.h_id, lc.string_ru, lc.string_en
                                                                               from (select lc.*, row_number() over(partition by lc.h_id order by lc.ldts desc) rn
                                                                                       from crs_sys_s_localization lc ) lc
                                                                              where lc.rn = 1
                                                                                and lc.removed = 0
                                                                            ) lc on lc.h_id = l.localization_id
                                                                       join (select lc2.h_id, lc2.string_ru, lc2.string_en
                                                                               from (select lc2.*, row_number() over(partition by lc2.h_id order by lc2.ldts desc) rn
                                                                                       from crs_sys_s_localization lc2 ) lc2
                                                                              where lc2.rn = 1
                                                                                and lc2.removed = 0
                                                                            ) lc2 on lc2.h_id = l2.localization_id
                                                           ) t2 on t.dep_name in (t2.short_ru, t2.short_en, t2.full_ru, t2.full_en)
                                                                or t.dep_name_en in (t2.short_ru, t2.short_en, t2.full_ru, t2.full_en)
                                                                or t.dep_full_name in (t2.short_ru, t2.short_en, t2.full_ru, t2.full_en)
                                                                or t.dep_full_name_en in (t2.short_ru, t2.short_en, t2.full_ru, t2.full_en);

        insert into crs_cpi_client_department_t(vtb_legalentityid, departmentid, lock_typeid, last_update)
        select t.vtb_legalentityid, t.departmentid, t.lock_typeid, t.last_update
          from crs_cpi_client_department t
         where t.departmentid in (select t2.departmentid
                                    from crs_cpi_department_name_t t2
                                   where t.departmentid = t2.departmentid);

        --insert not removed link
        insert into crs_cpi_l_client_department_t (id, ldts, removed, client_id, department_id)
        select crs_l_client_department_seq.nextval, p_current_run_ts, 0, c.id, t2.crs_departmentid
          from crs_cpi_client_department_t t join crs_cpi_department_name_t t2 on t.departmentid = t2.departmentid
                                             left join crs_h_client c on c.key = to_char(t.vtb_legalentityid)
                                             left join (select l.client_id, l.department_id
                                                          from ( select l.client_id, l.department_id, l.removed,
                                                                       row_number() over(partition by l.client_id, l.department_id order by l.ldts desc) rn
                                                                  from crs_l_client_department l) l
                                                        where l.removed = 0
                                                          and l.rn = 1) l on c.id = l.client_id
                                                                         and t2.crs_departmentid = l.department_id
         where l.client_id is null;

        --insert removed link if row absent in client portal
        insert into crs_cpi_l_client_department_t(id, ldts, removed, client_id, department_id)
        select crs_l_client_department_seq.nextval, (p_current_run_ts - MICROSECOND), 1, l.client_id, l.department_id
          from crs_cpi_client_department_t t join crs_cpi_department_name_t t2 on t.departmentid = t2.departmentid
                                        left join crs_h_client c on c.key = to_char(t.vtb_legalentityid)
                                        right join (select l.client_id, l.department_id
                                                      from ( select l.client_id, l.department_id, l.removed,
                                                                    row_number() over(partition by l.client_id, l.department_id order by l.ldts desc) rn
                                                               from crs_l_client_department l) l
                                                     where l.removed = 0
                                                       and l.rn = 1) l on c.id = l.client_id
                                                                      and t2.crs_departmentid = l.department_id
         where l.client_id is not null
           and t.departmentid is null;

        -- mark calculations to renew security tag
        update crs_h_calc set virtual_secure_tag = null
         where id in (select lcc.calc_id
                        from crs_l_calc_client lcc join crs_cpi_l_client_department_t t on lcc.calc_id = t.client_id);

        insert into crs_l_client_department(id, ldts, removed, client_id, department_id)
        select id, ldts, removed, client_id, department_id
          from crs_cpi_l_client_department_t;

        commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end;

    procedure sync_client_cp_department(
      p_current_run_ts timestamp
    ) is
      v_localization_hub_id number;
      v_previous_run_ts     timestamp;
      v_portal_view_name    varchar2(30) := 'V_CRS_DEPARTMENT';
    begin
      v_previous_run_ts := get_previous_run_ts(v_portal_view_name);

      insert into crs_cpi_department_t(departmentid, dep_name, dep_name_en, dep_full_name, dep_full_name_en, parent_departmentid, last_update)
      select departmentid, dep_name, dep_name_en, dep_full_name, dep_full_name_en, parent_departmentid, last_update
        from crs_cpi_department
       where last_update between v_previous_run_ts and p_current_run_ts;

      insert into crs_h_client_department (id, key, ldts)
      select crs_h_client_department_seq.nextval, t.departmentid, p_current_run_ts
        from crs_cpi_department_t t
       where not exists (
         select 1
           from crs_h_client_department t2
          where t2.key = t.departmentid
        )
      ;

      insert into crs_s_client_department(id, h_id, digest, ldts)
      select crs_s_client_department_seq.nextval, h.id, DEFAULT_DIGEST, p_current_run_ts
        from crs_cpi_department_t t
        join crs_h_client_department h on h.key = t.departmentid
        left join (
          select s.*
            from (
              select s.*, row_number() over(partition by s.h_id order by s.ldts desc) rn
                from crs_s_client_department s
            ) s
           where s.rn = 1
             and s.removed = 0
        ) s on s.h_id = h.id
       where s.id is null;

      for rec in (
        select t.departmentid, t.h_id as hub_id, dep_name as name, dep_name_en as name_en, dep_full_name as full_name, dep_full_name_en as full_name_en,
               decode(dep_name, short_ru, 0, 1) + decode(dep_name_en, short_en, 0, 1) as name_chg,
               decode(dep_full_name, full_ru, 0, 1) + decode(dep_full_name_en, full_en, 0, 1) as full_name_chg
          from (
            select t.departmentid as departmentid, hd.id as h_id,
                   trim(upper(dep_name)) dep_name, trim(upper(dep_name_en)) dep_name_en,
                   trim(upper(dep_full_name)) dep_full_name, trim(upper(dep_full_name_en)) dep_full_name_en, last_update
              from crs_cpi_department_t t
              join crs_h_client_department hd on hd.key = t.departmentid
          ) t left join (
                     select s.h_id, s.removed, trim(upper(lc.string_ru)) short_ru, trim(upper(lc.string_en)) short_en,
                            trim(upper(lc2.string_ru)) full_ru, trim(upper(lc2.string_en)) full_en
                       from (
                         select s.h_id, s.removed
                           from (
                             select s.h_id, s.removed, row_number() over(partition by s.h_id order by s.ldts desc) rn
                               from crs_s_client_department s
                           ) s
                         where s.rn = 1
                           and s.removed = 0
                       ) s join (
                             select l.client_department_id, l.localization_id
                               from (
                                 select l.*, row_number() over(partition by l.client_department_id order by l.ldts desc) rn
                                   from crs_l_client_department_name l
                               ) l
                              where l.rn = 1
                                and l.removed = 0
                           ) l on l.client_department_id = s.h_id
                           join (
                             select l2.client_department_id, l2.localization_id
                               from (
                                 select l2.*, row_number() over(partition by l2.client_department_id order by l2.ldts desc) rn
                                   from crs_l_client_department_fullname l2
                               ) l2
                              where l2.rn = 1
                                and l2.removed = 0
                           ) l2 on l2.client_department_id = s.h_id
                           join (
                             select lc.h_id, lc.string_ru, lc.string_en
                               from (
                                 select lc.*, row_number() over(partition by lc.h_id order by lc.ldts desc) rn
                                   from crs_sys_s_localization lc
                               ) lc
                              where lc.rn = 1
                                and lc.removed = 0
                           ) lc on lc.h_id = l.localization_id
                           join (
                             select lc2.h_id, lc2.string_ru, lc2.string_en
                               from (
                                 select lc2.*, row_number() over(partition by lc2.h_id order by lc2.ldts desc) rn
                                   from crs_sys_s_localization lc2
                               ) lc2
                              where lc2.rn = 1
                                and lc2.removed = 0
                           ) lc2 on lc2.h_id = l2.localization_id
                   ) t2 on t2.h_id = t.h_id
      )
      loop
        if rec.name_chg > 0 then
          v_localization_hub_id := insert_string_localization(rec.name, rec.name_en, p_current_run_ts);
          insert into crs_l_client_department_name(id, ldts, removed, client_department_id, localization_id)
          values(crs_l_client_department_name_seq.nextval, p_current_run_ts, 0, rec.hub_id, v_localization_hub_id);
        end if;
        if rec.full_name_chg > 0 then
          v_localization_hub_id := insert_string_localization(rec.full_name, rec.full_name_en, p_current_run_ts);
          insert into crs_l_client_department_fullname(id, ldts, removed, client_department_id, localization_id)
          values(crs_l_client_department_fullname_seq.nextval, p_current_run_ts, 0, rec.hub_id, v_localization_hub_id);
        end if;
      end loop;

      insert into crs_s_client_department (id, h_id, digest, ldts, removed)
      select crs_s_client_department_seq.nextval, s.h_id, DEFAULT_DIGEST, (p_current_run_ts - MICROSECOND), 1
        from (
          select s.*, row_number() over (partition by s.h_id order by s.ldts desc) rn
            from crs_s_client_department s
        ) s join crs_h_client_department h on h.id = s.h_id
       where s.removed = 0
         and s.rn = 1
         and h.key not in (
           select to_char(t.departmentid)
             from crs_cpi_department t
        )
      ;

      insert into crs_s_client_department (id, h_id, digest, ldts, removed)
      select crs_s_client_department_seq.nextval, s.h_id, DEFAULT_DIGEST, p_current_run_ts, 0
        from (
          select s.*, row_number() over (partition by s.h_id order by s.ldts desc) rn
            from crs_s_client_department s
        ) s join crs_h_client_department h on h.id = s.h_id
       where s.removed = 1
         and s.rn = 1
         and h.key in (
          select d.departmentid
            from crs_cpi_department d
           where d.last_update between v_previous_run_ts and p_current_run_ts
        )
      ;

      insert into crs_cpi_client_department_t(vtb_legalentityid, departmentid, lock_typeid, last_update)
      select t.vtb_legalentityid, t.departmentid, max(t.lock_typeid), max(t.last_update)
        from crs_cpi_client_department t
       where t.departmentid in (
          select t2.departmentid
            from crs_cpi_department_t t2
           where t.departmentid = t2.departmentid
        )
      group by t.vtb_legalentityid, t.departmentid;

      insert into crs_cpi_l_client_department_t (id, ldts, removed, client_id, department_id)
      select crs_l_client_department_seq.nextval, p_current_run_ts, 0, c.id, d.id
        from crs_cpi_client_department_t t
        join crs_h_client_department d on d.key = to_char(t.departmentid)
        join crs_h_client c on c.key = to_char(t.vtb_legalentityid)
        left join (
               select l.client_id, l.client_department_id
                 from (
                   select l.client_id, l.client_department_id, l.removed, row_number() over (partition by l.client_id, l.client_department_id order by l.ldts desc) rn
                     from crs_l_client_cp_department l
                 ) l
                where l.removed = 0
                  and l.rn = 1
            ) l on c.id = l.client_id and d.id = l.client_department_id
          where l.client_id is null
      ;

      insert into crs_cpi_l_client_department_t(id, ldts, removed, client_id, department_id)
      select crs_l_client_cp_department_seq.nextval, (p_current_run_ts - MICROSECOND), 1, l.client_id, l.client_department_id
        from crs_cpi_client_department_t t
        join crs_h_client_department d on d.key = to_char(t.departmentid)
        join crs_h_client c on c.key = to_char(t.vtb_legalentityid)
       right join (
               select l.client_id, l.client_department_id
                 from (
                   select l.client_id, l.client_department_id, l.removed, row_number() over (partition by l.client_id, l.client_department_id order by l.ldts desc) rn
                     from crs_l_client_cp_department l
                 ) l
                where l.removed = 0
                  and l.rn = 1
           ) l on c.id = l.client_id and d.id = l.client_department_id
       where l.client_id is not null
         and t.departmentid is null
      ;

      insert into crs_l_client_cp_department(id, ldts, removed, client_id, client_department_id)
      select id, ldts, removed, client_id, department_id
        from crs_cpi_l_client_department_t;

      commit_and_lock(v_portal_view_name, p_current_run_ts);
    exception
        when others then log_rollback_and_lock(v_portal_view_name, p_current_run_ts);
    end sync_client_cp_department;

    procedure synchronize_data is
      v_current_run_ts timestamp := systimestamp;
    begin
        commit_and_lock(null, v_current_run_ts);

        sync_client_category(v_current_run_ts);
        sync_client_currency(v_current_run_ts);
        sync_client_country(v_current_run_ts);
        sync_client_industry(v_current_run_ts);
        sync_client_opf(v_current_run_ts);
        sync_client_segment(v_current_run_ts);
        sync_client(v_current_run_ts);
        sync_client_deleted(v_current_run_ts);
        sync_client_ogrn(v_current_run_ts);
        sync_client_inn(v_current_run_ts);
        sync_client_type_dict(v_current_run_ts);
        sync_client_type(v_current_run_ts);
        sync_client_group(v_current_run_ts);
        sync_client_group_participant(v_current_run_ts);
        sync_client_department(v_current_run_ts);
        sync_client_cp_department(v_current_run_ts);

        commit;

        ctx_ddl.sync_index('crs_h_client_group_i01');
        ctx_ddl.sync_index('crs_h_client_i01');

    end synchronize_data;
end crs_cpi_pkg;
/
