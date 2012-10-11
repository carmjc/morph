package net.carmgate.morph.model.user;

/**
 * This class allows to identify the owner of something in the model (for instance, a ship).
 * If the user is not mentionned at a given level, it should be inherited from the container.
 */
public class User {

	/** The type of user. */
	public enum UserType {
		HUMAN,
		AI,
		GOD;
	}

	/** User type. */
	private final UserType type;

	/** User name. */
	private final String name;

	/**
	 * @param type a {@link UserType}
	 */
	public User(UserType type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * @return the user name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return a {@link UserType}.
	 */
	public UserType getType() {
		return type;
	}

}
