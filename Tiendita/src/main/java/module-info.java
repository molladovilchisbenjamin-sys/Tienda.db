module com.datapars.tiendita {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    opens com.datapars.tiendita to javafx.fxml;
    exports com.datapars.tiendita;
}
