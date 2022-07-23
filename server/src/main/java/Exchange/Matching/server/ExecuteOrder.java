package Exchange.Matching.server;

import java.util.Formatter.BigDecimalLayoutForm;

import java.time.Instant;

public class ExecuteOrder {
    private int buyer_id;
    private int seller_id;
    private int buyer_trans_id;
    private int seller_trans_id;
    private String symbol;
    private double amount;
    private double price;
    private long time;

    public ExecuteOrder(){}

    public ExecuteOrder(int bid, int sid, int buyer_trans_id, int seller_trans_id, String symbol, double amount, double price){
        this.buyer_id = bid;
        this.seller_id = sid;
        this.buyer_trans_id = buyer_trans_id;
        this.seller_trans_id = seller_trans_id;
        this.symbol = symbol;
        this.amount = amount;
        this.price = price;
        this.time = Instant.now().getEpochSecond();
    }

    public ExecuteOrder(int bid, int sid, int buyer_trans_id, int seller_trans_id, String symbol, double amount, double price, long time){
        this.buyer_id = bid;
        this.seller_id = sid;
        this.buyer_trans_id = buyer_trans_id;
        this.seller_trans_id = seller_trans_id;
        this.symbol = symbol;
        this.amount = amount;
        this.price = price;
        this.time = time;
    }

    public int getBuyerID(){
        return buyer_id;
    }

    public int getSellerID(){
        return seller_id;
    }

    public int getBuyerOrderID(){
        return buyer_trans_id;
    }

    public int getSellerOrderID(){
        return seller_trans_id;
    }

    public String getSymbol(){
        return symbol;
    }

    public double getAmount(){
        return amount;
    }

    public double getPrice(){
        return price;
    }

    public long getTime(){
        return time;
    }

}
