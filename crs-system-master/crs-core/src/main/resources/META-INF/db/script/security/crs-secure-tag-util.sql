--liquibase formatted sql

--changeset pmasalov:crs-secure-tag-util logicalFilePath:crs-secure-tag-util endDelimiter:/ runOnChange:true
create or replace package crs_secure_tag_util is
    /**
    * package:crs_secure_tag_util. Useful functions to create secure tags
    *
    * %usage
    * Technical usage information, feature or implementation and usage limits
    * 
    * %author PMASALOV
    * %created 30/08/2017 13:27:13
    * %version $Id$
    * %history
    * Date       By            Modification (TR#, essense, JIRA etc)              <BR>
    * ---------- ------------- -------------------------------------------------- <BR>
    * 30/08/2017                                                                  <BR>
    */

    --****************************************************************************
    --{%skip} Public function and procedure declarations
    --****************************************************************************

    function permited_users(p_entity_meta_key varchar2, p_business_action_key varchar2, p_cancel_secext_rule number default 0) return crs_number_table result_cache;
    function calc_author(p_calc_id number) return number deterministic;
    function calc_client(p_calc_id number) return number deterministic;
    function users_have_same_roles_as_user(p_user_id number) return crs_number_table result_cache;
    function users_in_same_and_parent_departments_as_user(p_user_id number) return crs_number_table result_cache;
    function users_in_same_departments_as_client(p_client_id number) return crs_number_table result_cache;
            
    procedure write_taged_ids(p_tag varchar2, p_Ids crs_number_table, p_clob_data in out nocopy clob);

end crs_secure_tag_util;
/
create or replace package body crs_secure_tag_util is

    function permited_users(p_entity_meta_key varchar2, p_business_action_key varchar2, p_cancel_secext_rule number default 0) return crs_number_table result_cache
    is
        v_user_ids crs_number_table;
    begin
        with lpba as (select permission_id
                      from (select permission_id, removed, row_number() over(partition by permission_id, business_action_id order by ldts desc) rn
                              from crs_l_permission_business_action lpba
                             where business_action_id in (select id
                                                            from crs_sys_h_business_action bh
                                                           where bh.key = p_business_action_key))
                     where rn = 1
                       and removed = 0),
           lpe as (select permission_id
                     from (select permission_id, removed, row_number() over(partition by permission_id, entity_id order by ldts desc) rn
                             from crs_l_permission_entity lpe
                            where entity_id in (select id
                                                  from crs_sys_h_entity he
                                                 where he.key = p_entity_meta_key))
                    where rn = 1
                      and removed = 0),
           sp as (select permission_id
                    from (select h_id permission_id, permit, cancel_secext_rule, removed, row_number() over(partition by h_id order by ldts desc) rn
                            from crs_s_permission
                           where h_id in (select permission_id
                                            from lpba)
                             and h_id in (select permission_id
                                            from lpe))
                   where rn = 1
                     and removed = 0
                     and permit = 1
                     and cancel_secext_rule = p_cancel_secext_rule),
           lpr as (select role_id
                     from (select role_id, removed, row_number() over(partition by permission_id, role_id order by ldts desc) rn
                             from crs_l_permission_role lpr
                            where permission_id in (select permission_id
                                                      from sp))
                    where removed = 0
                      and rn = 1),
           sr as (select role_id
                    from (select h_id role_id, removed, row_number() over(partition by h_id order by ldts desc) rn
                            from crs_s_role sr
                           where h_id in (select role_id
                                            from lpr))
                   where removed = 0
                     and rn = 1),
            lur as (select user_id
                     from (select user_id, removed, row_number() over(partition by user_id, role_id order by ldts desc) rn
                             from crs_l_user_role join 
                            where role_id in (select role_id
                                                from sr))
                    where removed = 0
                      and rn = 1)
        select user_id bulk collect into v_user_ids from lur;
        return v_user_ids;
    end;
    
    
    function calc_author(p_calc_id number) return number deterministic
    is
        v_user_ids crs_number_table;
    begin
        with
            alcu as (select user_id
                      from (select user_id, removed, row_number() over(partition by user_id, calc_id order by ldts desc) rn
                              from crs_l_calc_user
                             where calc_id = p_calc_id)
                     where rn = 1
                       and removed = 0)
        select /*+ result_cache */ user_id bulk collect into v_user_ids from alcu;
        
        return case when v_user_ids.count > 0 then v_user_ids(1) end;
    end;
        

    function calc_client(p_calc_id number) return number deterministic
    is
        v_client_ids crs_number_table;
    begin
        with
           lcc as (select client_id
                     from (select client_id, removed, row_number() over(partition by client_id, calc_id order by ldts desc) rn
                             from crs_l_calc_client
                            where calc_id = p_calc_id)
                    where removed = 0
                      and rn = 1),
           sc as (select client_id
                    from (select h_id client_id, removed, row_number() over(partition by h_id order by ldts desc) rn
                            from crs_s_client
                           where h_id in (select client_id
                                            from lcc))
                   where removed = 0
                     and rn = 1)
        select /*+ result_cache */ client_id bulk collect into v_client_ids from sc;
        
        return case when v_client_ids.count > 0 then v_client_ids(1) end;
    end;


    function users_have_same_roles_as_user(p_user_id number) return crs_number_table result_cache
    is
        v_user_ids crs_number_table;
    begin
        with
           alur as (select role_id
                      from (select role_id, removed, row_number() over(partition by role_id, user_id order by ldts desc) rn
                              from crs_l_user_role
                             where user_id = p_user_id)
                     where removed = 0
                       and rn = 1),
           asr as (select role_id
                     from (select h_id role_id, removed, row_number() over(partition by h_id order by ldts desc) rn
                             from crs_s_role
                            where h_id in (select role_id
                                             from alur))
                    where removed = 0
                      and rn = 1),
           aalur as (select user_id
                       from (select user_id, removed, row_number() over(partition by user_id, role_id order by ldts desc) rn
                               from crs_l_user_role
                              where role_id in (select role_id
                                                  from asr))
                      where removed = 0
                        and rn = 1)
        select user_id bulk collect into v_user_ids from aalur;
        return v_user_ids;
    end;
 
   
    function users_in_same_and_parent_departments_as_user(p_user_id number) return crs_number_table result_cache
    is
        v_user_ids crs_number_table;
    begin
        with
           d as (select department_id
                   from (select department_id, removed, row_number() over(partition by department_id, user_id order by ldts desc) rn
                           from crs_l_user_department
                          where user_id = p_user_id)
                  where removed = 0
                    and rn = 1),
           dt as (select hd.id, ld.department_p_id parent_id
                    from (select h_id id, removed, row_number() over(partition by h_id order by ldts desc) rn
                            from crs_s_department) hd left outer join (select department_id, department_p_id
                                                                         from (select department_id, department_p_id, removed, row_number() over(partition by department_id, department_p_id order by ldts desc) rn
                                                                                 from crs_l_department)
                                                                        where removed = 0
                                                                          and rn = 1) ld on ld.department_id = hd.id
                   where hd.removed = 0 and hd.rn = 1),
           dl as (select dt.id department_id, dt.parent_id, level
                    from dt
                 connect by prior parent_id = id
                   start with id in (select department_id
                                       from d)),
           lud as (select user_id
                     from (select user_id, removed, row_number() over(partition by user_id, department_id order by ldts desc) rn
                             from crs_l_user_department
                            where department_id in (select department_id
                                                      from dl))
                    where removed = 0
                      and rn = 1)
        select user_id bulk collect into v_user_ids from lud;
        return v_user_ids;
    end;


    function users_in_same_departments_as_client(p_client_id number) return crs_number_table result_cache
    is
        v_user_ids crs_number_table;
    begin
        with
           lcd as (select department_id
                     from (select department_id, removed, row_number() over(partition by client_id, department_id order by ldts desc) rn
                             from crs_l_client_department
                            where client_id = p_client_id)
                    where removed = 0
                      and rn = 1),
           dl as (select department_id
                    from (select h_id department_id, removed, row_number() over(partition by h_id order by ldts desc) rn
                            from crs_s_department
                           where h_id in (select department_id
                                            from lcd))
                   where removed = 0
                     and rn = 1),
           lud as (select user_id
                     from (select user_id, removed, row_number() over(partition by user_id, department_id order by ldts desc) rn
                             from crs_l_user_department
                            where department_id in (select department_id
                                                      from dl))
                    where removed = 0
                      and rn = 1)
        select user_id bulk collect into v_user_ids from lud;
        return v_user_ids;
    end;


    procedure write_taged_ids(p_tag varchar2, p_Ids crs_number_table, p_clob_data in out nocopy clob)
    is
        procedure i_writeAppend(p_add varchar2) is
        begin
            dbms_lob.writeAppend(p_clob_data, length(p_add), p_add); 
        end;

    begin
        if not p_Ids is null and p_Ids.count > 0 then
            for i in 1 .. p_Ids.count loop
                i_writeAppend('<'||p_tag||'>' || p_Ids(i) || '</'||p_tag||'>');
            end loop;
        end if;
    end;
    
end crs_secure_tag_util;
/
