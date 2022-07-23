package Exchange.Matching.server;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Spring;

public class TransactionId extends XMLObject {
    private int account_id;
    private int transactionId;
    private double shares;
    private double price;
    private long time;
    private String status;

    public TransactionId(int account_id,int transaction_id){
        this.account_id=account_id;
        this.transactionId=transaction_id;
        this.shares=0;
        this.price=0;
        this.time=0;
        this.status="";
    }

    public TransactionId(int account_id,int transaction_id,double shares,double price,long time,String status){
        this.account_id=account_id;
        this.transactionId=transaction_id;
        this.shares=shares;
        this.price=price;
        this.time=time;
        this.status=status;
        
    }

    public int getAccountId(){
        return account_id;
    }

    public int getTransactionId(){
        return transactionId;
    }

    public String getStatus(){
        return status;
    }

    public void updateOrder(double shares,double price,long time,String status){
        this.shares=shares;
        this.price=price;
        this.time=time;
        this.status=status;
    }

    @Override
    public Map<String, String> getAttribute() {
        Map<String,String> map=new LinkedHashMap<String,String>();
        map.put("id", Integer.toString(transactionId));
        return map;
    }

    public Map<String, String> getChild(){
        Map<String,String> map=new LinkedHashMap<String,String>();
        map.put("shares",Double.toString(shares));
        if (status.equals("executed")){
            map.put("price", Double.toString(price));
        }
        if (status.equals("canceled")||status.equals("executed")){
            map.put("time",Long.toString(time));
        }

        return map;
    }
    
}
