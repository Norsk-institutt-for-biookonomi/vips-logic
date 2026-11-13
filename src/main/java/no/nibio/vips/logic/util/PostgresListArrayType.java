package no.nibio.vips.logic.util;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import java.sql.Types;

public class PostgresListArrayType extends ListArrayType {

    // Keep Hypersistence's array binding, but match PostgreSQL JDBC schema metadata.
    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }
}
