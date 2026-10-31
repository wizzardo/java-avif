package com.github.wizzardo.avif;

import com.github.wizzardo.avif.imageio.AVIFTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageUtilsTest {

    @Test
    public void testReadWriteBytes() throws IOException {
        BufferedImage original = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = original.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 30, 30);
        g.dispose();

        byte[] pngBytes = ImageUtils.writePNG(original);
        Assertions.assertTrue(pngBytes.length > 0);

        BufferedImage fromPng = ImageUtils.read(pngBytes);
        Assertions.assertNotNull(fromPng);
        Assertions.assertEquals(30, fromPng.getWidth());
        Assertions.assertEquals(30, fromPng.getHeight());

        byte[] avifBytes = AVIFTools.writeAVIF(original);
        Assertions.assertTrue(avifBytes.length > 0);

        BufferedImage fromAvif = ImageUtils.read(avifBytes);
        Assertions.assertNotNull(fromAvif);
        Assertions.assertEquals(30, fromAvif.getWidth());
        Assertions.assertEquals(30, fromAvif.getHeight());
    }

    @Test
    public void testReadFromInputStream() throws IOException {
        BufferedImage original = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        byte[] bytes = ImageUtils.write(original, "png");
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            BufferedImage readBack = ImageUtils.read(in);
            Assertions.assertNotNull(readBack);
            Assertions.assertEquals(20, readBack.getWidth());
            Assertions.assertEquals(20, readBack.getHeight());
        }
    }

    @Test
    public void testWriteWithQuality() throws IOException {
        BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 40, 40);
        g.setColor(Color.BLUE);
        g.fillOval(5, 5, 30, 30);
        g.dispose();

        byte[] low = AVIFTools.writeAVIF(image, 0.1f);
        byte[] high = AVIFTools.writeAVIF(image, 0.9f);

        Assertions.assertTrue(low.length > 0);
        Assertions.assertTrue(high.length > 0);
        Assertions.assertTrue(low.length < high.length);
    }

    @Test
    public void testInvalidArguments() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ImageUtils.read((byte[]) null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ImageUtils.read((java.io.InputStream) null));
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Assertions.assertThrows(IOException.class, () -> ImageUtils.write(image, "non_existent_format"));
    }
}
