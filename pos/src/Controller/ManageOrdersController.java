package Controller;

import DB.DB;
import DTO.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
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
import javafx.scene.control.Label;
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
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class ManageOrdersController {
    public AnchorPane ManagerOrders;
    public JFXTextField txtOrderID;
    public JFXTextField txtDate;
    public JFXTextField txtCustomerName;
    public JFXComboBox comboCustomerID;
    public JFXTextField txtItemDesc;
    public JFXComboBox comboItemCode;
    public JFXTextField txtQtyOnHand;
    public JFXTextField txtUnitPrice;
    public JFXTextField txtQty;
    public TableView<ManageOrdersTM> tableOrderDetails;
    public int total = 0;
    public Label labTotal;
    public JFXButton btnSave;
    public ImageView icnHome;
    public Connection connection;
    public ArrayList<ItemManageTM> items = new ArrayList<>();
    public JFXButton btnPlaceOrder;

    public void initialize() {

        items.clear();
        System.out.println("Initialize");
        tableOrderDetails.getItems().clear();
        tableOrderDetails.refresh();


        //Date
        SimpleDateFormat fomatter = new SimpleDateFormat("dd/MM/yy");
        Date date = new Date();
        txtDate.setText(fomatter.format(date));
        txtDate.setDisable(true);

        if (txtOrderID.getText().isEmpty()) {
            try {
                orderID();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //load items
        items.clear();
        try {
            connection= DBConnection.getInstance().getConnection();
            PreparedStatement pstm = connection.prepareStatement("select * from item");
            ResultSet rst = pstm.executeQuery();

            while (rst.next()){
                items.add(new ItemManageTM(rst.getString(1),rst.getString(2),rst.getInt(3),rst.getInt(4)));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


        comboCustomerID.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {

                try {
                    connection= DBConnection.getInstance().getConnection();

                    if (comboCustomerID.getSelectionModel().getSelectedItem() != null) {
                        Object selectedItem = comboCustomerID.getSelectionModel().getSelectedItem();

                        PreparedStatement pstm = connection.prepareStatement("select name from customer where id=?");
                        pstm.setString(1,selectedItem.toString());
                        ResultSet rst = pstm.executeQuery();

                        if(rst.next()){
                            txtCustomerName.setText(rst.getString(1));
                            txtCustomerName.setDisable(true);
                            comboCustomerID.setDisable(true);
                        }
                    }



                } catch (SQLException e) {
                    e.printStackTrace();
                }

//                if(comboItemCode.getSelectionModel().getSelectedItem().equals(null)){
//                    return;
//                }

            }
        });

        comboItemCode.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {

                try {
                    connection= DBConnection.getInstance().getConnection();

                    Object selectedItem = comboItemCode.getSelectionModel().getSelectedItem();
                    //ObservableList<ItemManageTM> items = FXCollections.observableList(DB.items);
                    String ID = (String) selectedItem;
                    if (comboItemCode.getSelectionModel().isEmpty()) {
                        return;
                    } else {

                        for (int i = 0; i < items.size(); i++) {
                            if(items.get(i).getItemCode().equals(selectedItem)){
                                if(items.get(i).getQtyOnHand()>0){
                                    txtQtyOnHand.setText(String.valueOf(items.get(i).getQtyOnHand()));
                                    txtQtyOnHand.setDisable(true);
                                }else{
                                    Alert alert = new Alert(Alert.AlertType.ERROR,
                                            "Out of stock!",
                                            ButtonType.OK);
                                    Optional<ButtonType> buttonType = alert.showAndWait();
                                    return;
                                }
                            }
                        }

                        PreparedStatement pstm = connection.prepareStatement("select * from item where itemCode=?");
                        pstm.setString(1,selectedItem.toString());
                        ResultSet rst = pstm.executeQuery();

                        if(rst.next()){

                                txtItemDesc.setText(rst.getString(2));
                                //txtQtyOnHand.setText(rst.getString(3));
                                txtUnitPrice.setText(rst.getString(4));
                                txtItemDesc.setDisable(true);
                                //txtQtyOnHand.setDisable(true);
                                txtUnitPrice.setDisable(true);

                        }


                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }



            }
        });
        tableOrderDetails.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ManageOrdersTM>() {
            @Override
            public void changed(ObservableValue<? extends ManageOrdersTM> observable, ManageOrdersTM oldValue, ManageOrdersTM newValue) {
                if (tableOrderDetails.getSelectionModel().isEmpty()) {
                    return;
                }

                btnSave.setText("Update");
                ManageOrdersTM selectedItem = tableOrderDetails.getSelectionModel().getSelectedItem();
                //   ObservableList<ManageOrdersTM> orders = tableOrderDetails.getItems();
                ObservableList<ItemManageTM> items = FXCollections.observableList(DB.items);
                comboItemCode.getSelectionModel().select(selectedItem.getItemCode());
                txtItemDesc.setText(selectedItem.getItemDescription());
                for (int i = 0; i < items.size(); i++) {
                    if (selectedItem.getItemCode().equals(items.get(i).getItemCode())) {
                        txtQtyOnHand.setText(Integer.toString(items.get(i).getQtyOnHand()));
                    }
                }
                txtUnitPrice.setText(Integer.toString(selectedItem.getUnitPrice()));
                txtQty.setText(Integer.toString(selectedItem.getQty()));
                txtItemDesc.setDisable(true);
                txtUnitPrice.setDisable(true);
                txtQtyOnHand.setDisable(true);
            }
        });


        //set table columns
        tableOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        tableOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("itemDescription"));
        tableOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tableOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tableOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
        tableOrderDetails.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("delete"));

        ObservableList<ManageOrdersTM> orders = FXCollections.observableList(DB.orders);
        tableOrderDetails.setItems(orders);

        //customers
        comboCustomerID.getItems().clear();
        ObservableList custID = comboCustomerID.getItems();
        try {
            PreparedStatement pstm = connection.prepareStatement("select id from customer");
            ResultSet rst = pstm.executeQuery();

            while(rst.next()){
                custID.add(rst.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //items
        comboItemCode.getItems().clear();
        ObservableList itemID = comboItemCode.getItems();
        for (int i = 0; i < items.size(); i++) {
            itemID.add(items.get(i).getItemCode());
        }
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

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent)  {

        connection= DBConnection.getInstance().getConnection();

        try{


            ObservableList<ManageOrdersTM> orders = tableOrderDetails.getItems();

            PreparedStatement pstm =connection.prepareStatement("INSERT INTO orders VALUES (?,?,?)");
            pstm.setString(1,txtOrderID.getText());
            pstm.setString(2,txtDate.getText());
            pstm.setString(3, (String) comboCustomerID.getSelectionModel().getSelectedItem());
            int affected = pstm.executeUpdate();

            if(affected>0){
                System.out.println("Order inserted");
            }else{
                connection.rollback();
            }


            for (int i = 0; i < orders.size(); i++) {

                PreparedStatement pstm1 = connection.prepareStatement("INSERT INTO orderDetail values (?,?,?,?)");
                pstm1.setString(1,txtOrderID.getText());
                pstm1.setString(2,orders.get(i).getItemCode());
                pstm1.setString(3, String.valueOf(orders.get(i).getQty()));
                pstm1.setString(4, String.valueOf(orders.get(i).getUnitPrice()));
                int affected1 = pstm1.executeUpdate();

                if(affected1>0){
                    System.out.println("Order detail inseted");
                }else{
                    connection.rollback();
                }

                PreparedStatement pstm2 = connection.prepareStatement("select qtyOnHand from item where itemCode=?");
                pstm2.setString(1,orders.get(i).getItemCode());
                ResultSet rst = pstm2.executeQuery();

                if(rst.next()){

                    //int newQty = rst.getInt(1)-Integer.parseInt(txtQty.getText());
                    int newQty=0;
                    for (int j = 0; j <items.size() ; j++) {
                        if(items.get(j).getItemCode().equals(orders.get(i).getItemCode())){
                            newQty=items.get(j).getQtyOnHand();
                        }
                    }

                    PreparedStatement pstm3 = connection.prepareStatement("UPDATE item set qtyOnHand=? where itemCode=?");
                    pstm3.setString(1, String.valueOf(newQty));
                    pstm3.setString(2, orders.get(i).getItemCode());
                    int affected2 = pstm3.executeUpdate();

                    if(affected2>0){
                        System.out.println("Qty on hand updated!");
                    }else{
                        connection.rollback();
                    }
                }
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "You have successfully placed the order.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();




            JasperDesign mainjasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/reports/placeOrder.jrxml"));
            JasperDesign subjasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/reports/orderSub.jrxml"));



            JasperReport mainjasperReport =  JasperCompileManager.compileReport(mainjasperDesign);
            JasperReport subjasperReport =  JasperCompileManager.compileReport(subjasperDesign);

            //JasperReport jasperReport = (JasperReport) JRLoader.loadObject(this.getClass().getResourceAsStream("/reports/placeOrder.jasper"));
            Map<String, Object> params = new HashMap<>();
            params.put("orderID",txtOrderID.getText());
            params.put("subReport",subjasperReport);

//        JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/reports/placeOrder.jrxml"));
//        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            JasperPrint jasperPrint = JasperFillManager.fillReport(mainjasperReport, params, DBConnection.getInstance().getConnection());
            JasperViewer.viewReport(jasperPrint, false);

            orderID();
            initialize();
            comboCustomerID.setDisable(false);
            clearALL();
            return;

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

    private void orderID() throws SQLException {
        // Generate a new id
        connection= DBConnection.getInstance().getConnection();
        String sql ="Select orderId from orders";
        PreparedStatement pstm =connection.prepareStatement(sql);
        ResultSet rst = pstm.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()){
            ids=rst.getString(1);

            int id = Integer.parseInt(ids.replace("OD", ""));
            if (id > maxId) {
                maxId = id;
            }
        }


        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "OD00" + maxId;
        } else if (maxId < 100) {
            id = "OD0" + maxId;
        } else {
            id = "OD" + maxId;
        }
        txtOrderID.setText(id);
    }

    void clearALL() {
        btnNewOrder_OnAction(null);
        txtCustomerName.clear();
        tableOrderDetails.getItems().clear();
        labTotal.setText("0");
        comboCustomerID.getSelectionModel().clearSelection();
    }

    public void btnNewOrder_OnAction(ActionEvent actionEvent) {

        tableOrderDetails.getSelectionModel().clearSelection();
        //txtCustomerName.clear();
        btnSave.setText("Add to cart");
        txtItemDesc.clear();
        // txtOrderID.clear();
        txtQty.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        comboItemCode.getSelectionModel().clearSelection();
        //comboCustomerID.setDisable(true);
        comboCustomerID.requestFocus();


    }

    public void icnHome_OnAction(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/Dashboard.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) this.ManagerOrders.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
        tableOrderDetails.getItems().clear();


    }

    public void btnSave_OnAction(ActionEvent actionEvent) throws SQLException {

        connection= DBConnection.getInstance().getConnection();
        ObservableList<ManageOrdersTM> orders = tableOrderDetails.getItems();
        //ObservableList<ItemManageTM> items = FXCollections.observableList(DB.items);

        if (btnSave.getText().equals("Add to cart")) {



            if (txtQty.getText().equals(null) || txtQty.getText().equals("0") || !txtQty.getText().matches("^\\d+$")) {

                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Please add valid quontity!",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();
                return;

            } else if (comboItemCode.getSelectionModel().equals(null) || comboCustomerID.getSelectionModel().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Please select the customer ID and item ID",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();
                return;
            } else if ( txtQtyOnHand.getText().equals(null) || txtQty.getText().equals(null) || Integer.parseInt(txtQtyOnHand.getText()) < Integer.parseInt(txtQty.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Out of stock!",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();
                return;
            } else {
                //ObservableList<ManageOrdersTM> orders=tableOrderDetails.getItems();
                int qty = Integer.parseInt(txtQty.getText());
                int uniPrice = Integer.parseInt(txtUnitPrice.getText());


                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i).getItemCode().equals(comboItemCode.getSelectionModel().getSelectedItem())) {
                        int newQty = orders.get(i).getQty() + Integer.parseInt(txtQty.getText());
                        orders.get(i).setQty(newQty);
                        int unitPrice = Integer.parseInt(txtUnitPrice.getText());
                        int newTotal = newQty * unitPrice;
                        orders.get(i).setTotal(newTotal);
                        tableOrderDetails.refresh();
                        System.out.println("Done editing.");
                        int finalTotal = 0;
                        for (int j = 0; j < orders.size(); j++) {
                            finalTotal += orders.get(i).getTotal();
                        }
                        labTotal.setText(Integer.toString(finalTotal));
                        for (int j = 0; j < items.size(); j++) {
                            if (items.get(j).getItemCode().equals(comboItemCode.getSelectionModel().getSelectedItem())) {
                                int qtyOH = items.get(j).getQtyOnHand();
                                items.get(j).setQtyOnHand(qtyOH - newQty);

                            }
                        }
                        return;

                    }
                }

                total = (qty * uniPrice);
                JFXButton button = new JFXButton("Delete");
                String itemCode = (String) comboItemCode.getSelectionModel().getSelectedItem();
                ManageOrdersTM object = new ManageOrdersTM((String) comboItemCode.getSelectionModel().getSelectedItem(), txtItemDesc.getText(), Integer.parseInt(txtQty.getText()), Integer.parseInt(txtUnitPrice.getText()), total, button);
                orders.add(object);

                button.setOnAction(event -> {
                    btnTableDelete_OnAction(object);
                });
                for (int j = 0; j < items.size(); j++) {
                    if (items.get(j).getItemCode().equals(comboItemCode.getSelectionModel().getSelectedItem())) {
                        int qtyOH = items.get(j).getQtyOnHand();
                        items.get(j).setQtyOnHand(qtyOH - qty);
                    }
                }

                int finalTotal = 0;
                for (int i = 0; i < orders.size(); i++) {
                    finalTotal += orders.get(i).getTotal();
                }
                labTotal.setText(Integer.toString(finalTotal));
            }


        } else {
            System.out.println("Update");
            ManageOrdersTM item = tableOrderDetails.getSelectionModel().getSelectedItem();
            String ID = (String) comboCustomerID.getSelectionModel().getSelectedItem();
            ObservableList<ManageOrdersTM> table = tableOrderDetails.getItems();
//            for (int i = 0; i <orders.size() ; i++) {
//                if(item.getItemCode().equals(orders.get(i).getItemCode())){
//                    orders.get(i).setQty(Integer.parseInt(txtQty.getText()));
//                    int qty=orders.get(i).getQty();
//                    int unitPrice=orders.get(i).getUnitPrice();
//                    orders.get(i).setTotal(qty*unitPrice);
//
//                    int finalTotal=0;
//                    for (int j = 0; j <orders.size() ; j++) {
//                        finalTotal+=orders.get(i).getTotal();
//                    }
//                    labTotal.setText(Integer.toString(finalTotal));
//                    return;
//                }
//            }

            for (int i = 0; i < table.size(); i++) {
                if (item.getItemCode().equals(orders.get(i).getItemCode())) {
                    table.get(i).setQty(Integer.parseInt(txtQty.getText()));
                    int qty = table.get(i).getQty();
                    int unitPrice = table.get(i).getUnitPrice();
                    table.get(i).setTotal(qty * unitPrice);

                    int finalTotal = 0;
                    for (int j = 0; j < orders.size(); j++) {
                        finalTotal += table.get(i).getTotal();
                    }
                    labTotal.setText(Integer.toString(finalTotal));


                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Successfully updated the data..",
                            ButtonType.OK);
                    Optional<ButtonType> buttonType = alert.showAndWait();

                    for (int k = 0; k < items.size(); k++) {
                        if (items.get(k).getItemCode().equals(item.getItemCode())) {
                            int qtyOH = items.get(k).getQtyOnHand() + item.getQty();
                            int QTY = Integer.parseInt(txtQty.getText());
                            items.get(k).setQtyOnHand(qtyOH - QTY);
                        }
                    }
                    tableOrderDetails.refresh();
                    return;
                }
            }

        }
        btnNewOrder_OnAction(null);
    }

    public void btnTableDelete_OnAction(ManageOrdersTM object) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure whether you want to delete this customer?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();
        ObservableList<ItemManageTM> items = FXCollections.observableList(DB.items);
        if (buttonType.get() == ButtonType.YES) {
            int total = object.getTotal();
            int finalTotal = Integer.parseInt(labTotal.getText());
            tableOrderDetails.getItems().remove(object);
            labTotal.setText(Integer.toString(finalTotal - total));


            for (int j = 0; j < items.size(); j++) {
                if (items.get(j).getItemCode().equals(object.getItemCode())) {
                    int qOH = items.get(j).getQtyOnHand();
                    int qty = object.getQty();

                    items.get(j).setQtyOnHand(qOH + qty);
                }
            }
        }
    }

    public void txtQty_OnAction(ActionEvent actionEvent) throws SQLException {
        btnSave_OnAction(null);
    }

    public void initializeForSearchOrder(String orderID,String orderDate,String customerID,String customerName) {

        connection = DBConnection.getInstance().getConnection();
        tableOrderDetails.getItems().clear();
        ObservableList<ManageOrdersTM> table = tableOrderDetails.getItems();

        try {
            PreparedStatement pstm = connection.prepareStatement("select o.itemCode , i.description, o.qty,o.unitPrice,(o.unitPrice*o.qty) as total from orderDetail o,item i where (i.itemCode=o.itemCode) AND o.orderId=?");
            pstm.setString(1,orderID);
            ResultSet rst = pstm.executeQuery();

            while(rst.next()){
                JFXButton button = new JFXButton("Delete");
                ManageOrdersTM mng =  new ManageOrdersTM(rst.getString(1),rst.getString(2),rst.getInt(3),rst.getInt(4),rst.getInt(5),button);
                String itemCode = rst.getString(1);

                table.add(mng);
                txtOrderID.setText(orderID);
                txtDate.setText(orderDate);
                tableOrderDetails.setSelectionModel(null);
                comboCustomerID.setPromptText(customerID);
                comboCustomerID.setDisable(true);
                txtCustomerName.setText(customerName);
                txtCustomerName.setDisable(true);
                comboItemCode.setDisable(true);
                txtItemDesc.setDisable(true);
                txtQty.setDisable(true);
                txtQtyOnHand.setDisable(true);
                txtUnitPrice.setDisable(true);
                txtOrderID.setDisable(true);
                btnSave.setDisable(true);
                btnPlaceOrder.setDisable(true);

            }
            int finalTotal = 0;
            for (int i = 0; i < table.size(); i++) {
                finalTotal += table.get(i).getTotal();
            }
            labTotal.setText(Integer.toString(finalTotal));

        } catch (SQLException e) {
            e.printStackTrace();
        }


        tableOrderDetails.setItems(table);

    }


    public void btnReport_OnAction(ActionEvent actionEvent) throws JRException {

        JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/reports/orders.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        ObservableList<ManageOrdersTM> table = tableOrderDetails.getItems();
        ObservableList<ManageOrders> orders = FXCollections.observableArrayList();

        for (int i = 0; i <table.size() ; i++) {
            orders.add(new ManageOrders(table.get(i).getItemCode(),table.get(i).getItemDescription(),Integer.toString(table.get(i).getQty()),Integer.toString(table.get(i).getUnitPrice()),Integer.toString(table.get(i).getTotal())));
        }
        Map<String,Object> params = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,params,new JRBeanCollectionDataSource(orders));

        JasperViewer.viewReport(jasperPrint);

    }
}
