package connectx.MxLxPlayer;

import connectx.MxLxPlayer.MxLxDecisionTree;
import connectx.MxLxPlayer.Node;
import connectx.MxLxPlayer.Tree;

public class TreePrinter {
  public static String _get_node_name(Node node) {
    //return "*".toString();
    return String.format("%s", node.parent);
  }

  public static void traversePreOrder(StringBuilder sb, String padding, String pointer, Node node) {
    if (node != null) {
      sb.append(padding);
      sb.append(pointer);
      sb.append(_get_node_name(node));
      sb.append("\n");

      StringBuilder paddingBuilder = new StringBuilder(padding);
      paddingBuilder.append(" ");
      String paddingForBoth=paddingBuilder.toString();

      //String pointerLeft = (node.getRight() != null) ? "├──" : "└──";
      String pointerLeft = "└- ";

      for (int i = 0; i < node.children.size(); i++) {
        traversePreOrder(sb, paddingForBoth, pointerLeft, node.children.get(i));
      }
    }
  }

  // Maybe add a name getter callable with signature (node) -> String as parameter
  public static void printTree(MxLxDecisionTree decison_tree) {
    StringBuilder sb = new StringBuilder();
    traversePreOrder(sb,"","", decison_tree.tree.root);
    System.out.println(sb.toString());
  }
}
