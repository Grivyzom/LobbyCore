package gc.grivyzom.lobbyCore.managers;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FireworksManager {

    private final MainClass plugin;
    private final Random random;

    public FireworksManager(MainClass plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Lanza fuegos artificiales para dar la bienvenida a un jugador
     * @param player El jugador que se conecta
     */
    public void launchWelcomeFireworks(Player player) {
        // Verificar si los fuegos artificiales están habilitados
        if (!plugin.getConfigManager().isFireworksEnabled()) {
            return;
        }

        // Delay antes de lanzar los fuegos artificiales
        int delay = plugin.getConfigManager().getFireworksDelay();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                launchFireworksSequence(player);
            }
        }.runTaskLater(plugin, delay * 20L);
    }

    /**
     * Lanza una secuencia de fuegos artificiales
     * @param player El jugador
     */
    private void launchFireworksSequence(Player player) {
        int amount = plugin.getConfigManager().getFireworksAmount();

        for (int i = 0; i < amount; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    launchSingleFirework(player);
                }
            }.runTaskLater(plugin, i * 10L); // 0.5 segundos entre cada fuego artificial
        }
    }

    /**
     * Lanza un solo fuego artificial
     * @param player El jugador
     */
    private void launchSingleFirework(Player player) {
        try {
            Location location = getRandomFireworkLocation(player.getLocation());
            Firework firework = player.getWorld().spawn(location, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();

            // Crear el efecto del fuego artificial
            FireworkEffect effect = createFireworkEffect();
            meta.addEffect(effect);

            // Configurar poder (altura)
            int power = plugin.getConfigManager().getFireworksPower();
            meta.setPower(power);

            firework.setFireworkMeta(meta);

            plugin.getLogger().info(ColorUtils.translate(
                    "&a✨ &fFuego artificial lanzado para " + player.getName()
            ));

        } catch (Exception e) {
            plugin.getLogger().warning("Error al lanzar fuego artificial: " + e.getMessage());
        }
    }

    /**
     * Crea un efecto de fuego artificial aleatorio
     * @return El efecto del fuego artificial
     */
    private FireworkEffect createFireworkEffect() {
        FireworkEffect.Builder builder = FireworkEffect.builder();

        // Tipo aleatorio
        List<String> types = plugin.getConfigManager().getFireworksTypes();
        if (!types.isEmpty()) {
            String randomType = types.get(random.nextInt(types.size()));
            try {
                FireworkEffect.Type type = FireworkEffect.Type.valueOf(randomType.toUpperCase());
                builder.with(type);
            } catch (IllegalArgumentException e) {
                builder.with(FireworkEffect.Type.BALL); // Por defecto
            }
        }

        // Colores principales
        List<Color> colors = getRandomColors(plugin.getConfigManager().getFireworksColors(), 1, 3);
        if (!colors.isEmpty()) {
            builder.withColor(colors);
        }

        // Colores de desvanecimiento
        List<Color> fadeColors = getRandomColors(plugin.getConfigManager().getFireworksFadeColors(), 1, 2);
        if (!fadeColors.isEmpty()) {
            builder.withFade(fadeColors);
        }

        // Efectos especiales
        if (plugin.getConfigManager().isFireworksFlicker()) {
            builder.flicker(random.nextBoolean());
        }

        if (plugin.getConfigManager().isFireworksTrail()) {
            builder.trail(random.nextBoolean());
        }

        return builder.build();
    }

    /**
     * Obtiene una ubicación aleatoria para el fuego artificial
     * @param playerLocation Ubicación del jugador
     * @return Ubicación donde lanzar el fuego artificial
     */
    private Location getRandomFireworkLocation(Location playerLocation) {
        int height = plugin.getConfigManager().getFireworksHeight();
        int spread = plugin.getConfigManager().getFireworksSpread();

        double x = playerLocation.getX() + (random.nextDouble() - 0.5) * spread * 2;
        double y = playerLocation.getY() + height;
        double z = playerLocation.getZ() + (random.nextDouble() - 0.5) * spread * 2;

        return new Location(playerLocation.getWorld(), x, y, z);
    }

    /**
     * Convierte nombres de colores a objetos Color
     * @param colorNames Lista de nombres de colores
     * @param min Mínimo número de colores
     * @param max Máximo número de colores
     * @return Lista de colores
     */
    private List<Color> getRandomColors(List<String> colorNames, int min, int max) {
        List<Color> colors = new ArrayList<>();

        if (colorNames.isEmpty()) {
            colors.add(Color.RED);
            return colors;
        }

        int amount = random.nextInt(max - min + 1) + min;

        for (int i = 0; i < amount; i++) {
            String colorName = colorNames.get(random.nextInt(colorNames.size()));
            Color color = getColorByName(colorName);
            if (color != null && !colors.contains(color)) {
                colors.add(color);
            }
        }

        if (colors.isEmpty()) {
            colors.add(Color.RED);
        }

        return colors;
    }

    /**
     * Obtiene un color por su nombre
     * @param colorName Nombre del color
     * @return El color correspondiente
     */
    private Color getColorByName(String colorName) {
        switch (colorName.toUpperCase()) {
            case "RED": return Color.RED;
            case "BLUE": return Color.BLUE;
            case "GREEN": return Color.GREEN;
            case "YELLOW": return Color.YELLOW;
            case "ORANGE": return Color.ORANGE;
            case "PURPLE": return Color.PURPLE;
            case "PINK": return Color.FUCHSIA;
            case "WHITE": return Color.WHITE;
            case "BLACK": return Color.BLACK;
            case "GRAY": return Color.GRAY;
            case "LIME": return Color.LIME;
            case "CYAN": return Color.AQUA;
            case "NAVY": return Color.NAVY;
            case "MAROON": return Color.MAROON;
            case "OLIVE": return Color.OLIVE;
            case "SILVER": return Color.SILVER;
            case "TEAL": return Color.TEAL;
            default: return Color.RED;
        }
    }

    /**
     * Lanza fuegos artificiales de prueba para un jugador
     * @param player El jugador
     */
    public void launchTestFireworks(Player player) {
        ColorUtils.sendMessage(player, "&a✨ &fLanzando fuegos artificiales de prueba...");
        launchFireworksSequence(player);
    }

    /**
     * Alterna el estado de los fuegos artificiales
     * @param enabled Nuevo estado
     */
    public void setFireworksEnabled(boolean enabled) {
        plugin.getConfigManager().getConfig().set("welcome.fireworks.enabled", enabled);
        plugin.getConfigManager().saveConfig();

        String status = enabled ? "&ahabilitados" : "&cdeshabilitados";
        plugin.getLogger().info(ColorUtils.translate(
                "&a✨ &fFuegos artificiales " + status
        ));
    }

    /**
     * Obtiene el estado actual de los fuegos artificiales
     * @return true si están habilitados
     */
    public boolean isEnabled() {
        return plugin.getConfigManager().isFireworksEnabled();
    }

    /**
     * Recarga la configuración del gestor
     */
    public void reload() {
        plugin.getLogger().info(ColorUtils.translate("&a✨ &fGestor de fuegos artificiales recargado"));
    }
}