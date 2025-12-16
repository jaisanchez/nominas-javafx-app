/**
 * Objetivo de la clase: Es el controlador de la interfaz principal que maneja los
 *                       eventos de los botones. Permite realizar calculos, mostrarlos, guardarlos,
 *                       guardar y cargar parametros y salir de la aplicación.
 *
 * @author: Jairo Sánchez Ballesteros
 */

package b_controllers;

import d_basedatos.D_BaseDatos;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import org.json.JSONObject;

public class B_InterfazPrincipalController implements Initializable {

    // ==== Elementos de la interfaz ====

    /*
     * Campos de entrada relacionados con el trabajo realizado y conceptos salariales.
     */
    @FXML private TextField tfHorasNormales;
    @FXML private TextField tfHorasNocturnas;
    @FXML private TextField tfHorasExtras;
    @FXML private TextField tfHorasFestivas;
    @FXML private TextField tfSalarioBase;
    @FXML private TextField tfPlusTurnicidad;
    @FXML private TextField tfPlusNocturnidad;
    @FXML private TextField tfPlusHorasExtras;
    @FXML private TextField tfPlusFestivos;
    @FXML private TextField tfProrrataPagaExtra;
    @FXML private TextField tfProrrataVacaciones;
    @FXML private TextField tfCompensacionFinContrato;
    @FXML private TextField tfAtrasos;
    @FXML private TextField tfIRPF;

    /*
     * Áreas de texto para mostrar información procesada.
     */
    @FXML private TextArea taDatosSalariales;
    @FXML private TextArea taGastosAdicionales;
    @FXML private TextArea taRetenciones;

    /*
     * Campos relacionados con gastos adicionales.
     */
    @FXML private TextField tfDietaDiaria;
    @FXML private TextField tfDiasConDieta;
    @FXML private TextField tfKilometrosRecorridos;
    @FXML private TextField tfPrecioPorKilometro;
    @FXML private TextField tfNochesFuera;
    @FXML private TextField tfPrecioPorNoche;
    @FXML private TextField tfTotalTransportePublico;
    @FXML private TextField tfOtrosGastos;

    /*
     * Campos de aportación de seguridad social.
     */    
    @FXML private TextField tfContingenciasComunesTrabajador;
    @FXML private TextField tfDesempleoTrabajador;
    @FXML private TextField tfFormacionProfesionalTrabajador;
    @FXML private TextField tfPensionesMEITrabajador;
    @FXML private TextField tfContingenciasComunesEmpresa;
    @FXML private TextField tfATEPEmpresa;
    @FXML private TextField tfDesempleoEmpresa;
    @FXML private TextField tfFormacionProfesionalEmpresa;
    @FXML private TextField tfFOGASAEmpresa;
    @FXML private TextField tfPensionesMEIEmpresa;
    
    /*
     * Lista de historial de cálculos anteriores.
     */
    @FXML private ListView<String> lvHistoriallv;

    /*
     * Variables para almacenar resultados del cálculo actual.
     */
    private double salarioBrutoTributable;
    private double salarioBruto;
    private double salarioNeto;
    private String fechaActual;

    /**
     * Inicializa el controlador y carga la configuración almacenada.
     * También configura el evento de doble clic sobre el historial.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarConfiguracion();

        // Mostrar ventana de detalle al hacer doble clic en un ítem del historial
        lvHistoriallv.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = lvHistoriallv.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    mostrarVentanaDetalle(selectedItem);
                }
            }
        });
    }

    /**
     * Valida y convierte el valor numérico introducido en un campo de texto.
     *
     * @param tf Campo de texto a validar.
     * @param nombreCampo Nombre del campo para mostrar en el mensaje de error.
     * @return Valor numérico válido.
     * @throws NumberFormatException Si el valor no es numérico o es negativo.
     */
    private double lecturaConValidacionDeCampo(TextField tf, String nombreCampo) throws NumberFormatException {
        try {
            tf.setStyle("");
            double valor = Double.parseDouble(tf.getText().trim());
            if (valor < 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            tf.setStyle("-fx-border-color: red;");
            throw new NumberFormatException("Campo inválido: " + nombreCampo);
        }
    }

    /**
     * Calcula la nómina del trabajador en base a los datos ingresados y muestra los resultados.
     */
    @FXML
    private void calcularNomina() {
        try {
            // Lectura y validación de todos los campos necesarios
            double horasNormales = lecturaConValidacionDeCampo(tfHorasNormales, "Horas Normales");
            double horasNocturnas = lecturaConValidacionDeCampo(tfHorasNocturnas, "Horas Nocturnas");
            double horasExtras = lecturaConValidacionDeCampo(tfHorasExtras, "Horas Extras");
            double horasFestivas = lecturaConValidacionDeCampo(tfHorasFestivas, "Horas Festivas");
            double diasConDieta = lecturaConValidacionDeCampo(tfDiasConDieta, "Días con Dieta");
            double kilometrosRecorridos = lecturaConValidacionDeCampo(tfKilometrosRecorridos, "Kilómetros Recorridos");
            double diasAlojamiento = lecturaConValidacionDeCampo(tfNochesFuera, "Noches Fuera");
            double totalTransportePublico = lecturaConValidacionDeCampo(tfTotalTransportePublico, "Transporte Público");
            double otrosGastos = lecturaConValidacionDeCampo(tfOtrosGastos, "Otros Gastos");
            double salarioBase = lecturaConValidacionDeCampo(tfSalarioBase, "Salario Base");
            double plusTurnicidad = lecturaConValidacionDeCampo(tfPlusTurnicidad, "Plus Turnicidad");
            double plusNocturnidad = lecturaConValidacionDeCampo(tfPlusNocturnidad, "Plus Nocturnidad");
            double plusHorasExtras = lecturaConValidacionDeCampo(tfPlusHorasExtras, "Plus Horas Extras");
            double plusFestivos = lecturaConValidacionDeCampo(tfPlusFestivos, "Plus Festivos");
            double prorrataPagaExtra = lecturaConValidacionDeCampo(tfProrrataPagaExtra, "Prorrata Paga Extra");
            double prorrataVacaciones = lecturaConValidacionDeCampo(tfProrrataVacaciones, "Prorrata Vacaciones");
            double compensacionFinContrato = lecturaConValidacionDeCampo(tfCompensacionFinContrato, "Compensación Fin Contrato");
            double atrasos = lecturaConValidacionDeCampo(tfAtrasos, "Atrasos");

            double dietaDiaria = lecturaConValidacionDeCampo(tfDietaDiaria, "Dieta Diaria");
            double precioPorKilometro = lecturaConValidacionDeCampo(tfPrecioPorKilometro, "Precio por Kilómetro");
            double precioAlojamiento = lecturaConValidacionDeCampo(tfPrecioPorNoche, "Precio por Noche");

            double IRPF = lecturaConValidacionDeCampo(tfIRPF, "IRPF");
            double contingenciasComunesTrabajador = lecturaConValidacionDeCampo(tfContingenciasComunesTrabajador, "Contingencias Comunes Trabajador") / 100;
            double desempleoTrabajador = lecturaConValidacionDeCampo(tfDesempleoTrabajador, "Desempleo Trabajador") / 100;
            double fpTrabajador = lecturaConValidacionDeCampo(tfFormacionProfesionalTrabajador, "Formación Profesional Trabajador") / 100;
            double meiTrabajador = lecturaConValidacionDeCampo(tfPensionesMEITrabajador, "M.E.I. Trabajador") / 100;
            double contingenciasComunesEmpresa = lecturaConValidacionDeCampo(tfContingenciasComunesEmpresa, "Contingencias Comunes Empresa") / 100;
            double atEpEmpresa = lecturaConValidacionDeCampo(tfATEPEmpresa, "AT y EP Empresa") / 100;
            double desempleoEmpresa = lecturaConValidacionDeCampo(tfDesempleoEmpresa, "Desempleo Empresa") / 100;
            double fpEmpresa = lecturaConValidacionDeCampo(tfFormacionProfesionalEmpresa, "Formación Profesional Empresa") / 100;
            double fogasa = lecturaConValidacionDeCampo(tfFOGASAEmpresa, "FOGASA") / 100;
            double meiEmpresa = lecturaConValidacionDeCampo(tfPensionesMEIEmpresa, "M.E.I. Empresa") / 100;

            double devengosHorasNormales = salarioBase * horasNormales;
            double devengosPlusTurnicidad = plusTurnicidad * (horasNormales + horasNocturnas + horasExtras + horasFestivas);
            double devengosHorasNocturnas= (salarioBase + plusNocturnidad) * horasNocturnas;
            double devengosHorasExtras = (salarioBase + plusHorasExtras) * horasExtras;
            double devengosHorasFestivas= (salarioBase + plusFestivos) * horasFestivas;
            double devengosProrrataPagaExtra = prorrataPagaExtra * (horasNormales + horasNocturnas + horasExtras + horasFestivas);
            double devengosProrrataVacaciones = prorrataVacaciones * (horasNormales + horasNocturnas + horasExtras + horasFestivas);
            double devengosCompensacionFinContrato = compensacionFinContrato * (horasNormales + horasNocturnas + horasExtras + horasFestivas);

            double totalDietas = dietaDiaria * diasConDieta;
            double totalKilometraje = kilometrosRecorridos * precioPorKilometro;
            double totalAlojamiento = diasAlojamiento * precioAlojamiento;
            double totalGastosAdicionales = totalDietas + totalKilometraje + totalAlojamiento + totalTransportePublico + otrosGastos;

            double basesDeCotizacion = devengosHorasNormales + devengosPlusTurnicidad + devengosHorasNocturnas + devengosHorasExtras
                    + devengosHorasFestivas + devengosProrrataPagaExtra + devengosProrrataVacaciones + atrasos;

            double totalAportaciones_trabajador = basesDeCotizacion * (contingenciasComunesTrabajador + desempleoTrabajador + fpTrabajador + meiTrabajador);
            double totalAportaciones_empresa = basesDeCotizacion * (contingenciasComunesEmpresa + atEpEmpresa + desempleoEmpresa + fpEmpresa + fogasa + meiEmpresa);

            salarioBrutoTributable = devengosHorasNormales + devengosPlusTurnicidad + devengosHorasNocturnas
                    + devengosHorasExtras + devengosHorasFestivas + devengosProrrataPagaExtra
                    + devengosProrrataVacaciones + devengosCompensacionFinContrato + atrasos;
            
            salarioBruto = salarioBrutoTributable + totalGastosAdicionales;

            double retencionIRPF = salarioBrutoTributable * (IRPF / 100);
            double totalRetenciones = totalAportaciones_trabajador + retencionIRPF;
            salarioNeto = salarioBrutoTributable - totalRetenciones + totalGastosAdicionales;

            fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            taDatosSalariales.setText(
                    "=== Datos Salariales ===\n"
                    + "Fecha: " + fechaActual + "\n"
                    + "Horas normales: " + String.format("%.2f", devengosHorasNormales) + " €\n"
                    + "Horas nocturnas: " + String.format("%.2f", devengosHorasNocturnas) + " €\n"
                    + "Horas extras: " + String.format("%.2f", devengosHorasExtras) + " €\n"
                    + "Horas festivas: " + String.format("%.2f", devengosHorasFestivas) + " €\n"
                    + "Plus turnicidad: " + String.format("%.2f", devengosPlusTurnicidad) + " €\n"
                    + "Prorrata paga extra: " + String.format("%.2f", devengosProrrataPagaExtra) + " €\n"
                    + "Prorrata vacaciones: " + String.format("%.2f", devengosProrrataVacaciones) + " €\n"
                    + "Compensación fin contrato: " + String.format("%.2f", devengosCompensacionFinContrato) + " €\n"
                    + "Atrasos: " + String.format("%.2f", atrasos) + " €\n"
                    + "Salario bruto: " + String.format("%.2f", salarioBrutoTributable) + " €\n"
                    + "Salario neto: " + String.format("%.2f", salarioNeto) + " €"
            );

            taGastosAdicionales.setText(
                    "=== Gastos Adicionales (Exentos) ===\n"
                    + "Dietas: " + String.format("%.2f", totalDietas) + " €\n"
                    + "Kilometraje: " + String.format("%.2f", totalKilometraje) + " €\n"
                    + "Alojamiento: " + String.format("%.2f", totalAlojamiento) + " €\n"
                    + "Transporte público: " + String.format("%.2f", totalTransportePublico) + " €\n"
                    + "Otros gastos: " + String.format("%.2f", otrosGastos) + " €\n"
                    + "Total gastos adicionales: " + String.format("%.2f", totalGastosAdicionales) + " €"
            );

            taRetenciones.setText(
                    "=== Retenciones ===\n"
                    + "Aportaciones empresa: " + String.format("%.2f", totalAportaciones_empresa) + " €\n"
                    + "Aportaciones trabajador: " + String.format("%.2f", totalAportaciones_trabajador) + " €\n"
                    + "IRPF retenido: " + String.format("%.2f", retencionIRPF) + " €\n"
                    + "Total retenciones trabajador: " + String.format("%.2f", totalRetenciones) + " €"
            );

        } catch (NumberFormatException e) {
            taDatosSalariales.setText("Error: " + e.getMessage());
            taGastosAdicionales.setText("Error: " + e.getMessage());
            taRetenciones.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Guarda el cálculo actual en la base de datos en formato JSON.
     */
    @FXML
    private void guardarCalculo() {
        if (fechaActual != null) {
            D_BaseDatos.crearTabla();

            // Crear JSON extendido
            JSONObject detalle = new JSONObject();
            double brutoRedondeado = Math.round(salarioBruto * 100.0) / 100.0;
            double netoRedondeado = Math.round(salarioNeto * 100.0) / 100.0;

            detalle.put("fecha", fechaActual);
            detalle.put("bruto", brutoRedondeado);
            detalle.put("neto", netoRedondeado);
            detalle.put("datosSalariales", taDatosSalariales.getText());
            detalle.put("gastosAdicionales", taGastosAdicionales.getText());
            detalle.put("retenciones", taRetenciones.getText());

            try (Connection conn = D_BaseDatos.conectar()) {
                // Insertar el nuevo cálculo
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO calculos (fecha, salario_bruto, salario_neto, detalle_json) VALUES (?, ?, ?, ?)");
                pstmt.setString(1, fechaActual);
                pstmt.setDouble(2, brutoRedondeado);
                pstmt.setDouble(3, netoRedondeado);
                pstmt.setString(4, detalle.toString());
                pstmt.executeUpdate();

                taDatosSalariales.appendText("\n(Cálculo guardado)");
                taGastosAdicionales.setText("\n(Cálculo guardado)");
                taRetenciones.setText("\n(Cálculo guardado)");

            } catch (SQLException e) {
                taDatosSalariales.setText("Error al guardar: " + e.getMessage());
                taGastosAdicionales.setText("Error al guardar: " + e.getMessage());
                taRetenciones.setText("Error al guardar: " + e.getMessage());
            }
        } else {
            taDatosSalariales.setText("Primero calcula la nómina.");
            taGastosAdicionales.setText("Primero calcula la nómina.");
            taRetenciones.setText("Primero calcula la nómina.");
        }

        // Retardo para evitar registros duplicados
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Consulta y muestra los cálculos anteriores almacenados en la base de datos.
     */
    @FXML
    private void verHistorial() {
        ObservableList<String> historialItems = FXCollections.observableArrayList();
        lvHistoriallv.setItems(historialItems);

        try (Connection conn = D_BaseDatos.conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM calculos ORDER BY fecha DESC")) {
            while (rs.next()) {
                String fecha = rs.getString("fecha");
                double bruto = rs.getDouble("salario_bruto");
                double neto = rs.getDouble("salario_neto");

                String linea = "Fecha: " + fecha + " - Bruto: " + bruto + " € - Neto: " + neto + " €\n";

                historialItems.add(linea.trim()); // Añadir a la lista sin salto de línea
            }

        } catch (SQLException e) {
            historialItems.add("Error al consultar la base de datos: " + e.getMessage());
        }
    }

    /**
     * Elimina un cálculo seleccionado del historial, basado en la fecha.
     */
    @FXML
    private void eliminarRegistro() {
        String itemSeleccionado = lvHistoriallv.getSelectionModel().getSelectedItem();
        if (itemSeleccionado != null) {
            try {
                // Extrae la fecha del registro seleccionado y elimina de la base de datos.
                String fecha = itemSeleccionado.substring(itemSeleccionado.indexOf("Fecha: ") + 7, itemSeleccionado.indexOf(" - Bruto"));

                try (Connection conn = D_BaseDatos.conectar();
                        PreparedStatement stmt = conn.prepareStatement("DELETE FROM calculos WHERE fecha = ?")) {
                    stmt.setString(1, fecha);
                    int filas = stmt.executeUpdate();
                    if (filas > 0) {
                        taDatosSalariales.setText("Registro con fecha " + fecha + " eliminado.");
                        taGastosAdicionales.setText("Registro con fecha " + fecha + " eliminado.");
                        taRetenciones.setText("Registro con fecha " + fecha + " eliminado.");
                        verHistorial(); // Actualizar lista
                    } else {
                        taDatosSalariales.setText("Fecha " + fecha + " no encontrada.");
                        taGastosAdicionales.setText("Fecha " + fecha + " no encontrada.");
                        taRetenciones.setText("Fecha " + fecha + " no encontrada.");
                    }
                }
            } catch (Exception e) {
                taDatosSalariales.setText("Error al eliminar: " + e.getMessage());
                taGastosAdicionales.setText("Error al eliminar: " + e.getMessage());
                taRetenciones.setText("Error al eliminar: " + e.getMessage());
            }
        } else {
            taDatosSalariales.setText("Selecciona un registro en el historial para eliminarlo.");
            taGastosAdicionales.setText("Selecciona un registro en el historial para eliminarlo.");
            taRetenciones.setText("Selecciona un registro en el historial para eliminarlo.");
        }
    }

    /**
     * Guarda en un archivo de configuración (`config.properties`) los valores introducidos.
     * Se escriben los valores actuales de los campos en un archivo de propiedades.
     */
    @FXML
    private void guardarConfiguracion() {
        Properties props = new Properties();

        props.setProperty("atrasos", tfAtrasos.getText());
        props.setProperty("atepEmpresa", tfATEPEmpresa.getText());
        props.setProperty("ccEmpresa", tfContingenciasComunesEmpresa.getText());
        props.setProperty("ccTrabajador", tfContingenciasComunesTrabajador.getText());
        props.setProperty("compensacionFin", tfCompensacionFinContrato.getText());
        props.setProperty("desempleoEmpresa", tfDesempleoEmpresa.getText());
        props.setProperty("desempleoTrabajador", tfDesempleoTrabajador.getText());
        props.setProperty("dietaDiaria", tfDietaDiaria.getText());
        props.setProperty("diasDieta", tfDiasConDieta.getText());
        props.setProperty("festivas", tfHorasFestivas.getText());
        props.setProperty("fogasaEmpresa", tfFOGASAEmpresa.getText());
        props.setProperty("fpEmpresa", tfFormacionProfesionalEmpresa.getText());
        props.setProperty("fpTrabajador", tfFormacionProfesionalTrabajador.getText());
        props.setProperty("horasExtras", tfHorasExtras.getText());
        props.setProperty("horasNormales", tfHorasNormales.getText());
        props.setProperty("irpf", tfIRPF.getText());
        props.setProperty("kilometros", tfKilometrosRecorridos.getText());
        props.setProperty("meiEmpresa", tfPensionesMEIEmpresa.getText());
        props.setProperty("meiTrabajador", tfPensionesMEITrabajador.getText());
        props.setProperty("nochesFuera", tfNochesFuera.getText());
        props.setProperty("nocturnas", tfHorasNocturnas.getText());
        props.setProperty("otrosGastos", tfOtrosGastos.getText());
        props.setProperty("plusFestivos", tfPlusFestivos.getText());
        props.setProperty("plusHorasExtras", tfPlusHorasExtras.getText());
        props.setProperty("plusNocturnidad", tfPlusNocturnidad.getText());
        props.setProperty("plusTurnicidad", tfPlusTurnicidad.getText());
        props.setProperty("precioKilometro", tfPrecioPorKilometro.getText());
        props.setProperty("precioNoche", tfPrecioPorNoche.getText());
        props.setProperty("prorrataExtra", tfProrrataPagaExtra.getText());
        props.setProperty("prorrataVacaciones", tfProrrataVacaciones.getText());
        props.setProperty("salarioBase", tfSalarioBase.getText());
        props.setProperty("transporte", tfTotalTransportePublico.getText());

        try {
            File file = new File("config.properties");
            if (!file.exists()) {
                file.createNewFile(); // Crear si no existe
            }

            try (OutputStream out = new FileOutputStream(file)) {
                props.store(out, "Configuración de Nómina");
                taDatosSalariales.setText("Configuración guardada correctamente.");
                taGastosAdicionales.setText("Configuración guardada correctamente.");
                taRetenciones.setText("Configuración guardada correctamente.");
            }
        } catch (IOException e) {
            taDatosSalariales.setText("Error al guardar configuración: " + e.getMessage());
            taGastosAdicionales.setText("Error al guardar configuración: " + e.getMessage());
            taRetenciones.setText("Error al guardar configuración: " + e.getMessage());
        }
    }


    /**
     * Lee el archivo de configuración (`config.properties`) y devuelve las propiedades.
     * 
     * @return Objeto Properties cargado desde archivo.
     */
    private Properties leerConfiguracionDesdeArchivo() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
        } catch (IOException e) {
            System.out.println("No se pudo cargar el archivo config.properties.");
            taDatosSalariales.setText("No se pudo cargar la configuración. Se usarán valores por defecto.");
            taGastosAdicionales.setText("No se pudo cargar la configuración. Se usarán valores por defecto.");
            taRetenciones.setText("No se pudo cargar la configuración. Se usarán valores por defecto.");
        }
        return props;
    }

    /**
     * Carga la configuración desde archivo y actualiza los campos de entrada con dichos valores.
     */
    @FXML
    private void cargarConfiguracion() {
        Properties config = leerConfiguracionDesdeArchivo();

        tfAtrasos.setText(config.getProperty("atrasos", "0"));
        tfATEPEmpresa.setText(config.getProperty("atepEmpresa", "2.75"));
        tfContingenciasComunesEmpresa.setText(config.getProperty("ccEmpresa", "23.6"));
        tfContingenciasComunesTrabajador.setText(config.getProperty("ccTrabajador", "4.7"));
        tfCompensacionFinContrato.setText(config.getProperty("compensacionFin", "0.366"));
        tfDesempleoEmpresa.setText(config.getProperty("desempleoEmpresa", "6.7"));
        tfDesempleoTrabajador.setText(config.getProperty("desempleoTrabajador", "1.6"));
        tfDietaDiaria.setText(config.getProperty("dietaDiaria", "25.0"));
        tfDiasConDieta.setText(config.getProperty("diasDieta", "0"));
        tfHorasFestivas.setText(config.getProperty("festivas", "0"));
        tfFOGASAEmpresa.setText(config.getProperty("fogasaEmpresa", "0.2"));
        tfFormacionProfesionalEmpresa.setText(config.getProperty("fpEmpresa", "0.6"));
        tfFormacionProfesionalTrabajador.setText(config.getProperty("fpTrabajador", "0.1"));
        tfHorasExtras.setText(config.getProperty("horasExtras", "0"));
        tfHorasNormales.setText(config.getProperty("horasNormales", "8"));
        tfIRPF.setText(config.getProperty("irpf", "15"));
        tfKilometrosRecorridos.setText(config.getProperty("kilometros", "0"));
        tfPensionesMEIEmpresa.setText(config.getProperty("meiEmpresa", "0.5"));
        tfPensionesMEITrabajador.setText(config.getProperty("meiTrabajador", "0.1"));
        tfNochesFuera.setText(config.getProperty("nochesFuera", "0"));
        tfHorasNocturnas.setText(config.getProperty("nocturnas", "0"));
        tfOtrosGastos.setText(config.getProperty("otrosGastos", "0"));
        tfPlusFestivos.setText(config.getProperty("plusFestivos", "5.0"));
        tfPlusHorasExtras.setText(config.getProperty("plusHorasExtras", "0"));
        tfPlusNocturnidad.setText(config.getProperty("plusNocturnidad", "2.0"));
        tfPlusTurnicidad.setText(config.getProperty("plusTurnicidad", "2.23"));
        tfPrecioPorKilometro.setText(config.getProperty("precioKilometro", "0.20"));
        tfPrecioPorNoche.setText(config.getProperty("precioNoche", "60.0"));
        tfProrrataPagaExtra.setText(config.getProperty("prorrataExtra", "1.58"));
        tfProrrataVacaciones.setText(config.getProperty("prorrataVacaciones", "0.79"));
        tfSalarioBase.setText(config.getProperty("salarioBase", "8.79"));
        tfTotalTransportePublico.setText(config.getProperty("transporte", "0"));

        taDatosSalariales.setText("Configuración cargada correctamente.");
        taGastosAdicionales.setText("Configuración cargada correctamente.");
        taRetenciones.setText("Configuración cargada correctamente.");
    }


    /**
     * Muestra una ventana emergente con el detalle completo del cálculo seleccionado en el historial.
     *
     * @param item Texto del ítem seleccionado que contiene la fecha del cálculo.
     */
    private void mostrarVentanaDetalle(String item) {
        try {
            // Extrae la fecha del historial, recupera el JSON y muestra el detalle en un cuadro de diálogo.
            int inicio = item.indexOf("Fecha: ") + 7;
            int fin = item.indexOf(" - Bruto");
            if (inicio < 7 || fin == -1) {
                throw new IllegalArgumentException("Formato de historial no válido");
            }

            String fecha = item.substring(inicio, fin).trim();

            String detalleJson = D_BaseDatos.obtenerDetallePorFecha(fecha);

            if (detalleJson != null) {
                JSONObject json = new JSONObject(detalleJson);

                String mensaje = json.optString("datosSalariales", "") + "\n\n"
                        + json.optString("gastosAdicionales", "") + "\n\n"
                        + json.optString("retenciones", "");

                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Detalle del Cálculo");
                alerta.setHeaderText("Registro guardado en: " + fecha);

                TextArea textArea = new TextArea(mensaje);
                textArea.setEditable(false);
                textArea.setWrapText(true);

                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);

                alerta.getDialogPane().setContent(textArea);

                // Tamaño de la ventana
                alerta.getDialogPane().setPrefWidth(400);
                alerta.getDialogPane().setPrefHeight(600);

                alerta.showAndWait();
            } else {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("Detalle no disponible");
                alerta.setHeaderText("No se encontró información");
                alerta.setContentText("No se encontró detalle para la fecha: " + fecha);
                alerta.showAndWait();
            }

        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error al mostrar detalle");
            alerta.setHeaderText("Se produjo una excepción");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    /**
     * Cierra la aplicación.
     */
    @FXML
    private void salirAplicacion() {
        System.exit(0);
    }

}
