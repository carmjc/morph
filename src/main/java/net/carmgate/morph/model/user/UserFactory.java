package net.carmgate.morph.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.user.User.FriendOrFoe;

public class UserFactory {

	private static final Map<String, User> users = new HashMap<String, User>();
	private static final Map<FriendOrFoe, List<User>> usersByFOF = new HashMap<FriendOrFoe, List<User>>();

	public static void addUser(User user) {
		users.put(user.getName(), user);

		List<User> l = usersByFOF.get(user.getFriendOrFoe());
		if (l == null) {
			l = new ArrayList<User>();
			usersByFOF.put(user.getFriendOrFoe(), l);
		}
		l.add(user);
	}

	public static User findSelf() {
		return usersByFOF.get(FriendOrFoe.SELF).iterator().next();
	}

	public static User findUser(String name) {
		return users.get(name);
	}

	private UserFactory() {
	}
}
