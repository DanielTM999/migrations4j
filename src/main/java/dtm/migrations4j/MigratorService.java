package dtm.migrations4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import dtm.migrations4j.core.Migrator;
import dtm.migrations4j.core.MigratorLog;

public class MigratorService implements Migrator {

    private Connection dbHost;
    private Connection dbTarget;
    private String[] tablesToMigration;
    private MigratorLogServiceConsole logServiceConsole;

    public MigratorService(Connection dbHost, Connection dbTarget) {
        this.dbHost = dbHost;
        this.dbTarget = dbTarget;
        this.logServiceConsole = new MigratorLogServiceConsole();
    }

    @Override
    public void setTablesToMigrate(String... tables) {
        tablesToMigration = tables;
    }

    @Override
    public void executeMigration() throws SQLException {
        if (tablesToMigration == null || tablesToMigration.length == 0) {
            throw new IllegalArgumentException("No tables specified for migration.");
        }

        dbTarget.setAutoCommit(false); 

        for (String table : tablesToMigration) {
            try (Statement statementHost = dbHost.createStatement();
                 ResultSet resultSet = statementHost.executeQuery("SELECT * FROM " + table)) {

                logServiceConsole.builder.append("Starting migration for table: ").append(table).append("\n");

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                StringBuilder columns = new StringBuilder();
                StringBuilder placeholders = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    columns.append(metaData.getColumnName(i));
                    placeholders.append("?");
                    if (i < columnCount) {
                        columns.append(",");
                        placeholders.append(",");
                    }
                }

                String insertQuery = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";
                logServiceConsole.builder.append("Prepared query for insertion: ").append(insertQuery).append("\n");

                try (PreparedStatement preparedStatementTarget = dbTarget.prepareStatement(insertQuery)) {
                    int rowCount = 0;
                    while (resultSet.next()) {
                        StringBuilder rowLog = new StringBuilder("Inserting row with values: ");
                        for (int i = 1; i <= columnCount; i++) {
                            Object value = resultSet.getObject(i);
                            preparedStatementTarget.setObject(i, value);
                            rowLog.append(value).append(i < columnCount ? ", " : "");
                        }
                        logServiceConsole.builder.append(rowLog).append("\n");

                        preparedStatementTarget.executeUpdate();
                        rowCount++;
                    }
                    logServiceConsole.builder.append("Inserted ").append(rowCount).append(" rows into table: ").append(table).append("\n");

                }

                logServiceConsole.builder.append("Migration completed for table: ").append(table).append("\n");

            } catch (SQLException e) {
                logServiceConsole.builder.append("Error during migration of table: ").append(table).append("\n");
                logServiceConsole.builder.append("SQL State: ").append(e.getSQLState()).append("\n");
                logServiceConsole.builder.append("Error Code: ").append(e.getErrorCode()).append("\n");
                logServiceConsole.builder.append("Message: ").append(e.getMessage()).append("\n");
                logServiceConsole.builder.append("Rolling back changes...").append("\n");
                dbTarget.rollback(); 
                throw e;
            }
        }

        dbTarget.commit(); 
        logServiceConsole.builder.append("Migration committed successfully.").append("\n");
        dbTarget.setAutoCommit(true); 
    }

    public File createScriptMigrationFile() throws SQLException {
        return createScriptMigrationFile("migration_script.sql");
    }

    public File createScriptMigrationFile(String path) throws SQLException {
        String script = createScriptMigration();

        File scriptFile = new File(path);
        try (FileWriter writer = new FileWriter(scriptFile)) {
            writer.write(script);
        } catch (IOException e) {
            logServiceConsole.builder.append("Failed to write migration script to file: ").append(path).append("\n");
            throw new RuntimeException("Failed to write migration script to file: " + path, e);
        }

        logServiceConsole.builder.append("Migration script written to file: ").append(path).append("\n");
        return scriptFile;
    }
    
    public String createScriptMigration() throws SQLException {
        if (tablesToMigration == null || tablesToMigration.length == 0) {
            throw new IllegalArgumentException("No tables specified for migration.");
        }

        StringBuilder scriptBuilder = new StringBuilder();

        for (String table : tablesToMigration) {
            try (Statement statementHost = dbHost.createStatement();
                 ResultSet resultSet = statementHost.executeQuery("SELECT * FROM " + table)) {

                logServiceConsole.builder.append("Creating migration script for table: ").append(table).append("\n");

                var metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (resultSet.next()) {
                    StringBuilder columns = new StringBuilder();
                    StringBuilder values = new StringBuilder();

                    for (int i = 1; i <= columnCount; i++) {
                        columns.append(metaData.getColumnName(i));
                        Object value = resultSet.getObject(i);
                        values.append(formatValue(value));
                        if (i < columnCount) {
                            columns.append(",");
                            values.append(",");
                        }
                    }

                    String insertQuery = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ");\n";
                    scriptBuilder.append(insertQuery);

                    logServiceConsole.builder.append("Generated query: ").append(insertQuery);
                }

            } catch (SQLException e) {
                logServiceConsole.builder.append("Failed to create migration script for table: ").append(table).append("\n");
                throw new RuntimeException("Failed to create migration script for table: " + table, e);
            }
        }

        logServiceConsole.builder.append("Migration script creation completed.\n");
        return scriptBuilder.toString();
    }
    
    @Override
    public void clean() {
        tablesToMigration = null;
    }

    @Override
    public void close() {
        try {
            if (dbHost != null && !dbHost.isClosed()) {
                dbHost.close();
            }
            if (dbTarget != null && !dbTarget.isClosed()) {
                dbTarget.close();
            }
            tablesToMigration = null;
            logServiceConsole.builder.append("Connections closed successfully.").append("\n");
        } catch (SQLException e) {
            logServiceConsole.builder.append("Error closing connections: ").append(e.getMessage()).append("\n");
        }
    }

    @Override
    public MigratorLog getMigratorLog() {
        return logServiceConsole;
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String || value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
            return "'" + value.toString().replace("'", "''") + "'";
        } else {
            return value.toString();
        }
    }

    private static class MigratorLogServiceConsole implements MigratorLog {

        public StringBuilder builder = new StringBuilder();

        @Override
        public String writeLog() {
            return (builder.toString());
        }
    }
}
