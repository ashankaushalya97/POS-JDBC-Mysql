package Controller;

import DB.DB;
import DTO.TableModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class POS3Controller {

    public AnchorPane MngCust;
    public JFXTextField txtID;
    public JFXTextField txtName;
    public JFXTextField txtAddress;
    public TableView<TableModel> tableCustomer;
    public TableColumn columnID;
    public TableColumn ColumnName;
    public TableColumn columnAddress;
    public JFXButton btnSave;
    public ImageView icnHome;
    int count = 0;
    public Connection connection;

    public void initialize() throws ClassNotFoundException {




        //loadTable(String id,String name,String address)

        tableCustomer.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tableCustomer.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tableCustomer.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        //loadCustomers();
        try {
            connection= DBConnection.getInstance().getConnection();
            ObservableList<TableModel> customer = tableCustomer.getItems();

            String sql = "SELECT * from customer";
            PreparedStatement pstm = connection.prepareStatement(sql);
            ResultSet rst = pstm.executeQuery();
            while (rst.next()){
                System.out.println("Iitializer");
                customer.add(new TableModel(rst.getString(1),rst.getString(2),rst.getString(3)));
            }
            tableCustomer.setItems(customer);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableCustomer.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableModel>() {
            @Override
            public void changed(ObservableValue<? extends TableModel> observable, TableModel oldValue, TableModel newValue) {

                TableModel selectedItem = tableCustomer.getSelectionModel().getSelectedItem();

                try {
                    Connection connection = null;
                    try {
                        connection= DBConnection.getInstance().getConnection();
                        String sql="select * from customer where id=?";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1,selectedItem.getId());
                        ResultSet rst = pstm.executeQuery();

                        if(rst.next()){
                            txtID.setText(rst.getString(1));
                            txtName.setText(rst.getString(2));
                            txtAddress.setText(rst.getString(3));
                            txtID.setDisable(true);
                            btnSave.setText("Update");
                        }





                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }catch (NullPointerException n){
                    return;
                }
            }
        });

    }

    @FXML
    private void playMouseExitAnimation(MouseEvent event) {
        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();
            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1);
            scaleT.setToY(1);
            scaleT.play();

            icon.setEffect(null);
        }
    }

    @FXML
    private void playMouseEnterAnimation(MouseEvent event) {
        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();


            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1.2);
            scaleT.setToY(1.2);
            scaleT.play();

            DropShadow glow = new DropShadow();
            glow.setColor(Color.CORNFLOWERBLUE);
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(20);
            icon.setEffect(glow);
        }
    }

    private void setID() {
        String ID = "";
        if (0 < DB.nextIndex && DB.nextIndex < 10) {
            ID = "C00" + DB.nextIndex;
        } else if (10 <= DB.nextIndex && DB.nextIndex < 100) {
            ID = "C0" + DB.nextIndex;
        } else if ((100 <= DB.nextIndex && DB.nextIndex < 1000)) {
            ID = "C" + DB.nextIndex;
        }
        txtID.setText(ID);

    }

    public void loadTable(String id, String name, String address) {
        //tableList.add(new TableModel("C001","Nimal","Gampaha"));
    }

    private void loadCustomers() {
        //customer.add(new Customer("C001","Nimal","Gampaha"));
//        customer.add(new Customer("C002","Kamal","Polhena"));
        //      customer.add(new Customer("C003","Ajith","Gampaha"));
//        ObservableList<TableModel> tableList= tableCustomer.getItems();
//
//        if(count==0){
//
//            tableList.add(new TableModel("C001","Nimal","Gampaha"));
//            tableList.add(new TableModel("C002","Jagath","Matara"));
//            tableList.add(new TableModel("C003","Sunil","Panadura"));
//
//        }else{
//            tableList.add(new TableModel(customer.get(count-1).getId(),customer.get(count-1).getName(),customer.get(count-1).getAddress()));
//        }
    }


    public void icnHome_OnAction(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/Dashboard.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.MngCust.getScene().getWindow());
        primaryStage.setScene(scene);
        // primaryStage.centerOnScreen();

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
    }


    public void btnSave_OnAction(ActionEvent actionEvent){

        connection= DBConnection.getInstance().getConnection();

        try{
            if(!txtName.getText().isEmpty() || !txtID.getText().isEmpty() || !txtAddress.getText().isEmpty()){

                String name =txtName.getText();

                if(name.matches("^\\b([A-Za-z.]+\\s?)+$") && txtAddress.getText().matches("^\\b[A-Za-z0-9/,\\s]+.$")){

                    if(btnSave.getText().equals("Save")){
                        String sql = "INSERT INTO customer VALUES(?,?,?)";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1,txtID.getText());
                        pstm.setString(2,txtName.getText());
                        pstm.setString(3,txtAddress.getText());
                        int affect = pstm.executeUpdate();

                        if(affect>0){
                            System.out.println("Done");
                        }else{
                            connection.rollback();
                        }

                    }else if(btnSave.getText().equals("Update")){
                        System.out.println("Update");
                        String sql = "UPDATE customer SET name=? , address=?  where id=?";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1,txtName.getText());
                        pstm.setString(2,txtAddress.getText());
                        pstm.setString(3,txtID.getText());


                        int affected= pstm.executeUpdate();

                        if (affected>0) {

                            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                    "Record updated!",
                                    ButtonType.OK);
                            Optional<ButtonType> buttonType = alert.showAndWait();

                        }else{
                            connection.rollback();
                            Alert alert = new Alert(Alert.AlertType.ERROR,
                                    "Update error!",
                                    ButtonType.OK);
                            Optional<ButtonType> buttonType = alert.showAndWait();

                        }

                        btnSave.setText("Save");

                    }

                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Invalid inputs!",
                            ButtonType.OK);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                }

            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "You have empty fields!",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();

            }
            tableCustomer.getItems().clear();
            try {
                initialize();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }catch (Throwable e){
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }finally{
            try {
                connection.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        txtID.clear();
        txtName.clear();
        txtAddress.clear();

    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {

        if(tableCustomer.getSelectionModel().isEmpty()){
            Alert alert1 = new Alert(Alert.AlertType.ERROR,
                    "Please select a customer",
                    ButtonType.OK);
            Optional<ButtonType> buttonType1= alert1.showAndWait();
            return;
        }

        connection= DBConnection.getInstance().getConnection();

        try{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure whether you want to delete this customer?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> buttonType = alert.showAndWait();
            if (buttonType.get() == ButtonType.YES) {
                TableModel selectedItem = tableCustomer.getSelectionModel().getSelectedItem();

                String sql = "DELETE from customer where id=?";
                PreparedStatement pstm = connection.prepareStatement(sql);
                pstm.setString(1,selectedItem.getId());
                int affected= pstm.executeUpdate();

                if (affected>0){
                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION,
                            "Record deleted!",
                            ButtonType.OK);
                    Optional<ButtonType> buttonType1= alert1.showAndWait();
                }
                tableCustomer.getItems().clear();
                try {
                    initialize();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }

        }catch (Throwable e){
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }finally{
            try {
                connection.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void tableCustomer_OnClicked(MouseEvent mouseEvent) {
        TableModel temp = (TableModel) tableCustomer.getSelectionModel().getSelectedItem();
        txtID.setText(temp.getId());
        txtName.setText(temp.getName());
        txtAddress.setText(temp.getAddress());
    }

    public void btnNew_OnAction(ActionEvent actionEvent) throws SQLException {
        txtID.clear();
        txtName.clear();
        txtName.clear();
        tableCustomer.getSelectionModel().clearSelection();
        txtName.setDisable(false);
        txtAddress.setDisable(false);
        txtName.requestFocus();
        btnSave.setDisable(false);

        // Generate a new id
        connection= DBConnection.getInstance().getConnection();
        String sql ="Select id from customer";
        PreparedStatement pstm =connection.prepareStatement(sql);
        ResultSet rst = pstm.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()){
            ids=rst.getString(1);

            int id = Integer.parseInt(ids.replace("C", ""));
            if (id > maxId) {
                maxId = id;
            }
        }


        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "C00" + maxId;
        } else if (maxId < 100) {
            id = "C0" + maxId;
        } else {
            id = "C" + maxId;
        }
        txtID.setText(id);
    }

    public void btnReport_OnAction(ActionEvent actionEvent) throws JRException {

        JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/reports/customer1.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        Map<String,Object> params = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,params,new JRBeanCollectionDataSource(tableCustomer.getItems()));

        JasperViewer.viewReport(jasperPrint,false);


    }
}
