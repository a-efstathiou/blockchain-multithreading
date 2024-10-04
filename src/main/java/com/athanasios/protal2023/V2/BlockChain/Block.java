package com.athanasios.protal2023.V2.BlockChain;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;


public class Block {

    private String hash;
    private final String previousHash;
    private String data;
    private final long timeStamp;
    private int nonce;

    private AtomicBoolean found = new AtomicBoolean(false); //boolean to keep trap if we mined the hash


    public Block(String previousHash, String data, String hash,long timeStamp,int nonce) {
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = timeStamp;
        this.hash = hash;
        this.nonce = nonce;
    }

    public Block(String previousHash,String data){
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = new Date().getTime();
        this.hash = calculateBlockHash();
    }

    public String calculateBlockHash(){
        String dataToHash = previousHash+String.valueOf(timeStamp)
                +data+String.valueOf(nonce);
        MessageDigest digest = null;
        byte[] bytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes){
            builder.append(String.format("%02x",b));
        }
        return builder.toString();
    }
    public String calculateBlockHashAsync(int nonce){
        String dataToHash = previousHash+String.valueOf(timeStamp)
                +data+String.valueOf(nonce);
        MessageDigest digest = null;
        byte[] bytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes){
            builder.append(String.format("%02x",b));
        }
        return builder.toString();
    }

    public String mineBlockAsync(int prefix, int startNonce, int endNonce) {
        // prefix -> how many 0 the hash will start with
        String prefixString = new String(new char[prefix]).replace('\0', '0');
        int local_nonce=0;
        int i=startNonce;
        //System.out.println("Thread " + Thread.currentThread().getName() + " started mining from nonce "+startNonce);
        String local_hash = calculateBlockHash();
            while(i<endNonce){
                local_nonce = i;
                local_hash = calculateBlockHashAsync(local_nonce);
                //System.out.println("Hash: " + local_hash + ", Nonce: " + local_nonce + ", Thread: " + Thread.currentThread().getName());
                i++;
                if (local_hash.startsWith(prefixString)) {
                    setFound();
                    break;
                }
                if(getFound()){
                    break;
                }
            }

        if(getFound()){
            if (local_hash.startsWith(prefixString)) {
                //System.out.println("Thread " + Thread.currentThread().getName() + " found the hash: " + local_hash + " \nwith nonce " + local_nonce);
                nonce =local_nonce;
                hash = local_hash;
                return hash;
            }
            else{
                return null;
            }

        }
        else{
            return null;
        }
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getData() {
        return data;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

    public boolean getFound(){
        return found.get();
    }
    public  void setFound(){
        found.set(true);
    }


}
