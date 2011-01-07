package com.floreysoft.jmte;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

class ScopedMap implements Map<String, Object> {
	private final Map<String, Object> rawModel;
	private final Stack<Map<String, Object>> scope = new Stack<Map<String, Object>>();
	
	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException();
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Set<Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public Object get(Object key) {
		for (int i = scope.size() - 1; i >= 0; i--) {
			Map<String, Object> map = scope.get(i);
			Object value = map.get(key);
			if (value != null) {
				return value;
			}
		}
		return rawModel.get(key);
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	public Object put(String key, Object value) {
		return getCurrentScope().put(key, value);
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		throw new UnsupportedOperationException();
	}

	public Collection<Object> values() {
		throw new UnsupportedOperationException();
	}

	public ScopedMap(Map<String, Object> rawModel) {
		if (rawModel == null) {
			throw new IllegalArgumentException("Model must not be null");
		}
		this.rawModel = rawModel;
	}

	public Map<String, Object> getRawModel() {
		return rawModel;
	}

	public void enterScope() {
		scope.push(createScope());
	}

	public void exitScope() {
		if (scope.size() == 0) {
			throw new IllegalStateException("There is no state to exit");
		}
		scope.pop();
	}

	protected Map<String, Object> getCurrentScope() {
		if (scope.size() > 0) {
			return scope.peek();
		} else {
			return rawModel;
		}

	}

	protected Map<String, Object> createScope() {
		return new HashMap<String, Object>();
	}

}