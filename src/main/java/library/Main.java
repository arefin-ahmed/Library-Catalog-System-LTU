package library;

import javax.swing.SwingUtilities;

import library.core.LibrarySystemGUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibrarySystemGUI gui = new LibrarySystemGUI();
            gui.setVisible(true);
        });
    } 
}
