package gc.grivyzom.lobbyCore.managers;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.models.ActionItem;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemActionManager {

    private final MainClass plugin;
    private final Map<String, ActionItem> actionItems;

    public ItemActionManager(MainClass plugin) {
        this.plugin = plugin;
        this.actionItems = new HashMap<>();
        loadActionItems();
    }

    /**
     * Carga los items de acción desde la configuración
     */
    public void loadActionItems() {
        actionItems.clear();

        ConfigurationSection itemsSection = plugin.getConfigManager().getConfig().getConfigurationSection("action-items.items");
        if (itemsSection == null) {
            plugin.getLogger().info(ColorUtils.translate("&e⚠ &fNo se encontraron items de acción configurados"));
            return;
        }

        for (String itemId : itemsSection.getKeys(false)) {
            try {
                ActionItem actionItem = loadActionItemFromConfig(itemId, itemsSection.getConfigurationSection(itemId));
                if (actionItem != null) {
                    actionItems.put(itemId, actionItem);
                    plugin.getLogger().info(ColorUtils.translate("&a✓ &fItem de acción cargado: &e" + itemId));
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error al cargar item de acción '" + itemId + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info(ColorUtils.translate("&a✓ &fCargados &e" + actionItems.size() + " &fitems de acción"));
    }

    /**
     * Carga un item de acción desde la configuración
     */
    private ActionItem loadActionItemFromConfig(String itemId, ConfigurationSection section) {
        if (section == null) return null;

        try {
            // Datos básicos del item
            Material material = Material.valueOf(section.getString("material", "STICK").toUpperCase());
            String displayName = section.getString("display-name", "&fItem");
            List<String> lore = section.getStringList("lore");
            int slot = section.getInt("slot", 0);
            int amount = section.getInt("amount", 1);

            // Flags del item
            boolean giveOnJoin = section.getBoolean("flags.give-on-join", false);
            boolean preventDrop = section.getBoolean("flags.prevent-drop", false);
            boolean preventMove = section.getBoolean("flags.prevent-move", false);
            boolean preventInventoryClick = section.getBoolean("flags.prevent-inventory-click", false);
            boolean keepOnDeath = section.getBoolean("flags.keep-on-death", false);
            boolean replaceable = section.getBoolean("flags.replaceable", true);

            // Acciones
            List<String> rightClickActions = section.getStringList("actions.right-click");
            List<String> leftClickActions = section.getStringList("actions.left-click");
            List<String> shiftRightClickActions = section.getStringList("actions.shift-right-click");
            List<String> shiftLeftClickActions = section.getStringList("actions.shift-left-click");

            return new ActionItem(
                    itemId, material, displayName, lore, slot, amount,
                    giveOnJoin, preventDrop, preventMove, preventInventoryClick, keepOnDeath, replaceable,
                    rightClickActions, leftClickActions, shiftRightClickActions, shiftLeftClickActions
            );

        } catch (Exception e) {
            plugin.getLogger().severe("Error al cargar configuración del item '" + itemId + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Da todos los items de join al jugador
     */
    public void giveJoinItems(Player player) {
        if (!plugin.getConfigManager().getConfig().getBoolean("action-items.enabled", true)) {
            return;
        }

        // Delay antes de dar los items
        int delay = plugin.getConfigManager().getConfig().getInt("action-items.give-delay", 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                for (ActionItem actionItem : actionItems.values()) {
                    if (actionItem.isGiveOnJoin()) {
                        giveItemToPlayer(player, actionItem);
                    }
                }
            }
        }.runTaskLater(plugin, delay * 20L);
    }

    /**
     * Da un item específico a un jugador
     */
    public void giveItemToPlayer(Player player, ActionItem actionItem) {
        ItemStack itemStack = createItemStack(actionItem, player);

        int slot = actionItem.getSlot();

        // Verificar si el slot está ocupado
        if (slot >= 0 && slot < 36) { // 36 slots de inventario (0-35)
            ItemStack existingItem = player.getInventory().getItem(slot);

            if (existingItem != null && !existingItem.getType().isAir() && !actionItem.isReplaceable()) {
                // Si no es reemplazable, buscar un slot vacío
                slot = player.getInventory().firstEmpty();
                if (slot == -1) {
                    ColorUtils.sendMessage(player, "&c❌ &fNo hay espacio en el inventario para el item: &e" + actionItem.getDisplayName());
                    return;
                }
            }

            player.getInventory().setItem(slot, itemStack);
        } else {
            // Slot inválido, dar en cualquier lugar disponible
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
            if (!leftover.isEmpty()) {
                ColorUtils.sendMessage(player, "&c❌ &fNo hay espacio en el inventario para el item: &e" + actionItem.getDisplayName());
            }
        }
    }

    /**
     * Crea un ItemStack basado en un ActionItem
     */
    private ItemStack createItemStack(ActionItem actionItem, Player player) {
        ItemStack itemStack = new ItemStack(actionItem.getMaterial(), actionItem.getAmount());
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            // Nombre del item
            String displayName = replacePlaceholders(player, actionItem.getDisplayName());
            meta.setDisplayName(ColorUtils.translate(displayName));

            // Lore del item
            List<String> lore = new ArrayList<>();
            for (String loreLine : actionItem.getLore()) {
                String processedLine = replacePlaceholders(player, loreLine);
                lore.add(ColorUtils.translate(processedLine));
            }
            meta.setLore(lore);

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    /**
     * Ejecuta las acciones de un item
     */
    public void executeItemActions(Player player, ActionItem actionItem, String actionType) {
        List<String> actions = getActionsForType(actionItem, actionType);

        if (actions.isEmpty()) return;

        for (String action : actions) {
            executeAction(player, action);
        }
    }

    /**
     * Obtiene las acciones para un tipo específico
     */
    private List<String> getActionsForType(ActionItem actionItem, String actionType) {
        switch (actionType.toLowerCase()) {
            case "right-click":
                return actionItem.getRightClickActions();
            case "left-click":
                return actionItem.getLeftClickActions();
            case "shift-right-click":
                return actionItem.getShiftRightClickActions();
            case "shift-left-click":
                return actionItem.getShiftLeftClickActions();
            default:
                return new ArrayList<>();
        }
    }

    /**
     * Ejecuta una acción individual
     */
    private void executeAction(Player player, String action) {
        try {
            String processedAction = replacePlaceholders(player, action);

            if (processedAction.startsWith("[COMMAND]")) {
                // Ejecutar comando como jugador
                String command = processedAction.replace("[COMMAND]", "").trim();
                player.performCommand(command);

            } else if (processedAction.startsWith("[CONSOLE]")) {
                // Ejecutar comando desde consola
                String command = processedAction.replace("[CONSOLE]", "").trim();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            } else if (processedAction.startsWith("[MESSAGE]")) {
                // Enviar mensaje al jugador
                String message = processedAction.replace("[MESSAGE]", "").trim();
                ColorUtils.sendMessage(player, message);

            } else if (processedAction.startsWith("[BROADCAST]")) {
                // Broadcast a todos los jugadores
                String message = processedAction.replace("[BROADCAST]", "").trim();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    ColorUtils.sendMessage(onlinePlayer, message);
                }

            } else if (processedAction.startsWith("[SOUND]")) {
                // Reproducir sonido
                String[] parts = processedAction.replace("[SOUND]", "").trim().split(":");
                String soundName = parts[0];
                float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;

                try {
                    org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName.toUpperCase());
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Sonido inválido: " + soundName);
                }

            } else if (processedAction.startsWith("[TELEPORT]")) {
                // Teletransportar a coordenadas específicas
                String coords = processedAction.replace("[TELEPORT]", "").trim();
                String[] parts = coords.split(":");

                if (parts.length >= 3) {
                    try {
                        double x = Double.parseDouble(parts[0]);
                        double y = Double.parseDouble(parts[1]);
                        double z = Double.parseDouble(parts[2]);
                        String worldName = parts.length > 3 ? parts[3] : player.getWorld().getName();

                        org.bukkit.World world = Bukkit.getWorld(worldName);
                        if (world != null) {
                            org.bukkit.Location location = new org.bukkit.Location(world, x, y, z);
                            player.teleport(location);
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Coordenadas inválidas: " + coords);
                    }
                }

            } else if (processedAction.startsWith("[GIVE_ITEM]")) {
                // Dar item al jugador
                String itemId = processedAction.replace("[GIVE_ITEM]", "").trim();
                ActionItem item = actionItems.get(itemId);
                if (item != null) {
                    giveItemToPlayer(player, item);
                }

            } else if (processedAction.startsWith("[CLOSE_INVENTORY]")) {
                // Cerrar inventario
                player.closeInventory();

            } else if (processedAction.startsWith("[DELAY]")) {
                // Ejecutar acción con delay
                String[] parts = processedAction.split(":", 2);
                if (parts.length == 2) {
                    try {
                        int delay = Integer.parseInt(parts[0].replace("[DELAY]", "").trim());
                        String delayedAction = parts[1];

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                executeAction(player, delayedAction);
                            }
                        }.runTaskLater(plugin, delay * 20L);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Delay inválido en acción: " + processedAction);
                    }
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error ejecutando acción '" + action + "': " + e.getMessage());
        }
    }

    /**
     * Reemplaza placeholders en una cadena
     */
    private String replacePlaceholders(Player player, String text) {
        return text.replace("{PLAYER}", player.getName())
                .replace("{DISPLAYNAME}", player.getDisplayName())
                .replace("{WORLD}", player.getWorld().getName())
                .replace("{ONLINE}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{MAX_PLAYERS}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{SERVER}", Bukkit.getServer().getName());
    }

    /**
     * Obtiene un item de acción por su ID
     */
    public ActionItem getActionItem(String itemId) {
        return actionItems.get(itemId);
    }

    /**
     * Obtiene todos los items de acción
     */
    public Map<String, ActionItem> getAllActionItems() {
        return new HashMap<>(actionItems);
    }

    /**
     * Verifica si un ItemStack es un item de acción
     */
    public ActionItem getActionItemFromItemStack(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) {
            return null;
        }

        String itemDisplayName = itemStack.getItemMeta().getDisplayName();

        for (ActionItem actionItem : actionItems.values()) {
            String translatedName = ColorUtils.translate(actionItem.getDisplayName());
            if (itemDisplayName.equals(translatedName)) {
                return actionItem;
            }
        }

        return null;
    }

    /**
     * Recarga todos los items de acción
     */
    public void reload() {
        loadActionItems();
        plugin.getLogger().info(ColorUtils.translate("&a✓ &fGestor de items de acción recargado"));
    }
}