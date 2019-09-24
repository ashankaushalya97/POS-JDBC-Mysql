package Controller;

import DTO.SearchTM;
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

public class SearchController {

    public JFXTextField txtSearch;
    public TableView<SearchTM> tableResult;
    public AnchorPane searchPane;
    public ImageView icnHome;
    public Connection connection;
    public JFXButton btnSearch;

    public void initialize() {

        tableResult.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("orderID"));
        tableResult.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        tableResult.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("total"));
        tableResult.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("customerID"));
        tableResult.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("customerName"));

        //ObservableList<SearchTM> search = FXCollections.observableList(DB.search);
        tableResult.getItems().clear();
        ObservableList<SearchTM> search = tableResult.getItems();
        try {
            //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos","root","");
            connection= DBConnection.getInstance().getConnection();

            PreparedStatement pstm = connection.prepareStatement("select orders.orderId,orders.orderDate,sum((orderDetail.qty)*(orderDetail.unitPrice)) as total,orders.customerId,customer.name from ((orders \n" +
                    "INNER JOIN orderDetail ON orders.orderId = orderDetail.orderId)\n" +
                    "INNER JOIN customer ON orders.customerId = customer.id) group by orders.orderId");

            ResultSet rst = pstm.executeQuery();

            while(rst.next()){
                search.add(new SearchTM(rst.getString(1),rst.getString(2),rst.getInt(3),rst.getString(4),rst.getString(5)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        tableResult.setItems(search);

        txtSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String searchText = txtSearch.getText();
                ObservableList<SearchTM> search1 = tableResult.getItems();
                tableResult.getItems().clear();

                if(searchText.equals(null)){
                    initialize();
                    return;
                }

                try {

                    connection= DBConnection.getInstance().getConnection();
                    String like = "%"+searchText+"%";
                    PreparedStatement pstm = connection.prepareStatement("select orders.orderId,orders.orderDate,sum((orderDetail.qty)*(orderDetail.unitPrice)) as total,orders.customerId,customer.name from ((orders \n" +
                            "INNER JOIN orderDetail ON orders.orderId = orderDetail.orderId)\n" +
                            "INNER JOIN customer ON orders.customerId = customer.id) group by orders.orderId having orders.orderId like ? OR orders.orderDate like ? OR orders.customerId like ? OR customer.name like ? OR sum((orderDetail.qty)*(orderDetail.unitPrice)) like ?;");

                    pstm.setString(1,like);
                    pstm.setString(2,like);
                    pstm.setString(3,like);
                    pstm.setString(4,like);
                    pstm.setString(5,like);

               //     pstm.setString(5,searchText);
                    ResultSet rst = pstm.executeQuery();


                    while(rst.next()){

                        search1.add(new SearchTM(rst.getString(1),rst.getString(2),rst.getInt(3),rst.getString(4),rst.getString(5)));
                    }
                    tableResult.setItems(search1);

                } catch (SQLException e) {
                    e.printStackTrace();
                }



            }
        });
    }

    public void icnHome_OnAction(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/Dashboard.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) this.searchPane.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
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

    public void tableResult_OnAction(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            URL resource = this.getClass().getResource("/View/ManageOrders.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Parent root = fxmlLoader.load();
            Scene placeOrderScene = new Scene(root);
            Stage secondaryStage = new Stage();
            secondaryStage.setScene(placeOrderScene);
            secondaryStage.centerOnScreen();
            secondaryStage.setTitle("View Orders");
            secondaryStage.setResizable(false);
            secondaryStage.show();
            searchPane.getScene().getWindow().hide();

            ManageOrdersController contr = fxmlLoader.getController();

            SearchTM selectedOrder = tableResult.getSelectionModel().getSelectedItem();

            String orderID = selectedOrder.getOrderID();

            contr.initializeForSearchOrder(orderID,selectedOrder.getDate(),selectedOrder.getCustomerID(),selectedOrder.getCustomerName());

        }
    }

    public void btnSearch_OnAction(ActionEvent actionEvent) throws JRException {

        JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/reports/search.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        String search ="%"+txtSearch.getText()+"%";
        Map<String,Object> params = new HashMap<>();
        params.put("searchText",search);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,params,DBConnection.getInstance().getConnection());

        JasperViewer.viewReport(jasperPrint,false);

    }
}
