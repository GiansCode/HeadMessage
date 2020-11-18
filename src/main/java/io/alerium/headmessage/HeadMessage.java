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
import java.util.logging.Level;

public final class HeadMessage {

    private static Plugin plugin;
    private static BukkitAudiences audiences;
    private static File cacheFolder;

    private HeadMessage() { }

    /**
     * This method inits the HeadMessage util
     *
     * @param pl The Plugin that will use the HeadMessage util
     * @param cf The folder to save the cached skulls, can be null
     */
    public static void init(final Plugin pl, final File cf) {
        plugin = pl;
        audiences = BukkitAudiences.create(pl);
        cacheFolder = cf;

        if (cacheFolder != null && !cacheFolder.exists())
            cacheFolder.mkdirs();
    }

    /**
     * This method sends a message to a CommandSender with a skull of a Player
     *
     * @param receiver The CommandSender that will receive the message
     * @param skull    The UUID of the Player with the skull
     * @param height   The height of the message
     * @param center   True if the message should be centered
     * @param message  A list of String(s) with the message
     */
    public static void sendHead(final CommandSender receiver, final UUID skull, final int height, final boolean center, final List<String> message) {
        if (plugin == null)
            return;

        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> sendHead(receiver, skull, height, center, message));
            return;
        }

        final List<Component> components = getComponentList(skull, height, center, message);
        if (components.isEmpty()) return;

        components.forEach(component -> audiences.sender(receiver).sendMessage(Identity.nil(), component));
    }

    /**
     * Returns the component list to be sent
     *
     * @param skull   The UUID of the Player with the skull
     * @param height  The height of the message
     * @param center  True if the message should be centered
     * @param message A list of String(s) with the message
     * @return A list of components to be sent to an audience
     */
    public static List<Component> getComponentList(final UUID skull, final int height, final boolean center, final List<String> message) {
        final List<Component> components = new ArrayList<>();
        final BufferedImage image = getCachedSkullImage(skull, height);
        if (image == null) return components;

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

        return components;
    }

    /**
     * @param uuid The UUID of the Player with the skull
     * @param size The image size
     * @return A BufferedImage retrieved from the cache or null if not present
     */
    private static BufferedImage getCachedSkullImage(final UUID uuid, final int size) {
        try {
            if (cacheFolder != null) {
                final File file = new File(cacheFolder, uuid + "_" + size + ".png");
                if (file.exists())
                    return ImageIO.read(file);
            }

            final BufferedImage image = getSkullImage(uuid, size);
            if (cacheFolder != null)
                ImageIO.write(image, "PNG", new File(cacheFolder, uuid + "_" + size + ".png"));

            return image;
        } catch (final IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to retrieve a BufferImage from Cache!", ex);
            return null;
        }
    }

    /**
     * Returns the Skull image from Crafatar
     *
     * @param uuid The UUID of the Player with the skull
     * @param size The image size
     * @return BufferedImage of the Player's head avatar
     * @throws IOException If the Image is corrupt
     */
    private static BufferedImage getSkullImage(final UUID uuid, final int size) throws IOException {
        final URL url = new URL(String.format("https://crafatar.com/avatars/%s?size=%s", uuid, size));
        return ImageIO.read(url);
    }

    /**
     * Returns a centered String with a specified chat length
     *
     * @param s      Message string
     * @param length Chat length?
     * @return Centered message string
     */
    private static String centeredText(final String s, final int length) {
        if (s.length() >= length)
            return s;

        final int spaces = (length - s.length()) / 2;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaces; i++)
            sb.append(" ");
        sb.append(s);
        return sb.toString();
    }

}
