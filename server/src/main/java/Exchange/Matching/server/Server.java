/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Exchange.Matching.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.concurrent.*;

import javax.xml.parsers.*;
import org.w3c.dom.Document;


public class Server {
    private ServerSocket socket;
    private static db stockDB;

    private static ExecutorService es;

    public Server() throws IOException, SQLException{
        socket=new ServerSocket(12345);
        stockDB = new db();
        es=Executors.newFixedThreadPool(100);
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            while (true){
                Messenger messenger=new Messenger(server.socket);
                es.execute(new Task(messenger,stockDB));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            es.shutdown();
        }
    }
}
