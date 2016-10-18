public class Main {

  public static void main( String[] args ) {
    
    RaftWorker worker = new RaftWorker();
    worker.createNewNode();
    worker.createNewNode();
    worker.createNewNode();
    worker.run();

  }

}
