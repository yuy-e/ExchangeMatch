package Exchange.Matching.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Account extends XMLObject{
    private int id;
    private double balance;

    public Account(int id, double balance){
        this.id = id;
        this.balance = balance;
    }

    public int getID(){
        return this.id;
    }
    
    public double getBalance(){
        return this.balance;
    }

    @Override
    public Map<String,String> getAttribute(){
        Map<String,String> map=new LinkedHashMap<String,String>();
        map.put("id",Integer.toString(id));
        return map;
    }


}


