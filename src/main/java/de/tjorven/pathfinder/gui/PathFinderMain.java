package de.tjorven.pathfinder.gui;

import de.tjorven.pathfinder.gui.gui.MapGui;

import javax.swing.*;

public class PathFinderMain {

    public static void main(String[] args) {
        JFrame frame = new JFrame("PathFinder Visualization");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        MapGui mapGui = new MapGui(frame);

        frame.setVisible(true);
    }

}
