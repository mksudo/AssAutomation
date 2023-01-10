package org.sudo.tools;

import org.sudo.tools.ass.AssWriter;
import org.sudo.tools.models.VideoSection;
import org.sudo.tools.opencv.OpenCVDetector;
import org.sudo.tools.scene.SceneReader;
import org.sudo.tools.utils.LogArea;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class GUIProvider {
    protected static final String TITLE = "ASS_AUTOMATION";
    protected static final String VIDEO_FOLDER_USER_PREFERENCE = "ASS_AUTOMATION_VIDEO_FOLDER_USER_PREFERENCE";
    protected static final String JSON_FOLDER_USER_PREFERENCE = "ASS_AUTOMATION_JSON_FOLDER_USER_PREFERENCE";

    private final JFrame frame;
    private final JScrollPane scrollPane;
    private final JPanel panel;
    private final JTextArea textArea;
    private final JCheckBox useKeyFrameDetector;

    private final JButton videoSelectButton;
    private final JFileChooser videoChooser;
    private File videoFile;
    private final JButton jsonSelectButton;
    private final JFileChooser jsonChooser;
    private File jsonFile;

    private final JButton runButton;
    private ActionListener runButtonActionListener;

    private final List<LogArea> logAreas;
    private LogArea taskLogArea;

    private final SceneReader sceneReader;
    private final OpenCVDetector detector;
    private boolean isTaskRunning;

    public GUIProvider() {
        this.frame = new JFrame(TITLE);
        this.frame.setSize(800, 600);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.panel = new JPanel();

        this.textArea = new JTextArea(20, 16);
        this.textArea.setEditable(false);
        this.useKeyFrameDetector = new JCheckBox("Use Key Frame Detector");
        this.useKeyFrameDetector.setSelected(true);
        this.useKeyFrameDetector.setEnabled(false);

        this.videoSelectButton = new JButton("Select video");
        this.videoChooser = new JFileChooser();
        setFileChooserFolder(this.videoChooser, VIDEO_FOLDER_USER_PREFERENCE);
        setFileChooserFilter(
                this.videoChooser,
                new FileNameExtensionFilter("MP4 File", "mp4")
        );
        this.jsonSelectButton = new JButton("Select json file");
        this.jsonChooser = new JFileChooser();
        setFileChooserFolder(this.jsonChooser, JSON_FOLDER_USER_PREFERENCE);
        setFileChooserFilter(
                this.jsonChooser,
                new FileNameExtensionFilter("JSON File", "json")
        );

        this.runButton = new JButton("Run");
        this.runButtonActionListener = null;

        this.scrollPane = new JScrollPane(this.textArea);

        initializeSelectButton();
        initializeLayout();

        this.logAreas = new ArrayList<>();
        // add one mutable log area here
        this.logAreas.add(new LogArea());

        this.sceneReader = new SceneReader();
        this.detector = new OpenCVDetector();

        this.isTaskRunning = false;

        this.checkConfig();
    }

    private void checkConfig() {
        ConfigProvider configProvider = ConfigProvider.getInstance();

        if (configProvider.getUserConfig() == null) {
            this.addLogToLogArea("User config load failed!");
        }
        if (configProvider.getResolutionMaskConfigs() == null) {
            this.addLogToLogArea("Resolution mask config load failed!");
        } else if (configProvider.getResolutionMaskConfigs().size() == 0) {
            this.addLogToLogArea("No resolution mask config found!");
        }
        if (configProvider.getCharacterStyleConfig() == null) {
            this.addLogToLogArea("Character style config load failed!");
        }
    }

    private void initializeLayout() {
        this.panel.setLayout(new GridLayout(3, 1));
        this.panel.add(this.videoSelectButton);
        this.panel.add(this.jsonSelectButton);
        this.panel.add(this.useKeyFrameDetector);

        this.frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        this.frame.getContentPane().add(BorderLayout.EAST, panel);

        this.frame.setVisible(true);
    }

    private void setTaskLogArea(int size) {
        this.taskLogArea = new LogArea(size);
        for (var logArea : this.logAreas) {
            logArea.freeze();
        }
        this.logAreas.add(this.taskLogArea);
        this.logAreas.add(new LogArea());
    }

    private void runOpenCVDetect() {
        try {
            this.addLogToLogArea("Opencv detector initializing ...");

            this.detector.setTextSections(
                    this.sceneReader.getNoNewLineTextSections()
            );

            this.detector.setUseKeyframeDetector(this.useKeyFrameDetector.isSelected());

            this.addLogToLogArea("Opencv task preparing ...");

            this.detector.prepareTasks(this.videoFile);

            var detectors = this.detector.getDetectors();

            this.addLogToLogArea(
                    "Use key frame detector: " +
                            this.useKeyFrameDetector.isSelected() +
                            ", detector size " +
                            detectors.size()
            );

            detectors.forEach(videoSectionDetector -> {
                videoSectionDetector.getTaskProgress().addProgressUpdateListener(event -> {
                    String progress = (String) event.getNewValue();
                    this.taskLogArea.addLogFixed(progress, videoSectionDetector.getVideoSectionIndex());
                    this.updateLogArea();
                });
            });

            this.addLogToLogArea("Opencv task starting...");

            this.detector.setTaskFinishCallback(() -> {
                this.addLogToLogArea("Opencv tasks finished");
                this.addLogToLogArea(
                        "Result sections:\n" +
                                this.detector
                                        .getVideoSections()
                                        .stream()
                                        .map(VideoSection::toString)
                                        .collect(Collectors.joining("\n"))
                );
                this.writeAssFile();

                this.isTaskRunning = false;
                this.runButton.removeActionListener(this.runButtonActionListener);
                this.videoFile = null;
                this.jsonFile = null;
                this.videoChooser.setSelectedFile(null);
                this.videoSelectButton.setText("Select video");
                this.jsonChooser.setSelectedFile(null);
                this.jsonSelectButton.setText("Select json file");
            });

            this.setTaskLogArea(detectors.size());

            this.detector.start();
        } catch (Exception exception) {
            this.addLogToLogArea(exception.getMessage());
        }
    }

    private void runSceneReader() {
        try {
            this.sceneReader.read(this.jsonFile);
            this.addLogToLogArea("Read finished, found " + this.sceneReader.getTextSections().size() + " text sections");
        } catch (IOException exception) {
            this.addLogToLogArea("IOException while reading scene data: " + exception.getMessage());
        }
    }

    private void writeAssFile() {
        AssWriter assWriter = new AssWriter(
                this.videoFile,
                this.sceneReader.getEventData(),
                this.sceneReader.getTextSections(),
                this.detector.getVideoSections(),
                this.detector.getVideoWidth(),
                this.detector.getVideoHeight()
        );

        this.addLogToLogArea("ass writer analysing...");

        assWriter.analyse();

        this.addLogToLogArea("ass writer writing...");

        try {
            assWriter.write();

            this.addLogToLogArea("ass writer complete");
        } catch (IOException exception) {
            this.addLogToLogArea("IOException while writing ass file: " + exception.getMessage());
        }
    }

    private void handleSubmitFile() {
        if (this.videoFile == null || this.jsonFile == null) return;

        this.panel.setLayout(new GridLayout(4, 1));
        panel.add(this.runButton, 1);

        if (this.runButtonActionListener == null) {
            this.runButtonActionListener = action -> {
                if (this.videoFile == null || this.jsonFile == null) return;

                if (this.isTaskRunning) {
                    addLogToLogArea("Task is still running, please wait until it finishes");
                    return;
                }


                this.isTaskRunning = true;

                this.runSceneReader();
                this.runOpenCVDetect();
            };
        }

        this.runButton.addActionListener(this.runButtonActionListener);
    }

    private void initializeSelectButton() {
        this.videoSelectButton.addActionListener(action -> {
            if (this.videoChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
                return;

            if (this.isTaskRunning) {
                addLogToLogArea("Task is still running, please wait until it finishes");
                return;
            }

            this.videoFile = this.videoChooser.getSelectedFile();
            this.videoSelectButton.setText(this.videoFile.getName());
            Preferences.userRoot().node(TITLE).put(VIDEO_FOLDER_USER_PREFERENCE, this.videoFile.getAbsolutePath());

            this.addLogToLogArea("Selected video file: " + this.videoFile.getAbsolutePath());

            this.handleSubmitFile();
        });

        this.jsonSelectButton.addActionListener(action -> {
            if (this.jsonChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
                return;

            if (this.isTaskRunning) {
                addLogToLogArea("Task is still running, please wait until it finishes");
                return;
            }

            this.jsonFile = this.jsonChooser.getSelectedFile();
            this.jsonSelectButton.setText(this.jsonFile.getName());
            Preferences.userRoot().node(TITLE).put(JSON_FOLDER_USER_PREFERENCE, this.jsonFile.getAbsolutePath());

            this.addLogToLogArea("Selected json file: " + this.jsonFile.getAbsolutePath());

            this.handleSubmitFile();
        });
    }

    private LogArea getCurrentLogArea() {
        return this.logAreas.get(this.logAreas.size() - 1);
    }

    private synchronized void updateLogAreaSync() {
        this.textArea.setText(
                this.logAreas
                        .stream()
                        .map(LogArea::getLog)
                        .filter(Predicate.not(String::isBlank))
                        .collect(Collectors.joining("\n"))
        );
    }

    private void updateLogAreaNotSync() {
        this.textArea.setText(
                this.logAreas
                        .stream()
                        .map(LogArea::getLog)
                        .filter(Predicate.not(String::isBlank))
                        .collect(Collectors.joining("\n"))
        );
    }

    private void updateLogArea() {
        if (this.detector.isUseKeyframeDetector()) {
            this.updateLogAreaNotSync();
        } else {
            this.updateLogAreaSync();
        }
    }

    private void addLogToLogArea(String log) {
        this.getCurrentLogArea().addLog(log);
        this.updateLogArea();
    }

    private static void setFileChooserFilter(
            JFileChooser fileChooser,
            FileNameExtensionFilter fileNameExtensionFilter
    ) {
        fileChooser.setFileFilter(fileNameExtensionFilter);
    }

    private static void setFileChooserFolder(
            JFileChooser fileChooser,
            String preferenceKey
    ) {
        String preferenceDirectory = Preferences
                .userRoot()
                .node(TITLE)
                .get(
                        preferenceKey,
                        Path
                                .of("")
                                .toAbsolutePath()
                                .toString()
                );
        fileChooser.setCurrentDirectory(
                fileChooser.getFileSystemView().createFileObject(preferenceDirectory)
        );
    }
}
