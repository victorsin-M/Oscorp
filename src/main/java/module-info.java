module com.mycompany.soptec.it {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;

    opens com.mycompany.soptec.it to javafx.fxml;
    opens com.mycompany.soptec.it.controlador to javafx.fxml;
    opens com.mycompany.soptec.it.modelo to javafx.base;
    opens soptecit.controladores to javafx.fxml;

    exports com.mycompany.soptec.it;
    exports com.mycompany.soptec.it.controlador;
    exports com.mycompany.soptec.it.dao;
    exports com.mycompany.soptec.it.excepciones;
    exports com.mycompany.soptec.it.modelo;
    exports com.mycompany.soptec.it.servicio;
    exports com.mycompany.soptec.it.util;
    exports soptecit.controladores;
    exports soptecit.util;
}
