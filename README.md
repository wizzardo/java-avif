# AVIF ImageIO Plugin

This project provides a Java ImageIO plugin for the AVIF (AV1 Image File Format) image format. It allows Java applications to read and write AVIF images using the standard ImageIO API.

## Features

- Read AVIF images using ImageIO
- Write images to AVIF format using ImageIO
- Automatic plugin registration via the Java Service Provider Interface (SPI)
- Compatible with standard ImageIO workflows

## Usage

### Adding the Plugin to Your Project

1. Add the JAR file to your project's dependencies
2. The plugin will be automatically registered with ImageIO

### Reading AVIF Images

```java
// Method 1: Using ImageIO directly
BufferedImage image = ImageIO.read(new File("image.avif"));

// Method 2: Using the helper class
BufferedImage image = AVIFExample.readAVIF(new File("image.avif"));
```

### Writing AVIF Images

```java
// Method 1: Using ImageIO directly
BufferedImage image = ...;
ImageIO.write(image, "avif", new File("output.avif"));

// Method 2: Using the helper class
AVIFExample.writeAVIF(image, new File("output.avif"));
```

### Manual Registration

If the plugin is not automatically detected, you can register it manually:

```java
AVIFPlugin.registerPlugin();
```

## Implementation Details

This plugin is a minimal scaffold that connects to the native libavif library. The actual AVIF encoding and decoding is performed by the native library.

### Project Structure

- `AVIFImageReader` - Implements the ImageIO reader for AVIF
- `AVIFImageReaderSpi` - Service provider for the AVIF reader
- `AVIFImageWriter` - Implements the ImageIO writer for AVIF
- `AVIFImageWriterSpi` - Service provider for the AVIF writer
- `AVIFPlugin` - Utility class for plugin management
- `AVIFExample` - Example usage of the plugin

## Future Enhancements

The current implementation is a minimal scaffold. Future enhancements may include:

- Support for AVIF-specific features (HDR, alpha channels, etc.)
- Performance optimizations
- Support for AVIF animation
- Metadata handling
