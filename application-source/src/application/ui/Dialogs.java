package application.ui;

import application.model.ApplicationSettings;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Class to represent the states and dialogues.
 *
 * @author Dizzy
 */
public class Dialogs {

    /**
     * Displays a connection dialog box to get IP of the EV3 device.
     *
     * @return String IP adress of the device.
     */
    public static String connectDialog() {
        String ret = null;
        TextInputDialog dialog = new TextInputDialog("10.0.1.1");
        dialog.setTitle("Connect to device");
        dialog.setHeaderText("To connect to the EV3 device enter the ip address.\nBe patient, opening the ports may take a while.");
        dialog.setContentText("IP address:");
        
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            ret = result.get();
        }

        return ret;
    }

    /**
     * Displays a alert dialog box.
     *
     * @param title String title of dialog.
     * @param header String text in header of dialog.
     * @param content String content text.
     */
    public static void alertDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));

        alert.showAndWait();
    }

    /**
     * Displays a warning dialog box.
     *
     * @param title String title of dialog.
     * @param header String text in header of dialog.
     * @param content String content text.
     */
    public static void warningDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));

        alert.showAndWait();
    }

    /**
     * Displays a error dialog box.
     *
     * @param title String title of dialog.
     * @param header String text in header of dialog.
     * @param content String content text.
     */
    public static void errorDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));

        alert.showAndWait();
    }

    /**
     * Displays a exception dialog box.
     *
     * @param title String title of dialog.
     * @param header String text in header of dialog.
     * @param content String content text.
     * @param ex Exception
     */
    public static void exceptionDialog(String title, String header, String content, Exception ex) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));

        alert.showAndWait();
    }

    /**
     * Displays a confirm dialog box.
     *
     * @param title String title of dialog.
     * @param header String text in header of dialog.
     * @param content String content text.
     * @return ButtonType Returns Ok or Cancel ButtonType.
     */
    public static ButtonType confirmDialog(String title, String header, String content) {
        ButtonType ret = ButtonType.CANCEL;
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));

        Optional<ButtonType> result = alert.showAndWait();
        ret = result.get();

        return ret;
    }
}
