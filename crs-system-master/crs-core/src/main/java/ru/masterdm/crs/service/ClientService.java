package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.List;

import ru.masterdm.crs.domain.entity.criteria.RowRange;

/**
 * Client additional utility functions.
 * @author Pavel Masalov
 */
public interface ClientService {

    /**
     * Get of client hub ids for searching string.
     * Search clients by SLX (key), INN, Names
     * @param searchString searching string
     * @param rowRange paging
     * @return list of clients ids
     */
    List<Long> getClientIdsBySearchString(String searchString, RowRange rowRange);

    /**
     * Get of client hub ids for searching string.
     * Search clients by SLX (key), INN, Names
     * @param searchString searching string
     * @param rowRange paging
     * @param hubIds client hub identifiers
     * @return list of clients ids
     */
    List<Long> getClientIdsBySearchString(String searchString, RowRange rowRange, List<Long> hubIds);

    /**
     * Get of client group hub ids for searching string.
     * Search client groups by SLX (key), Names
     * @param searchString searching string
     * @param rowRange paging
     * @return list of client groups ids
     */
    List<Long> getClientGroupIdsBySearchString(String searchString, RowRange rowRange);

    /**
     * Get of client group hub ids for searching string.
     * Search client groups by SLX (key), Names
     * @param searchString searching string
     * @param rowRange paging
     * @param hubIds client groups hub identifiers
     * @return list of client groups ids
     */
    List<Long> getClientGroupIdsBySearchString(String searchString, RowRange rowRange, List<Long> hubIds);

    /**
     * Get datetime if consistent state for all clients date.
     * @return load datetime
     */
    LocalDateTime getClientsEntityRequestLdts();
}
