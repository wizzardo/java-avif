package com.github.wizzardo.avif.imageio;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.util.Locale;

public class AVIFImageWriterSpi extends ImageWriterSpi {

    private static final String VERSION = "1.0";
    private static final String WRITER_CLASS_NAME = "com.github.wizzardo.avif.imageio.AVIFImageWriter";
    private static final String[] NAMES = {"avif", "AVIF"};
    private static final String[] SUFFIXES = {"avif"};
    private static final String[] MIME_TYPES = {"image/avif"};

    // Standard constructor required by service provider interface
    public AVIFImageWriterSpi() {
        super(
            "com.github.wizzardo.avif",
            VERSION,   // version
            NAMES,     // names
            SUFFIXES,  // suffixes
            MIME_TYPES,// MIMETypes
            WRITER_CLASS_NAME, // writerClassName
            new Class<?>[] { ImageOutputStream.class }, // outputTypes
            new String[] { "com.github.wizzardo.avif.imageio.AVIFImageReaderSpi" }, // readerSpiNames
            false, // supportsStandardStreamMetadataFormat
            null, // nativeStreamMetadataFormatName
            null, // nativeStreamMetadataFormatClassName
            null, // extraStreamMetadataFormatNames
            null, // extraStreamMetadataFormatClassNames
            false, // supportsStandardImageMetadataFormat
            null, // nativeImageMetadataFormatName
            null, // nativeImageMetadataFormatClassName
            null, // extraImageMetadataFormatNames
            null  // extraImageMetadataFormatClassNames
        );
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        // In a real implementation, this would check if the image type can be encoded as AVIF
        // For now, return true as this is a placeholder implementation
        return true;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new AVIFImageWriter(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "AVIF Image Writer";
    }
}