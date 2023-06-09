package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropColumnStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class DropColumnGenerator extends AbstractSqlGenerator<DropColumnStatement> {

    @Override
    public ValidationErrors validate(DropColumnStatement dropColumnStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (dropColumnStatement.isMultiple()) {
            ValidationErrors validationErrors = new ValidationErrors();
            DropColumnStatement firstColumn = dropColumnStatement.getColumns().get(0);

            for (DropColumnStatement drop : dropColumnStatement.getColumns()) {
                validationErrors.addAll(validateSingleColumn(drop));
                if ((drop.getTableName() != null) && !drop.getTableName().equals(firstColumn.getTableName())) {
                    validationErrors.addError("All columns must be targeted at the same table");
                }
                if (drop.isMultiple()) {
                    validationErrors.addError("Nested multiple drop column statements are not supported");
                }
            }
            return validationErrors;
        } else {
            return validateSingleColumn(dropColumnStatement);
        }
    }

    private ValidationErrors validateSingleColumn(DropColumnStatement dropColumnStatement) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropColumnStatement.getTableName());
        validationErrors.checkRequiredField("columnName", dropColumnStatement.getColumnName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (statement.isMultiple()) {
            return generateMultipleColumnSql(statement.getColumns(), database);
        } else {
            return generateSingleColumnSql(statement, database);
        }
    }

    private Sql[] generateMultipleColumnSql(List<DropColumnStatement> columns, Database database) {
        List<Sql> result = new ArrayList<>();
        if (database instanceof MySQLDatabase) {
            final StringBuilder alterTable = new StringBuilder("ALTER TABLE " + database.escapeTableName(columns.get(0).getCatalogName(), columns.get(0).getSchemaName(), columns.get(0).getTableName()));
            for (int i = 0; i < columns.size(); i++) {
                alterTable.append(" DROP ").append(database.escapeColumnName(columns.get(i).getCatalogName(), columns.get(i).getSchemaName(), columns.get(i).getTableName(), columns.get(i).getColumnName()));
                if (i < (columns.size() - 1)) {
                    alterTable.append(",");
                }
            }
            result.add(new UnparsedSql(alterTable.toString(), getAffectedColumns(columns)));
        } else {
            if (database instanceof MSSQLDatabase) {
                for (DropColumnStatement column : columns) {
                    final Sql[] sqls = generateSingleColumnSql(column, database);
                    result.add(sqls[0]);
                    result.add(sqls[1]);
                }
            } else {
                for (DropColumnStatement column : columns) {
                    result.add(generateSingleColumnSql(column, database)[0]);
                }
            }
        }
        return result.toArray(EMPTY_SQL);
    }

    private Sql[] generateSingleColumnSql(DropColumnStatement statement, Database database) {
        if (database instanceof DB2Database) {
            return new Sql[] {new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()), getAffectedColumn(statement))};
        } else if (database instanceof Db2zDatabase) {
            return new Sql[]{new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " RESTRICT", getAffectedColumn(statement))};
        } else if ((database instanceof SybaseDatabase) || (database instanceof SybaseASADatabase) || (database
            instanceof FirebirdDatabase) || (database instanceof InformixDatabase)) {
            return new Sql[] {new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()), getAffectedColumn(statement))};
        } else if (database instanceof MSSQLDatabase) {
            return new Sql[] {
                    generateDropDV(statement, database),
                    new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP COLUMN " +
                            database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()), getAffectedColumn(statement))
            };
        }
        return new Sql[] {new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()), getAffectedColumn(statement))};
    }

    private Column[] getAffectedColumns(List<DropColumnStatement> columns) {
        List<Column> affected = new ArrayList<>();
        for (DropColumnStatement column : columns) {
            affected.add(getAffectedColumn(column));
        }
        return affected.toArray(new Column[0]);
    }

    protected Column getAffectedColumn(DropColumnStatement statement) {
        return new Column().setName(statement.getColumnName()).setRelation(new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
    }

    private UnparsedSql generateDropDV(DropColumnStatement statement, Database database) {
        return new UnparsedSql((String) DropDefaultValueGenerator.DROP_DF_MSSQL.apply(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()),
                database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName())));
    }
}
