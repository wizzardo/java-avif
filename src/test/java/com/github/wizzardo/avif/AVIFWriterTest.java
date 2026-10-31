package com.github.wizzardo.avif;

import com.github.wizzardo.avif.imageio.AVIFImageWriteParam;
import org.jextract.avif.avifDecoder;
import org.jextract.avif.avifImage;
import org.jextract.avif.avif_h;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

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

    @Test
    public void testDefaultSubsamplingIsYUV420() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height);

        byte[] bytes = ImageUtils.writeAVIF(image);
        Assertions.assertTrue(bytes.length > 0);

        int pixelFormat = getAvifPixelFormat(bytes);
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV420, pixelFormat);

        BufferedImage readBack = ImageUtils.read(bytes);
        Assertions.assertNotNull(readBack);
        Assertions.assertEquals(width, readBack.getWidth());
        Assertions.assertEquals(height, readBack.getHeight());
    }

    @Test
    public void testExplicitSubsamplingYUV444() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height);

        byte[] bytes = ImageUtils.writeAVIF(image, 0.8f, AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV444);
        Assertions.assertTrue(bytes.length > 0);

        int pixelFormat = getAvifPixelFormat(bytes);
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV444, pixelFormat);

        BufferedImage readBack = ImageUtils.read(bytes);
        Assertions.assertNotNull(readBack);
        Assertions.assertEquals(width, readBack.getWidth());
        Assertions.assertEquals(height, readBack.getHeight());
    }

    @Test
    public void testExplicitSubsamplingYUV420() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage image = createTestImage(width, height);

        byte[] bytes = ImageUtils.writeAVIF(image, 0.8f, AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV420);
        Assertions.assertTrue(bytes.length > 0);

        int pixelFormat = getAvifPixelFormat(bytes);
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV420, pixelFormat);

        BufferedImage readBack = ImageUtils.read(bytes);
        Assertions.assertNotNull(readBack);
        Assertions.assertEquals(width, readBack.getWidth());
        Assertions.assertEquals(height, readBack.getHeight());
    }

    private int getAvifPixelFormat(byte[] bytes) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment decoder = avif_h.avifDecoderCreate();
            try {
                ByteBuffer fileBuffer = ByteBuffer.allocateDirect(bytes.length);
                fileBuffer.put(bytes);
                fileBuffer.flip();
                MemorySegment fileMemorySegment = MemorySegment.ofBuffer(fileBuffer);
                int result = avif_h.avifDecoderSetIOMemory(decoder, fileMemorySegment, fileBuffer.capacity());
                if (result != avif_h.AVIF_RESULT_OK()) {
                    throw new IllegalStateException("avifDecoderSetIOMemory failed: " + result);
                }
                result = avif_h.avifDecoderParse(decoder);
                if (result != avif_h.AVIF_RESULT_OK()) {
                    throw new IllegalStateException("avifDecoderParse failed: " + result);
                }
                result = avif_h.avifDecoderNextImage(decoder);
                if (result != avif_h.AVIF_RESULT_OK()) {
                    throw new IllegalStateException("avifDecoderNextImage failed: " + result);
                }
                MemorySegment image = avifDecoder.image(decoder);
                return avifImage.yuvFormat(image);
            } finally {
                avif_h.avifDecoderDestroy(decoder);
            }
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
}
