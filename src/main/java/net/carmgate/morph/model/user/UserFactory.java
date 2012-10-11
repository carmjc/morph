package net.carmgate.morph.model.user;

import java.util.HashMap;
import java.util.Map;

public class UserFactory {

	private static final Map<String, User> users = new HashMap<String, User>();

	public static void addUser(User user) {
		users.put(user.getName(), user);
	}

	public static User findUser(String name) {
		return users.get(name);
	}

	private UserFactory() {
	}
}
