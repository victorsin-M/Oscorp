package com.mycompany.soptec.it.util;

import java.util.List;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Conecta un ComboBox descriptivo con un TextField buscador para evitar captura manual de IDs.
 */
public final class ComboBuscadorUtil {

    private ComboBuscadorUtil() {
    }

    public static <T> void configurar(ComboBox<T> combo, TextField buscador,
            List<T> elementos, Function<T, String> descriptor) {
        ObservableList<T> base = FXCollections.observableArrayList(elementos);
        FilteredList<T> filtrados = new FilteredList<>(base, item -> true);

        combo.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : descriptor.apply(object);
            }

            @Override
            public T fromString(String string) {
                if (string == null) {
                    return null;
                }
                for (T item : base) {
                    if (descriptor.apply(item).equals(string)) {
                        return item;
                    }
                }
                return null;
            }
        });

        combo.setItems(filtrados);

        if (buscador != null) {
            buscador.textProperty().addListener((obs, oldVal, newVal) -> {
                String filtro = newVal == null ? "" : newVal.trim().toLowerCase();
                T seleccionado = combo.getValue();
                filtrados.setPredicate(item -> filtro.isEmpty()
                        || descriptor.apply(item).toLowerCase().contains(filtro));
                if (seleccionado != null && filtrados.contains(seleccionado)) {
                    combo.setValue(seleccionado);
                }
            });
        }
    }

    public static <T> void seleccionarPorPredicado(ComboBox<T> combo, java.util.function.Predicate<T> predicado) {
        if (combo.getItems() == null) {
            return;
        }
        for (T item : combo.getItems()) {
            if (predicado.test(item)) {
                combo.setValue(item);
                return;
            }
        }
    }
}
