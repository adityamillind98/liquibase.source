package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;

/**
 * Modifies properties of an existing sequence. StartValue is not allowed since we cannot alter the starting sequence number
 */
@DatabaseChange(name = "alterSequence",
    description = "Alter properties of an existing sequence",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "sequence")
public class AlterSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private Boolean ordered;
    private BigInteger cacheSize;
    private Boolean cycle;
    private String dataType;

    @DatabaseChangeProperty(mustEqualExisting ="sequence.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="sequence.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence", description = "Name of the sequence")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @DatabaseChangeProperty(description = "New amount the sequence should increment by at each call")
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
    }

    @DatabaseChangeProperty(description = "New maximum value for the sequence")
    public BigInteger getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
    }

    @DatabaseChangeProperty(description = "New minimum value for the sequence")
    public BigInteger getMinValue() {
        return minValue;
    }

    public void setMinValue(BigInteger minValue) {
        this.minValue = minValue;
    }

    @DatabaseChangeProperty(description = "Whether the sequence is generated in the requested order")
    public Boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }

    @DatabaseChangeProperty(description = "Number of values to fetch per query")
    public BigInteger getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(BigInteger cacheSize) {
        this.cacheSize = cacheSize;
    }

    @DatabaseChangeProperty(description = "Whether the sequence cycles when it hits its max value")
    public Boolean getCycle() {
        return cycle;
    }

    public void setCycle(Boolean cycle) {
        this.cycle = cycle;
    }

    @DatabaseChangeProperty(description = "Data type of the sequence")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new AlterSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName())
                .setIncrementBy(getIncrementBy())
                .setMaxValue(getMaxValue())
                .setMinValue(getMinValue())
                .setCacheSize(getCacheSize())
                .setCycle(getCycle())
                .setOrdered(isOrdered())
                .setDataType(getDataType())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Sequence sequence = SnapshotGeneratorFactory.getInstance().createSnapshot(new Sequence(getCatalogName(), getSchemaName(), getSequenceName()), database);
            if (sequence == null) {
                return result.unknown("Sequence " + getSequenceName() + " does not exist");
            }

            if (getIncrementBy() != null) {
                result.assertCorrect(getIncrementBy().equals(sequence.getIncrementBy()), "Increment by has a different value");
            }
            if (getMinValue() != null) {
                result.assertCorrect(getMinValue().equals(sequence.getMinValue()), "Min Value is different");
            }
            if (getMaxValue() != null) {
                result.assertCorrect(getMaxValue().equals(sequence.getMaxValue()), "Max Value is different");
            }
            if (isOrdered() != null) {
                result.assertCorrect(isOrdered().equals(sequence.getOrdered()), "Max Value is different");
            }
            if (getCacheSize() != null) {
                result.assertCorrect(getCacheSize().equals(sequence.getCacheSize()), "Cache size is different");
            }
            if (getDataType() != null) {
                result.assertCorrect(getDataType().equals(sequence.getDataType()), "Data type is different");
            }
        } catch (Exception e) {
            return result.unknown(e);
        }
        return result;
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " altered";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
