/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jbenchmarker.ot.tree;

import collect.OrderedNode;
import crdt.CRDTMessage;
import crdt.OperationBasedOneMessage;
import crdt.tree.orderedtree.CRDTOrderedTree;
import crdt.tree.orderedtree.PositionIdentifierTree;
import jbenchmarker.ot.ottree.OTTree;
import jbenchmarker.ot.ottree.OTTreeTranformation;
import jbenchmarker.ot.soct2.SOCT2;
import jbenchmarker.ot.soct2.SOCT2Log;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.util.Arrays;
import junit.framework.Assert;

/**
 *
 * @author Stephane Martin <stephane.martin@loria.fr>
 */
public class OtTreeTest {

    private void assertSameTree(OrderedNode on, OrderedNode ot) {
        assertTrue(ot + " expected : " + on, CRDTOrderedTree.sameNode(on, ot));
    }

    private void assertSameTree(OrderedNode on, PositionIdentifierTree ot) {
        assertTrue(ot.lookup() + " expected : " + on, CRDTOrderedTree.sameNode(on, ot.lookup()));
    }

    @Test
    public void otTreeTestBasic() throws Exception {
        OTTree tree1 = new OTTree(new SOCT2(0, new SOCT2Log(new OTTreeTranformation()), null));
        OTTree tree2 = new OTTree(new SOCT2(1, new SOCT2Log(new OTTreeTranformation()), null));
        CRDTMessage mess = tree1.add(Arrays.asList(new Integer[]{}), 0, 'a');
        mess = mess.concat(tree1.add(Arrays.asList(new Integer[]{0}), 0, 'm')).clone();
        
        
        
        assertEquals(tree1.lookup().childrenNumber(), 1);
        assertEquals(tree1.lookup().getChild(0).childrenNumber(), 1);
        assertEquals(tree1.lookup().getChild(0).getChild(0).childrenNumber(), 0);
        assertEquals(tree1.lookup().getChild(0).getValue(), 'a');
        assertEquals(tree1.lookup().getChild(0).getChild(0).getValue(), 'm');

        tree2.applyRemote(mess);

        assertEquals(tree1, tree2);
        CRDTMessage mess2=tree1.add(Arrays.asList(new Integer[]{0,0}), 0, 'x').clone();
        mess = tree2.add(Arrays.asList(new Integer[]{0}), 0, 'c');
        mess = mess.concat(tree2.add(Arrays.asList(new Integer[]{0, 0}), 0, 'd')).clone();
        Assert.assertFalse(tree1.equals(tree2));
        
        
        tree1.applyRemote(mess);
       
        
        Assert.assertFalse(tree1.equals(tree2));
        
        tree2.applyRemote(mess2);
        assertEquals(tree1, tree2);
        assertEquals(tree1.lookup().childrenNumber(), 1);
        assertEquals(tree1.lookup().getChild(0).childrenNumber(), 1);
        assertEquals(getFromPath(tree1.lookup(),0,0).childrenNumber(), 0);
        assertEquals(tree1.lookup().getChild(0).getValue(), 'a');
        assertEquals(getFromPath(tree1.lookup(),0,0).getValue(), 'm');

    }
    private OrderedNode getFromPath(OrderedNode n,int... p){
        
        for (int a:p){
            n=n.getChild(a);
        }
        return n;
    }
}