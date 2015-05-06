package beg.m.ac;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author makko
 */
public class Possession {
	
	private String		playerName;
	private Material	itemType;
	private Integer		amount;
	
	Possession(String playerName, ItemStack itemStack) {
		this.playerName = playerName;
		this.itemType = itemStack.getType();
		this.amount = itemStack.getAmount();
	}
	
	public Integer decrement() {
		amount--;
		return amount;
	}
	
	/**
	 * @return the playerName
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * @return the itemType
	 */
	public Material getItemType() {
		return itemType;
	}

	/**
	 * @return the amount
	 */
	public Integer getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	
}
