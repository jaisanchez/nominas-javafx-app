/**
 * Objetivo del proyecto: Calcular nóminas de forma automática.
 * Objetivo de la clase: Es la clase principal que se encarga de cargar 
 * la interfaz gráfica desde un archivo FXML y mostrar la ventana principal.
 * 
 * @author Jairo Sánchez Ballesteros
 */

package a_main;

import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal que extiende Application para lanzar la interfaz gráfica.
 */
public class A_Main extends Application {

    /**
     * Inicia la aplicación cargando la interfaz principal.
     * 
     * @param primaryStage La ventana principal de la aplicación.
     * @throws Exception Si ocurre un error al cargar la interfaz.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el archivo FXML como nodo raíz
        Parent root = FXMLLoader.load(getClass().getResource("/c_view/C_InterfazPrincipal.fxml"));

        // Crear escena y aplicar hoja de estilos
        Scene scene = new Scene(root, 700, 860);
        scene.getStylesheets().add(getClass().getResource("/e_estilos/E_Estilo1.css").toExternalForm());

        // Configurar y mostrar la ventana
        primaryStage.setTitle("Calculadora de Nómina");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Establecer el icono
        primaryStage.getIcons().add(
                new Image(new File("icono1_png.png").toURI().toString())
        );
    }

    /**
     * Método principal que lanza la aplicación.
     * 
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
