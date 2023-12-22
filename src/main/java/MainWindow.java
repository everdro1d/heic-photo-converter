package main.java;

import javax.swing.*;
import java.awt.*;

import static main.java.MainWorker.*;

@SuppressWarnings("FieldCanBeLocal")
public class MainWindow extends JFrame {
    protected static JFrame frame;
        private JPanel mainPanel;
            private JPanel northPanel;
                private JLabel titleLabel;
                private JLabel versionLabel;
                private JSeparator northSeparator;
        private JPanel centerPanel;
            private JPanel centerPanelRow1;
                private JButton selectFileButton;
                private JComboBox<String> formatComboBox;
            private JPanel centerPanelRow2;
                private JScrollPane scrollPane;
                    protected static JLabel selectedFileLabel;
                private JCheckBox deleteOriginalCheckBox;
        private JPanel southPanel;
                private JSeparator southSeparator;
                protected static JButton convertButton;
                protected static JProgressBar progressBar;

    public static final String fontName = "Tahoma";
    public static final int fontSize = 20;

    public MainWindow() {
        initializeWindowProperties();

        initializeGUIComponents();

        frame.setVisible(true);
    }

    private void initializeWindowProperties() {
        frame = new JFrame("HEIF Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }

    private void initializeGUIComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        frame.add(mainPanel);
        {
            northPanel = new JPanel();
            northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
            mainPanel.add(northPanel, BorderLayout.NORTH);
            {
                titleLabel = new JLabel("HEIF Converter");
                titleLabel.setFont(new Font(fontName, Font.BOLD, fontSize+6));
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                northPanel.add(titleLabel);

                versionLabel = new JLabel(versionTag);
                versionLabel.setFont(new Font(fontName, Font.PLAIN, fontSize-4));
                versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                northPanel.add(versionLabel);

                northSeparator = new JSeparator();
                northSeparator.setMaximumSize(new Dimension(350, 10));
                northPanel.add(northSeparator);

                northPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            }

            centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            mainPanel.add(centerPanel, BorderLayout.CENTER);
            {
                centerPanelRow1 = new JPanel();
                centerPanelRow1.setLayout(new FlowLayout());
                centerPanelRow1.setMaximumSize(new Dimension(400, 40));
                centerPanel.add(centerPanelRow1);
                {
                    selectFileButton = new JButton("Select File");
                    selectFileButton.setFont(new Font(fontName, Font.PLAIN, fontSize));
                    selectFileButton.setIcon(getApplicationIcon("images/folderIcon.png", 24, 24));
                    centerPanelRow1.add(selectFileButton);

                    selectedFilePath = prefs.get("selectedFilePath", System.getProperty("user.home"));

                    selectFileButton.addActionListener(e -> {
                        // create file chooser
                        FileChooser fileChooser = new FileChooser(selectedFilePath);

                        int returnValue = fileChooser.showOpenDialog(frame);
                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                            selectedFileLabel.setText(selectedFilePath);
                            convertButton.setEnabled(true);
                        }
                    });

                    final String[] formats = {"jpg", "png", "webp", "gif", "tiff", "bmp", "ico"};
                    formatComboBox = new JComboBox<>(formats);
                    formatComboBox.setFont(new Font(fontName, Font.PLAIN, fontSize));
                    formatComboBox.setSelectedItem(format);
                    centerPanelRow1.add(formatComboBox);

                    formatComboBox.addActionListener(e -> format = formats[formatComboBox.getSelectedIndex()]);
                }

                centerPanelRow2 = new JPanel();
                centerPanelRow2.setLayout(new BoxLayout(centerPanelRow2, BoxLayout.Y_AXIS));
                centerPanelRow2.setMaximumSize(new Dimension(400, 80));
                centerPanel.add(centerPanelRow2);
                {
                    selectedFileLabel = new JLabel(selectedFilePath.isEmpty() ? "No file selected" : selectedFilePath);
                    selectedFileLabel.setFont(new Font(fontName, Font.PLAIN, fontSize-4));
                    selectedFileLabel.setHorizontalAlignment(JLabel.CENTER);

                    scrollPane = new JScrollPane(selectedFileLabel);
                    scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
                    scrollPane.setMaximumSize(new Dimension(350, 30));
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    scrollPane.setBorder(BorderFactory.createEmptyBorder());
                    centerPanelRow2.add(scrollPane);


                    centerPanelRow2.add(Box.createRigidArea(new Dimension(0, 10)));


                    deleteOriginalCheckBox = new JCheckBox("Delete Original File(s)");
                    deleteOriginalCheckBox.setFont(new Font(fontName, Font.PLAIN, fontSize-2));
                    deleteOriginalCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
                    deleteOriginalCheckBox.setSelected(deleteOriginal);
                    centerPanelRow2.add(deleteOriginalCheckBox);

                    deleteOriginalCheckBox.addActionListener(e -> MainWorker.deleteOriginal = deleteOriginalCheckBox.isSelected());

                }
            }

            southPanel = new JPanel();
            southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
            southPanel.setSize(new Dimension(400, 150));
            mainPanel.add(southPanel, BorderLayout.SOUTH);
            {
                southSeparator = new JSeparator();
                southSeparator.setMaximumSize(new Dimension(350, 10));
                southPanel.add(southSeparator);

                southPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                progressBar.setVisible(false);
                southPanel.add(progressBar);

                progressBar.setStringPainted(true);
                progressBar.setString("Converting...");
                progressBar.setFont(new Font(fontName, Font.PLAIN, fontSize-2));
                progressBar.setMaximumSize(new Dimension(350, 30));


                southPanel.add(Box.createRigidArea(new Dimension(0, 10)));


                convertButton = new JButton("Convert");
                convertButton.setFont(new Font(fontName, Font.PLAIN, fontSize));
                convertButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                convertButton.setEnabled(!selectedFilePath.isEmpty());
                convertButton.setIcon(getApplicationIcon("images/convertIcon.png", 24, 24));
                southPanel.add(convertButton);

                convertButton.addActionListener(e -> convertImage());

                southPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
    }
}
