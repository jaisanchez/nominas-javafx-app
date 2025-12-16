/**
 * Objetivo de la clase: Gestionar el acceso a la base de datos SQLite utilizada
 *                       para almacenar los cálculos de nómina.

 * La base de datos usada es: `calcu_nomina.db`
 * 
 * @author Jairo Sánchez Ballesteros
 */

package d_basedatos;

import java.sql.*;
import org.json.JSONObject;

public class D_BaseDatos {
    
    /*
     * URL de conexión a la base de datos SQLite.
     */
    private static final String URL = "jdbc:sqlite:calcu_nomina.db";
    
    /**
     * Crea la tabla "calculos" si no existe.
     * La tabla incluye:
     * - fecha (clave primaria)
     * - salario_bruto
     * - salario_neto
     * - detalle_json (campo para almacenar detalles como JSON)
     */
    public static void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS calculos (" +
                     "fecha TEXT PRIMARY KEY," +
                     "salario_bruto REAL NOT NULL," +
                     "salario_neto REAL NOT NULL," +
                     "detalle_json TEXT)";
        
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creando tabla: " + e.getMessage());
        }
    }
    
    /**
     * Establece una conexión con la base de datos.
     * 
     * @return Objeto Connection a la base de datos.
     * @throws SQLException Si ocurre un error al conectar.
     */
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Inserta un nuevo cálculo en la tabla.
     * También crea un objeto JSON con los datos básicos (fecha, bruto, neto).
     * 
     * @param fecha Fecha del cálculo.
     * @param bruto Salario bruto calculado.
     * @param neto  Salario neto calculado.
     */
    public static void insertarCalculo(String fecha, double bruto, double neto) {
        JSONObject detalle = new JSONObject();
        detalle.put("fecha", fecha);
        detalle.put("bruto", bruto);
        detalle.put("neto", neto);
        
        String sql = "INSERT INTO calculos (fecha, salario_bruto, salario_neto, detalle_json) VALUES (?, ?, ?, ?)";
        try (Connection conn = conectar(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fecha);
            pstmt.setDouble(2, bruto);
            pstmt.setDouble(3, neto);
            pstmt.setString(4, detalle.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error insertando cálculo: " + e.getMessage());
        }
    }

    /**
     * Recupera el contenido JSON detallado de un cálculo, a partir de la fecha.
     * 
     * @param fecha Fecha del cálculo buscado.
     * @return Cadena JSON con el detalle o `null` si no se encuentra.
     */
    public static String obtenerDetallePorFecha(String fecha) {
        String sql = "SELECT detalle_json FROM calculos WHERE fecha = ?";
        try (Connection conn = conectar(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fecha);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("detalle_json");
            }
        } catch (SQLException e) {
            System.err.println("Error consultando detalle: " + e.getMessage());
        }
        return null;
    }

    /**
     * Recupera todos los cálculos ordenados por fecha descendente.
     * 
     * @return ResultSet con las columnas: fecha, salario_bruto, salario_neto.
     * @throws SQLException Si ocurre un error en la consulta.
     */
    public static ResultSet obtenerTodosLosCalculos() throws SQLException {
        Connection conn = conectar();
        conn.setAutoCommit(false); // Mejora el control de transacciones
        
        String sql = "SELECT fecha, salario_bruto, salario_neto FROM calculos ORDER BY fecha DESC";
        Statement stmt = conn.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, 
            ResultSet.CONCUR_READ_ONLY
        );
        return stmt.executeQuery(sql);
    }
    
    /**
     * Cierra un ResultSet y sus recursos asociados (Statement y Connection).
     * 
     * @param rs ResultSet que se desea cerrar.
     */
    public static void cerrarResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                Statement stmt = rs.getStatement();
                Connection conn = stmt.getConnection();
                rs.close();
                stmt.close();
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error cerrando recursos: " + e.getMessage());
        }
    }
}