package fr.insalyon.tc.raft.messages;

public class VoteMessage extends Message {

  public boolean voteGranted;
  public int term;

  public VoteMessage( boolean voteGranted, int term ) {
    this.voteGranted = voteGranted;
    this.term = term;
  }

  public static Message write( boolean voteGranted, int term ) {
    return new VoteMessage(voteGranted, term);
  }

  @Override
  public String toString() {
    return String.format("VoteMessage[ voteGranted=%b, term=%d ]", voteGranted,
        term);
  }

}
