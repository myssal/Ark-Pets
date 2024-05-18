/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.PathConfig;
import static cn.harryh.arkpets.Const.charsetDefault;


public class  CheckModelUpdateTask extends FetchGitHubRemoteTask {
    public CheckModelUpdateTask(StackPane root, GuiTaskStyle style) {
        super(
                root,
                style,
                PathConfig.urlModelsData,
                PathConfig.tempDirPath + PathConfig.fileModelsDataPath,
                Const.isHttpsTrustAll,
                false);

        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (Exception e) {
            Logger.warn("Task", "Failed to create temp dir.");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getHeader() {
        return "Downloading module version information...";
    }

    @Override
    protected void onSucceeded(boolean result) {
        // When finished downloading the remote repo models info:
        try {
            String versionDescription;
            try {
                // Try to parse the remote repo models info
                JSONObject newModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath), charsetDefault));
                versionDescription = newModelsDataset.getString("gameDataVersionDescription");
            } catch (Exception e) {
                // When failed to parse the remote repo models info
                versionDescription = "unknown";
                Logger.error("Checker", "Unable to parse remote model repo version, details see below.", e);
                GuiPrefabs.DialogUtil.createCommonDialog(root,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                        "Check for module updates",
                        "Unable to determine the module warehouse version.",
                        "Unable to resolve the version of the remote module repository due to an error.",
                        null).show();
            }
            // When finished parsing the remote models info:
            // TODO do judgment more precisely
            // Compare the remote models info and the local models info by their MD5

            if (IOUtils.FileUtil.getMD5(new File(PathConfig.fileModelsDataPath)).equals(IOUtils.FileUtil.getMD5(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath)))) {
                Logger.info("Checker", "Model repo version check finished (up-to-dated)");
                GuiPrefabs.DialogUtil.createCommonDialog(root,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_SUCCESS_ALT, GuiPrefabs.Colors.COLOR_SUCCESS),
                        "Check for module updates",
                        "No module library updates are required.",
                        "The version of the local module library is consistent with that of the remote module library.",
                        "Tip: The version of the remote module library may not be updated simultaneously with the official version of the game.\nModule library version description: \n" + versionDescription).show();
            } else {
                // If the result of comparison is "not the same"
                String oldVersionDescription;
                try {
                    // Try to parse the local repo models info
                    JSONObject oldModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault));
                    oldVersionDescription = oldModelsDataset.getString("gameDataVersionDescription");
                } catch (Exception e) {
                    // When failed to parse the remote local models info
                    oldVersionDescription = "unknown";
                    Logger.error("Checker", "Unable to parse local model repo version, details see below.", e);
                    GuiPrefabs.DialogUtil.createCommonDialog(root,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                            "Check for module updates",
                            "Unable to determine the module library version.",
                            "Unable to parse the version of the local module library due to an error.",
                            null).show();
                }
                GuiPrefabs.DialogUtil.createCommonDialog(root,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_INFO_ALT, GuiPrefabs.Colors.COLOR_INFO),
                        "Check for module updates",
                        "The module library seems to be updated!",
                        "You can [re-download] the module to update the module library version.",
                        "Remote module library version description:\n" + versionDescription + "\n\nLocal module library version description: \n" + oldVersionDescription).show();
                Logger.info("Checker", "Model repo version check finished (not up-to-dated)");
            }
        } catch (IOException e) {
            Logger.error("Checker", "Model repo version check failed unexpectedly, details see below.", e);
            if (style != GuiTaskStyle.HIDDEN)
                GuiPrefabs.DialogUtil.createErrorDialog(root, e).show();
        }
    }
}
