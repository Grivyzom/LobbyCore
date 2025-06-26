package gc.grivyzom.lobbyCore.integration;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Clase para integrar LobbyCore con GrivyzomCore
 * Obtiene datos EN TIEMPO REAL del network
 */
public class GrivyzomCoreIntegration implements Listener {

    private final MainClass plugin;
    private boolean grivyzomCoreAvailable = false;
    private long lastStatsRequest = 0;
    private long lastPingTime = 0;

    // Canales de comunicación con GrivyzomCore
    private static final String GRIVYZOM_CHANNEL = "grivyzom:core";
    private static final String ECONOMY_CHANNEL = "grivyzom:economy";
    private static final String RANKUP_CHANNEL = "grivyzom:rankup";
    private static final String PVP_CHANNEL = "grivyzom:pvp";

    // Constantes para control de frecuencia
    private static final long STATS_REQUEST_COOLDOWN = 5000; // 5 segundos entre solicitudes
    private static final long PING_INTERVAL = 30000; // 30 segundos entre pings

    public GrivyzomCoreIntegration(MainClass plugin) {
        this.plugin = plugin;
        setupIntegration();
    }

    /**
     * Configura la integración con GrivyzomCore
     */
    private void setupIntegration() {
        try {
            // Registrar canales de comunicación salientes
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, GRIVYZOM_CHANNEL);
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, ECONOMY_CHANNEL);
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, RANKUP_CHANNEL);
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, PVP_CHANNEL);

            // Registrar listener para eventos
            plugin.getServer().getPluginManager().registerEvents(this, plugin);

            plugin.getLogger().info(ColorUtils.translate("&a✓ &fIntegración con GrivyzomCore configurada"));
            plugin.getLogger().info(ColorUtils.translate("&e📡 &fCanales registrados: &b4 canales activos"));

            // Iniciar monitoreo automático
            startAutomaticMonitoring();

            // Verificar si GrivyzomCore está disponible
            checkGrivyzomCoreAvailability();

        } catch (Exception e) {
            plugin.getLogger().warning("Error configurando integración con GrivyzomCore: " + e.getMessage());
        }
    }

    /**
     * Inicia el monitoreo automático de GrivyzomCore
     */
    private void startAutomaticMonitoring() {
        // Ping automático cada 30 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPingTime >= PING_INTERVAL) {
                    sendPingToGrivyzomCore();
                    lastPingTime = currentTime;
                }
            }
        }.runTaskTimerAsynchronously(plugin, 60L, 600L); // Después de 3s, cada 30s

        // Solicitud automática de estadísticas cada 15 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                if (grivyzomCoreAvailable) {
                    requestNetworkStatsIfNeeded();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 100L, 300L); // Después de 5s, cada 15s

        plugin.getLogger().info(ColorUtils.translate("&a✓ &fMonitoreo automático iniciado"));
    }

    /**
     * Verifica si GrivyzomCore está disponible en el proxy
     */
    private void checkGrivyzomCoreAvailability() {
        // Hacer ping a GrivyzomCore para verificar disponibilidad
        new BukkitRunnable() {
            @Override
            public void run() {
                sendPingToGrivyzomCore();
            }
        }.runTaskLater(plugin, 40L); // 2 segundos después del inicio
    }

    /**
     * Envía un ping a GrivyzomCore para verificar conexión
     */
    public void sendPingToGrivyzomCore() {
        try {
            if (!canSendMessage()) {
                plugin.getLogger().warning(ColorUtils.translate("&e⚠ &fNo hay jugadores conectados para enviar ping a GrivyzomCore"));
                scheduleRetryPing();
                return;
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PING");
            out.writeUTF("LOBBY_CORE");
            out.writeLong(System.currentTimeMillis());

            Player anyPlayer = getAnyPlayer();
            anyPlayer.sendPluginMessage(plugin, GRIVYZOM_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate("&e📡 &fPing enviado a GrivyzomCore..."));
            lastPingTime = System.currentTimeMillis();

        } catch (Exception e) {
            plugin.getLogger().warning("Error enviando ping a GrivyzomCore: " + e.getMessage());
            grivyzomCoreAvailable = false;
        }
    }

    /**
     * Programa un reintento de ping cuando no hay jugadores
     */
    private void scheduleRetryPing() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (canSendMessage()) {
                    sendPingToGrivyzomCore();
                } else if (this.isCancelled()) {
                    return;
                } else {
                    // Reintentar en 10 segundos
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            scheduleRetryPing();
                        }
                    }.runTaskLater(plugin, 200L);
                }
            }
        }.runTaskLater(plugin, 200L); // 10 segundos
    }

    /**
     * Notifica a GrivyzomCore cuando un jugador se conecta al lobby
     */
    @EventHandler
    public void onPlayerJoinLobby(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay para asegurar que el jugador esté completamente conectado
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    notifyPlayerJoinedLobby(player);
                    requestPlayerData(player);

                    // Si es el primer jugador y no teníamos conexión, intentar ping
                    if (!grivyzomCoreAvailable) {
                        sendPingToGrivyzomCore();
                    }
                }
            }
        }.runTaskLater(plugin, 20L); // 1 segundo después
    }

    /**
     * Notifica a GrivyzomCore que un jugador se unió al lobby
     */
    public void notifyPlayerJoinedLobby(Player player) {
        try {
            if (!canSendMessage()) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PLAYER_JOINED_LOBBY");
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(player.getName());
            out.writeUTF(getServerName());
            out.writeLong(System.currentTimeMillis());

            player.sendPluginMessage(plugin, GRIVYZOM_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate(
                    "&a📨 &fNotificado a GrivyzomCore: " + player.getName() + " se unió al lobby"));

        } catch (Exception e) {
            plugin.getLogger().warning("Error notificando conexión a lobby: " + e.getMessage());
        }
    }

    /**
     * Solicita estadísticas del network solo si es necesario
     */
    private void requestNetworkStatsIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastStatsRequest >= STATS_REQUEST_COOLDOWN) {
            requestNetworkStats();
            lastStatsRequest = currentTime;
        }
    }

    /**
     * Solicita estadísticas del network a GrivyzomCore EN TIEMPO REAL
     */
    public void requestNetworkStats() {
        try {
            if (!canSendMessage() || !grivyzomCoreAvailable) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("REQUEST_NETWORK_STATS");
            out.writeLong(System.currentTimeMillis());

            Player anyPlayer = getAnyPlayer();
            anyPlayer.sendPluginMessage(plugin, GRIVYZOM_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate("&e📈 &fSolicitando estadísticas del network"));

        } catch (Exception e) {
            plugin.getLogger().warning("Error solicitando estadísticas: " + e.getMessage());
        }
    }

    /**
     * Solicita datos del jugador a GrivyzomCore
     */
    public void requestPlayerData(Player player) {
        try {
            if (!canSendMessage() || !grivyzomCoreAvailable) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("REQUEST_PLAYER_DATA");
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF("COINS,GEMS,RANK,LEVEL,PLAYTIME"); // Datos que queremos obtener

            player.sendPluginMessage(plugin, GRIVYZOM_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate(
                    "&e📊 &fSolicitando datos de " + player.getName() + " a GrivyzomCore"));

        } catch (Exception e) {
            plugin.getLogger().warning("Error solicitando datos del jugador: " + e.getMessage());
        }
    }

    /**
     * Actualiza las monedas del jugador en GrivyzomCore
     */
    public void updatePlayerCoins(Player player, double newAmount) {
        try {
            if (!canSendMessage() || !grivyzomCoreAvailable) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UPDATE_PLAYER_COINS");
            out.writeUTF(player.getUniqueId().toString());
            out.writeDouble(newAmount);

            player.sendPluginMessage(plugin, ECONOMY_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate(
                    "&e💰 &fActualizando monedas de " + player.getName() + " a " + newAmount));

        } catch (Exception e) {
            plugin.getLogger().warning("Error actualizando monedas: " + e.getMessage());
        }
    }

    /**
     * Actualiza las gemas del jugador en GrivyzomCore
     */
    public void updatePlayerGems(Player player, int newAmount) {
        try {
            if (!canSendMessage() || !grivyzomCoreAvailable) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UPDATE_PLAYER_GEMS");
            out.writeUTF(player.getUniqueId().toString());
            out.writeInt(newAmount);

            player.sendPluginMessage(plugin, ECONOMY_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate(
                    "&d💎 &fActualizando gemas de " + player.getName() + " a " + newAmount));

        } catch (Exception e) {
            plugin.getLogger().warning("Error actualizando gemas: " + e.getMessage());
        }
    }

    /**
     * Solicita el top de jugadores por monedas
     */
    public void requestTopPlayers(int limit) {
        requestTopPlayers("COINS", limit);
    }

    /**
     * Solicita el top de jugadores por tipo específico
     */
    public void requestTopPlayers(String type, int limit) {
        try {
            if (!canSendMessage() || !grivyzomCoreAvailable) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("REQUEST_TOP_PLAYERS");
            out.writeUTF(type.toUpperCase());
            out.writeInt(limit);

            Player anyPlayer = getAnyPlayer();
            anyPlayer.sendPluginMessage(plugin, ECONOMY_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate(
                    "&e🏆 &fSolicitando top " + limit + " jugadores por " + type));

        } catch (Exception e) {
            plugin.getLogger().warning("Error solicitando top de jugadores: " + e.getMessage());
        }
    }

    /**
     * Notifica a GrivyzomCore sobre un evento especial del lobby
     */
    public void notifyLobbyEvent(String eventType, String... data) {
        try {
            if (!canSendMessage() || !grivyzomCoreAvailable) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("LOBBY_EVENT");
            out.writeUTF(eventType);
            out.writeInt(data.length);
            for (String datum : data) {
                out.writeUTF(datum);
            }

            Player anyPlayer = getAnyPlayer();
            anyPlayer.sendPluginMessage(plugin, GRIVYZOM_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate(
                    "&a🎉 &fEvento de lobby notificado: " + eventType));

        } catch (Exception e) {
            plugin.getLogger().warning("Error notificando evento de lobby: " + e.getMessage());
        }
    }

    /**
     * Actualiza el rango de un jugador
     */
    public void updatePlayerRank(Player player, String newRank) {
        try {
            if (!canSendMessage() || !grivyzomCoreAvailable) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UPDATE_PLAYER_RANK");
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(newRank);

            player.sendPluginMessage(plugin, RANKUP_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate(
                    "&b🏅 &fActualizando rango de " + player.getName() + " a " + newRank));

        } catch (Exception e) {
            plugin.getLogger().warning("Error actualizando rango: " + e.getMessage());
        }
    }

    /**
     * Registra estadísticas de PvP
     */
    public void updatePvpStats(Player player, String statType, int value) {
        try {
            if (!canSendMessage() || !grivyzomCoreAvailable) return;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UPDATE_PVP_STATS");
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(statType.toUpperCase());
            out.writeInt(value);

            player.sendPluginMessage(plugin, PVP_CHANNEL, out.toByteArray());

            plugin.getLogger().info(ColorUtils.translate(
                    "&c⚔ &fActualizando stats PvP de " + player.getName() + ": " + statType + " = " + value));

        } catch (Exception e) {
            plugin.getLogger().warning("Error actualizando stats PvP: " + e.getMessage());
        }
    }

    /**
     * Obtiene el número real de jugadores conectados en el network
     */
    public int getRealNetworkPlayerCount() {
        // Si tenemos conexión con GrivyzomCore, usar los datos del response handler
        if (grivyzomCoreAvailable && plugin.getResponseHandler() != null) {
            String networkPlayers = plugin.getResponseHandler().getNetworkData("players");
            if (networkPlayers != null && !networkPlayers.equals("127")) {
                try {
                    return Integer.parseInt(networkPlayers);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Datos de network inválidos: " + networkPlayers);
                }
            }
        }

        // Fallback: usar jugadores del servidor actual como mínimo
        return Bukkit.getOnlinePlayers().size();
    }

    /**
     * Obtiene el número real de servidores activos en el network
     */
    public int getRealNetworkServerCount() {
        // Si tenemos conexión con GrivyzomCore, usar los datos del response handler
        if (grivyzomCoreAvailable && plugin.getResponseHandler() != null) {
            String networkServers = plugin.getResponseHandler().getNetworkData("servers");
            if (networkServers != null && !networkServers.equals("5")) {
                try {
                    return Integer.parseInt(networkServers);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Datos de servidores inválidos: " + networkServers);
                }
            }
        }

        // Fallback: contar desde configuración local
        return getConfiguredServerCount();
    }

    /**
     * Cuenta servidores desde la configuración local
     */
    private int getConfiguredServerCount() {
        try {
            var serversSection = plugin.getConfigManager().getConfig().getConfigurationSection("servers");
            if (serversSection != null) {
                return serversSection.getKeys(false).size();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error contando servidores desde config: " + e.getMessage());
        }
        return 1; // Al menos este servidor existe
    }

    /**
     * Obtener el nombre del servidor de forma inteligente
     */
    public String getServerName() {
        try {
            // Método 1: Desde configuración específica
            String configName = plugin.getConfigManager().getConfig().getString("server.name");
            if (configName != null && !configName.isEmpty()) {
                return configName;
            }

            // Método 2: Desde MOTD limpio
            String motd = Bukkit.getServer().getMotd();
            if (motd != null && !motd.isEmpty()) {
                String cleanMotd = motd.replaceAll("§[0-9a-fk-or]", "").trim();
                if (!cleanMotd.isEmpty() && !cleanMotd.toLowerCase().contains("server")) {
                    return cleanMotd;
                }
            }

            // Método 3: Basado en puerto
            int port = Bukkit.getServer().getPort();
            return switch (port) {
                case 25565 -> "Hub";
                case 25566 -> "Survival";
                case 25567 -> "SkyBlock";
                case 25568 -> "Minigames";
                case 25569 -> "Creative";
                default -> "Lobby-" + port;
            };

        } catch (Exception e) {
            plugin.getLogger().warning("Error obteniendo nombre del servidor: " + e.getMessage());
            return "Lobby";
        }
    }

    /**
     * Verifica si hay jugadores para enviar mensajes
     */
    private boolean canSendMessage() {
        return !plugin.getServer().getOnlinePlayers().isEmpty();
    }

    /**
     * Obtiene un jugador cualquiera para enviar mensajes
     */
    private Player getAnyPlayer() {
        return plugin.getServer().getOnlinePlayers().iterator().next();
    }

    /**
     * Fuerza una reconexión con GrivyzomCore
     */
    public void forceReconnect() {
        plugin.getLogger().info(ColorUtils.translate("&e🔄 &fForzando reconexión con GrivyzomCore..."));

        setGrivyzomCoreAvailable(false);

        // Limpiar cache de respuestas
        if (plugin.getResponseHandler() != null) {
            plugin.getResponseHandler().clearCache();
        }

        // Reintento después de 3 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                sendPingToGrivyzomCore();
            }
        }.runTaskLater(plugin, 60L);
    }

    /**
     * Obtiene estadísticas detalladas de la integración
     */
    public IntegrationStats getIntegrationStats() {
        return new IntegrationStats(
                grivyzomCoreAvailable,
                canSendMessage(),
                plugin.getServer().getOnlinePlayers().size(),
                getRealNetworkPlayerCount(),
                getRealNetworkServerCount(),
                System.currentTimeMillis() - lastStatsRequest,
                System.currentTimeMillis() - lastPingTime
        );
    }

    /**
     * Verifica el estado de los canales
     */
    public boolean areChannelsRegistered() {
        try {
            return plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, GRIVYZOM_CHANNEL) &&
                    plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, ECONOMY_CHANNEL);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene información detallada del estado de la integración
     */
    public String getDetailedStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Estado de Integración GrivyzomCore:\n");
        status.append("┃ Conexión: ").append(grivyzomCoreAvailable ? "✅ Activa" : "❌ Inactiva").append("\n");
        status.append("┃ Canales: ").append(areChannelsRegistered() ? "✅ Registrados" : "❌ Error").append("\n");
        status.append("┃ Jugadores locales: ").append(plugin.getServer().getOnlinePlayers().size()).append("\n");
        status.append("┃ Jugadores network: ").append(getRealNetworkPlayerCount()).append("\n");
        status.append("┃ Servidores activos: ").append(getRealNetworkServerCount()).append("\n");
        status.append("┃ Último ping: ").append((System.currentTimeMillis() - lastPingTime) / 1000).append("s ago\n");
        status.append("┃ Última solicitud: ").append((System.currentTimeMillis() - lastStatsRequest) / 1000).append("s ago");
        return status.toString();
    }

    // Getters
    public boolean isGrivyzomCoreAvailable() {
        return grivyzomCoreAvailable;
    }

    public void setGrivyzomCoreAvailable(boolean available) {
        boolean wasAvailable = this.grivyzomCoreAvailable;
        this.grivyzomCoreAvailable = available;

        if (available && !wasAvailable) {
            plugin.getLogger().info(ColorUtils.translate("&a✅ &fGrivyzomCore detectado y disponible"));

            // Solicitar estadísticas iniciales inmediatamente
            new BukkitRunnable() {
                @Override
                public void run() {
                    requestNetworkStats();
                    requestTopPlayers("COINS", 5);
                    requestTopPlayers("GEMS", 5);
                }
            }.runTaskLater(plugin, 40L);

        } else if (!available && wasAvailable) {
            plugin.getLogger().warning(ColorUtils.translate("&c❌ &fConexión con GrivyzomCore perdida"));
        }
    }

    /**
     * Clase para estadísticas de integración mejorada
     */
    public static class IntegrationStats {
        private final boolean coreAvailable;
        private final boolean canSendMessages;
        private final int playersOnline;
        private final int networkPlayers;
        private final int networkServers;
        private final long timeSinceLastStatsRequest;
        private final long timeSinceLastPing;

        public IntegrationStats(boolean coreAvailable, boolean canSendMessages, int playersOnline,
                                int networkPlayers, int networkServers, long timeSinceLastStatsRequest,
                                long timeSinceLastPing) {
            this.coreAvailable = coreAvailable;
            this.canSendMessages = canSendMessages;
            this.playersOnline = playersOnline;
            this.networkPlayers = networkPlayers;
            this.networkServers = networkServers;
            this.timeSinceLastStatsRequest = timeSinceLastStatsRequest;
            this.timeSinceLastPing = timeSinceLastPing;
        }

        public boolean isCoreAvailable() { return coreAvailable; }
        public boolean canSendMessages() { return canSendMessages; }
        public int getPlayersOnline() { return playersOnline; }
        public int getNetworkPlayers() { return networkPlayers; }
        public int getNetworkServers() { return networkServers; }
        public long getTimeSinceLastStatsRequest() { return timeSinceLastStatsRequest; }
        public long getTimeSinceLastPing() { return timeSinceLastPing; }

        @Override
        public String toString() {
            return String.format(
                    "IntegrationStats{core=%s, messages=%s, local=%d, network=%d, servers=%d, lastStats=%ds, lastPing=%ds}",
                    coreAvailable, canSendMessages, playersOnline, networkPlayers, networkServers,
                    timeSinceLastStatsRequest / 1000, timeSinceLastPing / 1000
            );
        }
    }
}