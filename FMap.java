// Bart Flaherty and Nick Aquino
// bartholomew.flaherty@gmail.com
// aquino.nj@gmail.com

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;


public abstract class FMap<K, V> implements Iterable<K>{

	public abstract boolean isEmpty();
	public abstract int size();
	public abstract boolean containsKey(K key);
	public abstract V get(K key);
	public abstract String toString();
	public abstract FMap<K, V> accept(Visitor<K, V> avisitor);
		
	// loops through an FMap, puts its keys into a given ArrayList
	abstract ArrayList<K> addKeysToList(ArrayList<K> aList);
	
	// recurses until it gets to an instance of Empty
	// checks if the FMap was created with a comparator
	// returns null if it was not
	abstract boolean isSorted();
	abstract Comparator<? super K> getComparator();
	
	@Override
	public boolean equals(Object o){
		if (o == null)
			return false;
		if (!(o instanceof FMap))
			return false;
		FMap<K, V> one = this;
		FMap<K, V> two = (FMap<K, V>) o;
		
		Iterator<K> oneiter = one.iterator();
		Iterator<K> twoiter = two.iterator();

		K key;
		
		while(oneiter.hasNext()){
			key = oneiter.next(); // a key contained in this
			if (!(two.containsKey(key))) 
				return false;
			}
		
		while (twoiter.hasNext()){
			key = twoiter.next(); // a key contained in the passed FMap
			if (!(one.containsKey(key)))
				return false;
		}
		
		// at this point, we have returned false unless both FMaps
		// contain exactly the same unique keys
		
		// so this loop checks that the values at each key are equal
		oneiter = one.iterator(); 
		while (oneiter.hasNext()){
			key = oneiter.next();
			if (one.containsKey(key))
				if (!(one.get(key).equals(two.get(key))))
					return false;
		}
		return true;		
	}

	@Override
	public int hashCode(){
		
		int result = 0;
		
		if (this == null){
			return -1;
		}
		
		Iterator<K> iter = this.iterator();
		
		int temphash = 0;

		while (iter.hasNext()){
			K key = iter.next();
			V value = this.get(key);
			temphash = key.hashCode();
			result = (result + 7 * temphash);
			temphash = value.hashCode();
			result = (result + 47 * temphash);
		}
		return result;
	}
	
	// Basic creators for OLD FMap instances	
	public FMap<K, V> add(K key, V value){
		return FMap.add(key, value, this);
	}	
	static <K, V> FMap<K, V> add(K key, V value, FMap<K, V> map){
		return new Add(key, value, map);
	}
	public static <K, V> FMap<K, V> emptyMap(){
		return new Empty();
	}
	
	// Iterator
	public Iterator<K> iterator() {
		return new KeyIterator<K>(this);
	}
	
	// Basic creator for 1-argument emptyMap
	public static <K, V> FTree<K, V> emptyMap(Comparator<? super K> c){
		return new FTree.EmptyTree<K, V>(c);
	}
	
	static abstract class FTree<K, V> extends FMap<K, V>{
		
		// precalculates size, initializes to 0, 
		// updated in Node constructor
		int size;
		boolean red;
		
		// single argument passed to basic creator add
		// used by methods in FTree to compare the keys
		Comparator<? super K> c;

		// creates an empty tree
		public FTree(Comparator<? super K> c){
			this.c = c;
			red = false; // all empty trees are black
		}
		// overrides basic creator add, calls insert
		//public abstract FMap<K, V> add(K key, V value);
		
		/*{
			return this.add(key, value);
		}*/
		 
		@Override
		public FTree<K, V> add(K key, V value){
			return this.insert(key, value);
		}
		
		abstract FTree<K, V> insert(K key, V value);
		
		String printcolor(){
			if (red)
				return "Red";
			else
				return "Black";
		}
		
		static class EmptyTree<K, V> extends FTree<K, V>{
				
			public EmptyTree(Comparator<? super K> c){
				super(c);
				size = 0;
				red = false;
			}
		
			public FTree<K, V> insert(K key, V value){
				EmptyTree<K, V> left = new EmptyTree<K, V>(this.c);
				EmptyTree<K, V> right = new EmptyTree<K, V>(this.c);
				//this.size = this.size + 1;
				return new Node<K, V>(key, value, left, right, true);
			}
			
			@Override
			public boolean isEmpty() {
				return true;
			}

			@Override
			public int size() {
				return size;
				//return 0;
			}

			@Override
			public boolean containsKey(K key) {
				return false;
			}

			@Override
			public V get(K key) {
				throw new IllegalArgumentException();
			}

			@Override
			public String toString() {
				System.out.println("EmptyNode " + printcolor());
				return "{...(" + this.size() + " entries)...}";
			}


			@Override
			ArrayList<K> addKeysToList(ArrayList<K> aList) {
				return aList;
			}

			// all FTrees are sorted
			@Override
			boolean isSorted() {
				return true;
			}

			// all FTrees have a comparator instance variable
			@Override
			Comparator<? super K> getComparator() {
				return this.c;
			}

			@Override
			public FMap<K, V> accept(Visitor<K, V> avisitor) {
				return FMap.emptyMap(this.c);
			}
		}
		static class Node<K, V> extends FTree<K, V>{
			K k0; // the key of the tree node
			V v0; // the value associated with that key
			FTree<K, V> l0; // the left part of the tree
			FTree<K, V> r0; // the right part of the tree
			
			public Node(
					K key, 
					V value, 
					FTree<K, V> left, 
					FTree<K, V> right,
					boolean redorblack){
				super(left.getComparator());
				k0 = key;
				v0 = value;
				l0 = left;
				r0 = right;
				this.size = (left.size + right.size + 1);
				red = redorblack;
			}
			
			public Node<K, V> insert(K key, V value){
				
				Node<K, V> node = this;
				
				if (this.c.compare(key, k0) < 0){
					node = new Node<K, V>(
						k0, v0, l0.insert(key, value), r0, red);
					node = node.balance(true);
				}
				if (this.c.compare(key, k0) == 0){
					node = new Node<K, V>(
							key, value, l0, r0, true);
				}
				if (this.c.compare(key, k0) > 0){	
					 node = new Node<K, V>(
							 k0, v0, l0, r0.insert(key, value), red);
					 node = node.balance(false);
				}
				
				return node;
			}
			
			Node<K, V> makeBlack(){
				return new Node<K, V>(
						this.k0, 
						this.v0, 
						this.l0, 
						this.r0, 
						false);
			}
			
			Node<K, V> balance(boolean leftorright){
				
				Node<K, V> result = this;
				
				Node<K, V> x;
				Node<K, V> y;
				Node<K, V> z;
				FTree<K, V> a;
				FTree<K, V> b;
				FTree<K, V> c;
				FTree<K, V> d;
				
				if (leftorright == true) { 
					// if the node was inserted on the left side
					if (!(this.l0.isEmpty())){
						Node<K, V> leftChild = (Node<K, V>) this.l0;
						if (leftChild.red && leftChild.l0.red){
							z = this;
							y = leftChild;
							x = (Node<K, V>) leftChild.l0;
							a = x.l0;
							b = x.r0;
							c = y.r0;
							d = z.r0;
							Node<K, V> newleft = new Node<K, V>(x.k0, x.v0, a, b, false);
							Node<K, V> newright = new Node<K, V>(z.k0, z.v0, c, d, false);
							result = new Node<K, V>(y.k0, y.v0, newleft, newright, true);
						}
						else if (leftChild.red && leftChild.r0.red){
							z = this;
							y = (Node<K, V>) leftChild.r0;
							x = leftChild;
							a = x.l0;
							b = y.l0;
							c = y.r0;
							d = z.r0;
							Node<K, V> newleft = new Node<K, V>(x.k0, x.v0, a, b, false);
							Node<K, V> newright = new Node<K, V>(z.k0, z.v0, c, d, false);
							result = new Node<K, V>(y.k0, y.v0, newleft, newright, true);
						}
					}
				}
				else { // if the node was inserted on the right side
					if (!(this.r0.isEmpty())){
						Node<K, V> rightChild = (Node<K, V>) this.r0;
						if (rightChild.red && rightChild.l0.red){
							x = this;
							z = rightChild;
							y = (Node<K, V>) rightChild.l0;
							a = x.l0;
							b = y.l0;
							c = y.r0;
							d = z.r0;
							Node<K, V> newleft = new Node<K, V>(x.k0, x.v0, a, b, false);
							Node<K, V> newright = new Node<K, V>(z.k0, z.v0, c, d, false);
							result = new Node<K, V>(y.k0, y.v0, newleft, newright, true);
						}
						else if (rightChild.red && rightChild.r0.red){
							x = this;
							y = rightChild;
							z = (Node<K, V>) rightChild.r0;
							a = x.l0;
							b = y.l0;
							c = z.l0;
							d = z.r0;
							Node<K, V> newleft = new Node<K, V>(x.k0, x.v0, a, b, false);
							Node<K, V> newright = new Node<K, V>(z.k0, z.v0, c, d, false);
							result = new Node<K, V>(y.k0, y.v0, newleft, newright, true);
						}
					}
				}

				
				return result;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public int size() {
				return size;
			}

			
			
			@Override
			public boolean containsKey(K key) {
				if (this.c.compare(key, k0) < 0){
					return l0.containsKey(key);
				}
				if (this.c.compare(key, k0) == 0){
					return true;
				}
				else // meaning, if (this.c.compare(key, k0) > 0)	
					 return r0.containsKey(key);	
			}

			@Override
			public V get(K key) {
				if (this.c.compare(key, k0) < 0)
					return l0.get(key);
				if (this.c.compare(key, k0) == 0)
					return v0;
				else // meaning, if (this.c.compare(key, k0) > 0)	
					 return r0.get(key);
			}
			

			@Override
			public String toString() {
				System.out.println("This Node: " + k0 + " " + v0 + " " + printcolor());
				System.out.println(k0 + " Left Node: ");
				l0.toString();
				System.out.println(k0 + " Right Node: ");
				r0.toString();
				System.out.println("");
				return "{...(" + this.size() + " entries)...}";
				
			}

			@Override
			ArrayList<K> addKeysToList(ArrayList<K> aList) {
				aList.add(k0);
				aList = l0.addKeysToList(aList);
				aList = r0.addKeysToList(aList);
				return aList;
			}

			@Override
			boolean isSorted() {
				return true;
			}

			@Override
			Comparator<? super K> getComparator() {
				return this.c;
			}

			
			// accept returns an FMap with key value pairs
			// that are equal to 
			// (originalkey, visitor.visit(originalkey, originalvalue)
			
			@Override
			public FMap<K, V> accept(Visitor<K, V> avisitor) {

				FTree<K, V> left = (FTree<K, V>) l0.accept(avisitor);
				V value = avisitor.visit(k0, v0);
				FTree<K, V> right = (FTree<K, V>) r0.accept(avisitor);
				
				FMap<K, V> map = new Node<K, V>(
						k0,
						value,
						left,
						right,
						red);
						
				return map;
			}
		}
	}
	
	class KeyIterator<K> implements Iterator<K>{

		// a list of the keys in the FMap
		ArrayList<K> state;
		
		// sets the FMap instance variable to equal the passed argument
		// loops through the keys and adds them to the ArrayList state
		// sorts state according to the comparator
		KeyIterator(FMap<K, ?> f){
			state = new ArrayList<K>();
			f.addKeysToList(state);
			if (f.isSorted())
				Collections.sort(state, f.getComparator());
		}
			
		// you're at the end of the list of keys if state is empty
		// otherwise, return true
		public boolean hasNext() {
			//if (state.size() == 0)
			//System.out.println(((Integer) state.size()).toString());
			if (state.isEmpty())
				return false;
			else
				return true;
		}
		
		// returns the K at the 0 index of state, 
		// and then removes it from state
		public K next() {
			K nextKey;
			if (!(state.isEmpty())) {
				nextKey = state.get(0);
				state.remove(0);
				return nextKey;
			}
			else {
				throw new NoSuchElementException();  
			}
		}

		// does nothing
		public void remove() {
			String msg = "Remove method is unimplemented";
			throw new UnsupportedOperationException(msg);
		}
	}

	public static class Empty<K, V> extends FMap<K, V>{
		
		// no instance variables
		Comparator<? super K> sortby;
		boolean sorted;
		
		
		public Empty(){
			sorted = false;
			// constructor does nothing
		}
		
		public Empty(Comparator<? super K> c){
			sorted = true;
			sortby = c;
		}

		// instances of Empty are always Empty
		public boolean isEmpty() {
			return true;
		}

		// instances of Empty always have size 0
		public int size() {
			return 0;
		}

		// instances of Empty always return false 
		// because they can't contain keys
		public boolean containsKey(K key) {
			return false;
		}

		// the spec does not describe a get function for the Empty class
		// so this method throws an exception
		public V get(K key) {
			throw new IllegalArgumentException();
		}

		// has the exact same implementation as FMap.add(x, y).toString()
		// but this.size() will always return false on instances of Empty
		public String toString() {
			return "{...(" + this.size() + " entries)...}";
		}

		// returns the list, this is the base case for 
		// the recursion 
		ArrayList<K> addKeysToList(ArrayList<K> aList) {
			
			return aList;
		}

		@Override
		boolean isSorted() {
			return sorted;
		}

		// this is only called on instances of Empty instantiated
		// with a comparator argument (for which f.isSorted() is true)
		@Override
		Comparator<? super K> getComparator() { 
				return sortby;
		}

		@Override
		public FMap<K, V> accept(Visitor<K, V> avisitor) {
			return this;
		}		
	}
																			   
	public static class Add<K, V> extends FMap<K, V>{
		
		// instance variables
		K key;
		V value;
		FMap<K, V> m0;

		// constructor sets the instance values to equal the given arguments
		public Add(K givenkey, V givenvalue, FMap<K, V> map){
			key = givenkey;
			value = givenvalue;
			m0 = map;
		}
		
		// isEmpty() returns false on instances of the Add class
		public boolean isEmpty() {
			return false;
		}

		// counts the number of unique keys in the FMap
		public int size() {
			if (m0.containsKey(key)) // don't count duplicate keys
				return m0.size();
			return (1 + m0.size());	// if key on hand is not a duplicate,
									// recurse, and add 1
		}

		/* checks if the given key exists in the FMap */
		public boolean containsKey(K arg) {
			if (arg.equals(key))	// if the argument equals key on hand
				return true;		// return true
			return m0.containsKey(arg);	// else recurse
		}

		// returns the value paired with the first 
		// instance of K arg in the FMap
		public V get(K arg) {
			if (arg.equals(key))	// if the argument equals key on hand
				return value;		// return the value on hand

			return m0.get(arg);		// else recurse
		}

		/* toString, according to the spec, simpily returns the size
		 * of the FMap in a specifically formatted string.
		 */
		public String toString() {
			return ("{...(" + this.size() + " entries)...}");
		}

		// adds the key on hand to the ArrayList
		ArrayList<K> addKeysToList(ArrayList<K> aList) {
			if (!(m0.containsKey(key)))
				aList.add(key);
			return m0.addKeysToList(aList);
		}

		@Override
		boolean isSorted() {
			return m0.isSorted();
		}

		@Override
		Comparator<? super K> getComparator() {
			return m0.getComparator();
		}

		@Override
		public FMap<K, V> accept(Visitor<K, V> avisitor) {
			
			FMap<K, V> result = FMap.emptyMap();
			
			Iterator<K> iter = this.iterator();
			while (iter.hasNext()){
				K iterkey = iter.next();
				V itervalue = this.get(iterkey);
				V newvalue = avisitor.visit(iterkey, itervalue);
				result = result.add(iterkey, newvalue);
			}
			return result;
		}
	}
}