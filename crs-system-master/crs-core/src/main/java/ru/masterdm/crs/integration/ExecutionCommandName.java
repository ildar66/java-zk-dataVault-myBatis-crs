package ru.masterdm.crs.integration;

/**
 * Execution command names enumeration.
 * @author Alexey Chalov
 */
public enum ExecutionCommandName {

    CPI_LATEST_SYNC_DATE("cpiLatestSyncDate"),
    CPI_DEPARMENTS_BY_FILTER("cpiDepartmentsByFilter"),
    CPI_DEPARTMENT_MAPPINGS("cpiDepartmentMappings"),
    CPI_PERSIST_DEPARTMENT_MAPPINGS("cpiPersistDepartmentMappings");

    private final String commandName;

    /**
     * Constructor.
     * @param commandName command name
     */
    ExecutionCommandName(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Returns command name.
     * @return command name
     */
    public String getCommandName() {
        return commandName;
    }
}
