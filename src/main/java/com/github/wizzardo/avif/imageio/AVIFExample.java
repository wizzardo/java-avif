package com.github.wizzardo.avif.imageio;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Example class demonstrating how to use the AVIF ImageIO plugin.
 */
public class AVIFExample {

    /**
     * Reads an AVIF image file and returns a BufferedImage.
     * 
     * @param file the AVIF file to read
     * @return the decoded BufferedImage, or null if reading fails
     */
    public static BufferedImage readAVIF(File file) {
        try {
            // Ensure the plugin is registered
            if (!AVIFPlugin.isPluginRegistered()) {
                AVIFPlugin.registerPlugin();
            }
            
            // Get an ImageReader for AVIF format
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("avif");
            if (!readers.hasNext()) {
                System.err.println("No AVIF image reader found");
                return null;
            }
            
            ImageReader reader = readers.next();
            try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
                reader.setInput(input);
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            System.err.println("Error reading AVIF file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Writes a BufferedImage to an AVIF file.
     * 
     * @param image the BufferedImage to write
     * @param file the output file
     * @return true if writing succeeds, false otherwise
     */
    public static boolean writeAVIF(BufferedImage image, File file) {
        try {
            // Ensure the plugin is registered
            if (!AVIFPlugin.isPluginRegistered()) {
                AVIFPlugin.registerPlugin();
            }
            
            // Get an ImageWriter for AVIF format
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("avif");
            if (!writers.hasNext()) {
                System.err.println("No AVIF image writer found");
                return false;
            }
            
            ImageWriter writer = writers.next();
            try (ImageOutputStream output = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(output);
                writer.write(image);
                return true;
            } finally {
                writer.dispose();
            }
        } catch (IOException e) {
            System.err.println("Error writing AVIF file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Simple example of using the AVIF ImageIO plugin.
     */
    public static void main(String[] args) {
        System.out.println(AVIFPlugin.getPluginInfo());
        
        // Example usage:
        // BufferedImage image = readAVIF(new File("input.avif"));
        // if (image != null) {
        //     // Process the image...
        //     writeAVIF(image, new File("output.avif"));
        // }
    }
}