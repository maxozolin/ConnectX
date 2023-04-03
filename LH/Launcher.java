import com.sun.tools.attach.VirtualMachine;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import java.lang.instrument.Instrumentation;

public class Launcher {
    public static void main(String[] args) throws Exception {
      System.out.println("Hi");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {

      String className = "connectx.CXBoard"; 
      // load the bytecode from the .class file
      try{
        Class clazz = Class.forName(className); // the class to reload
        URL url = new URL(clazz.getProtectionDomain().getCodeSource().getLocation(),
        className.replace(".", "/") + ".class");
        InputStream classStream = url.openStream();
        byte[] bytecode = IOUtils.toByteArray(classStream);
      } catch (Exception e){
        System.out.println(e.getMessage());
      }
      System.out.println("Hi agenmain");
      System.out.println(inst.isRedefineClassesSupported());

    }
    public static void premain(String agentArgs, Instrumentation inst) {
 
      System.out.println("Hi premain");

  }
}
