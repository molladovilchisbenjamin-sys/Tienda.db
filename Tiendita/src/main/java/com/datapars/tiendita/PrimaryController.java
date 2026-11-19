package com.datapars.tiendita;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class PrimaryController implements Initializable {

    @FXML private Label lblConexion;

    // Formulario
    @FXML private TextField txtProducto;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtFabricante;
    @FXML private Button btnInsertar;
    @FXML private Button btnActualizar;                       // ← NUEVO
    @FXML private Button btnLimpiar;                          // ← NUEVO
    @FXML private Button btnEliminar;

    // Tabla
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String>  colProducto;
    @FXML private TableColumn<Producto, Double>  colPrecio;
    @FXML private TableColumn<Producto, String>  colFabricante;

    private final ObservableList<Producto> lista = FXCollections.observableArrayList();
    private final String cnxStr = "jdbc:sqlite:C:\\Users\\yolom\\OneDrive\\Desktop\\Tienda.db";

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colFabricante.setCellValueFactory(new PropertyValueFactory<>("fabricante"));

        tblProductos.setItems(lista);

        // ─── NUEVO: listener de selección ───────────────────────────
        // Cada vez que cambia la fila seleccionada, rellenar/limpiar el formulario.
        tblProductos.getSelectionModel().selectedItemProperty().addListener(
            (obs, anterior, actual) -> {
                if (actual != null) {
                    txtProducto.setText(actual.getProducto());
                    txtPrecio.setText(String.valueOf(actual.getPrecio()));
                    txtFabricante.setText(actual.getFabricante());
                } else {
                    limpiarFormulario();
                }
            }
        );

        // ─── NUEVO: botón Actualizar deshabilitado si no hay selección ───
        btnActualizar.disableProperty().bind(
            tblProductos.getSelectionModel().selectedItemProperty().isNull()
        );

        cargarProductos();
    }

    // ═══════════════════════════════════════════════════════════
    //                        SELECT
    // ═══════════════════════════════════════════════════════════
    private void cargarProductos() {
        lista.clear();
        String sql = "SELECT id, producto, precio, fabricante FROM productos ORDER BY id";
        try (Connection cnx = DriverManager.getConnection(cnxStr);
             Statement cmd = cnx.createStatement();
             ResultSet rst = cmd.executeQuery(sql)) {

            lblConexion.setText("Conexion establecida");
            while (rst.next()) {
                lista.add(new Producto(
                    rst.getInt("id"),
                    rst.getString("producto"),
                    rst.getDouble("precio"),
                    rst.getString("fabricante")
                ));
            }
        } catch (Exception ex) {
            lblConexion.setText("Error al cargar: " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //                        INSERT
    // ═══════════════════════════════════════════════════════════
    @FXML
    private void onInsertar() {
        // Si había una fila seleccionada, primero la deseleccionamos
        // para que el insertar sea siempre "producto nuevo".
        tblProductos.getSelectionModel().clearSelection();

        String producto   = txtProducto.getText().trim();
        String precioTxt  = txtPrecio.getText().trim();
        String fabricante = txtFabricante.getText().trim();

        if (producto.isEmpty() || precioTxt.isEmpty() || fabricante.isEmpty()) {
            lblConexion.setText("Insertar-Completa todos los campos");
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioTxt);
        } catch (NumberFormatException ex) {
            lblConexion.setText("El precio debe ser un número");
            return;
        }

        String sql = "INSERT INTO productos (producto, precio, fabricante) VALUES (?, ?, ?)";
        try (Connection cnx = DriverManager.getConnection(cnxStr);
             PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, producto);
            pst.setDouble(2, precio);
            pst.setString(3, fabricante);
            pst.executeUpdate();

            int nuevoId = 0;
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) nuevoId = keys.getInt(1);
            }

            lista.add(new Producto(nuevoId, producto, precio, fabricante));

            limpiarFormulario();
            txtProducto.requestFocus();
            lblConexion.setText("Producto insertado (id=" + nuevoId + ")");

        } catch (Exception ex) {
            lblConexion.setText("Error al insertar: " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //                        UPDATE (NUEVO)
    // ═══════════════════════════════════════════════════════════
    @FXML
    private void onActualizar() {

        // 1) ¿Hay algo seleccionado?
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblConexion.setText("Selecciona un producto en la tabla para actualizarlo");
            return;
        }

        // 2) Leer y validar los nuevos valores del formulario
        String productoNuevo   = txtProducto.getText().trim();
        String precioTxt       = txtPrecio.getText().trim();
        String fabricanteNuevo = txtFabricante.getText().trim();

        if (productoNuevo.isEmpty() || precioTxt.isEmpty() || fabricanteNuevo.isEmpty()) {
            lblConexion.setText("Insertar-Completa todos los campos");
            return;
        }

        double precioNuevo;
        try {
            precioNuevo = Double.parseDouble(precioTxt);
        } catch (NumberFormatException ex) {
            lblConexion.setText("El precio debe ser un número");
            return;
        }

        // 3) UPDATE en SQLite
        String sql = "UPDATE productos SET producto = ?, precio = ?, fabricante = ? WHERE id = ?";
        try (Connection cnx = DriverManager.getConnection(cnxStr);
             PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, productoNuevo);
            pst.setDouble(2, precioNuevo);
            pst.setString(3, fabricanteNuevo);
            pst.setInt(4, seleccionado.getId());   // la PK de la fila seleccionada
            int filasAfectadas = pst.executeUpdate();

            if (filasAfectadas > 0) {
                // 4) Refrescar la fila EN LA MISMA LISTA (sin recargar todo)
                seleccionado.setProducto(productoNuevo);
                seleccionado.setPrecio(precioNuevo);
                seleccionado.setFabricante(fabricanteNuevo);

                // 5) Limpiar selección y formulario → la tabla queda "en modo insertar"
                tblProductos.getSelectionModel().clearSelection();

                lblConexion.setText("Producto actualizado (id=" + seleccionado.getId() + ")");
            } else {
                lblConexion.setText("El producto ya no existía en la base de datos");
            }

        } catch (Exception ex) {
            lblConexion.setText("Error al actualizar: " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //                        DELETE
    // ═══════════════════════════════════════════════════════════
    @FXML
    private void onEliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblConexion.setText("Selecciona un producto en la tabla para eliminarlo");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar este producto?");
        confirm.setContentText(
            seleccionado.getId()       + " | " +
            seleccionado.getProducto() + " | " +
            seleccionado.getPrecio()   + " | " +
            seleccionado.getFabricante()
        );
        Optional<ButtonType> respuesta = confirm.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) return;

        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection cnx = DriverManager.getConnection(cnxStr);
             PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setInt(1, seleccionado.getId());
            int filasAfectadas = pst.executeUpdate();

            if (filasAfectadas > 0) {
                lista.remove(seleccionado);
                tblProductos.getSelectionModel().clearSelection();
                lblConexion.setText("Producto eliminado (id=" + seleccionado.getId() + ")");
            } else {
                lblConexion.setText("El producto ya no existía en la base de datos");
            }
        } catch (Exception ex) {
            lblConexion.setText("Error al eliminar: " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //                        LIMPIAR (NUEVO)
    // ═══════════════════════════════════════════════════════════
    @FXML
    private void onLimpiar() {
        tblProductos.getSelectionModel().clearSelection();
        // Al quedar sin selección, el listener llama a limpiarFormulario() solo,
        // pero lo llamamos explícitamente por si acaso.
        limpiarFormulario();
        txtProducto.requestFocus();
        lblConexion.setText("Insertar-Completa todos los campos");
    }

    private void limpiarFormulario() {
        txtProducto.clear();
        txtPrecio.clear();
        txtFabricante.clear();
    }
}