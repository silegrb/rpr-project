package ba.unsa.etf.rpr.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class DepartmentController implements Initializable {

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

    public TextField fieldDepartmentName = new TextField();
    public ChoiceBox<String> cbLocations = new ChoiceBox<>();
    public ChoiceBox<String> cbManagers = new ChoiceBox<>();
    public Button okBtn = new Button();
    private Department currentDepartment;

    public DepartmentController( Department currentDepartment ){
        this.currentDepartment = currentDepartment;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        okBtn.setDisable(true);
        if( currentDepartment != null ){
            okBtn.setDisable(false);
            fieldDepartmentName.setText( currentDepartment.getName() );
            if( currentDepartment.getManager() != null )
                cbManagers.setValue( currentDepartment.getManager().getFirstName() + " " + currentDepartment.getManager().getLastName() );
            if( currentDepartment.getLocation() != null )
                cbLocations.setValue( currentDepartment.getLocation().getStreetAddress() + " (" + currentDepartment.getLocation().getPostalCode() + ")" );
        }

        //Lets add items to those two choice boxes.
        ObservableList<String> locations = FXCollections.observableArrayList();
        for (Location l: dao.getLocations())
            locations.add( l.getStreetAddress() + " (" + l.getPostalCode() + ")" );
        cbLocations.setItems( locations );

        ObservableList<String> managers = FXCollections.observableArrayList();
        for (Employee e: dao.getEmployees())
            managers.add( e.getFirstName() + " " + e.getLastName() );
        cbManagers.setItems( managers );

        fieldDepartmentName.textProperty().addListener((observable, oldValue, newValue) -> {
            if( newValue.isEmpty() ){
                fieldDepartmentName.getStyleClass().removeAll("fieldValid","fieldInvalid","controllerFields");
                fieldDepartmentName.getStyleClass().add("controllerFields");
                okBtn.setDisable(true);
            }
            else{
                fieldDepartmentName.getStyleClass().removeAll("fieldValid","fieldInvalid","controllerFields");
                fieldDepartmentName.getStyleClass().add("fieldValid");
                okBtn.setDisable(false);
            }
        });

        cbListeners(cbLocations);

        cbListeners(cbManagers);

    }

    private void cbListeners(ChoiceBox<String> cbLocations) {
        cbLocations.valueProperty().addListener((observable, oldValue, newValue) -> {
            if( newValue != null ){
                cbLocations.getStyleClass().removeAll("fieldInvalid","controllerFields","fieldValid");
                cbLocations.getStyleClass().add("fieldValid");
            }
            else{
                cbLocations.getStyleClass().removeAll("fieldInvalid","controllerFields","fieldValid");
                cbLocations.getStyleClass().add("controllerFields");
            }
        });
    }


    private ObservableList<String> getErrors(){
        ObservableList<String> errors = FXCollections.observableArrayList();
        if( fieldDepartmentName.getText().isEmpty() ) errors.add("Department name field is empty");
        return errors;
    }

    private Department getDepartmentFromWindow(){
        int index;
        if( currentDepartment != null ) index = currentDepartment.getId();
        else index = dao.nextIndex("Department");

        Location location = null;
        if( cbLocations.getValue() != null ) {
            for (Location l : dao.getLocations()) {
                String tempLocation = l.getStreetAddress() + " (" + l.getPostalCode() + ")";
                if (tempLocation.equals(cbLocations.getValue())) {
                    location = l;
                    break;
                }
            }
        }

        Employee manager = null;
        if( cbManagers.getValue() != null ) {
            for (Employee e : dao.getEmployees()) {
                String tempManager = e.getFirstName() + " " + e.getLastName();
                if (tempManager.equals(cbManagers.getValue())) {
                    manager = e;
                    break;
                }
            }
        }

        return new Department( index, fieldDepartmentName.getText(), location, manager );

    }

    public void clickOkBtn(javafx.event.ActionEvent actionEvent) throws SQLException {
        if( currentDepartment == null ){
            dao.addDepartment( getDepartmentFromWindow() );
            fieldDepartmentName.getScene().getWindow().hide();
        }
        else{
            dao.changeDepartment( getDepartmentFromWindow() );
            fieldDepartmentName.getScene().getWindow().hide();
        }
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

    public void clickCancelBtn(ActionEvent actionEvent){
        fieldDepartmentName.getScene().getWindow().hide();
    }

    public void clickOnCancelLocationBtn(ActionEvent actionEvent){
        cbLocations.getStyleClass().removeAll("fieldValid","fieldInvalid","controllerFields");
        cbLocations.getStyleClass().add("controllerFields");
        cbLocations.setValue(null);
    }

    public void clickOnCancelManagerBtn(ActionEvent actionEvent){
        cbManagers.getStyleClass().removeAll("fieldValid","fieldInvalid","controllerFields");
        cbManagers.getStyleClass().add("controllerFields");
        cbManagers.setValue(null);
    }

}
