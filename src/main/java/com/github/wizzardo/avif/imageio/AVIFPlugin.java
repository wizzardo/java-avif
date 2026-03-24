package com.github.wizzardo.avif.imageio;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import java.util.Iterator;

/**
 * Utility class for the AVIF ImageIO plugin.
 * This class provides methods to check if the plugin is correctly registered.
 */
public class AVIFPlugin {

    /**
     * Checks if the AVIF plugin is registered with ImageIO.
     * 
     * @return true if both reader and writer are registered, false otherwise
     */
    public static boolean isPluginRegistered() {
        boolean readerRegistered = false;
        boolean writerRegistered = false;

        // Check if the reader is registered
        IIORegistry registry = IIORegistry.getDefaultInstance();
        Iterator<ImageReaderSpi> readers = registry.getServiceProviders(ImageReaderSpi.class, false);
        while (readers.hasNext()) {
            ImageReaderSpi spi = readers.next();
            if (spi instanceof AVIFImageReaderSpi) {
                readerRegistered = true;
                break;
            }
        }

        // Check if the writer is registered
        Iterator<ImageWriterSpi> writers = registry.getServiceProviders(ImageWriterSpi.class, false);
        while (writers.hasNext()) {
            ImageWriterSpi spi = writers.next();
            if (spi instanceof AVIFImageWriterSpi) {
                writerRegistered = true;
                break;
            }
        }

        return readerRegistered && writerRegistered;
    }

    /**
     * Manually registers the AVIF plugin with ImageIO.
     * This is usually not necessary as the plugin should be automatically discovered
     * through the service provider mechanism.
     */
    public static void registerPlugin() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new AVIFImageReaderSpi());
        registry.registerServiceProvider(new AVIFImageWriterSpi());
    }

    /**
     * Returns information about the plugin.
     * 
     * @return a string with plugin information
     */
    public static String getPluginInfo() {
        return "AVIF ImageIO Plugin v1.0\n" +
               "Supports reading and writing AVIF images\n" +
               "Plugin registered: " + isPluginRegistered();
    }
}
