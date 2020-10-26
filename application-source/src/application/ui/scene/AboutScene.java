package application.ui.scene;

import application.model.ApplicationSettings;
import application.ui.ApplicationScene;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Scene for information about application.
 *
 * @author Dizzy
 */
public class AboutScene extends ApplicationScene {

    private BorderPane root = new BorderPane();

    /**
     * Create scene.
     *
     */
    public AboutScene() {
        VBox content = new VBox();
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("EV3 Neural Control 1.3.3");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        Label prolog = new Label("The application has been made for a thesis Control of autonomous robot using neural networks (in Czech) at the University of Ostrava, Department of Informatics and Computers, Czech Republic.");
        prolog.setTextAlignment(TextAlignment.JUSTIFY);
        prolog.setWrapText(true);
        content.getChildren().add(prolog);
        
        Label author = new Label("Author: Bc. Adam Bartoň (barton.adam@email.cz)");
        author.setWrapText(true);
        content.getChildren().add(author);
        
        Label errors = new Label("If you find an error, please do not hesitate to contact me.");
        errors.setWrapText(true);
        content.getChildren().add(errors);
        
        Label howToTitle = new Label("How to create an experiment");
        howToTitle.setFont(Font.font(18));
        content.getChildren().add(howToTitle);
        
        Label howTo = new Label("To create an experiment correctly, the following procedure is recommended:\n\n"
                + "1. The definition of sensors scanned interval decomposition and determine the layout of inputs for the ANN.\n"
                + "2. Setting the generation parametrs of training set and its generation.\n"
                + "3. The filtration of training set using ART1 (optional, depending on the size of the TS).\n"
                + "4. Adjustment of the elements of the input vector of the TS to the desired speed motors (with regard to the forward or reverse) between 0 and " + ApplicationSettings.maxEV3Speed + " degrees / sec.\n"
                + "5. Creating the appropriate neural network (make sure the number of input neurons is the same as number of inputs of generated TS and choose the corresponding number of hidden neurons).\n"
                + "6. The adaptation of created network using the (filtered) training set.\n"
                + "7. Connect EV3 robot to the application (if it is not already done).\n"
                + "8. The creation / upload of the experiment and NN inputs setup depending to the sensor values (it is necessary to realize that the rows of the table represent individual inputs of the NN and measured values is in range 0 to 100 cm). Save the experiment.\n"
                + "9. Run the experiment.");
        howTo.setTextAlignment(TextAlignment.JUSTIFY);
        howTo.setWrapText(true);
        content.getChildren().add(howTo);
        
        root.setCenter(scrollPane);
        scene = new Scene(root, ApplicationSettings.POPUP_WINDOW_WIDTH, ApplicationSettings.POPUP_WINDOW_HEIGHT);
    }
}
