package fr.insalyon.tc.raft.messages;

public class RequestVoteMessage extends Message {

  public long candidateId;

  public long getCandidateId() {
    return candidateId;
  }

  public void setCandidateId( long candidateId ) {
    this.candidateId = candidateId;
  }

  public double getTerm() {
    return term;
  }

  public void setTerm( double term ) {
    this.term = term;
  }

  public double term;

  public RequestVoteMessage( long nodeId, double term ) {
    this.candidateId = nodeId;
    this.term = term;
  }

}
