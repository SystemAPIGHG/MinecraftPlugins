package rorg.example.randomrtp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Random;

public class RandomTPPlugin extends JavaPlugin implements CommandExecutor {

    private FileConfiguration langConfig;
    private String lang = "en";

    @Override
    public void onEnable() {
        this.getCommand("randomtp").setExecutor(this);
        this.getCommand("randomtplang").setExecutor(this);
        loadLanguage();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("randomtp")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                startTeleportCountdown(player);
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("randomtplang")) {
            if (args.length > 0 && (args[0].equalsIgnoreCase("en") || args[0].equalsIgnoreCase("de") || args[0].equalsIgnoreCase("tr") || args[0].equalsIgnoreCase("in") || args[0].equalsIgnoreCase("ru") || args[0].equalsIgnoreCase("ka"))) {
                lang = args[0].toLowerCase();
                loadLanguage();
                sender.sendMessage(langConfig.getString("messages.language_changed"));
                return true;
            } else {
                sender.sendMessage(langConfig.getString("messages.invalid_language"));
                return false;
            }
        }
        return false;
    }

    private void loadLanguage() {
        File langFile = new File(getDataFolder(), "lang_" + lang + ".yml");
        if (!langFile.exists()) {
            saveResource("lang_" + lang + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private void startTeleportCountdown(Player player) {
        new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (countdown > 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    player.sendMessage(String.format(langConfig.getString("messages.countdown"), countdown));
                    spawnParticles(player);
                    countdown--;
                } else {
                    randomTeleport(player);
                    cancel();
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void spawnParticles(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        for (double y = loc.getY(); y < loc.getY() + 2; y += 0.25) {
            world.spawnParticle(Particle.VILLAGER_HAPPY, loc.getX(), y, loc.getZ(), 5, 0.2, 0.2, 0.2, 0.01);
        }
    }

    private void randomTeleport(Player player) {
        World world = player.getWorld();
        Random random = new Random();
        Location randomLocation;

        do {
            int x = random.nextInt(100000) - 50000;
            int z = random.nextInt(100000) - 50000;
            int y = world.getHighestBlockYAt(x, z);

            randomLocation = new Location(world, x, y, z);
        } while (!isLocationSafe(randomLocation));

        player.teleport(randomLocation);
        player.sendMessage(langConfig.getString("messages.teleporting"));
    }

    private boolean isLocationSafe(Location location) {
        Material blockType = location.getBlock().getType();
        return blockType != Material.WATER && blockType != Material.LAVA && blockType != Material.BEDROCK;
    }
}
