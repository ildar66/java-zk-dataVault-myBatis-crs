--liquibase formatted sql

--changeset pmasalov:crs-js-lib-md-data-access-data-3 logicalFilePath:crs-js-lib-md-data-access-data-3 endDelimiter:/
declare
    v_count number;
    v_hub_id number;
    v_ldts timestamp := systimestamp;
begin
    select count(1) into v_count from crs_h_calc_formula where key = 'LIB_MD_DATA_ACCESS';
    if v_count = 0 then
        insert into crs_h_calc_formula (id, key, ldts)
        values (crs_h_calc_formula_seq.nextval, 'LIB_MD_DATA_ACCESS', v_ldts)
        returning id into v_hub_id;

        insert into crs_s_calc_formula_desc (id, h_id, ldts, digest, name_ru, name_en, comment_ru, comment_en, eval_lang, type)
        values (crs_s_calc_formula_desc_seq.nextval,
                v_hub_id,
                v_ldts,
                'no_digest',
                'Библиотека по доступу к данным',
                'Data access library',
                'Библиотека по доступу к данным',
                'Data access library',
                'nashorn',
                'LIBRARY');

        insert into crs_s_calc_formula (id, h_id, ldts, digest, formula)
        values (crs_s_calc_formula_seq.nextval, v_hub_id, v_ldts, 'no_digest', empty_clob());

        commit;
    end if;
end;
/
--changeset akirilchev:crs-js-lib-md-data-access-data-formula-type logicalFilePath:crs-js-lib-md-data-access-data-formula-type endDelimiter:/
update crs_s_calc_formula_desc s
   set type = 'LIBRARY'
 where s.h_id in (select h.id
                    from crs_h_calc_formula h
                   where h.key = 'LIB_MD_DATA_ACCESS')
/

--changeset emelnikov:crs-1.0-VTBCRS-297 logicalFilePath:crs-1.0-VTBCRS-297 endDelimiter:/
update crs_s_calc_formula_desc s
   set type = 'SYS_LIBRARY'
 where s.h_id in (select h.id
                    from crs_h_calc_formula h
                   where h.key = 'LIB_MD_DATA_ACCESS')
/