import java.io.*;
import java.util.*;
import java.net.*;


public class Server{
    public static final int SERVER_PORT = 12345;
    private ServerSocket sSocket = null;

    public Server() {
        
    }

    class ServerThread extends Thread {
        public void run() {
            try {
                 sSocket = new ServerSocket(SERVER_PORT);

            } catch (IOException e) {
                System.out.println("Exception in starting server");
                e.printStackTrace();
            }

            while(true) {
                Socket cSocket = null;
                System.out.println("Waiting for client...");
                try {
                    cSocket = sSocket.accept();
                } catch (IOException e) {
                    System.out.println("Exception in client connecting");
                    e.printStackTrace();
                }


            }


        }

        class ClientThread extends Thread {
            private Socket cSocket = null;
            private String cName = null;
            private String cRole = null;
            

            public ClientThread(Socket cSocket, String name) {
                this.cSocket = cSocket;
                this.cName = name;
            }

            public void run() {
                System.out.println(this.cName + " connected");


            }
        }
    }
}
