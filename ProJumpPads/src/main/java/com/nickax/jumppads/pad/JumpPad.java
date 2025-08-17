package com.nickax.jumppads.pad;

import com.nickax.jumppads.JumpPads;
import com.nickax.jumppads.api.JumpPadLandEvent;
import com.nickax.jumppads.util.Util;
import com.nickax.jumppads.util.Variable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JumpPad implements Listener {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private String name;
    private String permission1;
    private String permission2;
    private String permissionMessage;
    private double pos1X;
    private double pos1Z;
    private double pos2X;
    private double pos2Z;
    private double relative;
    private Location target = null;
    private List<String> commands = new ArrayList<>();
    private BukkitTask particleTask = null;
    private final Map<Player, BukkitTask> tasks = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public JumpPad(JumpPads plugin, Player player, String name) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder() + File.separator + "List", name + ".yml");

        if (file.exists()) {
            Util.sendMessage(player, "jump-pad-exists", false);
            return;
        }

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.name = name;
        this.target = player.getLocation();

        Variable.jumpPads.add(this);

        Util.sendMessage(player, "created", false);
        showParticles();
        save();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public JumpPad(JumpPads plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.name = file.getName().replace(".yml", "");
        this.pos1X = config.getDouble("pos1X");
        this.pos1Z = config.getDouble("pos1Z");
        this.pos2X = config.getDouble("pos2X");
        this.pos2Z = config.getDouble("pos2Z");
        this.relative = config.getDouble("relative-position");
        this.permissionMessage = config.getString("permission-message");
        this.permission1 = config.getString("permission-1");
        this.permission2 = config.getString("permission-2");
        this.commands = config.getStringList("commands");

        loadTargetLocation();
        showParticles();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void showParticles() {
        Location center = getCenter();
        if (center == null) return;

        if (particleTask != null) {
            particleTask.cancel();
        }

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Util.showParticle("jump-pad", center);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, (long)Math.max(Variable.settings.getInt("particles.jump-pad.time"), 1) * 20L);
    }

    public void save() {
        if (target != null) {
            config.set("fly-location.world", Objects.requireNonNull(target.getWorld()).getName());
            config.set("fly-location.x", target.getX());
            config.set("fly-location.y", target.getY());
            config.set("fly-location.z", target.getZ());
        }

        if (commands != null && !commands.isEmpty()) {
            config.set("commands", commands);
        }

        if (name != null && !name.isEmpty()) {
            config.set("name", name);
        }

        if (pos1X != 0 && pos1Z != 0) {
            config.set("pos1X", pos1X);
            config.set("pos1Z", pos1Z);
        }

        if (pos2X != 0 && pos2Z != 0) {
            config.set("pos2X", pos2X);
            config.set("pos2Z", pos2Z);
        }

        if (relative != 0) {
            config.set("relative-position", relative);
        }

        if (permission1 != null && !permission1.isEmpty()) {
            config.set("permission-1", permission1);
        }

        if (permission2 != null && !permission2.isEmpty()) {
            config.set("permission-2", permission2);
        }

        if (permissionMessage != null && !permissionMessage.isEmpty()) {
            config.set("permission-message", permissionMessage);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(boolean save) {
        if (save) save();
        HandlerList.unregisterAll(this);

        if (particleTask != null) {
            particleTask.cancel();
        }

        if (tasks.isEmpty()) return;

        tasks.forEach((player, task) -> {
            player.setNoDamageTicks(0);
            player.teleport(target);

            Variable.isJumping.remove(player);
            task.cancel();

            JumpPadLandEvent event = new JumpPadLandEvent(player, this);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) return;

            if (player.isOnline()) {
                Util.playSound(player, "land");
            }

            if (commands != null && !commands.isEmpty()) {
                for (String command : commands) {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            command
                                    .replace("%player%", player.getName())
                                    .replace("%uuid%", player.getUniqueId().toString())
                                    .replace("%jump-pad%", name)
                    );
                }
            }
        });
    }

    public boolean addCommand(String command) {
        return commands.add(command);
    }

    public boolean removeCommand(String command) {
        return commands.remove(command);
    }

    public void setTargetLocation(Location location) {
        this.target = location;
        showParticles();
        save();
    }

    public void setPermission(int target, String permission) {
        boolean none = permission == null || permission.isEmpty() || permission.equalsIgnoreCase("none");
        if (target == 1) {
            permission1 = none ? null : permission;
        } else if (target == 2) {
            permission2 = none ? null : permission;
        }
    }

    public void setPos1(Location location) {
        pos1X = location.getX();
        pos1Z = location.getZ();
        relative = location.getY();
        showParticles();
    }

    public void setPos2(Location location) {
        pos2X = location.getX();
        pos2Z = location.getZ();
        relative = location.getY();
        showParticles();
    }

    public void setPermissionMessage(String message) {
        if (message == null || message.isEmpty() || message.equals("none")) {
            permissionMessage = null;
        } else {
            permissionMessage = message;
        }
    }

    public String getName() {
        return name;
    }

    public Location getCenter() {
        if (isNegative() || target == null) return null;
        double minX = Math.min(this.pos1X, this.pos2X);
        double minZ = Math.min(this.pos1Z, this.pos2Z);
        double x1 = Math.max(this.pos1X, this.pos2X) + 1;
        double z1 = Math.max(this.pos1Z, this.pos2Z) + 1;
        return new Location(target.getWorld(), minX + (x1 - minX) / 2.0D, relative + (relative + 1 - relative) / 2.0D, minZ + (z1 - minZ) / 2.0D);
    }

    public void getInfo(Player player) {
        List<String> info = Variable.messages.getStringList("info");
        if (info.isEmpty()) return;

        for (String message : info) {
            player.sendMessage(Util.color(message
                    .replace("%name%", name)
                    .replace("%permission-1%", permission1 != null ? permission1 : Variable.messages.getString("none", "None"))
                    .replace("%permission-2%", permission2 != null ? permission2 : Variable.messages.getString("none", "None"))
                    .replace("%permission-message%", permissionMessage != null ? permissionMessage : Variable.messages.getString("none", "None"))
                    .replace("%fly-location%", target != null ? target.getX() + ", " + target.getY() + ", " + target.getZ() : Variable.messages.getString("not-set", "Not Set"))
                    .replace("%position-1%", pos1X != 0 ? pos1X + ", " + pos1Z : Variable.messages.getString("not-set", "Not Set"))
                    .replace("%position-2%", pos2X != 0 ? pos2X + ", " + pos2Z : Variable.messages.getString("not-set", "Not Set"))));
        }
    }

    public File getFile() {
        return file;
    }

    public String getPermission1() {
        return permission1;
    }

    public String getPermission2() {
        return permission2;
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e) {
        if (isNegative()) return;

        Location to = e.getTo();
        if (to == null) return;

        Player player = e.getPlayer();
        if (Variable.isJumping.containsKey(player)) return;

        Block down = to.getBlock().getRelative(BlockFace.DOWN);
        if (down.getType().equals(Material.AIR)) return;

        String name = down.getType().name();
        String accepted = Variable.settings.getString("block", "SLIME_BLOCK");

        if (!name.equalsIgnoreCase(accepted)) {
            down = down.getRelative(BlockFace.DOWN);
            if (down.getType().equals(Material.AIR)) return;
            name = down.getType().name();
            if (!name.equalsIgnoreCase(accepted)) return;
        }

        double y = to.getY();
        double relative1 = relative - 2;
        double relative2 = relative + 2;
        if (y < relative1 || y > relative2) return;

        if (!isInside(to)) return;

        List<String> permissions = new ArrayList<>();
        if (permission1 != null) permissions.add(permission1);
        if (permission2 != null) permissions.add(permission2);

        for (String permission : permissions) {
            if (!permission.isEmpty() && !player.hasPermission(permission)) {
                noPermission(player, permission);
                return;
            }
        }

        launch(player);
    }

    private void noPermission(Player player, String permission) {
        Util.sendMessage(player, "no-permission", false);

        String message = permissionMessage != null ? permissionMessage.replace("%permission%", permission) : null;
        if (message != null) {
            player.sendMessage(Util.color(message));
        }

        Util.showParticle("no-permission", player.getEyeLocation());

        ConfigurationSection section = Variable.settings.getConfigurationSection("push");
        if (section == null || !section.getBoolean("enabled")) return;

        Location center = getCenter();
        if (center == null) return;

        Vector push = player.getLocation().toVector().subtract(center.toVector());
        push = push.normalize().multiply(section.getDouble("power"));
        double y = section.getDouble("y");

        if (y > 0.0D) {
            push.setY(y);
        }

        player.setVelocity(push);
    }

    private void launch(Player player) {
        if (target == null) {
            Util.sendMessage(player, "fly-location-not-set", false);
            return;
        }

        if (!Objects.equals(target.getWorld(), player.getWorld())) return;

        JumpPadLandEvent event = new JumpPadLandEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.addPassenger(player);
        Variable.isJumping.put(player, stand);
        Util.playSound(player, "launch");

        Util.showParticle("travel", player.getEyeLocation());

        BukkitTask task = new BukkitRunnable() {

            private final double x3 = target.distance(player.getLocation()) - 0.0D;
            private final double x2 = this.x3 / 3.0D;
            private final double y3 = Math.abs(target.getY() - player.getLocation().getY()) % 10;
            private final double A3 = -((-this.x2 + this.x3) / (-0.0D + this.x2)) * (-0.0D + this.x2 * this.x2) - this.x2 * this.x2 + this.x3 * this.x3;
            private final double D3 = -((-this.x2 + this.x3) / (-0.0D + this.x2)) * (-0.0D + this.x2) - this.x2 + this.y3;
            private final double a = this.D3 / this.A3;
            private final double b = (-0.0D + this.x2 - (-0.0D + this.x2 * this.x2) * this.a) / (-0.0D + this.x2);
            private final double c = 0.0D - this.a * 0.0D * 0.0D - this.b * 0.0D;
            private double xC = 0.0D;

            public void run() {
                if (player.getNoDamageTicks() <= 0) {
                    player.setNoDamageTicks(100);
                }

                if (!player.isOnline()) {
                    player.setNoDamageTicks(0);
                    stand.remove();

                    player.teleport(target);

                    Variable.isJumping.remove(player);
                    tasks.remove(player);
                    cancel();

                    JumpPadLandEvent event = new JumpPadLandEvent(player, JumpPad.this);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) return;

                    if (commands != null && !commands.isEmpty()) {
                        for (String command : commands) {
                            Bukkit.dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    command
                                            .replace("%player%", player.getName())
                                            .replace("%uuid%", player.getUniqueId().toString())
                                            .replace("%jump-pad%", name)
                            );
                        }
                    }
                    return;
                }

                if (target.distance(stand.getLocation()) <= 0.5 || !Variable.isJumping.containsKey(player)) {
                    player.setNoDamageTicks(100);
                    stand.remove();

                    Variable.isJumping.remove(player);
                    tasks.remove(player);
                    cancel();

                    JumpPadLandEvent event = new JumpPadLandEvent(player, JumpPad.this);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) return;

                    Util.playSound(player, "land");
                    if (commands != null && !commands.isEmpty()) {
                        for (String command : commands) {
                            Bukkit.dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    command
                                            .replace("%player%", player.getName())
                                            .replace("%uuid%", player.getUniqueId().toString())
                                            .replace("%jump-pad%", name)
                            );
                        }
                    }
                    return;
                }

                Util.playSound(player, "flying");
                moveToward(stand, yCalculate(a, b, c, xC));
                xC += 0.84D;
            }
        }.runTaskTimer(plugin, 1L, 1L);

        tasks.put(player, task);
    }

    private void loadTargetLocation() {
        ConfigurationSection section = config.getConfigurationSection("fly-location");
        if (section == null) return;

        String worldName = section.getString("world");
        if (worldName == null || worldName.isEmpty()) return;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        this.target = new Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
    }

    private boolean isNegative() {
        return (pos1X == 0 && pos1Z == 0) || (pos2X == 0 && pos2Z == 0);
    }

    private boolean isInside(Location b) {
        boolean zCheck = isBetween(pos1Z, pos2Z, b.getZ(), 1.0);
        boolean xCheck = isBetween(pos1X, pos2X, b.getX(), 1.0);
        return zCheck && xCheck;
    }

    private boolean isBetween(double v1, double v2, double b, double buffer) {
        return (b >= v1 - buffer && b <= v2 + buffer) || (b <= v1 + buffer && b >= v2 - buffer);
    }

    private void moveToward(Entity stand, double yC) {
        Location location = stand.getLocation();
        double x = location.getX() - target.getX();
        double y = location.getY() - target.getY() - (Math.max(yC, 0.0D));
        double z = location.getZ() - target.getZ();
        Vector velocity = new Vector(x, y, z).normalize().multiply(-0.8D);
        stand.setVelocity(velocity);
    }

    private double yCalculate(double a, double b, double c, double x) {
        return (a * x * x) + (x * b + c);
    }
}
