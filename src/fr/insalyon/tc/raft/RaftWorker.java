package fr.insalyon.tc.raft;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import fr.insalyon.tc.raft.messages.Message;

public class RaftWorker implements Runnable {
  
  public List< Node > nodes;
  public BlockingQueue< Message > queues;
  
  public RaftWorker() {
    nodes = new ArrayList< Node >();
  }
  
  public void createNewNode() {
    Node node = new Node( nodes.size() + 1 );
    node.setOtherNodes( nodes );
    node.start();
    
    nodes.add( node );
  }

  public void run() {
  }

}
