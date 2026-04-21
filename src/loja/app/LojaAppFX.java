package loja.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.application.Application;
import javafx.stage.Stage;

public class LojaAppFX extends Application {

    public LojaAppFX() {
        super();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loja/view/LojaApp.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);

        primaryStage.setTitle("Controle de Loja");
        primaryStage.setScene(scene);

        // TAMANHO MÍNIMO (importante pra não quebrar layout)
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.centerOnScreen();

        /*primaryStage.setMaximized(true);
        *primaryStage.setResizable(true);*/

        // ✔️ Mostra normal (janela redimensionável)
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
