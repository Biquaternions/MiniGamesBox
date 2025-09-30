package plugily.projects.minigamesbox.classic.utils.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.events.spectator.settings.SpectatorSettingsMenu;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.actionbar.ActionBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovementTrackerTask implements Runnable {

    private final PluginMain plugin;
    private final SpectatorSettingsMenu settingsMenu;
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public MovementTrackerTask(PluginMain plugin, SpectatorSettingsMenu settingsMenu) {
        this.plugin = plugin;
        this.settingsMenu = settingsMenu;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location lastLocation = this.lastLocations.getOrDefault(player.getUniqueId(), player.getLocation());
            Location currentLocation = player.getLocation().clone();

            this.lastLocations.put(player.getUniqueId(), currentLocation);
            if (lastLocation.getWorld() == currentLocation.getWorld() && lastLocation.distance(currentLocation) < 0.1) {
                continue;
            }

            IUser user = this.plugin.getUserManager().getUser(player);
            if(user.getArena() != null) {
                this.settingsMenu.firstPersonMode.forEach(spectator -> {
                    if(spectator.getSpectatorTarget() instanceof Player) {
                        plugin.getActionBarManager().addActionBar(spectator, new ActionBar(new MessageBuilder("IN_GAME_SPECTATOR_SPECTATOR_MENU_SETTINGS_FIRST_PERSON_MODE_ACTION_BAR").asKey().arena(user.getArena()).player((Player) spectator.getSpectatorTarget()), ActionBar.ActionBarType.DISPLAY));
                    }
                });
            }
            if(!user.isSpectator()) {
                return;
            }
            Player target = this.settingsMenu.targetPlayer.get(player);
            if(target == null) {
                return;
            }
            if(player.getLocation().getWorld() != target.getLocation().getWorld()) {
                //Fix Cannot measure distance between worlds
                return;
            }
            double distance = player.getLocation().distance(target.getLocation());
            plugin.getActionBarManager().addActionBar(player, new ActionBar(new MessageBuilder("IN_GAME_SPECTATOR_SPECTATOR_MENU_SETTINGS_TARGET_PLAYER_ACTION_BAR").asKey().arena(user.getArena()).integer((int) distance).player(target), ActionBar.ActionBarType.DISPLAY));
            if(distance <= 15) {
                return;
            }
            if(this.settingsMenu.autoTeleport.contains(player)) {
                player.teleportAsync(target.getLocation());
            }
        }
    }

}
