package de.tjorven.pathfinder.gui.gui;

import de.tjorven.pathfinder.gui.AsyncTickService;

import javax.swing.*;

public class HomeGui extends JPanel {

    public HomeGui(JFrame frame, AsyncTickService asyncTickService) {
        super();

        JButton start = new JButton("Start Simulation");
        start.addActionListener(action -> {
            frame.remove(this);
            frame.add(new MapGui(frame, asyncTickService));
            frame.repaint();
        });

        add(start);
        repaint();
    }
}
