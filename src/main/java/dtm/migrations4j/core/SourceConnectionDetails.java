package dtm.migrations4j.core;

public abstract class SourceConnectionDetails {
    public abstract String connectionUrl();
    public abstract String user();
    public abstract String password();
    public abstract String driverClassName();
}
