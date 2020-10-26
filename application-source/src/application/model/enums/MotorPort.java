package application.model.enums;

/**
 * Motor ports.
 *
 * @author Dizzy
 */
public enum MotorPort {

    A("A"),
    B("B"),
    C("C"),
    D("D");

    private String port;

    private MotorPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return port;
    }
}
