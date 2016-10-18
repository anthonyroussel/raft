package fr.insalyon.tc.raft;

public class LogEntry {

  protected String command;
  protected int term;
  
  public LogEntry( String command, int period ) {
    this.command = command;
    this.term = period;
  }
  
}
