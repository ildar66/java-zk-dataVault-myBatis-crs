--liquibase formatted sql

--changeset pmasalov:crs-calc-secure-target-src logicalFilePath:crs-calc-secure-target-src endDelimiter:/ runOnChange:true
create or replace procedure crs_calc_secure_target_src( p_rowid in rowid, p_idx_data in out nocopy clob) is
    C_VIEW_BA constant varchar2(30) := 'VIEW';
    C_CALC_MD constant varchar2(30) := 'CALC';

    -- Secext rule
    v_calc_id number;
    v_has_client number;
    v_has_client_group number;
    v_published number;
    v_user_ids crs_number_table;

    procedure i_writeAppend(p_add varchar2) is
    begin
        dbms_lob.writeAppend( p_idx_data, length(p_add), p_add); 
    end;
    
    procedure i_proc_published_no_client(p_calc_id number, p_ids in out nocopy crs_number_table) is
    begin
        -- виден всем ролям которым разрешено действие "ПРОСМОТР"
        select /*+ result_cache cardinality(t, 1) */ user_id
          bulk collect into p_ids
          from (select h_id user_id, removed, row_number() over(partition by h_id order by ldts desc) rn 
                  from crs_s_user
                 where h_id in (select value(t)
                                  from table(crs_secure_tag_util.permited_users(C_CALC_MD, C_VIEW_BA)) t))
         where rn = 1
           and removed = 0;
    end;
        
    procedure i_proc_published_has_client(p_calc_id number, p_ids in out nocopy crs_number_table) is
    begin
        -- расчёт может видеть пользователь (USER), если в рамках назначенных ролей разрешено действие "ПРОСМОТР"
        -- и связанные клиенты расчёта (CALC#CLIENT) которых находятся в том же подразделении (DEPARTMENT) пользователя
        select /*+ result_cache cardinality(t, 1) cardinality(tt, 1) */ user_id
          bulk collect into p_ids
          from (select h_id user_id, removed, row_number() over(partition by h_id order by ldts desc) rn 
                  from crs_s_user
                 where h_id in (select value(t)
                                  from table(crs_secure_tag_util.permited_users(C_CALC_MD, C_VIEW_BA)) t
                             intersect
                                select value(tt)
                                  from table(crs_secure_tag_util.users_in_same_departments_as_client(crs_secure_tag_util.calc_client(p_calc_id))) tt))
         where removed = 0
           and rn = 1;
    end;
    
    procedure i_proc_draft(p_calc_id number, p_ids in out nocopy crs_number_table) is
    begin
        -- расчёт может видеть пользователь (USER), если в рамках назначенных ролей разрешено действие "ПРОСМОТР"
        -- и автор расчёта имеет одну из тех же ролей которые есть у пользователя и находится
        -- в том же подразделении (DEPARTMENT) пользователя или ниже по иерархии подразделений.
        select /*+ result_cache cardinality(t, 1) cardinality(tt, 1) cardinality(ttt, 1) */ user_id
          bulk collect into p_ids
          from (select h_id user_id, removed, row_number() over(partition by h_id order by ldts desc) rn 
                  from crs_s_user
                 where h_id in (select value(t)
                                  from table(crs_secure_tag_util.permited_users(C_CALC_MD, C_VIEW_BA)) t
                             intersect
                                select value(tt)
                                  from table(crs_secure_tag_util.users_in_same_and_parent_departments_as_user(crs_secure_tag_util.calc_author(p_calc_id))) tt
                             intersect
                                select value(ttt)
                                  from table(crs_secure_tag_util.users_have_same_roles_as_user(crs_secure_tag_util.calc_author(p_calc_id))) ttt))
         where removed = 0
           and rn = 1;
    end;

    function i_has_no_client return boolean is
    begin
        return v_has_client_group = 1 or (v_has_client_group = 0 and v_has_client = 0);
    end;

    function i_has_client return boolean is
    begin
        return not i_has_no_client;
    end;
begin
    begin
          with c as (select calc_id
                       from (select calc_id, client_id, removed, row_number() over(partition by calc_id, client_id order by ldts desc) rn
                               from crs_l_calc_client) lcc join (select h_id, removed, row_number() over(partition by h_id order by ldts desc) rn
                                                                   from crs_s_client) sc on sc.h_id = lcc.client_id
                      where lcc.removed = 0
                        and lcc.rn = 1
                        and sc.removed = 0
                        and sc.rn = 1),
               cg as (select calc_id
                        from (select calc_id, client_group_id, removed, row_number() over(partition by calc_id, client_group_id order by ldts desc) rn
                                from crs_l_calc_client_group) lccg join (select h_id, removed, row_number() over(partition by h_id order by ldts desc) rn
                                                                          from crs_s_client_group) scg on scg.h_id = lccg.client_group_id
                       where lccg.removed = 0
                         and lccg.rn = 1
                         and scg.removed = 0
                         and scg.rn = 1)
        select h.id,
               case when exists(select 1 from c where c.calc_id = h_id) then 1 else 0 end has_client,
               case when exists(select 1 from cg where cg.calc_id = h_id) then 1 else 0 end has_client_group,
               s.published
          into v_calc_id, v_has_client, v_has_client_group, v_published
          from crs_h_calc h join (select published,
                                         h_id,
                                         row_number() over(partition by h_id order by ldts desc) rn
                                    from crs_s_calc) s on h.id = s.h_id and s.rn = 1
         where h.rowid = p_rowid;
    exception when no_data_found then
        return;
    end;
  
    case
        when v_published = 1 and i_has_no_client then
            -- published, no client
            i_proc_published_no_client( v_calc_id, v_user_ids);
        when v_published = 1 and i_has_client then
            -- published has client  
            i_proc_published_has_client( v_calc_id, v_user_ids);
        when v_published = 0 then
            -- not published
            i_proc_draft( v_calc_id, v_user_ids);
        else
            return;
    end case;      
    
    crs_secure_tag_util.write_taged_ids(p_tag => 'U', p_Ids => v_user_ids, p_clob_data => p_idx_data);

    -- unconditional access to calcs
    v_user_ids := crs_secure_tag_util.permited_users(p_entity_meta_key => C_CALC_MD, p_business_action_key => C_VIEW_BA, p_cancel_secext_rule => 1);
    crs_secure_tag_util.write_taged_ids(p_tag => 'U', p_Ids => v_user_ids, p_clob_data => p_idx_data);
end crs_calc_secure_target_src;
/
