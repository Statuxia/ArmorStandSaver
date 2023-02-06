package me.statuxia.armorstandsaver;

import org.bukkit.plugin.java.JavaPlugin;

public final class ArmorStandSaver extends JavaPlugin {

    private static ArmorStandSaver INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ArmorStandSaver getInstance() {
        return INSTANCE;
    }
}
