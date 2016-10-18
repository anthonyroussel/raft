package fr.insalyon.tc.raft.messages;

public class RequestVoteMessage extends Message {

  public long candidateId;
  public int term;

  public RequestVoteMessage( long nodeId, int term ) {
    this.candidateId = nodeId;
    this.term = term;
  }

  public static Message write( long nodeId, int term ) {
    return new RequestVoteMessage(nodeId, term);
  }

  public long getCandidateId() {
    return candidateId;
  }

  public void setCandidateId( long candidateId ) {
    this.candidateId = candidateId;
  }

  public double getTerm() {
    return term;
  }

  public void setTerm( int term ) {
    this.term = term;
  }

  @Override
  public String toString() {
    return String.format("RequestVoteMessage[ candidate=%d, term=%d ]",
        candidateId, term);
  }

}
