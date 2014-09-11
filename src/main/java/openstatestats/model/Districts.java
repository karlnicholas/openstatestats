package openstatestats.model;

import java.util.*;

public class Districts extends UserData<District> implements List<District> {
	private List<District> districts = new ArrayList<District>();
	
	public District findDistrict(String chamber, String district) {
		for ( District d: districts ) {
			if ( d.getChamber().equals(chamber) && d.getDistrict().equals(district)) return d; 
		}
		return null;
	}

	@Override
	public int size() {
		return districts.size();
	}

	@Override
	public boolean isEmpty() {
		return districts.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return districts.contains(o);
	}

	@Override
	public Iterator<District> iterator() {
		return districts.iterator();
	}

	@Override
	public Object[] toArray() {
		return districts.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return districts.toArray(a);
	}

	@Override
	public boolean add(District e) {
		return districts.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return districts.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return districts.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends District> c) {
		return districts.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends District> c) {
		return districts.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return districts.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return districts.retainAll(c);
	}

	@Override
	public void clear() {
		districts.clear();
	}

	@Override
	public District get(int index) {
		return districts.get(index);
	}

	@Override
	public District set(int index, District element) {
		return districts.set(index, element);
	}

	@Override
	public void add(int index, District element) {
		districts.add(index, element);
	}

	@Override
	public District remove(int index) {
		return districts.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return districts.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return districts.lastIndexOf(o);
	}

	@Override
	public ListIterator<District> listIterator() {
		return districts.listIterator();
	}

	@Override
	public ListIterator<District> listIterator(int index) {
		return districts.listIterator(index);
	}

	@Override
	public List<District> subList(int fromIndex, int toIndex) {
		return districts.subList(fromIndex, toIndex);
	}

}
