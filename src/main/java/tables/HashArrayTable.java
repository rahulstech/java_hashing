package tables;

import java.util.Iterator;
import java.util.List;

/**
 * Implements a hash-based table
 * using an array data structure.
 */
public class HashArrayTable extends Table {

	private static class Node {

		final int keyHash;
		final Object key;
		List<Object> value;

		public Node(Object key, List<Object> value) {
			this.keyHash = HashArrayTable.calculateKeyHash(key);
			this.key = key;
			this.value = value;
		}
	}

	/*
	 * TODO: For Module 1, implement the stubs.
	 *
	 * Until then, this class is unused.
	 */

	private final int DEFAULT_CAPACITY = 19;

	private final int MAX_SIZE = Integer.MAX_VALUE - 8;

	private Node[] hTable;

	private float loadFactor;

	private int capacity;

	private int size;


	/**
	 * Creates a table and initializes
	 * the data structure.
	 *
	 * @param tableName the table name
	 * @param columnNames the column names
	 * @param columnTypes the column types
	 * @param primaryIndex the primary index
	 */
	public HashArrayTable(String tableName, List<String> columnNames, List<String> columnTypes, Integer primaryIndex) {
		setTableName(tableName);
		setColumnNames(columnNames);
		setColumnTypes(columnTypes);
		setPrimaryIndex(primaryIndex);

		this.capacity = DEFAULT_CAPACITY;
		this.size = 0;
		this.loadFactor = 0.5f;
		this.hTable = new Node[this.capacity];
	}

	@Override
	public void clear() {
		final Node[] old = this.hTable;
		final int oldCapacity = capacity;
		for (int i = oldCapacity; --i >= 0;)
			old[i] = null;
		this.capacity = DEFAULT_CAPACITY;
		this.hTable = new Node[this.capacity];
		this.size = 0;
	}

	@Override
	public boolean put(List<Object> row) {
		if (null == row) throw new NullPointerException("row == null");

		final Object key = row.get(getPrimaryIndex());
		if (null == key) throw new NullPointerException("key == null");

		Node node = new Node(key,row);

		int index = hash1(node.keyHash);
		int index2 = hash2(node.keyHash);

		if (hTable[index] != null && hTable[index].key.equals(key)) {
			hTable[index] = node;
			return true;
		}
		else if (hTable[index2] != null && hTable[index2].key.equals(key)) {
			hTable[index2] = node;
			return true;
		}
		else {
			int pos = index;
			for (int i=0; i<3; i++) {
				if (hTable[pos] == null) {
					hTable[pos] = node;
					size++;
					return false;
				}
				else {
					Node copy = hTable[pos];
					hTable[pos] = node;
					node = copy;
				}
				if (pos == index)
					pos = index2;
				else
					pos = index;
			}
			rehash();
			return put(row);
		}
	}

	@Override
	public boolean remove(Object key) {
		if (null == key) throw new NullPointerException("key == null");

		final int keyHash = calculateKeyHash(key);
		int index = hash1(keyHash);
		int index2 = hash2(keyHash);
		if (hTable[index] != null && hTable[index].key.equals(key)) {
			hTable[index] = null;
			size--;
			return true;
		}
		else if (hTable[index2] != null && hTable[index2].key.equals(key)) {
			hTable[index2] = null;
			size--;
			return true;
		}
		return false;
	}

	@Override
	public List<Object> get(Object key) {
		if (null == key) throw new NullPointerException("key == null");
		final int keyHash = calculateKeyHash(key);
		int index = hash1(keyHash);
		int index2 = hash2(keyHash);
		if (hTable[index] != null && hTable[index].key.equals(key))
			return hTable[index].value;
		else if (hTable[index2] != null && hTable[index2].key.equals(key))
			return hTable[index2].value;
		return null;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return new TableIterator(this.hTable,this.size);
	}

	private int hash1(int keyHash) {
		return keyHash%capacity;
	}

	private int hash2(int keyHash) {
		return (keyHash/capacity)%capacity;
	}

	private void rehash() {
		final int oldCapacity = capacity;
		final int threshold = (int) Math.min(oldCapacity * loadFactor, MAX_SIZE + 1);
		if (size < threshold) return;
		final Node[] copy = this.hTable.clone();
		final int oldSize = this.size;
		capacity = Math.min(capacity * 2 + 1,MAX_SIZE);
		if (oldCapacity == capacity) return;
		hTable = new Node[capacity];
		for (int i=0; i<oldCapacity; ++i) {
			if (copy[i] != null) {
				put(copy[i].value);
			}
		}
		this.size = oldSize;
	}

	public static int calculateKeyHash(Object key) {
		if (key instanceof String) {
			final char[] buff = ((String) key).toCharArray();
			int hash = 0;
			for (int i = buff.length; --i >= 0;) {
				hash = buff[i] + hash * 37;
			}
			return hash;
		}
		else {
			return key.hashCode();
		}
	}

	private static class TableIterator implements Iterator<List<Object>> {

		private final int size;

		private final Node[] items;

		private int index = 0;

		public TableIterator(Node[] org, int size) {
			this.size = size;
			this.items = new Node[size];
			for (int i = 0, j = 0; i < org.length;i++) {
				if (null == org[i]) continue;
				items[j++] = org[i];
			}
		}

		@Override
		public boolean hasNext() {
			return index < size;
		}

		@Override
		public List<Object> next() {
			return items[index++].value;
		}
	}
}
