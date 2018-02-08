package ru.masterdm.crs.util.annotation.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Authorize get for calculation.
 * @author Pavel Masalov
 */
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(1, T(ru.masterdm.crs.domain.calc.Calculation).METADATA_KEY,"
              + " T(ru.masterdm.crs.domain.entity.BusinessAction$Action).VIEW)")
public @interface PreAuthorizeGetCalculation {

}
