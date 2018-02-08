package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.dao.entity.ClientDao;
import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.exception.TooShortSearchStringException;

/**
 * Client additional utility functions.
 * @author Pavel Masalov
 */
@Validated
@Service("clientService")
public class ClientServiceImpl implements ClientService {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ClientDao clientDao;
    @Autowired
    private MetadataDao metadataDao;

    @Autowired
    private IntegrationService integrationService;

    @Override
    public List<Long> getClientIdsBySearchString(@NotNull String searchString, RowRange rowRange) {
        return getClientIdsBySearchString(searchString, rowRange, null);
    }

    @Override
    public List<Long> getClientIdsBySearchString(@NotNull String searchString, RowRange rowRange, List<Long> hubIds) {
        List<Pair<Long, Long>> hubIdAndCcs = null;
        try {
            hubIdAndCcs = clientDao.getClientIdsBySearchString(searchString, rowRange, hubIds);
        } catch (UncategorizedSQLException e) {
            throw new TooShortSearchStringException(e);
        }

        if (hubIdAndCcs.isEmpty()) {
            rowRange.setTotalCount(0L);
        } else {
            rowRange.setTotalCount(hubIdAndCcs.get(0).getValue());
        }
        return hubIdAndCcs.stream().map(p -> p.getKey()).collect(Collectors.toList());
    }

    @Override
    public List<Long> getClientGroupIdsBySearchString(@NotNull String searchString, RowRange rowRange) {
        return getClientGroupIdsBySearchString(searchString, rowRange, null);
    }

    @Override
    public List<Long> getClientGroupIdsBySearchString(@NotNull String searchString, RowRange rowRange, List<Long> hubIds) {
        List<Pair<Long, Long>> hubIdAndCcs = null;
        try {
            hubIdAndCcs = clientDao.getClientGroupIdsBySearchString(searchString, rowRange, hubIds);
        } catch (UncategorizedSQLException e) {
            throw new TooShortSearchStringException(e);
        }

        if (hubIdAndCcs.isEmpty()) {
            rowRange.setTotalCount(0L);
        } else {
            rowRange.setTotalCount(hubIdAndCcs.get(0).getValue());
        }
        return hubIdAndCcs.stream().map(p -> p.getKey()).collect(Collectors.toList());
    }

    @Override
    public LocalDateTime getClientsEntityRequestLdts() {
        LocalDateTime ldts = integrationService.getCpiLatestSyncDate();
        if (ldts == null)
            ldts = metadataDao.getSysTimestamp();
        return ldts;
    }
}
