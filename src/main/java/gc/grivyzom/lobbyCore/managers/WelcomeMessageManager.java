package gc.grivyzom.lobbyCore.managers;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

public class WelcomeMessageManager {

    private final MainClass plugin;
    private final Random random;

    public WelcomeMessageManager(MainClass plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Envía el mensaje de bienvenida completo a un jugador
     * @param player El jugador que se conecta
     */
    public void sendWelcomeMessage(Player player) {
        // Verificar si los mensajes de bienvenida están habilitados
        if (!plugin.getConfigManager().isWelcomeEnabled()) {
            return;
        }

        // Delay antes de enviar el mensaje (configurable)
        int delay = plugin.getConfigManager().getWelcomeDelay();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                // Enviar título y subtítulo
                sendWelcomeTitle(player);

                // Esperar un poco antes del mensaje en chat
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) return;
                        sendWelcomeChatMessage(player);
                        playWelcomeSound(player);

                        // Lanzar fuegos artificiales
                        plugin.getFireworksManager().launchWelcomeFireworks(player);
                    }
                }.runTaskLater(plugin, 20L); // 1 segundo después

            }
        }.runTaskLater(plugin, delay * 20L);
    }

    /**
     * Envía el título de bienvenida
     * @param player El jugador
     */
    private void sendWelcomeTitle(Player player) {
        String title = plugin.getConfigManager().getWelcomeTitle();
        String subtitle = plugin.getConfigManager().getWelcomeSubtitle();

        // Reemplazar placeholders
        title = replacePlaceholders(player, title);
        subtitle = replacePlaceholders(player, subtitle);

        // Obtener configuración de tiempo
        int fadeIn = plugin.getConfigManager().getTitleFadeIn();
        int stay = plugin.getConfigManager().getTitleStay();
        int fadeOut = plugin.getConfigManager().getTitleFadeOut();

        // Enviar título
        player.sendTitle(
                ColorUtils.translate(title),
                ColorUtils.translate(subtitle),
                fadeIn, stay, fadeOut
        );
    }

    /**
     * Envía el mensaje de bienvenida en el chat
     * @param player El jugador
     */
    private void sendWelcomeChatMessage(Player player) {
        List<String> messages = plugin.getConfigManager().getWelcomeMessages();

        // Verificar si hay mensajes configurados
        if (messages.isEmpty()) return;

        // Enviar cada línea del mensaje
        for (String message : messages) {
            String processedMessage = replacePlaceholders(player, message);

            // Aplicar efectos especiales si están configurados
            if (message.contains("{GRADIENT}")) {
                processedMessage = applyGradientEffect(processedMessage, player);
            }

            if (message.contains("{RAINBOW}")) {
                processedMessage = applyRainbowEffect(processedMessage);
            }

            ColorUtils.sendMessage(player, processedMessage);
        }

        // Mensaje adicional basado en la hora del día
        sendTimeBasedMessage(player);

        // Mensaje especial para nuevos jugadores
        if (!player.hasPlayedBefore()) {
            sendFirstTimeMessage(player);
        }
    }

    /**
     * Envía un mensaje basado en la hora del día
     * @param player El jugador
     */
    private void sendTimeBasedMessage(Player player) {
        if (!plugin.getConfigManager().isTimeBasedMessagesEnabled()) return;

        LocalTime now = LocalTime.now();
        String timeMessage = "";

        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(12, 0))) {
            timeMessage = plugin.getConfigManager().getMorningMessage();
        } else if (now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(18, 0))) {
            timeMessage = plugin.getConfigManager().getAfternoonMessage();
        } else if (now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            timeMessage = plugin.getConfigManager().getEveningMessage();
        } else {
            timeMessage = plugin.getConfigManager().getNightMessage();
        }

        if (!timeMessage.isEmpty()) {
            String processedMessage = replacePlaceholders(player, timeMessage);
            ColorUtils.sendMessage(player, processedMessage);
        }
    }

    /**
     * Envía mensaje especial para jugadores nuevos
     * @param player El jugador
     */
    private void sendFirstTimeMessage(Player player) {
        List<String> firstTimeMessages = plugin.getConfigManager().getFirstTimeMessages();

        if (firstTimeMessages.isEmpty()) return;

        // Delay adicional para mensaje de primer ingreso
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                for (String message : firstTimeMessages) {
                    String processedMessage = replacePlaceholders(player, message);
                    ColorUtils.sendMessage(player, processedMessage);
                }

                // Anunciar a todos los jugadores sobre el nuevo jugador
                if (plugin.getConfigManager().isAnnounceNewPlayers()) {
                    String announcement = plugin.getConfigManager().getNewPlayerAnnouncement();
                    announcement = replacePlaceholders(player, announcement);

                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.equals(player)) {
                            ColorUtils.sendMessage(online, announcement);
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 60L); // 3 segundos después
    }

    /**
     * Reproduce el sonido de bienvenida
     * @param player El jugador
     */
    private void playWelcomeSound(Player player) {
        if (!plugin.getConfigManager().isWelcomeSoundEnabled()) return;

        try {
            String soundString = plugin.getConfigManager().getWelcomeSound();
            float volume = (float) plugin.getConfigManager().getWelcomeSoundVolume();
            float pitch = (float) plugin.getConfigManager().getWelcomeSoundPitch();

            Sound sound = Sound.valueOf(soundString.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Error reproduciendo sonido de bienvenida: " + e.getMessage());
        }
    }

    /**
     * Aplica efecto degradado al texto
     * @param message El mensaje
     * @param player El jugador
     * @return El mensaje con degradado
     */
    private String applyGradientEffect(String message, Player player) {
        message = message.replace("{GRADIENT}", "");

        String startColor = plugin.getConfigManager().getGradientStartColor();
        String endColor = plugin.getConfigManager().getGradientEndColor();

        return ColorUtils.createGradient(message, startColor, endColor);
    }

    /**
     * Aplica efecto arcoíris al texto
     * @param message El mensaje
     * @return El mensaje con colores arcoíris
     */
    private String applyRainbowEffect(String message) {
        message = message.replace("{RAINBOW}", "");
        return ColorUtils.createRainbow(message, random.nextInt(12));
    }

    /**
     * Reemplaza placeholders en el mensaje
     * @param player El jugador
     * @param message El mensaje
     * @return El mensaje con placeholders reemplazados
     */
    private String replacePlaceholders(Player player, String message) {
        // Placeholders internos
        message = message.replace("{PLAYER}", player.getName())
                .replace("{DISPLAYNAME}", player.getDisplayName())
                .replace("{WORLD}", player.getWorld().getName())
                .replace("{ONLINE}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{MAX_PLAYERS}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{SERVER}", Bukkit.getServer().getName())
                .replace("{MOTD}", Bukkit.getServer().getMotd());

        // PlaceholderAPI si está disponible
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
    }

    /**
     * Envía un mensaje de bienvenida personalizado
     * @param player El jugador
     * @param customMessage El mensaje personalizado
     */
    public void sendCustomWelcomeMessage(Player player, String customMessage) {
        String processedMessage = replacePlaceholders(player, customMessage);
        ColorUtils.sendMessage(player, processedMessage);
    }

    /**
     * Recarga la configuración del gestor
     */
    public void reload() {
        plugin.getLogger().info(ColorUtils.translate("&a✓ &fGestor de mensajes de bienvenida recargado"));
    }
}