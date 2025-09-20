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
 * Listener para prevenir caídas al vacío - VERSIÓN CORREGIDA
 * Detecta cuando un jugador cae por debajo de cierta altura y lo teletransporta al spawn
 */
public class AntiVoidListener implements Listener {

    private final MainClass plugin;
    private final Map<UUID, Long> lastTeleportTime;
    private final Map<UUID, Boolean> playerFalling;

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
        this.soundName = config.getString("anti-void.sound.sound", "ENTITY_CHICKEN_EGG"); // Cambiado a chicken egg
        this.soundVolume = (float) config.getDouble("anti-void.sound.volume", 1.0);
        this.soundPitch = (float) config.getDouble("anti-void.sound.pitch", 1.0);
        this.messageEnabled = config.getBoolean("anti-void.message.enabled", true);
        this.teleportMessage = config.getString("anti-void.message.text",
                "&c⚠ &f¡Has sido salvado del vacío! Teletransportado al spawn.");
        this.teleportCooldown = config.getLong("anti-void.teleport-cooldown", 3000); // 3 segundos
        this.particlesEnabled = config.getBoolean("anti-void.effects.particles", true);
        this.actionBarEnabled = config.getBoolean("anti-void.effects.action-bar.enabled", true);
        this.actionBarMessage = config.getString("anti-void.effects.action-bar.message",
                "&c⚠ ¡Cuidado! Te estás acercando al vacío...");

        // Cargar ubicación del spawn
        loadSpawnLocation();

        plugin.getLogger().info(ColorUtils.translate(
                "&a✓ &fAnti-void configurado: altura=" + voidHeight + ", spawn=" +
                        (spawnLocation != null ? "configurado" : "no configurado")));
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
                    plugin.getLogger().info(ColorUtils.translate(
                            "&a✓ &fSpawn de anti-void cargado: X=" + x + ", Y=" + y + ", Z=" + z + " en mundo: &b" + worldName));
                } else {
                    plugin.getLogger().warning(ColorUtils.translate(
                            "&e⚠ &fMundo del spawn no encontrado: &c" + worldName));
                    this.spawnLocation = null;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error cargando spawn de anti-void: " + e.getMessage());
                this.spawnLocation = null;
            }
        } else {
            plugin.getLogger().warning(ColorUtils.translate(
                    "&e⚠ &fNo hay spawn configurado para anti-void. Usa /lobbycore antivoid setspawn"));
            this.spawnLocation = null;
        }
    }

    /**
     * Maneja el movimiento del jugador para detectar caídas al vacío
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled) {
            return;
        }

        if (spawnLocation == null) {
            plugin.getLogger().warning("AntiVoid: No hay spawn configurado");
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to == null) return;

        UUID playerId = player.getUniqueId();
        double playerY = to.getY();

        // Debug: Log de la posición del jugador si debug está habilitado
        if (plugin.getConfigManager().getConfig().getBoolean("anti-void-debug.enabled", false)) {
            plugin.getLogger().info("AntiVoid Debug: " + player.getName() + " en Y=" + playerY +
                    ", límite=" + voidHeight + ", mundo=" + to.getWorld().getName());
        }

        // Verificar si el jugador está en un mundo donde el anti-void está habilitado
        if (!isAntiVoidEnabledInWorld(to.getWorld().getName())) {
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

        // Detectar proximidad al vacío (warning)
        double warningHeight = voidHeight + 10; // 10 bloques antes del límite
        if (playerY <= warningHeight && playerY > voidHeight) {
            if (!playerFalling.getOrDefault(playerId, false)) {
                playerFalling.put(playerId, true);
                sendVoidWarning(player);
            }
        } else if (playerY > warningHeight) {
            playerFalling.remove(playerId);
        }

        // Detectar caída al vacío - ESTA ES LA PARTE CRÍTICA
        if (playerY <= voidHeight) {
            plugin.getLogger().info(ColorUtils.translate(
                    "&c🚨 &fJugador &b" + player.getName() + " &fha caído al vacío (Y=" + playerY +
                            "). Teletransportando al spawn..."
            ));

            // Cancelar el movimiento
            event.setCancelled(true);

            // Teletransportar al jugador de inmediato
            teleportPlayerToSafety(player);
        }
    }

    /**
     * Verifica si el anti-void está habilitado en un mundo específico
     */
    private boolean isAntiVoidEnabledInWorld(String worldName) {
        var config = plugin.getConfigManager().getConfig();

        // Verificar mundos deshabilitados
        if (config.getStringList("anti-void.advanced.detection.disabled-worlds").contains(worldName)) {
            return false;
        }

        // Verificar mundos activos específicos
        var activeWorlds = config.getStringList("anti-void.advanced.detection.active-worlds");
        if (!activeWorlds.isEmpty()) {
            return activeWorlds.contains(worldName);
        }

        return true; // Habilitado por defecto
    }

    /**
     * Envía una advertencia al jugador cuando se acerca al vacío
     */
    private void sendVoidWarning(Player player) {
        if (actionBarEnabled) {
            // Enviar mensaje en action bar
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
            }.runTaskTimer(plugin, 0L, 20L); // Cada segundo, 3 veces
        }

        // Sonido de advertencia (más suave)
        if (soundEnabled) {
            try {
                Sound warningSound = Sound.valueOf("BLOCK_NOTE_BLOCK_PLING");
                player.playSound(player.getLocation(), warningSound, soundVolume * 0.5f, 0.5f);
            } catch (IllegalArgumentException e) {
                // Sonido por defecto si no se encuentra
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
            }
        }
    }

    /**
     * Teletransporta al jugador a seguridad - MÉTODO CORREGIDO
     */
    private void teleportPlayerToSafety(Player player) {
        UUID playerId = player.getUniqueId();

        // Actualizar tiempo del último teletransporte
        lastTeleportTime.put(playerId, System.currentTimeMillis());
        playerFalling.remove(playerId);

        // Verificar que el spawn location no sea null
        if (spawnLocation == null) {
            plugin.getLogger().severe("Error: No hay spawn configurado para anti-void!");
            ColorUtils.sendMessage(player, "&c❌ &fError: No hay spawn configurado para el anti-void.");
            return;
        }

        // Verificar que el mundo del spawn exista
        if (spawnLocation.getWorld() == null) {
            plugin.getLogger().severe("Error: El mundo del spawn anti-void no existe!");
            ColorUtils.sendMessage(player, "&c❌ &fError: El mundo del spawn no existe.");
            return;
        }

        // Teletransportar al jugador SÍNCRONAMENTE
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Teletransporte seguro
                    boolean success = player.teleport(spawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

                    if (success) {
                        plugin.getLogger().info(ColorUtils.translate(
                                "&a✅ &fJugador &b" + player.getName() + " &fteletransportado exitosamente al spawn anti-void"
                        ));

                        // Efectos de teletransporte
                        playTeleportEffects(player);

                        // Mensaje de teletransporte
                        if (messageEnabled) {
                            String message = teleportMessage.replace("{PLAYER}", player.getName());
                            ColorUtils.sendMessage(player, message);
                        }

                        // Log del evento
                        plugin.getLogger().info(ColorUtils.translate(
                                "&c🚨 &fJugador &b" + player.getName() + " &fsalvado del vacío y teletransportado al spawn"
                        ));

                        // Enviar datos de evento a GrivyzomCore si está disponible
                        if (plugin.getGrivyzomIntegration() != null &&
                                plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()) {
                            plugin.getGrivyzomIntegration().notifyLobbyEvent(
                                    "VOID_SAVE",
                                    player.getName(),
                                    String.valueOf(voidHeight),
                                    spawnLocation.getWorld().getName()
                            );
                        }
                    } else {
                        plugin.getLogger().severe("Error: Fallo el teletransporte de " + player.getName());
                        ColorUtils.sendMessage(player, "&c❌ &fError al teletransportar. Contacta a un administrador.");
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Error crítico teletransportando " + player.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTask(plugin); // Ejecutar en el hilo principal
    }

    /**
     * Reproduce efectos de teletransporte - MÉTODO CORREGIDO
     */
    private void playTeleportEffects(Player player) {
        // Sonido de teletransporte - CAMBIADO A CHICKEN EGG
        if (soundEnabled) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, soundVolume, soundPitch);
                plugin.getLogger().info("Reproduciendo sonido: " + soundName + " para " + player.getName());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Sonido de anti-void inválido: " + soundName + ". Usando sonido por defecto.");
                // Sonido por defecto: chicken egg
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, soundVolume, soundPitch);
            }
        }

        // Partículas de teletransporte
        if (particlesEnabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;

                    Location loc = player.getLocation();

                    try {
                        // Partículas de portal
                        loc.getWorld().spawnParticle(
                                org.bukkit.Particle.PORTAL,
                                loc.add(0, 1, 0),
                                30,  // cantidad
                                0.5, // offset X
                                1.0, // offset Y
                                0.5, // offset Z
                                0.1  // velocidad
                        );

                        // Partículas de end rod para efecto de rescate
                        loc.getWorld().spawnParticle(
                                org.bukkit.Particle.END_ROD,
                                loc,
                                10,
                                0.3,
                                0.5,
                                0.3,
                                0.05
                        );
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error reproduciendo partículas: " + e.getMessage());
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    /**
     * Establece una nueva ubicación de spawn para el anti-void
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

        plugin.getLogger().info(ColorUtils.translate(
                "&a✓ &fNueva ubicación de spawn para anti-void establecida en: &b" +
                        String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ()) +
                        " &fen mundo: &e" + location.getWorld().getName()
        ));

        return true;
    }

    /**
     * Establece la altura del vacío
     */
    public void setVoidHeight(double height) {
        this.voidHeight = height;

        var config = plugin.getConfigManager().getConfig();
        config.set("anti-void.void-height", height);
        plugin.getConfigManager().saveConfig();

        plugin.getLogger().info(ColorUtils.translate(
                "&a✓ &fAltura del vacío establecida en: &e" + height
        ));
    }

    /**
     * Alterna el estado del anti-void
     */
    public void toggleEnabled() {
        this.enabled = !this.enabled;

        var config = plugin.getConfigManager().getConfig();
        config.set("anti-void.enabled", this.enabled);
        plugin.getConfigManager().saveConfig();

        String status = enabled ? "&ahabilitado" : "&cdeshabilitado";
        plugin.getLogger().info(ColorUtils.translate("&c🚨 &fAnti-void " + status));
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
     * Recarga la configuración
     */
    public void reload() {
        lastTeleportTime.clear();
        playerFalling.clear();
        loadConfiguration();
        plugin.getLogger().info(ColorUtils.translate("&c🚨 &fSistema Anti-void recargado"));
    }

    // Getters
    public boolean isEnabled() { return enabled; }
    public double getVoidHeight() { return voidHeight; }
    public Location getSpawnLocation() { return spawnLocation; }

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