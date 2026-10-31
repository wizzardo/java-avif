package com.github.wizzardo.avif;

import com.github.wizzardo.avif.imageio.AVIFImageWriteParam;
import org.jextract.avif.avif_h;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageWriteParam;
import java.util.Locale;

public class AVIFImageWriteParamTest {

    @Test
    public void testConstants() {
        Assertions.assertEquals(avif_h.AVIF_PIXEL_FORMAT_NONE(), AVIFImageWriteParam.AVIF_PIXEL_FORMAT_NONE);
        Assertions.assertEquals(avif_h.AVIF_PIXEL_FORMAT_YUV444(), AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV444);
        Assertions.assertEquals(avif_h.AVIF_PIXEL_FORMAT_YUV422(), AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV422);
        Assertions.assertEquals(avif_h.AVIF_PIXEL_FORMAT_YUV420(), AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV420);
        Assertions.assertEquals(avif_h.AVIF_PIXEL_FORMAT_YUV400(), AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV400);
    }

    @Test
    public void testDefaultConstructor() {
        AVIFImageWriteParam param = new AVIFImageWriteParam();
        Assertions.assertTrue(param.canWriteCompressed());
        Assertions.assertEquals(ImageWriteParam.MODE_EXPLICIT, param.getCompressionMode());
        Assertions.assertEquals("AV1", param.getCompressionType());
        Assertions.assertEquals(0.6f, param.getCompressionQuality(), 0.001f);
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV420, param.getPixelFormat());
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV420, param.getYuvFormat());
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV420, param.getPixelSubsampling());
    }

    @Test
    public void testLocaleConstructor() {
        AVIFImageWriteParam param = new AVIFImageWriteParam(Locale.GERMANY);
        Assertions.assertEquals(Locale.GERMANY, param.getLocale());
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV420, param.getPixelFormat());
    }

    @Test
    public void testSetPixelFormat() {
        AVIFImageWriteParam param = new AVIFImageWriteParam();

        param.setPixelFormat(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV444);
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV444, param.getPixelFormat());
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV444, param.getYuvFormat());
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV444, param.getPixelSubsampling());

        param.setYuvFormat(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV422);
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV422, param.getPixelFormat());
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV422, param.getYuvFormat());

        param.setPixelSubsampling(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV400);
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV400, param.getPixelFormat());
        Assertions.assertEquals(AVIFImageWriteParam.AVIF_PIXEL_FORMAT_YUV400, param.getPixelSubsampling());
    }
}
