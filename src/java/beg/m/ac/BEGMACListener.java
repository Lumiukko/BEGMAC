package beg.m.ac;

import java.util.*;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author makko
 */
public class BEGMACListener implements Listener {
	
	private static Map<String, Calendar> playTimes = new HashMap<String, Calendar>();
	private static Set<String> ignoredCommands = new HashSet<String>();
	
	
	private boolean isCheating(Player player) {
		if (player.isFlying()) return true;
		return false;
	}
		
	@EventHandler
	public void initPlayer(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		Integer playerID = BEGMAC.pu.getPlayerID(playerName);
		if (playerID == null) {
			BEGMAC.print("ERROR", "Unable to get playerID.");
		}
		else {
			BEGMAC.print("Player joined", "[" + playerID + "] " + playerName);
		}
		
		playTimes.put(playerName, new GregorianCalendar());
		
		List<Player> playerList = player.getWorld().getPlayers();
		for (Player p : playerList) {
			BEGMAC.addAction(new Action(p.getName(), "PLAYERS_CONCURRENT_ONLINE", playerList.size(), Modifier.SET_LARGER));
		}
		BEGMAC.addAction(new Action(playerName, "PLAYERS_CONCURRENT_ONLINE", playerList.size(), Modifier.SET_LARGER));
	}
	
	
	@EventHandler
	public void quitPlayer(PlayerQuitEvent event) {
		if (event != null) {
			Player player = event.getPlayer();
			savePlayerTime(player);
		}
		else {
			System.err.println("Could not receive PlayerQuitEvent... stats may be corrupted.");
		}
	}
	
	
	public void savePlayerTime(Player player) {
		Calendar joinTime = playTimes.get(player.getName());
		Calendar currTime = new GregorianCalendar();
		Long playTime = Math.abs((currTime.getTimeInMillis() - joinTime.getTimeInMillis()) / 1000);
		BEGMAC.addAction(new Action(player.getName(), "TIME_PLAYED", playTime.intValue()));
	}

	
	@EventHandler
	public void breakBlock(BlockBreakEvent event) {
		// Cheat protection
		if (isCheating(event.getPlayer())) return;
		
		String playerName = event.getPlayer().getName();
		String blockType = event.getBlock().getType().toString();
		//BEGMAC.addAction(new Action(playerName, "BLOCKS_BROKEN", 1));
		BEGMAC.addAction(new Action(playerName, "BLOCKS_BROKEN_" + blockType, 1));
	}
	
	
	@EventHandler
	public void placeBlock(BlockPlaceEvent event) {
		// Cheat protection
		if (isCheating(event.getPlayer())) return;
				
		Player player = event.getPlayer();
		String playerName = player.getName();
		String blockType = event.getBlock().getType().toString();
		//BEGMAC.addAction(new Action(playerName, "BLOCKS_PLACED", 1));
		BEGMAC.addAction(new Action(playerName, "BLOCKS_PLACED_" + blockType, 1));
	}
	
	
	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		// Cheat protection
		if (isCheating(event.getPlayer())) return;
		
		String playerName = event.getPlayer().getName();
		
		World world = event.getTo().getWorld();
		
		// LOCATION_NETHER_REACHED
		String locAttr = "LOCATION_NETHER_REACHED";
		if (!ignoredCommands.contains(playerName + locAttr)) {
			if (world.getEnvironment().equals(Environment.NETHER)) {
				BEGMAC.addAction(new Action(playerName, locAttr, 1, Modifier.SET_LARGER));
			}
		}
		
		// LOCATION_THE_END_REACHED
		locAttr = "LOCATION_THE_END_REACHED";
		if (!ignoredCommands.contains(playerName + locAttr)) {
			if (world.getEnvironment().equals(Environment.THE_END)) {
				BEGMAC.addAction(new Action(playerName, locAttr, 1, Modifier.SET_LARGER));
				ignoredCommands.add(playerName + locAttr);
			}
		}
		
		// BIOME VISITED
		Biome biome = event.getTo().getBlock().getBiome();
		locAttr = "BIOME_" + biome.name() + "_VISITED";
		
		if (!ignoredCommands.contains(playerName + locAttr)) {	
			if (biome != null) {
				BEGMAC.addAction(new Action(playerName, locAttr, 1, Modifier.SET_LARGER));
				ignoredCommands.add(playerName + locAttr);
			}
		}
		
		// HIGHEST POINT
		locAttr = "LOCATION_HIGHEST_REACHED";
		
		if (!ignoredCommands.contains(playerName + locAttr)) {
			int maxY = world.getMaxHeight();
			int curY = event.getTo().getBlockY();

			if (maxY == curY) {
				// highest point reached (need to jump for this, because block can't be placed here)
				BEGMAC.addAction(new Action(playerName, locAttr, 1, Modifier.SET_LARGER));
				ignoredCommands.add(playerName + locAttr);
			}
		}
	}
	
	
	@EventHandler
	public void entityDeath(EntityDeathEvent event) {
		if (event != null) {
			LivingEntity entity = event.getEntity();
			if (entity.isDead()) {
				if (entity.getType().equals(EntityType.PLAYER)) {
					String playerName = ((Player)entity).getName();
					
					// Cheat protection
					if (isCheating((Player)entity)) return;
					
					EntityDamageEvent deathCause = event.getEntity().getLastDamageCause();
					String causeOfDeath = "";
					if (deathCause.getCause().equals(DamageCause.ENTITY_ATTACK)) {
						EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent)deathCause;
						causeOfDeath = entityDamageEvent.getDamager().toString();
					}
					else if (deathCause.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
						EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent)deathCause;
						causeOfDeath = entityDamageEvent.getDamager() + "_EXPLOSION";
					}
					else {
						causeOfDeath = deathCause.getCause().toString();
					}

					if (causeOfDeath.equals("")) causeOfDeath = "UNKNOWN";
					BEGMAC.addAction(new Action(playerName, "PLAYER_DEATH_BY_" + causeOfDeath.toUpperCase(), 1));					
				}
				else {
					Player player = entity.getKiller();
					
					if (player != null) {
						String playerName = player.getName();
						
						// Cheat protection
						if (isCheating(player)) return;
						
						String weapon = player.getItemInHand().getType().toString();
						BEGMAC.addAction(new Action(playerName, "MONSTERS_KILLED_" + entity.toString().toUpperCase() + "_WITH_" + weapon, 1));
						//BEGMAC.addAction(new Action(playerName, "MONSTERS_KILLED_" + entity.toString().toUpperCase(), 1));
					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void eggThrow(PlayerEggThrowEvent event) {
		// Cheat protection
		if (isCheating(event.getPlayer())) return;
		
		String playerName = event.getPlayer().getName();
		BEGMAC.addAction(new Action(playerName, "EGGS_THROWN", 1));	
	}
	
	
	/* // encourages statspadding... so: deprecated
	@EventHandler
	public void pickup(PlayerPickupItemEvent event) {
		String playerName = event.getPlayer().getName();
		ItemStack itemStack = event.getItem().getItemStack();
		Integer amount = itemStack.getAmount();
		String itemName = itemStack.getType().toString();
		BEGMAC.addAction(new Action(playerName, "ITEM_PICKUP_" + itemName.toUpperCase(), amount));
	}
	*/
	
	
	@EventHandler
	public void ignite(BlockIgniteEvent event) {
		Player player = event.getPlayer();
		if (player != null) {
		
			// Cheat protection
			if (isCheating(event.getPlayer())) return;
		
			if (event.getCause().equals(IgniteCause.FLINT_AND_STEEL)) {
				BEGMAC.addAction(new Action(player.getName(), "FIRES_STARTED", 1));
			}
		}
	}
	
	
	@EventHandler
	public void levelUp(PlayerLevelChangeEvent event) {
		// Cheat protection
		if (isCheating(event.getPlayer())) return;
		
		BEGMAC.addAction(new Action(event.getPlayer().getName(), "PLAYER_MAX_LEVEL", event.getNewLevel(), Modifier.SET_LARGER));
	}
	
	@EventHandler
	public void chatMessage(PlayerChatEvent event) {
		// Cheat protection
		if (isCheating(event.getPlayer())) return;
		
		String playerName = event.getPlayer().getName();
		List<Player> playerList = event.getPlayer().getWorld().getPlayers();
		if (playerList.size() <= 1) {
			BEGMAC.addAction(new Action(playerName, "CHAT_LINES_ALONE", 1));
		}
		else {
			BEGMAC.addAction(new Action(playerName, "CHAT_LINES_COMPANY", 1));
		}
		
	}
	
	@EventHandler
	public void serverCommand(ServerCommandEvent event) {
		String command = event.getCommand();
		if (command.equals("begmac queries")) {
			BEGMAC.print("TOTAL QUERIES", BEGMAC.pu.NUM_QUERIES + "");
		}
		else if (command.equals("begmac commands")) {
			Iterator iter = ignoredCommands.iterator();
			BEGMAC.print("COMMANDS START", "------------------------------------------------------------------------");
			while (iter.hasNext()) {
				String entry = (String) iter.next();
				BEGMAC.print("IGNORED COMMAND", entry);
			}
			BEGMAC.print("COMMANDS END", "------------------------------------------------------------------------");
		}
		else if (command.equals("begmac actionlist")) {
			List<Action> aList = BEGMAC.copyCurrentActionList(false);
			BEGMAC.print("ACTIONLIST START", "------------------------------------------------------------------------");
			for (Action a : aList) {
				BEGMAC.print("ACTION", a.toString());
			}
			BEGMAC.print("ACTIONLIST END", "------------------------------------------------------------------------");
		}
	}
	
	
	@EventHandler
	public void fillBucket(PlayerBucketFillEvent event) {
		// Cheat protection
		if (isCheating(event.getPlayer())) return;
		
		String playerName = event.getPlayer().getName();
		Integer amount = event.getItemStack().getAmount();
		Material bucket = event.getItemStack().getType();
		Material origin = event.getBlockClicked().getType();
		
		BEGMAC.addAction(new Action(playerName, "FILLED_" + bucket + "_ON_" + origin.toString(), amount));
		
	}
	
	
	@EventHandler
	public void pourBucket(PlayerBucketEmptyEvent event) {
		// Cheat protection
		if (isCheating(event.getPlayer())) return;
		
		String playerName = event.getPlayer().getName();
		Material bucket = event.getBucket();		
		Material destination = event.getBlockClicked().getType();

		BEGMAC.addAction(new Action(playerName, "POURED_" + bucket + "_ON_" + destination.toString(), 1));
	}
	
	
	@EventHandler
	public void craftItem(CraftItemEvent event) {
		// Cheat protection
		if (isCheating((Player) event.getView().getPlayer())) return;
		
		CraftingInventory inv = event.getInventory();
		if ((inv != null) && (inv.getResult() != null)) {
			String craftedStuff = inv.getResult().getType().toString(); // fehler hier?
			String playerName = event.getView().getPlayer().getName();
			// getting the amount only works if player does not use shift+click for quick collection
			// directly to the inventory. maybe this can calculated by using the items in the inventory
			// getting the current recipe and calculating the maximum outcome.
			Integer amount = 0;
			if (event.isShiftClick()) {
				event.setCancelled(true);
				Player player = (Player) event.getView().getPlayer();
				player.sendMessage("Please do not shift+click for item collection.");
			}
			else {
				amount = inv.getResult().getAmount();
			}
			BEGMAC.addAction(new Action(playerName, "ITEM_CRAFT_" + craftedStuff, amount));
		}
	}
	
	
	@EventHandler
	public void enchant(EnchantItemEvent event) {
		Player player = event.getEnchanter();
		
		// Cheat protection
		if (isCheating(player)) return;
		
		ItemStack itemStack = event.getItem();
		Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();
		
		Iterator iter = enchantments.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Enchantment, Integer> entry = (Entry<Enchantment, Integer>) iter.next();
			BEGMAC.addAction(new Action(player.getName(), "ENCHANT_" + itemStack.getType() + "_WITH_" + entry.getKey().getName() + "_" + entry.getValue(), 1));
		}
	}
	
	
	@EventHandler
	public void itemBreak(PlayerItemBreakEvent event) {
		ItemStack itemStack = event.getBrokenItem();
		Player player = event.getPlayer();
		
		// Cheat protection
		if (isCheating(player)) return;
		
		// Amount is hardcoded to 1, since the itemstack amount is 0 after the item is broken.
		BEGMAC.addAction(new Action(player.getName(), "ITEM_BREAK_" + itemStack.getType(), 1));
	}

	@EventHandler
	public void consumeFood(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		ItemStack consumedItem = event.getItem();

		if (isCheating((player))) return;

		BEGMAC.addAction(new Action(player.getName(), "CONSUME_" + consumedItem.getType(), consumedItem.getAmount()));
	}

	
	/* TODO: Furnace Crafting as an action chain:
	 *  - put something in the oven (oven ID)
	 *  - oven (oven ID) is finishing something
	 * 
	 * It seems the furnace is buggy, the CRAFTING slot is returned as the
	 * CONTAINER slot and is therefore ambiguous with all the other f**king
	 * inventory slots, while in the furnace view...
	 */
	
	
	
	/*
	@EventHandler
	public void craftItemFurnace(FurnaceSmeltEvent event) {
		String craftedStuff = event.getResult().getType().toString();
		Integer amount = event.getResult().getAmount();
		
		Furnace furnace = (Furnace) event.getBlock().getState();
		//buggy if fuel is used but heat still remains
		//String fuel = furnace.getInventory().getFuel().getType().toString();
		String fuel = "UNKNOWN";
		Location furnaceLocation = furnace.getInventory().getHolder().getLocation();

		if (amount == 1) {
			String playerName = FurnaceManager.getSmeltPlayer(furnace.getInventory());
			BEGMAC.print("FURNACE SMELTED", playerName + " smelted " + amount + "x " + craftedStuff + " with " + fuel + " by Furnace: " + furnaceLocation.toString());
		}
		else {
			BEGMAC.print("PROBLEM", "Furnace smelted more than one Item at the same time!");
		}
		
	}
	*/
	
	/*
	@EventHandler
	public void inventoryClick(final InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (event.isShiftClick()) {
			player.sendMessage("Please do not use shift+click.");
		}
		else {
			Inventory inv = event.getInventory();
			final String playerName = player.getName();
			if (inv.getType() == InventoryType.FURNACE) {
				FurnaceInventory fInv = (FurnaceInventory) inv;
				Location furnaceLocation = fInv.getHolder().getLocation();
				if (event.getSlotType() == SlotType.CONTAINER) {
					if (event.getSlot() == 0) {
						FurnaceManager.dumpFurnaceActivity(fInv);
						// THIS IS STILL NOT WORKING, TOO MUCH THINKING MISTAKES! AAAAH!!
						ItemStack fStack = fInv.getSmelting();
						ItemStack cStack = player.getItemOnCursor();

						if ((fStack == null) || (fStack.getType().equals(Material.AIR))) {
							if ((cStack == null) || (cStack.getType().equals(Material.AIR))) {
								// Furnace empty + Cursor empty
								// do nothing
								event.setCancelled(true);
							}
							else {
								// Furnace empty + Cursor filled
								Integer allowedAddition = 0;
								if (event.isLeftClick()) {
									// left click
									allowedAddition = FurnaceManager.addStack(fInv, playerName, cStack);
									BEGMAC.print("FURNACE", "LeftClick: Furnace(0), Cursor(" + cStack.toString() + "), AllowedAddition(" + allowedAddition + ")");
								}
								else {
									// right click
									ItemStack singleItem = cStack.clone();
									singleItem.setAmount(1);
									allowedAddition = FurnaceManager.addStack(fInv, playerName, singleItem);
									BEGMAC.print("FURNACE", "RightClick: Furnace(0), Cursor(" + cStack.toString() + "), AllowedAddition(" + allowedAddition + ")");
								}
								
								if (allowedAddition > 0) {
									player.setItemOnCursor(new ItemStack(cStack.getType(), cStack.getAmount() - allowedAddition));
									fInv.setSmelting(new ItemStack(cStack.getType(), allowedAddition));
									event.setCancelled(true);
								}
							}
						}
						else {
							if ((cStack == null) || (cStack.getType().equals(Material.AIR))) {
								// Furnace filled + Cursor empty
								Integer allowedRemoval = 0;
								if (event.isLeftClick()) {
									// left click
									ItemStack referenceStack = new ItemStack(fStack.getType(), fStack.getAmount());
									allowedRemoval = FurnaceManager.removeStack(fInv, playerName, referenceStack);
									BEGMAC.print("FURNACE", "LeftClick: Furnace(" + fStack.toString() + "), Cursor(0), AllowedRemoval(" + allowedRemoval + ")");
								}
								else {
									// right click
									ItemStack referenceStack = new ItemStack(fStack.getType(), Math.round(fStack.getAmount()/2));
									allowedRemoval = FurnaceManager.removeStack(fInv, playerName, referenceStack);
									BEGMAC.print("FURNACE", "RightClick: Furnace(" + fStack.toString() + "), Cursor(0), AllowedRemoval(" + allowedRemoval + ")");
								}
								
								if (allowedRemoval > 0) {
									player.setItemOnCursor(new ItemStack(fStack.getType(), allowedRemoval));
									fInv.setSmelting(new ItemStack(fStack.getType(), fStack.getAmount() - allowedRemoval));
									if (fInv.getSmelting().getAmount() == 0) fInv.setSmelting(null);
									event.setCancelled(true);
								}
							}
							else {
								if ( (!cStack.getType().equals(Material.AIR)) && (!fStack.getType().equals(Material.AIR)) ) {
									// Furnace filled + Cursor filled
									if (cStack.getType() == fStack.getType()) {
										Integer allowedAddition = 0;
										if (event.isLeftClick()) {
											// same item + left click
											allowedAddition = FurnaceManager.addStack(fInv, playerName, cStack);
											BEGMAC.print("FURNACE", "LeftClick: Furnace(" + fStack.toString() + "), Cursor(" + cStack.toString() + "), AllowedAddition(" + allowedAddition + ")");
										}
										else {
											// same item + right click
											ItemStack singleItem = cStack.clone();
											singleItem.setAmount(1);
											allowedAddition = FurnaceManager.addStack(fInv, playerName, singleItem);
											BEGMAC.print("FURNACE", "RightClick: Furnace(" + fStack.toString() + "), Cursor(" + cStack.toString() + "), AllowedAddition(" + allowedAddition + ")");
										}

										if (allowedAddition > 0) {
											player.setItemOnCursor(new ItemStack(cStack.getType(), cStack.getAmount() - allowedAddition));
											fInv.setSmelting(new ItemStack(cStack.getType(), allowedAddition));
											event.setCancelled(true);
										}	
									}
									else {
										// different item
										Integer allowedRemoval = FurnaceManager.removeStack(fInv, playerName, fStack);
										BEGMAC.print("FURNACE", "Switch: Furnace(" + fStack.toString() + "), Cursor(" + cStack.toString() + "), AllowedAddition(" + allowedRemoval + ")");
										BEGMAC.print("aRemoval = fStack", allowedRemoval + " = " + fStack.getAmount());
										if (allowedRemoval == fStack.getAmount()) {
											FurnaceManager.addStack(fInv, playerName, cStack);
											player.setItemOnCursor(new ItemStack(fStack.getType(), fStack.getAmount()));
											fInv.setSmelting(new ItemStack(cStack.getType(), cStack.getAmount()));

											event.setCancelled(true);
										}
										else {
											FurnaceManager.addStack(fInv, playerName, fStack);
											event.setCancelled(true);
										}
									}
								}
								else {
									BEGMAC.print("FURNACE STUFF", "cStack or fStack is AIR");
								}
							}
						}
					}
				}
			}
		}
	}
	*/					

	
	
	
	// Combinational Events first "RIGHT CLICK INTERACT" (in List)
	//			then  "ENTITYREGAINHEALTH EVENT" if theres something eatable in the list
	//			maybe it was the stuff that has been eaten.... (TRY IT!)
	//		if someone is hacking on a block, put in list
	//			if he is picking up something that he was hacking on before
	//			add as "WORKED FOR IT HIM/HERSELF" or something...
	
	// Stuff eaten by players...
	// Stuff created by players.... (Workbench, Oven, Cauldron, etc.)
	// Redstone Distanz X (You're on wire!)
	// Distanz gelaufen
	// Distanz X fallen ohne zu sterben
	// Reach the bottom of the map - how to determine?
	
	/*
	 * If someone places a block, check for all playes if they are sorrunded
	 * completely by blocks (therefore being in a crouching posision with 26
	 * solid blocks sorrounding their posision. Maybe even differ whether
	 * this situation has been caused by another player or by oneself.
	 * --> Performance problem, need another solution for this...
	 */

}
