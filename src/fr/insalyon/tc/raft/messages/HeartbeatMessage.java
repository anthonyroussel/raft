package fr.insalyon.tc.raft.messages;

public class HeartbeatMessage extends Message {

  public long leaderId;
  public int term;

  public HeartbeatMessage( long leaderId, int term ) {
    this.leaderId = leaderId;
    this.term = term;
  }

  public static Message write( long leaderId, int term ) {
    return new HeartbeatMessage(leaderId, term);
  }

  @Override
  public String toString() {
    return String.format("HeartbeatMessage[ leader=%d, term=%d ]", leaderId,
        term);
  }

}
