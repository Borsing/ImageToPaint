package dev.borsing.imagetopaint.domain.color;

import dev.borsing.imagetopaint.domain.Image;

public class RgbCielabConverter {

    public Cielab[][] toCielabMatrix(Image image) {
        Cielab[][] result = new Cielab[image.height()][image.width()];
        for (int i = 0; i < image.height(); i++) {
            for (int j = 0; j < image.width(); j++) {
                result[i][j] = rgbToLab(image.pixels()[i][j]);
            }
        }
        return result;
    }

    /**
     * Inverse of {@link #toCielabMatrix(Image)}. Channels are clamped to [0, 255] since
     * out-of-gamut Lab values (from interpolation/quantization) can round slightly outside range.
     */
    public Image fromCielabMatrix(Cielab[][] cielab) {
        Rgb[][] pixels = new Rgb[cielab.length][];
        for (int i = 0; i < cielab.length; i++) {
            pixels[i] = new Rgb[cielab[i].length];
            for (int j = 0; j < cielab[i].length; j++) {
                pixels[i][j] = labToRgb(cielab[i][j]);
            }
        }
        return new Image(pixels);
    }
    private static double srgbToLinear(double c) {
        c = c / 255.0;
        return (c <= 0.04045) ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double f(double t) {
        double eps = Math.pow(6.0 / 29.0, 3);
        return (t > eps) ? Math.cbrt(t) : t / (3 * Math.pow(6.0 / 29.0, 2)) + 4.0 / 29.0;
    }

    private static Cielab rgbToLab(Rgb pixel) {
        double rLin = srgbToLinear(pixel.red());
        double gLin = srgbToLinear(pixel.green());
        double bLin = srgbToLinear(pixel.blue());

        double X = rLin * 0.4124 + gLin * 0.3576 + bLin * 0.1805;
        double Y = rLin * 0.2126 + gLin * 0.7152 + bLin * 0.0722;
        double Z = rLin * 0.0193 + gLin * 0.1192 + bLin * 0.9505;

        double Xn = 0.95047, Yn = 1.0, Zn = 1.08883;
        double xr = X / Xn, yr = Y / Yn, zr = Z / Zn;

        double fx = f(xr), fy = f(yr), fz = f(zr);

        double L = 116 * fy - 16;
        double a = 500 * (fx - fy);
        double bLab = 200 * (fy - fz);

        return new Cielab(L, a, bLab);
    }

    private static double finv(double t) {
        double delta = 6.0 / 29.0;
        return (t > delta) ? t * t * t : 3 * delta * delta * (t - 4.0 / 29.0);
    }

    private static double linearToSrgb(double c) {
        return (c <= 0.0031308) ? c * 12.92 : 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055;
    }

    private static int toChannel(double linear) {
        int value = (int) Math.round(linearToSrgb(linear) * 255.0);
        return Math.clamp(value, 0, 255);
    }

    private static Rgb labToRgb(Cielab lab) {
        double fy = (lab.lightness() + 16) / 116;
        double fx = fy + lab.a() / 500;
        double fz = fy - lab.b() / 200;

        double xn = 0.95047, yn = 1.0, zn = 1.08883;
        double x = finv(fx) * xn;
        double y = finv(fy) * yn;
        double z = finv(fz) * zn;

        double rLin = x * 3.2406 + y * -1.5372 + z * -0.4986;
        double gLin = x * -0.9689 + y * 1.8758 + z * 0.0415;
        double bLin = x * 0.0557 + y * -0.2040 + z * 1.0570;

        return new Rgb(toChannel(rLin), toChannel(gLin), toChannel(bLin));
    }
}
