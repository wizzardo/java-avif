package com.github.wizzardo.avif.imageio;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class AVIFTools {

    public static byte[] writeAVIF(RenderedImage image) throws IOException {
        return writeAVIF(image, 0.7f);
    }

    public static byte[] writeAVIF(RenderedImage image, float quality) throws IOException {
        return writeAVIF(image, quality, AVIFImageWriteParam.AVIF_PIXEL_FORMAT_DEFAULT, AVIFImageWriteParam.AVIF_SPEED_DEFAULT);
    }

    public static byte[] writeAVIF(RenderedImage image, float quality, int pixelFormat) throws IOException {
        return writeAVIF(image, quality, pixelFormat, AVIFImageWriteParam.AVIF_SPEED_DEFAULT);
    }

    public static byte[] writeAVIF(RenderedImage image, float quality, int pixelFormat, int speed) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("avif");
        if (!writers.hasNext()) {
            throw new IOException("No writer found for format: avif");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            AVIFImageWriteParam param = (AVIFImageWriteParam) writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                String[] compressionTypes = param.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    param.setCompressionType(compressionTypes[0]);
                }
                param.setCompressionQuality(quality);
            }
            param.setPixelFormat(pixelFormat);
            param.setSpeed(speed);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }
}
