package library.core;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibrarySystemGUI gui = new LibrarySystemGUI();
            gui.setVisible(true);
        });
    }
}