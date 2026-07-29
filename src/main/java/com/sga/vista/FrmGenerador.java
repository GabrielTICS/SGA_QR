package com.sga.vista;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class FrmGenerador extends JFrame {

    private JTextField txtTexto;
    private JButton btnGenerar;
    private JLabel lblResultadoQR;

    public FrmGenerador() {
        setTitle("SGA_QR - Generador de Texto Plano");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // Panel Superior: Entrada de texto
        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        
        panelSuperior.add(new JLabel("Texto plano o Matrícula:"), BorderLayout.NORTH);
        
        txtTexto = new JTextField();
        txtTexto.setFont(new Font("Arial", Font.PLAIN, 16));
        panelSuperior.add(txtTexto, BorderLayout.CENTER);
        
        btnGenerar = new JButton("Generar QR");
        btnGenerar.setFont(new Font("Arial", Font.BOLD, 14));
        btnGenerar.addActionListener(e -> generarCodigoQR());
        panelSuperior.add(btnGenerar, BorderLayout.SOUTH);
        
        add(panelSuperior, BorderLayout.NORTH);

        // Panel Central: Contenedor para mostrar el QR generado
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        
        lblResultadoQR = new JLabel("El QR aparecerá aquí", SwingConstants.CENTER);
        lblResultadoQR.setForeground(Color.GRAY);
        panelCentral.add(lblResultadoQR, BorderLayout.CENTER);
        
        // Margen para el cuadro del QR
        JPanel contenedorMargen = new JPanel(new BorderLayout());
        contenedorMargen.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        contenedorMargen.add(panelCentral, BorderLayout.CENTER);
        
        add(contenedorMargen, BorderLayout.CENTER);
    }

    private void generarCodigoQR() {
        String textoInput = txtTexto.getText().trim();
        
        if (textoInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, escribe algo primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Configurar el tamaño del QR (250x250 píxeles es perfecto)
            int ancho = 250;
            int alto = 250;
            
            // Invocar el motor de ZXing para codificar en formato QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(textoInput, BarcodeFormat.QR_CODE, ancho, alto);
            
            // Convertir la matriz de bits a una imagen BufferedImage que Java pueda entender
            BufferedImage imagenQR = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // Dibujar la imagen en el JLabel de la interfaz
            lblResultadoQR.setText(""); // Borrar el texto previo
            lblResultadoQR.setIcon(new ImageIcon(imagenQR));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar el QR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Ejecución rápida para probar solo esta ventana
        SwingUtilities.invokeLater(() -> new FrmGenerador().setVisible(true));
    }
}