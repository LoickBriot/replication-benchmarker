/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jbenchmarker.ot.soct2;

import collect.VectorClock;
import java.util.Map;
import jbenchmarker.core.Document;
import jbenchmarker.core.Operation;

/**
 *
 * @param <O> Is type of operation managed in this algorithm
 * @author Stephane Martin
 * Algorithm SOCT2 With document, Log with transformations, and an vector clock.
 */
public class SOCT2 <O extends Operation> {

    private VectorClock siteVC;
    private SOCT2Log<O> log;
    //private Document doc;
    private int siteId;

    
    
    /**
     * Make soct2 instance with document (to apply modification) transformations, and replicat number or site id
     * @param ot Tranformations
     * @param siteId Site identifier
     */
    public SOCT2(SOCT2TranformationInterface ot, int siteId) {
        this.siteVC = new VectorClock();
        this.log = new SOCT2Log(ot);
        
        this.siteId = siteId;
    }

    /**
     * 
     * @return the vector clock of the instance
     */
    public VectorClock getSiteVC() {
        return siteVC;
    }

    /**
     * @return return log object
     */
    public SOCT2Log getLog() {
        return log;
    }

    /**
     * @return replicat number or site identifier
     */
    public int getSiteId() {
        return siteId;
    }

    /**
     * Check if the operation is ready by its vector clock
     * @param siteId Site of operation
     * @param vcOp Vector clock of this operation.
     * @return true if its ready false else.
     */
    public boolean readyFor(int siteId, VectorClock vcOp) {
        if (this.siteVC.getSafe(siteId) != vcOp.getSafe(siteId)) {
            return false;
        }
        for (Map.Entry<Integer, Integer> e : vcOp.entrySet()) {
            if ((e.getKey() != siteId) && (this.siteVC.getSafe(e.getKey()) < e.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method put on operation the vector clock, increse the vector clock
     * the operation is not applyed
     * @param op The operation 
     * @return Soct2Message with vector clock.
     */
    public SOCT2Message estampileMessage(O op) {
        SOCT2Message ret = new SOCT2Message(new VectorClock(siteVC), siteId, op);
        this.siteVC.inc(this.siteId);
        this.log.add(ret);
        //doc.apply((Operation)op);

        return ret;
    }

    /**
     * Integre operation sent by another site or replicats.
     * The operation is returned to apply on document
     * @param Soct2message Is a message which contains the operation and vector clock
     */
    public Operation integrateRemote(SOCT2Message Soct2message) {

        if (this.readyFor(Soct2message.getSiteId(), Soct2message.getClock())) {
            this.log.merge(Soct2message);
            //this.getDoc().apply((Operation) Soct2message.getOperation());
            this.log.add(Soct2message);
            this.siteVC.inc(Soct2message.getSiteId());
            return Soct2message.getOperation();
        } else {
            throw new RuntimeException("it seems causal reception is broken");
        }
    }
}
