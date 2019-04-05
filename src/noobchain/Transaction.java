/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package noobchain;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

/**
 *
 * @author esau
 */
public class Transaction {

    public String transactionId; // this is also the hash of the transaction
    public PublicKey sender; // sender's address/public key
    public PublicKey recipient; // recipients address/public key
    public float value;
    public byte[] signature; // this is to prevent anybody else from spending funds in your wallet

    public ArrayList<TransactionInput> inputs = new ArrayList<>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // a rough count of how many transactions have been generated

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // this calculates the transaction hash (which will be used as its id)
    private String calculateHash() {
        sequence++; // increases the sequence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender)
                + StringUtil.getStringFromKey(recipient)
                + Float.toString(value) + sequence
        );
    }

    // signs all the data we dont wish to be tampered with
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // verifies the data we signed hasnt been tempered with
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    // returns true if new transaction could be created
    public boolean processTransaction() {
        if (verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // gather transaction inputs (Make sure they are unspent):
        for (TransactionInput i : inputs) {
            i.UTXO = NoobChain.UTXOs.get(i.transactionOutputId);
        }

        // check if transaction is valid
        if (getInputsValue() < NoobChain.minimumTransaction) {
            System.out.println("#Transaction Inputs too small: " + getInputsValue());
            return false;
        }

        // generate transaction outputs
        float leftOver = getInputsValue() - value; // get value of inputs then the left over change
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); //send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); //send the left over 'change' back to sender		
        
        // add outputs to Unpent list
        for(TransactionOutput o : outputs){
            NoobChain.UTXOs.put(o.id, o);
        }
        
        // remove tranasction inputs from UTXO list as spent
        for(TransactionInput i : inputs){
            if(i.UTXO == null){
                continue; // if transaction cannt be found skip it
            }
            NoobChain.UTXOs.remove(i.UTXO.id);
        }
        
        return true;
    }
    
    // returns sum of inputs (UTXOs) values
    public float getInputsValue(){
        float total = 0;
        for(TransactionInput i : inputs){
            if(i.UTXO == null){
                continue; // if transaction cant be found skip it
            }
            total += i.UTXO.value;            
        }
        return total;
    }
    
    // returns sum of outputs
    public float getOutputsValue(){
       float total = 0;
       for(TransactionOutput o : outputs){
           total += o.value;
       }
       return total;
    }

}
