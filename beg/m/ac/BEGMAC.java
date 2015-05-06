package beg.m.ac;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author makko
 */
public class BEGMAC extends JavaPlugin {
	public static String		pluginName;
	public static PersistencyUtil	pu;
	
	private static Lock		lock;
	//private static List<Action>	actionList;
	
	private PluginDescriptionFile	info;
	private Thread			persistThread;
	private BEGMACListener		listener;
	
	private static Map<String, Map<String, Action>>	actionMap;
		
	public static void addAction(Action action) {
		String playerName = action.getPlayerName();
		String attribute = action.getAttributeName();
		
		if (lock.tryLock()) {
			try {
				if (!actionMap.containsKey(playerName)) {
					Map<String, Action> playerMap = new HashMap<String, Action>();
					actionMap.put(playerName, playerMap);
					//print("ACTIONMAP NEW PLAYER", playerName);
				}

				Map<String, Action> playerMap = actionMap.get(playerName);
				if (!playerMap.containsKey(attribute)) {
					playerMap.put(attribute, action);
					//print("ACTIONMAP NEW ATTR", attribute);
				}
				else {
					Action lastAction = playerMap.get(attribute);
					playerMap.remove(attribute);
					playerMap.put(attribute, lastAction.merge(action));
					//print("ACTIONMAP ADD ACTION", action.toString());
				}
			}
			finally {
				lock.unlock();
			}
		}
		else {
			BEGMAC.print("ACTIONMAP LOCKED", "Lost action? " + action.toString());
		}
		
		//TODO: implement an action collection, that will reduce the amount of queries by
		//	accumulating actions more efficiently. (maybe by maps based on actions without the amounts)
		/*
		if (actionList.isEmpty()) {
			actionList.add(action);
		}
		else {
			Action lastAction = actionList.get(actionList.size()-1);
			if (lastAction.getAttributeName().equals(action.getAttributeName())
				&& (lastAction.getPlayerName().equals(action.getPlayerName())
				&& (lastAction.getModifier().equals(action.getModifier())))) {
				// accumulate same attribute actions if possible
				try {
					Action mergedAction = lastAction.merge(action);
					actionList.remove(actionList.size()-1);
					actionList.add(mergedAction);				
				}
				catch (RuntimeException e) {
					// action mismatch that didn't get caught by the if condition above
					//	should not happen! check if it does happen anyway!!!
					BEGMAC.print("MERGE FAIL", "Action merge mismatch: adding single action to list.");
					actionList.add(action);
				}
			}
			else {
				actionList.add(action);
			}
		}
		* 
		*/ 
	}
	
	/*
	public static List<Action> getCurrentActionList() {
		List<Action> aList = new ArrayList<Action>();
		for (Action a : actionList) {
			aList.add(a);
		}
		actionList.clear();
		return aList;
	}
	* */ 
	
	public static List<Action> getCurrentActionList() {
		return copyCurrentActionList(true);
	}
	
	public static List<Action> copyCurrentActionList(boolean clear) {
		List<Action> returnList = new ArrayList<Action>();
		//BEGMAC.print("LOCKING ACTIONMAP", "");
		lock.lock();
		try {
			Iterator iter = actionMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Map<String, Action>> entry1 = (Entry<String, Map<String, Action>>) iter.next();
				Map<String, Action> playerMap = entry1.getValue(); 
				Iterator iterInner = playerMap.entrySet().iterator();
				while (iterInner.hasNext()) {
					Entry<String, Action> entry = (Entry<String, Action>) iterInner.next();
					returnList.add(entry.getValue());
				}
				if (clear) playerMap.clear();
			}
			
		}
		finally {
			lock.unlock();
			//BEGMAC.print("UNLOCKED ACTIONMAP", "");
		}
		return returnList;
	}
	
	@Override
	public void onEnable() {
		info = getDescription();
		pluginName = info.getName();
		pu = new PersistencyUtil();
		lock = new ReentrantLock();
		//actionList = new ArrayList<Action>();
		actionMap = new HashMap<String, Map<String, Action>>();
		print("Services", "starting...");
		if (pu.connect()) {
			print("mySQL connect", "success.");
			print("mySQL timestamp", pu.getDBTimestamp().toString());
		}
		else {
			print("mySQL connect", "failed!");
		}
		
		listener = new BEGMACListener();
		getServer().getPluginManager().registerEvents(listener, this);
		persistThread = new Persister();
		persistThread.start();
	}
	
	@Override
	public void onDisable() {
		//TODO: call persisters run method one more time before disabling
		print("DISTABLE SIGNAL ", "Attempting to shutdown....");
		Player[] playerList = getServer().getOnlinePlayers();
		for (Player p : playerList) {
			listener.savePlayerTime(p);
		}
		Persister.persist();
		try {
			Thread.sleep(5000);
		}
		catch (InterruptedException e) {}
		pu.disconnect();
		print("TOTAL QUERIES ", "" + pu.NUM_QUERIES);
		print("Services ", "stopped.");
	}
		
	public static void print(String head, String message) {
		Formatter f = new Formatter();
		System.out.println(f.format("[%s] %-20s: %s", pluginName, head, message).toString());
	}
	
	
}
