package com.github.wizzardo.avif.imageio;

import org.jextract.avif.avifDecoder;
import org.jextract.avif.avifImage;
import org.jextract.avif.avifRGBImage;
import org.jextract.avif.avif_h;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class AVIFImageReader extends ImageReader {

    static {
        try {
            System.out.println("java.library.path: " + System.getProperty("java.library.path"));
            System.loadLibrary("avif"); // This might give a more verbose error message
            System.out.println("libavif loaded successfully via System.loadLibrary (diagnostic)");

            try (Arena arena = Arena.ofConfined()) {
                System.out.println(avif_h.avifVersion().getString(0));
                MemorySegment mem = arena.allocate(256);
                avif_h.avifCodecVersions(mem);
                System.out.println(mem.getString(0));
            }
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Error loading libavif with System.loadLibrary: " + e.getMessage());
            // This message can sometimes be more helpful in debugging.
        }
    }

    private ImageInputStream stream;
    private int width = -1;
    private int height = -1;
    private boolean headerRead = false;

    protected AVIFImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input instanceof ImageInputStream) {
            this.stream = (ImageInputStream) input;
            // Reset header information
            headerRead = false;
            width = -1;
            height = -1;
        } else {
            throw new IllegalArgumentException("Input not an ImageInputStream");
        }
    }

    private void readHeader() throws IOException {
        if (headerRead) return;
        if (stream == null) {
            throw new IllegalStateException("No input stream");
        }

        // In a real implementation, this would parse the AVIF header
        // and extract width, height, and other metadata
        // For now, we'll just set placeholder values
        width = 0;
        height = 0;
        headerRead = true;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1; // AVIF files contain a single image (for now, ignoring sequences)
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        readHeader();
        return width;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        readHeader();
        return height;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        // This would return the supported image types for the AVIF image
        // For now, return null as this is a placeholder implementation
        return null;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {

        MemorySegment decoder = null;
        try (Arena arena = Arena.ofConfined()) {
            decoder = avif_h.avifDecoderCreate();
//                byte[] bytes = Files.readAllBytes(Path.of("/users/wizzardo/downloads/hato.profile0.8bpc.yuv420.avif"));
//                byte[] bytes = Files.readAllBytes(Path.of("/users/wizzardo/downloads/hato.profile0.10bpc.yuv420.avif"));
//            byte[] bytes = Files.readAllBytes(Path.of("/users/wizzardo/downloads/PNG_transparency_demonstration_1.avif"));
            byte[] bytes = readAllBytes(stream);
            ByteBuffer fileBuffer = ByteBuffer.allocateDirect(bytes.length);
            fileBuffer.put(bytes);
            fileBuffer.flip();
            MemorySegment fileMemorySegment = MemorySegment.ofBuffer(fileBuffer);
            int result = avif_h.avifDecoderSetIOMemory(decoder, fileMemorySegment, fileBuffer.capacity());
            if (result != avif_h.AVIF_RESULT_OK()) {
                throw new IllegalStateException("avifDecoderSetIOMemory failed: " + getAvifResultToString(result));
            }

            result = avif_h.avifDecoderParse(decoder);
            if (result != avif_h.AVIF_RESULT_OK()) {
                throw new IllegalStateException("avifDecoderParse failed: " + getAvifResultToString(result));
            }

            boolean hasAlpha = avifDecoder.alphaPresent(decoder) == 1;
            System.out.println("has alpha: " + hasAlpha + " imageCount: " + avifDecoder.imageCount(decoder));

            MemorySegment image = avifDecoder.image(decoder);
            System.out.println("image: " + avifImage.width(image) + "x" + avifImage.height(image) + " depth: " + avifImage.depth(image));
//                if (avifImage.depth(image) > 8) {
//                    throw new IllegalStateException("depth of " + avifImage.depth(image) + " not implemented yet");
//                }

            long startTime = System.nanoTime();

            result = avif_h.avifDecoderNextImage(decoder);
            if (result != avif_h.AVIF_RESULT_OK()) {
                throw new IllegalStateException("avifDecoderNextImage failed: " + getAvifResultToString(result));
            }


            MemorySegment rgbImage = avifRGBImage.allocate(arena);
            avif_h.avifRGBImageSetDefaults(rgbImage, image);

            avifRGBImage.depth(rgbImage, 8);
            avifRGBImage.format(rgbImage, avif_h.AVIF_RGB_FORMAT_ARGB());

//            ByteBuffer raster = ByteBuffer.allocateDirect(avifImage.width(image) * avifImage.height(image) * 4);
//            MemorySegment rasterMemorySegment = MemorySegment.ofBuffer(raster);
//            avifRGBImage.pixels(rgbImage, rasterMemorySegment);

            result = avif_h.avifRGBImageAllocatePixels(rgbImage);
            if (result != avif_h.AVIF_RESULT_OK()) {
                throw new IllegalStateException("avifRGBImageAllocatePixels failed: " + getAvifResultToString(result));
            }

            result = avif_h.avifImageYUVToRGB(image, rgbImage);
            if (result != avif_h.AVIF_RESULT_OK()) {
                throw new IllegalStateException("avifImageYUVToRGB failed: " + getAvifResultToString(result));
            }

            MemorySegment pixels = avifRGBImage.pixels(rgbImage);
            MemorySegment pixelsSlice = pixels.asSlice(0, (long) avifImage.width(image) * avifImage.height(image) * 4);
            ByteBuffer raster = pixelsSlice.asByteBuffer();
            try {
                int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
                BufferedImage bi = new BufferedImage(avifImage.width(image), avifImage.height(image), type);
                WritableRaster biRaster = bi.getRaster();
                IntBuffer intBuffer = raster.asIntBuffer();
                int[] ints = new int[bi.getWidth()];
                for (int y = 0; y < bi.getHeight(); y++) {
                    intBuffer.get(ints);
//                for (int i = 0; i < ints.length; i++) {
//                    int pixel = ints[i];
//                    ints[i] = pixel >> 8;
//                }
                    biRaster.setDataElements(0, y, bi.getWidth(), 1, ints);
                }
                return bi;
            } finally {
                avif_h.avifRGBImageFreePixels(rgbImage);
            }
//            int[] ints = new int[bi.getWidth() * bi.getHeight()];
//            intBuffer.get(ints);
//            biRaster.setDataElements(0, 0, bi.getWidth(), bi.getHeight(), ints);

        } finally {
            if (decoder != null)
                avif_h.avifDecoderDestroy(decoder);
        }

    }

    private void checkIndex(int imageIndex) {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("Only single image supported");
        }
    }

    private static String getAvifResultToString(int result) {
        MemorySegment memorySegment = avif_h.avifResultToString(result);
//        memorySegment.getString(0)
//        ByteBuffer buffer = memorySegment.asByteBuffer();
        return memorySegment.getString(0);
//        return result + "";
    }

    public static byte[] readAllBytes(ImageInputStream iis) throws IOException {
        // Try fast path if length is known
        long length = iis.length();
        if (length >= 0 && length <= Integer.MAX_VALUE) {
            byte[] data = new byte[(int) length];
            iis.readFully(data);
            return data;
        }

        // Fallback: buffered read
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;

        while ((n = iis.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }

        return buffer.toByteArray();
    }
}