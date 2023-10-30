/**
 * This is application class is the main class, it will handle all GUI related operations as the controller of the system.
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建菜单
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open");
        MenuItem openRecentItem = new MenuItem("Open Recent");
        fileMenu.getItems().add(openItem);
        fileMenu.getItems().add(openRecentItem);

        // 创建菜单栏并添加菜单
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileMenu);

        // 设置布局和场景
        VBox vBox = new VBox(menuBar);
        Scene scene = new Scene(vBox, 960, 600);

        openItem.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                // 打开并处理文件
                // ...
            }
        });

        // (可选) 为 "Open Recent" 实现逻辑
        // ...

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX App");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
