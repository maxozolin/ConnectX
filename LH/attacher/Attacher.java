import com.sun.tools.attach.VirtualMachine;
class Attacher{
  public static void main(String[] args) {
      System.out.println("Hello Baeldung Reader!");
      //System.out.println(String.join(",",args));
      String pid = args[0];
      System.out.println(pid);

      try{
        VirtualMachine jvm = VirtualMachine.attach(pid);
        jvm.loadAgent("./LH/Launcher.jar");
      } catch (Exception e){
        System.out.println(e.getMessage());
      }
  }
}
