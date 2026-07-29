package com.sga.vista;

import javax.swing.*;
import java.awt.*;
import com.mycompany.sga_qr.conexion.Conexion; 

public class DashboardUsuario extends JFrame {

    private final Color COLOR_PRIMARIO = new Color(30, 41, 59);  
    private final Color COLOR_FONDO = new Color(241, 245, 249);   

    private JLabel lblContenedorQR;        
    private String nombreUsuario;
    private String rolUsuario;
    private String matriculaUsuario;

    public DashboardUsuario(String nombre, String rol, String matricula) {
        this.nombreUsuario = nombre;
        this.rolUsuario = rol;
        this.matriculaUsuario = matricula;

        setTitle("SGA_QR - Portal Institucional");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        construirInterfaz();
    }

    private void construirInterfaz() {
        // --- ENCABEZADO SUPERIOR ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(COLOR_PRIMARIO);
        panelHeader.setPreferredSize(new Dimension(1000, 70));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JLabel lblLogo = new JLabel("SGA_QR - Credencial Digital");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panelHeader.add(lblLogo, BorderLayout.WEST);

        JPanel panelUsuarioControles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panelUsuarioControles.setOpaque(false);

        JButton btnRegresar = new JButton("Cerrar Sesión");
        btnRegresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setBackground(new Color(62, 12, 83));
        btnRegresar.setOpaque(true);
        btnRegresar.setBorderPainted(false);
        btnRegresar.setFocusPainted(false);
        btnRegresar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        btnRegresar.addActionListener(e -> {
            FrmLogin login = new FrmLogin();
            login.setVisible(true);
            this.dispose();
        });
        panelUsuarioControles.add(btnRegresar);
        panelHeader.add(panelUsuarioControles, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // --- CUERPO PRINCIPAL ---
        JPanel panelCuerpo = new JPanel(new BorderLayout(30, 30));
        panelCuerpo.setBackground(COLOR_FONDO);
        panelCuerpo.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Panel Izquierdo: Información del Alumno/Profesor
        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 15, 15));
        panelInfo.setBackground(Color.WHITE);
        panelInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Mis Datos Académicos"),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panelInfo.setPreferredSize(new Dimension(380, 500));
        
        JLabel l1 = new JLabel("<html><b>Nombre:</b><br>" + nombreUsuario + "</html>");
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 15)); l1.setForeground(Color.BLACK);
        
        JLabel l2 = new JLabel("<html><b>Tipo de Cuenta:</b><br>" + rolUsuario + "</html>");
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 15)); l2.setForeground(Color.BLACK);
        
        JLabel l3 = new JLabel("<html><b>Matrícula Asignada:</b><br>" + matriculaUsuario + "</html>");
        l3.setFont(new Font("Segoe UI", Font.PLAIN, 15)); l3.setForeground(Color.BLACK);
        
        JButton btnGenerarPropio = new JButton("Activar mi Código QR de Registro");
        btnGenerarPropio.setBackground(new Color(199, 210, 254)); // Indigo pastel
        btnGenerarPropio.setForeground(Color.BLACK);
        btnGenerarPropio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        panelInfo.add(l1); 
        panelInfo.add(l2); 
        panelInfo.add(l3); 
        panelInfo.add(btnGenerarPropio);
        panelCuerpo.add(panelInfo, BorderLayout.WEST);

        // Panel Derecho: El lienzo donde se dibuja el QR
        JPanel panelLienzo = new JPanel(new BorderLayout());
        panelLienzo.setBackground(Color.WHITE);
        panelLienzo.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 2));
        
        lblContenedorQR = new JLabel("Presiona el botón de la izquierda para activar tu QR", SwingConstants.CENTER);
        lblContenedorQR.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblContenedorQR.setForeground(Color.GRAY);
        panelLienzo.add(lblContenedorQR, BorderLayout.CENTER);
        panelCuerpo.add(panelLienzo, BorderLayout.CENTER);

        add(panelCuerpo, BorderLayout.CENTER);

        // Acción para procesar y dibujar el QR usando ZXing
        btnGenerarPropio.addActionListener(e -> procesarYDibujarQR(matriculaUsuario));
    }

    private void procesarYDibujarQR(String texto) {
        try {
            int dimensiones = 350;
            com.google.zxing.qrcode.QRCodeWriter escritorQR = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix matrizBits = escritorQR.encode(texto, com.google.zxing.BarcodeFormat.QR_CODE, dimensiones, dimensiones);
            java.awt.image.BufferedImage imagenFinal = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(matrizBits);
            
            lblContenedorQR.setText(""); 
            lblContenedorQR.setIcon(new ImageIcon(imagenFinal));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar QR: " + ex.getMessage());
        }
    }
}