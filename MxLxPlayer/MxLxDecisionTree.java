package connectx.MxLxPlayer;

import connectx.CXBoard;
import connectx.MxLxPlayer.Node;
import java.util.Collections;
import java.util.List;

public class MxLxDecisionTree {
  private Integer current_depth;
  public Tree tree;
  /*
   Creates the decision Tree with specified depth

   depth: How many moves to consider
   first: If you are player1
   board: current board state
  */

  public MxLxDecisionTree(CXBoard B, boolean first, Integer depth) {
    tree = makeDecisionTree(B, first, depth);
    current_depth = depth;
  }

  /*
   Populates the Tree's base layer with an additional layer of moves
   Basically increases depth of search tree by 1

   leaf: A leaf node to expand
  */
  private void expandLeaf(Node leaf) {
    for (int i = 0; i < leaf.board.N; i++) {
      CXBoard new_b = leaf.board.copy();
      new_b.markColumn(i);
      Node new_node = new Node(new_b, !leaf.player);
      new_node.parent = leaf;

      leaf.children.add(new_node);
    }
  }

  /*
   Populates the Tree's base layer with an additional layer of moves
   Basically increases depth of search tree by 1

   nd: Node of the tree from which to expand
  */
  private void expandNode(Node nd) {
    if (nd.isLeaf()) {
      expandLeaf(nd);
    } else {
      for (Node child : nd.children) {
        expandNode(child);
      }
    }
  }

  /*
    Returns a Tree datastructure populated with all the possible moves as Nodes from current
    board state

    depth: How many moves to consider
    first: If you are player1
    board: current board state
  */
  private Tree makeDecisionTree(CXBoard B, boolean first, Integer depth) {
    Tree t = new Tree(B, first);
    for (int i = 0; i < depth; i++) {
      expandNode(t.root);
    }
    return t;
  }
}
