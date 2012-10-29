package net.carmgate.morph.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModifiableIterable<T> implements Iterable<T> {

	private List<T> list;
	private List<T> listOfAddedEltsDuringLock = new ArrayList<T>();
	private List<T> listOfRemovedEltsDuringLock = new ArrayList<T>();
	private static int lock = 0;

	public ModifiableIterable(List<T> list) {
		this.list = list;
	}

	public void add(T element) {
		if (lock == 0) {
			list.add(element);
		} else {
			listOfAddedEltsDuringLock.add(element);
		}
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	public void lock() {
		lock++;
	}

	public void remove(T element) {
		if (lock == 0) {
			list.remove(element);
		} else {
			listOfRemovedEltsDuringLock.add(element);
		}
	}

	public int size() {
		return list.size();
	}

	public void unlock() {
		lock--;
		if (lock == 0) {
			list.addAll(listOfAddedEltsDuringLock);
			listOfAddedEltsDuringLock.clear();
			list.removeAll(listOfRemovedEltsDuringLock);
			listOfRemovedEltsDuringLock.clear();
		}
	}
}
