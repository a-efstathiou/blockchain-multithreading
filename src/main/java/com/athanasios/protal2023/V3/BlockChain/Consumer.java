package com.athanasios.protal2023.V3.BlockChain;

import java.util.List;

public class Consumer implements Runnable {
    private final NonceLine nonceLine;
    private final String data;
    private final List<Block> chain;
    BlockChain bc = BlockChain.getInstance();
    int prefix = bc.getPrefix();


    public Consumer(NonceLine nonceLine, String data,List<Block> chain) {
        this.data = data;
        this.nonceLine = nonceLine;
        this.chain = chain;
    }

    public void run() {
        for(int receivedNonce = nonceLine.receive();
            receivedNonce != -1;
            receivedNonce = nonceLine.receive()) {


            int i_end = receivedNonce + 100000;

            if(bc.getBlockChainList().isEmpty()){
                Block genesisBlock = new Block("0",data);
                String hash = genesisBlock.mineBlockAsync(prefix, receivedNonce, i_end);
                if(hash != null) {
                    synchronized (this) {
                        bc.setGeneratedBlock(genesisBlock);
                        bc.setTerminated();
                    }
                }

            }
            else{
                Block block = new Block(chain.get(chain.size()-1).getHash(),data);
                String hash = block.mineBlockAsync(prefix, receivedNonce, i_end);
                if(hash != null) {
                    synchronized (this) {
                        bc.setGeneratedBlock(block);
                        bc.setTerminated();
                    }

                }
            }

        }
        //System.out.println("Bye bye from "+Thread.currentThread().getName());
    }
}
