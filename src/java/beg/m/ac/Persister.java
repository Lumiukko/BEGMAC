package beg.m.ac;

import java.util.List;

/**
 *
 * @author makko
 */
public class Persister extends Thread {
	private final int	SLEEP_PERIOD = 30*1000;	// 60*1000 = 1min
		
	Persister() {
	}
	
	@Override
	public void run() {
		BEGMAC.print("Persister", "started.");
		while (true) {
			persist();

			try {
				//BEGMAC.print("DEBUG, PERSISTER", "Sleeping [" + (SLEEP_PERIOD/1000) + "s]");
				Thread.sleep(SLEEP_PERIOD);
			}
			catch (InterruptedException e) {
				BEGMAC.print("PERSISTER", "Interrupted!");
			}
		}
	}
	
	public static void persist() {
		// copy action list
		List<Action> aList = BEGMAC.getCurrentActionList();
			
		for (Action a : aList) {
			// put into database / persist
			boolean result = BEGMAC.pu.addActionToDB(a);
			//BEGMAC.print("PERSISTER", "{" + result + "} " + a.toString());
		}
	}
}
