package liquibase.executor;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@LiquibaseService(skip = true)
public class LoggingExecutor extends AbstractExecutor implements Executor {

    private Writer output;
    private Executor delegatedReadExecutor;

    public LoggingExecutor(Executor delegatedExecutor, Writer output, Database database) {
        this.output = output;
        this.delegatedReadExecutor = delegatedExecutor;
        setDatabase(database);
    }

    protected Writer getOutput() {
        return output;
    }

    @Override
    public ExecuteResult execute(SqlStatement sql) throws DatabaseException {
        outputStatement(sql);
        return new ExecuteResult();
    }

    @Override
    public UpdateResult update(SqlStatement sql) throws DatabaseException {
        if (sql instanceof LockDatabaseChangeLogStatement) {
            return new UpdateResult(1);
        } else if (sql instanceof UnlockDatabaseChangeLogStatement) {
            return new UpdateResult(1);
        }

        outputStatement(sql);

        return new UpdateResult(0);
    }

    @Override
    public ExecuteResult execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
        return new ExecuteResult();
    }

    @Override
    public UpdateResult update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
        return new UpdateResult(0);
    }

    @Override
    public void comment(String message) throws DatabaseException {
        try {
            output.write(database.getLineComment());
            output.write(" ");
            output.write(message);
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    private void outputStatement(SqlStatement sql) throws DatabaseException {
        outputStatement(sql, new ArrayList<SqlVisitor>());
    }

    private void outputStatement(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        try {
            if (SqlGeneratorFactory.getInstance().generateStatementsVolatile(sql, database)) {
                throw new DatabaseException(sql.getClass().getSimpleName()+" requires access to up to date database metadata which is not available in SQL output mode");
            }
            for (String statement : applyVisitors(sql, sqlVisitors)) {
                if (statement == null) {
                    continue;
                }
                output.write(statement);


                if (database instanceof MSSQLDatabase || database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
                    output.write(StreamUtil.getLineSeparator());
                    output.write("GO");
    //            } else if (database instanceof OracleDatabase) {
    //                output.write(StreamUtil.getLineSeparator());
    //                output.write("/");
                } else {
                    String endDelimiter = ";";
                    if (sql instanceof RawSqlStatement) {
                        endDelimiter = ((RawSqlStatement) sql).getEndDelimiter();
                    }
                    if (!statement.endsWith(endDelimiter)) {
                        output.write(endDelimiter);
                    }
                }
                output.write(StreamUtil.getLineSeparator());
                output.write(StreamUtil.getLineSeparator());
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public QueryResult query(SqlStatement sql) throws DatabaseException {
        return query(sql, null);
    }

    @Override
    public QueryResult query(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        if (sql instanceof SelectFromDatabaseChangeLogLockStatement) {
            return new QueryResult(Boolean.FALSE);
        }
        try {
            return delegatedReadExecutor.query(sql, sqlVisitors);
        } catch (DatabaseException e) {
            if (sql instanceof GetNextChangeSetSequenceValueStatement) { //table probably does not exist
                return new QueryResult(0);
            }
            throw e;
        }
    }

    @Override
    public boolean updatesDatabase() {
        return false;
    }
}
