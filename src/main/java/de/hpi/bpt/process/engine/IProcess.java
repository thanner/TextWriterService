package de.hpi.bpt.process.engine;

import de.hpi.bpt.hypergraph.abs.Vertex;

import java.util.Set;

/***
 *
 * @author Artem Polyvyanyy
 *
 */
public interface IProcess {
    boolean isTerminated();

    Set<Vertex> getEnabledElements();

    boolean fire(Vertex t);

    void serialize();
}
