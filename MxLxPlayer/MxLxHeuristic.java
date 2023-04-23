package connectx.MxLxPlayer;
import Node;

// The goal of theis class is to abstract away the implementation details of getting and/or calculating the Heuristics on the go
public class MxLxHeuristics {
  Integer H_Connectivity;
  Integer H_Parity;
  Integer H_Dominance;

  // Constructor:
  // Takes a node and either retrieves Heuristics or calculates them directly
  MxLxHeuristics(Node n){
    // TODO: Scegliere e implementare o uno o l'altro per ogni Euristica
    H_Connectivity = get_connectivity(n);
    //H_Connectivity = calculate_connectivity(n);
    
    // ... 
  }
}
