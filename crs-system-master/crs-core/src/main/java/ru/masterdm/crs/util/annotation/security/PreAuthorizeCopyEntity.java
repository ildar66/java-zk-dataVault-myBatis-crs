package ru.masterdm.crs.util.annotation.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Authorize copy entity method.
 * @author Pavel Masalov
 */
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#entity, T(ru.masterdm.crs.domain.entity.BusinessAction$Action).CREATE_COPY)")
public @interface PreAuthorizeCopyEntity {

}
