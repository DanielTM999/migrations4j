package dtm.migrations4j.core;

import java.io.File;
import java.sql.SQLException;

public interface Migrator {
    void setTablesToMigrate(String... tables);
    void executeMigration() throws SQLException;
    String createScriptMigration() throws SQLException;
    File createScriptMigrationFile() throws SQLException;
    File createScriptMigrationFile(String path) throws SQLException;
    void clean();
    void close();
    MigratorLog getMigratorLog();
}
