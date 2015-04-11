package io.github.basicmark.oldmushroom;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.material.Mushroom;
import org.bukkit.plugin.java.JavaPlugin;

public class OldMushroom extends JavaPlugin implements Listener {

	public void onEnable(){
		// Register our event handlers
		getServer().getPluginManager().registerEvents(this, this);
	}
 
	public void onDisable(){
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return false;
	}
 
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		/* Don't process our own events */
		if (event instanceof OldMushroomBlockBreakEvent)
			return;

		Player player = event.getPlayer();
		Block block = event.getBlock();
		BlockState state = block.getState();
		Material material = state.getType();

		if ((material.equals(Material.HUGE_MUSHROOM_1) ||
				material.equals(Material.HUGE_MUSHROOM_2))
				&& player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)){
			Mushroom data = (Mushroom) state.getData();
			if (data.isStem()) {
				/* Cancel the block break */
				event.setCancelled(true);

				/* Dummy the block breakevent for permission plugins etc */
				OldMushroomBlockBreakEvent customEvent;
				customEvent = new OldMushroomBlockBreakEvent(block, player);
				getServer().getPluginManager().callEvent(customEvent);

				/* The event got cancelled so just exit */
				if (customEvent.isCancelled())
					return;

				/* Remove the block */
				state.setType(Material.AIR);
				state.update(true);

				/* Create the itemstack we'll drop instead of the mushroom cap */
				ItemStack drop = new ItemStack(material, 1);
				ItemMeta dropMeta = drop.getItemMeta();
				List<String> lore = new ArrayList<String>();
				lore.add("Stem block");
				dropMeta.setLore(lore);
				drop.setItemMeta(dropMeta);

				/* Drop the item */
				Location loc = block.getLocation();
				loc.getWorld().dropItemNaturally(loc, drop);
			}
		}
	}

	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		Block block = event.getBlockPlaced();
		BlockState state = block.getState();

		/* Don't process our own events */
		if (event instanceof OldMushroomBlockPlaceEvent)
			return;

		if (state.getType().equals(Material.HUGE_MUSHROOM_1) ||
				state.getType().equals(Material.HUGE_MUSHROOM_2)) {
			ItemStack placedItem = event.getItemInHand();
			ItemMeta meta = placedItem.getItemMeta();
			List<String> lore = meta.getLore();
			if ((lore != null) && (!lore.isEmpty())) {
				/* Check for the correct name and the typo version :( */
				if (lore.get(0).contains("Stem block") ||
						lore.get(0).contains("Steam block")) {
					Mushroom data = (Mushroom) state.getData();
					data.setStem();
					state.update(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block == null)
			return;

		BlockState state = block.getState();
		if (state.getType().equals(Material.HUGE_MUSHROOM_1) ||
				state.getType().equals(Material.HUGE_MUSHROOM_2)) {
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				Player player = event.getPlayer();
				ItemStack heldItem = player.getItemInHand();
				if (heldItem.getType().equals(Material.SHEARS)) {
					/* Dummy the block breakevent for permission plugins etc */
					OldMushroomBlockBreakEvent breakEvent;
					breakEvent = new OldMushroomBlockBreakEvent(block, player);
					getServer().getPluginManager().callEvent(breakEvent);

					/* The event got cancelled so just exit */
					if (breakEvent.isCancelled()) {
						return;	
					}

					/* Save the state before the change */
					BlockState prevState = block.getState();

					/* Update the state of the block */
					Mushroom data = (Mushroom) state.getData();
					data.setFacePainted(BlockFace.UP, false);
					data.setFacePainted(BlockFace.DOWN, false);
					data.setFacePainted(BlockFace.NORTH, false);
					data.setFacePainted(BlockFace.SOUTH, false);
					data.setFacePainted(BlockFace.EAST, false);
					data.setFacePainted(BlockFace.WEST, false);
					state.setData(data);
					state.update(true);

					/* Make it look like the player had a mushroom block in their hand */
					ItemStack dummyItem = new ItemStack(state.getType(), 1);

					/* Dummy the block place event for permission plugins etc */
					OldMushroomBlockPlaceEvent placeEvent;
					placeEvent= new OldMushroomBlockPlaceEvent(block, prevState,
							block.getRelative(BlockFace.DOWN), dummyItem, player, true);
					getServer().getPluginManager().callEvent(placeEvent);

					/* The event got cancelled so just exit */
					if (placeEvent.isCancelled()) {
						/* Restore the previous state of the block */
						prevState.update(true);
						return;
					}
				}
			}
		}
	}

	class OldMushroomBlockBreakEvent extends BlockBreakEvent {
		public OldMushroomBlockBreakEvent(Block theBlock, Player player) {
			super(theBlock, player);
		}		
	}

	class OldMushroomBlockPlaceEvent extends BlockPlaceEvent {
		public OldMushroomBlockPlaceEvent(Block placedBlock,
				BlockState replacedBlockState, Block placedAgainst,
				ItemStack itemInHand, Player thePlayer, boolean canBuild) {
			super(placedBlock, replacedBlockState, placedAgainst, itemInHand, thePlayer,
					canBuild);
		}
	}
}

