--liquibase formatted sql

--changeset akirilchev:crs-byte-to-char-converter logicalFilePath:crs-byte-to-char-converter endDelimiter:/ runOnChange:true
alter table crs_s_user modify full_name generated always as ('VTBCRS-424-temp-value')
/
alter table crs_sys_s_attribute modify link generated always as ('VTBCRS-424-temp-value')
/
declare
  v_sql varchar2(4000);
begin
  dbms_output.enable(null);
  for r in (select t.table_name,
                   t.column_name,
                   t.data_length,
                   t.data_type
              from user_tab_cols t join user_tables tab on tab.table_name = t.table_name
             where t.data_type in ('VARCHAR2', 'CHAR')
               and t.virtual_column = 'NO'
               and t.table_name like 'CRS%'
               and t.char_used = 'B')
  loop
    v_sql := 'alter table "' || r.table_name || '" modify "' || r.column_name || '" ' || r.data_type || '(' || r.data_length || ' char)';
    begin
        execute immediate v_sql;
    exception
        when others then
            dbms_output.put_line('error: ' || v_sql || ' . Reason: '|| sqlerrm);
    end;
  end loop;
end;
/
alter table crs_s_user modify full_name generated always as (rtrim(((case when surname is not null then surname ||' ' end) || (case when name is not null then name ||' ' end )) || patronymic)) virtual
/
alter table crs_sys_s_attribute modify link as (nvl2(link_table, 1, 0))
/