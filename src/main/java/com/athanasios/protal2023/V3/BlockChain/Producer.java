package com.athanasios.protal2023.V3.BlockChain;

import java.util.Random;

public class Producer implements Runnable {
    public Producer(NonceLine nonceLine,boolean terminationCondition) {
        this.nonceLine = nonceLine;
        this.terminationCondition = terminationCondition;
    }

    BlockChain bc = BlockChain.getInstance();
    private final NonceLine nonceLine;
    private volatile boolean terminationCondition;
    int increment =100000; //number of nonces for each thread

    // standard constructors



    public void run() {
        int nonce = 0;

        while(!bc.isTerminated() && !bc.isTerminated()){
            // Send the nonce to the product line
            nonceLine.send(nonce);

            // Increment nonce
            nonce = nonce + increment;

        }
        synchronized(nonceLine) {
            nonceLine.send(-1);
        }
        //System.out.println("Bye bye from "+Thread.currentThread().getName());
    }
}
