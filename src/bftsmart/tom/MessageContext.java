/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package bftsmart.tom;

import bftsmart.consensus.messages.ConsensusMessage;
import java.io.Serializable;

import bftsmart.tom.core.messages.TOMMessage;
import java.util.Set;

/**
 * This class represents the whole context of a request ordered in the system.
 * It stores all informations regarding the message sent and the consensus
 * execution that ordered it.
 * 
 * @author alysson
 */
public class MessageContext implements Serializable {
	
	private static final long serialVersionUID = -3757195646384786213L;
	
    private long timestamp;
    private byte[] nonces;
    private int regency;
    private int leader;
    private int consensusId;
    private Set<ConsensusMessage> proof;
            
    private int sender;
    private TOMMessage firstInBatch; //to be replaced by a statistics class
    private boolean lastInBatch; // indicates that the command is the last in the batch. Used for logging
    private boolean noOp;
    
    public boolean readOnly = false;
    
    public MessageContext(long timestamp, byte[] nonces, int regency, int leader, int consensusId, Set<ConsensusMessage> proof, int sender, TOMMessage firstInBatch, boolean noOp) {
        this.timestamp = timestamp;
        this.nonces = nonces;
        this.regency = regency;
        this.leader = leader;
        this.consensusId = consensusId;
        this.proof = proof;
        this.sender = sender;
        this.firstInBatch = firstInBatch;
        this.noOp = noOp;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the nonces
     */
    public byte[] getNonces() {
        return nonces;
    }

    /**
     * @return the consensusId
     */
    public int getConsensusId() {
        return consensusId;
    }
    
    /**
     * 
     * @return the leader with which the batch was decided
     */
    public int getLeader() {
        return leader;
    }
    /**
     * 
     * @return the proof for the consensus
     */
    public Set<ConsensusMessage> getProof() {
        return proof;
    }
    
    /**
     * @return the regency
     */
    public int getRegency() {
        return regency;
    }

    /**
     * @return the sender
     */
    public int getSender() {
        return sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(int sender) {
        this.sender = sender;
    }
    
    /**
     * @return the first message in the ordered batch
     */
    public TOMMessage getFirstInBatch() {
        return firstInBatch;
    }

    public void setLastInBatch() {
    	lastInBatch = true;
    }
    
    public boolean isLastInBatch() {
    	return lastInBatch;
    }

    public boolean isNoOp() {
        return noOp;
    }

}
