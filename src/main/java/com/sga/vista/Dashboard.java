package com.sga.vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mycompany.sga_qr.conexion.Conexion; 

public class Dashboard extends JFrame {

    private final Color COLOR_PRIMARIO = new Color(30, 41, 59);  
    private final Color COLOR_ACCENTO = new Color(199, 210, 254);   
    private final Color COLOR_FONDO = new Color(241, 245, 249);   

    private JTabbedPane panelPestanas;
    private String nombreUsuario;
    private String rolUsuario;

    // Componentes CRUD Usuarios
    private JTextField txtIdOculto, txtNewUsuario, txtNewPassword, txtNewNombre, txtNewMatricula, txtNewEdad, txtBuscarUsuarios;
    private JComboBox<String> cbNewRol;
    private JTable tablaUsuariosAdmin;
    private DefaultTableModel modeloTablaUsuarios;
    private TableRowSorter<DefaultTableModel> sorterUsuarios;

    // Componentes pestaña Matrículas
    private JTable tablaMatriculasGlobales;
    private DefaultTableModel modeloMatriculas;
    private TableRowSorter<DefaultTableModel> sorterMatriculas;
    private JTextField txtBuscarMatriculas;

    public Dashboard(String nombre, String rol, String matricula) {
        this.nombreUsuario = nombre;
        this.rolUsuario = rol;

        setTitle("SGA_QR - Panel de Administración Global");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        construirInterfaz();
    }

    private void construirInterfaz() {
        // --- ENCABEZADO SUPERIOR ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(COLOR_PRIMARIO);
        panelHeader.setPreferredSize(new Dimension(1200, 70));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JLabel lblLogo = new JLabel("SGA_QR - Panel de Administración");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panelHeader.add(lblLogo, BorderLayout.WEST);

        JPanel panelUsuarioControles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panelUsuarioControles.setOpaque(false);

        JLabel lblRol = new JLabel(nombreUsuario + " | Rol: " + rolUsuario);
        lblRol.setForeground(new Color(148, 163, 184));
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelUsuarioControles.add(lblRol);

        JButton btnRegresar = new JButton("Cerrar Sesión");
        btnRegresar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegresar.setForeground(Color.BLACK);
        btnRegresar.setBackground(new Color(252, 165, 165));
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

        // --- PANEL DE PESTAÑAS (SOLO PARA ADMIN) ---
        panelPestanas = new JTabbedPane();
        panelPestanas.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panelPestanas.setBackground(Color.WHITE);

        panelPestanas.addTab("  Gestión de Matrículas  ", crearPestanaMatriculasGlobales());
        panelPestanas.addTab("  Usuarios y Contraseñas  ", crearPestanaUsuariosGlobales());
        
        recargarTablaUsuariosSQL();
        recargarTablaMatriculasSQL();

        add(panelPestanas, BorderLayout.CENTER);
    }

    private JPanel crearPestanaMatriculasGlobales() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(COLOR_FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Buscador Rápido de Registros"));
        
        JLabel lblB = new JLabel("Filtrar Matrículas:"); lblB.setForeground(Color.BLACK);
        txtBuscarMatriculas = new JTextField(30); txtBuscarMatriculas.setForeground(Color.BLACK);
        panelBusqueda.add(lblB); panelBusqueda.add(txtBuscarMatriculas);
        p.add(panelBusqueda, BorderLayout.NORTH);

        String[] headers = {"ID", "Matrícula Asignada", "Nombre del Portador", "Rol Institucional"};
        modeloMatriculas = new DefaultTableModel(headers, 0);
        tablaMatriculasGlobales = new JTable(modeloMatriculas);
        tablaMatriculasGlobales.setForeground(Color.BLACK);
        
        sorterMatriculas = new TableRowSorter<>(modeloMatriculas);
        tablaMatriculasGlobales.setRowSorter(sorterMatriculas);

        p.add(new JScrollPane(tablaMatriculasGlobales), BorderLayout.CENTER);

        txtBuscarMatriculas.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBuscarMatriculas.getText().trim();
                if (texto.isEmpty()) sorterMatriculas.setRowFilter(null);
                else sorterMatriculas.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
            }
        });

        return p;
    }

    private JPanel crearPestanaUsuariosGlobales() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(COLOR_FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(Color.WHITE);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Mantenimiento de Credenciales"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panelForm.setPreferredSize(new Dimension(380, 700));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        txtIdOculto = new JTextField(); txtIdOculto.setVisible(false);
        gbc.gridy = 0; panelForm.add(txtIdOculto, gbc);

        Font fuenteInputs = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel lblU = new JLabel("Cuenta de Usuario:"); lblU.setForeground(Color.BLACK);
        gbc.gridy = 1; panelForm.add(lblU, gbc);
        txtNewUsuario = new JTextField(); txtNewUsuario.setFont(fuenteInputs); txtNewUsuario.setForeground(Color.BLACK);
        gbc.gridy = 2; panelForm.add(txtNewUsuario, gbc);

        JLabel lblP = new JLabel("Contraseña de Acceso:"); lblP.setForeground(Color.BLACK);
        gbc.gridy = 3; panelForm.add(lblP, gbc);
        txtNewPassword = new JTextField(); txtNewPassword.setFont(fuenteInputs); txtNewPassword.setForeground(Color.BLACK);
        gbc.gridy = 4; panelForm.add(txtNewPassword, gbc);

        JLabel lblN = new JLabel("Nombre Completo:"); lblN.setForeground(Color.BLACK);
        gbc.gridy = 5; panelForm.add(lblN, gbc);
        txtNewNombre = new JTextField(); txtNewNombre.setFont(fuenteInputs); txtNewNombre.setForeground(Color.BLACK);
        gbc.gridy = 6; panelForm.add(txtNewNombre, gbc);

        JLabel lblE = new JLabel("Edad:"); lblE.setForeground(Color.BLACK);
        gbc.gridy = 7; panelForm.add(lblE, gbc);
        txtNewEdad = new JTextField(); txtNewEdad.setFont(fuenteInputs); txtNewEdad.setForeground(Color.BLACK);
        gbc.gridy = 8; panelForm.add(txtNewEdad, gbc);

        JLabel lblR = new JLabel("Rol del Usuario (Tipo):"); lblR.setForeground(Color.BLACK);
        gbc.gridy = 9; panelForm.add(lblR, gbc);
        cbNewRol = new JComboBox<>(new String[]{"ALUMNO", "PROFESOR", "ADMIN"});
        cbNewRol.setFont(fuenteInputs); cbNewRol.setForeground(Color.BLACK);
        gbc.gridy = 10; panelForm.add(cbNewRol, gbc);

        JLabel lblM = new JLabel("Matrícula Generada automáticamente:"); lblM.setForeground(Color.BLUE);
        gbc.gridy = 11; panelForm.add(lblM, gbc);
        txtNewMatricula = new JTextField(); txtNewMatricula.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtNewMatricula.setForeground(Color.RED); txtNewMatricula.setEditable(false); 
        gbc.gridy = 12; panelForm.add(txtNewMatricula, gbc);

        JPanel panelAccionesBotones = new JPanel(new GridLayout(3, 2, 6, 6));
        panelAccionesBotones.setOpaque(false);

        JButton btnAnadir = new JButton("Añadir"); btnAnadir.setBackground(new Color(110, 231, 183)); btnAnadir.setForeground(Color.BLACK); btnAnadir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JButton btnModificar = new JButton("Modificar"); btnModificar.setBackground(new Color(252, 211, 77)); btnModificar.setForeground(Color.BLACK); btnModificar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JButton btnEliminar = new JButton("Eliminar"); btnEliminar.setBackground(new Color(252, 165, 165)); btnEliminar.setForeground(Color.BLACK); btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JButton btnLimpiar = new JButton("Limpiar"); btnLimpiar.setBackground(new Color(209, 213, 219)); btnLimpiar.setForeground(Color.BLACK); btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JButton btnRecargar = new JButton("Recargar"); btnRecargar.setBackground(new Color(199, 210, 254)); btnRecargar.setForeground(Color.BLACK); btnRecargar.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panelAccionesBotones.add(btnAnadir); panelAccionesBotones.add(btnModificar);
        panelAccionesBotones.add(btnEliminar); panelAccionesBotones.add(btnLimpiar);
        panelAccionesBotones.add(btnRecargar);

        gbc.gridy = 13; gbc.insets = new Insets(15, 6, 6, 6);
        panelForm.add(panelAccionesBotones, gbc);
        p.add(panelForm, BorderLayout.WEST);

        JPanel panelDerechoContenedor = new JPanel(new BorderLayout(10, 10));
        panelDerechoContenedor.setOpaque(false);

        JPanel panelBusquedaUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusquedaUser.setBackground(Color.WHITE);
        panelBusquedaUser.setBorder(BorderFactory.createTitledBorder("Filtrar Cuentas"));
        JLabel lblBU = new JLabel("Buscar Usuario:"); lblBU.setForeground(Color.BLACK);
        txtBuscarUsuarios = new JTextField(35); txtBuscarUsuarios.setForeground(Color.BLACK);
        panelBusquedaUser.add(lblBU); panelBusquedaUser.add(txtBuscarUsuarios);
        panelDerechoContenedor.add(panelBusquedaUser, BorderLayout.NORTH);

        String[] headersUser = {"ID", "Usuario", "Contraseña", "Nombre", "Rol", "Edad", "Matrícula"};
        modeloTablaUsuarios = new DefaultTableModel(headersUser, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        tablaUsuariosAdmin = new JTable(modeloTablaUsuarios);
        tablaUsuariosAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUsuariosAdmin.setForeground(Color.BLACK);
        tablaUsuariosAdmin.setSelectionBackground(new Color(199, 210, 254));
        tablaUsuariosAdmin.setSelectionForeground(Color.BLACK);
        
        sorterUsuarios = new TableRowSorter<>(modeloTablaUsuarios);
        tablaUsuariosAdmin.setRowSorter(sorterUsuarios);
        
        panelDerechoContenedor.add(new JScrollPane(tablaUsuariosAdmin), BorderLayout.CENTER);
        p.add(panelDerechoContenedor, BorderLayout.CENTER);

        DocumentListener oyenteMatriculaAuto = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { autoGenerar(); }
            public void removeUpdate(DocumentEvent e) { autoGenerar(); }
            public void changedUpdate(DocumentEvent e) { autoGenerar(); }
            
            private void autoGenerar() {
                String nombre = txtNewNombre.getText().trim();
                String edad = txtNewEdad.getText().trim();
                String rol = cbNewRol.getSelectedItem() != null ? cbNewRol.getSelectedItem().toString() : "ALUMNO";

                if (rol.equals("ADMIN")) {
                    txtNewMatricula.setText("N/A (ADMIN)");
                    return;
                }
                if (nombre.isEmpty()) {
                    txtNewMatricula.setText("");
                    return;
                }

                StringBuilder iniciales = new StringBuilder();
                for (String palabra : nombre.split(" ")) {
                    if (!palabra.isEmpty()) {
                        iniciales.append(Character.toUpperCase(palabra.charAt(0)));
                    }
                }
                
                String prefijo = rol.equals("ALUMNO") ? "ALUM-" : "PROF-";
                String sufijoEdad = edad.isEmpty() ? "00" : edad;
                
                txtNewMatricula.setText(prefijo + iniciales.toString() + "-" + sufijoEdad);
            }
        };

        txtNewNombre.getDocument().addDocumentListener(oyenteMatriculaAuto);
        txtNewEdad.getDocument().addDocumentListener(oyenteMatriculaAuto);
        cbNewRol.addActionListener(e -> oyenteMatriculaAuto.insertUpdate(null));

        txtBuscarUsuarios.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBuscarUsuarios.getText().trim();
                if (texto.isEmpty()) sorterUsuarios.setRowFilter(null);
                else sorterUsuarios.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
            }
        });

        tablaUsuariosAdmin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaUsuariosAdmin.getSelectedRow();
                if(fila >= 0) {
                    int realRow = tablaUsuariosAdmin.convertRowIndexToModel(fila);
                    txtIdOculto.setText(modeloTablaUsuarios.getValueAt(realRow, 0).toString());
                    txtNewUsuario.setText(modeloTablaUsuarios.getValueAt(realRow, 1).toString());
                    txtNewPassword.setText(modeloTablaUsuarios.getValueAt(realRow, 2).toString());
                    txtNewNombre.setText(modeloTablaUsuarios.getValueAt(realRow, 3).toString());
                    cbNewRol.setSelectedItem(modeloTablaUsuarios.getValueAt(realRow, 4).toString());
                    txtNewEdad.setText(modeloTablaUsuarios.getValueAt(realRow, 5).toString());
                    txtNewMatricula.setText(modeloTablaUsuarios.getValueAt(realRow, 6).toString());
                }
            }
        });

        btnAnadir.addActionListener(e -> ejecutarInsercionUsuarioSQL());
        btnModificar.addActionListener(e -> ejecutarModificacionUsuarioSQL());
        btnEliminar.addActionListener(e -> ejecutarEliminacionUsuarioSQL());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRecargar.addActionListener(e -> { recargarTablaUsuariosSQL(); recargarTablaMatriculasSQL(); });

        return p;
    }

    private void limpiarFormulario() {
        txtIdOculto.setText(""); txtNewUsuario.setText(""); txtNewPassword.setText("");
        txtNewNombre.setText(""); txtNewEdad.setText(""); txtNewMatricula.setText("");
        cbNewRol.setSelectedIndex(0); tablaUsuariosAdmin.clearSelection();
    }

    private String resolverMatriculaUnica(String matriculaBase) {
        if (matriculaBase.equals("N/A (ADMIN)")) return matriculaBase;
        String query = "SELECT COUNT(*) FROM usuarios WHERE matricula = ?";
        String matriculaCandidata = matriculaBase;
        int contador = 1;
        try (Connection cn = Conexion.getConexion()) {
            while (true) {
                try (PreparedStatement ps = cn.prepareStatement(query)) {
                    ps.setString(1, matriculaCandidata);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            matriculaCandidata = matriculaBase + "-" + contador;
                            contador++;
                        } else { break; }
                    }
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return matriculaCandidata;
    }

    private void ejecutarInsercionUsuarioSQL() {
        String user = txtNewUsuario.getText().trim(); String pass = txtNewPassword.getText().trim();
        String nombre = txtNewNombre.getText().trim(); String rol = cbNewRol.getSelectedItem().toString();
        String edadStr = txtNewEdad.getText().trim();

        if (user.isEmpty() || pass.isEmpty() || nombre.isEmpty() || edadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos vacíos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String matriculaFinal = resolverMatriculaUnica(txtNewMatricula.getText().trim());
        String sql = "INSERT INTO usuarios (usuario, contrasenia, nombre_completo, tipo_usuario, matricula, edad) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, user); ps.setString(2, pass); ps.setString(3, nombre); ps.setString(4, rol);
            if (rol.equals("ADMIN")) ps.setNull(5, java.sql.Types.VARCHAR); else ps.setString(5, matriculaFinal);
            ps.setInt(6, Integer.parseInt(edadStr));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "¡Usuario añadido con éxito!\nMatrícula asignada: " + matriculaFinal);
            limpiarFormulario(); recargarTablaUsuariosSQL(); recargarTablaMatriculasSQL();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error al insertar: " + ex.getMessage()); }
    }

    private void ejecutarModificacionUsuarioSQL() {
        String idStr = txtIdOculto.getText().trim();
        if (idStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Selecciona una fila."); return; }

        String user = txtNewUsuario.getText().trim(); String pass = txtNewPassword.getText().trim();
        String nombre = txtNewNombre.getText().trim(); String rol = cbNewRol.getSelectedItem().toString();
        String edadStr = txtNewEdad.getText().trim();

        String matriculaFinal = resolverMatriculaUnica(txtNewMatricula.getText().trim());
        String sql = "UPDATE usuarios SET usuario=?, contrasenia=?, nombre_completo=?, tipo_usuario=?, matricula=?, edad=? WHERE id_usuario=?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, user); ps.setString(2, pass); ps.setString(3, nombre); ps.setString(4, rol);
            if (rol.equals("ADMIN")) ps.setNull(5, java.sql.Types.VARCHAR); else ps.setString(5, matriculaFinal);
            ps.setInt(6, Integer.parseInt(edadStr)); ps.setInt(7, Integer.parseInt(idStr));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Datos actualizados correctamente.");
            limpiarFormulario(); recargarTablaUsuariosSQL(); recargarTablaMatriculasSQL();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error al modificar: " + ex.getMessage()); }
    }

    private void ejecutarEliminacionUsuarioSQL() {
        String idStr = txtIdOculto.getText().trim();
        if (idStr.isEmpty()) return;
        int conf = JOptionPane.showConfirmDialog(this, "¿Eliminar este usuario?", "Baja", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM usuarios WHERE id_usuario=?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idStr)); ps.executeUpdate();
            limpiarFormulario(); recargarTablaUsuariosSQL(); recargarTablaMatriculasSQL();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void recargarTablaUsuariosSQL() {
        if (modeloTablaUsuarios == null) return;
        modeloTablaUsuarios.setRowCount(0);
        String sql = "SELECT id_usuario, usuario, contrasenia, nombre_completo, tipo_usuario, edad, matricula FROM usuarios";
        try (Connection cn = Conexion.getConexion(); Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] fila = new Object[7];
                fila[0] = rs.getInt("id_usuario"); fila[1] = rs.getString("usuario");
                fila[2] = rs.getString("contrasenia"); fila[3] = rs.getString("nombre_completo");
                fila[4] = rs.getString("tipo_usuario"); fila[5] = rs.getInt("edad");
                fila[6] = rs.getString("matricula") != null ? rs.getString("matricula") : "N/A (ADMIN)";
                modeloTablaUsuarios.addRow(fila);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void recargarTablaMatriculasSQL() {
        if (modeloMatriculas == null) return;
        modeloMatriculas.setRowCount(0);
        String sql = "SELECT id_usuario, matricula, nombre_completo, tipo_usuario FROM usuarios WHERE matricula IS NOT NULL";
        try (Connection cn = Conexion.getConexion(); Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] fila = new Object[4];
                fila[0] = rs.getInt("id_usuario");
                fila[1] = rs.getString("matricula");
                fila[2] = rs.getString("nombre_completo");
                fila[3] = rs.getString("tipo_usuario");
                modeloMatriculas.addRow(fila);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}