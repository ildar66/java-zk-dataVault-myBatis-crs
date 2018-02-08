--liquibase formatted sql

--changeset pmasalov:crs-client-g-key-data-vc logicalFilePath:crs-1.0-crs-client-g-key-data-vc endDelimiter:\n/\n runOnChange:true
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

    for r in (with l as (
                 select linked_hub_id from (select l.localization_id linked_hub_id,
                            l.removed,
                            row_number() over(partition by l.client_group_id, l.localization_id order by l.ldts desc) rn
                       from crs_l_client_group_name l
                      where l.client_group_id = v_h_id
                        and l.ldts <= v_ldts) where rn = 1 and removed = 0
              )
              select string_ru, string_en
                from (select s.h_id,
                               s.string_ru, string_en,
                               s.removed,
                               row_number() over(partition by s.h_id order by s.ldts desc) rn
                          from crs_sys_s_localization s
                         where s.ldts <= v_ldts
                           and s.h_id in (select linked_hub_id from l)) c
                  where c.rn = 1
                    and c.removed = 0)
    loop
        v_element := 'NAME_RU:' || r.string_ru || ':NAME_RU';
        i_writeAppend(v_element||' ');
        v_element := 'NAME_EN:' || r.string_en || ':NAME_EN';
        i_writeAppend(v_element||' ');
    end loop;

end crs_client_g_key_data_vc;
/