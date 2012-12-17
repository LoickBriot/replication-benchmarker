/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jbenchmarker.logootsplit;

import crdt.CRDTMessage;
import crdt.PreconditionException;
import jbenchmarker.core.MergeAlgorithm;
import jbenchmarker.core.SequenceOperation;
import jbenchmarker.factories.LogootSFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author urso
 */
public class LogootSAlgorithmTest {
    
    public LogootSAlgorithmTest() {
    }

    private MergeAlgorithm replica;

    @Before
    public void setUp() throws Exception {
        replica = (MergeAlgorithm) new LogootSFactory().create();
        
    }

    @Test
    public void testEmpty() {
        assertEquals("", replica.lookup());
    }

    @Test
    public void testInsert() throws PreconditionException {
        String content = "abcdejk", c2 = "fghi";
        int pos = 3;      
        replica.applyLocal(SequenceOperation.insert(0, content));
        assertEquals(content, replica.lookup());
        replica.applyLocal(SequenceOperation.insert(pos, c2));
        assertEquals(content.substring(0, pos) + c2 + content.substring(pos), replica.lookup());        
    }
        
    @Test
    public void testDelete() throws PreconditionException {
        String content = "abcdefghijk";
        int pos = 3, off = 4;       
        replica.applyLocal(SequenceOperation.insert(0, content));
        assertEquals(content, replica.lookup());
        replica.applyLocal(SequenceOperation.delete(pos, off));
        assertEquals(content.substring(0, pos) + content.substring(pos+off), replica.lookup());        
    }
    
    @Test
    public void testConcurrentDelete() throws PreconditionException {
        String content = "abcdefghij";
        CRDTMessage m1 = replica.applyLocal(SequenceOperation.insert(0, content));
        replica.applyLocal(SequenceOperation.insert(2, "2"));
        replica.applyLocal(SequenceOperation.insert(7, "7"));
        
        MergeAlgorithm replica2 = (MergeAlgorithm) new LogootSFactory().create();
        replica2.setReplicaNumber(2);
        m1.execute(replica2);
        CRDTMessage m2 = replica2.applyLocal(SequenceOperation.delete(1, 8));
        m2.execute(replica);
        assertEquals("a27j", replica.lookup());
  
    }
    
    @Test
    public void testMultipleDeletions() throws PreconditionException {
         
        String content = "abcdefghij";
        CRDTMessage m1 = replica.applyLocal(SequenceOperation.insert(0, content));
        replica.applyLocal(SequenceOperation.insert(2, "28"));
        replica.applyLocal(SequenceOperation.insert(10, "73"));
        CRDTMessage m2=replica.applyLocal(SequenceOperation.delete(3, 8));
        assertEquals("ab23ij", replica.lookup());
        
        MergeAlgorithm replica2 = (MergeAlgorithm) new LogootSFactory().create();
        replica2.setReplicaNumber(2);
        m1.execute(replica2);
        replica2.applyLocal(SequenceOperation.insert(4, "00"));
        m2.execute(replica2);
        assertEquals("ab00ij", replica2.lookup());
        
    }
    
    @Test
    public void testMultipleUpdates() throws PreconditionException {
         
        String content = "abcdefghij";
        CRDTMessage m1 = replica.applyLocal(SequenceOperation.insert(0, content));
        replica.applyLocal(SequenceOperation.insert(2, "2"));
        replica.applyLocal(SequenceOperation.insert(7, "7"));
        CRDTMessage m2=replica.applyLocal(SequenceOperation.update(1, 10,"test"));
        assertEquals("atestj", replica.lookup());
        
        MergeAlgorithm replica2 = (MergeAlgorithm) new LogootSFactory().create();
        replica2.setReplicaNumber(2);
        m1.execute(replica2);
        replica2.applyLocal(SequenceOperation.insert(4, "00"));
        m2.execute(replica2);
        assertEquals("atest00j", replica2.lookup());
        
    } 

    
    @Test
    public void testConcurrentUpdate() throws PreconditionException{
        String content = "abcdefghij";
        CRDTMessage m1 = replica.applyLocal(SequenceOperation.insert(0, content));
        replica.applyLocal(SequenceOperation.insert(2, "2"));
        replica.applyLocal(SequenceOperation.insert(7, "7"));
        
        MergeAlgorithm replica2 = (MergeAlgorithm) new LogootSFactory().create();
        replica2.setReplicaNumber(2);
        m1.execute(replica2);
        CRDTMessage m2 = replica2.applyLocal(SequenceOperation.update(1, 8, "0000000"));
        m2.execute(replica);
        assertEquals("a000000027j", replica.lookup());
    }
    
    @Test
    public void testUpdate() throws PreconditionException {
        String content = "abcdefghijk", upd = "xy";
        int pos = 3, off = 5;       
        replica.applyLocal(SequenceOperation.insert(0, content));
        assertEquals(content, replica.lookup());
        replica.applyLocal(SequenceOperation.update(pos, off, upd));
        assertEquals(content.substring(0, pos) + upd + content.substring(pos+off), replica.lookup());        
    }
}
