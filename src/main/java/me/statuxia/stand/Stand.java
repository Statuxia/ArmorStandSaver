package me.statuxia.stand;

import me.statuxia.armorstandsaver.ArmorStandSaver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Stand {

    /**
     * Loads an ArmorStand from a file.
     *
     * @param path     - file name (not path).
     * @param location - position the player wants to use.
     *
     * @return ArmorStand as entity or null.
     */
    @Nullable
    public static ArmorStand loadFromFile(@NotNull String path, @Nullable Location location) {
        File file = new File(ArmorStandSaver.getInstance().getDataFolder(), path);
        if (!file.exists()) {
            Bukkit.getConsoleSender().sendMessage("§cFile not exists");
            return null;
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        String standName = configuration.getString("standName");
        List<String> standSettings = configuration.getStringList("standSettings");
        List<String> standPoses = configuration.getStringList("standPoses");
        List<String> standDisabledSlots = configuration.getStringList("standDisabledSlots");

        if (location == null) {
            location = configuration.getLocation("standLocation");
        }
        if (location == null || location.getWorld() == null) {
            Bukkit.getConsoleSender().sendMessage("§cLocation is undefined");
            return null;
        }

        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        loadSettings(stand, standSettings);
        stand.setCustomName(standName);
        loadPoses(stand, standPoses);
        loadDisabledSlots(stand, standDisabledSlots);

        EntityEquipment equipment = stand.getEquipment();
        if (equipment == null) {
            Bukkit.getConsoleSender().sendMessage("§cStand equipment is null");
            return null;
        }
        loadEquipment(stand.getEquipment(), configuration);

        Bukkit.getConsoleSender().sendMessage("§aStand successfully loaded and spawned");
        return stand;
    }

    private static void loadSettings(@NotNull ArmorStand stand, @NotNull List<String> standSettings) {
        for (String rawSettings : standSettings) {
            String[] rawSettingsString = rawSettings.split(":");
            switch (StandSettings.valueOf(rawSettingsString[0])) {
                case NAME_VISIBLE -> stand.setCustomNameVisible(Boolean.parseBoolean(rawSettingsString[1]));
                case VISIBLE -> stand.setVisible(Boolean.parseBoolean(rawSettingsString[1]));
                case MARKER -> stand.setMarker(Boolean.parseBoolean(rawSettingsString[1]));
                case SMALL -> stand.setSmall(Boolean.parseBoolean(rawSettingsString[1]));
                case GRAVITY -> stand.setGravity(Boolean.parseBoolean(rawSettingsString[1]));
                case ARMS -> stand.setArms(Boolean.parseBoolean(rawSettingsString[1]));
                case BASE_PLATE -> stand.setBasePlate(Boolean.parseBoolean(rawSettingsString[1]));
                case GLOWING -> stand.setGlowing(Boolean.parseBoolean(rawSettingsString[1]));
                case COLLIDABLE -> stand.setCollidable(Boolean.parseBoolean(rawSettingsString[1]));
            }
        }
    }

    private static void loadPoses(@NotNull ArmorStand stand, @NotNull List<String> standPoses) {
        for (String rawPose : standPoses) {
            String[] rawPoseString = rawPose.split(":");
            String[] angles = rawPoseString[1].split(",");
            EulerAngle angle = new EulerAngle(
                    Double.parseDouble(angles[0]),
                    Double.parseDouble(angles[1]),
                    Double.parseDouble(angles[2]));
            switch (StandPose.valueOf(rawPoseString[0])) {
                case HEAD -> stand.setHeadPose(angle);
                case LEFT_ARM -> stand.setLeftArmPose(angle);
                case RIGHT_ARM -> stand.setRightArmPose(angle);
                case LEFT_LEG -> stand.setLeftLegPose(angle);
                case RIGHT_LEG -> stand.setRightLegPose(angle);
                case BODY -> stand.setBodyPose(angle);
            }
        }
    }

    private static void loadEquipment(@NotNull EntityEquipment equipment, @NotNull YamlConfiguration configuration) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack item = configuration.getItemStack(equipmentSlot.name());
            equipment.setItem(equipmentSlot, item);
        }
    }

    private static void loadDisabledSlots(ArmorStand stand, List<String> standDisabledSlots) {
        for (String rawDisabledSlot : standDisabledSlots) {
            String[] rawDisabledSlotString = rawDisabledSlot.split(":");
            String[] rawDisabledSlotTypeString = rawDisabledSlotString[1].split(",");
            if (Boolean.parseBoolean(rawDisabledSlotTypeString[1])) {
                stand.addEquipmentLock(EquipmentSlot.valueOf(rawDisabledSlotString[1]), ArmorStand.LockType.valueOf(rawDisabledSlotTypeString[0]));
            }
        }
    }

    /**
     * Saves ArmorStand to the file.
     *
     * @param stand - ArmorStand to save.
     *
     * @return true or false depending on whether the file was saved.
     */
    public static boolean saveToFile(@NotNull ArmorStand stand) {
        String defaultName = "stand";
        int count = 0;
        File file = new File(ArmorStandSaver.getInstance().getDataFolder(), defaultName + ".txt");
        while (file.exists()) {
            file = new File(ArmorStandSaver.getInstance().getDataFolder(), defaultName + "_" + count++ + ".txt");
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        configuration.set("standName", stand.getName());
        configuration.set("standSettings", saveSettings(stand));
        configuration.set("standPoses", savePoses(stand));
        configuration.set("standLocation", stand.getLocation());
        configuration.set("standDisabledSlots", saveDisabledSlots(stand));
        saveEquipment(stand, configuration);

        try {
            configuration.save(file);
            Bukkit.getConsoleSender().sendMessage("§aStand successfully saved as §e" + file.getName());
            return true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("§cAn error occurred while saving the file");
            return false;
        }
    }

    private static List<String> saveSettings(@NotNull ArmorStand stand) {
        List<String> save = new ArrayList<>();
        save.add(StandSettings.NAME_VISIBLE.name() + ":" + stand.isCustomNameVisible());
        save.add(StandSettings.VISIBLE.name() + ":" + stand.isVisible());
        save.add(StandSettings.MARKER.name() + ":" + stand.isMarker());
        save.add(StandSettings.SMALL.name() + ":" + stand.isSmall());
        save.add(StandSettings.INVULNERABLE.name() + ":" + stand.isInvulnerable());
        save.add(StandSettings.GRAVITY.name() + ":" + stand.hasGravity());
        save.add(StandSettings.ARMS.name() + ":" + stand.hasArms());
        save.add(StandSettings.BASE_PLATE.name() + ":" + stand.hasBasePlate());
        save.add(StandSettings.GLOWING.name() + ":" + stand.isGlowing());
        save.add(StandSettings.COLLIDABLE.name() + ":" + stand.isCollidable());
        return save;
    }

    private static List<String> savePoses(@NotNull ArmorStand stand) {
        List<String> save = new ArrayList<>();
        StandPose[] poses = StandPose.values();
        EulerAngle[] angles = new EulerAngle[]{stand.getHeadPose(), stand.getLeftArmPose(), stand.getRightArmPose(), stand.getLeftLegPose(), stand.getRightLegPose(), stand.getBodyPose()};

        for (int i = 0; i < angles.length; i++) {
            save.add(poses[i].name() + ":" + angles[i].getX() + "," + angles[i].getY() + "," + angles[i].getZ());
        }
        return save;
    }

    private static void saveEquipment(@NotNull ArmorStand stand, @NotNull YamlConfiguration configuration) {

        EntityEquipment e = stand.getEquipment();

        if (e == null) {
            return;
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            configuration.set(equipmentSlot.name(), e.getItem(equipmentSlot));
        }
    }

    private static List<String> saveDisabledSlots(@NotNull ArmorStand stand) {
        List<String> save = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lock : ArmorStand.LockType.values()) {
                save.add(slot.name() + ":" + lock.name() + "," + stand.hasEquipmentLock(slot, lock));
            }
        }
        return save;
    }
}
