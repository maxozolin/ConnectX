package connectx.MxLxPlayer;

import connectx.MxLxPlayer.Node;
import connectx.CXBoard;
import java.util.Collections;
import java.util.List;

public class MxLxDecisionTree{
  private Integer current_depth;
	private Tree tree;
  /* Default empty constructor */
	public MxLxDecisionTree(CXBoard B, boolean first, Integer depth) {
    tree = _make_decision_tree(B, first, depth);
    current_depth = depth;
	}

  private void epxand_leaf(Node leaf){
    for(int i=0;i<leaf.board.N;i++){
      CXBoard new_b = leaf.board.copy();
      new_b.markColumn(i);
      Node new_node = new Node(new_b,!leaf.player);
      leaf.children.add(new_node);
    }
  }

  private void expand_node(Node nd){
    if (nd.is_leaf()){
      epxand_leaf(nd);
    }
    else {
      for (Node child : nd.children){
        expand_node(child);
      }
    }
  }

	private Tree _make_decision_tree(CXBoard B, boolean first, Integer depth) {
    Tree t = new Tree(B, first);
    Node current_node = t.root;
    for(int i=0;i<depth;i++){
      expand_node(t.root);
    }
    return t;
  }
}
