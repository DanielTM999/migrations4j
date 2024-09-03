package dtm.migrations4j.core;

import java.util.List;

public interface DataBaseActions {
    void setDbHost(SourceConnectionDetails connectionDetailsHost);
    void setDbTarget(SourceConnectionDetails SourceConnectionDetailsTarget);
    String exportDataToJson(String outputDir, List<String> tables);
    String exportDataToJson(String... tables);
    Migrator getMigrator();
}
