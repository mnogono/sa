package com.envionsoftware.sounds.tests;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Daniel on 09/07/2017.
 */
public class PlayWav {
    public static void main(String[] args)
        throws IOException, UnsupportedAudioFileException, LineUnavailableException
    {
        if (args.length < 1) {
            System.out.printf("Usage: %s input_file.wav%n", PlayWav.class);
            return;
        }
        final File inputFile = new File(args[0]);
        try (final AudioInputStream inputStream = AudioSystem.getAudioInputStream(inputFile)) {
            AudioFormat audioFormat = inputStream.getFormat();
            System.out.printf("Detected format: %s%n", audioFormat);

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
            sourceLine.start();

            int byteCount = 0;
            byte[] buffer = new byte[65536];
            while (byteCount != -1) {
                byteCount = inputStream.read(buffer);
                if (byteCount >= 0) {
                    int n = sourceLine.write(buffer, 0, byteCount);
                    System.out.printf("%d bytes played%n", n);
                }
            }
        }
    }
}
