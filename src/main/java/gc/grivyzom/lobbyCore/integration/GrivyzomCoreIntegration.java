package gc.grivyzom.lobbyCore.integration;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Clase para integrar LobbyCore con GrivyzomCore
 * Maneja la comunicación entre ambos plugins
 */
public class GrivyzomCoreIntegration implements Listener {

    private final MainClass plugin;
    private boolean grivyzomCoreAvailable = false;

    // Canales de comunicación con GrivyzomCore
    private static final String GRIVYZOM_CHANNEL = "grivyzom:core";
    private static final String ECONOMY_CHANNEL = "grivyzom:economy";
    private static final String RANKUP_CHANNEL = "grivyzom:rankup";
    private static final String PVP_CHANNEL = "grivyzom:pvp";

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

            // Verificar si GrivyzomCore está disponible
            checkGrivyzomCoreAvailability();

        } catch (Exception e) {
            plugin.getLogger().warning("Error configurando integración con GrivyzomCore: " + e.getMessage());
        }
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
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PING");
            out.writeUTF("LOBBY_CORE");
            out.writeLong(System.currentTimeMillis());

            // Enviar a cualquier jugador conectado para que llegue al proxy
            if (canSendMessage()) {
                Player anyPlayer = getAnyPlayer();
                anyPlayer.sendPluginMessage(plugin, GRIVYZOM_CHANNEL, out.toByteArray());

                plugin.getLogger().info(ColorUtils.translate("&e📡 &fPing enviado a GrivyzomCore..."));
            } else {
                plugin.getLogger().warning(ColorUtils.translate("&e⚠ &fNo hay jugadores conectados para enviar ping a GrivyzomCore"));

                // Reintento automático en 10 segundos
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (canSendMessage()) {
                            sendPingToGrivyzomCore();
                        }
                    }
                }.runTaskLater(plugin, 200L); // 10 segundos
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error enviando ping a GrivyzomCore: " + e.getMessage());
        }
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
                }
            }
        }.runTaskLater(plugin, 20L); // 1 segundo después
    }

    /**
     * Notifica a GrivyzomCore que un jugador se unió al lobby
     */
    public void notifyPlayerJoinedLobby(Player player) {
        try {
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
     * Solicita datos del jugador a GrivyzomCore
     */
    public void requestPlayerData(Player player) {
        try {
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
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("REQUEST_TOP_PLAYERS");
            out.writeUTF(type.toUpperCase());
            out.writeInt(limit);

            // Enviar a cualquier jugador conectado
            if (canSendMessage()) {
                Player anyPlayer = getAnyPlayer();
                anyPlayer.sendPluginMessage(plugin, ECONOMY_CHANNEL, out.toByteArray());

                plugin.getLogger().info(ColorUtils.translate(
                        "&e🏆 &fSolicitando top " + limit + " jugadores por " + type));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error solicitando top de jugadores: " + e.getMessage());
        }
    }

    /**
     * Notifica a GrivyzomCore sobre un evento especial del lobby
     */
    public void notifyLobbyEvent(String eventType, String... data) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("LOBBY_EVENT");
            out.writeUTF(eventType);
            out.writeInt(data.length);
            for (String datum : data) {
                out.writeUTF(datum);
            }

            // Enviar a cualquier jugador conectado
            if (canSendMessage()) {
                Player anyPlayer = getAnyPlayer();
                anyPlayer.sendPluginMessage(plugin, GRIVYZOM_CHANNEL, out.toByteArray());

                plugin.getLogger().info(ColorUtils.translate(
                        "&a🎉 &fEvento de lobby notificado: " + eventType));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error notificando evento de lobby: " + e.getMessage());
        }
    }

    /**
     * Solicita estadísticas del network a GrivyzomCore
     */
    public void requestNetworkStats() {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("REQUEST_NETWORK_STATS");
            out.writeLong(System.currentTimeMillis());

            if (canSendMessage()) {
                Player anyPlayer = getAnyPlayer();
                anyPlayer.sendPluginMessage(plugin, GRIVYZOM_CHANNEL, out.toByteArray());

                plugin.getLogger().info(ColorUtils.translate("&e📈 &fSolicitando estadísticas del network"));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error solicitando estadísticas: " + e.getMessage());
        }
    }

    /**
     * Actualiza el rango de un jugador
     */
    public void updatePlayerRank(Player player, String newRank) {
        try {
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
     * Obtener el nombre del servidor de forma segura
     */
    public String getServerName() {
        try {
            // Intentar obtener el nombre del servidor de diferentes maneras
            String serverName = plugin.getServer().getMotd();

            if (serverName != null && !serverName.isEmpty()) {
                // Limpiar el MOTD de códigos de color si es necesario
                serverName = serverName.replaceAll("§[0-9a-fk-or]", "").trim();
                if (!serverName.isEmpty()) {
                    return serverName;
                }
            }

            // Si el MOTD está vacío, usar un nombre por defecto basado en el puerto
            int port = plugin.getServer().getPort();
            return "lobby-" + port;

        } catch (Exception e) {
            plugin.getLogger().warning("Error obteniendo nombre del servidor: " + e.getMessage());
            return "lobby"; // Fallback por defecto
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

        // Reintento después de 3 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                sendPingToGrivyzomCore();
            }
        }.runTaskLater(plugin, 60L);
    }

    /**
     * Obtiene estadísticas de la integración
     */
    public IntegrationStats getIntegrationStats() {
        return new IntegrationStats(
                grivyzomCoreAvailable,
                canSendMessage(),
                plugin.getServer().getOnlinePlayers().size()
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

    // Getters
    public boolean isGrivyzomCoreAvailable() {
        return grivyzomCoreAvailable;
    }

    public void setGrivyzomCoreAvailable(boolean available) {
        boolean wasAvailable = this.grivyzomCoreAvailable;
        this.grivyzomCoreAvailable = available;

        if (available && !wasAvailable) {
            plugin.getLogger().info(ColorUtils.translate("&a✅ &fGrivyzomCore detectado y disponible"));

            // Solicitar estadísticas iniciales
            new BukkitRunnable() {
                @Override
                public void run() {
                    requestNetworkStats();
                    requestTopPlayers(5);
                }
            }.runTaskLater(plugin, 40L);

        } else if (!available && wasAvailable) {
            plugin.getLogger().warning(ColorUtils.translate("&c❌ &fConexión con GrivyzomCore perdida"));
        }
    }

    /**
     * Clase para estadísticas de integración
     */
    public static class IntegrationStats {
        private final boolean coreAvailable;
        private final boolean canSendMessages;
        private final int playersOnline;

        public IntegrationStats(boolean coreAvailable, boolean canSendMessages, int playersOnline) {
            this.coreAvailable = coreAvailable;
            this.canSendMessages = canSendMessages;
            this.playersOnline = playersOnline;
        }

        public boolean isCoreAvailable() { return coreAvailable; }
        public boolean canSendMessages() { return canSendMessages; }
        public int getPlayersOnline() { return playersOnline; }

        @Override
        public String toString() {
            return String.format("IntegrationStats{core=%s, messages=%s, players=%d}",
                    coreAvailable, canSendMessages, playersOnline);
        }
    }
}