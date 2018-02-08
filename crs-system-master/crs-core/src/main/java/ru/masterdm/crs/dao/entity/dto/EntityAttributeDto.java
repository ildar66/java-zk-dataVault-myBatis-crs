package ru.masterdm.crs.dao.entity.dto;

import java.util.List;

/**
 * Store partition of reference data for hub id.
 * @author Pavel Masalov
 */
public class EntityAttributeDto {

    private Long mainHubId;
    private List<EntityAttributeDtoKey> keys;

    /**
     * Returns hub id.
     * @return hub id
     */
    public Long getMainHubId() {
        return mainHubId;
    }

    /**
     * Sets hub id.
     * @param mainHubId hub id
     */
    public void setMainHubId(Long mainHubId) {
        this.mainHubId = mainHubId;
    }

    /**
     * Returns list of attribute-key-value.
     * @return list of attribute-key-value
     */
    public List<EntityAttributeDtoKey> getKeys() {
        return keys;
    }

    /**
     * Sets list of attribute-key-value.
     * @param keys list of attribute-key-value
     */
    public void setKeys(List<EntityAttributeDtoKey> keys) {
        this.keys = keys;
    }
}
