package com.tyron.code.path.impl;

import com.tyron.code.path.PathNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation of {@link PathNode}.
 *
 * @param <P>
 * 		Expected parent path node value type.
 * @param <V>
 * 		Wrapped path value type.
 *
 * @author Matt Coley
 */
public abstract class AbstractPathNode<P, V> implements PathNode<V> {
	private final PathNode<P> parent;
	private final Class<V> valueType;
	private final String id;
	private final V value;

	/**
	 * @param id
	 * 		Unique node type ID.
	 * @param parent
	 * 		Optional parent node.
	 * @param valueType
	 * 		Type of value.
	 * @param value
	 * 		Value instance.
	 */
	protected AbstractPathNode(@NotNull String id, @Nullable PathNode<P> parent, @NotNull Class<V> valueType, @NotNull V value) {
		this.id = id;
		this.parent = parent;
		this.valueType = valueType;
		this.value = value;
	}

	/**
	 * Convenient parent value getter.
	 *
	 * @return Parent value, or {@code null}.
	 */
	@Nullable
	protected P parentValue() {
		return parent == null ? null : parent.getValue();
	}

	/**
	 * @param path
	 * 		Some other path.
	 *
	 * @return Comparing our parent value type to the given path,
	 * and the other path parent value type to our own.
	 * If we are the child type, then {@code -1} or {@link 1} if the parent type.
	 * Otherwise {@code 0}.
	 */
	protected int cmpHierarchy(@NotNull PathNode<?> path) {
		if (getValueType() != path.getValueType()) {
			// Check direct parent (quicker validation) and then if that does not pass, a multi-level descendant test.
			if ((parent != null && parent.typeIdMatch(path)) || isDescendantOf(path)) {
				// We are the child type, show last.
				return 1;
			}

			// Check direct parent (quicker validation) and then if that does not pass, a multi-level descendant test.
			if ((path.getParent() != null && typeIdMatch(path.getParent())) || path.isDescendantOf(this)) {
				// We are the parent type, show first.
				return -1;
			}
		}

		// Unknown
		return 0;
	}

	/**
	 * @param path
	 * 		Some other path.
	 *
	 * @return Comparing {@link #getParent() the parent} to the given value.
	 */
	protected int cmpParent(@NotNull PathNode<?> path) {
		if (parent != null)
			return parent.compareTo(path);
		return 0;
	}

	@Override
	public PathNode<P> getParent() {
		return parent;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T, I extends PathNode<T>> I getParentOfType(@NotNull Class<T> type) {
		if (getValueType().isAssignableFrom(type))
			return (I) this;
		if (parent == null) return null;
		return parent.getParentOfType(type);
	}

	@NotNull
	@Override
	public String typeId() {
		return id;
	}

	@NotNull
	@Override
	public Class<V> getValueType() {
		return valueType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getValueOfType(@NotNull Class<T> type) {
		if (getValueType().isAssignableFrom(type)) {
            return (T) getValue();
        }
		if (parent == null) {
            return null;
        }
		return parent.getValueOfType(type);
	}

	@NotNull
	@Override
	@SuppressWarnings("all")
	public V getValue() {
		return value;
	}

	@Override
	public int compareTo(@NotNull PathNode<?> o) {
		if (this == o) return 0;
		int cmp = localCompare(o);
		if (cmp == 0) cmp = cmpHierarchy(o);
		if (cmp == 0) cmp = cmpParent(o);
		return cmp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PathNode<?> node = (PathNode<?>) o;

		return getValue().equals(node.getValue());
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}
}