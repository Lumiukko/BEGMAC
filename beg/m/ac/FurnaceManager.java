package beg.m.ac;

import java.util.*;
import net.minecraft.server.Material;
import org.bukkit.Location;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import sun.swing.AccumulativeRunnable;

/**
 *
 * @author makko
 * THIS IS NOT WORKING CORRECTLY!!!
 */
public class FurnaceManager {

	private static Map<FurnaceInventory, List<Possession>> furnaceMap = new HashMap<FurnaceInventory, List<Possession>>();

	
	public static Integer removeStack(FurnaceInventory furnaceInventory, String playerName, ItemStack removeStack) {
		Integer allowedRemoval = 0;
		List<Possession> pList;
		if (furnaceMap.containsKey(furnaceInventory)) {
			// Furnace exists already...
			pList = furnaceMap.get(furnaceInventory);
			if ((pList.isEmpty()) && (furnaceInventory.getSmelting().getAmount() > 0)) {
				pList.add(new Possession("", furnaceInventory.getSmelting()));
			}
			if (!pList.isEmpty()) {
				Possession oldest = pList.get(0);
				
				BEGMAC.print("OLDEST", oldest.getAmount() + "x " + oldest.getItemType());
				
				if (oldest.getItemType().equals(removeStack.getType())) {
					Integer ownedByPlayer = 0;
					Integer ownedByNobody = 0;
					List<Possession> newPList = new ArrayList<Possession>();
					for (Possession currPoss : pList) {
						if (currPoss.getPlayerName() == playerName) {
							ownedByPlayer += currPoss.getAmount();
						}
						else if (currPoss.getPlayerName() == "") {
							ownedByNobody += currPoss.getAmount();
						}
						else {
							newPList.add(currPoss);
						}
					}
					allowedRemoval = Math.min(ownedByPlayer + ownedByNobody, removeStack.getAmount());
					ItemStack leftoverItemStack = new ItemStack(oldest.getItemType(), ownedByPlayer + ownedByNobody - allowedRemoval);
					
					BEGMAC.print("leftoverItemStack", leftoverItemStack.getAmount() + "x " + leftoverItemStack.getType());
					
					Possession leftoverPossession = new Possession(playerName, leftoverItemStack);
					if (leftoverPossession.getAmount() > 0) newPList.add(leftoverPossession);

					furnaceMap.remove(furnaceInventory);
					furnaceMap.put(furnaceInventory, newPList);
				}
			}
		}
		else {
			// Furnace doesn't exist yet...
			pList = new ArrayList<Possession>();
			furnaceMap.put(furnaceInventory, pList);
		}
		dumpFurnaceActivity(furnaceInventory);
		return allowedRemoval;
	}
	
	public static Integer addStack(FurnaceInventory furnaceInventory, String playerName, ItemStack addStack) {
		Integer allowedAddition = 0;
		Possession p = new Possession(playerName, addStack);
		List<Possession> pList;
		if (furnaceMap.containsKey(furnaceInventory)) {
			// Furnace exists already...
			pList = furnaceMap.get(furnaceInventory);
			
			if (pList.isEmpty()) {
				pList.add(new Possession(playerName, new ItemStack(addStack.getType(), 0)));
			}
			Possession newest = pList.get(pList.size()-1);
			if (newest.getItemType().equals(p.getItemType())) {
				Integer occupied = 0;
				for (Possession currPoss : pList) {
					occupied += currPoss.getAmount();
				}
				allowedAddition = Math.min(64-occupied, p.getAmount());
			}
			
		}
		else {
			// Furnace doesn't exist yet...
			pList = new ArrayList<Possession>();
			furnaceMap.put(furnaceInventory, pList);
			allowedAddition = Math.min(addStack.getAmount(), 64);
		}
		p.setAmount(allowedAddition);
		pList.add(p);
		dumpFurnaceActivity(furnaceInventory);
		return allowedAddition;
	}

	// MAYBE GET LOCATION ALS ID DER MAP NICHT INVENTORY SELBST!
	public static String getSmeltPlayer(FurnaceInventory furnaceInventory) {
		if (furnaceMap.containsKey(furnaceInventory)) {
			List<Possession> pList = furnaceMap.get(furnaceInventory);
			if (!pList.isEmpty()) {
				Possession p = pList.get(0);
				BEGMAC.print("GESCHMOLZEN", "letztes item: " + p.getItemType() + " (" + p.getPlayerName() + ")");
				if (p.decrement() == 0) {
					pList.remove(0);
				}
				dumpFurnaceActivity(furnaceInventory);
				return p.getPlayerName();
			}
		}
		dumpFurnaceActivity(furnaceInventory);
		return "";
	}
	
	public static void dumpFurnaceActivity(FurnaceInventory furnaceInventory) {
		if (furnaceMap.containsKey(furnaceInventory)) {
			BEGMAC.print("FURNACE DUMP", furnaceInventory.toString());
			List<Possession> pList = furnaceMap.get(furnaceInventory);
			for(Possession p : pList) {
				BEGMAC.print("     - ", p.getPlayerName() + ": " + p.getAmount() + " x " + p.getItemType());
			}
		}
		else {
			BEGMAC.print("FURNACE DUMP", "Furnace is empty: " + furnaceInventory.toString());
		}
	}
}
