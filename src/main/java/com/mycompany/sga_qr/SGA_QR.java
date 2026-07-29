package com.mycompany.sga_qr;

import com.sga.vista.FrmEscaner;
import javax.swing.SwingUtilities;

/**
 *
 * @author gabriel
 */
public class SGA_QR {

    public static void main(String[] args) {
        // Iniciamos la interfaz gráfica de forma segura en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            FrmEscaner escaner = new FrmEscaner();
            escaner.setVisible(true);
        });
    }
}