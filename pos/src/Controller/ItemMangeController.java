package Controller;

import DB.DB;
import DTO.ItemManageTM;
import DTO.Items;
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

public class ItemMangeController {
    public JFXTextField txtItemCode;
    public JFXTextField txtItemDescription;
    public JFXTextField txtQtyOnHand;
    public JFXTextField txtUnitPrice;
    public TableView<ItemManageTM> tableItemDetails;
    public AnchorPane itemManageFrame;
    public JFXButton btnSave;
    public JFXButton btnDelete;
    public ImageView icnHome;
    public Connection connection;

    public void initialize() {

        tableItemDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        tableItemDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("itemDescription"));
        tableItemDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        tableItemDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));


        try {
            connection= DBConnection.getInstance().getConnection();
            ObservableList<ItemManageTM> items = tableItemDetails.getItems();

            String sql = "SELECT * from item";
            PreparedStatement pstm = connection.prepareStatement(sql);
            ResultSet rst = pstm.executeQuery();
            while (rst.next()){
                System.out.println("Iitializer");
                items.add(new ItemManageTM(rst.getString(1),rst.getString(2),Integer.parseInt(rst.getString(3)),Integer.parseInt(rst.getString(4))));
            }
            tableItemDetails.setItems(items);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableItemDetails.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemManageTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemManageTM> observable, ItemManageTM oldValue, ItemManageTM newValue) {

                ItemManageTM selectedItem = tableItemDetails.getSelectionModel().getSelectedItem();

                try {
                    Connection connection = null;
                    try {
                        connection= DBConnection.getInstance().getConnection();
                        String sql="select * from item where itemCode=?";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1,selectedItem.getItemCode());
                        ResultSet rst = pstm.executeQuery();

                        if(rst.next()){
                            txtItemCode.setText(rst.getString(1));
                            txtItemDescription.setText(rst.getString(2));
                            txtQtyOnHand.setText(rst.getString(3));
                            txtUnitPrice.setText(rst.getString(4));
                            txtItemCode.setDisable(true);
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

    public void btnSave_OnAction(ActionEvent actionEvent) {

        connection= DBConnection.getInstance().getConnection();

        try{

            if(txtItemDescription.getText().isEmpty() || txtQtyOnHand.getText().isEmpty() || txtUnitPrice.getText().isEmpty()){

                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "You have empty fields!",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();

                return;
            }else{

                if(txtItemDescription.getText().matches("^\\b([A-Za-z]+\\s*)+$") && txtUnitPrice.getText().matches("^\\d+$") && txtQtyOnHand.getText().matches("^\\d+$")){

                    if(btnSave.getText().equals("Save")){

                        System.out.println("Save");
                        String sql = "INSERT INTO item VALUES(?,?,?,?)";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1,txtItemCode.getText());
                        pstm.setString(2,txtItemDescription.getText());
                        pstm.setString(3,txtQtyOnHand.getText());
                        pstm.setString(4,txtUnitPrice.getText());
                        int affect = pstm.executeUpdate();

                        if(affect>0){
                            System.out.println("Done");
                        }

                    }else if(btnSave.getText().equals("Update")){
                        System.out.println("Update");
                        String sql = "UPDATE item SET description=? , qtyOnHand=?, unitPrice=?  where itemCode=?";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1,txtItemDescription.getText());
                        pstm.setString(2,txtQtyOnHand.getText());
                        pstm.setString(3,txtUnitPrice.getText());
                        pstm.setString(4,txtItemCode.getText());


                        int affected= pstm.executeUpdate();

                        if (affected>0) {

                            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                    "Record updated!",
                                    ButtonType.OK);
                            Optional<ButtonType> buttonType = alert.showAndWait();

                        }else{
                            Alert alert = new Alert(Alert.AlertType.ERROR,
                                    "Update error!",
                                    ButtonType.OK);
                            Optional<ButtonType> buttonType = alert.showAndWait();

                        }

                        btnSave.setText("Save");

                    }


                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Invalid input!",
                            ButtonType.OK);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                }
            }
            tableItemDetails.getItems().clear();

            initialize();
        }catch (Throwable e){
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        txtItemCode.clear();
        txtItemDescription.clear();
        txtUnitPrice.clear();
        txtQtyOnHand.clear();

    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {

        if(tableItemDetails.getSelectionModel().isEmpty()){
            Alert alert1 = new Alert(Alert.AlertType.ERROR,
                    "Please select a item",
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

                ItemManageTM selectedItem = tableItemDetails.getSelectionModel().getSelectedItem();

                String sql = "DELETE from item where itemCode=?";
                PreparedStatement pstm = connection.prepareStatement(sql);
                pstm.setString(1,selectedItem.getItemCode());
                int affected= pstm.executeUpdate();

                if (affected>0){
                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION,
                            "Record deleted!",
                            ButtonType.OK);
                    Optional<ButtonType> buttonType1= alert1.showAndWait();
                }
                tableItemDetails.getItems().clear();
                initialize();


            }
        }catch (Throwable e){
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void icnHome_OnAction(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/Dashboard.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) this.itemManageFrame.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
    }

    public void btnNewItem_OnAction(ActionEvent actionEvent) throws SQLException {
        txtItemCode.clear();
        txtItemDescription.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        tableItemDetails.getSelectionModel().clearSelection();
        txtItemCode.setDisable(false);
        txtItemDescription.setDisable(false);
        txtQtyOnHand.setDisable(false);
        txtUnitPrice.setDisable(false);
        txtItemDescription.requestFocus();
        btnSave.setDisable(false);

        // Generate a new id
        connection= DBConnection.getInstance().getConnection();
        String sql ="Select itemCode from item";
        PreparedStatement pstm =connection.prepareStatement(sql);
        ResultSet rst = pstm.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()){
            ids=rst.getString(1);

            int id = Integer.parseInt(ids.replace("I", ""));
            if (id > maxId) {
                maxId = id;
            }
        }


        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "I00" + maxId;
        } else if (maxId < 100) {
            id = "I0" + maxId;
        } else {
            id = "I" + maxId;
        }
        txtItemCode.setText(id);
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

    public void btnReport_OnAction(ActionEvent actionEvent) throws JRException {

        JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/reports/items.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        ObservableList<ItemManageTM> table = tableItemDetails.getItems();
        ObservableList<Items> items = FXCollections.observableArrayList();

        for (int i = 0; i <table.size() ; i++) {
            items.add(new Items(table.get(i).getItemCode(),table.get(i).getItemDescription(),(table.get(i).getQtyOnHand()),(table.get(i).getUnitPrice())));
        }
        Map<String,Object> params = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,params,new JRBeanCollectionDataSource(items));

        JasperViewer.viewReport(jasperPrint,false);


    }
}
