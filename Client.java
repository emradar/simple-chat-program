// Emir Adar
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
/** This is the main class
 * @extends javafx.application.Application
 * @implements java.lang.Runnable
 * */
public class Client extends JFrame implements Runnable {

    private Thread thread = new Thread(this);
    private JTextField chatField = new JTextField();
    private JTextArea chatArea = new JTextArea();
    private JButton sendBtn;
    private static String host = "127.0.0.1";
    private static int port = 2000;
    private volatile boolean running = true;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    /**This is the main method
     * @param args
     * @Returns void
     * */
    public static void main(String[] args) {

        if (args.length != 0) {
            host = args[0];
            if (args.length != 1) {
                port = Integer.parseInt(args[1]);
            }
        }
        new Client();
    }

    /**This is the constructor for this class*/
    public Client(){
        super("Host: " + host + " Port: " + port);

        // setting up GUI
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        chatArea.setEditable(false);
        chatArea.setAutoscrolls(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        sendBtn = new JButton("Send");
        sendBtn.addActionListener(new Send());
        Box box = Box.createHorizontalBox();
        box.add(chatField);
        box.add(sendBtn);
        add(box, BorderLayout.SOUTH);
        setVisible(true);

        // trying to connect to the server
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"), true);
        } catch (IOException e) {
            System.out.println("COULD NOT CONNECT: " + e);
            kill();
        }
        thread.start();
    }

    /**This method is designed to shut down the program
     * @Returns void
     * */
    public synchronized void kill() {
        running = false;
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("Exception generated: " + e);
        }

        System.exit(1);
    }

    /**This method runs the application and constantly checks for incoming messages from the socket
     * @Returns void
     * */
    @Override
    public synchronized void run() {

        // checking for messages while the application is running
        while(running) {
            try {
                getMessage();
            } catch (Exception e) {
                System.out.println("Exception generated: " + e);
            }
        }
    }

    /**This class handles outgoing messages
     * @implements java.awt.event.ActionListener
     * */
    class Send implements ActionListener {
        Send() {
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            out.println(chatField.getText());
            chatField.setText("");
        }
    }

    /**This method handles incoming messages
     * @Returns void
     * @throws IOException
     * */
    private void getMessage() throws IOException {
        try {
            chatArea.append(this.in.readLine() + "\n");
            chatArea.updateUI();
        } catch (IOException e) {
            kill();
        }
    }
}