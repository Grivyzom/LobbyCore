package gc.grivyzom.lobbyCore;

import gc.grivyzom.lobbyCore.commands.LobbyCommand;
import gc.grivyzom.lobbyCore.config.ConfigManager;
import gc.grivyzom.lobbyCore.listeners.PlayerJoinListener;
import gc.grivyzom.lobbyCore.managers.FireworksManager;
import gc.grivyzom.lobbyCore.managers.WelcomeMessageManager;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MainClass extends JavaPlugin {

    private static MainClass instance;
    private ConfigManager configManager;
    private WelcomeMessageManager welcomeMessageManager;
    private FireworksManager fireworksManager;

    @Override
    public void onEnable() {
        // Asignar instancia
        instance = this;

        // Mensaje de inicio colorido
        getLogger().info(ColorUtils.translate("&a========================================"));
        getLogger().info(ColorUtils.translate("&b  _      _____ ____  ____  __   ______ ____  ____  _____ "));
        getLogger().info(ColorUtils.translate("&b | |    / ___// __ \\/ __ )/ /  / ____// __ \\/ __ \\/ ___/"));
        getLogger().info(ColorUtils.translate("&b | |    \\__ \\/ / / / __  / /  / /    / / / / /_/ /\\__ \\ "));
        getLogger().info(ColorUtils.translate("&b | |______/ / /_/ / /_/ / /__/ /___ / /_/ / _, _/___/ / "));
        getLogger().info(ColorUtils.translate("&b |_____/____/\\____/_____/____/\\____/ \\____/_/ |_|/____/  "));
        getLogger().info(ColorUtils.translate("&a========================================"));
        getLogger().info(ColorUtils.translate("&e» &fVersión: &a" + getDescription().getVersion()));
        getLogger().info(ColorUtils.translate("&e» &fAutor: &a" + getDescription().getAuthors().get(0)));
        getLogger().info(ColorUtils.translate("&e» &fWeb: &a" + getDescription().getWebsite()));
        getLogger().info(ColorUtils.translate("&a========================================"));

        // Inicializar configuración
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        getLogger().info(ColorUtils.translate("&a✓ &fConfiguración cargada correctamente"));

        // Inicializar gestor de mensajes de bienvenida
        welcomeMessageManager = new WelcomeMessageManager(this);
        getLogger().info(ColorUtils.translate("&a✓ &fGestor de mensajes de bienvenida inicializado"));

        // Inicializar gestor de fuegos artificiales
        fireworksManager = new FireworksManager(this);
        getLogger().info(ColorUtils.translate("&a✓ &fGestor de fuegos artificiales inicializado"));

        // Registrar eventos
        registerEvents();
        getLogger().info(ColorUtils.translate("&a✓ &fEventos registrados correctamente"));

        // Registrar comandos
        registerCommands();
        getLogger().info(ColorUtils.translate("&a✓ &fComandos registrados correctamente"));

        // Verificar dependencias
        checkDependencies();

        getLogger().info(ColorUtils.translate("&a✓ &fLobbyCore ha sido habilitado correctamente"));
        getLogger().info(ColorUtils.translate("&a========================================"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ColorUtils.translate("&c========================================"));
        getLogger().info(ColorUtils.translate("&c» &fDeshabilitando LobbyCore..."));

        // Cancelar tareas programadas
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info(ColorUtils.translate("&c✓ &fTareas programadas canceladas"));

        // Cerrar conexiones de base de datos si existen
        if (configManager != null) {
            configManager.closeConnections();
            getLogger().info(ColorUtils.translate("&c✓ &fConexiones de base de datos cerradas"));
        }

        // Limpiar instancias
        welcomeMessageManager = null;
        fireworksManager = null;
        configManager = null;
        instance = null;

        getLogger().info(ColorUtils.translate("&c✓ &fLobbyCore ha sido deshabilitado correctamente"));
        getLogger().info(ColorUtils.translate("&c========================================"));
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    private void registerCommands() {
        LobbyCommand lobbyCommand = new LobbyCommand(this);
        getCommand("lobbycore").setExecutor(lobbyCommand);
        getCommand("lobbycore").setTabCompleter(lobbyCommand);
    }

    private void checkDependencies() {
        // Verificar PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info(ColorUtils.translate("&a✓ &fPlaceholderAPI encontrado"));
        } else {
            getLogger().warning(ColorUtils.translate("&e⚠ &fPlaceholderAPI no encontrado - Algunas funciones pueden no estar disponibles"));
        }

        // Verificar Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            getLogger().info(ColorUtils.translate("&a✓ &fVault encontrado"));
        } else {
            getLogger().warning(ColorUtils.translate("&e⚠ &fVault no encontrado - Funciones de economía no disponibles"));
        }
    }

    // Getters
    public static MainClass getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WelcomeMessageManager getWelcomeMessageManager() {
        return welcomeMessageManager;
    }

    public FireworksManager getFireworksManager() {
        return fireworksManager;
    }
}