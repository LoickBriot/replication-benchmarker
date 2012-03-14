/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crdt.tree.graphtree.connection;

import collect.HashMapSet;
import crdt.set.SetOperation;
import crdt.tree.graphtree.Edge;
import crdt.tree.graphtree.GraphConnectionPolicy;
import java.util.HashSet;
import java.util.Observable;

/**
 *
 * @author moi
 */
public abstract class GraphConnectionPolicyInc<T> extends GraphConnectionPolicy<T> {

    protected HashMapSet<T, Edge<T>> lookup;
    protected HashSet<T> nodes;
    protected HashMapSet<T, Edge<T>> badEdges;
    protected HashMapSet<T, Edge<T>> goodEdges;
    
    @Override
    public void update(Observable o, Object op) {
        if (op instanceof SetOperation) {
            SetOperation op2 = (SetOperation) op;
            if (op2.getContent() instanceof crdt.tree.edgetree.Edge) {
                /*
                 * L'objet est une arête
                 */
                Edge<T> edge = (Edge<T>) op2.getContent();
                if (op2.getType() == SetOperation.OpType.add) {
                    /*
                     * Ajout
                     */
                    boolean good = true;
                    if (!nodes.contains(edge.getFather())) {
                        badEdges.put(edge.getFather(), edge);
                        good = false;
                    }
                    if (!nodes.contains(edge.getFather())) {
                        badEdges.put(edge.getSon(), edge);
                    }
                    if (good) {
                        goodEdges.put(edge.getSon(), edge);
                        goodEdges.put(edge.getFather(), edge);
                        this.addEdge(edge);

                    }


                } else {
                    /*
                     * Suppression
                     */
                    delEdge((Edge<T>) op2.getContent());
                }

            } else {
                /*
                 * L'objet est un noeud
                 */
                T node = (T) op2.getContent();
                if (op2.getType() == SetOperation.OpType.add) {
                    /*
                     * Ajout
                     */
                    nodes.add(node);
                    for (Edge<T> e : badEdges.removeAll(node)) {
                        
                        if ((e.getFather() == node && nodes.contains(e.getSon()))
                                || (e.getSon() == node && nodes.contains(e.getFather()))) {
                            addEdge(e);
                            goodEdges.put(node, e);
                        }
                    }
                    //addNode(node);
                } else {
                    /*
                     * Suppression
                     */
                    for (Edge<T> e : goodEdges.removeAll(node)) {
                        badEdges.put(node, e);
                        delEdge(e);/* Enlève de la vue */
                        if (e.getFather()==node){
                            goodEdges.remove(e.getSon(), e);
                        }else{
                            goodEdges.remove(e.getFather(), e);
                        }
                    }
                    nodes.remove(node);
                    //delNode(node);
                }
            }
        }
    }

    boolean CheckEdge(Edge<T> e) {
        return nodes.contains(e.getFather()) && nodes.contains(e.getSon());
    }

    @Override
    public HashMapSet<T, Edge<T>> lookup() {
        return lookup;
    }

    abstract void addEdge(Edge<T> edge);

    abstract void delEdge(Edge<T> edge);

    /*abstract void addNode(T n);

    abstract void delNode(T n);*/
    //@Override
    //public abstract GraphConnectionPolicyInc<T> create();

    abstract GraphConnectionPolicyInc createSpe();

    public GraphConnectionPolicyInc create() {
        GraphConnectionPolicyInc ret = createSpe();
        ret.lookup = new HashMapSet<T, Edge<T>>();
        return ret;
    }
}
