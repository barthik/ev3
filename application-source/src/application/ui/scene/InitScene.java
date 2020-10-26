package application.ui.scene;

import application.model.ApplicationSettings;
import application.ui.ApplicationMenu;
import application.ui.ApplicationScene;
import application.ui.ApplicationStateBar;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Main scene after application start.
 *
 * @author Dizzy
 */
public class InitScene extends ApplicationScene {

    private BorderPane root = new BorderPane();

    /**
     * Create scene.
     *
     */
    public InitScene() {
        VBox content = new VBox();
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("Please prepare the EV3 device.");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        Label text = new Label("This application is designed for the tracked robot consisting of two large engines and 4 infrared sensors. At the moment to run program for the EV3 you will need to create a bootable micro SD card with LeJOS JVM. For the proper functionality, follow the steps below:\n\n"
                + "1. Make sure that the card with LeJOS is inserted into the microSD slot on the device.\n\n"
                + "2. Connect the left large motor to port [B], and the right large motor to port [A].\n\n"
                + "3. To port [S1, S2, S3] and [S4] connect the infrared sensors.\n\n"
                + "4. Now turn bluetooth on the host computer and start the device (wait until the LeJOS is booted) and add the EV3 device to the PAN.\n\n"
                + "5. Click on the connect button and enter the IP address of the device.");
        text.setWrapText(true);
        content.getChildren().add(text);

        root.setTop(ApplicationMenu.getInstance().getMenu());
        root.setCenter(content);
        root.setBottom(ApplicationStateBar.getInstance().getStateBar());
        scene = new Scene(root, ApplicationSettings.WINDOW_WIDTH, ApplicationSettings.WINDOW_HEIGHT);
    }

}
