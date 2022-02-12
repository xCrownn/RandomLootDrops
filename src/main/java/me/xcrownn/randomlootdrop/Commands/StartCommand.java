package me.xcrownn.randomlootdrop.Commands;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import de.leonhard.storage.Yaml;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.xcrownn.randomlootdrop.RandomLootDrop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class StartCommand implements CommandExecutor, TabCompleter {

    public final List<String> LIST = Arrays.asList("start", "stop");
    private final RandomLootDrop plugin;

    public StartCommand(RandomLootDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if (player.hasPermission("RLD.Admin") || player.isOp()) {
            if (label.equalsIgnoreCase("lootdrops")) {
                Yaml messages = new Yaml("Messages", "plugins/RandomLootDrop");
                Yaml config = new Yaml("Config", "plugins/RandomLootDrop");

                if (args.length == 0) player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Errors.Not_Enough_Args")));
                if (args.length == 1) {

                    //Start timer and spawn chest
                    if (args[0].equalsIgnoreCase("start")) {
                        if (config.getBoolean("Is_Active")) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Errors.Already_Started")));
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Info.Enabled")));
                            config.set("Is_Active", true);
                            config.set("Is_Found", false);
                            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                                makeLootDrop();
                                config.set("Is_Found", false);
                            }, config.getInt("Spawn_Time_Minutes") * 20L * 60L, config.getInt("Spawn_Time_Minutes") * 20L * 60L);
                        }
                    }

                    //Stop timer and delete chest
                    if (args[0].equalsIgnoreCase("stop")) {
                        config.set("Is_Active", false);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Info.Disabled")));
                        Location location = new Location(Bukkit.getWorld(config.getString("World")), config.getDouble("Do_Not_Edit.X"), config.getDouble("Do_Not_Edit.Y"), config.getDouble("Do_Not_Edit.Z"));
                        Bukkit.getScheduler().cancelTasks(plugin);

                        //Remove hologram
                        for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
                            removeChest(hologram, location);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return (args.length <= 1) ? StringUtil.copyPartialMatches(args[0], LIST, new ArrayList<>()) : null;
    }

    public static double randNum(double min, double max) {

        double rand = ThreadLocalRandom.current().nextDouble();

        return rand * (max - min) + min;

    }

    public static double roundToHalf(double d) {
        double roundedF;
        double round1 = Math.round(d * 2) / 2.0;
        if (round1 % 1 == 0) {
            roundedF = round1 + .5;
        } else {
            roundedF = round1 + 0;
        }

        return roundedF;
    }

    public void removeChest(Hologram hologram, Location location) {
        hologram.delete();
        Bukkit.getWorld(location.getWorld().getName()).setBlockData(location, Material.AIR.createBlockData());
    }

    public void makeLootDrop() {
        //use while loop to keep generating the random numbers until it is not in a region

        Yaml config = new Yaml("Config", "plugins/RandomLootDrop");
        Yaml messages = new Yaml("Messages", "plugins/RandomLootDrop");


        //Getting random location
        World world = Bukkit.getWorld(config.getString("World"));
        final double randomX = randNum(world.getWorldBorder().getSize() / -2, world.getWorldBorder().getSize() / 2);
        final double randomZ = randNum(world.getWorldBorder().getSize() / -2, world.getWorldBorder().getSize() / 2);
        double y = 0;


        //setting random location to a location
        Location location = new Location(Bukkit.getWorld(config.getString("World")), roundToHalf(randomX) , 0, roundToHalf(randomZ));
        y = location.getWorld().getHighestBlockYAt(location);
        location.setY(y + 1);


        while (GriefPrevention.instance.dataStore.getClaimAt(location, true, null) != null) {
            final double randomXl = roundToHalf(randNum(world.getWorldBorder().getSize() / -2, world.getWorldBorder().getSize() / 2));
            final double randomZl = roundToHalf(randNum(world.getWorldBorder().getSize() / -2, world.getWorldBorder().getSize() / 2));
            location.setX(randomXl);
            location.setZ(randomZl);
        }
        config.set("Do_Not_Edit.X", location.getBlockX());
        config.set("Do_Not_Edit.Y", location.getBlockY());
        config.set("Do_Not_Edit.Z", location.getBlockZ());

        //set the chest in the world
        Bukkit.getWorld(location.getWorld().getName()).setBlockData(location, Material.CHEST.createBlockData());


        //spawn the hologram above the chest
        double holoY = location.getBlockY() + 1.5;
        location.setY(holoY);
        Hologram hologram = HologramsAPI.createHologram(plugin, location);
        hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', messages.getString("Chest.Chest_Holo")));


        //Announce where the chest is near
        int range = config.getInt("Broadcast_Cords_Range");
        double randomNewX = randNum(0, range) + location.getBlockX();
        double randomNewZ = randNum(0, range) + location.getBlockZ();

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Chest.Chest_Spawned")
                .replace("{x}", roundToHalf(randomNewX) + "")
                .replace("{y}", location.getBlockY() + "")
                .replace("{z}", roundToHalf(randomNewZ) + "")));

        //Start chest despawn timer
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (!config.getBoolean("Is_Found")) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Chest.Chest_Never_Found")));
                hologram.delete();
                location.setY(location.getY() - 1);
                Bukkit.getWorld(location.getWorld().getName()).setBlockData(location, Material.AIR.createBlockData());
                System.out.println(location);
            }
        }, config.getInt("Time_To_Find_Minutes") * 20L * 60L);
    }
}
