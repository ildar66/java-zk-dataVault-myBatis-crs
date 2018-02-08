declare
    v_id number;
    v_local_id number;
    v_child_id number;
    v_file_id number;
    v_link_table varchar2(50);
    v_sql varchar2(2000);
    v_systimestamp timestamp := date '2017-01-01';

begin
    insert into CRS_H_PERSIST_TEST (ID, KEY, LDTS)
    values (CRS_H_PERSIST_TEST_SEQ.NEXTVAL, 'T0', v_systimestamp) returning id into v_id;

    insert into CRS_S_PERSIST_TEST (ID, H_ID, LDTS, REMOVED, DIGEST, PERSIST_TEST_STRING, PERSIST_TEST_NUMBER, PERSIST_TEST_BOOLEAN, PERSIST_TEST_TEXT, "DATE", PERSIST_TEST_DATETIME)
    values (CRS_S_PERSIST_TEST_SEQ.NEXTVAL, v_id, v_systimestamp, 0, 'testdigest', 'test string', 1, 0, 'test text', date '2009-09-09', to_date('2009-09-09 09:09:09','YYYY-MM-DD HH24:MI:SS'));

    insert into crs_sys_h_localization (id,key, ldts) values (crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.nextval, v_systimestamp) returning id into v_local_id;
    insert into crs_sys_s_localization (id,h_id,string_ru,string_en, ldts, digest) values (crs_sys_s_localization_seq.nextval, v_local_id, 'тест строка ru','test string en',v_systimestamp, 'n');
    select s.link_table into v_link_table from crs_sys_h_attribute h join crs_sys_s_attribute s on s.h_id = h.id and h.key = 'PERSIST_TEST#STRINGML';
    v_sql := 'insert into '||v_link_table||' (id, PERSIST_TEST_ID, LOCALIZATION_ID,ldts,removed) values ('||v_link_table||'_seq.nextval,:id, :lid,:v_systimestamp,0)';
    --dbms_output.put_line(v_sql);
    execute immediate v_sql using v_id, v_local_id, v_systimestamp;

    insert into crs_sys_h_localization (id,key,ldts) values (crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.nextval,v_systimestamp) returning id into v_local_id;
    insert into crs_sys_s_localization (id,h_id,text_ru,text_en,ldts, digest) values (crs_sys_s_localization_seq.nextval, v_local_id, 'тест текст ru','test text en',v_systimestamp,'n');
    select s.link_table into v_link_table from crs_sys_h_attribute h join crs_sys_s_attribute s on s.h_id = h.id and h.key = 'PERSIST_TEST#TEXTML';
    v_sql := 'insert into '||v_link_table||' (id, PERSIST_TEST_ID, LOCALIZATION_ID,ldts,removed) values ('||v_link_table||'_seq.nextval,:id, :lid,:v_systimestamp,0)';
    --dbms_output.put_line(v_sql);
    execute immediate v_sql using v_id, v_local_id,v_systimestamp;

    insert into crs_sys_h_storage (id, key,ldts) values (crs_sys_h_storage_seq.nextval, crs_sys_h_storage_seq.nextval, v_systimestamp) returning id into v_file_id;
    insert into crs_sys_s_storage_desc (id, h_id, mime_type, name,ldts,digest) values (crs_sys_s_storage_desc_seq.nextval, v_file_id, 'text/plain', 'test-filename.txt',v_systimestamp,'n');
    insert into crs_sys_s_storage (id,h_id,data,ldts,digest) values (crs_sys_s_storage_seq.nextval, v_file_id, utl_raw.cast_to_raw('filedata'), v_systimestamp,'n');
    select s.link_table into v_link_table from crs_sys_h_attribute h join crs_sys_s_attribute s on s.h_id = h.id and h.key = 'PERSIST_TEST#FILE';
    v_sql := 'insert into '||v_link_table||' (id, PERSIST_TEST_ID, storage_id,ldts,removed) values ('||v_link_table||'_seq.nextval,:id, :lid,:v_systimestamp,0)';
    --dbms_output.put_line(v_sql);
    execute immediate v_sql using v_id, v_file_id,v_systimestamp;



    insert into CRS_H_PERSIST_TEST_CHILD (ID,KEY,LDTS) values (CRS_H_PERSIST_TEST_CHILD_SEQ.Nextval, '1.1', v_systimestamp) returning id into v_child_id;
    insert into CRS_S_PERSIST_TEST_CHILD (ID,H_ID,LDTS,REMOVED,DIGEST,
                                          PERSIST_TEST_CHILD_STRING,
                                          PERSIST_TEST_CHILD_BOOLEAN,
                                          PERSIST_TEST_CHILD_TEXT,
                                          PERSIST_TEST_CHILD_NUMBER,
                                          PERSIST_TEST_CHILD_DATE,
                                          PERSIST_TEST_CHILD_DATETIME)
    values (CRS_S_PERSIST_TEST_CHILD_SEQ.Nextval,v_child_id,v_systimestamp,0,'TESTDIGEST',
                                                 'TEST_CHILD#STRING',
                                                 1,
                                                 'TEST_CHILD#TEXT',
                                                 2,
                                                 date '2008-08-08',
                                                 to_date('2008-08-08 08:08:08','YYYY-MM-DD HH24:MI:SS'));

    select s.link_table into v_link_table from crs_sys_h_attribute h join crs_sys_s_attribute s on s.h_id = h.id and h.key = 'PERSIST_TEST#REFERENCE';
    v_sql := 'insert into '||v_link_table||' (id, PERSIST_TEST_CHILD_ID, PERSIST_TEST_ID,ldts,removed) values ('||v_link_table||'_seq.nextval,:id, :lid,:v_systimestamp,0)';
    --dbms_output.put_line(v_sql);
    execute immediate v_sql using v_child_id, v_id,v_systimestamp;


    insert into crs_sys_h_localization (id,key,ldts) values (crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.nextval,v_systimestamp) returning id into v_local_id;
    insert into crs_sys_s_localization (id,h_id,string_ru,string_en,ldts,digest) values (crs_sys_s_localization_seq.nextval, v_local_id, 'дочерняя тест строка ru','child test string en',v_systimestamp,'n');
    select s.link_table into v_link_table from crs_sys_h_attribute h join crs_sys_s_attribute s on s.h_id = h.id and h.key = 'PERSIST_TEST_CHILD#STRINGML';
    v_sql := 'insert into '||v_link_table||' (id, PERSIST_TEST_CHILD_ID, LOCALIZATION_ID,ldts,removed) values ('||v_link_table||'_seq.nextval,:id, :lid,:v_systimestamp,0)';
    --dbms_output.put_line(v_sql);
    execute immediate v_sql using v_child_id, v_local_id,v_systimestamp;

    insert into crs_sys_h_localization (id,key,ldts) values (crs_sys_h_localization_seq.nextval, crs_sys_h_localization_seq.nextval,v_systimestamp) returning id into v_local_id;
    insert into crs_sys_s_localization (id,h_id,text_ru,text_en,ldts,digest) values (crs_sys_s_localization_seq.nextval, v_local_id, 'дочерний тест текст ru','child test text en',v_systimestamp,'n');
    select s.link_table into v_link_table from crs_sys_h_attribute h join crs_sys_s_attribute s on s.h_id = h.id and h.key = 'PERSIST_TEST_CHILD#TEXTML';
    v_sql := 'insert into '||v_link_table||' (id, PERSIST_TEST_CHILD_ID, LOCALIZATION_ID,ldts,removed) values ('||v_link_table||'_seq.nextval,:id, :lid,:v_systimestamp,0)';
    --dbms_output.put_line(v_sql);
    execute immediate v_sql using v_child_id, v_local_id,v_systimestamp;

    insert into crs_sys_h_storage (id, key,ldts) values (crs_sys_h_storage_seq.nextval, crs_sys_h_storage_seq.nextval,v_systimestamp) returning id into v_file_id;
    insert into crs_sys_s_storage_desc (id, h_id, mime_type, name,ldts,digest) values (crs_sys_s_storage_desc_seq.nextval, v_file_id, 'text/plain', 'child-text-filename.txt',v_systimestamp,'n');
    insert into crs_sys_s_storage (id,h_id,data,ldts,digest) values (crs_sys_s_storage_seq.nextval, v_file_id, utl_raw.cast_to_raw('childfiledata'),v_systimestamp,'n');
    select s.link_table into v_link_table from crs_sys_h_attribute h join crs_sys_s_attribute s on s.h_id = h.id and h.key = 'PERSIST_TEST_CHILD#FILE';
    v_sql := 'insert into '||v_link_table||' (id, PERSIST_TEST_CHILD_ID, storage_id,ldts,removed) values ('||v_link_table||'_seq.nextval,:id, :lid,:v_systimestamp,0)';
    --dbms_output.put_line(v_sql);
    execute immediate v_sql using v_child_id, v_file_id,v_systimestamp;

    commit;
    exception when others then
    rollback;
    raise;
end;
