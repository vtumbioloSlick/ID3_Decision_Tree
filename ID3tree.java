import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ID3tree {
   ArrayList<String[]> trainingExampes;
   String[] attributes;
   String[] rules;
   Node root;
   String selectionMeasure;
   ArrayList<Node> leafNodes = new ArrayList<>();
   public ID3tree (ArrayList<String[]> trainingExampes, String[] attributes, String selectionMeasure) {
      this.trainingExampes = trainingExampes;
      this.attributes = attributes;
      this.selectionMeasure = selectionMeasure + "Measure";
      findRoot(trainingExampes, attributes);
      setRules();

   }
   public void findRoot(ArrayList<String[]> examples, String[] initialAttributes) {
      // setup node
      Node node = new Node();
      this.root = node;
      node.setExamples(examples);
      node.setRemainingAttributes(attributes);
      int indexOfBestAttribute = findBestAttribute(examples, initialAttributes, node);
      String bestAttribute = initialAttributes[indexOfBestAttribute];
      node.setName(bestAttribute);
      String proposition = "IF " + bestAttribute;
      node.setRule(proposition);

      //remove attribute
      String[] remainingAttr = new String[initialAttributes.length - 1];
      int j = 0;
      for (int i = 0; i < initialAttributes.length; i++) {
         if (i == indexOfBestAttribute) {
            continue;
         }
         remainingAttr[j] = initialAttributes[i];
         j++;
      }

      // divide examples into value subsets
      HashMap<String, ArrayList<String[]>> subsets = mapValues(node, indexOfBestAttribute);

      // Create children
      setChildren(subsets, node, initialAttributes);

      // Depth first build approach
      for (String key : node.children.keySet()) {
         String expression = " = " + key + " ";
         Node child = node.children.get(key);
         child.setRule(expression);
         ID3(child);

      }

   }

   public void ID3(Node node){
      // setup parameters for base cases
      ArrayList<String[]> examples = node.examples;
      String[] attributes = node.remainingAttributes;

      // base cases
      int x = checkBaseCase(examples, attributes);
      switch (x) {
      case 1: // homogenous subset
         String outcome = examples.get(0)[examples.get(0).length - 1];
         node.setName(outcome);
         node.setRule("THEN " + outcome);
         node.setLeaf();
         this.leafNodes.add(node);
         return;
      case 2: // out of attributes
         String majority = getMajority(node.examples);
         node.setName(majority);
         node.setLeaf();
         node.setRule("THEN " + majority);
         this.leafNodes.add(node);
         return;
      case 3: // no examples left in the set
         String majorityy = getMajority(node.parent.examples);
         node.setName(majorityy);
         node.setLeaf();
         node.setRule("THEN " + majorityy);
         this.leafNodes.add(node);
         return;
      }
      // find best attribute
      int indexOfBestAttribute = findBestAttribute(examples, attributes, node);

      // node already has examples
      node.setName(attributes[indexOfBestAttribute]);

      // remove selected attribute
      String[] remainingAttr = removeAttribute(attributes, indexOfBestAttribute);

      // adjust indexOfBestAttribute to the original attributes list
      int updatedIndexOfBestAttribute = updatedIndex(node);

      // create subsets based on the updated indexOfBestAttribute
      HashMap<String, ArrayList<String[]>> subsets = mapValues(node, updatedIndexOfBestAttribute);

      // Create children
      setChildren(subsets, node, remainingAttr);

      // recurssive call on all the children
      for (String key : node.children.keySet()) {
         Node child = node.children.get(key);
         String predicate =  "^ " + node.name + " = " + key + " ";
         child.setRule(predicate);
         ID3(child);
      }

   }

   public int findBestAttribute(ArrayList<String[]> examples, String[] attributes, Node node) {
      // uses reflection to call invoke the statistical analysis method based on the selection measure field
      try {
         Method m1 = this.getClass().getDeclaredMethod(this.selectionMeasure, ArrayList.class, String[].class, Node.class);
         return (int) m1.invoke(this, examples, attributes, node); // Invoke the method and return the result
      } catch (NoSuchMethodException e) {
         System.out.println("No such method exists: " + this.selectionMeasure);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return -1; // Default return value in case of an error

   }
   // returns the index of the attribute with the lowest entropy
   public int entropyMeasure(ArrayList<String[]> examples, String[] attributes, Node node) {
      int index = 0;
      double entropyTracker = Double.MAX_VALUE;

      for (int i = 0; i < attributes.length; i++) {
         double entropy = entropy(examples, node.remainingAttributes ,i);
         if (entropy < entropyTracker) {
            entropyTracker = entropy;
            index = i;
         }
      }
      return index;
   }
   // returns the index of the attribute with the highest info gain
   public int infoGainMeasure(ArrayList<String[]> examples, String[] holder, Node node) {
      double setEntropy = entropyHelper(examples);
      double tracker = Double.MIN_VALUE;
      int highestIndex = 0;

      for (int k = 0; k < node.remainingAttributes.length; k++) {
         String attr = node.remainingAttributes[k];
         int index = 0;
         // find updated index
         for (int i = 0; i < this.attributes.length; i++) {
            if (this.attributes[i].equals(attr)) {
               index = i;
               break;
            }
         }
         // divide into subsets
         HashMap<String, ArrayList<String[]>> subsets = mapValues(node, index);
         double partialSum = 0;
         Set<String> keys = subsets.keySet();
         for (String key : keys) {
            ArrayList<String[]> subset = subsets.get(key);
            double ratio = Double.valueOf(subset.size()) / Double.valueOf(examples.size());
            partialSum += ratio * entropyHelper(subset);
         }
         double infoGain = setEntropy - partialSum;
         if (infoGain > tracker) {
            tracker = infoGain;
            highestIndex = k;
         }
      }
      return highestIndex;

   }
   public int updatedIndex(Node node) {
      int updatedIndex = 0;
      String name = node.name;
      for (int i = 0; i < this.attributes.length; i++) {
         if (this.attributes[i].equals(name)) {
            updatedIndex = i;
         }
      }
      return updatedIndex;
   }

   public void setChildren(HashMap<String, ArrayList<String[]>> classes, Node node, String[] remainingAttr) {
      Set<String> keys = classes.keySet();
      for (String key : keys) {
         ArrayList<String[]> subset = classes.get(key);
         Node temp = new Node();
         temp.setExamples(subset);
         temp.setRemainingAttributes(remainingAttr);
         temp.setRule(node.getRule().toString());
         node.children.put(key, temp);
         temp.setParent(node);
      }
   }

   public HashMap<String, ArrayList<String[]>> mapValues(Node node, int updatedIndex) {
      // create subsets based on the updated index
      HashMap<String, ArrayList<String[]>> classes = new HashMap<>();
      for (String[] ex : node.examples) {
         // checks if key value pair already exists
         if(!classes.containsKey(ex[updatedIndex])) {
            // sets up a key value pair
            classes.put(ex[updatedIndex], new ArrayList<>());
         }
         // adds the example to the appropriate key
         classes.get(ex[updatedIndex]).add(ex);
      }
      return classes;
   }

   public String[] removeAttribute(String[] attrs, int index) {
      // remove selected attribute
      String[] remainingAttr = new String[attrs.length - 1];
      int j = 0;
      for (int i = 0; i < attrs.length; i++) {
         if( i == index ) {
            continue;
         }
         remainingAttr[j] = attrs[i];
         j++;
      }
      return remainingAttr;
   }

   public double entropy(ArrayList<String[]> examples, String[] remainingAttrs, int attr) {

      String currentAttr = remainingAttrs[attr];
      int tracker = -1;
      for (int i = 0; i < this.attributes.length; i++) {
         if (this.attributes[i].equals(currentAttr)) {
            tracker = i;
            break;
         }
      }

      HashMap<String, ArrayList<String[]>> classes = new HashMap<>();

      for (String[] e : examples) {
         // checks if key value pair already exists
         if(!classes.containsKey(e[tracker])) {
            // sets up a key value pair
            classes.put(e[tracker], new ArrayList<>());
         }
         // adds the example to the appropriate key
         classes.get(e[tracker]).add(e);
      }

      double entropy = 0;
      double denom = Double.valueOf(examples.size());

      Set<String> keys = classes.keySet();
      // Iterate over the keys
      for (String key : keys) {
         ArrayList<String[]> subset = classes.get(key);
         double ratio = Double.valueOf(subset.size()) / denom;
         double value = entropyHelper(subset);
         entropy += ratio * value;
      }

      return entropy;
   }

   public double entropyHelper(ArrayList<String[]> examples) {
      HashMap<String, ArrayList<String[]>> classes = new HashMap<>();

      for (String[] e : examples) {
         // checks if key value pair already exists
         if(!classes.containsKey(e[e.length - 1])) {
            // sets up a key value pair
            classes.put(e[e.length - 1], new ArrayList<>());
         }
         // adds the example to the appropriate key
         classes.get(e[e.length - 1]).add(e);
      }
      double denom = Double.valueOf(examples.size());
      double entropy = 0;

      Set<String> keys = classes.keySet();

      // Iterate over the keys
      for (String key : keys) {
         double ratio = Double.valueOf(classes.get(key).size()) / denom;
         double log = logBase2(ratio);
         entropy -= ratio * log;
      }

      return  entropy;
   }

   public static double logBase2(double value) {
      if (value == 0) {
         return 0;
      }
      return Math.log(value) / Math.log(2);
   }

   public static int checkBaseCase(ArrayList<String[]> examples, String[] attrs) {

      // Empty subset
      if (examples.size() == 0) {
         return 3;
      }

      // check homogenous
      String c = examples.get(0)[examples.get(0).length - 1];
      int count = 0;
      for (String[] e : examples) {
         if (e[e.length - 1].equals(c)) {
            count++;
         }
      }
      if (count == examples.size()) {
         return 1;
      }

      // No more attributes
      if (attrs.length == 0) {
         return 2;
      }

      // base cases not met
      return -1;
   }

   public static String getMajority(ArrayList<String[]> examples) {
      HashMap<String, ArrayList<String[]>> classes = new HashMap<>();
      // Split up examples into subsets
      for (String[] e : examples) {
         // checks if key value pair already exists
         if(!classes.containsKey(e[e.length - 1])) {
            // sets up a key value pair
            classes.put(e[e.length - 1], new ArrayList<>());
         }
         // adds the example to the appropriate key
         classes.get(e[e.length - 1]).add(e);
      }
      // find majority
      Set<String> keys = classes.keySet();
      StringBuilder largest = new StringBuilder();
      int tracker = -1;

      // Iterate over the keys
      for (String key : keys) {
         int size = classes.get(key).size();
         if (size > tracker) {
            tracker = size;
            largest = new StringBuilder(key);
         }
      }
      return largest.toString();
   }

   public void setRules() {
      this.rules = new String[leafNodes.size()];
      for (int i = 0; i < rules.length; i++) {
         rules[i] = leafNodes.get(i).getRule().toString();
      }

   }

}
