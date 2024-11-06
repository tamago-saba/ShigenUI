package net.nutchi.shigenUI;

import com.google.common.collect.ImmutableMap;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ShigenUI extends JavaPlugin implements Listener {
    private final String OVERWORLD = "world";
    private final String SHIGEN = "shigen";
    private final String SHIGEN_NETHER = "shigen_nether";
    private final String SHIGEN_THE_END = "shigen_the_end";

    private final OverworldLastLocations overworldLastLocations = new OverworldLastLocations(getLogger(), getDataFolder());

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        overworldLastLocations.load();
        getServer().getScheduler().runTaskTimerAsynchronously(this, overworldLastLocations::save, 0, 20 * 60 * 5);
    }

    @Override
    public void onDisable() {
        overworldLastLocations.save();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            InventoryGUIListener ui = createUI(player);
            if (ui != null) {
                getServer().getPluginManager().registerEvents(ui, this);
                player.openInventory(ui.getInventory());
            }

            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to != null && to.getWorld() != null && event.getFrom().getWorld() != null) {
            boolean fromOverworld = event.getFrom().getWorld().getName().equals(OVERWORLD);
            boolean toShigen = to.getWorld().getName().equals(SHIGEN) || to.getWorld().getName().equals(SHIGEN_NETHER) || to.getWorld().getName().equals(SHIGEN_THE_END);
            if (fromOverworld && toShigen) {
                overworldLastLocations.setCoordinate(event.getPlayer().getUniqueId(), Coordinate.fromLocation(event.getFrom()));
            }
        }
    }

    @Nullable
    private InventoryGUIListener createUI(Player player) {
        World world = player.getLocation().getWorld();
        if (world != null) {
            switch (world.getName()) {
                case OVERWORLD:
                    return createOverworldUI(player);
                case SHIGEN:
                case SHIGEN_NETHER:
                case SHIGEN_THE_END:
                    return createShigenUI(player);
            }
        }

        return null;
    }

    private InventoryGUIListener createOverworldUI(Player player) {
        Inventory inv = getServer().createInventory(null, InventoryType.HOPPER, "資源ワールドへ移動");

        inv.setItem(0, createWorldItem(player, Material.GRASS_BLOCK, "資源ワールド", 9));
        inv.setItem(2, createWorldItem(player, Material.NETHERRACK, "資源ワールド(ネザー)", 18));
        inv.setItem(4, createWorldItem(player, Material.END_STONE, "資源ワールド(エンド)", 27));

        return new InventoryGUIListener(ImmutableMap.<Integer, Runnable>builder()
                .put(0, () -> teleportToWorld(player, SHIGEN, 9))
                .put(2, () -> teleportToWorld(player, SHIGEN_NETHER, 18))
                .put(4, () -> teleportToWorld(player, SHIGEN_THE_END, 27))
                .build(), inv);
    }

    private InventoryGUIListener createShigenUI(Player player) {
        Inventory inv = getServer().createInventory(null, InventoryType.HOPPER, "オーバーワールドへ移動");

        inv.setItem(2, createWorldItem(player, Material.GRASS_BLOCK, "オーバーワールド", 0));

        return new InventoryGUIListener(ImmutableMap.<Integer, Runnable>builder()
                .put(2, () -> teleportToOverworld(player))
                .build(), inv);
    }

    private ItemStack createWorldItem(Player player, Material material, String displayName, int expCost) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(displayName);
            itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "経験値" + ChatColor.AQUA + expCost + ChatColor.GRAY + "ポイントを消費して移動", canTeleportToWorld(player, expCost) ? ChatColor.GREEN + "クリックで移動" : ChatColor.RED + "経験値が足りません"));
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    private void teleportToOverworld(Player player) {
        if (overworldLastLocations.getCoordinate(player.getUniqueId()).isPresent()) {
            player.teleport(overworldLastLocations.getCoordinate(player.getUniqueId()).get().toLocation(getServer().getWorld(OVERWORLD)));
        } else {
            teleportToWorld(player, OVERWORLD);
        }

        overworldLastLocations.clearCoordinate(player.getUniqueId());
    }

    private boolean canTeleportToWorld(Player player, int expCost) {
        return player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR) || player.getTotalExperience() >= expCost;
    }

    private void teleportToWorld(Player player, String worldName, int expCost) {
        if (player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR)) {
            teleportToWorld(player, worldName);
        } else if (player.getTotalExperience() >= expCost) {
            player.setTotalExperience(player.getTotalExperience() - expCost);
            teleportToWorld(player, worldName);
        }
    }

    private void teleportToWorld(Player player, String worldName) {
        MultiverseCore mv = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (mv != null && mv.getMVWorldManager().isMVWorld(worldName) && player.isOnline()) {
            player.teleport(mv.getMVWorldManager().getMVWorld(worldName).getSpawnLocation());
        }
    }
}
