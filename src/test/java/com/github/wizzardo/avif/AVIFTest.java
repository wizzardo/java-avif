package com.github.wizzardo.avif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class AVIFTest {

    public static byte[] savePNGtoBytes(BufferedImage im) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(im, "png", bytes);
        return bytes.toByteArray();
    }

    @Test
    public void readImage() throws IOException {
        File input = new File("./src/test/resources/hato.profile0.8bpc.yuv420.avif");
        BufferedImage image = ImageIO.read(input);
        System.out.println(image.getWidth());
//        ImageIO.write(image, "png", new File("./src/test/resources/hato.8bpc.png"));
        byte[] result = savePNGtoBytes(image);
        byte[] expected = Files.readAllBytes(Path.of("./src/test/resources/hato.8bpc.png"));
        Assertions.assertTrue(Arrays.equals(result, expected));
    }

    @Test
    public void readImage2() throws IOException {
        File input = new File("./src/test/resources/hato.profile0.10bpc.yuv420.avif");
        BufferedImage image = ImageIO.read(input);
        System.out.println(image.getWidth());
//        ImageIO.write(image, "png", new File("./src/test/resources/hato.10bpc.png"));
        byte[] result = savePNGtoBytes(image);
        byte[] expected = Files.readAllBytes(Path.of("./src/test/resources/hato.10bpc.png"));
        Assertions.assertTrue(Arrays.equals(result, expected));
    }
}
