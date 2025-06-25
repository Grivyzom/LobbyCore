package gc.grivyzom.lobbyCore.integration;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manejador de respuestas de GrivyzomCore
 * Procesa mensajes entrantes y mantiene cache de datos
 */
public class GrivyzomResponseHandler implements PluginMessageListener {

    private final MainClass plugin;

    // Cache de datos del jugador
    private final Map<UUID, Map<String, String>> playerDataCache;

    // Cache de datos del network
    private final Map<String, String> networkDataCache;

    // Cache de top players
    private final Map<String, Map<Integer, Map<String, String>>> topPlayersCache;

    // Cache de datos de economía
    private final Map<String, String> economyDataCache;

    // Canales registrados
    private static final String GRIVYZOM_CHANNEL = "grivyzom:core";
    private static final String ECONOMY_CHANNEL = "grivyzom:economy";

    public GrivyzomResponseHandler(MainClass plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<>();
        this.networkDataCache = new ConcurrentHashMap<>();
        this.topPlayersCache = new ConcurrentHashMap<>();
        this.economyDataCache = new ConcurrentHashMap<>();

        setupChannels();
        initializeDefaultData();
    }

    /**
     * Configura los canales de comunicación entrantes
     */
    private void setupChannels() {
        // Registrar canales de entrada
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, GRIVYZOM_CHANNEL, this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, ECONOMY_CHANNEL, this);

        plugin.getLogger().info(ColorUtils.translate("&a✓ &fCanales de respuesta GrivyzomCore registrados"));
    }

    /**
     * Inicializa datos por defecto para demostración
     */
    private void initializeDefaultData() {
        // Datos por defecto del network
        networkDataCache.put("players", "127");
        networkDataCache.put("servers", "5");
        networkDataCache.put("status", "online");

        // Datos por defecto de economía
        economyDataCache.put("total_coins", "2,500,000");
        economyDataCache.put("total_gems", "150,000");

        // Top players por defecto
        initializeTopPlayersDefaults();

        plugin.getLogger().info(ColorUtils.translate("&a✓ &fDatos por defecto inicializados"));
    }

    /**
     * Inicializa datos por defecto de top players
     */
    private void initializeTopPlayersDefaults() {
        // Top coins
        Map<Integer, Map<String, String>> topCoins = new HashMap<>();

        Map<String, String> top1Coins = new HashMap<>();
        top1Coins.put("name", "JugadorPro");
        top1Coins.put("coins", "50,000");
        topCoins.put(1, top1Coins);

        Map<String, String> top2Coins = new HashMap<>();
        top2Coins.put("name", "MegaBuilder");
        top2Coins.put("coins", "35,000");
        topCoins.put(2, top2Coins);

        Map<String, String> top3Coins = new HashMap<>();
        top3Coins.put("name", "CraftMaster");
        top3Coins.put("coins", "28,000");
        topCoins.put(3, top3Coins);

        topPlayersCache.put("coins", topCoins);

        // Top gems
        Map<Integer, Map<String, String>> topGems = new HashMap<>();

        Map<String, String> top1Gems = new HashMap<>();
        top1Gems.put("name", "DiamondKing");
        top1Gems.put("gems", "5,000");
        topGems.put(1, top1Gems);

        Map<String, String> top2Gems = new HashMap<>();
        top2Gems.put("name", "EmeraldQueen");
        top2Gems.put("gems", "3,500");
        topGems.put(2, top2Gems);

        Map<String, String> top3Gems = new HashMap<>();
        top3Gems.put("name", "GemCollector");
        top3Gems.put("gems", "2,800");
        topGems.put(3, top3Gems);

        topPlayersCache.put("gems", topGems);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals(GRIVYZOM_CHANNEL) && !channel.equals(ECONOMY_CHANNEL)) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String messageType = in.readUTF();

            plugin.getLogger().info(ColorUtils.translate(
                    "&e📨 &fMensaje recibido - Canal: &b" + channel + "&f, Tipo: &e" + messageType
            ));

            switch (messageType) {
                case "PONG":
                    handlePong(in);
                    break;

                case "PLAYER_DATA_RESPONSE":
                    handlePlayerDataResponse(in);
                    break;

                case "TOP_PLAYERS_RESPONSE":
                    handleTopPlayersResponse(in);
                    break;

                case "NETWORK_STATS_RESPONSE":
                    handleNetworkStatsResponse(in);
                    break;

                case "ECONOMY_UPDATE":
                    handleEconomyUpdate(in);
                    break;

                case "COINS_UPDATED":
                case "GEMS_UPDATED":
                    handleCurrencyUpdate(in, messageType);
                    break;

                default:
                    plugin.getLogger().info(ColorUtils.translate(
                            "&e⚠ &fTipo de mensaje desconocido: &e" + messageType
                    ));
                    break;
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error procesando mensaje de GrivyzomCore: " + e.getMessage());
        }
    }

    /**
     * Maneja respuesta PONG de GrivyzomCore
     */
    private void handlePong(ByteArrayDataInput in) {
        try {
            String serverName = in.readUTF();
            long timestamp = in.readLong();

            // Marcar como disponible
            if (plugin.getGrivyzomIntegration() != null) {
                plugin.getGrivyzomIntegration().setGrivyzomCoreAvailable(true);
            }

            long latency = System.currentTimeMillis() - timestamp;
            plugin.getLogger().info(ColorUtils.translate(
                    "&a🏓 &fPONG recibido de &b" + serverName + " &f(Latencia: &e" + latency + "ms&f)"
            ));

        } catch (Exception e) {
            plugin.getLogger().warning("Error procesando PONG: " + e.getMessage());
        }
    }

    /**
     * Maneja respuesta de datos del jugador
     */
    private void handlePlayerDataResponse(ByteArrayDataInput in) {
        try {
            String playerUUID = in.readUTF();
            UUID uuid = UUID.fromString(playerUUID);

            // Leer datos del jugador
            Map<String, String> playerData = new HashMap<>();
            playerData.put("name", in.readUTF());
            playerData.put("coins", in.readUTF());
            playerData.put("gems", in.readUTF());
            playerData.put("rank", in.readUTF());
            playerData.put("level", in.readUTF());
            playerData.put("playtime", in.readUTF());

            // Guardar en cache
            playerDataCache.put(uuid, playerData);

            plugin.getLogger().info(ColorUtils.translate(
                    "&a📊 &fDatos actualizados para jugador: &b" + playerData.get("name")
            ));

        } catch (Exception e) {
            plugin.getLogger().warning("Error procesando datos del jugador: " + e.getMessage());
        }
    }

    /**
     * Maneja respuesta de top players
     */
    private void handleTopPlayersResponse(ByteArrayDataInput in) {
        try {
            String type = in.readUTF(); // "COINS" o "GEMS"
            int count = in.readInt();

            Map<Integer, Map<String, String>> topData = new HashMap<>();

            for (int i = 1; i <= count; i++) {
                Map<String, String> playerData = new HashMap<>();
                playerData.put("name", in.readUTF());
                playerData.put(type.toLowerCase(), in.readUTF());
                topData.put(i, playerData);
            }

            topPlayersCache.put(type.toLowerCase(), topData);

            plugin.getLogger().info(ColorUtils.translate(
                    "&a🏆 &fTop " + count + " jugadores por " + type + " actualizado"
            ));

        } catch (Exception e) {
            plugin.getLogger().warning("Error procesando top players: " + e.getMessage());
        }
    }

    /**
     * Maneja respuesta de estadísticas del network
     */
    private void handleNetworkStatsResponse(ByteArrayDataInput in) {
        try {
            int totalPlayers = in.readInt();
            int totalServers = in.readInt();
            String status = in.readUTF();

            networkDataCache.put("players", String.valueOf(totalPlayers));
            networkDataCache.put("servers", String.valueOf(totalServers));
            networkDataCache.put("status", status);

            plugin.getLogger().info(ColorUtils.translate(
                    "&a📈 &fEstadísticas del network actualizadas: &e" + totalPlayers + " &fjugadores, &e" + totalServers + " &fservidores"
            ));

        } catch (Exception e) {
            plugin.getLogger().warning("Error procesando estadísticas del network: " + e.getMessage());
        }
    }

    /**
     * Maneja actualizaciones de economía
     */
    private void handleEconomyUpdate(ByteArrayDataInput in) {
        try {
            String playerUUID = in.readUTF();
            String updateType = in.readUTF();
            String newValue = in.readUTF();

            UUID uuid = UUID.fromString(playerUUID);
            Map<String, String> playerData = playerDataCache.getOrDefault(uuid, new HashMap<>());
            playerData.put(updateType.toLowerCase(), newValue);
            playerDataCache.put(uuid, playerData);

            plugin.getLogger().info(ColorUtils.translate(
                    "&a💰 &fActualización de economía: &e" + updateType + " &f= &e" + newValue
            ));

        } catch (Exception e) {
            plugin.getLogger().warning("Error procesando actualización de economía: " + e.getMessage());
        }
    }

    /**
     * Maneja actualizaciones de monedas/gemas específicas
     */
    private void handleCurrencyUpdate(ByteArrayDataInput in, String messageType) {
        try {
            String playerUUID = in.readUTF();
            String newValue = in.readUTF();

            UUID uuid = UUID.fromString(playerUUID);
            Map<String, String> playerData = playerDataCache.getOrDefault(uuid, new HashMap<>());

            String currencyType = messageType.equals("COINS_UPDATED") ? "coins" : "gems";
            playerData.put(currencyType, newValue);
            playerDataCache.put(uuid, playerData);

            plugin.getLogger().info(ColorUtils.translate(
                    "&a💎 &f" + currencyType.toUpperCase() + " actualizadas: &e" + newValue
            ));

        } catch (Exception e) {
            plugin.getLogger().warning("Error procesando actualización de " + messageType + ": " + e.getMessage());
        }
    }

    // Métodos públicos para acceso a datos

    /**
     * Obtiene datos de un jugador
     */
    public String getPlayerData(UUID playerUUID, String dataType) {
        Map<String, String> playerData = playerDataCache.get(playerUUID);
        if (playerData != null) {
            return playerData.get(dataType.toLowerCase());
        }
        return null;
    }

    /**
     * Obtiene datos del network
     */
    public String getNetworkData(String dataType) {
        return networkDataCache.get(dataType.toLowerCase());
    }

    /**
     * Obtiene datos de top players
     */
    public String getTopPlayerData(String type, int position, String field) {
        Map<Integer, Map<String, String>> topData = topPlayersCache.get(type.toLowerCase());
        if (topData != null) {
            Map<String, String> playerData = topData.get(position);
            if (playerData != null) {
                return playerData.get(field.toLowerCase());
            }
        }
        return null;
    }

    /**
     * Obtiene datos de economía
     */
    public String getEconomyData(String dataType) {
        return economyDataCache.get(dataType.toLowerCase());
    }

    /**
     * Limpia el cache
     */
    public void clearCache() {
        playerDataCache.clear();
        networkDataCache.clear();
        topPlayersCache.clear();
        economyDataCache.clear();

        initializeDefaultData();

        plugin.getLogger().info(ColorUtils.translate("&e🔄 &fCache de datos limpiado y reinicializado"));
    }

    /**
     * Obtiene estadísticas del cache
     */
    public String getCacheStats() {
        return String.format(
                "Cache Stats: Players=%d, Network=%d, TopPlayers=%d, Economy=%d",
                playerDataCache.size(),
                networkDataCache.size(),
                topPlayersCache.size(),
                economyDataCache.size()
        );
    }
}