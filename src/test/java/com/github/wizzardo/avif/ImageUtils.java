package com.github.wizzardo.avif;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class ImageUtils {

    public static byte[] write(RenderedImage image, String formatName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean written = ImageIO.write(image, formatName, out);
        if (!written) {
            throw new IOException("No writer found for format: " + formatName);
        }
        return out.toByteArray();
    }

    public static byte[] write(RenderedImage image, String formatName, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        if (!writers.hasNext()) {
            throw new IOException("No writer found for format: " + formatName);
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                String[] compressionTypes = param.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    param.setCompressionType(compressionTypes[0]);
                }
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

    public static byte[] writeAVIF(RenderedImage image) throws IOException {
        return write(image, "avif");
    }

    public static byte[] writeAVIF(RenderedImage image, float quality) throws IOException {
        return write(image, "avif", quality);
    }

    public static byte[] writePNG(RenderedImage image) throws IOException {
        return write(image, "png");
    }

    public static byte[] savePNGtoBytes(BufferedImage image) throws IOException {
        return write(image, "png");
    }

    public static BufferedImage read(byte[] bytes) throws IOException {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null");
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(in);
        }
    }

    public static BufferedImage read(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        return ImageIO.read(in);
    }
}
