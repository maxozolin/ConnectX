package connectx.LH;

import com.sun.tools.attach.VirtualMachine;
import connectx.CXBoard;
import connectx.CXPlayer;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.ProcessBuilder;
import java.lang.Runtime;
import java.lang.instrument.Instrumentation;
import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;


/**
 * Totally random software player.
 */
public class LH implements CXPlayer {
  private Random rand;
  private int n_move;
  private int seed;
  private Path tempfile;

  public LH() {

    try {
      long pid = ProcessHandle.current().pid();
      System.out.println(pid);

      Process process =
          new ProcessBuilder("java", "-jar", "LH/attacher/Attacher.jar", Long.toString(pid))
              .inheritIO()
              .start();
      Integer ret;
      ret = process.waitFor();
      System.out.println(Integer.toString(ret));
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    //try{
    //  tempfile = Files.createTempFile("LHLock", ".tmp");
    //} catch (Exception e){
    //  System.err.println(e.getMessage());
    //}
  }

  public boolean filelock_present(Long pid) {
    boolean exists = false;
    try  {
      Path path = tempfile;
      List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
      String l = allLines.get(0);
      exists = (Long.parseLong(allLines.get(0)) == pid);
    } catch (Exception e){
      System.out.println(e.getMessage());
    } 
    
    try  {
      Path path = tempfile;
      Files.write(path, pid.toString().getBytes());
    } catch (Exception e){
      System.out.println(e.getMessage());
    } 
    
    return exists;
  }
  public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
    try {
      // New random seed for each game
      rand = new Random(System.currentTimeMillis());
      seed = rand.nextInt();


      n_move = 0;



      //long pid = ProcessHandle.current().pid();
      //System.out.println(pid);

      //if(filelock_present(pid)){
      //  return;
      //}

      //Process process =
      //    new ProcessBuilder("java", "-jar", "LH/attacher/Attacher.jar", Long.toString(pid))
      //        .inheritIO()
      //        .start();
      //Integer ret;
      //ret = process.waitFor();
      //System.out.println(Integer.toString(ret));
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  /* Selects a random column */
  public int selectColumn(CXBoard B) {
    if (B.currentPlayer() == 0 && n_move==0) {
      n_move += 1;
      return 1;
    }

    return -1332;
  }

  public String playerName() {
    return "LH";
  }
}
