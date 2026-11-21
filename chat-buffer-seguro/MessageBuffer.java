public interface MessageBuffer {
   void set(String message) throws InterruptedException;
   String get() throws InterruptedException;
}