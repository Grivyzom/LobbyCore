package gc.grivyzom.lobbyCore.integration;

import gc.grivyzom.lobbyCore.MainClass;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Expansión de PlaceholderAPI para GrivyzomCore
 * Proporciona placeholders integrados con el sistema de datos
 */
public class GrivyzomPlaceholders extends PlaceholderExpansion {

    private final MainClass plugin;
    private final GrivyzomResponseHandler responseHandler;

    public GrivyzomPlaceholders(MainClass plugin, GrivyzomResponseHandler responseHandler) {
        this.plugin = plugin;
        this.responseHandler = responseHandler;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "grivyzom";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // Datos del jugador
        switch (params.toLowerCase()) {
            case "player_name":
                return getPlayerData(player, "name", player.getName());

            case "player_coins":
                return getPlayerData(player, "coins", "1,500");

            case "player_gems":
                return getPlayerData(player, "gems", "250");

            case "player_rank":
                return getPlayerData(player, "rank", "VIP");

            case "player_level":
                return getPlayerData(player, "level", "15");

            case "player_playtime":
                return getPlayerData(player, "playtime", "45h 30m");

            // Network stats
            case "network_players":
                return getNetworkData("players", "127");

            case "network_servers":
                return getNetworkData("servers", "5");

            case "network_status":
                return plugin.getGrivyzomIntegration() != null &&
                        plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable() ?
                        "§aConectado" : "§cDesconectado";

            // Top players (coins)
            case "top_coins_1":
                return getTopPlayerData("coins", 1, "name", "JugadorPro");

            case "top_coins_2":
                return getTopPlayerData("coins", 2, "name", "MegaBuilder");

            case "top_coins_3":
                return getTopPlayerData("coins", 3, "name", "CraftMaster");

            case "top_coins_1_amount":
                return getTopPlayerData("coins", 1, "coins", "50,000");

            case "top_coins_2_amount":
                return getTopPlayerData("coins", 2, "coins", "35,000");

            case "top_coins_3_amount":
                return getTopPlayerData("coins", 3, "coins", "28,000");

            // Top players (gems)
            case "top_gems_1":
                return getTopPlayerData("gems", 1, "name", "DiamondKing");

            case "top_gems_2":
                return getTopPlayerData("gems", 2, "name", "EmeraldQueen");

            case "top_gems_3":
                return getTopPlayerData("gems", 3, "name", "GemCollector");

            case "top_gems_1_amount":
                return getTopPlayerData("gems", 1, "gems", "5,000");

            case "top_gems_2_amount":
                return getTopPlayerData("gems", 2, "gems", "3,500");

            case "top_gems_3_amount":
                return getTopPlayerData("gems", 3, "gems", "2,800");

            // Economy totals
            case "economy_total_coins":
                return getEconomyData("total_coins", "2,500,000");

            case "economy_total_gems":
                return getEconomyData("total_gems", "150,000");

            // Placeholders de conexión
            case "connection_status":
                return plugin.getGrivyzomIntegration() != null &&
                        plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable() ?
                        "§a●" : "§c●";

            case "connection_latency":
                return "23ms"; // TODO: Implementar medición real de latencia

            // Placeholders de servidor
            case "server_name":
                return plugin.getGrivyzomIntegration() != null ?
                        plugin.getGrivyzomIntegration().getServerName() : "Lobby";

            case "server_type":
                return "Lobby";

            default:
                return null; // Placeholder no reconocido
        }
    }

    /**
     * Obtiene datos del jugador del cache o valores por defecto
     */
    private String getPlayerData(Player player, String dataType, String defaultValue) {
        if (responseHandler == null) {
            return defaultValue;
        }

        String value = responseHandler.getPlayerData(player.getUniqueId(), dataType);
        return value != null ? value : defaultValue;
    }

    /**
     * Obtiene datos del network del cache o valores por defecto
     */
    private String getNetworkData(String dataType, String defaultValue) {
        if (responseHandler == null) {
            return defaultValue;
        }

        String value = responseHandler.getNetworkData(dataType);
        return value != null ? value : defaultValue;
    }

    /**
     * Obtiene datos de top players
     */
    private String getTopPlayerData(String type, int position, String field, String defaultValue) {
        if (responseHandler == null) {
            return defaultValue;
        }

        String value = responseHandler.getTopPlayerData(type, position, field);
        return value != null ? value : defaultValue;
    }

    /**
     * Obtiene datos de economía
     */
    private String getEconomyData(String dataType, String defaultValue) {
        if (responseHandler == null) {
            return defaultValue;
        }

        String value = responseHandler.getEconomyData(dataType);
        return value != null ? value : defaultValue;
    }

    /**
     * Registra los placeholders y muestra información
     */
    @Override
    public boolean register() {
        boolean success = super.register();
        if (success) {
            plugin.getLogger().info("§a✓ §fPlaceholders GrivyzomCore registrados exitosamente");

            // Log de placeholders disponibles
            logAvailablePlaceholders();
        } else {
            plugin.getLogger().warning("§c❌ §fError al registrar placeholders GrivyzomCore");
        }
        return success;
    }

    /**
     * Muestra todos los placeholders disponibles en el log
     */
    private void logAvailablePlaceholders() {
        plugin.getLogger().info("§e📋 §fPlaceholders disponibles:");

        // Placeholders del jugador
        plugin.getLogger().info("§b🧑 §fDatos del Jugador:");
        plugin.getLogger().info("§7  • §f%grivyzom_player_name% §7- Nombre del jugador");
        plugin.getLogger().info("§7  • §f%grivyzom_player_coins% §7- Monedas del jugador");
        plugin.getLogger().info("§7  • §f%grivyzom_player_gems% §7- Gemas del jugador");
        plugin.getLogger().info("§7  • §f%grivyzom_player_rank% §7- Rango del jugador");
        plugin.getLogger().info("§7  • §f%grivyzom_player_level% §7- Nivel del jugador");
        plugin.getLogger().info("§7  • §f%grivyzom_player_playtime% §7- Tiempo de juego");

        // Placeholders del network
        plugin.getLogger().info("§e🌐 §fDatos del Network:");
        plugin.getLogger().info("§7  • §f%grivyzom_network_players% §7- Jugadores online totales");
        plugin.getLogger().info("§7  • §f%grivyzom_network_servers% §7- Servidores activos");
        plugin.getLogger().info("§7  • §f%grivyzom_network_status% §7- Estado de conexión");

        // Placeholders de tops
        plugin.getLogger().info("§d🏆 §fTops de Jugadores:");
        plugin.getLogger().info("§7  • §f%grivyzom_top_coins_1% §7- Top 1 en monedas (nombre)");
        plugin.getLogger().info("§7  • §f%grivyzom_top_coins_1_amount% §7- Top 1 en monedas (cantidad)");
        plugin.getLogger().info("§7  • §f%grivyzom_top_gems_1% §7- Top 1 en gemas (nombre)");

        // Placeholders de economía
        plugin.getLogger().info("§a💰 §fEconomía Global:");
        plugin.getLogger().info("§7  • §f%grivyzom_economy_total_coins% §7- Total de monedas");
        plugin.getLogger().info("§7  • §f%grivyzom_economy_total_gems% §7- Total de gemas");

        plugin.getLogger().info("§7");
        plugin.getLogger().info("§7💡 §fUsa §e/papi parse me %placeholder% §fpara probar los placeholders");
    }

    /**
     * Verifica si los placeholders están funcionando
     */
    public boolean isWorking() {
        return super.isRegistered();
    }

    /**
     * Obtiene estadísticas de uso de placeholders
     */
    public PlaceholderStats getStats() {
        return new PlaceholderStats(
                super.isRegistered(),
                responseHandler != null,
                plugin.getGrivyzomIntegration() != null &&
                        plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()
        );
    }

    /**
     * Fuerza la actualización de datos desde GrivyzomCore
     */
    public void refreshData() {
        if (plugin.getGrivyzomIntegration() != null &&
                plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()) {

            plugin.getGrivyzomIntegration().requestNetworkStats();
            plugin.getGrivyzomIntegration().requestTopPlayers("COINS", 5);
            plugin.getGrivyzomIntegration().requestTopPlayers("GEMS", 5);

            plugin.getLogger().info("§e🔄 §fDatos de placeholders actualizados desde GrivyzomCore");
        }
    }

    /**
     * Clase para estadísticas de placeholders
     */
    public static class PlaceholderStats {
        private final boolean registered;
        private final boolean responseHandlerAvailable;
        private final boolean grivyzomConnected;

        public PlaceholderStats(boolean registered, boolean responseHandlerAvailable, boolean grivyzomConnected) {
            this.registered = registered;
            this.responseHandlerAvailable = responseHandlerAvailable;
            this.grivyzomConnected = grivyzomConnected;
        }

        public boolean isRegistered() { return registered; }
        public boolean isResponseHandlerAvailable() { return responseHandlerAvailable; }
        public boolean isGrivyzomConnected() { return grivyzomConnected; }

        @Override
        public String toString() {
            return String.format("PlaceholderStats{registered=%s, handler=%s, connected=%s}",
                    registered, responseHandlerAvailable, grivyzomConnected);
        }
    }
}