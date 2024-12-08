package de.tjorven.pathfinder.gui;

import de.tjorven.pathfinder.gui.gui.HomeGui;

import javax.swing.*;
import java.awt.*;

public class PathFinderMain {

    public static void main(String[] args) {
        JFrame frame = new JFrame("PathFinder Visualization");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.WHITE);

        AsyncTickService asyncTickService = new AsyncTickService();
        asyncTickService.startTicker();
        frame.add(new HomeGui(frame, asyncTickService));
        frame.setVisible(true);
    }

}
