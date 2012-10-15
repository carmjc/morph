package net.carmgate.morph.model.user;

/**
 * This class allows to identify the owner of something in the model (for instance, a ship).
 * If the user is not mentionned at a given level, it should be inherited from the container.
 */
public class User {

	public enum FriendOrFoe {
		SELF,
		FRIEND,
		FOE;
	}

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

	/** friend of foe ? */
	private final FriendOrFoe friendOrFoe;

	/**
	 * @param type a {@link UserType}
	 */
	public User(UserType type, String name, FriendOrFoe friendOrFoe) {
		this.type = type;
		this.name = name;
		this.friendOrFoe = friendOrFoe;
	}

	/**
	 * God is a {@link FriendOrFoe#FRIEND}
	 * @return a {@link FriendOrFoe} reference.
	 */
	public FriendOrFoe getFriendOrFoe() {
		return friendOrFoe;
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
