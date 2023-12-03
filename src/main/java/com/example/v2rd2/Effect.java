package com.example.v2rd2;

import javafx.scene.control.ProgressBar;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Range;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import javafx.concurrent.Task;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class Effect {
    private final ArrayList<String> effectList;

    public Effect() {
        effectList = new ArrayList<>();
        effectList.add("Rolling Digits(Small Digits)");
        effectList.add("Rolling Digits(Big Digits)");
    }

    // Getter and Setter
    public ArrayList<String> getEffectList() {
        return this.effectList;
    }

    /**
     * In Future version, because we use Strategy design pattern, a new effect can be easily create in a new function, and
     * calling it by changing only the input parameter: effectName.
     * @param effectName: mp4 file object
     * @param originalVideo: save path
     * @param saveDir: under which videos with effect will be saved
     * @param progressBar: show process status
     */
    public void applyAlgo(String effectName, File originalVideo, File saveDir, ProgressBar progressBar) {
        switch (effectName) {
            case "Rolling Digits(Small Digits)":
                rollingDigits(originalVideo, saveDir, progressBar, "small");
                break;
            case "Rolling Digits(Big Digits)":
                rollingDigits(originalVideo, saveDir, progressBar, "big");
                break;
        }
    }

    // Effect Algorithms
    public void rollingDigits(File videoFile, File saveDir, ProgressBar progressBar, String digitType) {
        String videoFilePath = videoFile.getAbsolutePath();
        String newFileName = videoFile.getName().replaceFirst("[.][^.]+$", "") + "_transformed.mp4";
        File transformedVideo = new File(saveDir, newFileName);

        // Put process code in a task to prevent thread conflicts
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoFilePath)) {
                    frameGrabber.start();
                    FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder(transformedVideo, frameGrabber.getImageWidth(), frameGrabber.getImageHeight());

                    // set basic configurations
                    frameRecorder.setFrameRate(frameGrabber.getFrameRate());
                    frameRecorder.setVideoCodec(frameGrabber.getVideoCodec());
                    frameRecorder.setAudioCodec(frameGrabber.getAudioCodec());
                    frameRecorder.setAudioChannels(frameGrabber.getAudioChannels());
                    frameRecorder.setAudioBitrate(frameGrabber.getAudioBitrate());
                    frameRecorder.setSampleRate(frameGrabber.getSampleRate());
                    frameRecorder.start();

                    Frame frame;
                    int frameCount = frameGrabber.getLengthInFrames();
                    int processedFrames = 0;

                    while ((frame = frameGrabber.grabFrame()) != null) {
                        if (frame.image != null) {
                            Frame processedFrame = applyRDEffect(frame, digitType);  // apply effect
                            frameRecorder.record(processedFrame); // save frame
                        }
                        processedFrames++;
                        updateProgress(processedFrames, frameCount); // update progressBar
                    }

                    // close grabber and recorder after process finished
                    frameRecorder.stop();
                    frameGrabber.stop();
                    frameRecorder.release();
                    frameGrabber.release();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty()); // bind progress bar
        new Thread(task).start();
    }

    private static byte[] flatten(byte[][] array) {
        // transform 2D byte matrix into 1D format to fit Mat requirement
        int rows = array.length;
        int cols = array[0].length;
        byte[] flat = new byte[rows * cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(array[i], 0, flat, i * cols, cols);
        }

        return flat;
    }


    public Frame applyRDEffect(Frame frame, String digitType) {
        // Init preset digit patterns
        int blockSize = 0;
        byte[][] onePattern = new byte[0][];
        byte[][] zeroPattern = new byte[0][];
        byte[] onePatternFlat = new byte[0];
        byte[] zeroPatternFlat = new byte[0];
        Mat oneMat = null;
        Mat zeroMat = null;
        if (Objects.equals(digitType, "small")) {
            blockSize = 5;
            onePattern = new byte[][] {
                    {-1, -1,  0, -1, -1},
                    {-1,  0,  0, -1, -1},
                    {-1, -1,  0, -1, -1},
                    {-1,  0,  0,  0, -1},
                    {-1, -1, -1, -1, -1},
            };

            zeroPattern = new byte[][] {
                    {-1, -1, -1, -1, -1},
                    {-1,  0,  0,  0, -1},
                    {-1,  0, -1,  0, -1},
                    {-1,  0,  0,  0, -1},
                    {-1, -1, -1, -1, -1},
            };
            onePatternFlat = flatten(onePattern);
            zeroPatternFlat = flatten(zeroPattern);
            oneMat = new Mat(5, 5, CV_8UC1, new BytePointer(onePatternFlat));
            zeroMat = new Mat(5, 5, CV_8UC1, new BytePointer(zeroPatternFlat));
        }
        if (Objects.equals(digitType, "big")) {
            blockSize = 10;
            onePattern = new byte[][] {
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  0,  0, -1, -1, -1, -1},
                    {-1, -1, -1,  0,  0,  0, -1, -1, -1, -1},
                    {-1, -1,  0,  0,  0,  0, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  0,  0, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  0,  0, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  0,  0, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  0,  0, -1, -1, -1, -1},
                    {-1,  0,  0,  0,  0,  0,  0,  0,  0, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
            };

            zeroPattern = new byte[][] {
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1,  0,  0,  0,  0,  0,  0, -1, -1},
                    {-1,  0,  0,  0,  0,  0,  0,  0,  0, -1},
                    {-1,  0,  0, -1, -1, -1, -1,  0,  0, -1},
                    {-1,  0,  0, -1, -1, -1, -1,  0,  0, -1},
                    {-1,  0,  0, -1, -1, -1, -1,  0,  0, -1},
                    {-1,  0,  0, -1, -1, -1, -1,  0,  0, -1},
                    {-1,  0,  0,  0,  0,  0,  0,  0,  0, -1},
                    {-1, -1,  0,  0,  0,  0,  0,  0, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
            };
            onePatternFlat = flatten(onePattern);
            zeroPatternFlat = flatten(zeroPattern);
            oneMat = new Mat(10, 10, CV_8UC1, new BytePointer(onePatternFlat));
            zeroMat = new Mat(10, 10, CV_8UC1, new BytePointer(zeroPatternFlat));
        }

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Mat mat = converter.convert(frame);
        Mat binaryMat = new Mat(mat.size(), mat.type());

        // Binary
        cvtColor(mat, binaryMat, COLOR_RGB2GRAY);
        threshold(binaryMat, binaryMat, 127, 255, THRESH_BINARY + THRESH_OTSU);

        // replace each block
        for (int y = 0; y < binaryMat.rows(); y += blockSize) {
            for (int x = 0; x < binaryMat.cols(); x += blockSize) {
                Mat block = binaryMat.apply(new Range(y, Math.min(y + blockSize, binaryMat.rows())),
                        new Range(x, Math.min(x + blockSize, binaryMat.cols())));

                // count black and white pixels
                int whitePixels = countNonZero(block);
                int totalPixels = block.rows() * block.cols();
                int blackPixels = totalPixels - whitePixels;

                // replace block
                if (whitePixels > blackPixels) {
                    oneMat.copyTo(block);
                } else {
                    zeroMat.copyTo(block);
                }
            }
        }

        // transform back to frame
        Frame processedFrame = converter.convert(binaryMat);
        return processedFrame;
    }
}
