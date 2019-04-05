/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package noobchain;

/**
 *
 * @author esau
 */
public class TransactionInput {
    public String transactionOutputId; // reference to the TransactionOutputs -> transactionId
    public TransactionOutput UTXO; // contains unspent transaction output
    
    public TransactionInput(String transactionOutputId){
        this.transactionOutputId = transactionOutputId;
    }
    
}
