package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.color.Cielab;
import dev.borsing.imagetopaint.domain.color.RgbCielabConverter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record PaintingFilter(PaintingFilterParams params) implements ImageFilter {

    @Override
    public Image filter(Image imageSource) {
        RgbCielabConverter rgbCielabConverter = new RgbCielabConverter();
        Cielab[][] cielabSource = rgbCielabConverter.toCielabMatrix(imageSource);

        Random random = new Random(params.seed());
        Set<Cielab> centroids = IntStream.range(0, params.numberOfColors())
                .mapToObj(i -> cielabSource[random.nextInt(imageSource.height())][random.nextInt(imageSource.width())])
                .collect(Collectors.toSet());

        Set<Cielab> finalCentroids1 = centroids;
        Map<Cielab, Cielab> bestCentroidPerPixel = Arrays.stream(cielabSource)
                .flatMap(Arrays::stream)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), c -> finalCentroids1.iterator().next()));

        for (int iter = 0; iter < params.maxIterations(); iter++) {
            Set<Cielab> finalCentroids = centroids;

            Map<Cielab, Cielab> newBestCentroidPerPixel = bestCentroidPerPixel.keySet().stream()
                    .map(pixel -> {
                        Cielab bestCentroid = finalCentroids.stream()
                                .min(Comparator.comparingDouble(centroid -> squaredDistance(pixel, centroid)))
                                .orElseThrow();
                        return Map.entry(pixel, bestCentroid);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<Cielab, Cielab> newCentroidAverageByCentroid = newBestCentroidPerPixel.entrySet()
                    .stream()
                    .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> average(e.getValue())));

            centroids = new HashSet<>(newCentroidAverageByCentroid.values());

            bestCentroidPerPixel = newBestCentroidPerPixel.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> newCentroidAverageByCentroid.get(e.getValue())));
        }

        Cielab[][] result = new Cielab[imageSource.height()][imageSource.width()];
        for (int height = 0; height < imageSource.height(); height++) {
            for (int width = 0; width < imageSource.width(); width++) {
                result[height][width] = bestCentroidPerPixel.get(cielabSource[height][width]);
            }
        }

        return rgbCielabConverter.fromCielabMatrix(result);
    }

    private Cielab average(List<Cielab> pixels) {
        double lightnessAverage = pixels.stream().mapToDouble(Cielab::lightness).average().orElse(0.0);
        double aAverage = pixels.stream().mapToDouble(Cielab::a).average().orElse(0.0);
        double bAverage = pixels.stream().mapToDouble(Cielab::b).average().orElse(0.0);
        return new Cielab(lightnessAverage, aAverage, bAverage);
    }

    private double squaredDistance(Cielab pixel1, Cielab pixel2) {
        double dl = pixel1.lightness() - pixel2.lightness();
        double da = pixel1.a() - pixel2.a();
        double db = pixel1.b() - pixel2.b();
        return dl * dl + da * da + db * db;
    }
}