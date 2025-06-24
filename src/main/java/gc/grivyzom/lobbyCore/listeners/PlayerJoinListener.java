package gc.grivyzom.lobbyCore.listeners;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final MainClass plugin;
    private Economy economy;

    public PlayerJoinListener(MainClass plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Ocultar el mensaje de join por defecto si está configurado
        if (plugin.getConfigManager().getConfig().getBoolean("welcome.hide-default-join", true)) {
            event.setJoinMessage(null);
        }

        // Verificar si el jugador es nuevo
        boolean isNewPlayer = !player.hasPlayedBefore();

        // Enviar mensaje de bienvenida
        plugin.getWelcomeMessageManager().sendWelcomeMessage(player);

        // Dar dinero de bienvenida si Vault está habilitado
        if (isNewPlayer && plugin.getConfigManager().isVaultEnabled() && economy != null) {
            giveWelcomeMoney(player);
        }

        // Log de conexión con estilo
        logPlayerJoin(player, isNewPlayer);
    }

    /**
     * Configura la economía usando Vault
     */
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }

        economy = rsp.getProvider();
        plugin.getLogger().info(ColorUtils.translate("&a✓ &fEconomía de Vault configurada correctamente"));
    }

    /**
     * Da dinero de bienvenida a un jugador nuevo
     * @param player El jugador
     */
    private void giveWelcomeMoney(Player player) {
        if (economy == null) return;

        double amount = plugin.getConfigManager().getWelcomeMoney();

        // Delay para dar el dinero después del mensaje de bienvenida
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                economy.depositPlayer(player, amount);

                String message = plugin.getConfigManager().getWelcomeMoneyMessage();
                message = message.replace("{AMOUNT}", String.valueOf(amount))
                        .replace("{PLAYER}", player.getName());

                ColorUtils.sendMessage(player, message);

                plugin.getLogger().info(ColorUtils.translate(
                        "&a✓ &fSe han dado &e$" + amount + " &fa " + player.getName() + " por primera vez"
                ));
            }
        }.runTaskLater(plugin, 100L); // 5 segundos después
    }

    /**
     * Registra la conexión del jugador en consola con estilo
     * @param player El jugador
     * @param isNewPlayer Si es un jugador nuevo
     */
    private void logPlayerJoin(Player player, boolean isNewPlayer) {
        String prefix = isNewPlayer ? "&a[NUEVO]" : "&b[CONEXIÓN]";
        String logMessage = String.format(
                "%s &f%s se ha conectado al servidor &7(%d/%d jugadores)",
                prefix,
                player.getName(),
                Bukkit.getOnlinePlayers().size(),
                Bukkit.getMaxPlayers()
        );

        plugin.getLogger().info(ColorUtils.stripColor(ColorUtils.translate(logMessage)));
    }
}