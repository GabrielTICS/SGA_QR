package com.mycompany.sga_qr.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:mysql://localhost:3306/sga_db?serverTimezone=UTC";
    private static final String USER = "root"; // Tu usuario de MySQL
    private static final String PASSWORD = "wiwi"; // Tu contraseña de MySQL

    public static Connection getConexion() {
        Connection cn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error en la conexión local: " + e.getMessage());
        }
        return cn;
    }
}