package de.hpi.bpt.graph.abs;

import de.hpi.bpt.hypergraph.abs.IVertex;

import java.util.Collection;

/**
 * Abstract tree interface
 *
 * @param <E> template for edge (extends IEdge)
 * @param <V> template for vertex (extends IVertex)
 * @author Artem Polyvyanyy
 */
public interface ITree<E extends IEdge<V>, V extends IVertex> {

    V getRoot();

    V reRoot(V v);

    Collection<V> getChildren(V v);

    Collection<V> getAllChildren(V v);

    V getParent(V v);

    Collection<V> getAllParents(V v);

    E addChild(V parent, V child);

    Collection<V> getLeaves();

    Collection<V> getInternalNodes();

}
