/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ZipTask extends GuiTask {
    protected final String zipPath;
    protected final Map<String, String> contents;

    public ZipTask(StackPane root, GuiTaskStyle style, String zipPath, Map<String, String> contents) {
        super(root, style);
        this.zipPath = zipPath;
        this.contents = contents;
    }

    public ZipTask(StackPane root, GuiTaskStyle style, String zipPath, List<String> contents) {
        super(root, style);
        this.zipPath = zipPath;
        this.contents = contents.stream()
                .collect(Collectors.toMap(
                        path -> path,
                        path -> Paths.get(path).getFileName().toString()
                ));
    }

    @Override
    protected String getHeader() {
        return "Creating compressed file...";
    }

    @Override
    protected String getInitialContent() {
        return "This may take some time";
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Logger.info("Zip", "Zipping " + contents.size() + " entries into " + zipPath);
                IOUtils.FileUtil.delete(new File(zipPath), false);
                IOUtils.ZipUtil.zip(zipPath, contents, false);
                Logger.info("Zip", "Zipped into " + zipPath + " , finished");
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.DialogUtil.createErrorDialog(root, e).show();
    }
}
