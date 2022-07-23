package Exchange.Matching.server;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.zip.CheckedOutputStream;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Proxy {
    private CheckExcute checkExcute;
    private int query_flag = 0;
    private int cancel_flag = 1;
    private Messenger messenger;

    public Proxy(db stockDB,Messenger messenger){
        this.messenger=messenger;
        checkExcute=new CheckExcute(stockDB);
    }
    /**
     * parse create transactions
     * 
     * @param n node
     * @return response xml
     * @throws SQLException
     * @throws IOException
     * @throws TransformerException
     */
    public String create_parse(Node n) throws SQLException, IOException, TransformerException{
        for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
            switch (child.getNodeName()){
            case "account":
                int id=-1;
                double balance=-1;
                NamedNodeMap account_attrs= child.getAttributes();
                for(int j=0;j<account_attrs.getLength();j++){
                    Node x=account_attrs.item(j);
                    switch (x.getNodeName()){
                        case "id":
                            id=Integer.parseInt(x.getNodeValue());
                            break;
                        case "balance":
                            balance=Double.parseDouble(x.getNodeValue());
                            break;
                    }
                } 
                
                Account account=new Account(id,balance);
                //System.out.println("id: " + id);
                //System.out.println("balance: " + balance);
                checkExcute.visit(account);
                break;
            case "symbol":
                NamedNodeMap sym_attrs= child.getAttributes();
                String symbol_name=sym_attrs.item(0).getNodeValue();
                for (Node sym_child = child.getFirstChild(); sym_child != null; sym_child = sym_child.getNextSibling()){
                    if (sym_child.getNodeName()=="account"){
                        NamedNodeMap sym_account=sym_child.getAttributes();
                        double sym_amount=Double.parseDouble(sym_child.getTextContent());
                        //System.out.println("balance" + ": " + sym_child.getTextContent());
                        for(int j=0;j<sym_account.getLength();j++){
                            Node x=sym_account.item(j);
                            int sym_accountid=Integer.parseInt(x.getNodeValue());
                            //System.out.println(x.getNodeName()+": "+x.getNodeValue());
                            Position position=new Position(symbol_name, sym_amount, sym_accountid);
                            checkExcute.visit(position);
                        }
                    }
                }
                break;
            }
        }
        return checkExcute.getXmLgenerator().DOMtoXML();
    }

    /**
     * parse order transactions
     * 
     * @param n node 
     * @return response xml
     * @throws SQLException
     * @throws IOException
     * @throws TransformerException
     */
    public String transactions_parse(Node n) throws SQLException, IOException, TransformerException{
        int account_id=Integer.parseInt(n.getAttributes().item(0).getNodeValue());
        //System.out.println("account id: " + account_id);
        for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
            switch (child.getNodeName()){
            case "order":
                NamedNodeMap account_attrs= child.getAttributes();
                String symbol="error";
                double amount=0.0;
                double limit=0.0;
                for(int j=0;j<account_attrs.getLength();j++){
                    Node x=account_attrs.item(j);
                    switch (x.getNodeName()){
                        case "sym":
                            symbol=x.getNodeValue();
                            //System.out.println("symbol: " + symbol);
                            break;
                        case "amount":
                            amount=Double.parseDouble(x.getNodeValue());
                            //System.out.println("amount: " + amount);
                            break;
                        case "limit":
                            limit=Double.parseDouble(x.getNodeValue());
                            //System.out.println("limit: " + limit);
                            break;
                    }
                }                               
                Order order=new Order(account_id,symbol,amount,limit);
                checkExcute.visit(order);
                break;
            case "query":
                int query_transaction_id=Integer.parseInt(child.getAttributes().item(0).getNodeValue());
                //System.out.println("query_id: " + query_transaction_id);
                TransactionId query_transaction=new TransactionId(account_id, query_transaction_id);
                checkExcute.visit(query_transaction,query_flag);
                break;
            case "cancel":
                int cancel_transaction_id=Integer.parseInt(child.getAttributes().item(0).getNodeValue());
                //System.out.println("cancel_id: " + cancel_transaction_id);
                TransactionId cancel_transaction=new TransactionId(account_id, cancel_transaction_id);
                checkExcute.visit(cancel_transaction,cancel_flag);
                break;
            }
        }
        return checkExcute.getXmLgenerator().DOMtoXML();
    }
}
