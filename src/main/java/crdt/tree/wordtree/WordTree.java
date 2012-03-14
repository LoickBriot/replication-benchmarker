/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crdt.tree.wordtree;

import crdt.PreconditionException;
import collect.Node;
import collect.Tree; 
import crdt.tree.*;
import crdt.CRDTMessage;
import crdt.Factory;
import crdt.set.CRDTSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author urso
 * 
 * 
 */
public class WordTree<T> extends CRDTTree<T> {
    CRDTSet words;
    WordPolicy<T> wcp;
    
    public WordTree(Factory<CRDTSet<List<T>>> setFactory, Factory<WordPolicy<T>> wcp) {
        this.wcp = wcp.create();
        this.words =  setFactory.create();
        this.words.addObserver(this.wcp);
    }
    
    @Override
    public CRDTMessage add(Node<T> father, T element) throws PreconditionException {
        if (!wcp.lookup().contains(father)) 
            throw new PreconditionException("Adding node " + element + " with father not in the tree");
        if (father.getChild(element) != null) 
            throw new PreconditionException("Adding node " + element + " already present under father");
        
        CRDTMessage msg = null;
        for (List<T> wf : wcp.addMapping(father)) {
            List<T> w =  new Word(wf, element);
            CRDTMessage add = words.add(w);
            msg = msg == null ? add : msg.concat(add);
        }
        return msg;
    }

    @Override
    public CRDTMessage remove(Node<T> subtree) throws PreconditionException {
        if (wcp.lookup().getRoot() == subtree) 
            throw new PreconditionException("Removing root");
        if (!wcp.lookup().contains(subtree)) 
            throw new PreconditionException("Removing node " + subtree + " not in the tree");
        
        Iterator<? extends Node<T>> subtreeIt = wcp.lookup().getBFSIterator(subtree);
        List<List<T>> toBeRemoved = new LinkedList<List<T>>();
        CRDTMessage msg = null;

        while (subtreeIt.hasNext()) {
            Node<T> n = subtreeIt.next();
            Collection<List<T>> w = wcp.delMapping(n);
            toBeRemoved.addAll(0, w);
        }
        for (List<T> w : toBeRemoved) {
            CRDTMessage del = words.remove(w);
            msg = msg == null ? del : msg.concat(del);
        }
        return msg;
    }

    @Override
    public void applyRemote(CRDTMessage op) {
        words.applyRemote(op);       
    }

    @Override
    public Tree<T> lookup() {
        return wcp.lookup();
    }

    @Override
    public Node<T> getRoot() {
        return wcp.lookup().getRoot();
    }

    @Override
    public WordTree<T>  create() {
        return new WordTree<T>(words, wcp);
    }

    @Override
    public void setReplicaNumber(int replicaNumber) {
        super.setReplicaNumber(replicaNumber);
        words.setReplicaNumber(replicaNumber);
    }

    @Override
    public String toString() {
        return "WordTree<" + words.getClass() + ',' + wcp.getClass() + ">{" + this.getReplicaNumber() + '}';
    }
}
