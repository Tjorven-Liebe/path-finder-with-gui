package de.tjorven.pathfinder.gui.gui;

import de.tjorven.pathfinder.gui.gui.map.MapPathfinder;
import de.tjorven.pathfinder.gui.gui.map.PerlinMap;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapGui extends JPanel {
    private static final int WIDTH = 2000;
    private static final int HEIGHT = 1000;
    private static final int SCALE = 20; // Smaller scale for larger terrain features
    private static final int PIXEL_SIZE = 5;
    MapPathfinder mapPathfinder;
    private double[][] map;

    public MapGui(JFrame frame) {
        frame.add(this);
        PerlinMap perlinMap = new PerlinMap();
        map = perlinMap.generateMap();
        mapPathfinder = new MapPathfinder(map);
        mapPathfinder.clearPath();
        mapPathfinder.generateRandomStartAndEndPoints();

        JButton simulateButton = new JButton("Resimulate");
        simulateButton.addActionListener(event -> {
            mapPathfinder.clearPath();
            map = perlinMap.generateMap();

            mapPathfinder.generateRandomStartAndEndPoints();

            this.repaint();
            frame.repaint();
            frame.revalidate();
        });

        JButton findPathButton = new JButton("Find Path");
        findPathButton.addActionListener(event -> {
            mapPathfinder.findPath();
            this.repaint();
        });

        this.add(simulateButton);
        this.add(findPathButton);
        frame.repaint();
        frame.revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                double height = map[x][y];
                Color color = getBiomeColor(height); // Get color based on smooth gradients
                g.setColor(color);
                g.fillRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
            }
        }

        // Draw start and end points
        g.setColor(Color.GREEN);
        g.fillRect(mapPathfinder.getStartPoint().x * PIXEL_SIZE, mapPathfinder.getStartPoint().y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);

        g.setColor(Color.RED);
        g.fillRect(mapPathfinder.getEndPoint().x * PIXEL_SIZE, mapPathfinder.getEndPoint().y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);

        // Draw the path if it exists
        List<Point> path = mapPathfinder.getPath();
        if (path != null) {
            g.setColor(Color.RED); // Path is red
            for (Point point : path) {
                g.fillRect(point.x * PIXEL_SIZE, point.y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
            }
        }
    }

    private Color getBiomeColor(double height) {
        if (height > 60) {
            return interpolateColor(new Color(169, 169, 169), Color.GRAY, (height - 60) / 40); // Dark Gray to Light Gray
        } else if (height > 25) {
            return interpolateColor(new Color(0, 100, 0), new Color(34, 139, 34), (height - 25) / 35); // Dark Green to Grass Green
        } else if (height > 5) {
            return interpolateColor(new Color(194, 178, 128), new Color(237, 201, 175), (height - 5) / 20); // Dark Sand to Light Sand
        } else if (height > -20) {
            return interpolateColor(new Color(0, 0, 200), new Color(0, 0, 255), (height + 20) / 25); // Dark Blue to Light Blue (Shallow Water)
        } else {
            return interpolateColor(new Color(0, 0, 100), new Color(0, 0, 139), height / -20); // Dark Blue to Deep Blue (Deep Water)
        }
    }

    private Color interpolateColor(Color color1, Color color2, double t) {
        t = Math.max(0, Math.min(1, t)); // Clamp t to [0, 1]
        t = Math.pow(t, 0.6); // Exponential curve to enhance contrast
        int r = (int) (color1.getRed() * (1 - t) + color2.getRed() * t);
        int g = (int) (color1.getGreen() * (1 - t) + color2.getGreen() * t);
        int b = (int) (color1.getBlue() * (1 - t) + color2.getBlue() * t);
        return new Color(r, g, b);
    }
}
