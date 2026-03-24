package com.github.wizzardo.avif;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AVIFTest {

    @Test
    public void readImage() throws IOException {
        File input = new File("./src/test/resources/hato.profile0.8bpc.yuv420.avif");
        BufferedImage image = ImageIO.read(input);
        System.out.println(image.getWidth());
    }
}
