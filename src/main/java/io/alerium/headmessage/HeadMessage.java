package io.alerium.headmessage;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.ChatPaginator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HeadMessage {

    private static Plugin plugin;
    private static BukkitAudiences audiences;
    private static File cacheFolder;

    private HeadMessage() {
    }

    /**
     * This method inits the HeadMessage util
     * @param pl The Plugin that will use the HeadMessage util
     * @param cf The folder to save the cached skulls, can be null
     */
    public static void init(Plugin pl, File cf) {
        plugin = pl;
        audiences = BukkitAudiences.create(pl);
        cacheFolder = cf;
        
        if (cacheFolder != null && !cacheFolder.exists())
            cacheFolder.mkdirs();
    }

    /**
     * This method sends a message to a CommandSender with a skull of a Player
     * @param receiver The CommandSender that will receive the message
     * @param skull The UUID of the Player with the skull
     * @param height The height of the message
     * @param center True if the message should be centered
     * @param message A list of String(s) with the message
     */
    public static void sendHead(CommandSender receiver, UUID skull, int height, boolean center, List<String> message) {
        if (plugin == null)
            return;
        
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> sendHead(receiver, skull, height, center, message));
            return;
        }
        
        List<Component> components = new ArrayList<>();
        BufferedImage image = getCachedSkullImage(skull, height);
        if (image == null)
            return;
        
        for (int y = 0; y < height; y++) {
            Component line = Component.empty();
            for (int x = 0; x < height; x++) {
                Color color = new Color(image.getRGB(x, y));
                line = line.append(Component.text('\u2588', TextColor.color(color.getRed(), color.getGreen(), color.getBlue())));
            }
            
            if (message != null && message.size() > y)
                line = line.append(LegacyComponentSerializer.legacyAmpersand().deserialize(center ? centeredText(message.get(y), ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH - height) : message.get(y)));
        
            components.add(line);
        }
        
        components.forEach(component -> audiences.sender(receiver).sendMessage(Identity.nil(), component));
    }

    private static BufferedImage getCachedSkullImage(UUID uuid, int size) {
        try {
            if (cacheFolder != null) {
                File file = new File(cacheFolder, uuid + "_" + size + ".png");
                if (file.exists())
                    return ImageIO.read(file);
            }
            
            BufferedImage image = getSkullImage(uuid, size);
            if (cacheFolder != null)
                ImageIO.write(image, "PNG", new File(cacheFolder, uuid + "_" + size + ".png"));
            
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static BufferedImage getSkullImage(UUID uuid, int size) throws IOException {
        URL url = new URL(String.format("https://crafthead.net/avatar/%s/%s", uuid, size));
        return ImageIO.read(url);
    }
    
    private static String centeredText(String s, int length) {
        if (s.length() >= length)
            return s;
        
        int spaces = (length - s.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaces; i++)
            sb.append(" ");
        sb.append(s);
        return sb.toString();
    }
    
}
