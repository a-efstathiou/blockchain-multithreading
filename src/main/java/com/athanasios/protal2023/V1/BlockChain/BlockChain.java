package com.athanasios.protal2023.V1.BlockChain;

import com.athanasios.protal2023.V1.db.Database;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockChain {

    private List<Block> blockChainList;
    public static final int prefix = 6;

    //We use a design pattern to create a unique instance of the BlockChain using
    //Singleton Lazy Initialization with on-demand holder
    private BlockChain(){
        blockChainList = new ArrayList<>();
    }
    private static class BlockChainHolder {
        static BlockChain blockChain = new BlockChain();
    }
    public static BlockChain getInstance() {
        return BlockChain.BlockChainHolder.blockChain;
    }

    //If a block from the database does not already exist in the blockchain it gets added.
    public void updateBlockChain(List<Block> blocks){
        List<Block> blockChain = getBlockChainList();

        for(Block block: blocks){
            if(!isBlockInBlockChain(block)){
                blockChain.add(block);
            }
        }
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
        if(getBlockChainList().isEmpty()){
            Block genesisBlock = new Block("0",data);
            genesisBlock.mineBlock(prefix);
            return genesisBlock;
        }
        else{
            Block block = new Block(chain.get(chain.size()-1).getHash(),data);
            block.mineBlock(prefix);
            return block;
        }
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

    private boolean isChainValid(List<Block> blockchain){
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[prefix]).replace('\0','0');
        for (int i=1;i<blockchain.size();i++){
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
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

    public boolean willBlockChainBeValid(Block block){
        //Checks whether the blockchain will be valid if the current created block is added.
        //The check is done before the addition to the database to ensure a valid blockchain in the database.

        List <Block> chain = new ArrayList<>(List.copyOf(getBlockChainList()));
        chain.add(block);
        return isChainValid(chain);

    }

}
