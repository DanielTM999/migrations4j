package dtm.migrations4j;

import dtm.migrations4j.core.SourceConnectionDetails;

public class ConfigurableConnectionDetails extends SourceConnectionDetails {
    private final String connectionUrl;
    private final String user;
    private final String password;
    private final String driverClassName;

    public ConfigurableConnectionDetails(String connectionUrl, String user, String password, String driverClassName) {
        this.connectionUrl = connectionUrl;
        this.user = user;
        this.password = password;
        this.driverClassName = driverClassName;
    }

    @Override
    public String connectionUrl() {
        return connectionUrl;
    }

    @Override
    public String user() {
        return user;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String driverClassName() {
        return driverClassName;
    }
}
