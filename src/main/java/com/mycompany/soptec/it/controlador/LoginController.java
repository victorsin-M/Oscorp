package com.mycompany.soptec_it;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
/**
 *
 * @author Oscorp
 */



public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbDepartamento;
    @FXML private Button btnIniciarSesion;
    @FXML private Hyperlink linkRecuperar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbDepartamento.getItems().addAll(
            "Ventas",
            "Levantamientos",
            "Administración",
            "Compras-Almacén",
            "Instalaciones"
        );
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();
        String departamento = cmbDepartamento.getValue();

        if (usuario.isEmpty() || password.isEmpty() || departamento == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Por favor, llene todos los campos y seleccione un departamento.");
            return;
        }

        System.out.println("Autenticando: " + usuario + " | Área: " + departamento);
        mostrarAlerta(Alert.AlertType.INFORMATION, "Acceso Concedido", "Bienvenido al sistema, conectando al Dashboard...");
        
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}