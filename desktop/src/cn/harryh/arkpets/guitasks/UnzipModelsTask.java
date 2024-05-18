/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import javafx.scene.layout.StackPane;

import static cn.harryh.arkpets.Const.PathConfig;


public class UnzipModelsTask extends UnzipTask {
    public UnzipModelsTask(StackPane root, GuiTaskStyle style, String zipPath) {
        super(root, style, zipPath, PathConfig.tempModelsUnzipDirPath);
    }

    @Override
    protected String getHeader() {
        return "Unzipping model resource files...";
    }

    @Override
    protected String getInitialContent() {
        return "This may take several seconds";
    }
}
