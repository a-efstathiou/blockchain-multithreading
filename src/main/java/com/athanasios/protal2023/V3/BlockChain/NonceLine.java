package com.athanasios.protal2023.V3.BlockChain;

public class NonceLine {
    private int nonce;
    //nonce :: starting nonce for every mining
    // True if receiver should wait
    // False if sender should wait
    private boolean waitForTransfering = true;
    BlockChain bc = BlockChain.getInstance();
    public synchronized int receive() {
        //-3 : work was cancelled
        //-1 : termination condition
        while (waitForTransfering && !bc.isTerminated()) {
            try {
                //System.out.println(Thread.currentThread().getName()+" is waiting");
                wait();
            } catch (InterruptedException e) {
                System.err.println("Thread work cancelled");
                return -3;
            }
        }
        waitForTransfering = true;
        int returnedNonce = nonce;
        notifyAll();
        //System.out.println(Thread.currentThread().getName()+" consumed nonce "+nonce);
        return returnedNonce;
    }
    public synchronized void send(int nonce) {
        while (!waitForTransfering) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Thread work cancelled");
                return;
            }
        }
        if(nonce == -1 ){
            bc.setTerminated();
        }
        this.nonce = nonce;
        waitForTransfering = false;

        //System.out.println("Produced nonce "+nonce);
        notifyAll();
    }
}

