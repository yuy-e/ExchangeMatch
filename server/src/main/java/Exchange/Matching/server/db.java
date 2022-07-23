package Exchange.Matching.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class db {
    private Connection connection;
    private int reponse_trans_id;

    public db() throws SQLException {
        this.connection = buildDBConnection();
        connection.setAutoCommit(false);
        deleteTables();
        buildTables();
    }

    /**
     * Build connection to DB
     * 
     * @return Connection
     */
    public Connection buildDBConnection() {
        String url = "jdbc:postgresql://localhost:5432/stockDB";
        String username = "postgres";
        String password = "postgres";
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            //System.out.println("Successfully connected to DataBase.");
            return connection;
        } catch (Exception e) {
            //System.out.print(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete Tables in DB when initializing
     * 
     */
    private synchronized void deleteTables() throws SQLException {
        Statement st = connection.createStatement();
        String sql_delete = "DROP TABLE IF EXISTS SYM, POSITION, ACCOUNT, ORDER_ALL, ORDER_EXECUTE;";
        st.executeUpdate(sql_delete);
        st.close();
        connection.commit();
    }

    /**
     * Build Tables
     * 
     * @throws SQLException
     */
    private synchronized void buildTables() throws SQLException {
        Statement st = connection.createStatement();

        String sql_sym = "CREATE TABLE SYM(" +
                "SYMBOL_ID SERIAL PRIMARY KEY," +
                "SYMBOL VARCHAR);";

        String sql_position = "CREATE TABLE POSITION(" +
                "POSITION_ID SERIAL PRIMARY KEY," +
                "ACCOUNT_ID INT," +
                "SYMBOL VARCHAR," +
                "AMOUNT FLOAT CHECK (AMOUNT >= 0)," +
                "CONSTRAINT POSITION_FK FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ACCOUNT_ID) ON DELETE SET NULL ON UPDATE CASCADE);";

        String sql_account = "CREATE TABLE ACCOUNT(" + "ACCOUNT_ID INT PRIMARY KEY," + "BALANCE FLOAT CHECK(BALANCE>=0));";

        String sql_order = "CREATE TABLE ORDER_ALL(" +
                "ORDER_ID SERIAL PRIMARY KEY," +
                "ACCOUNT_ID INT," +
                "SYMBOL VARCHAR," +
                "AMOUNT FLOAT," +
                "BOUND FLOAT CHECK(BOUND >= 0)," +
                "STATUS VARCHAR," +
                "TYPE VARCHAR," +
                "TIME BIGINT," +
                //"constraint symbol_fk foreign key(symbol) references sym(symbol) on delete set null on update cascade," + 
                "CONSTRAINT ACCOUNT_FK FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ACCOUNT_ID) ON DELETE SET NULL ON UPDATE CASCADE);";

        // Store trades info
        String sql_exectute_order = "CREATE TABLE ORDER_EXECUTE(" +
                "ORDER_ID SERIAL PRIMARY KEY," +
                "BUYER_ID INT," +
                "SELLER_ID INT," +
                "BUYER_TRANS_ID INT," +
                "SELLER_TRANS_ID INT," +
                "SYMBOL VARCHAR," +
                "AMOUNT FLOAT," +
                "PRICE FLOAT," +
                "TIME BIGINT," +
                //"constraint symbol_fk foreign key(symbol) references sym(symbol) on delete set null on update cascade," + 
                "CONSTRAINT BUYER_FK FOREIGN KEY (BUYER_ID) REFERENCES ACCOUNT(ACCOUNT_ID) ON DELETE SET NULL ON UPDATE CASCADE,"
                +
                "CONSTRAINT SELLER_FK FOREIGN KEY (SELLER_ID) REFERENCES ACCOUNT(ACCOUNT_ID) ON DELETE SET NULL ON UPDATE CASCADE);";

        st.executeUpdate(sql_sym);
        st.executeUpdate(sql_account);
        st.executeUpdate(sql_position);
        st.executeUpdate(sql_order);
        st.executeUpdate(sql_exectute_order);
        st.close();
        connection.commit();
    }

    /**
     * Close connection to DB
     * 
     * @throws SQLException
     */
    private synchronized void closeConnection() throws SQLException {
        connection.close();
    }


    /**
     * Insert Date into DB according to Object type
     * 
     * @param obj Object
     * @throws SQLException
     */
    public void insertData(Object obj) throws SQLException{
        if (obj instanceof Symbol) {
            Symbol temp = (Symbol) obj;
            Statement st = connection.createStatement();
            String sql = "INSERT INTO SYM (SYMBOL) VALUES('" + temp.getSym() + "');";
            st.executeUpdate(sql);
            // st.close();
            connection.commit();
        }
        if (obj instanceof Account) {
            Account temp = (Account) obj;
            Statement st = connection.createStatement();
            String sql = "INSERT INTO ACCOUNT (ACCOUNT_ID, BALANCE) VALUES(" + temp.getID() + ", " + temp.getBalance()
                    + ");";
            st.executeUpdate(sql);
            // st.close();
            connection.commit();
        }
        if (obj instanceof Position) {
            Position temp = (Position) obj;
            Statement st = connection.createStatement();
            String sql = "insert into position(account_id, symbol, amount) values(" + temp.getAccountID() + ", '"
                    + temp.getSym() + "', " + temp.getAmount() + ");";
            st.executeUpdate(sql);
            // st.close();
            connection.commit();
        }
        if (obj instanceof Order) {
            Order temp = (Order) obj;
            Statement st = connection.createStatement();
            String sql = "insert into order_all(account_id, symbol, amount, bound, status, type, time) values("
                    + temp.getAccountID() + ", '" + temp.getSymbol() + "', " + temp.getAmount() + ", " + temp.getLimit()
                    + ", '" + temp.getStatus() + "', '" + temp.getType() + "', " +  temp.getTime() + ");";
            st.executeUpdate(sql);
            String getTransactionID_sql = "select lastval();";
            ResultSet res = st.executeQuery(getTransactionID_sql);
            //trans_id : For reponse Order
            int trans_id = -1;
            if(res.next()){
                trans_id = res.getInt("lastval");
            }
            //System.out.println("The transaction id is :" + trans_id);
            // st.close();
            this.reponse_trans_id = trans_id;
            connection.commit();
        }
        if (obj instanceof ExecuteOrder) {
            ExecuteOrder temp = (ExecuteOrder) obj;
            Statement st = connection.createStatement();
            String sql = "insert into order_execute(buyer_id, seller_id, buyer_trans_id, seller_trans_id, symbol, amount, price, time) values("
                    + temp.getBuyerID() + ", " + temp.getSellerID() + ", " + temp.getBuyerOrderID() + ", "
                    + temp.getSellerOrderID() + ", '" + temp.getSymbol() + "', " + temp.getAmount() + ", "
                    + temp.getPrice()
                    + ", " + temp.getTime() + ");";
            st.executeUpdate(sql);
            // st.close();
            connection.commit();
        }
    }

    /**
     * Search for specific data in tables. For buy & sell orders, also do price
     * matching & balance changement.
     * 
     * @param obj
     * @return query results as ResultSet
     * @throws SQLException
     */
    public synchronized ResultSet search(Object obj) throws SQLException {
        ResultSet res = null;
        if (obj instanceof Symbol) {
            Symbol temp = (Symbol) obj;
            Statement st = connection.createStatement();
            String sql = "select * from sym where symbol = '" + temp.getSym() + "';";
            res = st.executeQuery(sql);
            // st.close();
            connection.commit();
            return res;
        } else if (obj instanceof Account) {
            Account temp = (Account) obj;
            Statement st = connection.createStatement();
            String sql = "select * from account where account_id = " + temp.getID() + ";";
            res = st.executeQuery(sql);
            // st.close();
            connection.commit();
            return res;
        } else if (obj instanceof Position) {
            Position temp = (Position) obj;
            Statement st = connection.createStatement();
            String sql = "select * from position where account_id = " + temp.getAccountID() + " and symbol = '" + temp.getSym() + "';";
            res = st.executeQuery(sql);
            // st.close();
            connection.commit();
            return res;
        } else if (obj instanceof TransactionId) {
            // query Orders
            TransactionId transactionId = (TransactionId)obj;
            int account_id = transactionId.getAccountId();
            int temp = transactionId.getTransactionId();
            Statement st = connection.createStatement();
            String sql = "select * from order_all where order_id = " + temp + " and account_id = " + account_id + ";";
            res = st.executeQuery(sql);
            // String sql_execute = "select * from order_execute where "
            // st.close();
            connection.commit();
            return res;
        } else if (obj instanceof Order) {
            Order temp = (Order) obj;
            Statement st = connection.createStatement();
            String sql = "";
            // Handle Buy Orders
            if (temp.getType().equals("buy")) {
                sql = "select * from order_all where symbol = '"
                        + temp.getSymbol() + "' and bound <= " + temp.getLimit()
                        + " and status = 'open' and type = 'sell' and account_id !="+ temp.getAccountID() +" order by bound asc, time asc for update;";
                //System.out.println(sql);
            } else if(temp.getType().equals("sell")){
                sql = "select * from order_all where symbol = '"
                        + temp.getSymbol() + "' and bound >= " + temp.getLimit() 
                        + " and status = 'open' and type = 'buy'  and account_id !="+ temp.getAccountID() +" order by bound desc, time asc for update;";
                //System.out.println(sql);
            }
            res = st.executeQuery(sql);
            // Order Matching
            Matching matching = new Matching(temp, res);
            // Fill Order_Execute Table
            ArrayList<ExecuteOrder> execute_list = matching.getExecuteList();
            for (ExecuteOrder eorder : execute_list) {
                insertData(eorder);
                double balance_change = 0.0;
                double seller_balance_change = 0.0;
                if (temp.getType().equals("buy")) {
                    double origin_buyer_price = temp.getLimit();
                    balance_change = eorder.getAmount() * (origin_buyer_price-eorder.getPrice());
                    seller_balance_change = eorder.getAmount() * eorder.getPrice();
                    //System.out.println("----------------");
                    //System.out.println("The change balance of Buyer is : " + balance_change);
                    //System.out.println("The change balance of Seller is : " + seller_balance_change);

                    // update balance of Buyer & Seller
                    //System.out.println("Info of E Order: " + eorder.getBuyerID() + eorder.getSellerID()+ eorder.getBuyerOrderID()+eorder.getSellerOrderID()+eorder.getSymbol()+eorder.getAmount()+ eorder.getPrice());
                    
                    Account buyer_account_temp = new Account(eorder.getBuyerID(), balance_change);
                    Account seller_account_temp = new Account(eorder.getSellerID(), seller_balance_change);
                    updateData(buyer_account_temp);
                    updateData(seller_account_temp);

                    // update buyer position
                    Position buyer_position = new Position(eorder.getSymbol(), eorder.getAmount(), eorder.getBuyerID());
                    //System.out.println("Update position:" + eorder.getSymbol() + eorder.getAmount() +  eorder.getBuyerID() );
                    updateData(buyer_position);

                    // seller position already updated when first came in.
                }
                // Handle Sell Order
                else if(temp.getType().equals("sell")){
                    String sql_help = "select * from order_all where order_id =" + eorder.getBuyerOrderID() + ";";
                    res = st.executeQuery(sql_help);
                    Double buyer_price = 0.0;
                    if(res.next()){
                        buyer_price = res.getDouble("BOUND");
                        //System.out.println("The original buyer price is: --" + buyer_price);
                    }
                    double original_buyer_price = buyer_price;

                    balance_change = eorder.getAmount() * eorder.getPrice(); //seller
                    seller_balance_change = eorder.getAmount() * (original_buyer_price-eorder.getPrice()); //buyer
                    //System.out.println("----------------");
                    //System.out.println("The change balance of Seller is : " + balance_change);
                    //System.out.println("The change balance of Buyer is : " + seller_balance_change);

                    // update balance of Buyer & Seller
                    //System.out.println("Info of E Order: " + eorder.getBuyerID() + eorder.getSellerID()+ eorder.getBuyerOrderID()+eorder.getSellerOrderID()+eorder.getSymbol()+eorder.getAmount()+ eorder.getPrice());

                    Account buyer_account_temp = new Account(eorder.getBuyerID(), seller_balance_change); //buyer
                    Account seller_account_temp = new Account(eorder.getSellerID(), balance_change); //seller
                    updateData(buyer_account_temp);
                    updateData(seller_account_temp);

                    // update position
                    Position buyer_position = new Position(eorder.getSymbol(), eorder.getAmount(), eorder.getBuyerID());
                    //System.out.println("Update position:" + eorder.getSymbol() + eorder.getAmount() +  eorder.getBuyerID() );
                    updateData(buyer_position);

                }
            }
            // Update Order in Order_all Table : remain part
            Order new_order = matching.getOrder();
            updateData(new_order);
            
            ArrayList<Order> sell_order_list = matching.getSellList();
            for (Order sorder : sell_order_list) {
                updateData(sorder);
            }
            // st.close();
            connection.commit();
            return res;
        }
        return res;
    }


    /**
     * search orders and map ResultSet to ArrayList<Order>
     * 
     * @param transaction_id transaction id
     * @return queried Order List
     * @throws SQLException
     */
    public synchronized ArrayList<Order> searchOrder(TransactionId transaction_id) throws SQLException {
        ArrayList<Order> query_order_list = new ArrayList<Order>();
        ResultSet res = search(transaction_id);
        Matching matching = new Matching();
        query_order_list = matching.mapOrder(res);
        return query_order_list;
    }

    /**
     * search orders and map ResultSet to ArrayList<ExecuteOrder>
     * 
     * @param transaction_id transaction id
     * @param type order type
     * @return queried executeorder list
     * @throws SQLException
     */
    public synchronized ArrayList<ExecuteOrder> searchExecuteOrder(int transaction_id, String type) throws SQLException {
        ArrayList<ExecuteOrder> query_execute_order = new ArrayList<ExecuteOrder>();
        Statement st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String sql = "";
        if (type.equals("buy")) {
            sql = "select * from order_execute where buyer_trans_id = " + transaction_id + ";";  
        } else if (type.equals("sell")) {
            sql = "select * from order_execute where seller_trans_id = " + transaction_id + ";";
        }
        ResultSet res = st.executeQuery(sql);
        if(!res.next()){
            return null;
        }
        else{
            res.previous();
            // st.close();
            Matching matching = new Matching();
            query_execute_order = matching.mapExecuteOrder(res);
            connection.commit();
            return query_execute_order;
        }
    }

    /**
     * Updata Data in tables according to object type
     * 
     * @param obj 
     * @throws SQLException
     */
    public synchronized void updateData(Object obj) throws SQLException {
        // update order amount
        if (obj instanceof Order) {
            Order temp = (Order) obj;
            Statement st = connection.createStatement();
            String sql = "update order_all set amount = " + temp.getAmount() + " where order_id = " + temp.getOrderID()
                    + ";";
            st.executeUpdate(sql);
            //st.close();
            //connection.commit();
        }
        // update balance
        else if (obj instanceof Account) {
            Account temp = (Account) obj;
            Statement st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            //System.out.println("The ID of the account is: " + temp.getID());
            String sql = "select * from account where account_id = " + temp.getID() + " for update;";
            ResultSet res = st.executeQuery(sql);
            double balance = 0.0;
            if(res.next()){
                //res.previous();
                balance = res.getDouble("BALANCE");
            }
            //double balance = 0.0;
            //System.out.println("The original balance is --"+ balance);
            double new_balance = balance + temp.getBalance();
            String sql_update = "update account set balance = " + new_balance + " where account_id = " + temp.getID()
                    + ";";
            st.executeUpdate(sql_update);
            // st.close();
            connection.commit();
        }
        // update position
        else if(obj instanceof Position){
            Position temp = (Position) obj;
            Statement st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = "select * from position where symbol = '" + temp.getSym() +"' and account_id = " + temp.getAccountID() + " for update;";
            ResultSet res = st.executeQuery(sql);
            double amount = 0.0;
            if(res.next()){
                //res.previous();
                amount = res.getDouble("amount");
                //System.out.println("'Increased amount" + amount);
            }
            double new_amount = amount + temp.getAmount();
            String sql_update = "update position set amount = " + new_amount + " where symbol = '" + temp.getSym() +"' and account_id = " + temp.getAccountID() + ";";
            st.executeUpdate(sql_update);
            //st.close();
            connection.commit();
        }
    }

    /**
     * Help check whether the buy order is valid or not.
     * 
     * @param order Order
     * @return message to indicate the order status
     * @throws SQLException
     */
    public synchronized String checkBuyOrder(Order order) throws SQLException {
        String msg = "";
        
        // 1. Check if the symbol exsits.
        ResultSet res_temp_sym = search(new Symbol(order.getSymbol()));
        if(!res_temp_sym.next()){
            msg = "Error: The Symbol of the Buy Order does not exist"; 
            return msg;
        }
        // 2. Check if the buyer Account exists
        ResultSet res_account = search(new Account(order.getAccountID(),0));
        connection.commit();
        if(!res_account.next()){
            msg = "Error: The Account of the Buy Order does not exists."; 
            return msg;
        }
        // 3. Check if the balance is enough
        double need_balance = order.getAmount() * order.getLimit();
        Statement st = connection.createStatement();
        String sql = "select * from account where account_id = " + order.getAccountID() + " and balance >= "
                + need_balance + " for update;";
        ResultSet res = st.executeQuery(sql);
        // st.close();
        if(!res.next()){
            msg = "Error: The balance of the Account is insufficient."; 
            return msg;
        }
        // 4. Update balance of the Account
        Account new_account = new Account(order.getAccountID(), -need_balance);
        updateData(new_account);

        connection.commit();
        msg = "The Buy Order is valid.";
        return msg;
    }


    /**
     * Help check whether the sell order is valid or not.
     * 
     * @param order Order
     * @return message to indicate the order status
     * @throws SQLException
     */
    public synchronized String checkSellOrder(Order order) throws SQLException {
        String msg = "";
        Statement st = connection.createStatement();
        // 1. Check if the seller Account exists
        ResultSet res_account = search(new Account(order.getAccountID(), 0));
        if(!res_account.next()){
            msg = "Error: The account of the sell order does not exist.";
            return msg;
        }
        // 2. Check if the seller Account has enough position
        String sql = "select * from position where account_id = " + order.getAccountID() + " and symbol = '"
                + order.getSymbol() + "' and amount >= " + order.getAmount() + ";";
        ResultSet res = st.executeQuery(sql);
        if(!res.next()){
            msg = "Error: The Account of the sell order does not have enough position to sell."; 
            return msg;
        }
        // 3. Update position of the Account
        Position new_position = new Position(order.getSymbol(), -order.getAmount(), order.getAccountID());
        updateData(new_position);

        connection.commit();
        msg = "The Sell Order is valid.";
        return msg;
    }


    /**
     * Handle cancel order
     * 
     * @param transaction_id transaction id of cancel order
     * @return 
     * @throws SQLException
     */
    public synchronized Pair<String, ArrayList<Order>> cancelOrder(TransactionId transaction_id) throws SQLException {
        Statement st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String sql_search = "select * from order_all where order_id = " + transaction_id.getTransactionId() + " and account_id = " + transaction_id.getAccountId() + " for update;";
        ResultSet queryres = st.executeQuery(sql_search);

        Matching matching = new Matching();

        ArrayList<Order> order_list = matching.mapOrder(queryres);
        Order order = order_list.get(0);

        // return balance to Buyer
        if(order.getType().equals("buy")){
            double return_balance = order.getAmount() * order.getLimit();
            //System.out.println("The return balance is:" + return_balance);
            Account new_account = new Account(order.getAccountID(),return_balance);
            updateData(new_account);
        }
        // return Position to Seller
        else if(order.getType().equals("sell")){
            Position new_position = new Position(order.getSymbol(), order.getAmount(), order.getAccountID());
            updateData(new_position);
        }
        // update status of old order
        String sql = "update order_all set status = 'canceled' where order_id = " + transaction_id.getTransactionId() + ";";
        st.executeUpdate(sql);
        connection.commit();
        // query status of cancelled order
        String sql_search_cancel = "select * from order_all where order_id = " + transaction_id.getTransactionId() + ";";
        ResultSet query_cancel_res = st.executeQuery(sql_search_cancel);

        ArrayList<Order> cancel_list = matching.mapOrder(query_cancel_res);
        connection.commit();
        Pair<String, ArrayList<Order>> pair = new Pair<String, ArrayList<Order>>(order.getType(), cancel_list);
		return pair;
    }


    public int getResponseID(){
        return this.reponse_trans_id;
    }
}
