package de.tjorven.pathfinder.gui.gui.map;

import de.tjorven.pathfinder.gui.SectionManager;

import java.awt.*;
import java.util.List;
import java.util.*;

public class MapPathfinder {
    private final SectionManager sectionManager;
    private final int maxSearchRadius; // Limit the search space to reduce initialization time
    private Point startPoint;
    private Point endPoint;
    private List<Point> path;

    public MapPathfinder(SectionManager sectionManager, int maxSearchRadius) {
        this.sectionManager = sectionManager;
        this.maxSearchRadius = maxSearchRadius;
    }

    public void findPath() {
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

            // Clamp neighbors within valid bounds
            if (nx >= 0 && ny >= 0 && nx < maxSearchRadius * 2 && ny < maxSearchRadius * 2) {
                neighbors.add(new Point(nx, ny));
            }
        }

        return neighbors;
    }

    private double heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); // Manhattan distance
    }

    private double cost(Point a, Point b) {
        double terrainHeight = getHeightAt(b.x, b.y);
        if (terrainHeight < -20) return Double.MAX_VALUE; // Deep water is inaccessible
        return 1 + Math.abs(15 - terrainHeight); // Adjust cost based on terrain height
    }

    private double getHeightAt(int x, int y) {
        int sectionX = Math.floorDiv(x, SectionManager.SECTION_SIZE);
        int sectionY = Math.floorDiv(y, SectionManager.SECTION_SIZE);
        Point sectionKey = new Point(sectionX, sectionY);
        double[][] section = sectionManager.getSection(sectionKey, null);

        if (section != null) {
            int localX = Math.floorMod(x, SectionManager.SECTION_SIZE);
            int localY = Math.floorMod(y, SectionManager.SECTION_SIZE);
            return section[localX][localY];
        }

        return 0; // Default height if section is not loaded
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

    public void clearPath() {
        path = null;
    }

    public void generateRandomStartAndEndPoints() {
        Random random = new Random();

        do {
            startPoint = generateRandomPoint(random);
        } while (getHeightAt(startPoint.x, startPoint.y) < 5); // Ensure start is on land

        do {
            endPoint = generateRandomPoint(random);
        } while (getHeightAt(endPoint.x, endPoint.y) < 5 || startPoint.equals(endPoint)); // Ensure end is on land
    }

    private Point generateRandomPoint(Random random) {
        int x = random.nextInt(maxSearchRadius * 2) - maxSearchRadius;
        int y = random.nextInt(maxSearchRadius * 2) - maxSearchRadius;

        // Clamp the point to ensure it is within valid map bounds
        x = Math.max(0, x);
        y = Math.max(0, y);

        return new Point(x, y);
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public List<Point> getPath() {
        return path;
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
