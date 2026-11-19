package com.mycompany.soptec.it;

import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import java.io.IOException;
import java.io.InputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Punto de entrada unificado de SOPTEC-IT.
 * Inicia con las vistas estilizadas (Login y Principal por áreas) conectadas al CRUD real en MySQL.
 * F11 alterna entre pantalla completa y ventana maximizada.
 */
public class App extends Application {

    private static final boolean PANTALLA_COMPLETA = false;
    private static Scene scene;

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            AppLogger.registrarExcepcion("Sistema", "App", "EXCEPCION_NO_CAPTURADA_UI", ex);
        });

        try {
            cargarFuentes();

            Parent root = loadFXML("/soptecit/vistas/Login");
            scene = new Scene(root, 1280, 760);

            stage.setTitle("SOPTEC-IT · Control de Información y Soporte Técnico");
            InputStream iconoStream = getClass().getResourceAsStream("/soptecit/img/icono.png");
            if (iconoStream != null) {
                stage.getIcons().add(new Image(iconoStream));
            }
            stage.setMinWidth(1160);
            stage.setMinHeight(700);
            stage.setScene(scene);

            // Pantalla completa con F11 (ESC no sale por accidente)
            stage.setMaximized(true);
            stage.setFullScreenExitHint("Presiona F11 para salir de pantalla completa");
            stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("F11"));
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
            if (PANTALLA_COMPLETA) {
                stage.setFullScreen(true);
            }
            stage.show();
        } catch (IOException ex) {
            AppLogger.registrarExcepcion("Sistema", "App", "INICIAR_APLICACION", ex);
            AlertaUtil.error("Error al iniciar SOPTEC-IT",
                    "No se pudo cargar la ventana de acceso. Consulta el archivo de log para más detalles.");
        }
    }

    /** Carga la tipografía Poppins incluida en /soptecit/fonts/. */
    public static void cargarFuentes() {
        String[] fuentes = {"Poppins-Regular", "Poppins-Medium", "Poppins-SemiBold", "Poppins-Bold"};
        for (String f : fuentes) {
            InputStream is = App.class.getResourceAsStream("/soptecit/fonts/" + f + ".ttf");
            if (is != null) {
                Font.loadFont(is, 13);
            }
        }
    }

    public static void setRoot(String fxml) throws IOException {
        String ruta = fxml.startsWith("/") ? fxml : "/soptecit/vistas/" + fxml;
        scene.setRoot(loadFXML(ruta));
    }

    private static Parent loadFXML(String rutaSinExtension) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(rutaSinExtension + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
