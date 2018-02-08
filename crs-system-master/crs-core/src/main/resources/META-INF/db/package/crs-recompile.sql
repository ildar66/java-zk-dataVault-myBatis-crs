--liquibase formatted sql

--changeset akamordin:crs-1.1.0-VTBCRS-618-recompile-schema logicalFilePath:crs-1.1.0-VTBCRS-618-recompile-schema endDelimiter:/ runOnChange:true runAlways:true
declare
    ERR_NUM constant number := -20001;
    MAX_LEN constant number := 2000;
    v_text varchar2(MAX_LEN);
begin
    dbms_utility.compile_schema(schema => USER, compile_all => false);
    for r in (
        select substr(listagg(to_char(e.sequence||'. '||e.type||' '||user||'.'||e.name||' at line '||e.line||':'||e.position||' Text: '||e.text), chr(10)) within group (order by e.sequence), 1, MAX_LEN) as text
          from user_errors e
    )
    loop
        v_text := r.text;
    end loop;
    if v_text is not null then
        raise_application_error(ERR_NUM, v_text);
    end if;
end;
/
