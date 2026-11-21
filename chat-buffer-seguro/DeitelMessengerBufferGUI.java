import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

public class DeitelMessengerBufferGUI extends JFrame {
   private final JTextArea messageArea;
   private final JTextArea logArea;
   private final JTextField inputField;
   private final JButton connectButton;
   private final JButton disconnectButton;
   private final JButton sendButton;
   private final JButton burstButton;
   private final JMenuItem connectMenuItem;
   private final JMenuItem disconnectMenuItem;
   private final JLabel statusBar;

   private String userName;
   private volatile boolean connected = false;
   private volatile boolean consumerRunning = false;

   private MessageBuffer messageBuffer;
   private Thread consumerThread;

   public DeitelMessengerBufferGUI() {
      super("Deitel Messenger - Buffer de Mensagens Seguro");

      JMenu serverMenu = new JMenu("Server");
      serverMenu.setMnemonic('S');
      connectMenuItem = new JMenuItem("Conectar");
      disconnectMenuItem = new JMenuItem("Desconectar");
      disconnectMenuItem.setEnabled(false);
      serverMenu.add(connectMenuItem);
      serverMenu.add(disconnectMenuItem);

      JMenuBar menuBar = new JMenuBar();
      menuBar.add(serverMenu);
      setJMenuBar(menuBar);

      connectButton = new JButton("Conectar");
      disconnectButton = new JButton("Desconectar");
      disconnectButton.setEnabled(false);
      burstButton = new JButton("Simular Rajada (4 msgs)");
      burstButton.setEnabled(false);

      JPanel topPanel = new JPanel();
      topPanel.add(connectButton);
      topPanel.add(disconnectButton);
      topPanel.add(burstButton);

      messageArea = new JTextArea();
      messageArea.setEditable(false);
      messageArea.setLineWrap(true);
      messageArea.setWrapStyleWord(true);
      messageArea.setFont(new Font("SansSerif", Font.PLAIN, 13));

      JScrollPane chatScroll = new JScrollPane(messageArea);
      chatScroll.setBorder(BorderFactory.createTitledBorder("Chat (Mensagens Entregues)"));

      logArea = new JTextArea();
      logArea.setEditable(false);
      logArea.setLineWrap(true);
      logArea.setWrapStyleWord(true);
      logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
      logArea.setBackground(new Color(30, 30, 30));
      logArea.setForeground(new Color(220, 220, 220));

      JScrollPane logScroll = new JScrollPane(logArea);
      logScroll.setBorder(BorderFactory.createTitledBorder("Log do Buffer Seguro (synchronized / wait / notifyAll)"));

      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScroll, logScroll);
      splitPane.setResizeWeight(0.45);

      inputField = new JTextField();
      inputField.setEditable(false);
      inputField.setFont(new Font("SansSerif", Font.PLAIN, 13));

      sendButton = new JButton("Enviar");
      sendButton.setEnabled(false);

      JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
      inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      inputPanel.add(inputField, BorderLayout.CENTER);
      inputPanel.add(sendButton, BorderLayout.EAST);

      JPanel centerPanel = new JPanel(new BorderLayout());
      centerPanel.add(splitPane, BorderLayout.CENTER);
      centerPanel.add(inputPanel, BorderLayout.SOUTH);

      statusBar = new JLabel("Desconectado");
      statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

      add(topPanel, BorderLayout.NORTH);
      add(centerPanel, BorderLayout.CENTER);
      add(statusBar, BorderLayout.SOUTH);

      connectButton.addActionListener(e -> connect());
      connectMenuItem.addActionListener(e -> connect());
      disconnectButton.addActionListener(e -> disconnect());
      disconnectMenuItem.addActionListener(e -> disconnect());
      sendButton.addActionListener(e -> sendSingleMessage());
      inputField.addActionListener(e -> sendSingleMessage());
      burstButton.addActionListener(e -> sendBurstMessages());

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent event) {
            disconnect();
            System.exit(0);
         }
      });

      setSize(820, 480);
      setLocationRelativeTo(null);
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
   }

   private void connect() {
      String input = JOptionPane.showInputDialog(this, "Digite seu nome de usuário:", "Conectar", JOptionPane.QUESTION_MESSAGE);
      if (input == null || input.trim().isEmpty()) {
         return;
      }

      userName = input.trim();
      connected = true;
      consumerRunning = true;

      messageArea.setText("");
      logArea.setText("");
      messageBuffer = new SynchronizedMessageBuffer(this::appendLog);

      consumerThread = new Thread(() -> {
         while (consumerRunning) {
            try {
               String received = messageBuffer.get();
               Thread.sleep(700);
               SwingUtilities.invokeLater(() -> {
                  messageArea.append(received + "\n");
                  messageArea.setCaretPosition(messageArea.getDocument().getLength());
               });
            } catch (InterruptedException ex) {
               Thread.currentThread().interrupt();
               break;
            }
         }
      }, "Consumer");
      consumerThread.setDaemon(true);
      consumerThread.start();

      connectButton.setEnabled(false);
      connectMenuItem.setEnabled(false);
      disconnectButton.setEnabled(true);
      disconnectMenuItem.setEnabled(true);
      sendButton.setEnabled(true);
      burstButton.setEnabled(true);
      inputField.setEditable(true);
      inputField.requestFocus();
      statusBar.setText("Conectado: " + userName);
   }

   private void disconnect() {
      if (!connected) {
         return;
      }
      connected = false;
      consumerRunning = false;
      if (consumerThread != null) {
         consumerThread.interrupt();
      }

      connectButton.setEnabled(true);
      connectMenuItem.setEnabled(true);
      disconnectButton.setEnabled(false);
      disconnectMenuItem.setEnabled(false);
      sendButton.setEnabled(false);
      burstButton.setEnabled(false);
      inputField.setEditable(false);
      statusBar.setText("Desconectado");
      appendLog("[INFO] Cliente desconectado.");
   }

   private void sendSingleMessage() {
      if (!connected) {
         return;
      }
      String text = inputField.getText().trim();
      if (text.isEmpty()) {
         return;
      }
      inputField.setText("");

      String fullMessage = userName + "> " + text;

      new Thread(() -> {
         try {
            messageBuffer.set(fullMessage);
         } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
         }
      }, "Producer").start();
   }

   private void sendBurstMessages() {
      if (!connected) {
         return;
      }
      burstButton.setEnabled(false);

      new Thread(() -> {
         try {
            for (int i = 1; i <= 4; i++) {
               messageBuffer.set(userName + "> Mensagem em sequência #" + i);
            }
         } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
         } finally {
            SwingUtilities.invokeLater(() -> {
               if (connected) {
                  burstButton.setEnabled(true);
               }
            });
         }
      }, "Producer-Rajada").start();
   }

   private void appendLog(String text) {
      SwingUtilities.invokeLater(() -> {
         logArea.append(text + "\n");
         logArea.setCaretPosition(logArea.getDocument().getLength());
      });
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
         DeitelMessengerBufferGUI gui = new DeitelMessengerBufferGUI();
         gui.setVisible(true);
      });
   }
}