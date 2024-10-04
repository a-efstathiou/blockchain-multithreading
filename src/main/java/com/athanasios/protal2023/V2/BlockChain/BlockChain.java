package com.athanasios.protal2023.V2.BlockChain;

import com.athanasios.protal2023.V2.db.Database;
import com.google.gson.GsonBuilder;
import org.openjdk.jmh.annotations.Mode;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockChain {

    private List<Block> blockChainList;
    private static final int prefix = 6;
    private static final int availableCores = Runtime.getRuntime().availableProcessors(); //Available processors of the JVM
    private int numOfThreads = availableCores - 2;

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
        System.out.println("Is the BlockChain valid? "+isChainValid());
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

        Database db = Database.getInstance();
        db.updateBlockChain();
        List<Block> chain = getBlockChainList();
        Block generatedBlock = null;
        String hash = null;
        AtomicInteger currentNonce = new AtomicInteger(0);

        int increment =100000; //number of nonces for each thread
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads); //Adjust Thread numbers
        Semaphore semaphore1 = new Semaphore(numOfThreads); // Allow only 'numOfThreads' to run concurrently
        List<Future<String>> futures = new ArrayList<>(); //save all the hashvalues and get only the one that is not null


        try{
            if(getBlockChainList().isEmpty()){
                Block genesisBlock = new Block("0",data);
                do {
                    semaphore1.acquire();
                    int i_start = currentNonce.getAndAdd(increment);
                    int i_end = i_start + increment;

                    Callable<String> callable = () -> {
                        try {
                            //System.out.println("i_start: "+i_start+" , i_end: "+i_end);
                            return genesisBlock.mineBlockAsync(prefix, i_start, i_end);
                        }
                        finally{
                            semaphore1.release();
                        }
                    };
                    Future<String> future = executorService.submit(callable);
                    futures.add(future);

                } while (!genesisBlock.getFound());

                for(Future<String> future : futures){
                    if(future.get() != null) {
                        generatedBlock = genesisBlock;
                        break;
                    }
                }

            }
            else{
                Block block = new Block(chain.get(chain.size()-1).getHash(),data);
                do {
                    semaphore1.acquire();
                    int i_start = currentNonce.getAndAdd(increment);
                    int i_end = i_start + increment;
                    Callable<String> callable = () -> {
                        try {
                            //System.out.println("i_start: "+i_start+" , i_end: "+i_end);
                            return block.mineBlockAsync(prefix, i_start, i_end);
                        }
                        finally{
                            semaphore1.release();
                        }
                    };

                    Future<String> future = executorService.submit(callable);
                    futures.add(future);

                } while (!block.getFound());

                for(Future<String> future : futures){
                    if(future.get() != null) {
                        generatedBlock = block;
                        break;
                    }
                }



            }

        } catch (InterruptedException | ExecutionException e) {
            e.getStackTrace();
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.MINUTES);
            }
            catch(InterruptedException e){
                e.getStackTrace();
            }
            System.out.println("Are all tasks completed after termination? : "+executorService.isTerminated());

        }
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
                System.out.println("blockchain not valid because of 1 for i: "+i);
                return false;
            }
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("blockchain not valid because of 2 for i: "+i);
                return false;
            }
            if (!currentBlock.getHash().substring(0,prefix).equals(hashTarget)) {
                System.out.println("blockchain not valid because of 3 for i: "+i);
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

    public int getNumOfThreads(){
        return numOfThreads;
    }

    public void setNumOfThreads(int num){
        numOfThreads = num;
    }

}
