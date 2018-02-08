-- run this script after application installs

update crs_h_client set key = key;
commit;
update crs_h_client_group set key = key;
commit;

begin
    ctx_ddl.sync_index('crs_h_client_group_i01');
    ctx_ddl.sync_index('crs_h_client_i01');
end;
/
