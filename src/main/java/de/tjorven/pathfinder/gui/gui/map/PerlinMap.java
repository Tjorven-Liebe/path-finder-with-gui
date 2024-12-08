package de.tjorven.pathfinder.gui.gui.map;

import de.tjorven.pathfinder.gui.PerlinNoise;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class PerlinMap {

    private static final int WIDTH = 2000;
    private static final int HEIGHT = 1000;
    private static final int SCALE = 20; // Smaller scale for larger terrain features
    private static final int PIXEL_SIZE = 5;
    private double[][] map;

    public double[][] generateMap() {
        PerlinNoise perlin = new PerlinNoise(UUID.randomUUID().toString().getBytes());
        map = new double[WIDTH / PIXEL_SIZE][HEIGHT / PIXEL_SIZE];

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                // Combine multiple noise layers for smoother transitions
                double noiseValue = (perlin.noise(x / (double) SCALE, y / (double) SCALE) * 0.6
                        + perlin.noise(x / (double) (SCALE / 2), y / (double) (SCALE / 2)) * 0.3
                        + perlin.noise(x / (double) (SCALE * 2), y / (double) (SCALE * 2)) * 0.1)
                        * 200 - 100;
                map[x][y] = noiseValue;
            }
        }

        // Smooth the map multiple times
        map = smoothMap(map, 5);

        // Identify and merge small regions
        Map<Point, java.util.List<Point>> regions = floodFillRegions(map, 0); // Threshold at 0 for water regions
        mergeSmallRegions(map, regions, 50); // Merge regions smaller than 50 cells7

        return map;
    }

    private double[][] smoothMap(double[][] map, int iterations) {
        int width = map.length;
        int height = map[0].length;

        for (int i = 0; i < iterations; i++) {
            double[][] smoothed = new double[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double sum = 0;
                    int count = 0;

                    // Iterate over a larger neighborhood (5x5 instead of 3x3)
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dy = -2; dy <= 2; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;

                            // Check boundaries
                            if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                                sum += map[nx][ny];
                                count++;
                            }
                        }
                    }

                    // Compute average of neighboring cells
                    smoothed[x][y] = sum / count;
                }
            }

            map = smoothed; // Update the map with smoothed values
        }

        return map;
    }


    private Map<Point, java.util.List<Point>> floodFillRegions(double[][] map, double threshold) {
        int width = map.length;
        int height = map[0].length;
        boolean[][] visited = new boolean[width][height];
        Map<Point, java.util.List<Point>> regions = new HashMap<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!visited[x][y]) {
                    double value = map[x][y];
                    if (value < threshold) { // Only consider points below the threshold
                        java.util.List<Point> region = new ArrayList<>();
                        floodFill(x, y, value, map, visited, region);
                        regions.put(new Point(x, y), region);
                    }
                }
            }
        }

        return regions;
    }

    private void floodFill(int x, int y, double value, double[][] map, boolean[][] visited, java.util.List<Point> region) {
        int width = map.length;
        int height = map[0].length;
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty()) {
            Point point = queue.poll();
            int px = point.x;
            int py = point.y;

            if (px < 0 || py < 0 || px >= width || py >= height || visited[px][py] || map[px][py] != value) {
                continue;
            }

            visited[px][py] = true;
            region.add(point);

            // Add neighbors to the queue
            queue.add(new Point(px - 1, py));
            queue.add(new Point(px + 1, py));
            queue.add(new Point(px, py - 1));
            queue.add(new Point(px, py + 1));
        }
    }

    private void mergeSmallRegions(double[][] map, Map<Point, java.util.List<Point>> regions, int minRegionSize) {
        for (Map.Entry<Point, java.util.List<Point>> entry : regions.entrySet()) {
            List<Point> region = entry.getValue();
            if (region.size() < minRegionSize) {
                for (Point point : region) {
                    double height = map[point.x][point.y];
                    if (height > 5) { // Merge into sand if it's higher
                        map[point.x][point.y] = 10;
                    } else if (height > -20) { // Merge into shallow water if lower
                        map[point.x][point.y] = -10;
                    } else {
                        map[point.x][point.y] = -50; // Merge into deep water
                    }
                }
            }
        }
    }
}
