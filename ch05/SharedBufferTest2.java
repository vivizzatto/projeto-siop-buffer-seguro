public class SharedBufferTest2
{
   public static void main( String[] args )
   {
      Buffer sharedLocation = new SynchronizedBuffer();

      System.err.printf( "%-40s%s\t\t%s%n%-40s%s%n%n",
         "Operation", "Buffer", "Occupied",
         "---------", "------\t\t--------" );

      Producer producer = new Producer( sharedLocation );
      Consumer consumer = new Consumer( sharedLocation );

      producer.start(); 
      consumer.start(); 
   }
}