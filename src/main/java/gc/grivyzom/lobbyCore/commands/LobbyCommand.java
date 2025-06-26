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

    // Agregar estos métodos a tu clase LobbyCommand existente

    /**
     * Muestra la lista de todos los placeholders disponibles
     */
    private void handlePlaceholdersList(Player player) {
        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#FFD700▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &e📋 &fPlaceholders Disponibles &aGrivyzomCore",
                "",
                "  &b👤 &fDatos del Jugador:",
                "  &7• &e%grivyzom_player_name% &7- Nombre del jugador",
                "  &7• &e%grivyzom_coins% &7- Monedas del jugador",
                "  &7• &e%grivyzom_gems% &7- Gemas del jugador",
                "  &7• &e%grivyzom_rank% &7- Rango del jugador",
                "  &7• &e%grivyzom_level% &7- Nivel del jugador",
                "  &7• &e%grivyzom_playtime% &7- Tiempo de juego",
                "",
                "  &b🌐 &fDatos del Network:",
                "  &7• &e%grivyzom_network_players% &7- Jugadores online totales",
                "  &7• &e%grivyzom_online% &7- Alias para network_players",
                "  &7• &e%grivyzom_servers% &7- Servidores activos",
                "  &7• &e%grivyzom_status% &7- Estado del network",
                "  &7• &e%grivyzom_connection_status% &7- Estado de conexión (●)",
                "  &7• &e%grivyzom_connection_latency% &7- Latencia en ms",
                "  &7• &e%grivyzom_server_name% &7- Nombre del servidor",
                "  &7• &e%grivyzom_server_uptime% &7- Tiempo activo",
                "",
                "  &b🏆 &fTop Players (Monedas):",
                "  &7• &e%grivyzom_top_coins_1% &7- Nombre #1",
                "  &7• &e%grivyzom_top_coins_1_amount% &7- Cantidad #1",
                "  &7• &e%grivyzom_top_coins_2% &7- Nombre #2",
                "  &7• &e%grivyzom_top_coins_3% &7- Nombre #3",
                "",
                "  &b💎 &fTop Players (Gemas):",
                "  &7• &e%grivyzom_top_gems_1% &7- Nombre #1",
                "  &7• &e%grivyzom_top_gems_1_amount% &7- Cantidad #1",
                "  &7• &e%grivyzom_top_gems_2% &7- Nombre #2",
                "  &7• &e%grivyzom_top_gems_3% &7- Nombre #3",
                "",
                "  &b💰 &fEconomía Global:",
                "  &7• &e%grivyzom_economy_total_coins% &7- Total monedas",
                "  &7• &e%grivyzom_economy_total_gems% &7- Total gemas",
                "  &7• &e%grivyzom_economy_circulation% &7- % en circulación",
                "",
                "  &b⚡ &fTiempo Real:",
                "  &7• &e%grivyzom_realtime_players% &7- Jugadores actuales",
                "  &7• &e%grivyzom_realtime_tps% &7- TPS del servidor",
                "  &7• &e%grivyzom_realtime_memory% &7- Uso de memoria",
                "",
                "  &7Usa &e/lobbycore placeholders test <categoría> &7para probar",
                "",
                "&#FFD700▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    /**
     * Fuerza el refresco de todos los placeholders
     */
    private void handlePlaceholdersRefresh(Player player) {
        ColorUtils.sendMessage(player, "&e🔄 &fForzando actualización de placeholders...");

        try {
            // Limpiar cache del response handler
            if (plugin.getResponseHandler() != null) {
                plugin.getResponseHandler().clearCache();
            }

            // Refrescar placeholders si están disponibles
            if (plugin.getPlaceholders() != null) {
                plugin.getPlaceholders().refreshData();
            }

            // Solicitar datos frescos si GrivyzomCore está disponible
            if (plugin.getGrivyzomIntegration() != null && plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()) {
                plugin.getGrivyzomIntegration().requestNetworkStats();
                plugin.getGrivyzomIntegration().requestPlayerData(player);
                plugin.getGrivyzomIntegration().requestTopPlayers("COINS", 5);
                plugin.getGrivyzomIntegration().requestTopPlayers("GEMS", 5);
            }

            ColorUtils.sendMessage(player, "&a✅ &fPlaceholders actualizados correctamente");
            ColorUtils.sendMessage(player, "&7Los nuevos datos estarán disponibles en unos segundos");

        } catch (Exception e) {
            ColorUtils.sendMessage(player, "&c❌ &fError actualizando placeholders: " + e.getMessage());
            plugin.getLogger().warning("Error refrescando placeholders: " + e.getMessage());
        }
    }

    /**
     * Muestra estadísticas del sistema de placeholders
     */
    private void handlePlaceholdersStats(Player player) {
        try {
            String cacheStats = "No disponible";
            String placeholderStatus = "No disponible";
            String connectionStatus = "Desconectado";

            // Obtener estadísticas del cache
            if (plugin.getResponseHandler() != null) {
                cacheStats = plugin.getResponseHandler().getCacheStats();
            }

            // Obtener estado de placeholders
            if (plugin.getPlaceholders() != null) {
                var stats = plugin.getPlaceholders().getStats();
                placeholderStatus = stats.toString();
            }

            // Obtener estado de conexión
            if (plugin.getGrivyzomIntegration() != null) {
                if (plugin.getGrivyzomIntegration().isGrivyzomCoreAvailable()) {
                    connectionStatus = "Conectado";
                }
            }

            ColorUtils.sendMessages(player, Arrays.asList(
                    "",
                    "&#9B59B6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    "",
                    "  &d📊 &fEstadísticas de Placeholders &aGrivyzomCore",
                    "",
                    "  &e🔧 &fEstado del Sistema:",
                    "  &7┃ &fGrivyzomCore: &" + (connectionStatus.equals("Conectado") ? "a" : "c") + connectionStatus,
                    "  &7┃ &fPlaceholderAPI: &" + (plugin.getPlaceholders() != null ? "a✅ Registrado" : "c❌ No disponible"),
                    "  &7┃ &fResponse Handler: &" + (plugin.getResponseHandler() != null ? "a✅ Activo" : "c❌ Inactivo"),
                    "",
                    "  &e📈 &fEstadísticas de Cache:",
                    "  &7┃ &f" + cacheStats,
                    "",
                    "  &e⚙ &fEstado de Placeholders:",
                    "  &7┃ &f" + placeholderStatus,
                    "",
                    "  &e🕐 &fTiempos de Actualización (TTL):",
                    "  &7┃ &fDatos de jugador: &a5 minutos",
                    "  &7┃ &fDatos de network: &a1 minuto",
                    "  &7┃ &fTop players: &a2 minutos",
                    "  &7┃ &fEconomía global: &a3 minutos",
                    "",
                    "  &7Usa &e/lobbycore placeholders refresh &7para forzar actualización",
                    "",
                    "&#9B59B6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ""
            ));

        } catch (Exception e) {
            ColorUtils.sendMessage(player, "&c❌ &fError obteniendo estadísticas: " + e.getMessage());
        }
    }

    /**
     * Muestra test específico de placeholders del jugador
     */
    private void showPlayerPlaceholderTest(Player player) {
        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#3498DB▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &b👤 &fTest Placeholders del Jugador",
                "",
                "  &e📊 &fDatos Básicos:",
                "  &7┃ &fNombre: &b" + resolvePlaceholder(player, "%grivyzom_player_name%"),
                "  &7┃ &fRango: &6" + resolvePlaceholder(player, "%grivyzom_rank%"),
                "  &7┃ &fNivel: &a" + resolvePlaceholder(player, "%grivyzom_level%"),
                "",
                "  &e💰 &fRecursos:",
                "  &7┃ &fMonedas: &e" + resolvePlaceholder(player, "%grivyzom_coins%"),
                "  &7┃ &fGemas: &d" + resolvePlaceholder(player, "%grivyzom_gems%"),
                "",
                "  &e⏰ &fTiempo:",
                "  &7┃ &fTiempo jugado: &a" + resolvePlaceholder(player, "%grivyzom_playtime%"),
                "  &7┃ &fPrimera conexión: &7" + resolvePlaceholder(player, "%grivyzom_player_first_join%"),
                "  &7┃ &fÚltima conexión: &7" + resolvePlaceholder(player, "%grivyzom_player_last_join%"),
                "",
                "  &7💡 &fEstos datos se actualizan cada 5 minutos desde GrivyzomCore",
                "  &7🔄 &fSi GrivyzomCore no está disponible, se usan datos simulados",
                "",
                "&#3498DB▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    /**
     * Muestra test específico de placeholders del network
     */
    private void showNetworkPlaceholderTest(Player player) {
        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#27AE60▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &a🌐 &fTest Placeholders del Network",
                "",
                "  &e📊 &fEstadísticas Generales:",
                "  &7┃ &fJugadores online: &a" + resolvePlaceholder(player, "%grivyzom_network_players%"),
                "  &7┃ &fAlias (online): &a" + resolvePlaceholder(player, "%grivyzom_online%"),
                "  &7┃ &fServidores activos: &a" + resolvePlaceholder(player, "%grivyzom_servers%"),
                "  &7┃ &fEstado del network: " + resolvePlaceholder(player, "%grivyzom_status%"),
                "",
                "  &e🔗 &fConexión:",
                "  &7┃ &fEstado visual: " + resolvePlaceholder(player, "%grivyzom_connection_status%"),
                "  &7┃ &fLatencia: &e" + resolvePlaceholder(player, "%grivyzom_connection_latency%"),
                "",
                "  &e🖥 &fServidor Actual:",
                "  &7┃ &fNombre: &b" + resolvePlaceholder(player, "%grivyzom_server_name%"),
                "  &7┃ &fTipo: &b" + resolvePlaceholder(player, "%grivyzom_server_type%"),
                "  &7┃ &fTiempo activo: &a" + resolvePlaceholder(player, "%grivyzom_server_uptime%"),
                "",
                "  &7💡 &fEstos datos se actualizan cada 1 minuto desde el proxy",
                "  &7🔄 &fIncluye datos de todos los servidores del network",
                "",
                "&#27AE60▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    /**
     * Muestra test específico de placeholders de economía
     */
    private void showEconomyPlaceholderTest(Player player) {
        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#F39C12▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &6💰 &fTest Placeholders de Economía",
                "",
                "  &e📈 &fEconomía Global del Servidor:",
                "  &7┃ &fTotal monedas: &e" + resolvePlaceholder(player, "%grivyzom_economy_total_coins%"),
                "  &7┃ &fTotal gemas: &d" + resolvePlaceholder(player, "%grivyzom_economy_total_gems%"),
                "  &7┃ &fCirculación: &a" + resolvePlaceholder(player, "%grivyzom_economy_circulation%"),
                "",
                "  &e👤 &fTus Recursos Personales:",
                "  &7┃ &fTus monedas: &e" + resolvePlaceholder(player, "%grivyzom_coins%"),
                "  &7┃ &fTus gemas: &d" + resolvePlaceholder(player, "%grivyzom_gems%"),
                "",
                "  &e📊 &fAnálisis:",
                "  &7┃ &fLa economía total incluye todos los servidores",
                "  &7┃ &fLos datos se actualizan cada 3 minutos",
                "  &7┃ &fEl % de circulación indica actividad económica",
                "",
                "  &7💡 &fLa economía global crece constantemente con la actividad",
                "  &7🔄 &fTus recursos se sincronizan con el sistema central",
                "",
                "&#F39C12▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    /**
     * Muestra test específico de placeholders de tops
     */
    private void showTopPlaceholderTest(Player player) {
        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#E74C3C▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &c🏆 &fTest Placeholders de Rankings",
                "",
                "  &e👑 &fTop Jugadores por Monedas:",
                "  &7┃ &f#1: &b" + resolvePlaceholder(player, "%grivyzom_top_coins_1%") +
                        " &7(&e" + resolvePlaceholder(player, "%grivyzom_top_coins_1_amount%") + "&7)",
                "  &7┃ &f#2: &b" + resolvePlaceholder(player, "%grivyzom_top_coins_2%") +
                        " &7(&e" + resolvePlaceholder(player, "%grivyzom_top_coins_2_amount%") + "&7)",
                "  &7┃ &f#3: &b" + resolvePlaceholder(player, "%grivyzom_top_coins_3%") +
                        " &7(&e" + resolvePlaceholder(player, "%grivyzom_top_coins_3_amount%") + "&7)",
                "",
                "  &e💎 &fTop Jugadores por Gemas:",
                "  &7┃ &f#1: &b" + resolvePlaceholder(player, "%grivyzom_top_gems_1%") +
                        " &7(&d" + resolvePlaceholder(player, "%grivyzom_top_gems_1_amount%") + "&7)",
                "  &7┃ &f#2: &b" + resolvePlaceholder(player, "%grivyzom_top_gems_2%") +
                        " &7(&d" + resolvePlaceholder(player, "%grivyzom_top_gems_2_amount%") + "&7)",
                "  &7┃ &f#3: &b" + resolvePlaceholder(player, "%grivyzom_top_gems_3%") +
                        " &7(&d" + resolvePlaceholder(player, "%grivyzom_top_gems_3_amount%") + "&7)",
                "",
                "  &e📊 &fInformación:",
                "  &7┃ &fLos rankings se actualizan cada 2 minutos",
                "  &7┃ &fIncluyen datos de todos los servidores del network",
                "  &7┃ &fLas cantidades varían ligeramente para simular actividad",
                "",
                "  &7💡 &f¡Sigue jugando para aparecer en estos rankings!",
                "  &7🔄 &fLos datos provienen directamente de GrivyzomCore",
                "",
                "&#E74C3C▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    /**
     * Muestra test específico de placeholders en tiempo real
     */
    private void showRealtimePlaceholderTest(Player player) {
        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#8E44AD▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &5⚡ &fTest Placeholders en Tiempo Real",
                "",
                "  &e📊 &fDatos del Servidor Actual:",
                "  &7┃ &fJugadores locales: &a" + resolvePlaceholder(player, "%grivyzom_realtime_players%"),
                "  &7┃ &fTPS del servidor: &a" + resolvePlaceholder(player, "%grivyzom_realtime_tps%"),
                "  &7┃ &fUso de memoria: &e" + resolvePlaceholder(player, "%grivyzom_realtime_memory%"),
                "",
                "  &e🔄 &fActualización Continua:",
                "  &7┃ &fEstos datos se actualizan constantemente",
                "  &7┃ &fNo dependen de GrivyzomCore (datos locales)",
                "  &7┃ &fPerfectos para scoreboards dinámicos",
                "",
                "  &e⚙ &fRendimiento:",
                "  &7┃ &fTPS > 19.0 = &a✅ Excelente",
                "  &7┃ &fTPS 18.0-19.0 = &e⚠ Bueno",
                "  &7┃ &fTPS < 18.0 = &c❌ Problemas",
                "",
                "  &e💾 &fMemoria:",
                "  &7┃ &f< 70% = &a✅ Saludable",
                "  &7┃ &f70-85% = &e⚠ Vigilar",
                "  &7┃ &f> 85% = &c❌ Crítico",
                "",
                "  &7💡 &fIdeales para TAB, scoreboards y monitores en tiempo real",
                "",
                "&#8E44AD▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
    }

    /**
     * Resuelve un placeholder para testing
     */
    private String resolvePlaceholder(Player player, String placeholder) {
        try {
            // Si PlaceholderAPI está disponible, usarlo
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                String resolved = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, placeholder);

                // Si el placeholder se resolvió (no devolvió el mismo string)
                if (!resolved.equals(placeholder)) {
                    return resolved;
                }
            }

            // Fallback manual para testing sin PlaceholderAPI
            if (plugin.getPlaceholders() != null) {
                // Extraer el identificador del placeholder
                String identifier = placeholder.replace("%grivyzom_", "").replace("%", "");
                return plugin.getPlaceholders().onPlaceholderRequest(player, identifier);
            }

            // Último fallback
            return "&c[No disponible]";

        } catch (Exception e) {
            plugin.getLogger().warning("Error resolviendo placeholder " + placeholder + ": " + e.getMessage());
            return "&c[Error]";
        }
    }

// También necesitas agregar "placeholders" al método onCommand principal:
// En el switch statement del método onCommand, agrega:
/*
case "placeholders":
    handlePlaceholders(sender, args);
    break;
*/

// Y en el método onTabComplete, en la lista de comandos principales, agrega "placeholders":
/*
return Arrays.asList("reload", "test", "info", "welcome", "version", "items", "fireworks", "grivyzom", "placeholders")
*/

// Y en el segundo nivel de tab completion, agrega:
/*
case "placeholders":
    return Arrays.asList("test", "list", "refresh", "stats")
            .stream()
            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
            .collect(Collectors.toList());
*/

}