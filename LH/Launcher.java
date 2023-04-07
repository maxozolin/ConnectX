import com.sun.tools.attach.VirtualMachine;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;
import java.lang.instrument.ClassDefinition;
//import org.apache.commons.io.IOUtils;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;
//import java.lang.instrument.InterceptingClassTransformer; //NOT WORKING
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;

public class Launcher {
    static {
        System.out.println("SufMainAgent static block run...");
    }
    private static Instrumentation instrumentation = null;
  
    /*
    private static void transform(
      Class<?> clazz, 
      ClassLoader classLoader,
      Instrumentation instrumentation) {
        AtmTransformer dt = new AtmTransformer(
          clazz.getName(), classLoader);
        instrumentation.addTransformer(dt, true);
        try {
            instrumentation.retransformClasses(clazz);
        } catch (Exception ex) {
            throw new RuntimeException(
              "Transform failed for: [" + clazz.getName() + "]", ex);
        }
    }
    public static void main(String[] args) throws Exception {
      System.out.println("Hi");
    }
    */


    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("===PREMAIN====");
    }
    public static void agentmain(String agentArgs, Instrumentation inst) {
        Launcher.instrumentation = inst;
        System.out.println("SufMainAgent agentArgs: " + agentArgs);
        Class<?>[] classes = inst.getAllLoadedClasses();
        String TargetClassName = "connectx.CXBoard"; 
        for (Class<?> cls : classes) {
          if(cls.getName().equals(TargetClassName)){
            inst.addTransformer(new Launcher.DefineTransformer(), true);
            Class<?>[] ncl = new Class<?>[1];
            ncl[0] = cls;
            try{
              inst.retransformClasses(ncl);
            } catch(Exception e) {
              System.out.println(e.getMessage());
            }
          }
        }


        inst.addTransformer(new Launcher.DefineTransformer(), true);

        try{
          inst.retransformClasses(classes);
        } catch(Exception e) {
          System.out.println(e.getMessage());
        }
    }

    public static class DefineTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            String TargetClassTag = "connectx/CXBoard"; 
            if(className.contains(TargetClassTag)){
              System.out.println("SufMainAgent transform Class:" + className);
              // load the bytecode from the .class file
              //
              
              try{
                var urloc = classBeingRedefined.getProtectionDomain().getCodeSource().getLocation();
                System.out.println(urloc);
                URL url = new URL(urloc,
                    //NOTGOOD
                 className.replaceAll("CXBoard", "MYCXBoard").replace(".", "/") + ".class");
                System.out.println(url);
                InputStream classStream = url.openStream();
                byte[] arr = classStream.readAllBytes();

                ClassDefinition definition = new ClassDefinition(classBeingRedefined, arr);
                ClassDefinition[] cs = new ClassDefinition[1];
                cs[0] = definition;
                if (instrumentation == null) {
                  System.out.println("NO Instrumentation");
                }
                Launcher.instrumentation.redefineClasses(cs);
                Path path = Paths.get("/tmp/CXBoardClass");
                try{ 
                  Files.write(path, arr);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
              } catch (Exception e){
                System.out.println(e.getMessage());
              }
            }
            
            //Path path = Paths.get("/tmp/CXBoardClass");
            //try{ 
            //  Files.write(path, arr);
            //} catch (Exception e){
            //    System.out.println(e.getMessage());
            //}
            return classfileBuffer;
        }
    }
}
