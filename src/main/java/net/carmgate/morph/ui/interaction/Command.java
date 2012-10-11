package net.carmgate.morph.ui.interaction;

import java.util.Set;

public class Command {

	private int key;
	private Set<Integer> modifiers;

	public Command(int key, Set<Integer> modifiers) {
		this.key = key;
		this.modifiers = modifiers;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Command other = (Command) obj;
		if (key != other.key) {
			return false;
		}
		if (modifiers == null) {
			if (other.modifiers != null) {
				return false;
			}
		} else if (!modifiers.equals(other.modifiers)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key;
		result = prime * result + (modifiers == null ? 0 : modifiers.hashCode());
		return result;
	}

}
