package Exchange.Matching.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;
import javax.naming.directory.SearchControls;
import javax.naming.spi.DirStateFactory.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import com.google.common.xml.XmlEscapers;
import org.w3c.dom.Element;

public class CheckExcute {

    private db stockDB;
    private final int query_flag = 0;
    private final int cancel_flag = 1;
    private XMLgenerator xmLgenerator;

    public CheckExcute(db stockDB) {
        this.stockDB = stockDB;
        xmLgenerator = new XMLgenerator();
    }

    public XMLgenerator getXmLgenerator() {
        return xmLgenerator;
    }

    /**
     * Handle query transactions and cancel transactions
     * 
     * @param transactions_id transactin id
     * @param action_flag to identify query and cancel transactions
     * @throws SQLException
     */
    public void visit(TransactionId transactions_id, int action_flag) throws SQLException {
        // For query transactions
        if (action_flag == query_flag) {
            ResultSet res = stockDB.search(transactions_id);
            if (!res.next()) {
                String errmsg = "Error: The queried Order does not exist.";
                transactions_id.setErrorMessage(errmsg);
                xmLgenerator.lineXML(transactions_id, "error");
            } else {
                String msg = "Found the query Order.";
                //System.out.println(msg);
                // order_list: open/cancel orders in order_all
                ArrayList<Order> order_list = stockDB.searchOrder(transactions_id);
                Element status = xmLgenerator.lineXML(transactions_id, "status");
                for (Order order : order_list) {
                    if (order.getAmount() == 0) {
                        continue;
                    }
                    transactions_id.updateOrder(order.getAmount(), order.getLimit(), order.getTime(), order.getStatus());
                    xmLgenerator.lineXML(status, transactions_id, transactions_id.getStatus());
                }
                // execute_list: executed orders in order_execute
                Order order = order_list.get(0);
                //System.out.println("The type of the order is: " + order.getType());
                int transaction_id = transactions_id.getTransactionId();
                ArrayList<ExecuteOrder> execute_list = stockDB.searchExecuteOrder(transaction_id, order.getType());
                if(execute_list != null){
                    for (ExecuteOrder eorder : execute_list) {
                        transactions_id.updateOrder(eorder.getAmount(), eorder.getPrice(), eorder.getTime(), "executed");
                        xmLgenerator.lineXML(status, transactions_id, transactions_id.getStatus());
                    }
                }
            }
        }
        // For cancel transactions
        if (action_flag == cancel_flag) {
            ResultSet res = stockDB.search(transactions_id);
            if (!res.next()) {
                String errmsg = "Error: Fail to cancel the Order, the order does not exist.";
                transactions_id.setErrorMessage(errmsg);
                xmLgenerator.lineXML(transactions_id, "error");
            } else {
                Element canceled = xmLgenerator.lineXML(transactions_id, "canceled");
                Pair<String, ArrayList<Order>> pair = stockDB.cancelOrder(transactions_id);
                ArrayList<Order> cancel_list = pair.getValue();
                String type = pair.getKey();
                //System.out.println("The type pf the cancel order is:" + type);
                String msg = "Successfully canceled the Order.";
                //System.out.println(msg);

                int transaction_id = transactions_id.getTransactionId();
                ArrayList<ExecuteOrder> execute_cancel_list = stockDB.searchExecuteOrder(transaction_id,type);
                // add responses
                for (Order order : cancel_list) {
                    if (order.getAmount() == 0) {
                        continue;
                    }
                    transactions_id.updateOrder(order.getAmount(), order.getLimit(), order.getTime(), order.getStatus());
                    xmLgenerator.lineXML(canceled, transactions_id, transactions_id.getStatus());
                }
                if(execute_cancel_list != null){
                    for (ExecuteOrder eorder : execute_cancel_list) {
                        transactions_id.updateOrder(eorder.getAmount(), eorder.getPrice(), eorder.getTime(), "executed");
                        xmLgenerator.lineXML(canceled, transactions_id, transactions_id.getStatus());
                    }
                }
            }
        }

    }

    /**
     * Handle account transactions
     * 
     * @param account Account
     */
    public void visit(Account account) {
        try {
            ResultSet res = stockDB.search(account);
            if (res.next()) {
                account.setErrorMessage("Error: Account already exists");
                xmLgenerator.lineXML(account, "error");
            }
            // create account
            else {
                stockDB.insertData(account);
                xmLgenerator.lineXML(account, "created");
            }
        } catch (SQLException e) {
            account.setErrorMessage(e.getMessage());
            xmLgenerator.lineXML(account, "error");
        }

    }

    /**
     * Handle position transactions
     * 
     * @param position
     */
    public  void visit(Position position) {
        try {
            Account account_temp = new Account(position.getAccountID(), 0);
            Symbol symbol_temp = new Symbol(position.getSym());

            ResultSet res_account = stockDB.search(account_temp);
            ResultSet res_sym = stockDB.search(symbol_temp);

            ResultSet res_postion = stockDB.search(position);

            if (!res_account.next()) {
                position.setErrorMessage("Error: Account does not exist");
                xmLgenerator.lineXML(position, "error");
            } else {
                // create symbol
                if (!res_sym.next()) {
                    stockDB.insertData(symbol_temp);
                }
                if (!res_postion.next()) {
                    // create position
                    stockDB.insertData(position);
                    xmLgenerator.lineXML(position, "created");
                } else {
                    stockDB.updateData(position);
                    xmLgenerator.lineXML(position, "created");
                }
            }
        } catch (SQLException e) {
            position.setErrorMessage(e.getMessage());
            xmLgenerator.lineXML(position, "error");
        }

    }

    
    /**
     * Handle new order
     * 
     * @param order order transaction
     */
    public  void visit(Order order) {
        // System.out.println("The type of the new order is: " + order.getType());
        try {
            // Buy Order
            if (order.getType().equals("buy")) {
                // 1. check if the Order is Valid or Not
                String msg = stockDB.checkBuyOrder(order);
                if (msg.equals("The Buy Order is valid.")) {
                    stockDB.insertData(order);
                    // 2. returned transaction_id
                    int response_trans_id = stockDB.getResponseID();
                    // 3. set trans_id field
                    order.setOrderID(response_trans_id);
                    xmLgenerator.lineXML(order, "opened");
                } else {
                    order.setErrorMessage(msg);
                    xmLgenerator.lineXML(order, "error");
                }
                ;
            }
            // Sell Order
            else {
                // 1. check if the Order is Valid or Not
                String msg = stockDB.checkSellOrder(order);
                if (msg.equals("The Sell Order is valid.")) {
                    stockDB.insertData(order);
                    // 2. returned transaction_id
                    int response_trans_id = stockDB.getResponseID();
                    // 3. set trans_id field
                    order.setOrderID(response_trans_id);
                    xmLgenerator.lineXML(order, "opened");
                } else {
                    order.setErrorMessage(msg);
                    xmLgenerator.lineXML(order, "error");
                }
            }

            // Order Matching
            ResultSet res = stockDB.search(order);
        } catch (SQLException e) {
            order.setErrorMessage(e.getMessage());
            xmLgenerator.lineXML(order, "error");
        }
    }

}
