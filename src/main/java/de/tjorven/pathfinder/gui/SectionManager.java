package de.tjorven.pathfinder.gui;

import de.tjorven.pathfinder.gui.gui.map.PerlinMap;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SectionManager {
    public static final int SECTION_SIZE = 50; // Each section is 50x50 tiles
    private final File mapDirectory = new File("map-data");
    public final Map<Point, double[][]> sectionCache = new HashMap<>();
    private final PerlinMap perlinMap;

    public SectionManager(PerlinMap perlinMap) {
        this.perlinMap = perlinMap; // Initialize the PerlinMap instance
        if (!mapDirectory.exists()) {
            mapDirectory.mkdirs();
        }
    }

    // Save a map section to a file
    public void saveSection(Point sectionKey, double[][] sectionData) {
        File sectionFile = new File(mapDirectory, sectionKey.x + "_" + sectionKey.y + ".dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(sectionFile))) {
            oos.writeObject(sectionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load a map section from a file
    public double[][] loadSection(Point sectionKey) {
        File sectionFile = new File(mapDirectory, sectionKey.x + "_" + sectionKey.y + ".dat");
        if (sectionFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sectionFile))) {
                return (double[][]) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null; // Return null if the section does not exist
    }

    // Get a section from the cache or load it from file if not cached
    public double[][] getSection(Point sectionKey, PerlinMap unused) {
        if (!sectionCache.containsKey(sectionKey)) {
            double[][] sectionData = loadSection(sectionKey);
            if (sectionData == null) {
                // Generate the section if it doesn't exist
                sectionData = generateSection(sectionKey);
                saveSection(sectionKey, sectionData);
            }
            sectionCache.put(sectionKey, sectionData);
        }
        return sectionCache.get(sectionKey);
    }

    // Remove a section from the cache
    public void unloadSection(Point sectionKey) {
        sectionCache.remove(sectionKey);
    }

    // Generate a new map section using Perlin noise
    private double[][] generateSection(Point sectionKey) {
        double[][] section = new double[SECTION_SIZE][SECTION_SIZE];
        int startX = sectionKey.x * SECTION_SIZE;
        int startY = sectionKey.y * SECTION_SIZE;

        for (int x = 0; x < SECTION_SIZE; x++) {
            for (int y = 0; y < SECTION_SIZE; y++) {
                section[x][y] = perlinMap.generateTile(startX + x, startY + y);
            }
        }

        return section;
    }

    public void delete() {
        for (File file : mapDirectory.listFiles()) {
            file.delete();
        }
    }
}
