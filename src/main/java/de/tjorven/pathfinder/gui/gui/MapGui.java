package de.tjorven.pathfinder.gui.gui;

import de.tjorven.pathfinder.gui.PerlinNoise;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MapGui extends JPanel {
    private static final int WIDTH = 2000;
    private static final int HEIGHT = 1000;
    private static final int SCALE = 20; // Smaller scale for more visible features
    private static final int PIXEL_SIZE = 5;
    private double[][] map;
    private Point startPoint;
    private Point endPoint;
    private List<Point> path;

    public MapGui(JFrame frame) {
        frame.add(this);
        generateMap();

        JButton simulateButton = new JButton("Resimulate");
        simulateButton.addActionListener(event -> {
            generateMap();
            this.repaint();
            frame.repaint();
            frame.revalidate();
        });

        JButton findPathButton = new JButton("Find Path");
        findPathButton.addActionListener(event -> {
            long currentMillis = System.currentTimeMillis();
            findPath();
            System.out.println(System.currentTimeMillis() - currentMillis + "ms");
            this.repaint();
        });

        this.add(simulateButton);
        this.add(findPathButton);
        frame.repaint();
        frame.revalidate();
    }

    private void generateMap() {
        PerlinNoise perlin = new PerlinNoise(UUID.randomUUID().toString().getBytes());
        map = new double[WIDTH / PIXEL_SIZE][HEIGHT / PIXEL_SIZE];

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                double noiseValue = perlin.noise(x / (double) SCALE, y / (double) SCALE);
                map[x][y] = Math.max(noiseValue, 0.1); // Ensure a minimum value for reachability
            }
        }

        // Define random start and end points
        Random random = new Random();
        startPoint = new Point(random.nextInt(map.length), random.nextInt(map[0].length));
        endPoint = new Point(random.nextInt(map.length), random.nextInt(map[0].length));

        path = null; // Clear the previous path
    }

    private void findPath() {
        path = aStarPathfinding(startPoint, endPoint);
    }

    private List<Point> aStarPathfinding(Point start, Point end) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        Set<Point> closedSet = new HashSet<>();
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);
        openSet.add(new Node(start, heuristic(start, end)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.point.equals(end)) {
                return reconstructPath(cameFrom, end);
            }

            closedSet.add(current.point);

            for (Point neighbor : getNeighbors(current.point)) {
                if (closedSet.contains(neighbor)) continue;

                double tentativeGScore = gScore.getOrDefault(current.point, Double.MAX_VALUE) + cost(current.point, neighbor);

                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current.point);
                    gScore.put(neighbor, tentativeGScore);
                    double fScore = tentativeGScore + heuristic(neighbor, end);

                    openSet.add(new Node(neighbor, fScore));
                }
            }
        }

        return null; // No path found
    }

    private List<Point> getNeighbors(Point point) {
        List<Point> neighbors = new ArrayList<>();
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nx = point.x + dx[i];
            int ny = point.y + dy[i];
            if (nx >= 0 && ny >= 0 && nx < map.length && ny < map[0].length) {
                neighbors.add(new Point(nx, ny));
            }
        }

        return neighbors;
    }

    private double heuristic(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)); // Euclidean distance
    }

    private double cost(Point a, Point b) {
        double terrainCost = map[b.x][b.y]; // Terrain cost based on brightness (0.1 to 1)
        return 1 + Math.pow(1 - terrainCost, 3) * 50; // Exponential penalty for darker areas
    }

    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        List<Point> path = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        path.add(current);
        Collections.reverse(path);
        return path;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the terrain
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                int color = (int) (map[x][y] * 255); // Convert Perlin noise value to grayscale
                g.setColor(new Color(color, color, color));
                g.fillRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
            }
        }

        // Draw start and end points
        g.setColor(Color.GREEN);
        g.fillRect(startPoint.x * PIXEL_SIZE, startPoint.y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);

        g.setColor(Color.RED);
        g.fillRect(endPoint.x * PIXEL_SIZE, endPoint.y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);

        // Draw the path if it exists
        if (path != null) {
            g.setColor(Color.BLUE); // Use a constant color for the path
            for (Point point : path) {
                g.fillRect(point.x * PIXEL_SIZE, point.y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
            }
        }
    }


    private static class Node {
        Point point;
        double f;

        Node(Point point, double f) {
            this.point = point;
            this.f = f;
        }

        double getF() {
            return f;
        }
    }
}
