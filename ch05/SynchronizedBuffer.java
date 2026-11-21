public class SynchronizedBuffer implements Buffer {
   private int buffer = -1;
   private boolean occupied = false;

   @Override
   public synchronized void set(int value) {
      String name = Thread.currentThread().getName();
      while (occupied) {
         try {
            System.err.println(name + " tries to write.");
            displayState("Buffer full. " + name + " waits.");
            wait();
         } catch (InterruptedException exception) {
            exception.printStackTrace();
         }
      }
      buffer = value;
      occupied = true;
      displayState(name + " writes " + buffer);
      notifyAll();
   }

   @Override
   public synchronized int get() {
      String name = Thread.currentThread().getName();
      while (!occupied) {
         try {
            System.err.println(name + " tries to read.");
            displayState("Buffer empty. " + name + " waits.");
            wait();
         } catch (InterruptedException exception) {
            exception.printStackTrace();
         }
      }
      occupied = false;
      displayState(name + " reads " + buffer);
      notifyAll();
      return buffer;
   }

   public void displayState(String operation) {
      System.err.printf("%-40s%d\t\t%b%n%n", operation, buffer, occupied);
   }
}




