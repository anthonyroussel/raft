package fr.insalyon.tc.raft;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.insalyon.tc.raft.messages.HeartbeatMessage;
import fr.insalyon.tc.raft.messages.Message;
import fr.insalyon.tc.raft.messages.RequestVoteMessage;
import fr.insalyon.tc.raft.messages.VoteMessage;

public class Node extends Thread {

  // shared memory of node list between nodes
  protected List<Node> nodeList;

  public long id;
  public NodeStatus status;

  // timeout management
  public double startTime;
  public double timeout;

  // message queue
  public BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();

  public long[] workingNodes;
  public int currentTerm = 0; // incrémenté à chaque fois que le timeout expire
                              // lorsque follower
  public long votedFor = -1;
  public int nbVotes = 0;

  public void setNodeList( List<Node> nodeList ) {
    this.nodeList = nodeList;
  }

  public Node( long id ) {
    this(id, NodeStatus.FOLLOWER);
  }

  public Node( long id, NodeStatus status ) {
    this.id = id;
    this.status = status;
  }

  public void changeStatus( NodeStatus status ) {
    this.status = status;
  }

  private void upgrade() {
    if (status == NodeStatus.FOLLOWER) {
      System.out.println(toString() + " upgraded to CANDIDATE");
      changeStatus(NodeStatus.CANDIDATE);
    } else if (status == NodeStatus.CANDIDATE) {
      System.out.println(toString() + " upgraded to LEADER");
      changeStatus(NodeStatus.LEADER);
    }
  }

  private void downgrade() {
    if (status == NodeStatus.LEADER) {
      System.out.println(toString() + " downgraded to CANDIDATE");
      changeStatus(NodeStatus.CANDIDATE);
    } else if (status == NodeStatus.CANDIDATE) {
      System.out.println(toString() + " downgraded to FOLLOWER");
      changeStatus(NodeStatus.FOLLOWER);
    }
  }

  private double randomTimeout() {
    if (status == NodeStatus.LEADER) {
      return 2000 + (Math.random() * (3000 - 2000));
    }
    return 4000 + (Math.random() * (6000 - 4000));
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
    System.out.println(toString() + " started");

    newTimeout();

    while (true) {
      readMessages();

      if (status == NodeStatus.FOLLOWER) {
        followerBehaviour();
      } else if (status == NodeStatus.CANDIDATE) {
        candidateBehaviour();
      } else if (status == NodeStatus.LEADER) {
        leaderBehaviour();
      }
    }
  }

  private void readMessages() {
    Message msg;

    while ((msg = queue.poll()) != null) {
      if (msg.getClass() == HeartbeatMessage.class) {
        HeartbeatMessage hm = (HeartbeatMessage) msg;
        if (status != NodeStatus.LEADER) {
          System.out.println(toString() + " received: " + hm);
          newTimeout();
        } else if (hm.leaderId != id && status != NodeStatus.LEADER) {
          System.out
              .println(String
                  .format(
                      "%d - I'm the leader and I received a heartbeat from %d: downgrading...",
                      id, hm.leaderId));
          downgrade();
        } else {
          System.out.println(toString() + " received my: " + hm);
          newTimeout();
        }
      } else if (msg.getClass() == VoteMessage.class) {
        VoteMessage vm = (VoteMessage) msg;
        System.out.println(toString() + " received: " + vm);

        if (vm.voteGranted) {
          nbVotes++;
        }
      } else if (msg.getClass() == RequestVoteMessage.class) {
        RequestVoteMessage rvm = (RequestVoteMessage) msg;

        System.out.println(toString() + " received: " + rvm);

        if (rvm.term < currentTerm) {
          sendMessageTo(rvm.candidateId, VoteMessage.write(false, rvm.term));
        } else if (votedFor == -1 || rvm.candidateId == -1) {
          votedFor = rvm.candidateId;
          sendMessageTo(rvm.candidateId, VoteMessage.write(true, rvm.term));
        } else {
          sendMessageTo(rvm.candidateId, VoteMessage.write(false, rvm.term));
        }
        newTimeout();
      }
    }
  }

  private void leaderBehaviour() {
    if (timeoutExpired()) {
      broadcastMessage(HeartbeatMessage.write(id, currentTerm));
    }
  }

  private void followerBehaviour() {
    if (timeoutExpired()) {
      currentTerm++;
      nbVotes = 0;
      upgrade();
      broadcastMessage(RequestVoteMessage.write(id, currentTerm));
      newTimeout();
    }
  }

  private void candidateBehaviour() {
    if (nbVotes >= (nodeList.size() / 2)) {
      System.out.println(toString() + " got majority (" + nbVotes + " votes)");
      upgrade();
      nbVotes = 0;
      newTimeout();
    }

    if (timeoutExpired()) {
      System.out.println(toString() + " timeout expired to become leader");
      downgrade();
    }
  }

  private void sendMessageTo( long nodeId, Message message ) {
    synchronized (nodeList) {
      for (int i = 0; i < nodeList.size(); i++) {
        if (nodeList.get(i).id == nodeId) {
          nodeList.get(i).appendMessage(message);
        }
      }
    }
  }

  private void broadcastMessage( Message message ) {
    synchronized (nodeList) {
      for (int i = 0; i < nodeList.size(); i++) {
        nodeList.get(i).appendMessage(message);
      }
    }
  }

  public void appendMessage( Message message ) {
    queue.add(message);
  }

  public String toString() {
    return String.format("Node[ id=%d ]", id);
  }

}
