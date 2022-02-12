package me.xcrownn.randomlootdrop;

import de.leonhard.storage.Yaml;
import me.xcrownn.randomlootdrop.Commands.StartCommand;
import me.xcrownn.randomlootdrop.Events.ChestListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class RandomLootDrop extends JavaPlugin {


    @Override
    public void onEnable() {
        Yaml config = new Yaml("Config", "plugins/RandomLootDrop");
        config.setDefault("World", "world");
        config.setDefault("Spawn_Time_Minutes", 30);
        config.setDefault("Time_To_Find_Minutes", 15);
        config.setDefault("Broadcast_Cords_Range", 10);
        config.setDefault("Is_Active", false);
        config.setDefault("Is_Found", false);

        Yaml commands = new Yaml("Commands", "plugins/RandomLootDrop");
        ArrayList<String> list = new ArrayList<>();
        list.add("give {player} minecraft:diamond");
        list.add("give {player} minecraft:gold_block");
        list.add("give {player} minecraft:diamond_block");
        commands.setDefault("Commands", list);

        Yaml messages = new Yaml("Messages", "plugins/RandomLootDrop");
        messages.setDefault("Errors.No_Permission", No_perms);
        messages.setDefault("Errors.Not_Enough_Args", no_Args);
        messages.setDefault("Errors.Already_Started", already_Started);

        messages.setDefault("Info.Enabled", enabled);
        messages.setDefault("Info.Disabled", disabled);

        messages.setDefault("Chest.Chest_Holo", chestHolo);
        messages.setDefault("Chest.Chest_Spawned", chestSpawn);
        messages.setDefault("Chest.Chest_Never_Found", not_found);
        messages.setDefault("Chest.Chest_Found", chest_Found);

        //&a&lLoot Chest &7(Click to claim!)
        //&aNo one found the loot drop in time! It has been despawned!

        getCommand("lootdrops").setExecutor(new StartCommand(this));
        getCommand("lootdrops").setTabCompleter(new StartCommand(this));
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);

    }

    @Override
    public void onDisable() {
        Yaml config = new Yaml("Config", "plugins/RandomLootDrop");
        config.set("Is_Active", false);
    }

    private final String No_perms = "&cYou do not have permission to use this command!";
    private final String no_Args = "&cYou do not enter a sub-command!";
    private final String already_Started = "&cLoot drops is already started!";
    private final String enabled = "&aLoot drops has been started!";
    private final String disabled = "&cLoot drops have been disabled!";
    private final String chestHolo = "&a&lLoot Chest &7(Click to claim!)";
    private final String not_found = "&aNo one found the loot drop in time! It has been despawned!";
    private final String chestSpawn = "&aA chest has spawned near &6[&f{x}&6, &f{y}&6, &f{z}&6]&a!";
    private final String chest_Found = "&aThe loot chest was found by &f" + "{player}" + "&a&l! A new loot chest is spawning in " + "{spawnTime}" + " &a&lminutes!";


}
