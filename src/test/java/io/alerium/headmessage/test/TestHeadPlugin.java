package io.alerium.headmessage.test;

import io.alerium.headmessage.HeadMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

public class TestHeadPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        HeadMessage.init(this, new File(getDataFolder(), "cache"));
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        HeadMessage.sendHead(
                player,
                player.getUniqueId(),
                8,
                true,
                Arrays.asList("", "", "This is a centered", "message")
        );
    }
    
}
