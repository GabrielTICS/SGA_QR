# SGA_QR - Sistema de Gestion y Acceso con Autenticacion mediante Codigo QR

SGA_QR es un sistema de escritorio multiplataforma diseñado para la administracion de credenciales institucionales y el control de accesos logicos. La aplicacion implementa interfaces graficas de usuario basadas en Java Swing y delega la logica de autenticacion a un motor de lectura y generacion de codigos QR asistido por vision artificial en tiempo real.

## Arquitectura y Componentes Principales

El proyecto esta estructurado mediante componentes desacoplados que interactuan con una base de datos centralizada a traves de Java Database Connectivity (JDBC):

* **FrmLogin**: Formulario de acceso tradicional que implementa tecnicas de autenticacion SQL parametrizadas para mitigar ataques de inyeccion de codigo. Redirige dinamicamente segun el rol del usuario (Administrador o Usuario General).
* **Dashboard**: Panel administrativo global restringido para perfiles avanzados. Cuenta con modulos especificos para el mantenimiento de credenciales (CRUD de usuarios) y un buscador en tiempo real acoplado a filtros dinamicos de tablas.
* **DashboardUsuario**: Portal institucional del portador. Permite la visualizacion de datos academicos e invoca el motor de codificacion binaria para generar la credencial digital unica.
* **FrmEscaner**: Estacion de escaneo autonoma que inicializa hilos secundarios daemon para procesar flujos de video continuos, optimizar frames mediante procesamiento digital de imagenes y validar accesos automaticamente al detectar la matriz QR.
* **FrmGenerador**: Modulo utilitario para la transformacion de cadenas de texto plano y matriculas directamente a formatos de codigos de barra bidimensionales.

## Stack Tecnologico

* **Lenguaje de Programacion:** Java (Compatibilidad verificada en entornos macOS/Windows)
* **Gestor de Proyectos y Dependencias:** Apache Maven
* **Entorno de Desarrollo Integrado:** NetBeans IDE
* **Procesamiento de Vision Artificial:** OpenCV (Carga de librerias nativas locales)
* **Procesamiento y Decodificacion de Imagenes:** Google ZXing (Zebra Crossing Core / Java SE Extensions)
* **Conexion de Base de Datos:** Driver JDBC (MySQL/PostgreSQL)

## Requisitos e Instalacion Local

Para desplegar y ejecutar el proyecto en un entorno de desarrollo local, siga las instrucciones detalladas a continuacion:

1. Clonar el repositorio remoto en su maquina local:
   ```bash
   git clone https://github.com/GabrielTICS/SGA_QR.git
   ```
2. Inicializar NetBeans IDE u otro entorno compatible y proceder con la importacion del proyecto Maven existente.
3. Configurar las variables de conexion a la base de datos dentro de la clase utilitaria `Conexion`.
4. Ejecutar el ciclo de construccion de Maven para descargar las dependencias declaradas en el archivo pom.xml:
   ```bash
   mvn clean install
   ```
5. Ejecutar la clase principal `FrmLogin` En la terminal para tener autorización de la camara para iniciar la aplicacion.
