package beg.m.ac;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 *
 * @author makko
 * This class offers methods to access the database. Database should only be 
 * accessed through this utility class. The get* and create* methods may be a
 * bit confusing, since they are having the opposite use. Please check them
 * yourself before using them.
 */
public class PersistencyUtil {
	
	
	private final String	SERVER = "jdbc:mysql://<someaddress>:3306/<dbname>";
	private final String	USERNAME = "<username>";
	private final String	PASSWORD = "<password>";
	
	public static Integer	NUM_QUERIES = 0;
	
	private Connection	con;
	private Statement	updateStmt;
	private Statement	resultStmt;
	
	/**
	 * Connects to the defined database server.
	 * @return a <code>boolean</code> whether the connection was successful or not.
	 */
	public boolean connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String serverURL = "jdbc:mysql://brainscorch.net:3306/mysql";
			con = (Connection) DriverManager.getConnection(SERVER, USERNAME, PASSWORD);
			updateStmt = (Statement) con.createStatement();
			resultStmt = (Statement) con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (Exception e) {
			System.err.println("PersistencyUtil/connect: " + e.toString());
		}
		if (con != null) return true;
		return false;
	}
	
	/**
	 * Closes the connection to the database server.
	 */
	public void disconnect() {
		try {
			updateStmt.close();
			resultStmt.close();
			con.close();
		}
		catch (Exception e) {
			System.err.println("PersistencyUtil/disconnect: " + e.toString());
		}
	}
	
	/**
	 * Gets the current timestamp of the database server.
	 * @return the <code>Integer</code> containing the timestamp of the database server.
	 */
	public Integer getDBTimestamp() {
		checkConnection();
		try {
			ResultSet result = resultStmt.executeQuery("SELECT UNIX_TIMESTAMP();");
			NUM_QUERIES++;
			while (result.next()) {
				return result.getInt(1);
			}
		}
		catch (Exception e) {
			System.err.println("PersistencyUtil/getDBTimestamp: " + e.toString());
		}
		return null;
	}
	
	/**
	 * Is looking for the ID of the player in the database and returns the
	 * ID or null if no player with the given name was found.
	 * @param playerName the name of the player to search for
	 * @return the <code>Integer</code> containing the ID of the player,
	 * or <code>null</code> if no playerID was found.
	 */
	public Integer createPlayer(String playerName) {
		checkConnection();
		Integer playerID = null;
		try {
			ResultSet r = resultStmt.executeQuery("SELECT ID FROM player WHERE Name = '" + playerName + "';");
			NUM_QUERIES++;
			while (r.next()) {
				if (!r.getString("ID").isEmpty()) {
					playerID = r.getInt("ID");
				}
			}
		}
		catch (Exception e) {
			System.err.println("PersistencyUtil/createPlayer: " + e.toString());
		}
		return playerID;
	}
	
	/**
	 * Attempts to create a player in the database including a previous
	 * check if the player already exists. Returns the newly created playerID.
	 * @param playerName
	 * @return the <code>Integer</code> containing the newly created playerID.
	 */
	public Integer getPlayerID(String playerName) {
		checkConnection();
		Integer playerID = createPlayer(playerName);
		
		if (playerID == null) {
			try {
				// Player doesn't exist in database
				updateStmt.executeUpdate("INSERT INTO player (Name) VALUES ('" + playerName + "');");
				NUM_QUERIES++;
				// get Player ID of newly created player
				playerID = createPlayer(playerName);
				BEGMAC.print("Player created", "[" + playerID + "] " + playerName);
			}
			catch (Exception e) {
				System.err.println("PersistencyUtil/getPlayerID: " + e.toString());
			}
		}
		return playerID;
	}
	
	/**
	 * Is looking for the ID of the attribute in the database and returns the
	 * ID or null if no attribute with the given name was found.
	 * @param attributeName the name of the attribute to search for
	 * @return the <code>Integer</code> containing the ID of the attribute,
	 * or <code>null</code> if no attributeID was found.
	 */
	public Integer createAttribute(String attributeName) {
		checkConnection();
		Integer attributeID = null;
		try {
			ResultSet r = resultStmt.executeQuery("SELECT ID FROM attribute WHERE Name = '" + attributeName + "';");
			NUM_QUERIES++;
			while (r.next()) {
				if (!r.getString("ID").isEmpty()) {
					attributeID = r.getInt("ID");
				}
			}
		}
		catch (Exception e) {
			System.err.println("PersistencyUtil/createAttribute: " + e.toString());
		}
		return attributeID;
	}
	
	
	/**
	 * Attempts to create an attribute in the database including a previous
	 * check if the attribute already exists. Returns the newly created attributeID.
	 * @param attributeName
	 * @return the <code>Integer</code> containing the newly created attributeID.
	 */
	public Integer getAttributeID(String attributeName) {
		checkConnection();
		Integer attributeID = createAttribute(attributeName);
		
		if (attributeID == null) {
			try {
				// Player doesn't exist in database
				updateStmt.executeUpdate("INSERT INTO attribute (Name, Description, Started) VALUES ('" + attributeName + "', '_AUTOGENERATED_" + attributeName + "', UNIX_TIMESTAMP());");
				NUM_QUERIES++;
				// get Player ID of newly created player
				attributeID = createAttribute(attributeName);
				BEGMAC.print("Attribute created", "[" + attributeID + "] " + attributeName);
			}
			catch (Exception e) {
				System.err.println("PersistencyUtil/getAttributeID: " + e.toString());
			}
		}
		return attributeID;
	}
	
	/**
	 * Gets the current value of an attribute from the db.
	 * @param action the respective <code>Action</code> to get the value from
	 * @return an <code>Integer</code> representing the current value or
	 * null if no respective attribute value could be found.
	 */
	public Integer getStatValue(Action action) {
		checkConnection();
		try {
			ResultSet r = updateStmt.executeQuery("	SELECT state"
				+ "				FROM stats, attribute"
				+ "				WHERE attribute.Name = '" + action.getAttributeName() +"'"
				+ "				AND stats.playerID = '" + getPlayerID(action.getPlayerName()) + "'"
				+ "				AND attribute.ID = stats.attributeID;");
			NUM_QUERIES++;
			
			if(r.next()) {
				if (!r.getString("state").isEmpty()) {
					return r.getInt("state");
				}
			}
		}
		catch (SQLException e) {
			System.err.println("PersistencyUtil/getStatValue: " + e.toString());
		}
		return null;
	}
	
	/**
	 * Persists an action in the database by addition.
	 * The modifier of the given action will be added to the current one.
	 * @param action the <code>Action</code> to persist in the database
	 * @return a <code>boolean</code> whether the persisting was successful or not.
	 */
	public boolean addActionToDB(Action action) {
		checkConnection();
		try {
			// check if attribute exists
			if (getStatValue(action) != null) {
				// stats for that player exists.
				Integer newState = -1;
				Integer oldState = getStatValue(action);
				oldState = (oldState == null ? 0 : oldState);
				switch(action.getModifier()) {
					case ADD: {
						newState = oldState + action.getValue();
						break;
					}
					case MULTIPLY: {
						newState = oldState * action.getValue();
						break;
					}
					case SET_LARGER: {
						newState = (action.getValue() > oldState ? action.getValue() : oldState);
						break;
					}
				}

				if (newState != oldState) {
					String query = "UPDATE stats"
						+ "	SET state='" + newState+ "'"
						+ "	WHERE playerID='" + getPlayerID(action.getPlayerName()) + "'"
						+ "	AND attributeID='" + getAttributeID(action.getAttributeName()) + "';";
					updateStmt.executeUpdate(query);
					NUM_QUERIES++;
					query = "INSERT INTO actionlog (time, message) VALUES (UNIX_TIMESTAMP(), '" + action.toString() + "');";
					updateStmt.executeUpdate(query);
					NUM_QUERIES++;
					setLastChanged();
				}
				return true;
				
			}
			else {
				// stats for that player does not exist yet.
				updateStmt.executeUpdate("INSERT INTO stats (playerID, attributeID, state) VALUES ('" + getPlayerID(action.getPlayerName()) + "', '" + getAttributeID(action.getAttributeName()) + "', '" + action.getValue() + "');");
				NUM_QUERIES++;
				String query = "INSERT INTO actionlog (time, message) VALUES (UNIX_TIMESTAMP(), '" + action.toString() + "');";
				updateStmt.executeUpdate(query);
				NUM_QUERIES++;
				return true;
			}
		}
		catch (Exception e) {
			System.err.println("PersistencyUtil/addActionToDB: " + e.toString());
		}
		return false;
	}
	
	/**
	 * Refreshes the last changed attribute in the config table to the
	 * current timestamp.
	 */
	public void setLastChanged() {
		checkConnection();
		try {
			String query = "UPDATE config"
				+ "	SET Value=UNIX_TIMESTAMP()"
				+ "	WHERE Name='LAST_UPDATE';";
			updateStmt.executeUpdate(query);
			NUM_QUERIES++;
		}
		catch (SQLException e) {
			System.err.println("PersistencyUtil/setLastChanged: " + e.toString());
		}
	}
	
	/**
	 * Checks if the database connection is still valid. If the connection
	 * timed out, it reconnects to the db server. This method should be
	 * called every time before a database connection is required.
	 */
	private void checkConnection() {
		try {
			if (!con.isValid(2)) connect();
		}
		catch (SQLException e) {
			System.err.println("PersistencyUtil/checkConnection: " + e.toString());
		}
	}
	
}
