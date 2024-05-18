/** Copyright (c) 2022-2024, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.concurrent.SocketData;

import javax.swing.*;
import java.util.UUID;


public abstract class MemberTray {
    protected JMenuItem optKeepAnimEn       = new JMenuItem("Keep moving");
    protected JMenuItem optKeepAnimDis      = new JMenuItem("Cancel hold");
    protected JMenuItem optTransparentEn    = new JMenuItem("Transparent mode");
    protected JMenuItem optTransparentDis   = new JMenuItem("Cancel transparency");
    protected JMenuItem optChangeStage      = new JMenuItem("Switch form");
    protected JMenuItem optExit             = new JMenuItem("Quit");
    protected final UUID uuid;
    protected final String name;

    static {
        Const.FontsConfig.loadFontsToSwing();
    }

    /** Initializes a tray icon instance for a ArkPets.
     * @param name The name to be displayed in the menu, in the icon tooltip, etc.
     */
    public MemberTray(String name) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        
        optKeepAnimEn       .addActionListener(e -> onKeepAnimEn());
        optKeepAnimDis      .addActionListener(e -> onKeepAnimDis());
        optTransparentEn    .addActionListener(e -> onTransparentEn());
        optTransparentDis   .addActionListener(e -> onTransparentDis());
        optChangeStage      .addActionListener(e -> onChangeStage());
        optExit             .addActionListener(e -> onExit());

        optKeepAnimEn       .addActionListener(e -> sendOperation(SocketData.Operation.KEEP_ACTION));
        optKeepAnimDis      .addActionListener(e -> sendOperation(SocketData.Operation.NO_KEEP_ACTION));
        optTransparentEn    .addActionListener(e -> sendOperation(SocketData.Operation.TRANSPARENT_MODE));
        optTransparentDis   .addActionListener(e -> sendOperation(SocketData.Operation.NO_TRANSPARENT_MODE));
        optChangeStage      .addActionListener(e -> sendOperation(SocketData.Operation.CHANGE_STAGE));
        optExit             .addActionListener(e -> sendOperation(SocketData.Operation.LOGOUT));
    }

    abstract public void onExit();

    abstract public void onChangeStage();

    abstract public void onTransparentDis();

    abstract public void onTransparentEn();

    abstract public void onKeepAnimDis();

    abstract public void onKeepAnimEn();

    abstract public void remove();

    abstract public void sendOperation(SocketData.Operation operation);
}
