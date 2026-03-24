package com.github.wizzardo.avif.imageio;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Service provider interface for AVIF image reader.
 */
public class AVIFImageReaderSpi extends ImageReaderSpi {

    private static final String VERSION = "1.0";
    private static final String READER_CLASS_NAME = "com.github.wizzardo.avif.imageio.AVIFImageReader";
    private static final String[] NAMES = {"avif", "AVIF"};
    private static final String[] SUFFIXES = {"avif"};
    private static final String[] MIME_TYPES = {"image/avif"};

    public AVIFImageReaderSpi() {
        super(
                "Example", // vendorName
                VERSION,   // version
                NAMES,     // names
                SUFFIXES,  // suffixes
                MIME_TYPES,// MIMETypes
                READER_CLASS_NAME, // readerClassName
                new Class<?>[]{ImageInputStream.class}, // inputTypes
                null, // writerSpiNames
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
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }

        ImageInputStream stream = (ImageInputStream) source;
        stream.mark();

        try {
            // Check for AVIF file signature
            // AVIF files start with 'ftyp' box after 4 bytes for size
            // and have 'avif' as compatible brand
            byte[] signature = new byte[12];
            stream.readFully(signature);

            // Check for 'ftyp' box type at offset 4
            if (signature[4] == 'f' && signature[5] == 't' &&
                    signature[6] == 'y' && signature[7] == 'p') {

                // Check for 'avif' brand at offset 8
                if (signature[8] == 'a' && signature[9] == 'v' &&
                        signature[10] == 'i' && signature[11] == 'f') {
                    return true;
                }
            }

            return false;
        } finally {
            stream.reset();
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new AVIFImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "AVIF Image Reader";
    }
}