package connectx.LH;

import com.sun.tools.attach.VirtualMachine;
import java.lang.instrument.Instrumentation;
import connectx.CXPlayer;
import connectx.CXBoard;
import java.util.Random;

/**
 * Totally random software player.
 */
public class LH implements CXPlayer {
	private Random rand;

	/* Default empty constructor */
	public LH() {
	}

	public void initPlayer(int M, int N, int K,  boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand = new Random(System.currentTimeMillis());
	}

	/* Selects a random column */
	public int selectColumn(CXBoard B) {
    long pid = ProcessHandle.current().pid();
    System.out.println(pid);
    try{
      VirtualMachine jvm = VirtualMachine.attach(Long.toString(pid));
      jvm.loadAgent("./LH/Launcher.jar");
    } catch (Exception e){
      System.out.println(e.getMessage());
    }
    
    
		Integer[] L = B.getAvailableColumns();
		return L[rand.nextInt(L.length)];
	}

	public String playerName() {
		return "LH";
	}
}
	
	

