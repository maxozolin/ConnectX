package connectx.MxLxPlayer;
import connectx.CXBoard;
import connectx.MxLxPlayer.Node;

public class Tree {
    Node root;
    // setters and getters
    public Tree(CXBoard iboard,boolean iplayer){
      root = new Node(iboard, iplayer);
    }
}
