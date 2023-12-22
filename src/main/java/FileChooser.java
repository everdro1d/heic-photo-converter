package main.java;

import javax.swing.*;
import java.awt.*;

import static main.java.MainWindow.fontName;

public class FileChooser extends JFileChooser {
    // create a file chooser that allows the user to select a directory or image file
    // and set the current directory to the specified path
    public FileChooser(String path) {
        super();
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        setAcceptAllFileFilterUsed(false);
        setDialogTitle("Select a folder or '.heic' file:");
        setApproveButtonText("Select");
        setMultiSelectionEnabled(false);
        setCurrentDirectory(new java.io.File(path.isEmpty() ? System.getProperty("user.home") : path));
        setFileHidingEnabled(true);
        setPreferredSize(new java.awt.Dimension(700, 550));



        // set the font of the file chooser to the same font as the rest of the program
        setFileChooserFont(this.getComponents());

        // set the file filter to only allow directories and '.heic' files
        setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".heic");
            }

            @Override
            public String getDescription() {
                return "HEIC Files (*.heic) or Directories (Folders)";
            }
        });
    }

    private void setFileChooserFont(Component[] comp) {
        for (Component component : comp) {
            if (component instanceof Container) setFileChooserFont(((Container) component).getComponents());
            try {
                component.setFont(new Font(fontName, Font.PLAIN, 16));
            } catch (Exception ignored) {}
        }
    }
}
