package ba.unsa.etf.rpr.project;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader( getClass().getResource("/FXML/loginWindow.fxml") );
        LoginWindowController lwc = new LoginWindowController();
        loader.setController( lwc );
        Parent root = loader.load();
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 220, 140));
        primaryStage.show();
        primaryStage.setOnHidden(event -> {
            if( lwc.loginSuccess() ) {
                Stage secondaryStage = new Stage();
                FXMLLoader secondaryLoader = new FXMLLoader(getClass().getResource("/FXML/humanResourcesController.fxml"));
                HumanResourcesController hrc = new HumanResourcesController( lwc.getUsernameField().getText() );

                secondaryLoader.setController(hrc);
                Parent secondaryRoot = null;
                try {
                    secondaryRoot = secondaryLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                secondaryStage.setTitle("Human Resources");
                secondaryStage.setResizable(false);
                secondaryStage.setScene(new Scene(secondaryRoot, 850, 650));
                secondaryStage.show();
                secondaryStage.setOnHidden(new EventHandler<>() {
                    @Override
                    public void handle(WindowEvent event) {
                        hrc.getChangingColorTimer().cancel();
                    }
                });
            }
        });


    }


    public static void main(String[] args) {
        launch(args);
    }
}

