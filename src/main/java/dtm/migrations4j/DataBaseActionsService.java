package dtm.migrations4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import dtm.migrations4j.core.DataBaseActions;
import dtm.migrations4j.core.Migrator;
import dtm.migrations4j.core.SourceConnectionDetails;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseActionsService implements DataBaseActions {

    private Connection dbHost;
    private Connection dbTarget;

    public DataBaseActionsService(){}

    public DataBaseActionsService(SourceConnectionDetails connectionDetailsHost, SourceConnectionDetails connectionDetailsTarget){
        try {
            Class.forName(connectionDetailsHost.driverClassName());
            dbHost = DriverManager.getConnection(
                connectionDetailsHost.connectionUrl(),
                connectionDetailsHost.user(),
                connectionDetailsHost.password()
            );
            dbTarget = DriverManager.getConnection(
                connectionDetailsTarget.connectionUrl(),
                connectionDetailsTarget.user(),
                connectionDetailsTarget.password()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to the database.");
        }
    }

    @Override
    public void setDbHost(SourceConnectionDetails connectionDetailsHost) {
        try {
            Class.forName(connectionDetailsHost.driverClassName());
            dbHost = DriverManager.getConnection(
                connectionDetailsHost.connectionUrl(),
                connectionDetailsHost.user(),
                connectionDetailsHost.password()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to the host database.");
        }
    }

    @Override
    public void setDbTarget(SourceConnectionDetails connectionDetailsTarget) {
        try {
            Class.forName(connectionDetailsTarget.driverClassName());
            dbTarget = DriverManager.getConnection(
                connectionDetailsTarget.connectionUrl(),
                connectionDetailsTarget.user(),
                connectionDetailsTarget.password()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to the target database.");
        }
    }

    @Override
    public String exportDataToJson(String outputDir, List<String> tables) {
        if (tables == null || tables.size() == 0) {
            throw new IllegalArgumentException("No tables specified for export.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder outputPaths = new StringBuilder();

        for (String table : tables) {
            try (Statement statement = dbHost.createStatement()) {
                String query = "SELECT * FROM " + table;
                ResultSet resultSet = statement.executeQuery(query);

                Map<String, Object> tableData = new HashMap<>();
                while (resultSet.next()) {
                    Map<String, Object> rowData = new HashMap<>();
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        rowData.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                    }
                    tableData.put(String.valueOf(resultSet.getRow()), rowData);
                }

                File outputFile = new File(outputDir, table + ".json");
                objectMapper.writeValue(outputFile, tableData);
                outputPaths.append(outputFile.getAbsolutePath()).append("\n");

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to export data from table: " + table);
            }
        }

        return outputPaths.toString().trim();
    }

    @Override
    public String exportDataToJson(String... tables) {
        List<String> tablesTarget = Arrays.asList(tables);
        return exportDataToJson(".", tablesTarget); 
    }

    @Override
    public Migrator getMigrator() {
        return new MigratorService(dbHost, dbTarget);
    }
}
