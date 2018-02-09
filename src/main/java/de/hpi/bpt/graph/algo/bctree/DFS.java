package de.hpi.bpt.graph.algo.bctree;

import de.hpi.bpt.graph.abs.IEdge;
import de.hpi.bpt.graph.abs.IGraph;
import de.hpi.bpt.hypergraph.abs.IVertex;

import java.util.Hashtable;
import java.util.Iterator;

public abstract class DFS<E extends IEdge<V>, V extends IVertex> {

    protected Hashtable<V, NodeAttrs> attrs = null;
    protected IGraph<E, V> graph;
    private Iterator<V> nodes = null;

    public DFS(IGraph<E, V> graph) {
        nodes = graph.getVertices().iterator();
        attrs = new Hashtable<V, NodeAttrs>(graph.getVertices().size());
        this.graph = graph;
        while (nodes.hasNext()) {
            prepareNode(nodes.next());
        }
    }

    protected void prepareNode(V node) {
        attrs.put(node, new NodeAttrs());
    }

    protected boolean visited(V node) {
        return attrs.get(node).visited;
    }

    protected void process(V node) {
        NodeAttrs attributes = attrs.get(node);
        attributes.visited = true;

        for (V i : this.graph.getAdjacent(node))
            if (!visited(i))
                process(i);
    }

    protected class NodeAttrs {
        boolean visited;

        public NodeAttrs() {
            visited = false;
        }
    }

}
