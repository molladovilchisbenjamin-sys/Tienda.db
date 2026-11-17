package com.datapars.tiendita;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class PrimaryController implements Initializable {
    
    @FXML private Label lblConexion;
    @FXML private TextArea txtProductos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection cnx = null;
        Statement cmd = null;
        ResultSet rst = null;
        String cnxStr = "jdbc:sqlite:C:\\Users\\yolom\\OneDrive\\Desktop\\Tienda.db";

        try {
            Class.forName("org.sqlite.JDBC");
            cnx = DriverManager.getConnection(cnxStr);
            lblConexion.setText("Conexion establecida");

            String queryte = "SELECT *  FROM productos";
            cmd = cnx.createStatement();
            rst = cmd.executeQuery(queryte);

            while(rst.next()){
                int id              = rst.getInt("id");
                String producto     = rst.getString("producto");
                double precio       = rst.getDouble("precio");
                String fabricante   = rst.getString("fabricante");
                
                txtProductos.appendText(
                id + " | " + producto + " | " + precio + " | " + fabricante + "\n"
                );
                
                
                
            }
            

        } catch (Exception ex) {
            txtProductos.appendText("Error: " + ex.getMessage() + "\n");
        } finally {
            
            try {
                rst.close();
                cmd.close();
                cnx.close();
            } catch (Exception ex) {
                txtProductos.appendText("Error: " + ex.getMessage() + "\n");
            }
            
        }
    }
}