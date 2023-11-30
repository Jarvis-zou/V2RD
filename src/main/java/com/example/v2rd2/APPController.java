package com.example.v2rd2;

import javafx.fxml.FXML;

import java.io.IOException;
import java.util.LinkedList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javafx.event.ActionEvent;
import javafx.application.Platform;

import org.bytedeco.javacv.FFmpegFrameGrabber;



public class APPController {
    // main user operation board
    @FXML
    private Pane functionBoard;
    private boolean videoPaused;


    // Details Panel
    @FXML
    private GridPane Details;
    @FXML
    private Label title;
    @FXML
    private Label resolution;
    @FXML
    private Label audioChannel;
    @FXML
    private Label fileLocation;
    @FXML
    private Label duration;
    @FXML
    private Label frameRate;
    @FXML
    private Label videoFormat;


    // Main Features
    @FXML
    private ListView videoList;
    @FXML
    private Menu OpenRecentMenu;
    @FXML
    private MediaView MediaPreview;

    private MediaPlayer mediaPlayer;

    private LinkedList<File> recentFiles = new LinkedList<>();
    private File selectedDirectory;

    @FXML
    protected void onFileOpenClick(ActionEvent event) throws IOException {
        // pop out a file chooser, this chooser allows only mp4 files to be chosen
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MP4 files (*.mp4)", "*.mp4");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(new Stage());

        // once open the mp4 file, user can immediately preview the file content
        if (file != null) {
            addFileToOpenRecentMenu(file);  // add file to recent file list
            playSelectedFile(file);
        }
    }

    @FXML
    private void addFileToOpenRecentMenu(File file) {
        String fileName = file.getName();

        // stores recent file in fxml
        MenuItem recentFileItem = new MenuItem(fileName);
        recentFileItem.setOnAction(event -> playSelectedFile(file));
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
            recentFileName.setOnAction(event -> playSelectedFile(recentFile));
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
                            ShowFileDetails(recentFile);
                            playSelectedFile(recentFile);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * This method initiate all components in the main operation board.
     */
    private void initFunctionBoard(boolean allowPreview) {
        // Initiate play/pause button
        Button playPauseButton = new Button();
        changeIcon(playPauseButton);

        // Bond click event
        playPauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Button Click
                // 1. Play/Pause video
                if (videoPaused) {
                    mediaPlayer.play();
                } else {
                    mediaPlayer.pause();
                }

                // 2. Change button icon
                videoPaused = !videoPaused;
                changeIcon(playPauseButton);
            }
        });

        // 这里的列表用另一个算法类的方法来代替
        ObservableList<String> effectOptions =
                FXCollections.observableArrayList("Effect 1", "Effect 2", "Effect 3");
        ComboBox<String> effectComboBox = new ComboBox<>(effectOptions);
        effectComboBox.setPromptText("Select Effect");

        // 选择特效算法之后的操作
        effectComboBox.setOnAction(event -> {
            String selectedEffect = effectComboBox.getValue();
            // 选择了之后和算法类内部比对
        });

        // Initiate transform button
        Button transformButton = new Button("Transform");

        // Initiate preview button
        Button previewButton = new Button("Preview");
        if (!allowPreview) {
            previewButton.setDisable(true);
        }
        previewButton.setOnAction(event -> {
//            File file = 算法保存的file
//            playSelectedFile();·
        });

        // Set layout and add components
        playPauseButton.setLayoutX(190);
        playPauseButton.setLayoutY(0);

        effectComboBox.setPrefWidth(200);
        effectComboBox.setLayoutX(20);
        effectComboBox.setLayoutY(70);

        transformButton.setLayoutX(340);
        transformButton.setPrefWidth(80);
        transformButton.setLayoutY(70);

        previewButton.setLayoutX(340);
        previewButton.setPrefWidth(80);
        previewButton.setLayoutY(110);

        functionBoard.getChildren().addAll(playPauseButton, effectComboBox, transformButton, previewButton);
    }

    private void changeIcon(Button playPauseButton) {
        Image newImage;
        if (!videoPaused) {
            newImage = new Image("pause.png");
        } else {
            newImage = new Image("play.png");
        }
        ImageView newImageView = new ImageView(newImage);
        newImageView.setFitWidth(30);
        newImageView.setFitHeight(30);
        playPauseButton.setGraphic(newImageView);
    }

    private void playSelectedFile(File file) {
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        MediaPreview.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(true);
        videoPaused = false;
        ShowFileDetails(file);

        // here need to check curr file preview status

        initFunctionBoard(false);  // show function board
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
        });
    }

    private void ShowFileDetails(File file) {
        // get video metadata using javacv
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        String filePath = file.getAbsolutePath();
        String fileTitle = fileName.substring(0, dotIndex);
        int width = 0;
        int height = 0;
        int channels = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        double fr = 0;
        String format = "unknown";



        try(FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filePath)) {
            grabber.start();
            width = grabber.getImageWidth();
            height = grabber.getImageHeight();
            channels = grabber.getAudioChannels();
            long dr = grabber.getLengthInTime() / 1000;  // total milliseconds
            hours = TimeUnit.MILLISECONDS.toHours(dr);
            minutes = TimeUnit.MILLISECONDS.toMinutes(dr) % 60;
            seconds = TimeUnit.MILLISECONDS.toSeconds(dr) % 60;
            fr = grabber.getFrameRate();
            int codec = grabber.getVideoCodec();
            if (codec == 28) {
                format = "H.264";
            }

            grabber.stop();
            grabber.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // show details in GridPane
        title.setText(fileTitle);
        resolution.setText(height + "(H)x" + width + "(W)");
        audioChannel.setText("" + channels);
        fileLocation.setText(filePath);
        duration.setText(hours + ":" + minutes + ":" + seconds);
        frameRate.setText(String.format("%.2f", fr));
        videoFormat.setText(format);
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
