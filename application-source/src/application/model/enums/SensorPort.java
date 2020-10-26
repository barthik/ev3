package application.model.enums;

/**
 * Sensor ports.
 *
 * @author Dizzy
 */
public enum SensorPort {

    S1("S1"),
    S2("S2"),
    S3("S3"),
    S4("S4");

    private String port;

    private SensorPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return port;
    }
}
