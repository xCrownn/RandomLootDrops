package me.xcrownn.randomlootdrop.Events;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import de.leonhard.storage.Yaml;
import me.xcrownn.randomlootdrop.RandomLootDrop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.ArrayList;
import java.util.Random;

public class ChestListener implements Listener {

    private final RandomLootDrop plugin;

    public ChestListener(RandomLootDrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void chestInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Yaml config = new Yaml("Config", "plugins/RandomLootDrop");
        Yaml messages = new Yaml("Messages", "plugins/RandomLootDrop");

        Location location = new Location(Bukkit.getWorld(config.getString("World")), config.getDouble("Do_Not_Edit.X"), config.getDouble("Do_Not_Edit.Y"), config.getDouble("Do_Not_Edit.Z"));

        int spawnTime = config.getInt("Spawn_Time_Minutes");
        boolean isActive = config.getBoolean("Is_Active");
        boolean isFound = config.getBoolean("Is_Found");

        if (event.getClickedBlock() == null) return;

        if (!isFound && isActive && event.getClickedBlock().getLocation().equals(location) && event.getClickedBlock().getBlockData().getMaterial() == Material.CHEST) {

            event.setCancelled(true);
            config.set("Is_Found", true);
            randomReward(player);

            player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Chest.Chest_Found")
                    .replace("{player}", player.getName())
                    .replace("{spawnTime}", spawnTime + "")));

            for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
                removeChestStartNew(hologram, location);
            }
        }

    }

    public void removeChestStartNew(Hologram hologram, Location location) {
            hologram.delete();
            Bukkit.getWorld(location.getWorld().getName()).setBlockData(location, Material.AIR.createBlockData());
    }

    public void randomReward(Player player) {
        Yaml commands = new Yaml("Commands", "plugins/RandomLootDrop");
        ArrayList<String> list = (ArrayList<String>) commands.getStringList("Commands");
        Random random = new Random();
        int i = random.nextInt(list.size());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), list.get(i).replace("{player}", player.getName()));
    }


}
