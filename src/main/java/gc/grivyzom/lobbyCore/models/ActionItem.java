package gc.grivyzom.lobbyCore.models;

import org.bukkit.Material;

import java.util.List;

public class ActionItem {

    private final String itemId;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final int slot;
    private final int amount;

    // Flags
    private final boolean giveOnJoin;
    private final boolean preventDrop;
    private final boolean preventMove;
    private final boolean preventInventoryClick;
    private final boolean keepOnDeath;
    private final boolean replaceable;

    // Acciones
    private final List<String> rightClickActions;
    private final List<String> leftClickActions;
    private final List<String> shiftRightClickActions;
    private final List<String> shiftLeftClickActions;

    public ActionItem(String itemId, Material material, String displayName, List<String> lore,
                      int slot, int amount, boolean giveOnJoin, boolean preventDrop,
                      boolean preventMove, boolean preventInventoryClick, boolean keepOnDeath,
                      boolean replaceable, List<String> rightClickActions, List<String> leftClickActions,
                      List<String> shiftRightClickActions, List<String> shiftLeftClickActions) {
        this.itemId = itemId;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.slot = slot;
        this.amount = amount;
        this.giveOnJoin = giveOnJoin;
        this.preventDrop = preventDrop;
        this.preventMove = preventMove;
        this.preventInventoryClick = preventInventoryClick;
        this.keepOnDeath = keepOnDeath;
        this.replaceable = replaceable;
        this.rightClickActions = rightClickActions;
        this.leftClickActions = leftClickActions;
        this.shiftRightClickActions = shiftRightClickActions;
        this.shiftLeftClickActions = shiftLeftClickActions;
    }

    // Getters
    public String getItemId() { return itemId; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public int getSlot() { return slot; }
    public int getAmount() { return amount; }

    // Flags
    public boolean isGiveOnJoin() { return giveOnJoin; }
    public boolean isPreventDrop() { return preventDrop; }
    public boolean isPreventMove() { return preventMove; }
    public boolean isPreventInventoryClick() { return preventInventoryClick; }
    public boolean isKeepOnDeath() { return keepOnDeath; }
    public boolean isReplaceable() { return replaceable; }

    // Acciones
    public List<String> getRightClickActions() { return rightClickActions; }
    public List<String> getLeftClickActions() { return leftClickActions; }
    public List<String> getShiftRightClickActions() { return shiftRightClickActions; }
    public List<String> getShiftLeftClickActions() { return shiftLeftClickActions; }

    @Override
    public String toString() {
        return "ActionItem{" +
                "itemId='" + itemId + '\'' +
                ", material=" + material +
                ", displayName='" + displayName + '\'' +
                ", slot=" + slot +
                '}';
    }
}