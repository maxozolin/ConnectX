package connectx.MxLxPlayer;
import java.util.List;
import connectx.CXBoard;
import java.util.ArrayList;

public class Node {
    CXBoard board;
    boolean player;
    List<Node> children;
    
    public Node(CXBoard iboard,boolean iplayer){
      board = iboard;
      player = iplayer;
      children = new ArrayList<Node>();
    }

    public boolean is_leaf(){
      return children.size() == 0;
    }
}
