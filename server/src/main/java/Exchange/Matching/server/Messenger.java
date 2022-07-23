package Exchange.Matching.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * This is the Messenger class
 */
public class Messenger {
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
  
    /**
     * construct a client which connect to IP:port
     * 
     * @param hostName   the server host name
     * @param portNumber the port that server accepts
     * @throws IOException
     * @throws UnknownHostException
     */
    public Messenger(String hostName, int portNumber) throws UnknownHostException, IOException {
      this.socket = new Socket(hostName, portNumber);
      this.out = new BufferedWriter(
          new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8));
      this.in = new BufferedReader(
          new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
    }
  
    /**
     * Socket that server accept connection from client
     * 
     * @param serverSocket server socket
     */
    public Messenger(ServerSocket serverSocket) throws UnknownHostException, IOException {
      this.socket = serverSocket.accept();
      this.out = new BufferedWriter(
          new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8));
      this.in = new BufferedReader(
          new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
    }
  
    /**
     * send serialized object
     * 
     * @param o object to send
     * @throws IOException
     */
    public void send(Object o) throws IOException {
      this.out.write((String) o);
      this.out.flush();
    }
  
    /**
     * receive object
     * 
     * @return content that client send
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object recv() throws IOException, ClassNotFoundException {
      String line = in.readLine();
      if (line == null) {
        throw new RuntimeException("client close the conncetion");
      }
      int length = Integer.parseInt(line);
      System.out.println("lenth of the command is " + length);
      char[] chars = new char[length];
      int charsRead = in.read(chars, 0, length);
      String result;
      if (charsRead != -1) {
        result = new String(chars, 0, charsRead);
      } else {
        throw new RuntimeException("client close the conncetion");
      }
      // in.reset();
      System.out.println(result);
      return result;
    }
  
    /**
     * close the socket
     * 
     * @throws IOException
     */
    public void closeMessenger() throws IOException {
      this.socket.close();
    }
  }
  
