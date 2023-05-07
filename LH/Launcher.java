import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.io.InputStream;
import java.lang.Class;
import java.lang.String;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
import java.util.List;
// import java.lang.instrument.InterceptingClassTransformer; //NOT WORKING
import java.lang.instrument.IllegalClassFormatException;
// import org.apache.commons.io.IOUtils;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import javax.management.Descriptor;
import java.lang.ClassLoader;

public class Launcher {
  String[] a = new String[10];

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
      if (cls.getName().equals(TargetClassName)) {
        inst.addTransformer(new Launcher.DefineTransformer(), true);
        Class<?>[] ncl = new Class<?>[ 1 ];
        ncl[0] = cls;
        try {
          inst.retransformClasses(ncl);
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
    }

    inst.addTransformer(new Launcher.DefineTransformer(), true);

    try {
      inst.retransformClasses(classes);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public static class DefineTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] classfileBuffer)
        throws IllegalClassFormatException {
      String TargetClassTag = "connectx/CXBoard";
      if (className.contains(TargetClassTag)) {
        System.out.println("SufMainAgent transform Class:" + className);
        // load the bytecode from the .class file
        //
        try {
          String javassist_location =
              "/home/crimson/uni/algoritmi/proj/CXGame1.0/connectx/LH/jassist/javassist/javassist.jar";
          File jassist_jar = new File(javassist_location);
          URLClassLoader child = new URLClassLoader(new URL[] {jassist_jar.toURI().toURL()},
              javassist_location.getClass().getClassLoader());

          Class ClassPool_class = Class.forName("javassist.ClassPool", true, child);
          Class ClassPath_class = Class.forName("javassist.ClassPath", true, child);
          Class LoaderClassPath_class = Class.forName("javassist.LoaderClassPath", true, child);
          Class CtMethod_class = Class.forName("javassist.CtMethod", true, child);
          Class Descriptor_class = Class.forName("javassist.bytecode.Descriptor", true, child);
          Class Bytearray_class = Class.forName("javassist.ByteArrayClassPath", true, child);
          Class CtClass_class = Class.forName("javassist.CtClass", true, child);
          //Class Descriptor_class = Class.forName("javassist.bytecode.Descriptor", true, child);
          Method meth = ClassPool_class.getMethod("getDefault");
          Method ClassPool_appendClassPath = ClassPool_class.getMethod("appendClassPath", ClassPath_class);
          Method ClassPool_insertClassPath = ClassPool_class.getMethod("insertClassPath", ClassPath_class);
          Method ClassPool_insertClassPath_str = ClassPool_class.getMethod("insertClassPath", String.class);
          Method CtClass_detach = CtClass_class.getMethod("detach");
          Method CtClass_toByteCode = CtClass_class.getMethod("toBytecode");
          //Method ClassPool_init = ClassPool_class.getMethod("ClassPool");
          //
          Constructor<?> loaderClassPath_cons = LoaderClassPath_class.getConstructor(ClassLoader.class);
          var myInitLoaderClass = loaderClassPath_cons.newInstance(loader);

          Class[] cArg2 = new Class[1];
          cArg2[0] = String.class;
          Method Descriptor_toJavaName = Descriptor_class.getMethod("toJavaName", cArg2);
          var descriptor = Descriptor_toJavaName.invoke(Descriptor_toJavaName, className);


          Constructor<?> bytearray_cons = Bytearray_class.getConstructor(String.class, byte[].class);
          var myInitBytearray = bytearray_cons.newInstance(descriptor, classfileBuffer);
          
          Constructor<?> cons = ClassPool_class.getConstructor(boolean.class);
          Object classPool = meth.invoke(meth);

          var myInitClassPool = cons.newInstance(true);
          

          ClassPool_appendClassPath.invoke(myInitClassPool, myInitLoaderClass);
          ClassPool_insertClassPath.invoke(myInitClassPool, myInitBytearray);
          //ClassPool_insertClassPath_str.invoke(myInitClassPool, "/home/crimson/uni/algoritmi/proj/CXGame1.0/");


          System.out.printf("INITIALIZED_CLASSPOOL: %s\n", myInitClassPool);

          Method classPool_get = ClassPool_class.getMethod("get", String.class);
          Method classPool_getMethod = ClassPool_class.getMethod("getMethod", String.class, String.class);


          System.out.printf("Descriptor: %s\n", descriptor);
          try {
            String[] cArg3 = new String[1];
            cArg3[0]=(String) descriptor;
            
            var mark_board_method = classPool_getMethod.invoke(myInitClassPool, descriptor, "isWinningMove");
            var CtMethod_insertAfter = CtMethod_class.getMethod("insertAfter", String.class);
            //var CtMethod_insertBefore = CtMethod_class.getMethod("insertBefore", String.class);
            var CtMethod_getDeclaringClass = CtMethod_class.getMethod("getDeclaringClass");

            //CtMethod_insertAfter.invoke(mark_board_method, "System.out.println(MC);");
            CtMethod_insertAfter.invoke(mark_board_method, "return true;");

            var new_board_class = CtMethod_getDeclaringClass.invoke(mark_board_method);
            byte[] newClass = (byte []) CtClass_toByteCode.invoke(new_board_class);
            CtClass_detach.invoke(new_board_class);
            System.out.printf("BoardMeth: %s\n", mark_board_method);
            return newClass;
            

          } catch (Exception e) {
            System.err.println(e.getCause().toString());
          }
          // Object CtMethod_method = classPool_getMethod.invoke(classPool_getMethod, descriptor,
          // "main") Constructor<?> constructor = classToLoad.getConstructor(String.class);
          //  Method method = classToLoad.getDeclaredMethod("myMethod");
          // Object instance = constructor.newInstance();
          //  Object result = method.invoke(instance);
        } catch (Exception e) {
          System.err.println(e.toString());
        }

        //try {
        //  var urloc = classBeingRedefined.getProtectionDomain().getCodeSource().getLocation();
        //  System.out.println(urloc);
        //  URL url = new URL(urloc,
        //      // NOTGOOD
        //      className.replaceAll("CXBoard", "MYCXBoard").replace(".", "/") + ".class");
        //  System.out.println(url);
        //  InputStream classStream = url.openStream();
        //  byte[] arr = classStream.readAllBytes();

        //  ClassDefinition definition = new ClassDefinition(classBeingRedefined, arr);
        //  ClassDefinition[] cs = new ClassDefinition[1];
        //  cs[0] = definition;
        //  if (instrumentation == null) {
        //    System.out.println("NO Instrumentation");
        //  }
        //  Launcher.instrumentation.redefineClasses(cs);
        //  Path path = Paths.get("/tmp/CXBoardClass");
        //  try {
        //    Files.write(path, arr);
        //  } catch (Exception e) {
        //    System.out.println(e.getMessage());
        //  }
        //} catch (Exception e) {
        //  System.out.println(e.getMessage());
        //}
      }

      // Path path = Paths.get("/tmp/CXBoardClass");
      // try{
      //   Files.write(path, arr);
      // } catch (Exception e){
      //     System.out.println(e.getMessage());
      // }
      return classfileBuffer;
    }
  }
}
