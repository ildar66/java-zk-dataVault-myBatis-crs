package ru.masterdm.crs.dao.entity;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.annotations.Param;

import ru.masterdm.crs.domain.entity.criteria.RowRange;

/**
 * Additional data access methods for client related information.
 * @author Pavel Masalov
 */
public interface ClientDao {

    /**
     * Get of client hub ids for searching string.
     * Search clients by SLX (key), INN, Names
     * @param searchString searching string
     * @param rowRange paging
     * @param hubIds hubIds. If null then not influence, if empty or not null then filter
     * @return list of clients identifiers
     */
    List<Pair<Long, Long>> getClientIdsBySearchString(@Param("searchString") String searchString, @Param("rowRange") RowRange rowRange,
                                                      @Param("hubIds") List<Long> hubIds);

    /**
     * Get of client group hub ids for searching string.
     * Search client groups by SLX (key), Names
     * @param searchString searching string
     * @param rowRange paging
     * @param hubIds hubIds. If null then not influence, if empty or not null then filter
     * @return list of clients identifiers
     */
    List<Pair<Long, Long>> getClientGroupIdsBySearchString(@Param("searchString") String searchString, @Param("rowRange") RowRange rowRange,
                                                           @Param("hubIds") List<Long> hubIds);
}
