package com.github.wizzardo.avif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class AVIFWriterTest {

    @Test
    public void testWriteRGB() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height, BufferedImage.TYPE_INT_RGB);

        File output = new File("test_output_rgb.avif");
        try {
            boolean written = ImageIO.write(image, "avif", output);
            Assertions.assertTrue(written, "AVIF writer not found");
            Assertions.assertTrue(output.exists(), "Output file does not exist");
            Assertions.assertTrue(output.length() > 0, "Output file is empty");

            BufferedImage readBack = ImageIO.read(output);
            Assertions.assertNotNull(readBack, "Failed to read back the AVIF image");
            Assertions.assertEquals(width, readBack.getWidth());
            Assertions.assertEquals(height, readBack.getHeight());
            Assertions.assertFalse(readBack.getColorModel().hasAlpha(), "RGB image should not have alpha channel");
            Assertions.assertEquals(BufferedImage.TYPE_INT_RGB, readBack.getType());
        } finally {
            if (output.exists()) {
                output.delete();
            }
        }
    }

    @Test
    public void testWriteARGB() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImageWithAlpha(width, height);

        File output = new File("test_output_argb.avif");
        try {
            boolean written = ImageIO.write(image, "avif", output);
            Assertions.assertTrue(written, "AVIF writer not found");
            Assertions.assertTrue(output.exists(), "Output file does not exist");
            Assertions.assertTrue(output.length() > 0, "Output file is empty");

            BufferedImage readBack = ImageIO.read(output);
            Assertions.assertNotNull(readBack, "Failed to read back the AVIF image");
            Assertions.assertEquals(width, readBack.getWidth());
            Assertions.assertEquals(height, readBack.getHeight());
            Assertions.assertTrue(readBack.getColorModel().hasAlpha(), "ARGB image should have alpha channel");
            Assertions.assertEquals(BufferedImage.TYPE_INT_ARGB, readBack.getType());
        } finally {
            if (output.exists()) {
                output.delete();
            }
        }
    }

    @Test
    public void testWriteDifferentTypes() throws IOException {
        int[] typesWithoutAlpha = new int[]{
            BufferedImage.TYPE_INT_RGB,
            BufferedImage.TYPE_INT_BGR,
            BufferedImage.TYPE_3BYTE_BGR,
            BufferedImage.TYPE_BYTE_GRAY
        };

        for (int type : typesWithoutAlpha) {
            BufferedImage image = createTestImage(50, 50, type);
            File output = new File("test_type_" + type + ".avif");
            try {
                boolean written = ImageIO.write(image, "avif", output);
                Assertions.assertTrue(written, "AVIF writer failed for type: " + type);
                Assertions.assertTrue(output.exists());
                Assertions.assertTrue(output.length() > 0);

                BufferedImage readBack = ImageIO.read(output);
                Assertions.assertNotNull(readBack);
                Assertions.assertEquals(50, readBack.getWidth());
                Assertions.assertEquals(50, readBack.getHeight());
                Assertions.assertFalse(readBack.getColorModel().hasAlpha());
            } finally {
                if (output.exists()) {
                    output.delete();
                }
            }
        }
    }

    @Test
    public void testWrite() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height);

        File output = new File("test_output.avif");
        try {
            boolean written = ImageIO.write(image, "avif", output);
            Assertions.assertTrue(written, "AVIF writer not found");
            Assertions.assertTrue(output.exists(), "Output file does not exist");
            Assertions.assertTrue(output.length() > 0, "Output file is empty");

            BufferedImage readBack = ImageIO.read(output);
            Assertions.assertNotNull(readBack, "Failed to read back the AVIF image");
            Assertions.assertEquals(width, readBack.getWidth());
            Assertions.assertEquals(height, readBack.getHeight());
        } finally {
            if (output.exists()) {
                output.delete();
            }
        }
    }

    @Test
    public void testWriteWithQuality() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height);

        File outputLow = new File("test_output_low.avif");
        File outputHigh = new File("test_output_high.avif");

        try {
            writeWithQuality(image, outputLow, 0.1f);
            writeWithQuality(image, outputHigh, 0.9f);

            Assertions.assertTrue(outputLow.exists());
            Assertions.assertTrue(outputHigh.exists());
            Assertions.assertTrue(outputLow.length() < outputHigh.length(), 
                "Low quality file (" + outputLow.length() + ") should be smaller than high quality file (" + outputHigh.length() + ")");

            BufferedImage readBackLow = ImageIO.read(outputLow);
            Assertions.assertNotNull(readBackLow);
            BufferedImage readBackHigh = ImageIO.read(outputHigh);
            Assertions.assertNotNull(readBackHigh);
        } finally {
            outputLow.delete();
            outputHigh.delete();
        }
    }

    private BufferedImage createTestImage(int width, int height) {
        return createTestImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    private BufferedImage createTestImageWithAlpha(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(0, 0, 255, 200));
        g.fillOval(10, 10, 80, 80);
        g.dispose();
        return image;
    }

    private BufferedImage createTestImage(int width, int height, int imageType) {
        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLUE);
        g.fillOval(10, 10, 80, 80);
        g.dispose();
        return image;
    }

    private void writeWithQuality(BufferedImage image, File output, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("avif");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No AVIF writer found");
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionType("AV1");
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
