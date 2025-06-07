package pl.frot.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.frot.model.SummaryMachine;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SummaryMachine summaryMachine = new SummaryMachine();
        summaryMachine.run();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main.fxml"));

        fxmlLoader.setControllerFactory(controllerClass -> {
            if (controllerClass == MainController.class) {
                MainController controller = new MainController();
                controller.setSummaryMachine(summaryMachine);
                return controller;
            } else {
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        stage.setTitle("KSR: lingwistyczne podsumowania baz danych");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
