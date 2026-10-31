package com.github.wizzardo.avif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class AVIFWriterTest {

    @Test
    public void testWriteRGB() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height, BufferedImage.TYPE_INT_RGB);

        byte[] bytes = ImageUtils.write(image, "avif");
        Assertions.assertTrue(bytes.length > 0, "Output bytes is empty");

        BufferedImage readBack = ImageUtils.read(bytes);
        Assertions.assertNotNull(readBack, "Failed to read back the AVIF image");
        Assertions.assertEquals(width, readBack.getWidth());
        Assertions.assertEquals(height, readBack.getHeight());
        Assertions.assertFalse(readBack.getColorModel().hasAlpha(), "RGB image should not have alpha channel");
        Assertions.assertEquals(BufferedImage.TYPE_INT_RGB, readBack.getType());
    }

    @Test
    public void testWriteARGB() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImageWithAlpha(width, height);

        byte[] bytes = ImageUtils.write(image, "avif");
        Assertions.assertTrue(bytes.length > 0, "Output bytes is empty");

        BufferedImage readBack = ImageUtils.read(bytes);
        Assertions.assertNotNull(readBack, "Failed to read back the AVIF image");
        Assertions.assertEquals(width, readBack.getWidth());
        Assertions.assertEquals(height, readBack.getHeight());
        Assertions.assertTrue(readBack.getColorModel().hasAlpha(), "ARGB image should have alpha channel");
        Assertions.assertEquals(BufferedImage.TYPE_INT_ARGB, readBack.getType());
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
            byte[] bytes = ImageUtils.write(image, "avif");
            Assertions.assertTrue(bytes.length > 0);

            BufferedImage readBack = ImageUtils.read(bytes);
            Assertions.assertNotNull(readBack);
            Assertions.assertEquals(50, readBack.getWidth());
            Assertions.assertEquals(50, readBack.getHeight());
            Assertions.assertFalse(readBack.getColorModel().hasAlpha());
        }
    }

    @Test
    public void testWrite() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height);

        byte[] bytes = ImageUtils.write(image, "avif");
        Assertions.assertTrue(bytes.length > 0, "Output bytes is empty");

        BufferedImage readBack = ImageUtils.read(bytes);
        Assertions.assertNotNull(readBack, "Failed to read back the AVIF image");
        Assertions.assertEquals(width, readBack.getWidth());
        Assertions.assertEquals(height, readBack.getHeight());
    }

    @Test
    public void testWriteWithQuality() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height);

        byte[] bytesLow = ImageUtils.write(image, "avif", 0.1f);
        byte[] bytesHigh = ImageUtils.write(image, "avif", 0.9f);

        Assertions.assertTrue(bytesLow.length > 0);
        Assertions.assertTrue(bytesHigh.length > 0);
        Assertions.assertTrue(bytesLow.length < bytesHigh.length, 
            "Low quality bytes (" + bytesLow.length + ") should be smaller than high quality bytes (" + bytesHigh.length + ")");

        BufferedImage readBackLow = ImageUtils.read(bytesLow);
        Assertions.assertNotNull(readBackLow);
        BufferedImage readBackHigh = ImageUtils.read(bytesHigh);
        Assertions.assertNotNull(readBackHigh);
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
}
