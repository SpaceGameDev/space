package space.engine.key;

@SuppressWarnings("unused")
public class Key<V> {
	
	public final int id;
	
	/**
	 * Constructor for the Key. <b>DO NOT MAKE THIS CONSTRUCTOR PUBLIC.</b> Only call it internally inside your custom createKey()-Method inside your {@link KeyGenerator}.
	 *
	 * @param gen your generator
	 */
	protected Key(KeyGenerator<?> gen) {
		id = gen.generateNextId(this);
	}
}
