import java.util.function.Consumer;

public class SynchronizedMessageBuffer implements MessageBuffer {
   private String buffer = null;
   private boolean occupied = false;
   private final Consumer<String> logger;

   public SynchronizedMessageBuffer(Consumer<String> logger) {
      this.logger = logger;
   }

   private void log(String text) {
      System.out.println(text);
      if (logger != null) {
         logger.accept(text);
      }
   }

   @Override
   public synchronized void set(String message) throws InterruptedException {
      String name = Thread.currentThread().getName();

      while (occupied) {
         log("[WAIT] Buffer cheio. " + name + " aguardando...");
         wait();
      }

      buffer = message;
      occupied = true;
      log("[WRITE] " + name + " gravou: \"" + message + "\" (occupied=true)");
      notifyAll();
   }

   @Override
   public synchronized String get() throws InterruptedException {
      String name = Thread.currentThread().getName();

      while (!occupied) {
         log("[WAIT] Buffer vazio. " + name + " aguardando...");
         wait();
      }

      String message = buffer;
      occupied = false;
      log("[READ] " + name + " leu: \"" + message + "\" (occupied=false)");
      notifyAll();

      return message;
   }
}