package gc.grivyzom.lobbyCore.commands;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.models.ActionItem;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LobbyCommand implements CommandExecutor, TabCompleter {

    private final MainClass plugin;

    public LobbyCommand(MainClass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "test":
                handleTest(sender, args);
                break;

            case "info":
                handleInfo(sender);
                break;

            case "welcome":
                handleWelcome(sender, args);
                break;

            case "version":
                handleVersion(sender);
                break;

            case "items":
                handleItems(sender, args);
                break;

            case "fireworks":
                handleFireworks(sender, args);
                break;

            case "grivyzom":
                handleGrivyzom(sender, args);
                break;

            default:
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    /**
     * Maneja el comando de recarga
     */
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("lobbycore.admin")) {
            ColorUtils.sendMessage((Player) sender, "&c❌ &fNo tienes permisos para usar este comando.");
            return;
        }

        try {
            plugin.reloadAll();
            ColorUtils.sendMessage((Player) sender,
                    "&a✅ &f¡Configuración recargada correctamente!");

            plugin.getLogger().info(sender.getName() + " ha recargado la configuración de LobbyCore");

        } catch (Exception e) {
            ColorUtils.sendMessage((Player) sender,
                    "&c❌ &fError al recargar la configuración: " + e.getMessage());
            plugin.getLogger().severe("Error al recargar configuración: " + e.getMessage());
        }
    }

    /**
     * Maneja los comandos de integración con GrivyzomCore
     */
    private void handleGrivyzom(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lobbycore.admin")) {
            ColorUtils.sendMessage((Player) sender, "&c❌ &fNo tienes permisos para usar este comando.");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            ColorUtils.sendMessage(player,
                    "&c❌ &fUso: /lobbycore grivyzom <ping|status|stats|data|top|reconnect>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "ping":
                handleGrivyzomPing(player);
                break;

            case "status":
                handleGrivyzomStatus(player);
                break;

            case "stats":
                handleGrivyzomStats(player);
                break;

            case "data":
                handleGrivyzomData(player, args);
                break;

            case "top":
                handleGrivyzomTop(player, args);
                break;

            case "reconnect":
                handleGrivyzomReconnect(player);
                break;

            default:
                ColorUtils.sendMessage(player,
                        "&c❌ &fUso: /lobbycore grivyzom <ping|status|stats|data|top|reconnect>");
                break;
        }
    }

    /**
     * Envía ping a GrivyzomCore
     */
    private void handleGrivyzomPing(Player player) {
        if (plugin.getGrivyzomIntegration() == null) {
            ColorUtils.sendMessage(player, "&c❌ &fIntegración GrivyzomCore no disponible.");
            return;
        }

        ColorUtils.sendMessage(player, "&e📡 &fEnviando ping a GrivyzomCore...");
        plugin.getGrivyzomIntegration().sendPingToGrivyzomCore();

        // Verificar respuesta después de 3 segundos
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()) {
                ColorUtils.sendMessage(player, "&a🏓 &fPONG recibido de GrivyzomCore!");
            } else {
                ColorUtils.sendMessage(player, "&c❌ &fNo se recibió respuesta de GrivyzomCore.");
            }
        }, 60L);
    }

    /**
     * Muestra el estado de la integración
     */
    private void handleGrivyzomStatus(Player player) {
        boolean integrationActive = plugin.getGrivyzomIntegration() != null;
        boolean connected = integrationActive && plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable();
        boolean placeholdersActive = plugin.getPlaceholders() != null && plugin.getPlaceholders().isWorking();

        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#4ECDC4▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &b🔧 &fEstado de Integración &aGrivyzomCore",
                "",
                "  &7┃ &fIntegración: " + (integrationActive ? "&a✅ Activa" : "&c❌ Inactiva"),
                "  &7┃ &fConexión: " + (connected ? "&a✅ Conectado" : "&c❌ Desconectado"),
                "  &7┃ &fPlaceholders: " + (placeholdersActive ? "&a✅ Funcionando" : "&c❌ No disponible"),
                "  &7┃ &fCanales: " + (integrationActive ? "&a4 registrados" : "&c0 registrados"),
                "",
                "  &7┃ &fResponse Handler: " + (plugin.getResponseHandler() != null ? "&a✅ Activo" : "&c❌ Inactivo"),
                "  &7┃ &fCache Stats: " + (plugin.getResponseHandler() != null ?
                        plugin.getResponseHandler().getCacheStats() : "&7No disponible"),
                "",
                connected ? "  &a✨ &fTodos los sistemas funcionando correctamente" :
                        "  &c⚠ &fAlgunos sistemas no están disponibles",
                "",
                "&#4ECDC4▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    /**
     * Solicita estadísticas del network
     */
    private void handleGrivyzomStats(Player player) {
        if (plugin.getGrivyzomIntegration() == null || !plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()) {
            ColorUtils.sendMessage(player, "&c❌ &fGrivyzomCore no está conectado.");
            return;
        }

        ColorUtils.sendMessage(player, "&e📈 &fSolicitando estadísticas del network...");
        plugin.getGrivyzomIntegration().requestNetworkStats();

        ColorUtils.sendMessage(player, "&7Los datos se actualizarán automáticamente en los placeholders.");
    }

    /**
     * Solicita datos de un jugador específico
     */
    private void handleGrivyzomData(Player player, String[] args) {
        if (plugin.getGrivyzomIntegration() == null || !plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()) {
            ColorUtils.sendMessage(player, "&c❌ &fGrivyzomCore no está conectado.");
            return;
        }

        Player target = player;
        if (args.length > 2) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                ColorUtils.sendMessage(player, "&c❌ &fJugador no encontrado.");
                return;
            }
        }

        ColorUtils.sendMessage(player, "&e📊 &fSolicitando datos de &b" + target.getName() + "&f...");
        plugin.getGrivyzomIntegration().requestPlayerData(target);

        ColorUtils.sendMessage(player, "&7Los datos se actualizarán automáticamente en los placeholders.");
    }

    /**
     * Solicita top de jugadores
     */
    private void handleGrivyzomTop(Player player, String[] args) {
        if (plugin.getGrivyzomIntegration() == null || !plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()) {
            ColorUtils.sendMessage(player, "&c❌ &fGrivyzomCore no está conectado.");
            return;
        }

        int limit = 5;
        if (args.length > 2) {
            try {
                limit = Integer.parseInt(args[2]);
                if (limit < 1 || limit > 10) {
                    limit = 5;
                }
            } catch (NumberFormatException e) {
                limit = 5;
            }
        }

        ColorUtils.sendMessage(player, "&e🏆 &fSolicitando top " + limit + " jugadores...");
        plugin.getGrivyzomIntegration().requestTopPlayers(limit);

        ColorUtils.sendMessage(player, "&7Los datos se actualizarán automáticamente en los placeholders.");
    }

    /**
     * Fuerza reconexión con GrivyzomCore
     */
    private void handleGrivyzomReconnect(Player player) {
        if (plugin.getGrivyzomIntegration() == null) {
            ColorUtils.sendMessage(player, "&c❌ &fIntegración GrivyzomCore no disponible.");
            return;
        }

        ColorUtils.sendMessage(player, "&e🔄 &fForzando reconexión con GrivyzomCore...");
        plugin.getGrivyzomIntegration().forceReconnect();

        ColorUtils.sendMessage(player, "&7Reintentando conexión en 3 segundos...");
    }

    /**
     * Maneja el comando de prueba
     */
    private void handleTest(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lobbycore.admin")) {
            ColorUtils.sendMessage((Player) sender, "&c❌ &fNo tienes permisos para usar este comando.");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return;
        }

        Player player = (Player) sender;

        if (args.length > 1) {
            // Probar con otro jugador
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                ColorUtils.sendMessage(player, "&c❌ &fJugador no encontrado.");
                return;
            }

            plugin.getWelcomeMessageManager().sendWelcomeMessage(target);
            ColorUtils.sendMessage(player,
                    "&a✅ &fMensaje de bienvenida enviado a &b" + target.getName() + "&f.");
        } else {
            // Probar consigo mismo
            plugin.getWelcomeMessageManager().sendWelcomeMessage(player);
            ColorUtils.sendMessage(player,
                    "&a✅ &fMensaje de bienvenida enviado.");
        }
    }

    /**
     * Maneja el comando de información
     */
    private void handleInfo(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return;
        }

        Player player = (Player) sender;
        MainClass.PluginStats stats = plugin.getPluginStats();

        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#FF6B6B▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &b⚡ &fInformación de &aLobbyCore",
                "",
                "  &7┃ &fVersión: &a" + plugin.getDescription().getVersion(),
                "  &7┃ &fAutor: &a" + plugin.getDescription().getAuthors().get(0),
                "  &7┃ &fWeb: &a" + plugin.getDescription().getWebsite(),
                "  &7┃ &fBienvenida: &a" + (plugin.getConfigManager().isWelcomeEnabled() ? "Habilitada" : "Deshabilitada"),
                "  &7┃ &fItems de acción: &a" + stats.getActionItemsLoaded() + " configurados",
                "  &7┃ &fJugadores: &a" + stats.getOnlinePlayers() + "&7/&a" + Bukkit.getMaxPlayers(),
                "",
                "  &b🔧 &fIntegración GrivyzomCore:",
                "  &7┃ &fEstado: " + (stats.isGrivyzomConnected() ? "&a✅ Conectado" : "&c❌ Desconectado"),
                "  &7┃ &fPlaceholders: " + (stats.isPlaceholdersActive() ? "&a✅ Activos" : "&c❌ Inactivos"),
                "",
                "  &7Usa &e/lobbycore grivyzom status &7para más detalles",
                "  &7Usa &e/lobbycore help &7para ver todos los comandos",
                "",
                "&#FF6B6B▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    /**
     * Maneja el comando de items de acción
     */
    private void handleItems(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lobbycore.admin")) {
            ColorUtils.sendMessage((Player) sender, "&c❌ &fNo tienes permisos para usar este comando.");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            ColorUtils.sendMessage(player,
                    "&c❌ &fUso: /lobbycore items <list|give|reload>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list":
                handleItemsList(player);
                break;

            case "give":
                handleItemsGive(player, args);
                break;

            case "reload":
                plugin.getItemActionManager().reload();
                ColorUtils.sendMessage(player,
                        "&a✅ &f¡Items de acción recargados!");
                break;

            default:
                ColorUtils.sendMessage(player,
                        "&c❌ &fUso: /lobbycore items <list|give|reload>");
                break;
        }
    }

    /**
     * Lista todos los items de acción disponibles
     */
    private void handleItemsList(Player player) {
        Map<String, ActionItem> items = plugin.getItemActionManager().getAllActionItems();

        if (items.isEmpty()) {
            ColorUtils.sendMessage(player, "&e⚠ &fNo hay items de acción configurados.");
            return;
        }

        ColorUtils.sendMessage(player, "");
        ColorUtils.sendMessage(player, "&b📦 &fItems de acción disponibles:");
        ColorUtils.sendMessage(player, "");

        for (ActionItem item : items.values()) {
            String flags = "";
            if (item.isGiveOnJoin()) flags += "&a[AUTO] ";
            if (item.isPreventDrop()) flags += "&c[NO-DROP] ";
            if (item.isPreventMove()) flags += "&e[FIJO] ";
            if (item.isKeepOnDeath()) flags += "&d[INMORTAL] ";

            ColorUtils.sendMessage(player,
                    "&7• &e" + item.getItemId() + " &7- " + item.getDisplayName() + " " + flags);
        }

        ColorUtils.sendMessage(player, "");
        ColorUtils.sendMessage(player, "&7Usa &e/lobbycore items give <item> [jugador] &7para dar un item");
        ColorUtils.sendMessage(player, "");
    }

    /**
     * Da un item de acción a un jugador
     */
    private void handleItemsGive(Player sender, String[] args) {
        if (args.length < 3) {
            ColorUtils.sendMessage(sender,
                    "&c❌ &fUso: /lobbycore items give <item> [jugador]");
            return;
        }

        String itemId = args[2];
        ActionItem actionItem = plugin.getItemActionManager().getActionItem(itemId);

        if (actionItem == null) {
            ColorUtils.sendMessage(sender,
                    "&c❌ &fItem de acción no encontrado: &e" + itemId);
            return;
        }

        Player target = sender;
        if (args.length > 3) {
            target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                ColorUtils.sendMessage(sender, "&c❌ &fJugador no encontrado.");
                return;
            }
        }

        plugin.getItemActionManager().giveItemToPlayer(target, actionItem);

        if (target.equals(sender)) {
            ColorUtils.sendMessage(sender,
                    "&a✅ &fHas recibido el item: " + actionItem.getDisplayName());
        } else {
            ColorUtils.sendMessage(sender,
                    "&a✅ &fItem &e" + itemId + " &fdado a &b" + target.getName() + "&f.");
            ColorUtils.sendMessage(target,
                    "&a🎁 &fHas recibido un item: " + actionItem.getDisplayName());
        }
    }

    /**
     * Maneja el comando de fuegos artificiales
     */
    private void handleFireworks(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lobbycore.admin")) {
            ColorUtils.sendMessage((Player) sender, "&c❌ &fNo tienes permisos para usar este comando.");
            return;
        }

        if (args.length < 2) {
            ColorUtils.sendMessage((Player) sender,
                    "&c❌ &fUso: /lobbycore fireworks <enable|disable|test|status>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable":
                plugin.getFireworksManager().setFireworksEnabled(true);
                ColorUtils.sendMessage((Player) sender,
                        "&a✨ &f¡Fuegos artificiales habilitados!");
                break;

            case "disable":
                plugin.getFireworksManager().setFireworksEnabled(false);
                ColorUtils.sendMessage((Player) sender,
                        "&c✨ &fFuegos artificiales deshabilitados.");
                break;

            case "test":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return;
                }

                Player player = (Player) sender;
                plugin.getFireworksManager().launchTestFireworks(player);
                break;

            case "status":
                boolean enabled = plugin.getFireworksManager().isEnabled();
                String status = enabled ? "&aHabilitados" : "&cDeshabilitados";
                ColorUtils.sendMessage((Player) sender,
                        "&7Estado de fuegos artificiales: " + status);
                break;

            default:
                ColorUtils.sendMessage((Player) sender,
                        "&c❌ &fUso: /lobbycore fireworks <enable|disable|test|status>");
                break;
        }
    }

    /**
     * Maneja el comando de bienvenida personalizada
     */
    private void handleWelcome(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lobbycore.admin")) {
            ColorUtils.sendMessage((Player) sender, "&c❌ &fNo tienes permisos para usar este comando.");
            return;
        }

        if (args.length < 3) {
            ColorUtils.sendMessage((Player) sender,
                    "&c❌ &fUso: /lobbycore welcome <jugador> <mensaje>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            ColorUtils.sendMessage((Player) sender, "&c❌ &fJugador no encontrado.");
            return;
        }

        // Unir el mensaje desde el argumento 2 en adelante
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        plugin.getWelcomeMessageManager().sendCustomWelcomeMessage(target, message);

        if (sender instanceof Player) {
            ColorUtils.sendMessage((Player) sender,
                    "&a✅ &fMensaje personalizado enviado a &b" + target.getName() + "&f.");
        }
    }

    /**
     * Maneja el comando de versión
     */
    private void handleVersion(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("LobbyCore v" + plugin.getDescription().getVersion() +
                    " por " + plugin.getDescription().getAuthors().get(0));
            return;
        }

        Player player = (Player) sender;

        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "  &b🔧 &fLobbyCore &av" + plugin.getDescription().getVersion(),
                "  &7Por &a" + plugin.getDescription().getAuthors().get(0),
                "  &7Web: &b" + plugin.getDescription().getWebsite(),
                "  &7Integración: &bGrivyzomCore " + (plugin.isGrivyzomIntegrationActive() ? "&a✅" : "&c❌"),
                ""
        ));
    }

    /**
     * Envía el mensaje de ayuda
     */
    private void sendHelpMessage(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("=== LobbyCore Commands ===");
            sender.sendMessage("/lobbycore reload - Recarga la configuración");
            sender.sendMessage("/lobbycore info - Información del plugin");
            sender.sendMessage("/lobbycore test [jugador] - Prueba el mensaje de bienvenida");
            sender.sendMessage("/lobbycore items <list|give|reload> - Gestionar items de acción");
            sender.sendMessage("/lobbycore grivyzom <ping|status|stats> - Comandos GrivyzomCore");
            return;
        }

        Player player = (Player) sender;

        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#FF6B6B▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &b📋 &fComandos de &aLobbyCore",
                "",
                "  &e/lobbycore reload &7- &fRecarga la configuración",
                "  &e/lobbycore info &7- &fInformación del plugin",
                "  &e/lobbycore version &7- &fMuestra la versión",
                "  &e/lobbycore test [jugador] &7- &fPrueba el mensaje de bienvenida",
                "  &e/lobbycore welcome <jugador> <mensaje> &7- &fEnvía mensaje personalizado",
                "  &e/lobbycore items <list|give|reload> &7- &fGestionar items de acción",
                "  &e/lobbycore fireworks <enable|disable|test> &7- &fGestionar fuegos artificiales",
                "",
                "  &b🔧 &fComandos GrivyzomCore:",
                "  &e/lobbycore grivyzom ping &7- &fProbar conexión",
                "  &e/lobbycore grivyzom status &7- &fVer estado de integración",
                "  &e/lobbycore grivyzom stats &7- &fEstadísticas del network",
                "  &e/lobbycore grivyzom data [jugador] &7- &fDatos de jugador",
                "  &e/lobbycore grivyzom top [número] &7- &fTop jugadores",
                "  &e/lobbycore grivyzom reconnect &7- &fForzar reconexión",
                "",
                "  &7Permisos:",
                "  &c• &flobbycore.admin &7- &fAcceso a comandos administrativos",
                "",
                "&#FF6B6B▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "test", "info", "welcome", "version", "items", "fireworks", "grivyzom")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "test":
                case "welcome":
                    return Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "items":
                    return Arrays.asList("list", "give", "reload")
                            .stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "fireworks":
                    return Arrays.asList("enable", "disable", "test", "status")
                            .stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "grivyzom":
                    return Arrays.asList("ping", "status", "stats", "data", "top", "reconnect")
                            .stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("items") && args[1].equalsIgnoreCase("give")) {
                return plugin.getItemActionManager().getAllActionItems().keySet()
                        .stream()
                        .filter(itemId -> itemId.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (args[0].equalsIgnoreCase("grivyzom") && args[1].equalsIgnoreCase("data")) {
                return Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (args[0].equalsIgnoreCase("grivyzom") && args[1].equalsIgnoreCase("top")) {
                return Arrays.asList("3", "5", "10")
                        .stream()
                        .filter(s -> s.startsWith(args[2]))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("items") && args[1].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Arrays.asList();
    }
}