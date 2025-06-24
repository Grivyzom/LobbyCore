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
    private FileConfiguration config;

    public ConfigManager(MainClass plugin) {
        this.plugin = plugin;
    }

    /**
     * Carga o crea la configuración
     */
    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            createDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Verificar y añadir nuevas opciones si no existen
        addDefaults();
        saveConfig();
    }

    /**
     * Crea la configuración por defecto
     */
    private void createDefaultConfig() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    /**
     * Añade valores por defecto si no existen
     */
    private void addDefaults() {
        // Configuración general de bienvenida
        addDefault("welcome.enabled", true);
        addDefault("welcome.delay", 1);

        // Configuración de título
        addDefault("welcome.title.enabled", true);
        addDefault("welcome.title.title", "&#FF6B6B¡B&#FF8E53i&#FFB347e&#FFCC02n&#E4FF02v&#90FF02e&#02FF02n&#02FF90i&#02FFE4d&#02CCFF&#0247FFo&#6B02FF!");
        addDefault("welcome.title.subtitle", "&7¡Disfruta tu estadía en &b{SERVER}&7!");
        addDefault("welcome.title.fade-in", 10);
        addDefault("welcome.title.stay", 40);
        addDefault("welcome.title.fade-out", 10);

        // Mensajes de chat
        addDefault("welcome.messages", Arrays.asList(
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
        addDefault("welcome.time-based.enabled", true);
        addDefault("welcome.time-based.morning", "&e☀ &fBuenos días &b{PLAYER}&f, que tengas un excelente día!");
        addDefault("welcome.time-based.afternoon", "&6🌅 &fBuenas tardes &b{PLAYER}&f, perfecta hora para jugar!");
        addDefault("welcome.time-based.evening", "&c🌆 &fBuenas tardes &b{PLAYER}&f, disfruta la tarde!");
        addDefault("welcome.time-based.night", "&9🌙 &fBuenas noches &b{PLAYER}&f, hora perfecta para relajarse!");

        // Mensajes para nuevos jugadores
        addDefault("welcome.first-time.enabled", true);
        addDefault("welcome.first-time.messages", Arrays.asList(
                "",
                "&a🎉 &f¡Es tu primera vez aquí! Te damos una cálida bienvenida.",
                "&7💡 &fTip: Usa &e/help &fpara ver los comandos disponibles.",
                "&7📋 &fAsegúrate de leer las reglas con &e/rules",
                ""
        ));

        // Anuncio de nuevo jugador
        addDefault("welcome.new-player.announce", true);
        addDefault("welcome.new-player.announcement", "&a🎊 &f¡Démosle la bienvenida a &b{PLAYER} &fque se une por primera vez!");

        // Configuración de sonido
        addDefault("welcome.sound.enabled", true);
        addDefault("welcome.sound.sound", "ENTITY_PLAYER_LEVELUP");
        addDefault("welcome.sound.volume", 0.5);
        addDefault("welcome.sound.pitch", 1.0);

        // Efectos especiales
        addDefault("welcome.effects.gradient.start-color", "FF6B6B");
        addDefault("welcome.effects.gradient.end-color", "4ECDC4");

        // Configuración de base de datos (opcional)
        addDefault("database.enabled", false);
        addDefault("database.host", "localhost");
        addDefault("database.port", 3306);
        addDefault("database.database", "lobbycore");
        addDefault("database.username", "root");
        addDefault("database.password", "password");

        // Configuración de Vault (opcional)
        addDefault("vault.enabled", false);
        addDefault("vault.welcome-money", 100.0);
        addDefault("vault.welcome-money-message", "&a💰 &f¡Has recibido &e${AMOUNT} &fpor unirte al servidor!");

        // Configuración de fuegos artificiales
        addDefault("welcome.fireworks.enabled", true);
        addDefault("welcome.fireworks.delay", 2);
        addDefault("welcome.fireworks.amount", 3);
        addDefault("welcome.fireworks.height", 5);
        addDefault("welcome.fireworks.spread", 3);
        addDefault("welcome.fireworks.types", Arrays.asList("BALL", "STAR", "BURST"));
        addDefault("welcome.fireworks.colors", Arrays.asList("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE"));
        addDefault("welcome.fireworks.fade-colors", Arrays.asList("WHITE", "GRAY"));
        addDefault("welcome.fireworks.flicker", true);
        addDefault("welcome.fireworks.trail", true);
        addDefault("welcome.fireworks.power", 1);

        // Configuración de items de acción
        addDefault("action-items.enabled", true);
        addDefault("action-items.give-delay", 2);

        // Configuración por defecto para el ejemplo del palito del lobby
        if (!config.contains("action-items.items.lobby_stick")) {
            addDefault("action-items.items.lobby_stick.material", "STICK");
            addDefault("action-items.items.lobby_stick.display-name", "&b🏠 &fIr al Lobby");
            addDefault("action-items.items.lobby_stick.lore", Arrays.asList(
                    "&7Click derecho para",
                    "&7teletransportarte al lobby",
                    "",
                    "&a✅ &fDisponible siempre"
            ));
            addDefault("action-items.items.lobby_stick.slot", 4);
            addDefault("action-items.items.lobby_stick.amount", 1);
            addDefault("action-items.items.lobby_stick.flags.give-on-join", true);
            addDefault("action-items.items.lobby_stick.flags.prevent-drop", true);
            addDefault("action-items.items.lobby_stick.flags.prevent-move", true);
            addDefault("action-items.items.lobby_stick.flags.prevent-inventory-click", true);
            addDefault("action-items.items.lobby_stick.flags.keep-on-death", true);
            addDefault("action-items.items.lobby_stick.flags.replaceable", true);
            addDefault("action-items.items.lobby_stick.actions.right-click", Arrays.asList(
                    "[SOUND]ENTITY_ENDERMAN_TELEPORT:1.0:1.2",
                    "[MESSAGE]&a🏠 &f¡Teletransportándote al lobby!",
                    "[CONSOLE]spawn {PLAYER}"
            ));
            addDefault("action-items.items.lobby_stick.actions.left-click", Arrays.asList());
            addDefault("action-items.items.lobby_stick.actions.shift-right-click", Arrays.asList(
                    "[MESSAGE]&7💡 &fUsa click derecho normal para ir al lobby"
            ));
            addDefault("action-items.items.lobby_stick.actions.shift-left-click", Arrays.asList());
        }
    }

    /**
     * Añade un valor por defecto si no existe
     */
    private void addDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    /**
     * Guarda la configuración
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar la configuración: " + e.getMessage());
        }
    }

    /**
     * Recarga la configuración
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Configuración recargada correctamente");
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
     * Obtiene la configuración actual
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Cierra conexiones de base de datos
     */
    public void closeConnections() {
        // Implementar cierre de conexiones de BD si es necesario
        plugin.getLogger().info("Conexiones cerradas correctamente");
    }
}