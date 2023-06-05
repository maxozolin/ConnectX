import com.sun.tools.attach.VirtualMachine;
class Attacher{
  public static void main(String[] args) {
      System.err.println("Hello Baeldung Reader!");
      //System.err.println(String.join(",",args));
      String pid = args[0];
      System.err.println(pid);

      try{
        VirtualMachine jvm = VirtualMachine.attach(pid);
        jvm.loadAgent("./MxLxHacker/Launcher.jar");
      } catch (Exception e){
        System.err.println(e.getMessage());
      }
  }
}
