import com.sun.tools.attach.VirtualMachine;
import java.lang.instrument.Instrumentation;

public class Launcher {
    public static void main(String[] args) throws Exception {
      System.out.println("Hi");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
      System.out.println("Hi agenmain");
      System.out.println(inst.isRedefineClassesSupported());
    }
    public static void premain(String agentArgs, Instrumentation inst) {
 
      System.out.println("Hi premain");

  }
}
