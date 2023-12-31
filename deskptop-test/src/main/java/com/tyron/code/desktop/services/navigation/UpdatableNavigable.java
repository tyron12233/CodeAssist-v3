package com.tyron.code.desktop.services.navigation;

import com.tyron.code.path.PathNode;
import org.jetbrains.annotations.NotNull;

/**
 * Navigable subtype with intent for content to be updated.
 *
 * @author Matt Coley
 */
public interface UpdatableNavigable extends Navigable {
	/**
	 * Called when the underlying content of the path is updated.
	 * The new content is wrapped in a new path, which is passed to this method.
	 * <br>
	 * Implementations must update their internal {@link #getPath()} value
	 * if the passed path type is the same as the one they represent.
	 * For instance, a {@link ClassPane} will have this called when
	 * a user modifies a class. It will then update its {@link ClassPane#getPath()} backing value.
	 * It will then pass along the update message to its {@link ClassPane#getNavigableChildren()}.
	 * Content displaying {@link ClassMember} values should re-fetch the member info from the new path
	 * to maintain correctness.
	 *
	 * @param path
	 * 		New path value.
	 */
	void onUpdatePath(@NotNull PathNode<?> path);
}