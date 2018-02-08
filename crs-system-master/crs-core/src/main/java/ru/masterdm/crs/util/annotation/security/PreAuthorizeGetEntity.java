package ru.masterdm.crs.util.annotation.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Authorize get entities methods.
 * @author Pavel Masalov
 */
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#entityMeta, T(ru.masterdm.crs.domain.entity.BusinessAction$Action).VIEW)")
public @interface PreAuthorizeGetEntity {

}
