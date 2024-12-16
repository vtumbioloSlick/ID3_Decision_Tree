import java.util.ArrayList;
import java.util.HashMap;
public class Node {
   String name = "test";
   ArrayList<String[]> examples = new ArrayList<>();
   HashMap<String, Node> children = new HashMap<>();
   Node parent;
   String[] remainingAttributes;
   private boolean leaf = false;
   private StringBuilder rule = new StringBuilder();

   public Node() {

   }
   public Node(String name, ArrayList<String[]> examples) {
      this.name = name;
      this.examples = examples;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setExamples(ArrayList<String[]> examples) {
      this.examples = examples;
   }

   public void setParent(Node parent) {
      this.parent = parent;
   }

   public void setRemainingAttributes(String[] attr) {
      this.remainingAttributes = attr;
   }

   public void setRule(String proposition) {
      this.rule.append(proposition);
   }

   public StringBuilder getRule() {
      return this.rule;
   }

   public void setLeaf() {
      this.leaf = true;
   }

   public boolean isLeaf() {
      return this.leaf;
   }
}
