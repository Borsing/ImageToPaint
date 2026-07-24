package dev.borsing.imagetopaint.adapter;

/**
 * Format and dimensions read from an image's magic bytes, without fully decoding its pixels.
 */
public record ImageMetadata(String format, int width, int height) {
}