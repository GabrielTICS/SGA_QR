package com.sga.vista;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.mycompany.sga_qr.conexion.Conexion; 

public class FrmLogin extends JFrame {

    private final Color COLOR_PRIMARIO = new Color(30, 41, 59);    // Slate 800
    private final Color COLOR_ACCENTO = new Color(199, 210, 254);   // Indigo pastel
    private final Color COLOR_FONDO = new Color(241, 245, 249);     // Gris claro limpio

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;

    public FrmLogin() {
        setTitle("SGA_QR - Inicio de Sesión");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_FONDO);

        construirInterfaz();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout());

        // --- Panel Superior (Banner) ---
        JPanel panelBanner = new JPanel();
        panelBanner.setBackground(COLOR_PRIMARIO);
        panelBanner.setPreferredSize(new Dimension(450, 120));
        panelBanner.setLayout(new GridBagLayout());

        JLabel lblTitulo = new JLabel("SGA_QR");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JLabel lblSubtitulo = new JLabel("Sistema de Gestión y Acceso");
        lblSubtitulo.setForeground(new Color(148, 163, 184));
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        GridBagConstraints gbcBanner = new GridBagConstraints();
        gbcBanner.gridx = 0; gbcBanner.gridy = 0;
        panelBanner.add(lblTitulo, gbcBanner);
        gbcBanner.gridy = 1; gbcBanner.insets = new Insets(5, 0, 0, 0);
        panelBanner.add(lblSubtitulo, gbcBanner);

        add(panelBanner, BorderLayout.NORTH);

        // --- Panel Central (Formulario) ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(Color.WHITE);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        Font fuenteEtiquetas = new Font("Segoe UI", Font.BOLD, 13);
        Font fuenteCampos = new Font("Segoe UI", Font.PLAIN, 15);

        // Campo: Usuario
        JLabel lblUser = new JLabel("Cuenta de Usuario:");
        lblUser.setFont(fuenteEtiquetas);
        lblUser.setForeground(COLOR_PRIMARIO);
        gbc.gridy = 0; panelForm.add(lblUser, gbc);

        txtUsuario = new JTextField();
        txtUsuario.setFont(fuenteCampos);
        txtUsuario.setForeground(Color.BLACK);
        txtUsuario.setPreferredSize(new Dimension(300, 38));
        gbc.gridy = 1; panelForm.add(txtUsuario, gbc);

        // Campo: Contraseña
        JLabel lblPass = new JLabel("Contraseña de Acceso:");
        lblPass.setFont(fuenteEtiquetas);
        lblPass.setForeground(COLOR_PRIMARIO);
        gbc.gridy = 2; gbc.insets = new Insets(15, 0, 8, 0);
        panelForm.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(fuenteCampos);
        txtPassword.setForeground(Color.BLACK);
        txtPassword.setPreferredSize(new Dimension(300, 38));
        gbc.gridy = 3; gbc.insets = new Insets(8, 0, 8, 0);
        panelForm.add(txtPassword, gbc);

        // Botón de Acción Principal
        btnIngresar = new JButton("Iniciar Sesión");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnIngresar.setForeground(Color.BLACK);
        btnIngresar.setBackground(COLOR_ACCENTO);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.setPreferredSize(new Dimension(300, 42));
        
        gbc.gridy = 4; gbc.insets = new Insets(30, 0, 10, 0);
        panelForm.add(btnIngresar, gbc);

        JPanel panelContenedorCentral = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        panelContenedorCentral.setBackground(COLOR_FONDO);
        panelContenedorCentral.add(panelForm);

        add(panelContenedorCentral, BorderLayout.CENTER);

        btnIngresar.addActionListener(e -> ejecutarAutenticacionSQL());
        txtPassword.addActionListener(e -> ejecutarAutenticacionSQL());
    }

    private void ejecutarAutenticacionSQL() {
        String usuarioInput = txtUsuario.getText().trim();
        String passwordInput = new String(txtPassword.getPassword()).trim();

        if (usuarioInput.isEmpty() || passwordInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor introduce tu usuario y contraseña.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT nombre_completo, tipo_usuario, matricula FROM usuarios WHERE usuario = ? AND contrasenia = ?";

        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuarioInput);
            ps.setString(2, passwordInput);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre_completo");
                    String rol = rs.getString("tipo_usuario");
                    String matricula = rs.getString("matricula");

                    JOptionPane.showMessageDialog(this, "¡Bienvenido al sistema, " + nombre + "!");

                    if (rol.equalsIgnoreCase("ADMIN")) {
                        Dashboard adminDash = new Dashboard(nombre, rol, matricula);
                        adminDash.setVisible(true);
                    } else {
                        DashboardUsuario userDash = new DashboardUsuario(nombre, rol, matricula);
                        userDash.setVisible(true);
                    }
                    this.dispose(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}