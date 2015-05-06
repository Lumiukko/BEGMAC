package beg.m.ac;

/**
 *
 * @author makko
 */

public enum Modifier {
	/**
	 * Uses multiplication: new value = old value * current value
	 */
	MULTIPLY,
	/**
	 * Uses addition: new value = old value + current value
	 */
	ADD,
	/**
	 * Simply sets the larger value: new value = MAX(old value, current value)
	 */
	SET_LARGER
}
