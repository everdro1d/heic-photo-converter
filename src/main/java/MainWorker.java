package main.java;

import com.formdev.flatlaf.FlatLightLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static main.java.MainWindow.*;

public class MainWorker {
    public final static String versionTag = "v1.0.0";
    private static String jarPath;
    private static final String fileDiv = File.separator;
    public static boolean debug = false;
    protected static String format = "jpg";
    protected static String selectedFilePath = "";
    private static String heifConvertPath;
    protected static boolean deleteOriginal = false;
    public static final Preferences prefs = Preferences.userNodeForPackage(MainWorker.class);

    public static void main(String[] args) {
        checkOSCompatability();
        getJarPath();

        Icon frameIcon = getApplicationIcon("main/resources/convertIcon.png", 32, 32);

        heifConvertPath = jarPath + fileDiv + "heif-convert.exe";
        copyBinaryTempFile();

        FlatLightLaf.setup();

        setUI();

        prefs();

        EventQueue.invokeLater(() -> {
            try {
                new MainWindow();

                if (frameIcon != null) {
                    frame.setIconImage(((ImageIcon) frameIcon).getImage());
                } else {
                    System.err.println("Failed to set icon.");
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.err.println("Failed to start main window.");
            }
        });
    }

    private static void setUI() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("RootPane.background", new Color(0xe1e1e1));
            UIManager.put("RootPane.foreground", new Color(0x000000));

            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Separator.stripeWidth", 10);
            UIManager.put("RootPane.background", new Color(0xe1e1e1));
            UIManager.put("RootPane.foreground", new Color(0x000000));

            UIManager.put("OptionPane.minimumSize",new Dimension(300, 100));
            UIManager.put("OptionPane.messageFont", new Font(fontName, Font.PLAIN, 14));
            UIManager.put("OptionPane.buttonFont", new Font(fontName, Font.PLAIN, 16));

            UIManager.put("FileChooser.noPlacesBar", Boolean.TRUE);
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void checkOSCompatability() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("win")) {
            if (debug) System.err.println("This program is not compatible with your operating system.");
            JOptionPane.showMessageDialog(null, "This program is not compatible with your operating system.", "Error!", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static void getJarPath() {
        try {
            jarPath = Paths.get(MainWorker.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().toString();
            if (debug) System.out.println("Jar Path: " + jarPath);
        } catch (URISyntaxException e) {
            e.printStackTrace(System.err);
            System.err.println("[ERROR] Failed to get jar path.");
        }
    }

    private static void prefs() {
        format = prefs.get("format", "jpg");
        selectedFilePath = prefs.get("selectedFilePath", System.getProperty("user.home"));
        deleteOriginal = prefs.getBoolean("deleteOriginal", false);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            prefs.put("format", format);
            prefs.put("selectedFilePath", selectedFilePath);
            prefs.putBoolean("deleteOriginal", deleteOriginal);
        }));
    }

    private static void copyBinaryTempFile() {
        try (InputStream binaryPathStream = MainWorker.class.getClassLoader().getResourceAsStream("main/libs/heif-convert.exe")) {
            if (binaryPathStream == null) {
                System.err.println("Could not find binary file: heif-convert.exe");
                return;
            }
            Path outputPath = new File(heifConvertPath).toPath();

            Files.copy(binaryPathStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
            Files.setAttribute(outputPath, "dos:hidden", true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteFile(heifConvertPath) ));

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void deleteFile(String path) {
        File fileToDelete = new File(path);
        String name = fileToDelete.getName();
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                if (debug) System.out.println("Deleted file: " + name);
            } else {
                System.err.println("Failed to delete file: " + name);
            }
        }
    }

    private static int totalFiles;
    private static int currentFile;
    protected static void convertImage() {
        convertButton.setEnabled(false);

        // show warning confirmation dialog
        if (deleteOriginal) {
            int dialogResult = JOptionPane.showConfirmDialog(frame, "Deleting original files is enabled. \nAre you sure you want to continue?", "Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (dialogResult == JOptionPane.NO_OPTION) {
                if (debug) System.out.println("User cancelled conversion.");
                return;
            }
        }

        // check if file or directory is selected
        if (selectedFilePath.isEmpty()) {
            selectedFilePath = "";
            MainWindow.convertButton.setEnabled(false);
            JOptionPane.showMessageDialog(frame, "Please select a file or directory.", "Error!", JOptionPane.ERROR_MESSAGE);
            convertButton.setEnabled(true);
            return;
        }

        // single file conversion
        if (selectedFilePath.endsWith(".heic")) {
            EventQueue.invokeLater(() -> {
                progressBar.setVisible(true);
                progressBar.setMaximum(100);
                progressBar.setValue(99);
                progressBar.setIndeterminate(false);
                progressBar.setString("Converting 1 of 1 file...");
            });

            convertProcess(selectedFilePath, 1, 1);

            if (deleteOriginal) {
                deleteFile(selectedFilePath);
            }

            convertButton.setEnabled(true);
            return;
        }

        // directory conversion
        Set<String> heicFiles = getHeicFiles(selectedFilePath);
        if (heicFiles == null) {
            JOptionPane.showMessageDialog(frame, "No HEIC files found in directory.", "Error!", JOptionPane.ERROR_MESSAGE);
            if (debug) System.err.println("No HEIC files found in directory: " + selectedFilePath);
            convertButton.setEnabled(true);
            return;
        }

        new Thread(() -> {
            totalFiles = heicFiles.size();
            currentFile = 0;

            progressBar.setVisible(true);
            progressBar.setMaximum(totalFiles);
            progressBar.setValue(0);
            progressBar.setIndeterminate(false);

            for (String heicFile : heicFiles) {
                String filePath = selectedFilePath + fileDiv + heicFile;
                convertProcess(filePath, ++currentFile, totalFiles);

                progressBar.setValue(currentFile);
                progressBar.setString("Converting file: " + currentFile + " of " + totalFiles + " files...");

                if (deleteOriginal) {
                    deleteFile(filePath);
                }
            }
            convertButton.setEnabled(true);
        }).start();
    }

    private static Set<String> getHeicFiles(String inputDirectory) {
        // get list of all heic files in input directory
        Set<String> files = Stream.of(Objects.requireNonNull(new File(inputDirectory).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());

        if (debug) System.out.println("All Files: \n" + files);

        // filter out non-heic files
        Set<String> heicFiles = new HashSet<>();
        for (String file : files) {
            if (file.endsWith(".heic")) {
                heicFiles.add(file);
            }
        }

        if (heicFiles.isEmpty()) {
            return null;
        }

        if (debug) System.out.println("HEIC Files: \n" + heicFiles);
        return heicFiles;
    }

    private static void convertProcess(String filePath, int currentIndex, int totalIndex) {
        String[] cmd = {heifConvertPath, filePath, "-f", format};
        if (debug) System.out.println("Running command: " + String.join(" ", cmd));

        String outputPath = filePath.substring(0, filePath.lastIndexOf(".")) + "." + format;
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            if (debug) System.out.println("File already exists, skipping: " + outputPath);

            totalFiles--;
            currentFile--;

            if (currentIndex == totalIndex) {
                if (currentFile <= 0 && totalFiles <= 0) {
                    EventQueue.invokeLater(() -> progressBar.setVisible(false));

                    JOptionPane.showMessageDialog(frame, "File(s) already exist! \nNo file(s) were converted.", "Error!", JOptionPane.ERROR_MESSAGE);
                } else {
                    showFinishedDialog(totalIndex);
                }
            }
            return;
        }

        if (debug) System.out.println("Converting file: " + filePath);

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            Scanner s = new Scanner(p.getInputStream());
            while (s.hasNextLine()) {
                if (s.nextLine().contains("Wrote")) {
                    if (debug) System.out.println("Wrote file: " + outputPath);
                    if (currentIndex == totalIndex) {
                        showFinishedDialog(totalIndex);
                    } else {
                        if (debug) System.out.println("Finished converting file " + currentIndex + " of " + totalIndex);
                    }
                } else {
                    System.err.println("Something went wrong: \n" + s.nextLine());
                }
            }
            p.waitFor();
            if (debug) System.out.println("Exit value: " + p.exitValue());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void showFinishedDialog(int totalIndex) {
        EventQueue.invokeLater(() -> { // dialog is modal but is called from a separate thread
            progressBar.setVisible(false);
            JOptionPane.showMessageDialog(frame, "Finished converting " + totalIndex + " file(s).", "Finished!", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    protected static Icon getApplicationIcon(String internalPath, int width, int height) {
        Icon icon = null;
        try (InputStream iconStream = MainWorker.class.getClassLoader().getResourceAsStream(internalPath)) {
            if (iconStream != null) {
                // set scale as well as icon
                icon = new ImageIcon(ImageIO.read(iconStream).getScaledInstance(width, height, Image.SCALE_SMOOTH));

            }
        } catch (Exception e) {
            if (debug) e.printStackTrace(System.err);
        }
        if (icon == null) {
            System.err.println("[ERROR] Could not find icon file at: " + internalPath);
        }
        return icon;
    }
}
