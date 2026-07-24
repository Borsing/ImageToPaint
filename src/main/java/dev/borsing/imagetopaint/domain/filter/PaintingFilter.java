package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.color.Cielab;
import dev.borsing.imagetopaint.domain.color.RgbCielabConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public record PaintingFilter(PaintingFilterParams params) implements ImageFilter {

    @Override
    public Image filter(Image imageSource) {
        RgbCielabConverter converter = new RgbCielabConverter();
        Cielab[][] cielabSource = converter.toCielabMatrix(imageSource);

        List<Cielab> distinctPixels = Arrays.stream(cielabSource)
                .flatMap(Arrays::stream)
                .distinct()
                .toList();

        List<Cielab> centroids = pickInitialCentroids(cielabSource, imageSource);

        Map<Cielab, Cielab> centroidByPixel = new HashMap<>();
        for (Cielab pixel : distinctPixels) {
            centroidByPixel.put(pixel, centroids.getFirst());
        }

        for (int iteration = 0; iteration < params.maxIterations(); iteration++) {
            // One pass both assigns each pixel to its nearest centroid and accumulates the sums
            // needed to recompute that centroid's new position - instead of separate
            // group-then-average passes over the whole pixel set.
            double[] sumLightness = new double[centroids.size()];
            double[] sumA = new double[centroids.size()];
            double[] sumB = new double[centroids.size()];
            int[] pixelsPerCentroid = new int[centroids.size()];
            Map<Cielab, Integer> centroidIndexByPixel = new HashMap<>();

            for (Cielab pixel : distinctPixels) {
                int nearestCentroidIndex = nearestCentroidIndex(pixel, centroids);
                centroidIndexByPixel.put(pixel, nearestCentroidIndex);

                sumLightness[nearestCentroidIndex] += pixel.lightness();
                sumA[nearestCentroidIndex] += pixel.a();
                sumB[nearestCentroidIndex] += pixel.b();
                pixelsPerCentroid[nearestCentroidIndex]++;
            }

            List<Cielab> newCentroids = new ArrayList<>(centroids.size());
            for (int i = 0; i < centroids.size(); i++) {
                newCentroids.add(pixelsPerCentroid[i] == 0
                        ? centroids.get(i)
                        : new Cielab(sumLightness[i] / pixelsPerCentroid[i],
                                sumA[i] / pixelsPerCentroid[i],
                                sumB[i] / pixelsPerCentroid[i]));
            }

            for (Map.Entry<Cielab, Integer> entry : centroidIndexByPixel.entrySet()) {
                centroidByPixel.put(entry.getKey(), newCentroids.get(entry.getValue()));
            }

            boolean centroidsStillMoving = !newCentroids.equals(centroids);
            centroids = newCentroids;
            if (!centroidsStillMoving) {
                break;
            }
        }

        Cielab[][] result = new Cielab[imageSource.height()][imageSource.width()];
        for (int y = 0; y < imageSource.height(); y++) {
            for (int x = 0; x < imageSource.width(); x++) {
                result[y][x] = centroidByPixel.get(cielabSource[y][x]);
            }
        }

        return converter.fromCielabMatrix(result);
    }

    private List<Cielab> pickInitialCentroids(Cielab[][] cielabSource, Image imageSource) {
        Random random = new Random(params.seed());
        List<Cielab> centroids = new ArrayList<>(params.numberOfColors());
        for (int i = 0; i < params.numberOfColors(); i++) {
            centroids.add(cielabSource[random.nextInt(imageSource.height())][random.nextInt(imageSource.width())]);
        }
        return centroids;
    }

    private int nearestCentroidIndex(Cielab pixel, List<Cielab> centroids) {
        int nearestIndex = 0;
        double nearestDistance = squaredDistance(pixel, centroids.getFirst());
        for (int i = 1; i < centroids.size(); i++) {
            double distance = squaredDistance(pixel, centroids.get(i));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    private double squaredDistance(Cielab pixel1, Cielab pixel2) {
        double dl = pixel1.lightness() - pixel2.lightness();
        double da = pixel1.a() - pixel2.a();
        double db = pixel1.b() - pixel2.b();
        return dl * dl + da * da + db * db;
    }
}