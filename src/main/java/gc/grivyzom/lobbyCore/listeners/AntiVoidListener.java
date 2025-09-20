package gc.grivyzom.lobbyCore.listeners;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener para prevenir caídas al vacío - LOGGING OPTIMIZADO
 * Detecta cuando un jugador cae por debajo de cierta altura y lo teletransporta al spawn
 */
public class AntiVoidListener implements Listener {

    private final MainClass plugin;
    private final Map<UUID, Long> lastTeleportTime;
    private final Map<UUID, Boolean> playerFalling;
    private final Map<UUID, Integer> playerVoidFalls; // Contador de caídas por jugador

    // Configuración del anti-void
    private double voidHeight;
    private Location spawnLocation;
    private boolean enabled;
    private boolean soundEnabled;
    private String soundName;
    private float soundVolume;
    private float soundPitch;
    private boolean messageEnabled;
    private String teleportMessage;
    private long teleportCooldown;
    private boolean particlesEnabled;
    private boolean actionBarEnabled;
    private String actionBarMessage;

    public AntiVoidListener(MainClass plugin) {
        this.plugin = plugin;
        this.lastTeleportTime = new HashMap<>();
        this.playerFalling = new HashMap<>();
        this.playerVoidFalls = new HashMap<>();
        loadConfiguration();
    }

    /**
     * Carga la configuración del anti-void
     */
    public void loadConfiguration() {
        var config = plugin.getConfigManager().getConfig();

        this.enabled = config.getBoolean("anti-void.enabled", true);
        this.voidHeight = config.getDouble("anti-void.void-height", 0.0);
        this.soundEnabled = config.getBoolean("anti-void.sound.enabled", true);
        this.soundName = config.getString("anti-void.sound.sound", "ENTITY_ENDERMAN_TELEPORT");
        this.soundVolume = (float) config.getDouble("anti-void.sound.volume", 1.0);
        this.soundPitch = (float) config.getDouble("anti-void.sound.pitch", 1.0);
        this.messageEnabled = config.getBoolean("anti-void.message.enabled", true);
        this.teleportMessage = config.getString("anti-void.message.text",
                "&c⚠ &f¡Has sido salvado del vacío! Teletransportado al spawn.");
        this.teleportCooldown = config.getLong("anti-void.teleport-cooldown", 3000);
        this.particlesEnabled = config.getBoolean("anti-void.effects.particles", true);
        this.actionBarEnabled = config.getBoolean("anti-void.effects.action-bar.enabled", true);
        this.actionBarMessage = config.getString("anti-void.effects.action-bar.message",
                "&c⚠ ¡Cuidado! Te estás acercando al vacío...");

        // Cargar ubicación del spawn
        loadSpawnLocation();
    }

    /**
     * Carga la ubicación del spawn desde la configuración
     */
    private void loadSpawnLocation() {
        var config = plugin.getConfigManager().getConfig();

        if (config.contains("anti-void.spawn-location.world")) {
            try {
                String worldName = config.getString("anti-void.spawn-location.world");
                double x = config.getDouble("anti-void.spawn-location.x");
                double y = config.getDouble("anti-void.spawn-location.y");
                double z = config.getDouble("anti-void.spawn-location.z");
                float yaw = (float) config.getDouble("anti-void.spawn-location.yaw", 0.0);
                float pitch = (float) config.getDouble("anti-void.spawn-location.pitch", 0.0);

                org.bukkit.World world = plugin.getServer().getWorld(worldName);
                if (world != null) {
                    this.spawnLocation = new Location(world, x, y, z, yaw, pitch);
                } else {
                    plugin.getLogger().warning("Mundo del spawn anti-void no encontrado: " + worldName);
                    this.spawnLocation = null;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error cargando spawn de anti-void: " + e.getMessage());
                this.spawnLocation = null;
            }
        } else {
            // Usar spawn por defecto del mundo
            this.spawnLocation = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
    }

    /**
     * Maneja el movimiento del jugador para detectar caídas al vacío
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled || spawnLocation == null) return;

        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to == null) return;

        UUID playerId = player.getUniqueId();
        double playerY = to.getY();

        // Verificar si el jugador está en el mismo mundo que el spawn
        if (!to.getWorld().equals(spawnLocation.getWorld())) {
            return;
        }

        // Verificar cooldown de teletransporte
        long currentTime = System.currentTimeMillis();
        if (lastTeleportTime.containsKey(playerId)) {
            long lastTeleport = lastTeleportTime.get(playerId);
            if (currentTime - lastTeleport < teleportCooldown) {
                return; // Aún en cooldown
            }
        }

        // Detectar proximidad al vacío (warning) - SIN LOG REPETITIVO
        double warningHeight = voidHeight + 10;
        if (playerY <= warningHeight && playerY > voidHeight) {
            if (!playerFalling.getOrDefault(playerId, false)) {
                playerFalling.put(playerId, true);
                sendVoidWarning(player);
            }
        } else if (playerY > warningHeight) {
            playerFalling.remove(playerId);
        }

        // Detectar caída al vacío
        if (playerY <= voidHeight) {
            event.setCancelled(true);
            teleportPlayerToSafety(player);
        }
    }

    /**
     * Envía una advertencia al jugador cuando se acerca al vacío (SIN LOG)
     */
    private void sendVoidWarning(Player player) {
        if (actionBarEnabled) {
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || count >= 3) {
                        cancel();
                        return;
                    }

                    String message = actionBarMessage.replace("{PLAYER}", player.getName());
                    player.sendActionBar(ColorUtils.translate(message));
                    count++;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        // Sonido de advertencia (más suave) - SIN LOG
        if (soundEnabled) {
            try {
                Sound warningSound = Sound.valueOf("BLOCK_NOTE_BLOCK_PLING");
                player.playSound(player.getLocation(), warningSound, soundVolume * 0.5f, 0.5f);
            } catch (IllegalArgumentException e) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
            }
        }
    }

    /**
     * Teletransporta al jugador a seguridad - SOLO LOG IMPORTANTE
     */
    private void teleportPlayerToSafety(Player player) {
        UUID playerId = player.getUniqueId();

        // Actualizar tiempo del último teletransporte
        lastTeleportTime.put(playerId, System.currentTimeMillis());
        playerFalling.remove(playerId);

        // Contar caídas para detectar actividad sospechosa
        int fallCount = playerVoidFalls.getOrDefault(playerId, 0) + 1;
        playerVoidFalls.put(playerId, fallCount);

        // Teletransportar al jugador
        player.teleport(spawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

        // Efectos de teletransporte (sin log)
        playTeleportEffects(player);

        // Mensaje de teletransporte
        if (messageEnabled) {
            String message = teleportMessage.replace("{PLAYER}", player.getName());
            ColorUtils.sendMessage(player, message);
        }

        // LOG SOLO PARA EVENTOS IMPORTANTES
        // 1. Primer salvamento del jugador
        // 2. Actividad sospechosa (muchas caídas)
        // 3. Caídas desde gran altura (posible griefing)

        boolean shouldLog = false;
        String logMessage = "";

        if (fallCount == 1) {
            // Primera caída - log normal
            shouldLog = true;
            logMessage = "&c🚨 &fJugador &b" + player.getName() + " &fsalvado del vacío (Y=" +
                    String.format("%.1f", player.getLocation().getY()) + ")";
        } else if (fallCount >= 5) {
            // Actividad sospechosa - log de advertencia
            shouldLog = true;
            logMessage = "&c⚠ &fACTIVIDAD SOSPECHOSA: &b" + player.getName() +
                    " &fha caído al vacío &e" + fallCount + " &fveces";
        } else if (fallCount % 10 == 0) {
            // Cada 10 caídas - posible spam/griefing
            shouldLog = true;
            logMessage = "&c🚨 &fPOSIBLE GRIEFING: &b" + player.getName() +
                    " &fha caído al vacío &c" + fallCount + " &fveces";
        }

        if (shouldLog) {
            plugin.getLogger().info(ColorUtils.translate(logMessage));
        }

        // Enviar datos de evento a GrivyzomCore solo para eventos importantes
        if (plugin.getGrivyzomIntegration() != null &&
                plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable() &&
                (fallCount == 1 || fallCount >= 5)) {
            plugin.getGrivyzomIntegration().notifyLobbyEvent(
                    "VOID_SAVE",
                    player.getName(),
                    String.valueOf(player.getLocation().getY()),
                    spawnLocation.getWorld().getName(),
                    "falls:" + fallCount
            );
        }

        // Notificar a administradores sobre actividad sospechosa
        if (fallCount >= 5) {
            notifyAdminsAboutSuspiciousActivity(player, fallCount);
        }

        // Limpiar contador después de un tiempo sin caídas
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    // Reducir contador gradualmente si no ha caído recientemente
                    int currentFalls = playerVoidFalls.getOrDefault(playerId, 0);
                    if (currentFalls > 0) {
                        playerVoidFalls.put(playerId, Math.max(0, currentFalls - 1));
                    }
                }
            }
        }.runTaskLater(plugin, 6000L); // 5 minutos después
    }

    /**
     * Notifica a administradores sobre actividad sospechosa
     */
    private void notifyAdminsAboutSuspiciousActivity(Player player, int fallCount) {
        String permission = plugin.getConfigManager().getAntiVoidNotificationPermission();
        String notification = "&c🚨 &fActividad sospechosa: &b" + player.getName() +
                " &fha caído al vacío &e" + fallCount + " &fveces en poco tiempo";

        for (Player admin : plugin.getServer().getOnlinePlayers()) {
            if (admin.hasPermission(permission) && !admin.equals(player)) {
                ColorUtils.sendMessage(admin, notification);
            }
        }
    }

    /**
     * Reproduce efectos de teletransporte (SIN LOG)
     */
    private void playTeleportEffects(Player player) {
        // Sonido de teletransporte
        if (soundEnabled) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, soundVolume, soundPitch);
            } catch (IllegalArgumentException e) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, soundVolume, soundPitch);
            }
        }

        // Partículas de teletransporte
        if (particlesEnabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;

                    Location loc = player.getLocation();

                    // Partículas de portal
                    loc.getWorld().spawnParticle(
                            org.bukkit.Particle.PORTAL,
                            loc.add(0, 1, 0),
                            30, 0.5, 1.0, 0.5, 0.1
                    );

                    // Partículas de end rod para efecto de rescate
                    loc.getWorld().spawnParticle(
                            org.bukkit.Particle.END_ROD,
                            loc, 10, 0.3, 0.5, 0.3, 0.05
                    );
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    /**
     * Establece una nueva ubicación de spawn para el anti-void - LOG ADMIN
     */
    public boolean setSpawnLocation(Location location) {
        if (location == null) return false;

        this.spawnLocation = location.clone();

        // Guardar en configuración
        var config = plugin.getConfigManager().getConfig();
        config.set("anti-void.spawn-location.world", location.getWorld().getName());
        config.set("anti-void.spawn-location.x", location.getX());
        config.set("anti-void.spawn-location.y", location.getY());
        config.set("anti-void.spawn-location.z", location.getZ());
        config.set("anti-void.spawn-location.yaw", location.getYaw());
        config.set("anti-void.spawn-location.pitch", location.getPitch());

        plugin.getConfigManager().saveConfig();

        // LOG PARA ACCIÓN ADMINISTRATIVA - SIEMPRE MOSTRAR
        plugin.getLogger().info(ColorUtils.translate(
                "&a✓ &fAdmin estableció spawn anti-void en: &b" +
                        String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ())
        ));

        return true;
    }

    /**
     * Establece la altura del vacío - LOG ADMIN
     */
    public void setVoidHeight(double height) {
        this.voidHeight = height;

        var config = plugin.getConfigManager().getConfig();
        config.set("anti-void.void-height", height);
        plugin.getConfigManager().saveConfig();

        // LOG PARA ACCIÓN ADMINISTRATIVA - SIEMPRE MOSTRAR
        plugin.getLogger().info(ColorUtils.translate(
                "&a✓ &fAdmin estableció altura del vacío en: &e" + height
        ));
    }

    /**
     * Alterna el estado del anti-void - LOG ADMIN
     */
    public void toggleEnabled() {
        this.enabled = !this.enabled;

        var config = plugin.getConfigManager().getConfig();
        config.set("anti-void.enabled", this.enabled);
        plugin.getConfigManager().saveConfig();

        // LOG PARA ACCIÓN ADMINISTRATIVA - SIEMPRE MOSTRAR
        String status = enabled ? "&ahabilitado" : "&cdeshabilitado";
        plugin.getLogger().info(ColorUtils.translate("&c🚨 &fAdmin cambió anti-void a " + status));
    }

    /**
     * Obtiene estadísticas del anti-void
     */
    public AntiVoidStats getStats() {
        int playersInCooldown = lastTeleportTime.size();
        int playersFalling = playerFalling.size();

        return new AntiVoidStats(
                enabled,
                voidHeight,
                spawnLocation != null,
                playersInCooldown,
                playersFalling,
                teleportCooldown
        );
    }

    /**
     * Recarga la configuración - LOG ADMIN
     */
    public void reload() {
        lastTeleportTime.clear();
        playerFalling.clear();
        // NO limpiar playerVoidFalls para mantener estadísticas de actividad sospechosa

        loadConfiguration();

        // LOG PARA ACCIÓN ADMINISTRATIVA - SIEMPRE MOSTRAR
        plugin.getLogger().info(ColorUtils.translate("&c🚨 &fSistema Anti-void recargado por admin"));
    }

    // Getters
    public boolean isEnabled() { return enabled; }
    public double getVoidHeight() { return voidHeight; }
    public Location getSpawnLocation() { return spawnLocation; }

    /**
     * Obtiene el número de caídas de un jugador (para estadísticas)
     */
    public int getPlayerVoidFalls(UUID playerId) {
        return playerVoidFalls.getOrDefault(playerId, 0);
    }

    /**
     * Clase para estadísticas del anti-void
     */
    public static class AntiVoidStats {
        private final boolean enabled;
        private final double voidHeight;
        private final boolean spawnConfigured;
        private final int playersInCooldown;
        private final int playersFalling;
        private final long cooldownMs;

        public AntiVoidStats(boolean enabled, double voidHeight, boolean spawnConfigured,
                             int playersInCooldown, int playersFalling, long cooldownMs) {
            this.enabled = enabled;
            this.voidHeight = voidHeight;
            this.spawnConfigured = spawnConfigured;
            this.playersInCooldown = playersInCooldown;
            this.playersFalling = playersFalling;
            this.cooldownMs = cooldownMs;
        }

        // Getters
        public boolean isEnabled() { return enabled; }
        public double getVoidHeight() { return voidHeight; }
        public boolean isSpawnConfigured() { return spawnConfigured; }
        public int getPlayersInCooldown() { return playersInCooldown; }
        public int getPlayersFalling() { return playersFalling; }
        public long getCooldownMs() { return cooldownMs; }

        @Override
        public String toString() {
            return String.format(
                    "AntiVoidStats{enabled=%s, height=%.1f, spawn=%s, cooldown=%d, falling=%d}",
                    enabled, voidHeight, spawnConfigured, playersInCooldown, playersFalling
            );
        }
    }
}