package me.statuxia.stand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Stand {

    ArmorStand stand;
    YamlConfiguration configuration;

    public Stand(ArmorStand stand) {
        this.stand = stand;
    }

    public Stand(String path, Location location) {
        load(path, location);
    }

    public Stand(String path) {
        load(path, null);
    }

    @Nullable
    public ArmorStand load(String path, Location location) {
        File file = new File("plugins/ArmorStandSaver/", path);
        if (!file.exists()) {
            return null;
        }
        configuration = YamlConfiguration.loadConfiguration(file);
        if (location == null) {
            location = loadLocation();
            if (location == null) {
                return null;
            }
        }
        String standName = configuration.getString("name");

        stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setCustomName(standName);
        loadPoses();
        loadSettings();
        loadEquipment();
        loadDisabledSlots();
        return stand;
    }

    public void save() {
        if (stand == null) {
            Bukkit.getConsoleSender().sendMessage("§cStand for save is null");
            return;
        }

        String defaultName = "stand";
        int count = 0;
        File file = new File("plugins/ArmorStandSaver/", defaultName + ".txt");
        while (file.exists()) {
            file = new File("plugins/ArmorStandSaver/", defaultName + "_" + count++ + ".txt");
        }
        configuration = YamlConfiguration.loadConfiguration(file);

        configuration.set("name", stand.getName());
        configuration.set("settings", saveSettings());
        configuration.set("poses", savePoses());
        configuration.set("location", stand.getLocation());
        saveEquipment();
        configuration.set("disabledSlots", saveDisabledSlots());

        try {
            configuration.save(file);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void loadSettings() {
        for (String rawSettings : configuration.getStringList("settings")) {
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

    private void loadPoses() {
        for (String rawPose : configuration.getStringList("poses")) {
            String[] rawPoseString = rawPose.split(":");
            String[] angles = rawPoseString[1].split(",");
            EulerAngle angle = new EulerAngle(Double.parseDouble(angles[0]), Double.parseDouble(angles[1]), Double.parseDouble(angles[2]));
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

    @Nullable
    private Location loadLocation() {
        Location location = configuration.getLocation("location");
        if (location != null) {
            return location;
        }
        String[] rawLocation = configuration.getStringList("location").get(0).split(":");
        String[] stringLocation = rawLocation[1].split(",");
        Float[] floats = Arrays.stream(stringLocation).map(Float::valueOf).toArray(Float[]::new);
        World world = Bukkit.getWorld(rawLocation[0]) == null ? Bukkit.getWorlds().get(0) : Bukkit.getWorld(rawLocation[0]);

        switch (floats.length) {
            case 3 -> {
                return new Location(world, floats[0], floats[1], floats[2]);
            }
            case 5 -> {
                return new Location(world, floats[0], floats[1], floats[2], floats[3], floats[4]);
            }
            default -> {
                return null;
            }
        }
    }

    private void loadEquipment() {
        EntityEquipment equipment = stand.getEquipment();
        if (equipment == null) {
            return;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack item = configuration.getItemStack(equipmentSlot.name());
            equipment.setItem(equipmentSlot, item);
        }
    }

    private void loadDisabledSlots() {
        List<String> rawDisabledSlots = configuration.getStringList("disabledSlots");

        for (String rawDisabledSlot : rawDisabledSlots) {
            String[] rawDisabledSlotString = rawDisabledSlot.split(":");
            String[] rawDisabledSlotTypeString = rawDisabledSlotString[1].split(",");
            if (Boolean.parseBoolean(rawDisabledSlotTypeString[1])) {
                stand.addEquipmentLock(EquipmentSlot.valueOf(rawDisabledSlotString[1]), ArmorStand.LockType.valueOf(rawDisabledSlotTypeString[0]));
            }
        }
    }

    private List<String> saveSettings() {
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

    private List<String> savePoses() {
        List<String> save = new ArrayList<>();
        StandPose[] poses = StandPose.values();
        EulerAngle[] angles = new EulerAngle[]{stand.getHeadPose(), stand.getLeftArmPose(), stand.getRightArmPose(), stand.getLeftLegPose(), stand.getRightLegPose(), stand.getBodyPose()};

        for (int i = 0; i < angles.length; i++) {
            save.add(poses[i].name() + ":" + angles[i].getX() + "," + angles[i].getY() + "," + angles[i].getZ());
        }
        return save;
    }

    private void saveEquipment() {

        EntityEquipment e = stand.getEquipment();

        if (e == null) {
            return;
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            configuration.set(equipmentSlot.name(), e.getItem(equipmentSlot));
        }
    }

    private List<String> saveDisabledSlots() {
        List<String> save = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lock : ArmorStand.LockType.values()) {
                save.add(slot.name() + ":" + lock.name() + "," + stand.hasEquipmentLock(slot, lock));
            }
        }
        return save;
    }
}
