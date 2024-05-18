/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.guitasks.CheckAppUpdateTask;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.utils.*;
import cn.harryh.arkpets.utils.GuiComponents.*;
import com.jfoenix.controls.*;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static cn.harryh.arkpets.Const.*;


public final class SettingsModule implements Controller<ArkHomeFX> {
    @FXML
    private Pane noticeBox;
    @FXML
    private JFXComboBox<NamedItem<Float>> configDisplayScale;
    @FXML
    private JFXComboBox<NamedItem<Integer>> configDisplayFps;
    @FXML
    private JFXComboBox<NamedItem<Integer>> configCanvasSize;
    @FXML
    private JFXCheckBox configWindowTopmost;
    @FXML
    private JFXComboBox<String> configLoggingLevel;
    @FXML
    private Label exploreLogDir;
    @FXML
    private JFXTextField configNetworkAgent;
    @FXML
    private Label configNetworkAgentStatus;
    @FXML
    private JFXCheckBox configAutoStartup;
    @FXML
    private JFXCheckBox configSolidExit;
    @FXML
    private JFXButton configCanvasSizeHelp;
    @FXML
    private JFXCheckBox configWindowToolwindow;
    @FXML
    private JFXButton configWindowToolwindowHelp;
    @FXML
    private Label aboutQueryUpdate;
    @FXML
    private Label aboutVisitWebsite;
    @FXML
    private Label aboutReadme;
    @FXML
    private Label aboutGitHub;

    private NoticeBar appVersionNotice;
    private NoticeBar diskFreeSpaceNotice;
    private NoticeBar fpsUnreachableNotice;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        initNoticeBox();
        initConfigDisplay();
        initConfigAdvanced();
        initAbout();
        initScheduledListener();
    }

    private void initConfigDisplay() {
        new ComboBoxSetup<>(configDisplayScale).setItems(new NamedItem<>("x0.5", 0.5f),
                new NamedItem<>("x0.75", 0.75f),
                new NamedItem<>("x1.0", 1f),
                new NamedItem<>("x1.25", 1.25f),
                new NamedItem<>("x1.5", 1.5f),
                new NamedItem<>("x2.0", 2f),
                new NamedItem<>("x2.5", 2.5f),
                new NamedItem<>("x3.0", 3.0f))
                .selectValue(app.config.display_scale, "x" + app.config.display_scale + "（customize）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.display_scale = newValue.value();
                    app.config.save();
                });
        new ComboBoxSetup<>(configDisplayFps).setItems(new NamedItem<>("25", 25),
                new NamedItem<>("30", 30),
                new NamedItem<>("45", 45),
                new NamedItem<>("60", 60),
                new NamedItem<>("120", 120))
                .selectValue(app.config.display_fps, app.config.display_fps + "（customize）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.display_fps = newValue.value();
                    app.config.save();
                    fpsUnreachableNotice.refresh();
                });
        new ComboBoxSetup<>(configCanvasSize).setItems(new NamedItem<>("widest", 4),
                new NamedItem<>("wider", 8),
                new NamedItem<>("standard", 16),
                new NamedItem<>("narrow", 32),
                new NamedItem<>("narrowest", 0))
                .selectValue(app.config.canvas_fitting_samples, "Every" + app.config.canvas_fitting_samples + "Frame sampling (custom)")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.canvas_fitting_samples = newValue.value();
                    app.config.save();
                });
        new HandbookEntrance(app.root, configCanvasSizeHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHandbook((Labeled)configCanvasSize.getParent().getChildrenUnmodifiable().get(0)) {
                    @Override
                    public String getContent() {
                        return "Sets the relative size of the desktop window's borders. \nWider borders prevent animations from overflowing;\nnarrower borders prevent accidental mouse clicks.";
                    }
                };
            }
        };

        configWindowTopmost.setSelected(app.config.window_style_topmost);
        configWindowTopmost.setOnAction(e -> {
            app.config.window_style_topmost = configWindowTopmost.isSelected();
            app.config.save();
        });
    }

    private void initConfigAdvanced() {
        configLoggingLevel.getItems().setAll(Const.LogConfig.debug, Const.LogConfig.info, Const.LogConfig.warn, Const.LogConfig.error);
        configLoggingLevel.valueProperty().addListener(observable -> {
            if (configLoggingLevel.getValue() != null) {
                Logger.setLevel(Level.toLevel(configLoggingLevel.getValue(), Level.INFO));
                app.config.logging_level = Logger.getLevel().toString();
                app.config.save();
            }
        });
        String level = app.config.logging_level;
        List<String> args = Arrays.asList(ArgPending.argCache);
        if (args.contains(Const.LogConfig.errorArg))
            level = Const.LogConfig.error;
        else if (args.contains(Const.LogConfig.warnArg))
            level = Const.LogConfig.warn;
        else if (args.contains(Const.LogConfig.infoArg))
            level = Const.LogConfig.info;
        else if (args.contains(Const.LogConfig.debugArg))
            level = Const.LogConfig.debug;
        configLoggingLevel.getSelectionModel().select(level);

        exploreLogDir.setOnMouseClicked(e -> {
            // Only available in Windows OS
            try {
                Logger.debug("Config", "Request to explore the log dir");
                Runtime.getRuntime().exec("explorer logs");
            } catch (IOException ex) {
                Logger.warn("Config", "Exploring log dir failed");
            }
        });

        configNetworkAgent.setPromptText("Example：0.0.0.0:0");
        configNetworkAgent.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                configNetworkAgentStatus.setText("No proxy used.");
                configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_LIGHT_GRAY);
                Logger.info("Network", "Set proxy to none");
                System.setProperty("http.proxyHost", "");
                System.setProperty("http.proxyPort", "");
                System.setProperty("https.proxyHost", "");
                System.setProperty("https.proxyPort", "");
            } else {
                if (newValue.matches(ipPortRegex)) {
                    String[] ipPort = newValue.split(":");
                    System.setProperty("http.proxyHost", ipPort[0]);
                    System.setProperty("http.proxyPort", ipPort[1]);
                    System.setProperty("https.proxyHost", ipPort[0]);
                    System.setProperty("https.proxyPort", ipPort[1]);
                    configNetworkAgentStatus.setText("The host is in effect");
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_SUCCESS);
                    Logger.info("Network", "Set proxy to host " + ipPort[0] + ", port " + ipPort[1]);
                } else {
                    configNetworkAgentStatus.setText("Illegal input");
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_DANGER);
                }
            }
        });
        configNetworkAgentStatus.setText("No proxy used");
        configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_LIGHT_GRAY);

        configAutoStartup.setSelected(ArkConfig.StartupConfig.isSetStartup());
        configAutoStartup.setOnAction(e -> {
            if (configAutoStartup.isSelected()) {
                if (ArkConfig.StartupConfig.addStartup()) {
                    GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_SUCCESS_ALT, GuiPrefabs.Colors.COLOR_SUCCESS),
                            "Start automatically at boot",
                            "The auto-start setting is successful.",
                            "The desktop pet you last started will be automatically\ngenerated the next time you start the computer.",
                            null).show();
                } else {
                    if (ArkConfig.StartupConfig.generateScript() == null)
                        GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                                "Start automatically at boot",
                                "Auto start setting failed.",
                                "The location of the target program cannot be confirmed.\nThe reasons and related solutions are as follows:",
                                "To ensure the stability of the self-starting service, directly open ArkPets.jar. The version of the launcher does not support configuring auto-start. Please use the exe version of the installation package to install ArkPets and run it, or use the zip version of the compressed package to decompress the program file and run it. Also, this can occur when you run the launcher with the wrong working directory.").show();
                    else
                        GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                                "Start automatically at boot",
                                "Auto start setting failed.",
                                "The system's startup directory cannot be written. The reason can be found in the log file.",
                                "This may be caused by insufficient permissions. Please try turning off anti-virus software and running the launcher with administrator rights.").show();
                    configAutoStartup.setSelected(false);
                }
            } else {
                ArkConfig.StartupConfig.removeStartup();
            }
        });

        configSolidExit.setSelected(app.config.launcher_solid_exit);
        configSolidExit.setOnAction(e -> {
            app.config.launcher_solid_exit = configSolidExit.isSelected();
            app.config.save();
        });

        configWindowToolwindow.setSelected(app.config.window_style_toolwindow);
        configWindowToolwindow.setOnAction(e -> {
            app.config.window_style_toolwindow = configWindowToolwindow.isSelected();
            app.config.save();
        });
        new HandbookEntrance(app.root, configWindowToolwindowHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHandbook(configWindowToolwindow) {
                    @Override
                    public String getContent() {
                        return "When enabled, Desktop Pet will be launched as a background tool program and will not display the program icon in the taskbar. When disabled, desktop pets launched as normal programs can be captured by live streaming software.";
                    }
                };
            }
        };
    }

    private void initAbout() {
        aboutQueryUpdate.setOnMouseClicked  (e -> {
            /* Foreground check app update */
            new CheckAppUpdateTask(app.root, GuiTask.GuiTaskStyle.COMMON, "manual").start();
        });
        aboutVisitWebsite.setOnMouseClicked (e -> NetUtils.browseWebpage(Const.PathConfig.urlOfficial));
        aboutReadme.setOnMouseClicked       (e -> NetUtils.browseWebpage(Const.PathConfig.urlReadme));
        aboutGitHub.setOnMouseClicked       (e -> NetUtils.browseWebpage(Const.PathConfig.urlLicense));
    }

    private void initNoticeBox() {
        appVersionNotice = new NoticeBar(noticeBox) {
            @Override
            protected boolean isToActivate() {
                return isUpdateAvailable;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.Colors.COLOR_INFO;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.ICON_UPDATE;
            }

            @Override
            protected String getText() {
                return "A new Ark-pets version is available! Click here to download~";
            }

            @Override
            protected void onClick(MouseEvent event) {
                NetUtils.browseWebpage(Const.PathConfig.urlDownload);
            }
        };
        diskFreeSpaceNotice = new NoticeBar(noticeBox) {
            @Override
            protected boolean isToActivate() {
                long freeSpace = new File(".").getFreeSpace();
                return freeSpace < diskFreeSpaceRecommended && freeSpace > 0;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.Colors.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "The current disk storage space is insufficient, which may affect the user experience.";
            }
        };
        fpsUnreachableNotice = new NoticeBar(noticeBox) {
            @Override
            protected boolean isToActivate() {
                for (ArkConfig.Monitor i : ArkConfig.Monitor.getMonitors())
                    if (i.hz >= configDisplayFps.getValue().value())
                        return false;
                return true;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.Colors.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "The currently set frame rate exceeds the current monitor's refresh rate.";
            }
        };
    }

    private void initScheduledListener() {
        ScheduledService<Boolean> ss = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return true;
                    }
                };
                task.setOnSucceeded(e -> {
                    appVersionNotice.refresh();
                    diskFreeSpaceNotice.refresh();
                });
                return task;
            }
        };
        ss.setDelay(new Duration(5000));
        ss.setPeriod(new Duration(5000));
        ss.setRestartOnFailure(true);
        ss.start();
    }


    abstract private static class ControlHandbook extends Handbook {
        private final Labeled control;

        public ControlHandbook(Labeled control) {
            super();
            this.control = control;
        }

        @Override
        public String getTitle() {
            return "Option description";
        }

        @Override
        public String getHeader() {
            return control.getText();
        }
    }
}
