package com.envionsoftware.sounds.tests;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;

public class CaptureSoundToWav {
    private static final int NUMBER_OF_CHANNELS = 1;

    static AudioFormat audioFormatForMicrophone(int numberOfChannels) {
        float sampleRate = 44100.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, numberOfChannels, signed, bigEndian);
    }

    public static void main(String args[])
        throws LineUnavailableException, IOException, UnsupportedAudioFileException
    {
        if (args.length < 1) {
            System.out.printf("Usage: %s result_file.wav [graph_file_with_analysis_results]%n",
                CaptureSoundToWav.class);
            return;
        }
        final File outputFile = new File(args[0]);
        final File graphFile = args.length >= 2 ? new File(args[1]) : null;

        AudioFormat audioFormat = audioFormatForMicrophone(NUMBER_OF_CHANNELS);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
        targetLine.open(audioFormat);
        final AudioInputStream audioInputStream = new AudioInputStream(targetLine);

        targetLine.start();
        new Thread(() -> {
            try {
                final int byteCount = AudioSystem.write(
                    audioInputStream,
                    AudioFileFormat.Type.WAVE,
                    outputFile);
                System.out.printf("%d bytes successfully written in %s%n", byteCount, outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("Recording started.");

        System.out.println("Press ENTER to stop the recording...");
        System.in.read();

        System.out.println("Stopping...");
        targetLine.stop();
        System.out.println("Recording stopped.");

        if (graphFile != null) {
            AnalyseWav.main(args);
        }

    }
}