package com.datapars.tiendita;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Producto {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty producto = new SimpleStringProperty();
    private final DoubleProperty precio = new SimpleDoubleProperty();
    private final StringProperty fabricante = new SimpleStringProperty();

    public Producto(int id, String producto, double precio, String fabricante) {
        this.id.set(id);
        this.producto.set(producto);
        this.precio.set(precio);
        this.fabricante.set(fabricante);
    }

    // --- Properties ---
    public IntegerProperty idProperty()         { return id; }
    public StringProperty  productoProperty()   { return producto; }
    public DoubleProperty  precioProperty()     { return precio; }
    public StringProperty  fabricanteProperty() { return fabricante; }

    // --- Getters ---
    public int    getId()         { return id.get(); }
    public String getProducto()   { return producto.get(); }
    public double getPrecio()     { return precio.get(); }
    public String getFabricante() { return fabricante.get(); }

    // --- Setters (NUEVOS) ---
    public void setId(int id)                { this.id.set(id); }
    public void setProducto(String producto) { this.producto.set(producto); }
    public void setPrecio(double precio)     { this.precio.set(precio); }
    public void setFabricante(String fab)    { this.fabricante.set(fab); }
}