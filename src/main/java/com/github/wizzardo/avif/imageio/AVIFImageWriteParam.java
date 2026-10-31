package com.github.wizzardo.avif.imageio;

import org.jextract.avif.avif_h;

import javax.imageio.ImageWriteParam;
import java.util.Locale;

public class AVIFImageWriteParam extends ImageWriteParam {

    public static final int AVIF_PIXEL_FORMAT_NONE = avif_h.AVIF_PIXEL_FORMAT_NONE();
    public static final int AVIF_PIXEL_FORMAT_YUV444 = avif_h.AVIF_PIXEL_FORMAT_YUV444();
    public static final int AVIF_PIXEL_FORMAT_YUV422 = avif_h.AVIF_PIXEL_FORMAT_YUV422();
    public static final int AVIF_PIXEL_FORMAT_YUV420 = avif_h.AVIF_PIXEL_FORMAT_YUV420();
    public static final int AVIF_PIXEL_FORMAT_YUV400 = avif_h.AVIF_PIXEL_FORMAT_YUV400();

    private int pixelFormat = AVIF_PIXEL_FORMAT_YUV420;

    public AVIFImageWriteParam() {
        this(Locale.getDefault());
    }

    public AVIFImageWriteParam(Locale locale) {
        super(locale);
        this.canWriteCompressed = true;
        this.compressionTypes = new String[]{"AV1"};
        this.compressionType = "AV1";
        this.compressionMode = MODE_EXPLICIT;
        this.compressionQuality = 0.6f;
        this.pixelFormat = AVIF_PIXEL_FORMAT_YUV420;
    }

    public int getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(int pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public int getYuvFormat() {
        return pixelFormat;
    }

    public void setYuvFormat(int yuvFormat) {
        this.pixelFormat = yuvFormat;
    }

    public int getPixelSubsampling() {
        return pixelFormat;
    }

    public void setPixelSubsampling(int pixelSubsampling) {
        this.pixelFormat = pixelSubsampling;
    }
}
