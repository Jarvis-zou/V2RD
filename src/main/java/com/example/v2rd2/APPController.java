package com.example.v2rd2;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.application.Platform;

public class APPController {
    @FXML
    private MediaView MediaPreview;
    @FXML
    private MenuItem OpenFile;
    @FXML
    private MenuItem Quit;

    @FXML
    protected void onFileOpenClick(ActionEvent event) {
        // pop out a file chooser, this chooser allows only mp4 files to be chosen
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MP4 files (*.mp4)", "*.mp4");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(new Stage());

        // once open the mp4 file, user can immediately preview the file content
        if (file != null) {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaPreview.setMediaPlayer(mediaPlayer);
            mediaPlayer.play();
        }
    }

    @FXML
    protected void onQuitClick(ActionEvent event) {
        Platform.exit();
    }
}
