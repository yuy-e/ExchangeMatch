package Exchange.Matching.server;

import java.util.LinkedHashMap;
import java.util.Map;

public class Position extends XMLObject {
    private String symbol;
    private double amount;
    private int account_id;
    
    public Position(String symbol,double sym_amount,int account_id){
        this.symbol=symbol;
        this.amount=sym_amount;
        this.account_id=account_id;
    }

    public String getSym(){
        return this.symbol;
    }

    public double getAmount(){
        return this.amount;
    }

    public int getAccountID(){
        return this.account_id;
    }


    @Override
    public Map<String,String> getAttribute(){
        Map<String,String> map=new LinkedHashMap<String,String>();
        map.put("sym",symbol);
        map.put("id",Integer.toString(account_id));
        return map;
    }

}
