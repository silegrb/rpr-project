package ba.unsa.etf.rpr.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class UsernameController implements Initializable {
    HumanResourcesDAO dao;

    {
        try {
            dao = HumanResourcesDAO.getInstance();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TextField currentUsernameField;
    public TextField newUsernameField;
    public TextField confirmNewUsernameField;
    public Button okBtn = new Button();
    private String password;
    private String oldUsername;
    private boolean usernameChanged = false;

    public UsernameController(String password, String oldUsername) {
        this.password = password;
        this.oldUsername = oldUsername;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        okBtn.setDisable(true);

        currentUsernameField.textProperty().addListener((observable, oldValue, newValue) -> {

            if( newValue.equals("") ){
                currentUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                currentUsernameField.getStyleClass().add("usernameFields");
                okBtn.setDisable(true);
            }

            else if( newValue.equals( oldUsername )  ){
                currentUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                currentUsernameField.getStyleClass().add("fieldValid");
                if( newUsernameField.getStyleClass().contains("fieldValid") && confirmNewUsernameField.getStyleClass().contains("fieldValid") )
                    okBtn.setDisable(false);
            }
            else{
                currentUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                currentUsernameField.getStyleClass().add("fieldInvalid");
                okBtn.setDisable(true);

            }
        });

        newUsernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean usernameAlreadyExists = false;
            for (Administrator a: dao.getAdministrators())
                if( !newValue.equals(oldUsername) && newValue.equals( a.getUsername() ) )
                    usernameAlreadyExists = true;
            if( !newValue.equals( "" ) && !usernameAlreadyExists ){
                newUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                newUsernameField.getStyleClass().add("fieldValid");
                if( !confirmNewUsernameField.getText().isEmpty() ) {
                    if (confirmNewUsernameField.getText().equals(newValue)) {
                        confirmNewUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","passwordFields");
                        confirmNewUsernameField.getStyleClass().add("fieldValid");
                    } else {
                        confirmNewUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","passwordFields");
                        confirmNewUsernameField.getStyleClass().add("fieldInvalid");
                    }
                }
                if( currentUsernameField.getStyleClass().contains("fieldValid") && confirmNewUsernameField.getStyleClass().contains("fieldValid") )
                    okBtn.setDisable(false);
            }
            else if( usernameAlreadyExists ){
                newUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                newUsernameField.getStyleClass().add("fieldInvalid");
                okBtn.setDisable(true);
            }
            else{
                newUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                newUsernameField.getStyleClass().add("usernameFields");
                if( !confirmNewUsernameField.getText().isEmpty() ){
                    confirmNewUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                    confirmNewUsernameField.getStyleClass().add("fieldInvalid");
                }
                okBtn.setDisable(true);
            }
        });

        confirmNewUsernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean usernameAlreadyExists = false;
            for (Administrator a: dao.getAdministrators())
                if( !newValue.equals(oldUsername) && newValue.equals( a.getUsername() ) )
                    usernameAlreadyExists = true;
            if( newValue.equals("") ){
                confirmNewUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                confirmNewUsernameField.getStyleClass().add("usernameFields");
                okBtn.setDisable(true);
            }
            else if( !usernameAlreadyExists && newValue.equals( newUsernameField.getText() ) ){
                confirmNewUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                confirmNewUsernameField.getStyleClass().add("fieldValid");
                if( currentUsernameField.getStyleClass().contains("fieldValid") && newUsernameField.getStyleClass().contains("fieldValid"))
                    okBtn.setDisable(false);
            }
            else{
                confirmNewUsernameField.getStyleClass().removeAll("fieldInvalid","fieldValid","usernameFields");
                confirmNewUsernameField.getStyleClass().add("fieldInvalid");
                okBtn.setDisable(true);
            }
        });
    }

    public void clickOkBtn(ActionEvent actionEvent) throws SQLException {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Updating username");
        a.setHeaderText("Are you sure you want to change your username?");
        Optional<ButtonType> result = a.showAndWait();
        if( result.get() == ButtonType.OK ) {
            int wantedId = -1;
            for( int i = 0; i < dao.getAdministrators().size(); i++ )
                if(dao.getAdministrators().get(i).getPassword().equals(password)){
                    wantedId = i;
                    break;
                }
            dao.getAdministrators().get(wantedId).setUsername(  newUsernameField.getText() );
            dao.changeCurrentUser(newUsernameField.getText(), password);
            ArrayList<Integer> indexes = new ArrayList<>();
            for ( Login l : dao.getLogins())
                if( l.getUser().equals( oldUsername ) )
                    indexes.add(l.getId());

            dao.changeLogin( indexes, newUsernameField.getText() );
            usernameChanged = true;
            confirmNewUsernameField.getScene().getWindow().hide();
        }
        confirmNewUsernameField.getScene().getWindow().hide();

    }

    public void clickCancelBtn(ActionEvent actionEvent){
        currentUsernameField.getScene().getWindow().hide();
    }
    
    public void clickErrorReportBtn(ActionEvent actionEvent){
        Stage secondaryStage = new Stage();
        FXMLLoader secondaryLoader = new FXMLLoader(getClass().getResource("/FXML/errorReportWindow.fxml"));
        ErrorReportController erc = new ErrorReportController( getErrors() );
        secondaryLoader.setController(erc);
        Parent secondaryRoot = null;
        try {
            secondaryRoot = secondaryLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        secondaryStage.setTitle("Error report");
        secondaryStage.setResizable(false);
        secondaryStage.initModality(Modality.APPLICATION_MODAL);
        secondaryStage.setScene(new Scene(secondaryRoot, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        secondaryStage.show();
    }

    private ObservableList<String> getErrors() {
        ObservableList<String> errors = FXCollections.observableArrayList();
        if( currentUsernameField.getText().isEmpty() ) errors.add("Current username field is empty");
        if( currentUsernameField.getStyleClass().contains("fieldInvalid") ) errors.add( "Wrong current username" );
        if( newUsernameField.getText().isEmpty() ) errors.add("New username field is empty");
        if( newUsernameField.getStyleClass().contains("fieldInvalid") ) errors.add("This username is already in use");
        if( confirmNewUsernameField.getText().isEmpty() ) errors.add("Confirm new username field is empty");
        if( !newUsernameField.getStyleClass().contains("fieldInvalid") && confirmNewUsernameField.getStyleClass().contains("fieldInvalid") ) errors.add("Confirmation failed");
        return errors;
    }

    public boolean isUsernameChanged() {
        return usernameChanged;
    }

    public TextField getNewUsernameField() {
        return newUsernameField;
    }
}
