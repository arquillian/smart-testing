package org.arquillian.smart.testing.strategies.affected;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.ExportException;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;

class GraphExporter
{
    public void dumpGraph(DirectedGraph<JavaElement, DefaultEdge> graph, String graphName) {
        try {
            new DOTExporter<JavaElement, DefaultEdge>(component -> "\"" + component.getClassName() + "\"",
                null, null)
                .exportGraph(graph, new File(System.getProperty("java.io.tmpdir") + "/graph-" + graphName + ".dot"));
        } catch (ExportException e) {
            throw new RuntimeException(e);
        }
    }
}
