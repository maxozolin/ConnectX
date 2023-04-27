package connectx.MxLxPlayer;
import connectx.MxLxPlayer.Node;

public class MxLxDecider {
  // true: if the fist node's position is considered better
  // false: if the second node's position is considered better
  public static boolean compare(Node n1, Node n2){
    Heuristics hr_1 = new Heuristics(n1);
    Heuristics hr_2 = new Heuristics(n2);

    Integer n1_score = compute_with_weights(hr_1);
    Integer n2_score = compute_with_weights(hr_2);

    return n1_score > n2_score; 
  }

  //TODO: 
  //This function should compute the values with the weights based on the Heuristics
  //Heuristic value generations should be based on Nodes, and handeled in the Heuristics class
  //This implementation should not know about how to calculate Heuristics, only how 'Important' it is in the game
  //The weights should be assigned in the MxLxPlayer.Configuration class 
  public static Integer compute_with_weights(Heuristics hr){
    return 1;
  }
}

