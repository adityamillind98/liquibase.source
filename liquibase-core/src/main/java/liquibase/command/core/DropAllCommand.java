package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @deprecated Implement commands with {@link liquibase.command.CommandStep} and call them with {@link liquibase.command.CommandFactory#getCommandDefinition(String...)}.
 */
public class DropAllCommand extends AbstractCommand<CommandResult> {

    private Database database;
    private CatalogAndSchema[] schemas;
    private String changeLogFile;
    private Liquibase liquibase;

    @Override
    public String getName() {
        return "dropAll";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public void setLiquibase(Liquibase liquibase) {
        this.liquibase = liquibase;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public CatalogAndSchema[] getSchemas() {
        return schemas;
    }

    public void setSchemas(CatalogAndSchema[] schemas) {
        this.schemas = schemas;
    }

    public DropAllCommand setSchemas(String... schemas) {
        if ((schemas == null) || (schemas.length == 0) || (schemas[0] == null)) {
            this.schemas = null;
            return this;
        }

        schemas = StringUtil.join(schemas, ",").split("\\s*,\\s*");
        List<CatalogAndSchema> finalList = new ArrayList<>();
        for (String schema : schemas) {
            finalList.add(new CatalogAndSchema(null, schema).customize(database));
        }

        this.schemas = finalList.toArray(new CatalogAndSchema[0]);


        return this;

    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    @Override
    public CommandResult run() throws Exception {
        final CommandScope commandScope = new CommandScope("dropAllInternal");
        commandScope.addArgumentValue(InternalDropAllCommandStep.CHANGELOG_ARG, this.liquibase.getDatabaseChangeLog());
        commandScope.addArgumentValue(InternalDropAllCommandStep.CHANGELOG_FILE_ARG, this.changeLogFile);
        commandScope.addArgumentValue(InternalDropAllCommandStep.DATABASE_ARG, this.database);
        commandScope.addArgumentValue(InternalDropAllCommandStep.SCHEMAS_ARG, this.schemas);

        final CommandResults results = commandScope.execute();

        return new CommandResult("All objects dropped from " + database.getConnection().getConnectionUserName() + "@" + database.getConnection().getURL());
    }
}
