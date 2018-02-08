--liquibase formatted sql

--changeset svaliev:crs-pkg logicalFilePath:crs-pkg endDelimiter:/ runOnChange:true
create or replace package crs_pkg is
    /**
     * Package for common functions and procedures.
     */

    /**
     * Returns generated link table name.
     * @return generated link table name
     */
    function generate_link_table_name return varchar2;
end crs_pkg;
/

create or replace package body crs_pkg is
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
end crs_pkg;
/

