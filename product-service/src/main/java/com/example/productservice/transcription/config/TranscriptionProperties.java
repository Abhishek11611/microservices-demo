
package com.example.productservice.transcription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "transcription")
public class TranscriptionProperties {

    private Video video = new Video();
    private Audio audio = new Audio();
    private Output output = new Output();
    private Ffmpeg ffmpeg = new Ffmpeg();
    private Whisper whisper = new Whisper();

    public static class Video {
        private String uploadDir;

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }
    }

    public static class Audio {
        private String outputDir;

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }
    }

    public static class Output {
        private String dir;

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }

    public static class Ffmpeg {
        private String executable;

        public String getExecutable() {
            return executable;
        }

        public void setExecutable(String executable) {
            this.executable = executable;
        }
    }

    public static class Whisper {
        private String executable;
        private String modelPath;
        private String language = "auto";
        private boolean translate = true;
        private int threads = 8;
        private int beamSize = 5;

        public String getExecutable() {
            return executable;
        }

        public void setExecutable(String executable) {
            this.executable = executable;
        }

        public String getModelPath() {
            return modelPath;
        }

        public void setModelPath(String modelPath) {
            this.modelPath = modelPath;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public boolean isTranslate() {
            return translate;
        }

        public void setTranslate(boolean translate) {
            this.translate = translate;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getBeamSize() {
            return beamSize;
        }

        public void setBeamSize(int beamSize) {
            this.beamSize = beamSize;
        }
    }

    public Video getVideo() {
        return video;
    }

    public Audio getAudio() {
        return audio;
    }

    public Output getOutput() {
        return output;
    }

    public Ffmpeg getFfmpeg() {
        return ffmpeg;
    }

    public Whisper getWhisper() {
        return whisper;
    }
}
