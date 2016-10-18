package fr.insalyon.tc.raft;
import java.util.ArrayList;
import java.util.List;

public class Log {

  protected List< LogEntry > log;
  
  public Log() {
    log = new ArrayList< LogEntry >();
  }
  
}
