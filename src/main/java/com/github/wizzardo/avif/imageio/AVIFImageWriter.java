package com.github.wizzardo.avif.imageio;

import org.jextract.avif.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Locale;

/**
 * ImageWriter implementation for AVIF format.
 * This is a minimal implementation that will be expanded later with actual AVIF encoding.
 */
public class AVIFImageWriter extends ImageWriter {

    static {
        try {
            System.loadLibrary("avif");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Error loading libavif: " + e.getMessage());
        }
    }

    private ImageOutputStream stream;

    protected AVIFImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setOutput(Object output) {
        super.setOutput(output);
        if (output instanceof ImageOutputStream) {
            this.stream = (ImageOutputStream) output;
        } else if (output != null) {
            throw new IllegalArgumentException("Output not an ImageOutputStream");
        }
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new AVIFImageWriteParam(getLocale());
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage iioImage, ImageWriteParam param) throws IOException {
        if (stream == null) {
            throw new IllegalStateException("No output stream specified");
        }

        RenderedImage renderedImage = iioImage.getRenderedImage();
        if (!(renderedImage instanceof BufferedImage)) {
            throw new IllegalArgumentException("Only BufferedImage is supported for now");
        }

        BufferedImage bi = (BufferedImage) renderedImage;
        int width = bi.getWidth();
        int height = bi.getHeight();
        boolean hasAlpha = bi.getColorModel().hasAlpha();

        int pixelFormat = avif_h.AVIF_PIXEL_FORMAT_YUV420();
        if (param instanceof AVIFImageWriteParam) {
            pixelFormat = ((AVIFImageWriteParam) param).getPixelFormat();
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment image = avif_h.avifImageCreate(width, height, 8, pixelFormat);
            if (image.equals(MemorySegment.NULL)) {
                throw new IOException("Could not create avifImage");
            }

            try {
                MemorySegment rgbImage = avifRGBImage.allocate(arena);
                avif_h.avifRGBImageSetDefaults(rgbImage, image);
                avifRGBImage.format(rgbImage, hasAlpha ? avif_h.AVIF_RGB_FORMAT_ARGB() : avif_h.AVIF_RGB_FORMAT_RGB());
                avifRGBImage.depth(rgbImage, 8);

                int result = avif_h.avifRGBImageAllocatePixels(rgbImage);
                if (result != avif_h.AVIF_RESULT_OK()) {
                    throw new IOException("avifRGBImageAllocatePixels failed: " + getAvifResultToString(result));
                }

                try {
                    MemorySegment pixels = avifRGBImage.pixels(rgbImage);
                    copyBufferedImageToPixels(bi, pixels, hasAlpha);

                    result = avif_h.avifImageRGBToYUV(image, rgbImage);
                    if (result != avif_h.AVIF_RESULT_OK()) {
                        throw new IOException("avifImageRGBToYUV failed: " + getAvifResultToString(result));
                    }

                    MemorySegment encoder = avif_h.avifEncoderCreate();
                    if (encoder.equals(MemorySegment.NULL)) {
                        throw new IOException("Could not create avifEncoder");
                    }

                    try {
                        if (param != null && param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT) {
                            float quality = param.getCompressionQuality();
                            avifEncoder.quality(encoder, (int) (quality * 100));
                        }

                        MemorySegment output = avifRWData.allocate(arena);
                        result = avif_h.avifEncoderWrite(encoder, image, output);
                        if (result != avif_h.AVIF_RESULT_OK()) {
                            throw new IOException("avifEncoderWrite failed: " + getAvifResultToString(result));
                        }

                        try {
                            long size = avifRWData.size(output);
                            MemorySegment data = avifRWData.data(output);
                            byte[] bytes = data.asSlice(0, size).toArray(java.lang.foreign.ValueLayout.JAVA_BYTE);
                            stream.write(bytes);
                        } finally {
                            avif_h.avifRWDataFree(output);
                        }
                    } finally {
                        avif_h.avifEncoderDestroy(encoder);
                    }
                } finally {
                    avif_h.avifRGBImageFreePixels(rgbImage);
                }
            } finally {
                avif_h.avifImageDestroy(image);
            }
        }
    }

    private void copyBufferedImageToPixels(BufferedImage bi, MemorySegment pixels, boolean hasAlpha) {
        int width = bi.getWidth();
        int height = bi.getHeight();

        // Standard way to get pixel data
        int[] rgb = bi.getRGB(0, 0, width, height, null, 0, width);

        if (hasAlpha) {
            // Convert to ARGB (which is what we set in avifRGBImage.format)
            // libavif's AVIF_RGB_FORMAT_ARGB is A, R, G, B in memory order.
            // On little-endian, it is B, G, R, A as an int.
            // BufferedImage.getRGB returns ARGB as an int: (A << 24) | (R << 16) | (G << 8) | B

            ByteBuffer buffer = pixels.asSlice(0, (long) width * height * 4).asByteBuffer().order(ByteOrder.nativeOrder());
            IntBuffer intBuffer = buffer.asIntBuffer();

            for (int i = 0; i < rgb.length; i++) {
                int argb = rgb[i];
                // Reorder if necessary. AVIF_RGB_FORMAT_ARGB expects:
                // byte[0] = A, byte[1] = R, byte[2] = G, byte[3] = B
                // On little-endian, this is int: 0xBBGGRRAA

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                    intBuffer.put(i, (b << 24) | (g << 16) | (r << 8) | a);
                } else {
                    intBuffer.put(i, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
        } else {
            // Convert to RGB (which is what we set in avifRGBImage.format)
            // libavif's AVIF_RGB_FORMAT_RGB is R, G, B in memory order:
            // byte[0] = R, byte[1] = G, byte[2] = B
            ByteBuffer buffer = pixels.asSlice(0, (long) width * height * 3).asByteBuffer();
            byte[] byteBuffer = new byte[rgb.length * 3];
            int offset = 0;
            for (int i = 0; i < rgb.length; i++) {
                int val = rgb[i];
                byteBuffer[offset++] = (byte) ((val >> 16) & 0xFF);
                byteBuffer[offset++] = (byte) ((val >> 8) & 0xFF);
                byteBuffer[offset++] = (byte) (val & 0xFF);
            }
            buffer.put(byteBuffer);
        }
    }

    private static String getAvifResultToString(int result) {
        MemorySegment memorySegment = avif_h.avifResultToString(result);
        return memorySegment.getString(0);
    }
}