package Exchange.Matching.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class Matching {

    private Order order;
    private ArrayList<Order> order_list;
    private ArrayList<ExecuteOrder> execute_list; 
    private ArrayList<Order> sell_order_list; 

    public Matching(){
        this.order = new Order();
        this.order_list = new ArrayList<Order>();
        this.execute_list = new ArrayList<ExecuteOrder>();
        this.sell_order_list = new ArrayList<Order>();
    }

    public Matching(Order order, ResultSet res) throws SQLException{
        this.order = order;
        // 1. Mapping ResultSet
        this.order_list = mapOrder(res);
        // Store the Execute info of buy orders
        this.execute_list = new ArrayList<ExecuteOrder>();
        // Store the executed sell orders
        this.sell_order_list = new ArrayList<Order>();
        matchOrder();
    }

    public ArrayList<ExecuteOrder> getExecuteList(){
        return execute_list;
    }
    public ArrayList<Order> getSellList(){
        return sell_order_list;
    }
    public Order getOrder(){
        return order;
    }

    /**
     * Map query results to a list of order
     * 
     * @param res ResultSet
     * @return order list
     * @throws SQLException
     */
    public ArrayList<Order> mapOrder(ResultSet res) throws SQLException {
        ArrayList<Order> order_list = new ArrayList<Order>();
        while (res.next()) {
            int order_id = res.getInt("ORDER_ID");
            int account_id = res.getInt("ACCOUNT_ID");
            String symbol = res.getString("SYMBOL");
            double amount = res.getDouble("AMOUNT");
            double limit = res.getDouble("BOUND");
            String status = res.getString("STATUS");
            String type = res.getString("TYPE");
            Long time = res.getLong("TIME");
            Order order = new Order(order_id,account_id, symbol, amount, limit, status, type, time);
            order_list.add(order);
        }
        return order_list;
    }

    /**
     * Map query results to a list of ExecuteOrder
     * 
     * @param res ResultSet
     * @return ExecuteOrder List
     * @throws SQLException
     */
    public ArrayList<ExecuteOrder> mapExecuteOrder(ResultSet res) throws SQLException {
        ArrayList<ExecuteOrder> e_list = new ArrayList<ExecuteOrder>();
        while (res.next()) {
            int bid = res.getInt("BUYER_ID");
            int sid = res.getInt("SELLER_ID");
            int b_trans_id = res.getInt("BUYER_TRANS_ID");
            int s_trans_id = res.getInt("SELLER_TRANS_ID");
            String symbol = res.getString("SYMBOL");
            double amount = res.getDouble("AMOUNT");
            double limit = res.getDouble("PRICE");
            long time = res.getLong("TIME");
            ExecuteOrder order = new ExecuteOrder(bid, sid, b_trans_id, s_trans_id, symbol, amount, limit, time);
            e_list.add(order);
        }
        return e_list;
    }


    // 2. Find matching Orders
    /**
     *  Match Orders according to specific rules and types
     */
    public void matchOrder() {
        double buy_amount = order.getAmount();
        for (Order or : order_list) {
            System.out.println("Info of the Order - order trans ID" + or.getOrderID());
            double sell_amount = or.getAmount();
            double price = 0;
            if (buy_amount > 0) {
                if (buy_amount == sell_amount) {
                    // compare time
                    if (order.getTime() < or.getTime()) {
                        price = order.getLimit();
                    } else {
                        price = or.getLimit();
                    }
                    ExecuteOrder eorder = new ExecuteOrder();
                    if(order.getType().equals("buy")){
                        eorder = new ExecuteOrder(order.getAccountID(), or.getAccountID(), order.getOrderID(),
                        or.getOrderID(), or.getSymbol(),
                        buy_amount, price);
                    }
                    else if(order.getType().equals("sell")){
                        eorder = new ExecuteOrder(or.getAccountID(), order.getAccountID(), or.getOrderID(),
                        order.getOrderID(), order.getSymbol(),
                        buy_amount, price);
                    }
                    execute_list.add(eorder);
                    buy_amount = 0;
                    sell_amount = 0;
                    or.setAmount(sell_amount);
                    order.setAmount(buy_amount);
                    sell_order_list.add(or);
                    break;
                } else if (buy_amount > sell_amount) {
                    // compare time
                    if (order.getTime() < or.getTime()) {
                        price = order.getLimit();
                    } else {
                        price = or.getLimit();
                    }
                    ExecuteOrder eorder = new ExecuteOrder();
                    if(order.getType().equals("buy")){
                        eorder = new ExecuteOrder(order.getAccountID(), or.getAccountID(), order.getOrderID(),
                        or.getOrderID(), or.getSymbol(),
                        sell_amount, price);
                    }
                    else if(order.getType().equals("sell")){
                        eorder = new ExecuteOrder(or.getAccountID(), order.getAccountID(), or.getOrderID(),
                        order.getOrderID(), order.getSymbol(),
                        sell_amount, price);
                    }
                    execute_list.add(eorder);
                    buy_amount -= sell_amount;
                    sell_amount = 0;
                    order.setAmount(buy_amount);
                    or.setAmount(sell_amount);
                    sell_order_list.add(or);
                } else {
                    // compare time
                    if (order.getTime() < or.getTime()) {
                        price = order.getLimit();
                    } else {
                        price = or.getLimit();
                    }
                    ExecuteOrder eorder = new ExecuteOrder();
                    if(order.getType().equals("buy")){
                        eorder = new ExecuteOrder(order.getAccountID(), or.getAccountID(), order.getOrderID(),
                        or.getOrderID(), or.getSymbol(),
                        buy_amount, price);
                    }
                    else if(order.getType().equals("sell")){
                        eorder = new ExecuteOrder(or.getAccountID(), order.getAccountID(), or.getOrderID(),
                        order.getOrderID(), order.getSymbol(),
                        buy_amount, price);
                    }
                    System.out.println("Exe Order: " + order.getAccountID() + or.getAccountID()+ order.getOrderID()+or.getOrderID()+or.getSymbol()+buy_amount+ price);
                    execute_list.add(eorder);
                    sell_amount -= buy_amount;
                    buy_amount = 0;
                    order.setAmount(buy_amount);
                    or.setAmount(sell_amount);
                    // update remaining Sell Order
                    sell_order_list.add(or);
                }
            }
        }
        // Remaining buy order
    }
}
