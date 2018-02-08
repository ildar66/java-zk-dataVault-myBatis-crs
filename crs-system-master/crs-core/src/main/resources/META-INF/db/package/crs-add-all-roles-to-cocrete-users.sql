insert into crs_l_user_role(id, user_id, role_id, removed, ldts)
select crs_l_user_role_seq.nextval, u.id, r.id, 0, to_timestamp('011111', 'mmyyyy')
  from crs_h_user u join crs_h_role r on 1=1
where u.key in ( 'ADMINWF' ) --вместо ADMINWF указать перечень логинов, кому дать все права
and not exists(select 1 from crs_l_user_role where user_id = u.id and role_id = r.id)
/
commit
/