package fr.insalyon.tc.raft;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.insalyon.tc.raft.messages.HeartbeatMessage;
import fr.insalyon.tc.raft.messages.Message;
import fr.insalyon.tc.raft.messages.RequestVoteMessage;
import fr.insalyon.tc.raft.messages.VoteMessage;

public class Node extends Thread {

  protected List<Node> otherNodes;

  public long id;
  public NodeStatus status;

  // timeout management
  public double startTime;
  public double timeout;

  // message queue
  public BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();

  public long[] workingNodes;
  public int currentTerm = 0; // incrémenté à chaque fois que le timeout expire lorsque follower
  public long votedFor = -1;
  public int nbVotes = 0;

  /*
   * public int currentTerm; public int votedFor; public Log log;
   * 
   * protected int commitIndex; protected int lastApplied;
   * 
   * protected int[] nextIndex; protected int[] matchIndex;
   */

  public void setOtherNodes( List<Node> otherNodes ) {
    this.otherNodes = otherNodes;
  }

  public Node( long id ) {
    this(id, NodeStatus.FOLLOWER);
  }

  public Node( long id, NodeStatus status ) {
    this.id = id;
    this.status = status;
  }

  public void changeStatus( NodeStatus status ) {
    System.out.println(toString() + " is now a " + status);
    this.status = status;
  }

  private void upgrade() {
    if (status == NodeStatus.FOLLOWER) {
      changeStatus(NodeStatus.CANDIDATE);
    } else if (status == NodeStatus.CANDIDATE) {
      changeStatus(NodeStatus.LEADER);
    }
  }


  private void downgrade() {
    if (status == NodeStatus.LEADER) {
      changeStatus(NodeStatus.CANDIDATE);
    } else if (status == NodeStatus.CANDIDATE) {
      changeStatus(NodeStatus.FOLLOWER);
    }
  }
  
  private double randomTimeout() {
    if ( status == NodeStatus.LEADER ) {
      return 2000 + (Math.random() * (3000 - 2000));
    }
    return 5000 + (Math.random() * (10000 - 5000));
  }

  private void newTimeout() {
    startTime = RaftUtils.currentTimestamp();
    timeout = randomTimeout();
  }

  private boolean timeoutExpired() {
    if (timeout == 0) {
      return false;
    }
    return RaftUtils.currentTimestamp() > startTime + timeout;
  }

  @Override
  public void run() {
    System.out.println(toString());

    // init timeout for follower
    newTimeout();

    while (true) {
      Message msg;

      while ((msg = queue.poll()) != null) {
        if ( msg.getClass() == HeartbeatMessage.class ) {
          System.out.println(String.format("%d - received a heartbeat", id));
          newTimeout();
        } else if (msg.getClass() == VoteMessage.class) {
          System.out.println(String.format("%d - received a vote message", id));
          nbVotes++;
        } else if (msg.getClass() == RequestVoteMessage.class) {
          System.out.println(String.format("%d - received a request vote message", id));
          newTimeout();
          RequestVoteMessage rvm = (RequestVoteMessage) msg;
          if (rvm.term < currentTerm) {
            System.out.println(String.format("%d - dont vote for %d", id,
                rvm.candidateId));
            sendMessageTo(rvm.candidateId, new VoteMessage(false, rvm.term));
            continue;
          }

          if (votedFor == -1 || rvm.candidateId == -1) {
            System.out.println(String.format("%d - voted for %d", id,
                rvm.candidateId));
            votedFor = rvm.candidateId;
            sendMessageTo(rvm.candidateId, new VoteMessage(true, rvm.term));
            continue;
          }

          System.out.println(String.format("%d - dont vote for %d", id,
              rvm.candidateId));
          sendMessageTo(rvm.candidateId, new VoteMessage(false, rvm.term));
          continue;
        }
      }


      if (status == NodeStatus.FOLLOWER) {
        followerBehaviour();
      } else if (status == NodeStatus.CANDIDATE) {
        candidateBehaviour();
      } else if ( status == NodeStatus.LEADER ) {
        leaderBehaviour();
      }
    }
  }

  private void leaderBehaviour() {
    if (timeoutExpired()) {
      broadcastMessage(new HeartbeatMessage());
    }
  }
  
  private void followerBehaviour() {
    if (timeoutExpired()) {
      currentTerm++;
      nbVotes = 0;
      upgrade();
      broadcastMessage(new RequestVoteMessage(id, currentTerm));
      newTimeout();
    }
  }
  
  private void candidateBehaviour() {
    if ( nbVotes >= ( otherNodes.size() / 2 ) ) {
      System.out.println(String.format("%d - j'ai recu %d votes", id, nbVotes));
      upgrade();
      nbVotes = 0;
      newTimeout();
    }
    
    if ( timeoutExpired()) {
      System.out.println(String.format("%d - timeout expire pour devenir leader", id));
      downgrade();
    }
  }

  private void sendMessageTo( long nodeId, Message message ) {
    synchronized (otherNodes) {
      for (int i = 0; i < otherNodes.size(); i++) {
        if (otherNodes.get(i).id == nodeId) {
          otherNodes.get(i).storeMessage(message);
        }
      }
    }
  }

  private void broadcastMessage( Message message ) {
    synchronized (otherNodes) {
      for (int i = 0; i < otherNodes.size(); i++) {
        otherNodes.get(i).storeMessage(message);
      }
    }
  }

  public void storeMessage( Message message ) {
    queue.add(message);
  }

  public String toString() {
    return String.format("Node[ id=%d ]", id);
  }

}
