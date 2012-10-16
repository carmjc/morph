package net.carmgate.morph.ui.interaction;

import java.util.Arrays;

public class Command {

	private int key;

	private Integer[] modifiers;

	public Command(int key, Integer[] integers) {
		this.key = key;
		modifiers = integers;
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
		if (!Arrays.equals(modifiers, other.modifiers)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key;
		result = prime * result + Arrays.hashCode(modifiers);
		return result;
	}

}
