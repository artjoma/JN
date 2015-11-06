package org.jn;
/**
 * Node state
 * @author ArtjomAminov
 *
 * 5 Nov 2015 13:39:19
 */
public enum JNState {
	/**
	 * Connection to all nodes in progess in progress.
	 */
	SYNCHRONIZATION, 
	/**
	 * Connected to all nodes in cluster
	 */
	SYNCHRONIZED,
	/**
	 * shutdown() called or can't startup
	 */
	SHUTDOWN
}
