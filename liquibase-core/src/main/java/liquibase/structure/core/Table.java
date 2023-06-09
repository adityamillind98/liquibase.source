package liquibase.structure.core;

import liquibase.statement.NotNullConstraint;
import liquibase.util.BooleanUtil;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Table extends Relation {


    public Table() {
        setAttribute("outgoingForeignKeys", new ArrayList<ForeignKey>());
        setAttribute("indexes", new ArrayList<Index>());
        setAttribute("uniqueConstraints", new ArrayList<UniqueConstraint>());
        setAttribute("notNullConstraints", new ArrayList<UniqueConstraint>());
    }

    public Table(String catalogName, String schemaName, String tableName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(tableName);
    }

    public PrimaryKey getPrimaryKey() {
        return getAttribute("primaryKey", PrimaryKey.class);
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.setAttribute("primaryKey", primaryKey);
    }

    /**
     * Returns the list of all outgoing FOREIGN KEYS from this table
     */
    public List<ForeignKey> getOutgoingForeignKeys() {
        List<ForeignKey> fkList = getAttribute("outgoingForeignKeys", List.class);
        return ((fkList == null) ? new ArrayList<>(0) : fkList);
    }

    @Override
    public List<Index> getIndexes() {
        return getAttribute("indexes", List.class);
    }

    @Override
    public List<UniqueConstraint> getUniqueConstraints() {
        return getAttribute("uniqueConstraints", List.class);
    }
    public List<NotNullConstraint> getNotNullConstraints() {
        return getAttribute("notNullConstraints", List.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        Table that = (Table) o;

        if ((this.getSchema() != null) && (that.getSchema() != null)) {
            boolean schemasTheSame = this.getSchema().equals(that.getSchema());
            if (!schemasTheSame) {
                return false;
            }
        }

        return getName().equalsIgnoreCase(that.getName());

    }

    @Override
    public int hashCode() {
        return StringUtil.trimToEmpty(getName()).toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Table setName(String name) {
        return (Table) super.setName(name);
    }

    public String getTablespace() {
        return getAttribute("tablespace",String.class);
    }

    public Table setTablespace(String tablespace) {
        setAttribute("tablespace", tablespace);
        return this;
    }

    public Boolean getDefaultTablespace() {
        return getAttribute("default_tablespace", Boolean.class);
    }

    public Table setDefaultTablespace(Boolean tablespace) {
        setAttribute("default_tablespace", tablespace);
        return this;
    }

    public boolean isDefaultTablespace() {
        Boolean b = getAttribute("default_tablespace",Boolean.class);
        return BooleanUtil.isTrue(b);
    }
}
