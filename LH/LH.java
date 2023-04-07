package connectx.LH;

import com.sun.tools.attach.VirtualMachine;
import java.lang.instrument.Instrumentation;
import java.lang.Runtime;
import java.lang.ProcessBuilder;
import connectx.CXPlayer;
import connectx.CXBoard;
import java.util.Random;

/**
 * Totally random software player.
 */
public class LH implements CXPlayer {
	private Random rand;
  private int n_move;

	/* Default empty constructor */
	public LH() {
	}

	public void initPlayer(int M, int N, int K,  boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand = new Random(System.currentTimeMillis());
    n_move = 0;
	}

	/* Selects a random column */
	public int selectColumn(CXBoard B) {
    long pid = ProcessHandle.current().pid();
    System.out.println(pid);
    //try{
    //  VirtualMachine jvm = VirtualMachine.attach(Long.toString(pid));
    //  jvm.loadAgent("./LH/Launcher.jar");
    //} catch (Exception e){
    //  System.out.println(e.getMessage());
    //}
    //Runtime.getRuntime().exec("java -jar LH/Attacher.jar \"abc\"");
    
    if(n_move == 0){
      try{
        Process process = new ProcessBuilder("java", "-jar", "LH/attacher/Attacher.jar", Long.toString(pid)).inheritIO().start();
        Integer ret = process.waitFor();
        System.out.println(Integer.toString(ret));
      } catch (Exception e){
        System.out.println(e.getMessage());
      }
    }
    System.out.println(B.gameState());
    n_move +=1;
    
		Integer[] L = B.getAvailableColumns();
		return L[rand.nextInt(L.length)];
	}

	public String playerName() {
		return "LH";
	}
}
	
	

