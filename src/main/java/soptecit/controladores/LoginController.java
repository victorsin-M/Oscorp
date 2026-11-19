package soptecit.controladores;

import com.mycompany.soptec.it.util.AppLogger;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import soptecit.util.Mensajes;

public class LoginController implements Initializable {

    private static String ultimoUsuarioRecordado = "";

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private ComboBox<String> cmbArea;
    @FXML private CheckBox chkRecordar;
    @FXML private Label lblError;
    @FXML private Button btnIngresar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbArea.getSelectionModel().selectFirst();
        if (!ultimoUsuarioRecordado.isEmpty()) {
            txtUsuario.setText(ultimoUsuarioRecordado);
            chkRecordar.setSelected(true);
        }
        txtUsuario.textProperty().addListener((o, a, n) -> mostrarError(null));
        txtContrasena.textProperty().addListener((o, a, n) -> mostrarError(null));
    }

    @FXML
    private void ingresar(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();
        String area = cmbArea.getValue();

        if (usuario.isEmpty() || contrasena.isEmpty() || area == null) {
            mostrarError("Completa usuario, contraseña y área para continuar.");
            return;
        }

        ultimoUsuarioRecordado = chkRecordar.isSelected() ? usuario : "";
        AppLogger.registrarInfo("Autenticacion", "LoginController", "INICIO_SESION",
                "Acceso concedido al usuario '" + usuario + "' en área '" + area + "'.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/soptecit/vistas/Principal.fxml"));
            Parent principal = loader.load();
            PrincipalController controlador = loader.getController();
            controlador.setUsuario(usuario, area);
            btnIngresar.getScene().setRoot(principal);
        } catch (IOException ex) {
            AppLogger.registrarExcepcion("Autenticacion", "LoginController", "ABRIR_PRINCIPAL", ex);
            mostrarError("No se pudo abrir la ventana principal. Revisa el archivo de log.");
        }
    }

    @FXML
    private void recuperarContrasena(ActionEvent event) {
        Mensajes.info("Recuperación de acceso",
                "Contacta al Administrador general de SOPTEC-IT para restablecer tus credenciales.");
    }

    private void mostrarError(String mensaje) {
        boolean hay = mensaje != null;
        if (hay) {
            lblError.setText(mensaje);
        }
        lblError.setVisible(hay);
        lblError.setManaged(hay);
    }

    @FXML
    private void salirAplicacion(ActionEvent event) {
        if (Mensajes.confirmar("Cerrar la aplicación", "¿Deseas cerrar SOPTEC-IT?")) {
            Platform.exit();
        }
    }
}
