
package com.mycompany.soptec.it;
import com.mycompany.soptec.it.dao.Conexion;
import java.sql.Connection;
public class PruebaConexion {
    public static void main(String[] args) {
        Connection con = Conexion.conectar();
        
        if (con != null){
            System.out.println("Base de datos conectada.");
        }else{
            System.out.println("No se pudo conectar.");
        }
    }
}
