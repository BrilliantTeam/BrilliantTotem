package Rice.Chen.BrilliantTotem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.PlayerInventory;

public class TotemPlugin extends JavaPlugin implements Listener {
    private boolean isFolia;

    @Override
    public void onEnable() {
        isFolia = checkFolia();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("BrilliantTotem has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BrilliantTotem has been disabled!");
    }

    private boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        PlayerInventory inventory = player.getInventory();
        ItemStack mainHand = inventory.getItemInMainHand();
        ItemStack offHand = inventory.getItemInOffHand();
        
        // 如果手上有圖騰，讓原版處理
        if (mainHand.getType() == Material.TOTEM_OF_UNDYING || 
            offHand.getType() == Material.TOTEM_OF_UNDYING) {
            return;
        }
        
        // 取消原版處理，改用我們的邏輯
        event.setCancelled(true);
        
        // 檢查背包和快捷欄
        if (hasTotemInInventory(player)) {
            if (isFolia) {
                player.getScheduler().run(this, task -> {
                    consumeTotem(player);
                    processResurrection(player);
                }, () -> {});
            } else {
                consumeTotem(player);
                processResurrection(player);
            }
            event.setCancelled(false);
        }
    }

    private boolean hasTotemInInventory(Player player) {
        PlayerInventory inventory = player.getInventory();
        
        // 檢查快捷欄
        for (int i = 0; i < 9; i++) {
            if (isTotem(inventory.getItem(i))) return true;
        }
        
        // 檢查背包
        for (int i = 9; i < 36; i++) {
            if (isTotem(inventory.getItem(i))) return true;
        }
        
        return false;
    }
    
    private boolean isTotem(ItemStack item) {
        return item != null && item.getType() == Material.TOTEM_OF_UNDYING;
    }

    private void consumeTotem(Player player) {
        PlayerInventory inventory = player.getInventory();
        
        // 先檢查快捷欄
        for (int i = 0; i < 9; i++) {
            if (consumeTotemAtSlot(inventory, i)) return;
        }
        
        // 再檢查背包
        for (int i = 9; i < 36; i++) {
            if (consumeTotemAtSlot(inventory, i)) return;
        }
    }
    
    private boolean consumeTotemAtSlot(PlayerInventory inventory, int slot) {
        ItemStack item = inventory.getItem(slot);
        if (isTotem(item)) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                inventory.setItem(slot, item);
            } else {
                inventory.setItem(slot, null);
            }
            return true;
        }
        return false;
    }

    private void processResurrection(Player player) {
        player.setHealth(1.0);
        player.setFireTicks(0);
        
        if (isFolia) {
            player.getScheduler().run(this, task -> applyTotemEffects(player), () -> {});
        } else {
            applyTotemEffects(player);
        }
    }

    private void applyTotemEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));
    }
}