package beg.m.ac;

/**
 *
 * @author makko
 */
public class Action {
	// Constant is used to seperate developer attributes from live attributes
	//	to avoid interference with live system stats.
	// DEV_SUFFIX = "DEV_"	-> dev mode
	// DEV_SUFFIX = ""	-> live mode
	//public static final String	DEV_SUFFIX = "_DEV_";
	public static final String	DEV_SUFFIX = "";
	
	private String		playerName;
	private String		attributeName;
	private Integer		value;
	private Modifier	modifier;

	Action(String player, String attribute, Integer value) {
		this(player, attribute, value, Modifier.ADD);
	}
	
	Action (String player, String attribute, Integer value, Modifier modifier)
	{
		this.playerName = player;
		this.attributeName = attribute;
		this.value = value;
		this.modifier = modifier;
	}
	
	public Action merge(Action a) throws RuntimeException {
		Action merged = null;
		if (!modifier.equals(a.getModifier())) {
			throw new RuntimeException("Can't merge actions: Action Modifier mismatch.");
		}
		else if (!playerName.equals(a.getPlayerName())) {
			throw new RuntimeException("Can't merge actions: Action Player mismatch.");
		}
		else if (!getAttributeName().equals(a.getAttributeName())) {
			throw new RuntimeException("Can't merge actions: Action Attribute mismatch.");
		}
		else {
			switch (modifier) {
				case ADD: {
					merged = new Action(playerName, getAttributeName(), value + a.getValue(), modifier);
					break;
				}
				case MULTIPLY: {
					merged = new Action(playerName, getAttributeName(), value * a.getValue(), modifier);
					break;
				}
				case SET_LARGER: {
					merged = new Action(playerName, getAttributeName(), (value > a.getValue() ? value : a.getValue()), modifier);
					break;
				}
			}
		}
		return merged;
	}

	@Override
	public String toString() {
		return "[" + playerName + "] " + getAttributeName() + " " + modifier + " " + value;
	}
	
	/**
	 * @return the playerName
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		if (attributeName.startsWith(DEV_SUFFIX)) {
			return attributeName;
		}
		return DEV_SUFFIX + attributeName;
	}

	/**
	 * @return the modifier
	 */
	public Integer getValue() {
		return value;
	}

	/**
	 * @return the modifier
	 */
	public Modifier getModifier() {
		return modifier;
	}
}
