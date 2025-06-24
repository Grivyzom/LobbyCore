package gc.grivyzom.lobbyCore.commands;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
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
            plugin.getConfigManager().reloadConfig();
            plugin.getWelcomeMessageManager().reload();

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

        ColorUtils.sendMessages(player, Arrays.asList(
                "",
                "&#FF6B6B▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &b⚡ &fInformación de &aLobbyCore",
                "",
                "  &7┃ &fVersión: &a" + plugin.getDescription().getVersion(),
                "  &7┃ &fAutor: &a" + plugin.getDescription().getAuthors().get(0),
                "  &7┃ &fWeb: &a" + plugin.getDescription().getWebsite(),
                "  &7┃ &fEstado: &a" + (plugin.getConfigManager().isWelcomeEnabled() ? "Habilitado" : "Deshabilitado"),
                "  &7┃ &fJugadores: &a" + Bukkit.getOnlinePlayers().size() + "&7/&a" + Bukkit.getMaxPlayers(),
                "",
                "  &7Usa &e/lobbycore help &7para ver todos los comandos",
                "",
                "&#FF6B6B▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));
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
            return Arrays.asList("reload", "test", "info", "welcome", "version")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("test") || args[0].equalsIgnoreCase("welcome"))) {
            return Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Arrays.asList();
    }
}