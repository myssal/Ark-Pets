/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.Version;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import static cn.harryh.arkpets.Const.PathConfig;
import static cn.harryh.arkpets.Const.appVersion;


public class CheckAppUpdateTask extends FetchRemoteTask {
    public CheckAppUpdateTask(StackPane root, GuiTaskStyle style, String sourceStr) {
        super(root,
                style,
                PathConfig.urlApi + "?type=queryVersion&cliVer=" + appVersion + "&source=" + sourceStr,
                PathConfig.tempQueryVersionCachePath,
                Const.isHttpsTrustAll);

        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (Exception e) {
            Logger.warn("Task", "Failed to create temp dir.");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getHeader() {
        return "Downloading software version information...";
    }

    @Override
    protected void onSucceeded(boolean result) {
        // When finished downloading the latest app ver-info:
        try {
            // Try to parse the latest app ver-info
            JSONObject queryVersionResult = Objects.requireNonNull(JSONObject.parseObject(IOUtils.FileUtil.readByte(new File(PathConfig.tempQueryVersionCachePath))));
            // TODO show in-test version
            if (queryVersionResult.getString("msg").equals("success")) {
                // If the response status is "success":
                int[] stableVersionResult = queryVersionResult.getJSONObject("data").getObject("stableVersion", int[].class);
                Version stableVersion = new Version(stableVersionResult);
                if (appVersion.lessThan(stableVersion)) {
                    // On update is available:
                    Const.isUpdateAvailable = true;
                    if (style != GuiTaskStyle.HIDDEN)
                        GuiPrefabs.DialogUtil.createCommonDialog(root,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_INFO_ALT, GuiPrefabs.Colors.COLOR_INFO),
                                "Check for software updates",
                                "A new version of the software has been detected!",
                                "Current version " + appVersion + " can be updated to " + stableVersion + "\nPlease visit ArkPets official website or GitHub to download the new installation package. ",
                                null).show();
                } else {
                    // On up-to-dated:
                    Const.isUpdateAvailable = false;
                    if (style != GuiTaskStyle.HIDDEN)
                        GuiPrefabs.DialogUtil.createCommonDialog(root,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_SUCCESS_ALT, GuiPrefabs.Colors.COLOR_SUCCESS),
                                "Check for software updates",
                                "No new official version has been discovered yet.",
                                "Current version " + appVersion + " already the latest",
                                null).show();
                }
                Logger.info("Checker", "Application version check finished, newest: " + stableVersion);
            } else {
                // On API failed:
                Logger.warn("Checker", "Application version check failed (api failed)");
                if (style != GuiTaskStyle.HIDDEN)
                    GuiPrefabs.DialogUtil.createCommonDialog(root,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_DANGER_ALT, GuiPrefabs.Colors.COLOR_DANGER),
                            "Check for software updates",
                            "Invalid message returned.",
                            "It could be a compatibility issue or the server is unavailable.\nYou can visit the ArkPets official website or GitHub repository to check if there is a new version.",
                            null).show();
            }
        } catch (Exception e) {
            // On parsing failed:
            Logger.error("Checker", "Application version check failed unexpectedly, details see below.", e);
            if (style != GuiTaskStyle.HIDDEN)
                GuiPrefabs.DialogUtil.createErrorDialog(root, e).show();
        }
    }
}
