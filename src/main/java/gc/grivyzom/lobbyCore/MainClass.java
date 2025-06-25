package gc.grivyzom.lobbyCore;

import gc.grivyzom.lobbyCore.commands.LobbyCommand;
import gc.grivyzom.lobbyCore.config.ConfigManager;
import gc.grivyzom.lobbyCore.integration.GrivyzomCoreIntegration;
import gc.grivyzom.lobbyCore.integration.GrivyzomResponseHandler;
import gc.grivyzom.lobbyCore.integration.GrivyzomPlaceholders;
import gc.grivyzom.lobbyCore.listeners.ItemActionListener;
import gc.grivyzom.lobbyCore.listeners.PlayerJoinListener;
import gc.grivyzom.lobbyCore.managers.FireworksManager;
import gc.grivyzom.lobbyCore.managers.ItemActionManager;
import gc.grivyzom.lobbyCore.managers.WelcomeMessageManager;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class MainClass extends JavaPlugin {

    private static MainClass instance;
    private ConfigManager configManager;
    private WelcomeMessageManager welcomeMessageManager;
    private FireworksManager fireworksManager;
    private ItemActionManager itemActionManager;

    // Componentes de integración con GrivyzomCore
    private GrivyzomCoreIntegration grivyzomIntegration;
    private GrivyzomResponseHandler responseHandler;
    private GrivyzomPlaceholders placeholders;

    @Override
    public void onEnable() {
        // Asignar instancia
        instance = this;

        // Mensaje de inicio colorido
        sendStartupMessage();

        try {
            // Inicializar configuración
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            getLogger().info(ColorUtils.translate("&a✓ &fConfiguración cargada correctamente"));

            // Inicializar gestores principales
            initializeManagers();

            // Inicializar integración con GrivyzomCore
            initializeGrivyzomIntegration();

            // Registrar eventos y comandos
            registerEvents();
            registerCommands();

            // Verificar dependencias
            checkDependencies();

            // Tareas de inicialización tardía
            schedulePostInitTasks();

            getLogger().info(ColorUtils.translate("&a✓ &fLobbyCore ha sido habilitado correctamente"));
            getLogger().info(ColorUtils.translate("&a========================================"));

        } catch (Exception e) {
            getLogger().severe("❌ Error crítico durante la inicialización: " + e.getMessage());
            e.printStackTrace();
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(ColorUtils.translate("&c========================================"));
        getLogger().info(ColorUtils.translate("&c» &fDeshabilitando LobbyCore..."));

        try {
            // Cancelar tareas programadas
            Bukkit.getScheduler().cancelTasks(this);
            getLogger().info(ColorUtils.translate("&c✓ &fTareas programadas canceladas"));

            // Desregistrar placeholders
            if (placeholders != null && placeholders.isWorking()) {
                // PlaceholderAPI maneja el unregister automáticamente
                getLogger().info(ColorUtils.translate("&c✓ &fPlaceholders desregistrados"));
            }

            // Cerrar conexiones de base de datos si existen
            if (configManager != null) {
                configManager.closeConnections();
                getLogger().info(ColorUtils.translate("&c✓ &fConexiones cerradas"));
            }

            // Limpiar instancias
            cleanupInstances();

            getLogger().info(ColorUtils.translate("&c✓ &fLobbyCore ha sido deshabilitado correctamente"));
            getLogger().info(ColorUtils.translate("&c========================================"));

        } catch (Exception e) {
            getLogger().severe("Error durante el cierre: " + e.getMessage());
        }
    }

    /**
     * Envía el mensaje de inicio con arte ASCII
     */
    private void sendStartupMessage() {
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
        getLogger().info(ColorUtils.translate("&e» &fIntegración: &bGrivyzomCore"));
        getLogger().info(ColorUtils.translate("&a========================================"));
    }

    /**
     * Inicializa los gestores principales
     */
    private void initializeManagers() {
        welcomeMessageManager = new WelcomeMessageManager(this);
        getLogger().info(ColorUtils.translate("&a✓ &fGestor de mensajes de bienvenida inicializado"));

        fireworksManager = new FireworksManager(this);
        getLogger().info(ColorUtils.translate("&a✓ &fGestor de fuegos artificiales inicializado"));

        itemActionManager = new ItemActionManager(this);
        getLogger().info(ColorUtils.translate("&a✓ &fGestor de items de acción inicializado"));
    }

    /**
     * Inicializa la integración con GrivyzomCore
     */
    private void initializeGrivyzomIntegration() {
        getLogger().info(ColorUtils.translate("&e🔧 &fInicializando integración con GrivyzomCore..."));

        try {
            // Inicializar manejador de respuestas
            responseHandler = new GrivyzomResponseHandler(this);
            getLogger().info(ColorUtils.translate("&a✓ &fManejador de respuestas GrivyzomCore inicializado"));

            // Inicializar integración principal
            grivyzomIntegration = new GrivyzomCoreIntegration(this);
            getLogger().info(ColorUtils.translate("&a✓ &fIntegración con GrivyzomCore inicializada"));

            // Inicializar placeholders si PlaceholderAPI está disponible
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                placeholders = new GrivyzomPlaceholders(this, responseHandler);
                placeholders.register();
                getLogger().info(ColorUtils.translate("&a✓ &fPlaceholders integrados registrados"));
            } else {
                getLogger().warning(ColorUtils.translate("&e⚠ &fPlaceholderAPI no encontrado - Placeholders limitados"));
            }

            getLogger().info(ColorUtils.translate("&a✅ &fIntegración GrivyzomCore completada exitosamente"));

        } catch (Exception e) {
            getLogger().severe("❌ Error inicializando integración GrivyzomCore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registra los eventos del plugin
     */
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemActionListener(this), this);
        getLogger().info(ColorUtils.translate("&a✓ &fEventos registrados correctamente"));
    }

    /**
     * Registra los comandos del plugin
     */
    private void registerCommands() {
        LobbyCommand lobbyCommand = new LobbyCommand(this);
        getCommand("lobbycore").setExecutor(lobbyCommand);
        getCommand("lobbycore").setTabCompleter(lobbyCommand);
        getLogger().info(ColorUtils.translate("&a✓ &fComandos registrados correctamente"));
    }

    /**
     * Verifica las dependencias del plugin
     */
    private void checkDependencies() {
        // Verificar PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info(ColorUtils.translate("&a✓ &fPlaceholderAPI encontrado"));
        } else {
            getLogger().warning(ColorUtils.translate("&e⚠ &fPlaceholderAPI no encontrado - Placeholders limitados"));
        }

        // Verificar Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            getLogger().info(ColorUtils.translate("&a✓ &fVault encontrado"));
        } else {
            getLogger().warning(ColorUtils.translate("&e⚠ &fVault no encontrado - Funciones de economía limitadas"));
        }

        // Mostrar información de integración
        getLogger().info(ColorUtils.translate("&e🌐 &fIntegración GrivyzomCore: &b" +
                (grivyzomIntegration != null ? "Activa" : "Inactiva")));
    }

    /**
     * Programa tareas de inicialización tardía
     */
    private void schedulePostInitTasks() {
        // Verificar conexión con GrivyzomCore después de 3 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                if (grivyzomIntegration != null) {
                    checkGrivyzomCoreConnection();
                }
            }
        }.runTaskLater(this, 60L); // 3 segundos

        // Sincronización automática de datos cada 5 minutos
        new BukkitRunnable() {
            @Override
            public void run() {
                if (grivyzomIntegration != null && grivyzomIntegration.isGrivyzomCoreAvailable()) {
                    grivyzomIntegration.requestNetworkStats();
                }
            }
        }.runTaskTimer(this, 100L, 6000L); // Cada 5 minutos
    }

    /**
     * Verifica la conexión con GrivyzomCore
     */
    private void checkGrivyzomCoreConnection() {
        if (grivyzomIntegration != null && grivyzomIntegration.isGrivyzomCoreAvailable()) {
            getLogger().info(ColorUtils.translate("&a✅ &fConexión con GrivyzomCore establecida"));

            // Mostrar información de funciones disponibles
            getLogger().info(ColorUtils.translate("&e📋 &fFunciones disponibles:"));
            getLogger().info(ColorUtils.translate("&7  • &fSincronización de datos de jugadores"));
            getLogger().info(ColorUtils.translate("&7  • &fActualizaciones de economía en tiempo real"));
            getLogger().info(ColorUtils.translate("&7  • &fEstadísticas del network"));
            getLogger().info(ColorUtils.translate("&7  • &fPlaceholders integrados"));
            getLogger().info(ColorUtils.translate("&7  • &fEventos y notificaciones"));

        } else {
            getLogger().warning(ColorUtils.translate("&c⚠ &fNo se pudo establecer conexión con GrivyzomCore"));
            getLogger().warning(ColorUtils.translate("&7Funcionando con datos por defecto para demostración"));

            // Reintentar conexión en 30 segundos
            if (grivyzomIntegration != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (grivyzomIntegration != null && !getServer().getOnlinePlayers().isEmpty()) {
                            grivyzomIntegration.sendPingToGrivyzomCore();
                        } else {
                            getLogger().info(ColorUtils.translate("&e⚠ &fReintento de conexión pospuesto - sin jugadores online"));
                        }
                    }
                }.runTaskLater(this, 600L); // 30 segundos
            }
        }
    }

    /**
     * Limpia todas las instancias al deshabilitar
     */
    private void cleanupInstances() {
        welcomeMessageManager = null;
        fireworksManager = null;
        itemActionManager = null;
        grivyzomIntegration = null;
        responseHandler = null;
        placeholders = null;
        configManager = null;
        instance = null;
    }

    /**
     * Recarga toda la configuración y componentes
     */
    public void reloadAll() {
        try {
            getLogger().info(ColorUtils.translate("&e🔄 &fIniciando recarga completa..."));

            // Recargar configuración
            configManager.reloadConfig();

            // Recargar gestores
            if (welcomeMessageManager != null) welcomeMessageManager.reload();
            if (fireworksManager != null) fireworksManager.reload();
            if (itemActionManager != null) itemActionManager.reload();

            // Verificar conexión con GrivyzomCore
            if (grivyzomIntegration != null && !getServer().getOnlinePlayers().isEmpty()) {
                grivyzomIntegration.sendPingToGrivyzomCore();
            }

            // Limpiar cache de respuestas
            if (responseHandler != null) {
                responseHandler.clearCache();
            }

            getLogger().info(ColorUtils.translate("&a✅ &fRecarga completa finalizada"));

        } catch (Exception e) {
            getLogger().severe("Error durante la recarga: " + e.getMessage());
        }
    }

    /**
     * Verifica si la integración está disponible y funcionando
     */
    public boolean isGrivyzomIntegrationActive() {
        return grivyzomIntegration != null && grivyzomIntegration.isGrivyzomCoreAvailable();
    }

    /**
     * Obtiene estadísticas del plugin
     */
    public PluginStats getPluginStats() {
        return new PluginStats(
                Bukkit.getOnlinePlayers().size(),
                isGrivyzomIntegrationActive(),
                placeholders != null && placeholders.isWorking(),
                itemActionManager != null ? itemActionManager.getAllActionItems().size() : 0
        );
    }

    // Getters para acceso desde otros componentes
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

    public ItemActionManager getItemActionManager() {
        return itemActionManager;
    }

    public GrivyzomCoreIntegration getGrivyzomIntegration() {
        return grivyzomIntegration;
    }

    public GrivyzomResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public GrivyzomPlaceholders getPlaceholders() {
        return placeholders;
    }

    /**
     * Clase para estadísticas del plugin
     */
    public static class PluginStats {
        private final int onlinePlayers;
        private final boolean grivyzomConnected;
        private final boolean placeholdersActive;
        private final int actionItemsLoaded;

        public PluginStats(int onlinePlayers, boolean grivyzomConnected,
                           boolean placeholdersActive, int actionItemsLoaded) {
            this.onlinePlayers = onlinePlayers;
            this.grivyzomConnected = grivyzomConnected;
            this.placeholdersActive = placeholdersActive;
            this.actionItemsLoaded = actionItemsLoaded;
        }

        // Getters
        public int getOnlinePlayers() { return onlinePlayers; }
        public boolean isGrivyzomConnected() { return grivyzomConnected; }
        public boolean isPlaceholdersActive() { return placeholdersActive; }
        public int getActionItemsLoaded() { return actionItemsLoaded; }

        @Override
        public String toString() {
            return String.format(
                    "PluginStats{players=%d, grivyzom=%s, placeholders=%s, items=%d}",
                    onlinePlayers, grivyzomConnected, placeholdersActive, actionItemsLoaded
            );
        }
    }
}