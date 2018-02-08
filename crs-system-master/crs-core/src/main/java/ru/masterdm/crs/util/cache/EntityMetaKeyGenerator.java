package ru.masterdm.crs.util.cache;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.service.EntityMetaService;

/**
 * Generator to generate unique key for metadata cache.
 * @author Pavel Masalov
 */
@Service("entityMetaKeyGenerator")
public class EntityMetaKeyGenerator implements KeyGenerator {

    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private MetadataDao metadataDao;

    @Override
    public Object generate(Object target, Method method, Object... params) {
        String entityMetaKey = (String) params[0];
        LocalDateTime ldts = (LocalDateTime) params[1];
        if (ldts == null)
            ldts = metadataDao.getSysTimestamp();
        List<LocalDateTime> timeSlices = entityMetaService.getEntityMetaTimeSlices(entityMetaKey);
        if (timeSlices.isEmpty())
            return SimpleKey.EMPTY;

        LocalDateTime keyDT = normalizeLdts(timeSlices, ldts);
        if (keyDT == null)
            return SimpleKey.EMPTY;

        return new SimpleKey(entityMetaKey, keyDT);
    }

    /**
     * Get proper time slice.
     * @param timeSlices slises
     * @param ldts load datetime
     * @return time slice or null if ldts less than earliest slice
     */
    private LocalDateTime normalizeLdts(List<LocalDateTime> timeSlices, LocalDateTime ldts) {
        Iterator<LocalDateTime> iterator = timeSlices.iterator();
        while (iterator.hasNext()) {
            LocalDateTime curr = iterator.next();
            if (ldts.compareTo(curr) >= 0)
                return curr;
        }
        return null;
    }

}
