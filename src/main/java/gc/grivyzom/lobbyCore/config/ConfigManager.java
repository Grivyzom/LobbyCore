package gc.grivyzom.lobbyCore.config;

import gc.grivyzom.lobbyCore.MainClass;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {

    private final MainClass plugin;
    private File configFile;
    private File itemsFile;
    private FileConfiguration config;
    private FileConfiguration itemsConfig;
    private boolean firstTimeSetup = false;

    public ConfigManager(MainClass plugin) {
        this.plugin = plugin;
    }

    /**
     * Carga o crea la configuración principal e items
     */
    public void loadConfig() {
        // Cargar config.yml principal
        loadMainConfig();

        // Cargar items.yml
        loadItemsConfig();
    }

    /**
     * Carga la configuración principal (config.yml)
     */
    private void loadMainConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            createDefaultMainConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Verificar y añadir nuevas opciones si no existen
        addMainConfigDefaults();
        saveMainConfig();
    }

    /**
     * Carga la configuración de items (items.yml)
     */
    private void loadItemsConfig() {
        itemsFile = new File(plugin.getDataFolder(), "items.yml");

        if (!itemsFile.exists()) {
            firstTimeSetup = true;
            createDefaultItemsConfig();
        }

        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        // Solo añadir items por defecto si es la primera vez
        if (firstTimeSetup) {
            plugin.getLogger().info("§e⚙ §fCreando items por defecto por primera vez...");
            addItemsConfigDefaults();
            saveItemsConfig();
            firstTimeSetup = false;
        } else {
            plugin.getLogger().info("§a✓ §fArchivo items.yml cargado - respetando configuración existente");
        }
    }

    /**
     * Crea la configuración principal por defecto
     */
    private void createDefaultMainConfig() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    /**
     * Crea la configuración de items por defecto
     */
    private void createDefaultItemsConfig() {
        try {
            itemsFile.createNewFile();
            plugin.getLogger().info("Archivo items.yml creado correctamente");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al crear items.yml: " + e.getMessage());
        }
    }

    /**
     * Añade valores por defecto a la configuración principal
     */
    private void addMainConfigDefaults() {
        // Configuración general de bienvenida
        addMainDefault("welcome.enabled", true);
        addMainDefault("welcome.delay", 1);

        // Configuración de título
        addMainDefault("welcome.title.enabled", true);
        addMainDefault("welcome.title.title", "&#FF6B6B¡B&#FF8E53i&#FFB347e&#FFCC02n&#E4FF02v&#90FF02e&#02FF02n&#02FF90i&#02FFE4d&#02CCFF&#0247FFo&#6B02FF!");
        addMainDefault("welcome.title.subtitle", "&7¡Disfruta tu estadía en &b{SERVER}&7!");
        addMainDefault("welcome.title.fade-in", 10);
        addMainDefault("welcome.title.stay", 40);
        addMainDefault("welcome.title.fade-out", 10);

        // Mensajes de chat
        addMainDefault("welcome.messages", Arrays.asList(
                "",
                "&#FF6B6B▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "",
                "  &f¡Hola &b{PLAYER}&f! Te damos la bienvenida a &a{SERVER}",
                "",
                "  &7┃ &fJugadores conectados: &a{ONLINE}&7/&a{MAX_PLAYERS}",
                "  &7┃ &fTu mundo actual: &e{WORLD}",
                "  &7┃ &fWeb: &bwww.grivyzom.com",
                "",
                "  &a¡Esperamos que disfrutes tu estadía!",
                "",
                "&#FF6B6B▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ""
        ));

        // Mensajes basados en tiempo
        addMainDefault("welcome.time-based.enabled", true);
        addMainDefault("welcome.time-based.morning", "&e☀ &fBuenos días &b{PLAYER}&f, que tengas un excelente día!");
        addMainDefault("welcome.time-based.afternoon", "&6🌅 &fBuenas tardes &b{PLAYER}&f, perfecta hora para jugar!");
        addMainDefault("welcome.time-based.evening", "&c🌆 &fBuenas tardes &b{PLAYER}&f, disfruta la tarde!");
        addMainDefault("welcome.time-based.night", "&9🌙 &fBuenas noches &b{PLAYER}&f, hora perfecta para relajarse!");

        // Mensajes para nuevos jugadores
        addMainDefault("welcome.first-time.enabled", true);
        addMainDefault("welcome.first-time.messages", Arrays.asList(
                "",
                "&a🎉 &f¡Es tu primera vez aquí! Te damos una cálida bienvenida.",
                "&7💡 &fTip: Usa &e/help &fpara ver los comandos disponibles.",
                "&7📋 &fAsegúrate de leer las reglas con &e/rules",
                ""
        ));

        // Anuncio de nuevo jugador
        addMainDefault("welcome.new-player.announce", true);
        addMainDefault("welcome.new-player.announcement", "&a🎊 &f¡Démosle la bienvenida a &b{PLAYER} &fque se une por primera vez!");

        // Configuración de sonido
        addMainDefault("welcome.sound.enabled", true);
        addMainDefault("welcome.sound.sound", "ENTITY_PLAYER_LEVELUP");
        addMainDefault("welcome.sound.volume", 0.5);
        addMainDefault("welcome.sound.pitch", 1.0);

        // Efectos especiales
        addMainDefault("welcome.effects.gradient.start-color", "FF6B6B");
        addMainDefault("welcome.effects.gradient.end-color", "4ECDC4");

        // Configuración de base de datos (opcional)
        addMainDefault("database.enabled", false);
        addMainDefault("database.host", "localhost");
        addMainDefault("database.port", 3306);
        addMainDefault("database.database", "lobbycore");
        addMainDefault("database.username", "root");
        addMainDefault("database.password", "password");

        // Configuración de Vault (opcional)
        addMainDefault("vault.enabled", false);
        addMainDefault("vault.welcome-money", 100.0);
        addMainDefault("vault.welcome-money-message", "&a💰 &f¡Has recibido &e${AMOUNT} &fpor unirte al servidor!");

        // Configuración de fuegos artificiales
        addMainDefault("welcome.fireworks.enabled", true);
        addMainDefault("welcome.fireworks.delay", 2);
        addMainDefault("welcome.fireworks.amount", 3);
        addMainDefault("welcome.fireworks.height", 5);
        addMainDefault("welcome.fireworks.spread", 3);
        addMainDefault("welcome.fireworks.types", Arrays.asList("BALL", "STAR", "BURST"));
        addMainDefault("welcome.fireworks.colors", Arrays.asList("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE"));
        addMainDefault("welcome.fireworks.fade-colors", Arrays.asList("WHITE", "GRAY"));
        addMainDefault("welcome.fireworks.flicker", true);
        addMainDefault("welcome.fireworks.trail", true);
        addMainDefault("welcome.fireworks.power", 1);

        // Configuración de items de acción (solo configuración general)
        addMainDefault("action-items.enabled", true);
        addMainDefault("action-items.give-delay", 2);

        // Configuración del proxy
        addMainDefault("proxy.type", "velocity");
        addMainDefault("proxy.velocity.use-modern-channel", true);
        addMainDefault("proxy.velocity.connection-timeout", 5000);
        addMainDefault("proxy.velocity.max-retries", 3);
        addMainDefault("proxy.bungeecord.legacy-only", false);
        addMainDefault("proxy.bungeecord.connection-timeout", 3000);
    }

    /**
     * Añade items por defecto SOLO la primera vez que se crea el archivo
     */
    private void addItemsConfigDefaults() {
        plugin.getLogger().info("§e📦 §fCreando items de ejemplo por primera vez...");

        // Header del archivo
        addItemsComment("# ========================================");
        addItemsComment("#         CONFIGURACIÓN DE ITEMS");
        addItemsComment("#              LobbyCore");
        addItemsComment("# ========================================");
        addItemsComment("# NOTA: Los items aquí configurados son ejemplos.");
        addItemsComment("# Puedes eliminar, modificar o añadir nuevos items.");
        addItemsComment("# Si eliminas un item, NO se volverá a crear automáticamente.");
        addItemsComment("# ========================================");

        // Item de selector de servidores
        if (!itemsConfig.contains("server_selector")) {
            addItemsDefault("server_selector.material", "NETHER_STAR");
            addItemsDefault("server_selector.display-name", "&d🌐 &fSelector de Servidores");
            addItemsDefault("server_selector.lore", Arrays.asList(
                    "&7Click para navegar entre",
                    "&7los diferentes servidores",
                    "",
                    "&dClick derecho: &fServidor Survival",
                    "&dClick izquierdo: &fServidor SkyBlock"
            ));
            addItemsDefault("server_selector.slot", 0);
            addItemsDefault("server_selector.amount", 1);
            addItemsDefault("server_selector.flags.give-on-join", true);
            addItemsDefault("server_selector.flags.prevent-drop", true);
            addItemsDefault("server_selector.flags.prevent-move", true);
            addItemsDefault("server_selector.flags.prevent-inventory-click", true);
            addItemsDefault("server_selector.flags.keep-on-death", true);
            addItemsDefault("server_selector.flags.replaceable", true);
            addItemsDefault("server_selector.flags.hide-minecraft-info", true);
            addItemsDefault("server_selector.actions.right-click", Arrays.asList(
                    "[SOUND]BLOCK_PORTAL_TRAVEL:0.8:1.5",
                    "[MESSAGE]&d🌐 &f¡Conectando al servidor &eSurvival&f!",
                    "[SERVER]survival"
            ));
            addItemsDefault("server_selector.actions.left-click", Arrays.asList(
                    "[SOUND]BLOCK_PORTAL_TRAVEL:0.8:1.2",
                    "[MESSAGE]&d🌐 &f¡Conectando al servidor &bSkyBlock&f!",
                    "[SERVER]skyblock"
            ));
        }

        // Item de navegador
        if (!itemsConfig.contains("compass_navigator")) {
            addItemsDefault("compass_navigator.material", "COMPASS");
            addItemsDefault("compass_navigator.display-name", "&e🧭 &fNavegador");
            addItemsDefault("compass_navigator.lore", Arrays.asList(
                    "&7Click derecho para abrir",
                    "&7el menú de navegación",
                    "",
                    "&eClick izquierdo: &fInfo del servidor"
            ));
            addItemsDefault("compass_navigator.slot", 1);
            addItemsDefault("compass_navigator.amount", 1);
            addItemsDefault("compass_navigator.flags.give-on-join", true);
            addItemsDefault("compass_navigator.flags.prevent-drop", true);
            addItemsDefault("compass_navigator.flags.prevent-move", true);
            addItemsDefault("compass_navigator.flags.prevent-inventory-click", true);
            addItemsDefault("compass_navigator.flags.keep-on-death", true);
            addItemsDefault("compass_navigator.flags.replaceable", true);
            addItemsDefault("compass_navigator.flags.hide-minecraft-info", true);
            addItemsDefault("compass_navigator.actions.right-click", Arrays.asList(
                    "[SOUND]UI_BUTTON_CLICK:0.8:1.0",
                    "[CONSOLE]menu navegacion {PLAYER}"
            ));
            addItemsDefault("compass_navigator.actions.left-click", Arrays.asList(
                    "[MESSAGE]&e📊 &fServidor: &b{SERVER}",
                    "[MESSAGE]&e👥 &fJugadores: &a{ONLINE}&7/&a{MAX_PLAYERS}",
                    "[MESSAGE]&e🌍 &fMundo: &e{WORLD}"
            ));
        }

        // Libro de ayuda
        if (!itemsConfig.contains("help_book")) {
            addItemsDefault("help_book.material", "BOOK");
            addItemsDefault("help_book.display-name", "&a📚 &fGuía de Ayuda");
            addItemsDefault("help_book.lore", Arrays.asList(
                    "&7Todo lo que necesitas",
                    "&7saber sobre el servidor",
                    "",
                    "&aClick: &fAbrir guía"
            ));
            addItemsDefault("help_book.slot", 8);
            addItemsDefault("help_book.amount", 1);
            addItemsDefault("help_book.flags.give-on-join", true);
            addItemsDefault("help_book.flags.prevent-drop", true);
            addItemsDefault("help_book.flags.prevent-move", false);
            addItemsDefault("help_book.flags.prevent-inventory-click", false);
            addItemsDefault("help_book.flags.keep-on-death", false);
            addItemsDefault("help_book.flags.replaceable", true);
            addItemsDefault("help_book.flags.hide-minecraft-info", false);
            addItemsDefault("help_book.actions.right-click", Arrays.asList(
                    "[SOUND]ITEM_BOOK_PAGE_TURN:1.0:1.0",
                    "[MESSAGE]&a📚 &f¡Abriendo guía de ayuda!",
                    "[COMMAND]help"
            ));
            addItemsDefault("help_book.actions.left-click", Arrays.asList(
                    "[MESSAGE]&a📚 &fGuía rápida:",
                    "[MESSAGE]&7• &f/help - Ver comandos",
                    "[MESSAGE]&7• &f/spawn - Ir al spawn",
                    "[MESSAGE]&7• &f/menu - Abrir menú principal"
            ));
        }
    }

    /**
     * Añade un valor por defecto a la configuración principal
     */
    private void addMainDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    /**
     * Añade un valor por defecto a la configuración de items
     */
    private void addItemsDefault(String path, Object value) {
        if (value != null && !itemsConfig.contains(path)) {
            itemsConfig.set(path, value);
        }
    }

    /**
     * Añade un comentario al archivo de items
     */
    private void addItemsComment(String comment) {
        // Los comentarios no se pueden añadir directamente con la API de configuración
        // pero se añaden al crear el archivo por primera vez
    }

    /**
     * Elimina un item específico del archivo de configuración
     */
    public boolean removeItem(String itemId) {
        if (itemsConfig.contains(itemId)) {
            itemsConfig.set(itemId, null);
            saveItemsConfig();
            plugin.getLogger().info("§c✓ §fItem '§e" + itemId + "§f' eliminado del archivo items.yml");
            return true;
        }
        return false;
    }

    /**
     * Verifica si un item existe en la configuración
     */
    public boolean itemExists(String itemId) {
        return itemsConfig.contains(itemId);
    }

    /**
     * Obtiene la lista de items disponibles
     */
    public List<String> getAvailableItems() {
        return itemsConfig.getKeys(false).stream()
                .filter(key -> !key.startsWith("#")) // Filtrar comentarios
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Guarda la configuración principal
     */
    public void saveMainConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar config.yml: " + e.getMessage());
        }
    }

    /**
     * Guarda la configuración de items
     */
    public void saveItemsConfig() {
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar items.yml: " + e.getMessage());
        }
    }

    /**
     * Guarda ambas configuraciones
     */
    public void saveConfig() {
        saveMainConfig();
        saveItemsConfig();
    }

    /**
     * Recarga la configuración
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        plugin.getLogger().info("Configuraciones recargadas correctamente");
    }

    // Getters para configuración de bienvenida
    public boolean isWelcomeEnabled() {
        return config.getBoolean("welcome.enabled", true);
    }

    public int getWelcomeDelay() {
        return config.getInt("welcome.delay", 1);
    }

    // Getters para títulos
    public boolean isTitleEnabled() {
        return config.getBoolean("welcome.title.enabled", true);
    }

    public String getWelcomeTitle() {
        return config.getString("welcome.title.title", "¡Bienvenido!");
    }

    public String getWelcomeSubtitle() {
        return config.getString("welcome.title.subtitle", "Disfruta tu estadía");
    }

    public int getTitleFadeIn() {
        return config.getInt("welcome.title.fade-in", 10);
    }

    public int getTitleStay() {
        return config.getInt("welcome.title.stay", 40);
    }

    public int getTitleFadeOut() {
        return config.getInt("welcome.title.fade-out", 10);
    }

    // Getters para mensajes
    public List<String> getWelcomeMessages() {
        return config.getStringList("welcome.messages");
    }

    // Getters para mensajes basados en tiempo
    public boolean isTimeBasedMessagesEnabled() {
        return config.getBoolean("welcome.time-based.enabled", true);
    }

    public String getMorningMessage() {
        return config.getString("welcome.time-based.morning", "¡Buenos días!");
    }

    public String getAfternoonMessage() {
        return config.getString("welcome.time-based.afternoon", "¡Buenas tardes!");
    }

    public String getEveningMessage() {
        return config.getString("welcome.time-based.evening", "¡Buenas tardes!");
    }

    public String getNightMessage() {
        return config.getString("welcome.time-based.night", "¡Buenas noches!");
    }

    // Getters para nuevos jugadores
    public List<String> getFirstTimeMessages() {
        return config.getStringList("welcome.first-time.messages");
    }

    public boolean isAnnounceNewPlayers() {
        return config.getBoolean("welcome.new-player.announce", true);
    }

    public String getNewPlayerAnnouncement() {
        return config.getString("welcome.new-player.announcement", "¡Nuevo jugador!");
    }

    // Getters para sonido
    public boolean isWelcomeSoundEnabled() {
        return config.getBoolean("welcome.sound.enabled", true);
    }

    public String getWelcomeSound() {
        return config.getString("welcome.sound.sound", "ENTITY_PLAYER_LEVELUP");
    }

    public double getWelcomeSoundVolume() {
        return config.getDouble("welcome.sound.volume", 0.5);
    }

    public double getWelcomeSoundPitch() {
        return config.getDouble("welcome.sound.pitch", 1.0);
    }

    // Getters para efectos
    public String getGradientStartColor() {
        return config.getString("welcome.effects.gradient.start-color", "FF6B6B");
    }

    public String getGradientEndColor() {
        return config.getString("welcome.effects.gradient.end-color", "4ECDC4");
    }

    // Getters para base de datos
    public boolean isDatabaseEnabled() {
        return config.getBoolean("database.enabled", false);
    }

    public String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }

    public int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }

    public String getDatabaseName() {
        return config.getString("database.database", "lobbycore");
    }

    public String getDatabaseUsername() {
        return config.getString("database.username", "root");
    }

    public String getDatabasePassword() {
        return config.getString("database.password", "password");
    }

    // Getters para Vault
    public boolean isVaultEnabled() {
        return config.getBoolean("vault.enabled", false);
    }

    public double getWelcomeMoney() {
        return config.getDouble("vault.welcome-money", 100.0);
    }

    public String getWelcomeMoneyMessage() {
        return config.getString("vault.welcome-money-message", "¡Has recibido dinero!");
    }

    // Getters para fuegos artificiales
    public boolean isFireworksEnabled() {
        return config.getBoolean("welcome.fireworks.enabled", true);
    }

    public int getFireworksDelay() {
        return config.getInt("welcome.fireworks.delay", 2);
    }

    public int getFireworksAmount() {
        return config.getInt("welcome.fireworks.amount", 3);
    }

    public int getFireworksHeight() {
        return config.getInt("welcome.fireworks.height", 5);
    }

    public int getFireworksSpread() {
        return config.getInt("welcome.fireworks.spread", 3);
    }

    public List<String> getFireworksTypes() {
        return config.getStringList("welcome.fireworks.types");
    }

    public List<String> getFireworksColors() {
        return config.getStringList("welcome.fireworks.colors");
    }

    public List<String> getFireworksFadeColors() {
        return config.getStringList("welcome.fireworks.fade-colors");
    }

    public boolean isFireworksFlicker() {
        return config.getBoolean("welcome.fireworks.flicker", true);
    }

    public boolean isFireworksTrail() {
        return config.getBoolean("welcome.fireworks.trail", true);
    }

    public int getFireworksPower() {
        return config.getInt("welcome.fireworks.power", 1);
    }

    // Getters para items de acción
    public boolean isActionItemsEnabled() {
        return config.getBoolean("action-items.enabled", true);
    }

    public int getActionItemsGiveDelay() {
        return config.getInt("action-items.give-delay", 2);
    }

    /**
     * Obtiene la configuración principal
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Obtiene la configuración de items
     */
    public FileConfiguration getItemsConfig() {
        return itemsConfig;
    }

    /**
     * Cierra conexiones de base de datos
     */
    public void closeConnections() {
        // Implementar cierre de conexiones de BD si es necesario
        plugin.getLogger().info("Conexiones cerradas correctamente");
    }
}