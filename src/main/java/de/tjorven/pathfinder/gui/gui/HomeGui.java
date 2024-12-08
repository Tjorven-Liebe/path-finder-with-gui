package de.tjorven.pathfinder.gui.gui;

import javax.swing.*;

public class HomeGui extends JPanel {

    public HomeGui(JFrame frame) {
        super();

        JButton start = new JButton("Start Simulation");
        start.addActionListener(action -> {
            frame.remove(this);
            frame.add(new MapGui(frame));
            frame.repaint();
        });

        add(start);
        repaint();
    }
}
