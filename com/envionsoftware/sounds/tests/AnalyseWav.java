package com.envionsoftware.sounds.tests;

import com.envionsoftware.sounds.analysis.AnalysingTools;
import com.envionsoftware.sounds.analysis.AudioAnalyser;
import com.envionsoftware.sounds.analysis.AudioLocalMaximumApertureInfo;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AnalyseWav {
    private static final int LOUD_SOUND_AMPLITUDE = Integer.getInteger("loadSoundAmplitude", 1000);
    private static int[][] getUnscaledAmplitudes(byte[] eightBitByteArray, AudioFormat audioFormat) {
        if (audioFormat.getSampleSizeInBits() != 16
            || !audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
        {
            throw new IllegalArgumentException("Unsupported audio format: " + audioFormat);
        }
        int channels = audioFormat.getChannels();
        final boolean bigEndian = audioFormat.isBigEndian();
        int[][] result = new int[channels][eightBitByteArray.length / (2 * channels)];
        int index = 0;

        for (int audioByte = 0; audioByte < eightBitByteArray.length; ) {
            for (int channel = 0; channel < channels; channel++) {
                result[channel][index] = unpack16Bit(eightBitByteArray, audioByte, bigEndian);
                audioByte += 2;
            }
            index++;
        }

        return result;
    }

    private static int unpack16Bit(byte[] bytes, int index, boolean isBigEndian) {
        if (isBigEndian) {
            return (short) (
                ((bytes[index] & 0xff) << 8)
                    | (bytes[index + 1] & 0xff)
            );
        } else {
            return (short) (
                (bytes[index] & 0xff)
                    | ((bytes[index + 1] & 0xff) << 8)
            );
        }
    }


    private static BufferedImage drawGraph(AudioAnalyser analyser, int w, int h) {
        final double[] samples = analyser.amplitudeSamples();
        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D g = (Graphics2D) image.getGraphics();
        final Stroke defaultStroke = g.getStroke();
        double maxAbs = analyser.maxAbsoluteSignal();
        final double scaleX = (double) w / (double) samples.length;
        final double scaleY = (double) (h / 2 - 1) / (double) maxAbs;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.MAGENTA);
        for (int k = 0; k < samples.length; k++) {
            final int x1 = (int) Math.round(k * scaleX);
            final int x2 = (int) Math.round((k + 1) * scaleX);
            final int y = h / 2 - (int) Math.round(samples[k] * scaleY);
            fillRectByCorners(g, x1, h / 2, x2, y);
        }
        g.setColor(Color.BLACK);
        g.fillRect(0, h / 2, w, 1);
        g.setColor(new Color(0, 128, 0));
        g.fillRect(0, h / 2 - (int) Math.round(analyser.typicalAveragedSignal() * scaleY), w, 1);
        g.setColor(new Color(128, 128, 128));
        g.fillRect(0, h / 2 - (int) Math.round(analyser.getGlobalAmplitudeOfLoudSound() * scaleY), w, 1);
        int index = 0;
        for (AudioLocalMaximumApertureInfo info : analyser.allLocalMaximums()) {
            System.out.printf("Found local maximum #%d: %s%n", index, info);
            final int x = (int) Math.round(info.getLocalMaximumSampleIndex() * scaleX);
            if (info.isGoodLocalMaximum()) {
                g.setColor(new Color(0, 255, 0));
                g.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    20.0f, new float[] {20.0f, 10.0f}, 0.0f));
            } else {
                g.setColor(new Color(0, 0, 255));
                g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    5.0f, new float[] {5.0f}, 0.0f));
            }
            g.drawLine(x, 0, x, h - 1);
            int half = analyser.getLocalMaxApertureLength() / 2;
            int x1 = (int) Math.round((info.getLocalMaximumSampleIndex() - half) * scaleX);
            int x2 = (int) Math.round((info.getLocalMaximumSampleIndex() + half) * scaleX);
            int y = h / 2 - (int) Math.round(info.getMaxAveragedAbsoluteAmplitude() * scaleY);
            g.setColor(new Color(0, 0, 255));
            g.setStroke(defaultStroke);
            g.drawLine(x1, y, x2, y);
            half = analyser.getLocalMaxWideApertureLength() / 2;
            x1 = (int) Math.round((info.getLocalMaximumSampleIndex() - half) * scaleX);
            x2 = (int) Math.round((info.getLocalMaximumSampleIndex() + half) * scaleX);
            final double perc = info.getLowPercentileAveragedAbsoluteAmplitude();
            g.setColor(new Color(0, 0, 255));
            if (!Double.isNaN(perc)) {
                y = h / 2 - (int) Math.round(perc * scaleY);
                g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    5.0f, new float[] {5.0f}, 0.0f));
                g.drawLine(x1, y, x2, y);
            }
            final double min = info.getMinAveragedAbsoluteAmplitude();
            if (!Double.isNaN(min)) {
                y = h / 2 - (int) Math.round(min * scaleY);
                g.setStroke(defaultStroke);
                g.drawLine(x1, y, x2, y);
            }
            index++;
            if (info.isShortImpulse()) {
                g.setColor(new Color(255, 255, 0));
                g.setStroke(info.isGoodLocalMaximum() ?
                    new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        5.0f, new float[] {5.0f}, 0.0f):
                    new BasicStroke(6));
                x1 = (int) Math.round(info.getShortImpulseLeft() * scaleX);
                x2 = (int) Math.round(info.getShortImpulseRight() * scaleX);
                y = h / 2 - (int) Math.round(info.getShortImpulseAbsoluteAmplitude() * scaleY);
                g.drawLine(x1, y, x2, y);
            }
        }
        g.setStroke(defaultStroke);
        g.setColor(new Color(255, 128, 255));
        final double[] averagedImpulse = analyser.averagedImpulseSquare();
        for (int k = 0; k < averagedImpulse.length - 1; k++) {
            final int sampleIndex = k + (samples.length - averagedImpulse.length) / 2;
            final int x1 = (int) Math.round(sampleIndex * scaleX);
            final int x2 = (int) Math.round((sampleIndex + 1) * scaleX);
            final int y1 = h / 2 - (int) Math.round(Math.sqrt(averagedImpulse[k]) * scaleY);
            final int y2 = h / 2 - (int) Math.round(Math.sqrt(averagedImpulse[k + 1]) * scaleY);
            g.drawLine(x1, y1, x2, y2);
        }
        g.setColor(new Color(0, 0, 255));
        final double[] averaged = analyser.averagedSquare();
        for (int k = 0; k < averaged.length - 1; k++) {
            final int sampleIndex = k + (samples.length - averaged.length) / 2;
            final int x1 = (int) Math.round(sampleIndex * scaleX);
            final int x2 = (int) Math.round((sampleIndex + 1) * scaleX);
            final int y1 = h / 2 - (int) Math.round(Math.sqrt(averaged[k]) * scaleY);
            final int y2 = h / 2 - (int) Math.round(Math.sqrt(averaged[k + 1]) * scaleY);
            g.drawLine(x1, y1, x2, y2);
        }
        return image;
    }

    private static void fillRectByCorners(Graphics g, int x1, int y1, int x2, int y2) {
        g.fillRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1) + 1, Math.abs(y2 - y1) + 1);
    }

    private static String getImageFormat(String fileName) {
        int p = fileName.lastIndexOf('.');
        if (p == -1) {
            return "png";
        }
        return fileName.substring(p + 1);
    }


    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[65536];
            for (int len; (len = inputStream.read(buffer)) != -1; ) {
                out.write(buffer, 0, len);
            }
            out.flush();
            return out.toByteArray();
        }
    }

    public static void main(String[] args)
        throws LineUnavailableException, IOException, UnsupportedAudioFileException
    {
        if (args.length < 2) {
            System.out.printf("Usage: %s input_file.wav result_graph_file.png [graph_width [graph_height]]%n",
                AnalyseWav.class);
            return;
        }
        File inputFile = new File(args[0]);
        File graphFile = new File(args[1]);
        if (graphFile.getName().equals("*")) {
            graphFile = new File(graphFile.getParent(), inputFile.getName() + ".png");
        }
        final int width = args.length > 2 ? Integer.parseInt(args[2]) : 8000;
        final int height = args.length > 3 ? Integer.parseInt(args[3]) : 512;
        final int[][] amplitude;
        final double singleSampleDurationInSeconds;
        System.out.printf("Decoding audio file %s...%n", inputFile);
        try (final AudioInputStream inputStream = AudioSystem.getAudioInputStream(inputFile)) {
            final AudioFormat audioFormat = inputStream.getFormat();
            singleSampleDurationInSeconds = 1.0 / audioFormat.getFrameRate();
            final byte[] bytes = readAllBytes(inputStream);
            amplitude = getUnscaledAmplitudes(bytes, audioFormat);
            System.out.printf("Detected format: %s; duration: %.3f seconds (%d frames * %.3f ms, %d samples)%n",
                audioFormat,
                inputStream.getFrameLength() * singleSampleDurationInSeconds,
                inputStream.getFrameLength(),
                singleSampleDurationInSeconds * 0.001,
                amplitude[0].length);
        }
//        System.out.println(Arrays.toString(amplitude[0]));

        final double[] samples = AnalysingTools.intToDouble(amplitude[0]);
        final AudioAnalyser analyser = new AudioAnalyser(singleSampleDurationInSeconds);
        analyser.setGlobalAmplitudeOfLoudSound(LOUD_SOUND_AMPLITUDE);
        analyser.setSamples(samples);
        analyser.preprocess();
        analyser.analyze();
        // - warming JVM...
        System.out.println("Analysing...");
        long t1 = System.nanoTime();
        analyser.setSamples(samples);
        long t2 = System.nanoTime();
        analyser.preprocess();
        long t3 = System.nanoTime();
        analyser.analyze();
        analyser.postprocessFoundMaximums();
        long t4 = System.nanoTime();
        System.out.printf("Analysing time %.3f ms setting + %.3f ms preprocessing + %.3f ms analysis%n",
            (t2 - t1) * 1e-6, (t3 - t2) * 1e-6, (t4 - t3) * 1e-6);
        System.out.printf("Number of strong signals: %d%n", analyser.goodLocalMaximums().size());
        System.out.println("Making graph...");
        ImageIO.write(drawGraph(analyser, width, height), getImageFormat(graphFile.getName()), graphFile);
        System.out.printf("Graph saved in %s%n%n", graphFile);
        System.out.printf("Maximal number of sequential strong signals: %d%n",
            analyser.maxNumberOfSequentialGoodMaximums());
        System.out.printf("Total number of sequential strong signals: %d%n%n",
            analyser.totalNumberOfSequentialGoodMaximums());
    }
}
