package de.tjorven.pathfinder.gui.gui.map;

import java.awt.*;
import java.util.List;
import java.util.*;

public class MapPathfinder {

    private final double[][] map;
    private Point startPoint;
    private Point endPoint;
    private List<Point> path;

    public MapPathfinder(double[][] map) {
        this.map = map;
    }

    public void findPath() {
        path = aStarPathfinding(startPoint, endPoint);
    }

    private java.util.List<Point> aStarPathfinding(Point start, Point end) {
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

    private java.util.List<Point> getNeighbors(Point point) {
        java.util.List<Point> neighbors = new ArrayList<>();
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
        double terrainCost = map[b.x][b.y]; // Terrain cost based on height
        return 1 + Math.abs(15 - terrainCost); // Higher terrain values increase cost
    }

    private java.util.List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
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
        // Define random start and end points
        Random random = new Random();
        startPoint = new Point(random.nextInt(map.length), random.nextInt(map[0].length));
        endPoint = new Point(random.nextInt(map.length), random.nextInt(map[0].length));
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
