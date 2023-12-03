package com.example.v2rd2;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Range;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;

import org.junit.Test;

import java.io.File;

public class UnitTest {
    public void rollingDigits(File videoFile, File saveDir) {
        String videoFilePath = videoFile.getAbsolutePath();
        String newFileName = videoFile.getName().replaceFirst("[.][^.]+$", "") + "_transformed.mp4";
        File transformedVideo = new File(saveDir, newFileName);
        System.out.println(videoFilePath);


        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoFilePath)) {
            frameGrabber.start();
            FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder(transformedVideo, frameGrabber.getImageWidth(), frameGrabber.getImageHeight());

            frameRecorder.setFrameRate(frameGrabber.getFrameRate());
            frameRecorder.setVideoCodec(frameGrabber.getVideoCodec());
            frameRecorder.setAudioCodec(frameGrabber.getAudioCodec());
            frameRecorder.setAudioChannels(frameGrabber.getAudioChannels());
            frameRecorder.setAudioBitrate(frameGrabber.getAudioBitrate());
            frameRecorder.setSampleRate(frameGrabber.getSampleRate());
            frameRecorder.start();

            Frame frame;
            while ((frame = frameGrabber.grabFrame()) != null) {
                if (frame.image != null) {
                    Frame processedFrame = applyRDEffect(frame);  // apply effect
                    frameRecorder.record(processedFrame); // save frame
                }
            }

            frameRecorder.stop();
            frameGrabber.stop();
            frameRecorder.release();
            frameGrabber.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] flatten(byte[][] array) {
        int rows = array.length; // 二维数组的行数
        int cols = array[0].length; // 二维数组的列数
        byte[] flat = new byte[rows * cols]; // 创建一个足够大的数组来容纳所有的数据

        for (int i = 0; i < rows; i++) {
            System.arraycopy(array[i], 0, flat, i * cols, cols); // 将每一行的数据复制到一维数组中
        }

        return flat; // 返回包含了所有数据的一维数组
    }

    public Frame applyRDEffect(Frame frame) {
        int blockSize = 10;
        byte[][] onePattern = {
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

        byte[][] zeroPattern = {
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

//        int blockSize = 5;
//        byte[][] onePattern = {
//                {-1, -1,  0, -1, -1},
//                {-1,  0,  0, -1, -1},
//                {-1, -1,  0, -1, -1},
//                {-1,  0,  0,  0, -1},
//                {-1, -1, -1, -1, -1},
//        };
//
//        byte[][] zeroPattern = {
//                {-1, -1, -1, -1, -1},
//                {-1,  0,  0,  0, -1},
//                {-1,  0, -1,  0, -1},
//                {-1,  0,  0,  0, -1},
//                {-1, -1, -1, -1, -1},
//        };

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Mat mat = converter.convert(frame);
        Mat binaryMat = new Mat(mat.size(), mat.type());

        byte[] onePatternFlat = flatten(onePattern);
        byte[] zeroPatternFlat = flatten(zeroPattern);
        Mat oneMat = new Mat(10, 10, CV_8UC1, new BytePointer(onePatternFlat));
        Mat zeroMat = new Mat(10, 10, CV_8UC1, new BytePointer(zeroPatternFlat));


        // Binary
        cvtColor(mat, binaryMat, COLOR_RGB2GRAY);
        threshold(binaryMat, binaryMat, 128, 255, THRESH_BINARY + THRESH_OTSU);

        // replace each block
        for (int y = 0; y < binaryMat.rows(); y += blockSize) {
            for (int x = 0; x < binaryMat.cols(); x += blockSize) {
                Mat block = binaryMat.apply(new Range(y, Math.min(y + blockSize, binaryMat.rows())),
                        new Range(x, Math.min(x + blockSize, binaryMat.cols())));

                // count black and white pixels
                int whitePixels = countNonZero(block);
                int totalPixels = block.rows() * block.cols();
                int blackPixels = totalPixels - whitePixels;

                // replace blocks
                if (whitePixels > blackPixels) {
                    oneMat.copyTo(block);
                } else {
                    zeroMat.copyTo(block);
                }
            }
        }

        // return transformed frame
        Frame processedFrame = converter.convert(binaryMat);
        return processedFrame;
    }

    @Test
    public void testEffect () {
        UnitTest unitTest = new UnitTest();
        File videoFile = new File("test1.mp4");
        File saveDir = new File("C:\\Users\\86181\\Desktop\\workspace\\V2RD");

        unitTest.rollingDigits(videoFile, saveDir);
    }

    public static void main(String[] args) {
        UnitTest unitTest = new UnitTest();
        unitTest.testEffect();
    }
}
