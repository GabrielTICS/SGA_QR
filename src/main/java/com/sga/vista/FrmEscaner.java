package com.sga.vista;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

// Importaciones para la conexión a la Base de Datos
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.mycompany.sga_qr.conexion.Conexion;

public class FrmEscaner extends JFrame {

    private JLabel lblTitulo;
    private JTextField txtMatricula;
    private JPanel contenedorCamara;
    private JLabel lblVideoFeed; 

    private VideoCapture camara;
    private boolean ejecutando = true;

    // Paleta de colores consistente con la identidad del sistema
    private final Color COLOR_PRIMARIO = new Color(30, 41, 59); // Slate 800
    private final Color COLOR_FONDO = new Color(241, 245, 249);    // Gris claro limpio

    public FrmEscaner() {
        // Cargar las librerías nativas de OpenCV para Mac
        nu.pattern.OpenCV.loadLocally();

        setTitle("SGA_QR - Estación de Escaneo Autónoma");
        setSize(660, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COLOR_FONDO);

        inicializarComponentes();

        // Hilo secundario para captura de video y lectura de QR
        Thread hiloEscaneo = new Thread(this::procesarCamaraYQR);
        hiloEscaneo.setDaemon(true);
        hiloEscaneo.start();
    }

    private void inicializarComponentes() {
        lblTitulo = new JLabel("Muestre su código QR frente a la cámara", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        
        // Panel superior (Banner informativo y de feedback de bienvenida)
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(COLOR_PRIMARIO);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        panelHeader.add(lblTitulo, BorderLayout.CENTER);
        add(panelHeader, BorderLayout.NORTH);

        contenedorCamara = new JPanel(new BorderLayout());
        contenedorCamara.setBackground(Color.BLACK);
        contenedorCamara.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 4));
        
        lblVideoFeed = new JLabel();
        lblVideoFeed.setHorizontalAlignment(SwingConstants.CENTER);
        contenedorCamara.add(lblVideoFeed, BorderLayout.CENTER);
        add(contenedorCamara, BorderLayout.CENTER);

        txtMatricula = new JTextField();
        txtMatricula.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtMatricula.setHorizontalAlignment(JTextField.CENTER);
        txtMatricula.setForeground(Color.BLACK);
        txtMatricula.setBackground(Color.WHITE);
        txtMatricula.setEditable(false); // Evita manipulación manual durante el escaneo continuo
        
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        panelInferior.setOpaque(false);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JLabel lblInfoMatricula = new JLabel("Última Matrícula Detectada por el Sensor:", SwingConstants.CENTER);
        lblInfoMatricula.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfoMatricula.setForeground(COLOR_PRIMARIO);
        
        panelInferior.add(lblInfoMatricula, BorderLayout.NORTH);
        panelInferior.add(txtMatricula, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void procesarCamaraYQR() {
        camara = new VideoCapture(0);
        Mat frameMat = new Mat();
        Mat frameRedimensionado = new Mat();

        if (!camara.isOpened()) {
            SwingUtilities.invokeLater(() -> {
                lblTitulo.setText("Error: No se pudo acceder a la cámara de la Mac.");
                lblTitulo.setForeground(new Color(239, 68, 68)); // Rojo sutil
            });
            return;
        }

        while (ejecutando) {
            if (camara.read(frameMat) && !frameMat.empty()) {
                // 1. Invertir horizontalmente para efecto espejo cómodo
                Core.flip(frameMat, frameMat, 1);
                
                // 2. Obtener el tamaño actual del contenedor en la app
                int anchoContenedor = contenedorCamara.getWidth();
                int altoContenedor = contenedorCamara.getHeight();
                
                if (anchoContenedor > 0 && altoContenedor > 0) {
                    // 3. Redimensionar el video al tamaño exacto de la interfaz usando interpolación lineal
                    Size nuevoTamano = new Size(anchoContenedor, altoContenedor);
                    Imgproc.resize(frameMat, frameRedimensionado, nuevoTamano, 0, 0, Imgproc.INTER_LINEAR);
                    
                    // Convertir matriz OpenCV redimensionada a BufferedImage de Java
                    BufferedImage imagen = matToBufferedImage(frameRedimensionado);
                    
                    if (imagen != null) {
                        ImageIcon icono = new ImageIcon(imagen);
                        lblVideoFeed.setIcon(icono);
                        
                        // Intentar decodificar el QR usando la imagen actual
                        analizarQR(imagen);
                    }
                }
            }

            try {
                Thread.sleep(33); // Mantener ~30 FPS estables
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        camara.release();
    }

    private void analizarQR(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);

            if (result != null) {
                String codigoDetectado = result.getText().trim();
                
                // Reflejamos visualmente la matrícula detectada en la interfaz
                txtMatricula.setText(codigoDetectado);
                System.out.println("¡Código QR leído!: " + codigoDetectado);
                
                // Detenemos temporalmente el flujo de la cámara para procesar la lectura única
                ejecutando = false; 
                
                // Ejecutamos la consulta a la base de datos
                validarAccesoPorMatriculaSQL(codigoDetectado);
            }
        } catch (Exception e) {
            // No se detectó QR en este frame, continúa el flujo del bucle
        }
    }

    // --- CONEXIÓN Y LOGUEO DIRECTO VÍA MATRÍCULA ---
    private void validarAccesoPorMatriculaSQL(String matriculaInput) {
        String sql = "SELECT nombre_completo, tipo_usuario, matricula FROM usuarios WHERE matricula = ?";

        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matriculaInput);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre_completo");
                    String rol = rs.getString("tipo_usuario");
                    String matricula = rs.getString("matricula");

                    // --- FEEDBACK VISUAL INMEDIATO EN LA UI ---
                    // Cambia el banner a verde esmeralda y escribe el nombre del QR
                    SwingUtilities.invokeLater(() -> {
                        lblTitulo.setText("¡Bienvenido, " + nombre + "!");
                        lblTitulo.getParent().setBackground(new Color(16, 185, 129)); 
                    });

                    // Congela la pantalla por 3 segundos para que el usuario pueda ver su nombre
                    Thread.sleep(3000); 

                    // Apagamos físicamente el hardware de la cámara antes de abrir las nuevas ventanas
                    if (camara != null && camara.isOpened()) {
                        camara.release();
                    }

                    // Redirección inteligente de privilegios según el tipo de usuario de la BD
                    if (rol.equalsIgnoreCase("ADMIN")) {
                        Dashboard adminDash = new Dashboard(nombre, rol, matricula);
                        adminDash.setVisible(true);
                    } else {
                        DashboardUsuario userDash = new DashboardUsuario(nombre, rol, matricula);
                        userDash.setVisible(true);
                    }

                    this.dispose(); // Destruimos la ventana actual del escáner para liberar la RAM
                } else {
                    // Si el QR no coincide con ningún registro activo de la BD
                    SwingUtilities.invokeLater(() -> {
                        lblTitulo.setText("Acceso Denegado: Código QR no registrado.");
                        lblTitulo.getParent().setBackground(new Color(239, 68, 68)); // Rojo sutil
                    });
                    
                    Thread.sleep(2500); // Pausa para que alcancen a leer el mensaje de error
                    
                    // Restauramos el estado visual original del banner para el siguiente alumno
                    SwingUtilities.invokeLater(() -> {
                        lblTitulo.setText("Muestre su código QR frente a la cámara");
                        lblTitulo.getParent().setBackground(COLOR_PRIMARIO); 
                        txtMatricula.setText("");
                    });

                    // Reactivamos el bucle y reiniciamos el hilo de la cámara
                    ejecutando = true;
                    Thread hiloReinicio = new Thread(this::procesarCamaraYQR);
                    hiloReinicio.setDaemon(true);
                    hiloReinicio.start();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // En caso de una excepción o caída de red, permitimos reintentar el flujo
            ejecutando = true;
        }
    }

    private BufferedImage matToBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    // Método main para pruebas de ejecución directa individual o por terminal de comandos
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> {
            new FrmEscaner().setVisible(true);
        });
    }
}