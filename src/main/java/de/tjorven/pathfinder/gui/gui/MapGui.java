package de.tjorven.pathfinder.gui.gui;

import de.tjorven.pathfinder.gui.SectionManager;
import de.tjorven.pathfinder.gui.gui.map.MapPathfinder;
import de.tjorven.pathfinder.gui.gui.map.PerlinMap;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapGui extends JPanel implements Scrollable {
    private static final int INITIAL_PIXEL_SIZE = 5;
    private static final int MIN_PIXEL_SIZE = 1;
    private static final int MAX_PIXEL_SIZE = 50;
    private final SectionManager sectionManager;
    private final PerlinMap perlinMap;
    private final MapPathfinder mapPathfinder;
    private final int visibleBuffer = 2; // Number of extra sections to load around the visible area
    private int pixelSize = INITIAL_PIXEL_SIZE;

    public MapGui(JFrame frame) {
        JScrollPane scrollPane = new JScrollPane(this);

        // Configure scroll bars
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        frame.add(scrollPane, BorderLayout.CENTER);

        perlinMap = new PerlinMap();
        sectionManager = new SectionManager(perlinMap); // Pass PerlinMap instance to SectionManager
        mapPathfinder = new MapPathfinder(sectionManager, 1000); // Initialize pathfinder with section manager

        // Set initial preferred size
        this.setPreferredSize(new Dimension(2000, 1000));

        JPanel interaction = getInteraction();
        frame.add(interaction, BorderLayout.NORTH);

        // Add scroll and zoom functionality
        addMouseWheelListener(e -> {
            if (e.isShiftDown() && !e.isControlDown()) {
                JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
                int scrollAmount = e.getUnitsToScroll() * horizontalScrollBar.getUnitIncrement();
                horizontalScrollBar.setValue(horizontalScrollBar.getValue() + scrollAmount);
            } else if (e.isControlDown() && !e.isShiftDown()) {
                int rotation = e.getWheelRotation();
                if (rotation < 0 && pixelSize < MAX_PIXEL_SIZE) {
                    pixelSize++;
                } else if (rotation > 0 && pixelSize > MIN_PIXEL_SIZE) {
                    pixelSize--;
                }
                updatePreferredSize();
                revalidate();
                repaint();
            } else if (!e.isControlDown() && !e.isShiftDown()) {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                int scrollAmount = e.getUnitsToScroll() * verticalScrollBar.getUnitIncrement();
                verticalScrollBar.setValue(verticalScrollBar.getValue() + scrollAmount);
            }
        });

        handleDynamicSectionManagement(scrollPane);

        // Generate random valid start and end points for the pathfinder
        mapPathfinder.generateRandomStartAndEndPoints();
    }

    private void updatePreferredSize() {
        int totalSectionsX = sectionManager.sectionCache.keySet().stream()
                .mapToInt(value -> value.x)
                .max()
                .orElse(0) + 1;

        int totalSectionsY = sectionManager.sectionCache.keySet().stream()
                .mapToInt(value -> value.y)
                .max()
                .orElse(0) + 1;

        int width = totalSectionsX * SectionManager.SECTION_SIZE * pixelSize;
        int height = totalSectionsY * SectionManager.SECTION_SIZE * pixelSize;

        setPreferredSize(new Dimension(width, height));
    }

    private void handleDynamicSectionManagement(JScrollPane scrollPane) {
        scrollPane.getViewport().addChangeListener(e -> {
            Rectangle viewRect = scrollPane.getViewport().getViewRect();

            int startX = (int) Math.floor(viewRect.x / (double) (SectionManager.SECTION_SIZE * pixelSize)) - visibleBuffer;
            int startY = (int) Math.floor(viewRect.y / (double) (SectionManager.SECTION_SIZE * pixelSize)) - visibleBuffer;
            int endX = (int) Math.ceil((viewRect.x + viewRect.width) / (double) (SectionManager.SECTION_SIZE * pixelSize)) + visibleBuffer;
            int endY = (int) Math.ceil((viewRect.y + viewRect.height) / (double) (SectionManager.SECTION_SIZE * pixelSize)) + visibleBuffer;

            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    sectionManager.getSection(new Point(x, y), perlinMap);
                }
            }

            sectionManager.sectionCache.keySet().removeIf(key ->
                    key.x < startX || key.x > endX || key.y < startY || key.y > endY
            );

            repaint();
        });
    }

    private JPanel getInteraction() {
        JPanel interaction = new JPanel();
        JButton simulateButton = new JButton("Resimulate");
        simulateButton.addActionListener(event -> {
            sectionManager.delete();
            sectionManager.sectionCache.clear();
            mapPathfinder.generateRandomStartAndEndPoints();
            repaint();
        });

        JButton findPathButton = new JButton("Find Path");
        findPathButton.addActionListener(event -> {
            mapPathfinder.findPath();
            repaint();
        });

        interaction.add(simulateButton);
        interaction.add(findPathButton);
        return interaction;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle viewRect = ((JScrollPane) getParent().getParent()).getViewport().getViewRect();

        int startX = (int) Math.floor(viewRect.x / (double) (SectionManager.SECTION_SIZE * pixelSize));
        int startY = (int) Math.floor(viewRect.y / (double) (SectionManager.SECTION_SIZE * pixelSize));
        int endX = (int) Math.ceil((viewRect.x + viewRect.width) / (double) (SectionManager.SECTION_SIZE * pixelSize));
        int endY = (int) Math.ceil((viewRect.y + viewRect.height) / (double) (SectionManager.SECTION_SIZE * pixelSize));

        for (int sx = startX; sx <= endX; sx++) {
            for (int sy = startY; sy <= endY; sy++) {
                double[][] section = sectionManager.getSection(new Point(sx, sy), perlinMap);
                if (section != null) {
                    drawSection(g, section, sx, sy);
                }
            }
        }

        Point startPoint = mapPathfinder.getStartPoint();
        Point endPoint = mapPathfinder.getEndPoint();
        if (startPoint != null) {
            g.setColor(Color.GREEN);
            g.fillRect(startPoint.x * pixelSize, startPoint.y * pixelSize, pixelSize * 5, pixelSize * 5);
        }
        if (endPoint != null) {
            g.setColor(Color.RED);
            g.fillRect(endPoint.x * pixelSize, endPoint.y * pixelSize, pixelSize * 5, pixelSize * 5);
        }

        List<Point> path = mapPathfinder.getPath();
        if (path != null) {
            g.setColor(Color.MAGENTA);
            for (Point point : path) {
                g.fillRect(point.x * pixelSize, point.y * pixelSize, pixelSize * 5, pixelSize * 5);
            }
        }
    }

    private void drawSection(Graphics g, double[][] section, int sectionX, int sectionY) {
        for (int x = 0; x < SectionManager.SECTION_SIZE; x++) {
            for (int y = 0; y < SectionManager.SECTION_SIZE; y++) {
                int globalX = sectionX * SectionManager.SECTION_SIZE + x;
                int globalY = sectionY * SectionManager.SECTION_SIZE + y;
                g.setColor(getBiomeColor(section[x][y]));
                g.fillRect(globalX * pixelSize, globalY * pixelSize, pixelSize, pixelSize);
            }
        }
    }

    private Color getBiomeColor(double height) {
        if (height > 60) return Color.GRAY;
        if (height > 25) return new Color(34, 139, 34);
        if (height > 5) return new Color(237, 201, 175);
        if (height > -20) return new Color(0, 0, 255);
        return new Color(0, 0, 139);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(800, 600);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return pixelSize;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return pixelSize * 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}