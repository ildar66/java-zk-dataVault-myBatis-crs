--liquibase formatted sql

--changeset pmasalov:crs-VTBCRS-509-rebuild-secure-index logicalFilePath:crs-VTBCRS-509-rebuild-secure-index endDelimiter:/ runOnChange:true
-- rebuild index once after installation
/* 20170914 */
update crs_h_calc set virtual_secure_tag = null
/
commit
/