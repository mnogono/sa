package com.envionsoftware.sounds.tests;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Daniel on 09/07/2017.
 */
public class ReadSamplesTest {
    static final int NUMBER_OF_CHANNELS = 1;

    public static int[][] getUnscaledAmplitude(byte[] eightBitByteArray, int nbChannels) {
        int[][] toReturn = new int[nbChannels][eightBitByteArray.length / (2 * nbChannels)];
        int index = 0;

        for (int audioByte = 0; audioByte < eightBitByteArray.length;)
        {
            for (int channel = 0; channel < nbChannels; channel++)
            {
                // Do the byte to sample conversion.
                int low = (int) eightBitByteArray[audioByte];
                audioByte++;
                int high = (int) eightBitByteArray[audioByte];
                audioByte++;
                int sample = (high << 8) + (low & 0x00ff);

                toReturn[channel][index] = sample;
            }
            index++;
        }

        return toReturn;
    }

    public static String getFileExtension(String fileName) {
        int p = fileName.lastIndexOf('.');
        if (p == -1) {
            return null;
        }
        return fileName.substring(p + 1);
    }

    public static void main(String[] args) throws LineUnavailableException, IOException {
        AudioFormat audioFormat = CaptureSoundToWav.audioFormatForMicrophone(NUMBER_OF_CHANNELS);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);

        ByteArrayOutputStream out  = new ByteArrayOutputStream();
        byte[] data = new byte[line.getBufferSize() / 5];

        line.start();

        // Here, stopped is a global boolean set by another thread.
        for (long t = System.currentTimeMillis(); System.currentTimeMillis() - t < 5000; ) {
            // Read the next chunk of data from the TargetDataLine.
            int numBytesRead = line.read(data, 0, data.length);
            out.write(data, 0, numBytesRead);
            System.out.println(numBytesRead);
        }
        final int[][] amplitude = getUnscaledAmplitude(out.toByteArray(), NUMBER_OF_CHANNELS);
        System.out.println(Arrays.toString(amplitude[0]));
    }
}
