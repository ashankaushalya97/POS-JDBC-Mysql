package Controller;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBConnection {
    private static DBConnection dbConnection;
    private Connection connection;

    private DBConnection(){

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos?createDatabaseIfNotExist=true&allowMultiQueries=true","root","");
            PreparedStatement pstm = connection.prepareStatement("show tables");
            ResultSet rst = pstm.executeQuery();
            if(!rst.next()){
                String sql = "\n" +
                        "CREATE TABLE customer (\n" +
                        "  id varchar(10) NOT NULL,\n" +
                        "  name varchar(50) DEFAULT NULL,\n" +
                        "  address varchar(50) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (id)\n" +
                        ") ;\n" +
                        "\n" +
                        "CREATE TABLE item (\n" +
                        "  itemCode varchar(10) NOT NULL,\n" +
                        "  description varchar(50) DEFAULT NULL,\n" +
                        "  qtyOnHand int(11) DEFAULT NULL,\n" +
                        "  unitPrice int(11) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (itemCode)\n" +
                        ");\n" +
                        "\n" +
                        "CREATE TABLE orderDetail (\n" +
                        "  orderId varchar(10) DEFAULT NULL,\n" +
                        "  itemCode varchar(10) DEFAULT NULL,\n" +
                        "  qty int(11) DEFAULT NULL,\n" +
                        "  unitPrice int(11) DEFAULT NULL\n" +
                        ");\n" +
                        "\n" +
                        "CREATE TABLE orders (\n" +
                        "  orderId varchar(10) NOT NULL,\n" +
                        "  orderDate date DEFAULT NULL,\n" +
                        "  customerId varchar(10) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (orderId)\n" +
                        ");\n" +
                        "ALTER table orderDetail\n" +
                        "ADD  CONSTRAINT fk_itemCode FOREIGN KEY (itemCode) REFERENCES item (itemCode) ON DELETE  CASCADE  ON UPDATE  CASCADE ;\n" +
                        "\n" +
                        "ALTER table orderDetail  \n" +
                        "add CONSTRAINT fk_orderId FOREIGN KEY (orderId) REFERENCES orders (orderId)ON DELETE  CASCADE  ON UPDATE  CASCADE ;\n" +
                        "\n" +
                        "Alter table orders \n" +
                        "add CONSTRAINT fk_customerId FOREIGN KEY (customerId) REFERENCES customer (id) ON DELETE  CASCADE  ON UPDATE  CASCADE ;\n";
                pstm=connection.prepareStatement(sql);
                pstm.execute();
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DBConnection getInstance(){
        return  dbConnection = ((dbConnection==null) ? new DBConnection(): dbConnection);
    }

    public Connection getConnection(){
        return connection;
    }
}
