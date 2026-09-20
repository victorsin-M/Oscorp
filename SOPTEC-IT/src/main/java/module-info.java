module com.mycompany.soptec.it {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.soptec.it to javafx.fxml;
    exports com.mycompany.soptec.it;
}
