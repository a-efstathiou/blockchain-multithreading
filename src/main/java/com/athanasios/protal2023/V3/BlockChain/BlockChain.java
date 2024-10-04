package com.athanasios.protal2023.V3.BlockChain;

import com.athanasios.protal2023.V3.db.Database;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;


public class BlockChain {

    private List<Block> blockChainList;
    private static final int prefix = 6;
    private static final int availableCores = Runtime.getRuntime().availableProcessors(); //Available processors of the JVM
    private int consumerThreads = availableCores - 1;


    private Block generatedBlock; //the block after the mining is done
    private volatile boolean terminated = false; //boolean to track if we need to terminate the mining after a suitable hash has been found.

    //We use a design pattern to create a unique instance of the BlockChain using
    //Singleton Lazy Initialization with on-demand holder
    private BlockChain(){
        blockChainList = new ArrayList<>();
    }
    private static class BlockChainHolder {
        static BlockChain blockChain = new BlockChain();
    }
    public static BlockChain getInstance() {
        return BlockChainHolder.blockChain;
    }

    //If the block does not already exist in the blockchain it gets added.
    public void updateBlockChain(List<Block> blocks){
        List<Block> blockChain = getBlockChainList();

        for(Block block: blocks){
            if(!isBlockInBlockChain(block)){
                blockChain.add(block);
            }
        }
        //System.out.println("Is the BlockChain valid? "+isChainValid());
    }

    //Checks if the block exists in the blockchain
    public boolean isBlockInBlockChain(Block block){
        List<Block> blockChain = getBlockChainList();
        for(Block block1 : blockChain){
            if(     block.getHash().equals(block1.getHash()) &&
                    block.getData().equals(block1.getData()) &&
                    block.getNonce() == block1.getNonce() &&
                    block.getTimeStamp() == block1.getTimeStamp() &&
                    block.getPreviousHash().equals(block1.getPreviousHash())){
                return true;
            }
        }
        return false;
    }

    public Block createBlock(String data){
        resetTerminated(); //reset the termination condition back to false
        initializeConsumerThreads();
        Database db = Database.getInstance();
        db.updateBlockChain();
        List<Block> chain = getBlockChainList();

        NonceLine nonceLine = new NonceLine();
        Thread producer = new Thread(new Producer(nonceLine,isTerminated()),"Producer");
        producer.start();
        for (int i = 1; i <= consumerThreads; i++) {
            Thread consumer = new Thread(new Consumer(nonceLine, data, chain), "Consumer" + i);
            consumer.start();
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //System.out.println("I was notified!");
        return generatedBlock;
    }


    public boolean isChainValid(){
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[prefix]).replace('\0','0');
        for (int i=1;i<blockChainList.size();i++){
            currentBlock = blockChainList.get(i);
            previousBlock = blockChainList.get(i-1);
            if (!currentBlock.getHash().equals(currentBlock.calculateBlockHash())) {
                return false;
            }
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                return false;
            }
            if (!currentBlock.getHash().substring(0,prefix).equals(hashTarget)) {
                return false;
            }
        }
        return true;
    }

    public List<Block> getBlockChainList(){
        return blockChainList;
    }

    public void printBlockChain(){
        //Transform into Json
        List <Block> blockChain = getBlockChainList();
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
        System.out.println("The blockChain:");
        System.out.println(json);
    }

    public synchronized void setTerminated() {
        terminated = true;
        notify();
    }

    public synchronized void resetTerminated(){
        terminated = false;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public int getPrefix(){
        return prefix;
    }

    public synchronized void setGeneratedBlock(Block block){
        generatedBlock = block;
    }
    public void setConsumerThreads(int num){consumerThreads = num;}

    private void initializeConsumerThreads(){
        //Fastest computational time in 8 threads
        if(availableCores < 8){
            consumerThreads = availableCores - 1;
        }
        else{
            consumerThreads = 7;
        }
    }

}
