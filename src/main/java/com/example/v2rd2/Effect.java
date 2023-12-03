package com.example.v2rd2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class Effect {
    private final ArrayList<String> effectList;

    public Effect() {
        effectList = new ArrayList<>();
        effectList.add("Rolling Digits");
        effectList.add("Test1");
        effectList.add("Test2");
    }

    // Getter and Setter
    public ArrayList<String> getEffectList() {
        return this.effectList;
    }

    public void applyAlgo(String effectName, File originalVideo, File saveDir) {
        switch (effectName) {
            case "Rolling Digits":
                rollingDigits(originalVideo, saveDir);
                break;
            case "Test1":
                System.out.println("Applying Test1 Effect.");
                break;
            case "Test2":
                System.out.println("Applying Test2 Effect.");
                break;
        }
    }

    // Effect Algorithms
    public void rollingDigits(File videoFile, File saveDir) {
        String newFileName = videoFile.getName().replace(".", "_transformed.");
        File transformedVideo = new File(saveDir, newFileName);

        try {
            Files.copy(videoFile.toPath(), transformedVideo.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // real algo
        
    }
}
