package com.example.v2rd2;

import javafx.fxml.FXML;
import java.util.LinkedList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.application.Platform;

public class APPController {
    @FXML
    private ListView videoList;
    @FXML
    private Menu OpenRecentMenu;
    @FXML
    private MediaView MediaPreview;

    private LinkedList<File> recentFiles = new LinkedList<>();
    private File selectedDirectory;

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
            addFileToOpenRecentMenu(file);  // add file to recent file list
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaPreview.setMediaPlayer(mediaPlayer);
            mediaPlayer.play();
        }
    }

    @FXML
    private void addFileToOpenRecentMenu(File file) {
        String fileName = file.getName();

        // stores recent file in fxml
        MenuItem recentFileItem = new MenuItem(fileName);
        recentFileItem.setOnAction(event -> openRecentFile(file));
        OpenRecentMenu.getItems().add(recentFileItem);

        // after we add this file into RecentFileMenu, we add it into a listview
        if (!videoList.getItems().contains(fileName)) {
            videoList.getItems().add(fileName);
        }

        // add file to first position because the most recent file should be above
        recentFiles.remove(file);
        recentFiles.addFirst(file);

        // the menu should only show up to 5 recent files
        OpenRecentMenu.getItems().clear();

        // rewrite menuitem in new order
        int count = 0;
        for (File recentFile : recentFiles) {
            if (count >= 5) { break; }
            MenuItem recentFileName = new MenuItem(recentFile.getName());
            recentFileName.setOnAction(event -> openRecentFile(recentFile));
            OpenRecentMenu.getItems().add(recentFileName);
            count++;
        }

        // double click then user can preview the file
        videoList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Object selectedFile = videoList.getSelectionModel().getSelectedItem();
                if (selectedFile != null && selectedFile instanceof String) {
                    String selectedFileName = (String) selectedFile;
                    for (File recentFile : recentFiles) {
                        if (recentFile.getName().equals(selectedFileName)) {
                            playSelectedFile(recentFile);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void playSelectedFile(File file) {
        Media media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaPreview.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
    }

    @FXML
    private void openRecentFile(File file) {
        Media media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaPreview.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
    }

    @FXML
    protected void onSaveToClick(ActionEvent event) {
        // User decide files will be saved to which directory
        // Save path will be stored in this.selectedDirectory
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to Save");
        selectedDirectory = directoryChooser.showDialog(new Stage());
    }

    @FXML
    protected void onQuitClick(ActionEvent event) {
        Platform.exit();
    }
}
