package com.mycompany.soptec.it;

import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de SOPTEC-IT (Modulos CRUD Tablas Base).
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            AppLogger.registrarExcepcion("Sistema", "App", "EXCEPCION_NO_CAPTURADA_UI", ex);
        });

        try {
            Parent root = loadFXML("/fxml/MainCrud");
            scene = new Scene(root, 1280, 780);
            stage.setTitle("SOPTEC-IT · CRUD Tablas Base (Actividad 4.6)");
            stage.setMinWidth(1080);
            stage.setMinHeight(680);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException ex) {
            AppLogger.registrarExcepcion("Sistema", "App", "INICIAR_APLICACION", ex);
            AlertaUtil.error("Error al iniciar SOPTEC-IT",
                    "No se pudo cargar la ventana principal. Consulta el archivo de log para más detalles.");
        }
    }

    public static void setRoot(String fxml) throws IOException {
        String ruta = fxml.startsWith("/") ? fxml : "/fxml/" + fxml;
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
