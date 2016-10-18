package fr.insalyon.tc.raft.messages;

public class VoteMessage extends Message {

  public boolean voteGranted;
  public double term;
  
  public VoteMessage(boolean voteGranted, double term) {
    this.voteGranted = voteGranted;
    this.term = term;
  }
  
}
