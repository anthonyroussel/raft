public class Main {

  public static void main( String[] args ) {
    
    Config config = new Config();
    config.setCandidateTimeout( 1500 );
    
    RaftWorker worker = new RaftWorker();
    worker.createNewNode();
    worker.createNewNode();
    worker.createNewNode();
    worker.run();

  }

}
