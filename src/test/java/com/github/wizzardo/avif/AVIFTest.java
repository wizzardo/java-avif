package com.github.wizzardo.avif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class AVIFTest {

    @Test
    public void readImage() throws IOException {
        byte[] avifBytes = Files.readAllBytes(Path.of("./src/test/resources/hato.profile0.8bpc.yuv420.avif"));
        BufferedImage image = ImageUtils.read(avifBytes);
        System.out.println(image.getWidth());
        byte[] result = ImageUtils.writePNG(image);
        byte[] expected = Files.readAllBytes(Path.of("./src/test/resources/hato.8bpc.png"));
        Assertions.assertTrue(Arrays.equals(result, expected));
    }

    @Test
    public void readImage2() throws IOException {
        byte[] avifBytes = Files.readAllBytes(Path.of("./src/test/resources/hato.profile0.10bpc.yuv420.avif"));
        BufferedImage image = ImageUtils.read(avifBytes);
        System.out.println(image.getWidth());
        byte[] result = ImageUtils.writePNG(image);
        byte[] expected = Files.readAllBytes(Path.of("./src/test/resources/hato.10bpc.png"));
        Assertions.assertTrue(Arrays.equals(result, expected));
    }
}
