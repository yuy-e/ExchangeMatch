package Exchange.Matching.server;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.ErrorManager;

public class Order extends XMLObject {
    private int order_id;
    private int account_id;
    private String symbol;
    private double amount;
    private double limit;
    private String status;
    private String type;
    private long time;

    public Order() {
        this.time = Instant.now().getEpochSecond();
    }

    // For create new Order
    public Order(int account_id, String symbol, double amount, double limit) {
        this.account_id = account_id;
        this.symbol = symbol;
        if (amount >= 0) {
            this.type = "buy";
        } else {
            this.type = "sell";
        }
        this.amount = Math.abs(amount);
        this.limit = limit;
        this.status = "open"; // open/executed/cancel
        this.time = Instant.now().getEpochSecond();
    }

    // For Mapping SQL
    public Order(int order_id, int account_id, String symbol, double amount, double limit, String status, String type,
            long time) {
        this.order_id = order_id;
        this.account_id = account_id;
        this.symbol = symbol;
        this.amount = Math.abs(amount);
        this.limit = limit;
        this.status = status;
        this.type = type;
        this.time = time;
    }

    // For separating Order
    public Order(int account_id, String symbol, double amount, double limit, String type, long time) {
        this.account_id = account_id;
        this.symbol = symbol;
        this.amount = Math.abs(amount);
        this.limit = limit;
        this.status = "open";
        this.type = type;
        this.time = time;
    }

    // For Order response
    public Order(int account_id) {
        this.account_id = account_id;
    }

    // Get from Database
    public void setOrderID(int id) {
        this.order_id = id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAccountID() {
        return account_id;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getAmount() {
        return amount;
    }

    public double getLimit() {
        return limit;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public int getOrderID() {
        return order_id;
    }

    @Override
    public Map<String, String> getAttribute() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("sym", symbol);
        if (type.equals("buy")) {
            map.put("amount", Double.toString(amount));
        }
        else{
            map.put("amount", Double.toString(-1*amount));
        }
        
        map.put("limit", Double.toString(limit));
        if (errorMessage == null) {
            map.put("id", Integer.toString(order_id));
        }
        return map;
    }
}
