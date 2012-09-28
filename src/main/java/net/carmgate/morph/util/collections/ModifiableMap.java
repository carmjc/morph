package net.carmgate.morph.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifiableMap<K, V> {

	private Map<K, V> map;
	private Map<K, V> mapOfAddedEltsDuringLock = new HashMap<K, V>();
	private List<K> listOfRemovedEltsDuringLock = new ArrayList<K>();
	private static int lock = 0;

	public ModifiableMap(Map<K, V> map) {
		this.map = map;
	}

	public Map<K, V> getMap() {
		return map;
	}

	public void lock() {
		lock++;
	}

	public void put(K key, V value) {
		if (lock == 0) {
			map.put(key, value);
		} else {
			mapOfAddedEltsDuringLock.put(key, value);
		}
	}

	public void remove(K key) {
		if (lock == 0) {
			map.remove(key);
		} else {
			listOfRemovedEltsDuringLock.add(key);
		}
	}

	public void unlock() {
		lock--;
		if (lock == 0) {
			map.putAll(mapOfAddedEltsDuringLock);
			mapOfAddedEltsDuringLock.clear();
			for (K key : listOfRemovedEltsDuringLock) {
				map.remove(key);
			}
			listOfRemovedEltsDuringLock.clear();
		}
	}

	public Collection<V> values() {
		return Collections.unmodifiableCollection(map.values());
	}
}
