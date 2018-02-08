package ru.masterdm.crs.util.annotation.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * To check persists methods.
 * @author Pavel Masalov
 */
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#entity,"
              + " {T(ru.masterdm.crs.domain.entity.BusinessAction$Action).CREATE_NEW,"
              + " T(ru.masterdm.crs.domain.entity.BusinessAction$Action).EDIT})")
public @interface PreAuthorizePersistEntity {

}
