package com.github.wizzardo.avif;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static byte[] write(RenderedImage image, String formatName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean written = ImageIO.write(image, formatName, out);
        if (!written) {
            throw new IOException("No writer found for format: " + formatName);
        }
        return out.toByteArray();
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
