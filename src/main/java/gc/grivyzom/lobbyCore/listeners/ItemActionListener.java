package gc.grivyzom.lobbyCore.listeners;

import gc.grivyzom.lobbyCore.MainClass;
import gc.grivyzom.lobbyCore.models.ActionItem;
import gc.grivyzom.lobbyCore.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ItemActionListener implements Listener {

    private final MainClass plugin;
    private final Map<Player, Map<String, ItemStack>> deathItems;

    public ItemActionListener(MainClass plugin) {
        this.plugin = plugin;
        this.deathItems = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        ActionItem actionItem = plugin.getItemActionManager().getActionItemFromItemStack(item);
        if (actionItem == null) return;

        event.setCancelled(true);

        String actionType = "";

        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                if (player.isSneaking()) {
                    actionType = "shift-right-click";
                } else {
                    actionType = "right-click";
                }
                break;
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                if (player.isSneaking()) {
                    actionType = "shift-left-click";
                } else {
                    actionType = "left-click";
                }
                break;
        }

        if (!actionType.isEmpty()) {
            plugin.getItemActionManager().executeItemActions(player, actionItem, actionType);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        ActionItem actionItem = plugin.getItemActionManager().getActionItemFromItemStack(item);

        if (actionItem != null && actionItem.isPreventDrop()) {
            event.setCancelled(true);
            ColorUtils.sendMessage(event.getPlayer(), "&c❌ &fNo puedes soltar este item.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Verificar item clickeado
        if (clickedItem != null) {
            ActionItem actionItem = plugin.getItemActionManager().getActionItemFromItemStack(clickedItem);
            if (actionItem != null) {
                if (actionItem.isPreventInventoryClick()) {
                    event.setCancelled(true);
                    ColorUtils.sendMessage(player, "&c❌ &fNo puedes mover este item.");
                    return;
                }

                if (actionItem.isPreventMove() && event.getSlot() != actionItem.getSlot()) {
                    event.setCancelled(true);
                    ColorUtils.sendMessage(player, "&c❌ &fEste item debe permanecer en su slot asignado.");
                    return;
                }
            }
        }

        // Verificar item en cursor
        if (cursorItem != null) {
            ActionItem actionItem = plugin.getItemActionManager().getActionItemFromItemStack(cursorItem);
            if (actionItem != null && actionItem.isPreventMove()) {
                event.setCancelled(true);
                ColorUtils.sendMessage(player, "&c❌ &fNo puedes mover este item.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack draggedItem = event.getOldCursor();

        if (draggedItem != null) {
            ActionItem actionItem = plugin.getItemActionManager().getActionItemFromItemStack(draggedItem);
            if (actionItem != null && actionItem.isPreventMove()) {
                event.setCancelled(true);
                ColorUtils.sendMessage(player, "&c❌ &fNo puedes mover este item.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (item != null) {
            ActionItem actionItem = plugin.getItemActionManager().getActionItemFromItemStack(item);
            if (actionItem != null && actionItem.isPreventMove() && event.getNewSlot() != actionItem.getSlot()) {
                event.setCancelled(true);

                // Devolver el item a su slot correcto
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            player.getInventory().setItem(actionItem.getSlot(), item);
                            player.getInventory().setItem(event.getNewSlot(), null);
                            ColorUtils.sendMessage(player, "&c❌ &fEste item debe permanecer en el slot " + (actionItem.getSlot() + 1) + ".");
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Map<String, ItemStack> playerKeepItems = new HashMap<>();

        // Buscar items que deben mantenerse al morir
        for (ItemStack item : event.getDrops()) {
            if (item != null) {
                ActionItem actionItem = plugin.getItemActionManager().getActionItemFromItemStack(item);
                if (actionItem != null && actionItem.isKeepOnDeath()) {
                    playerKeepItems.put(actionItem.getItemId(), item.clone());
                }
            }
        }

        // Remover los items que se mantienen de los drops
        event.getDrops().removeIf(item -> {
            if (item != null) {
                ActionItem actionItem = plugin.getItemActionManager().getActionItemFromItemStack(item);
                return actionItem != null && actionItem.isKeepOnDeath();
            }
            return false;
        });

        // Guardar los items para cuando respawnee
        if (!playerKeepItems.isEmpty()) {
            deathItems.put(player, playerKeepItems);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Restaurar items guardados
        Map<String, ItemStack> playerKeepItems = deathItems.remove(player);
        if (playerKeepItems != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        for (Map.Entry<String, ItemStack> entry : playerKeepItems.entrySet()) {
                            String itemId = entry.getKey();
                            ItemStack item = entry.getValue();
                            ActionItem actionItem = plugin.getItemActionManager().getActionItem(itemId);

                            if (actionItem != null) {
                                player.getInventory().setItem(actionItem.getSlot(), item);
                            } else {
                                player.getInventory().addItem(item);
                            }
                        }

                        ColorUtils.sendMessage(player, "&a✅ &fTus items especiales han sido restaurados.");
                    }
                }
            }.runTaskLater(plugin, 20L); // 1 segundo después del respawn
        }
    }
}