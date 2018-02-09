package de.hpi.bpt.graph.abs;

import de.hpi.bpt.hypergraph.abs.IDirectedHyperEdge;
import de.hpi.bpt.hypergraph.abs.IVertex;

/**
 * Interface describing directed binary graph edge behavior (constrained by implementation)
 * Directed binary edge is an edge that connects exactly two vertices and makes a difference between source and target
 *
 * @param <V> Vertex type employed in the edge
 * @author Artem Polyvyanyy
 */
public interface IDirectedEdge<V extends IVertex> extends IDirectedHyperEdge<V>, IEdge<V> {
    /**
     * Get source vertex
     *
     * @return Source vertex
     */
    V getSource();

    /**
     * Set source vertex
     *
     * @param v Source vertex
     * @return Vertex set as source, <code>null</code> upon failure
     */
    V setSource(V v);

    /**
     * Get target vertex
     *
     * @return Target vertex
     */
    V getTarget();

    /**
     * Set target vertex
     *
     * @param v Target vertex
     * @return Vertex set as target, <code>null</code> upon failure
     */
    V setTarget(V v);

    /**
     * Set directed graph edge vertices
     *
     * @param v1 Source vertex
     * @param v2 Target vertex
     */
    void setVertices(V s, V t);
}
