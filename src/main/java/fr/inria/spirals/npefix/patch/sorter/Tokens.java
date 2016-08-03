package fr.inria.spirals.npefix.patch.sorter;

import fr.inria.spirals.npefix.patch.sorter.tokenizer.Tokenizer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class Tokens implements List<Token>{

	private List<Token> tokens = new ArrayList<>();
	private Map<String, List<Integer>> tokensPosition = new HashMap<>();
	private Tokenizer tokenizer;

	public Tokens(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	@Override
	public int size() {
		return tokensPosition.size();
	}

	public int fullSize() {
		return tokens.size();
	}

	@Override
	public boolean isEmpty() {
		return tokens.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Token)) {
			return false;
		}
		return tokensPosition.containsKey(tokenizer.computeRepresentation((Token) o));
	}

	@Override
	public Iterator<Token> iterator() {
		return tokens.iterator();
	}

	@Override
	public Object[] toArray() {
		return tokens.toArray();
	}

	@Override
	public <T> T[] toArray(T[] ts) {
		return tokens.toArray(ts);
	}

	@Override
	public boolean add(Token token) {
		String tToken = tokenizer.computeRepresentation(token);
		if (!this.tokensPosition.containsKey(tToken)) {
			this.tokensPosition.put(tToken, new ArrayList<Integer>());
		}
		List<Integer> integers = this.tokensPosition.get(tToken);
		integers.add(this.fullSize());
		return tokens.add(token);
	}

	@Override
	public boolean remove(Object o) {
		return tokens.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext(); ) {
			Object next = iterator.next();
			if (!this.contains(next)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Token> collection) {
		for (Iterator<? extends Token> iterator = collection.iterator(); iterator.hasNext(); ) {
			Token next = iterator.next();
			if(!this.add(next)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(int i, Collection<? extends Token> collection) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		return false;
	}

	@Override
	public void clear() {
		this.tokens.clear();
		this.tokensPosition.clear();
	}

	@Override
	public Token get(int i) {
		return tokens.get(i);
	}

	@Override
	public Token set(int i, Token token) {
		return tokens.set(i, token);
	}

	@Override
	public void add(int i, Token token) {

	}

	@Override
	public Token remove(int i) {
		Token token = get(i);
		String tToken = tokenizer.computeRepresentation(token);
		this.tokensPosition.get(tToken).remove(i);
		return tokens.remove(i);
	}

	@Override
	public int indexOf(Object o) {
		return tokens.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return tokens.lastIndexOf(o);
	}

	@Override
	public ListIterator<Token> listIterator() {
		return tokens.listIterator();
	}

	@Override
	public ListIterator<Token> listIterator(int i) {
		return tokens.listIterator(i);
	}

	@Override
	public Tokens subList(int i, int i1) {
		Tokens iTokens = new Tokens(tokenizer);
		for (Token token :  tokens.subList(i, i1)) {
			iTokens.add(token);
		}
		return iTokens;
	}

	public int countWordCanPrefix(Token token) {
		Set<String> followingPrefix = new HashSet<>();

		String s = tokenizer.computeRepresentation(token);
		List<Integer> positions = tokensPosition.get(s);
		for (int i = 0; i < positions.size(); i++) {
			int position = positions.get(i);
			int expectedPositionToken = position - 1;
			if (expectedPositionToken < 0) {
				continue;
			}
			String expected = tokenizer.computeRepresentation(get(expectedPositionToken));
			followingPrefix.add(expected);
		}
		return followingPrefix.size();
	}

	public int nbWordCanFollow(List<Token> predicates) {
		int output = 0;
		int size = predicates.size();

		Token token = predicates.get(size - 1);
		String s = tokenizer.computeRepresentation(token);
		List<Integer> positions = tokensPosition.get(s);
		if (positions == null) {
			return 0;
		}
		positionLoop: for (int i = 0; i < positions.size(); i++) {
			int position = positions.get(i);
			for (int j = 0; j < size - 1; j++) {
				int expectedPositionToken = position - size + j;
				if (expectedPositionToken < 0) {
					continue positionLoop;
				}
				String predicate = tokenizer.computeRepresentation(predicates.get(j));
				String expected = tokenizer.computeRepresentation(get(expectedPositionToken));
				if (!expected.equals(predicate)) {
					continue positionLoop;
				}
			}
			int expectedPositionToken = position + 1;
			if (expectedPositionToken >= this.fullSize()) {
				continue positionLoop;
			}
			output ++;
		}

		return output;
	}

	public int countWordCanFollow(List<Token> predicates) {
		Set<String> followingWords = new HashSet<>();
		int size = predicates.size();

		Token token = predicates.get(size - 1);
		String s = tokenizer.computeRepresentation(token);
		List<Integer> positions = tokensPosition.get(s);
		positionLoop: for (int i = 0; i < positions.size(); i++) {
			int position = positions.get(i);
			for (int j = 0; j < size - 1; j++) {
				int expectedPositionToken = position - size + j;
				if (expectedPositionToken < 0) {
					continue positionLoop;
				}
				String predicate = tokenizer.computeRepresentation(predicates.get(j));
				String expected = tokenizer.computeRepresentation(get(expectedPositionToken));
				if (!expected.equals(predicate)) {
					continue positionLoop;
				}
			}
			int expectedPositionToken = position + 1;
			if (expectedPositionToken >= this.fullSize()) {
				continue positionLoop;
			}
			String expected = tokenizer.computeRepresentation(get(expectedPositionToken));
			followingWords.add(expected);
		}

		return followingWords.size();
	}
	public int count (Token token) {
		return count(token,Collections.EMPTY_LIST);
	}

	public int count (Token token, List<Token> predicates) {
		if (!this.contains(token)) {
			return 0;
		}
		String tToken = tokenizer.computeRepresentation(token);
		List<Integer> positions = tokensPosition.get(tToken);
		int size = predicates.size();
		if (size == 0) {
			return positions.size();
		}
		int output = 0;
		positionLoop: for (int i = 0; i < positions.size(); i++) {
			int position = positions.get(i);
			for (int j = 0; j < size; j++) {
				int expectedPositionToken = position - size + j;
				if (expectedPositionToken < 0) {
					continue positionLoop;
				}
				String predicate = tokenizer.computeRepresentation(predicates.get(j));
				String expected = tokenizer.computeRepresentation(get(expectedPositionToken));
				if (!expected.equals(predicate)) {
					continue positionLoop;
				}
			}
			output ++;
		}
		return output;
	}

	public Set<Tokens> getAllNGram(int n) {
		Set<Tokens> output = new HashSet<>();

		ArrayDeque<Token> deque = new ArrayDeque<>();

		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			deque.add(token);
			if (deque.size() == n) {
				Tokens current = new Tokens(tokenizer);
				current.addAll(deque);
				output.add(current);
				deque.poll();
			}
		}
		return output;
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Tokens tokens1 = (Tokens) o;

		if (tokens1.size() != this.size()) {
			return false;
		}
		if (tokens1.fullSize() != this.fullSize()) {
			return false;
		}
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			String s = tokenizer.computeRepresentation(token);
			String s1 = tokenizer.computeRepresentation(tokens1.tokens.get(i));
			if (!s.equals(s1)) {
				return false;
			}
		}

		return true;

	}

	@Override
	public int hashCode() {
		return tokens.hashCode();
	}
}
