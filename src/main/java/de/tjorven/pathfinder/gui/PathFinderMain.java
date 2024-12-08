package de.tjorven.pathfinder.gui;

import de.tjorven.pathfinder.gui.gui.HomeGui;

import javax.swing.*;
import java.awt.*;

public class PathFinderMain {

    public static void main(String[] args) {
        JFrame frame = new JFrame("PathFinder Visualization");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.WHITE);

        frame.add(new HomeGui(frame));
        frame.setVisible(true);
    }

}
