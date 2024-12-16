import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Set;

public class Main {
   public static void main (String[] args) {

      Scanner sc = new Scanner(System.in);
      System.out.println("Choose Selection Measure: 1 for info gain, 2 for entropy");
      int choice = sc.nextInt();
      StringBuilder sb = new StringBuilder();
      if (choice == 1) {
         sb = new StringBuilder("infoGain");
      } else if (choice == 2) {
         sb = new StringBuilder("entropy");
      } else {
         System.out.println("Invalid input");
         System.exit(0);
      }


      ArrayList<String[]> trainingExamples = processExamples("Tennis-Train.txt");
      ArrayList<String[]> testingExamples = processExamples("Tennis-Test.txt");
      ArrayList<String[]> extendedExamples = processExamples("Tennis-Set-2.txt");
      String[] attrs = {"Outlook", "Temprature", "Humidity", "Wind"};
      ID3tree tree = new ID3tree(extendedExamples, attrs, sb.toString());
      String[] rules = tree.rules;
      testRules(rules, extendedExamples);
      for (String rule : rules) {
         System.out.println(rule);
      }

   }

   public static long duration(long start, long end) {
      long duration = end - start;

      return duration;
   }

   public static Set<String> setKeywords(ArrayList<String[]> examples) {
      Set<String> keywords = new HashSet<>();
      for(String[] example : examples) {
         for (String value : example) {
            keywords.add(value);
         }
      }
      return keywords;
   }

   public static void preorder(Node n, int level) {
      // Print the node name along with its level
      System.out.println("Level " + level + ": " + n.name);
      if (n.isLeaf()) {
         System.out.println(n.getRule());
      }

      // Recursively call preorder on each child, increasing the level by 1
      for (String key : n.children.keySet()) {
         preorder(n.children.get(key), level + 1);
      }

   }

   public static void testRules(String[] rules, ArrayList<String[]> trainingExamples) {
      double correctCount = 0.0;
      ArrayList<String[]> testingExamples = processExamples("Tennis-Test.txt");
      String[] filteredRules =  processRules(rules, trainingExamples);

      // test against rules for each testing example
      for (String[] example : testingExamples) {
         // check each rule to see which rule is applicable
         for (String rule : filteredRules) {
            String[] ruleValues = rule.split(" ");
            int valueCounter = 0;
            // Check to see if all rule preconditions are met
            for (int i = 0; i < ruleValues.length - 1; i++) {
               String value = ruleValues[i];
               if (contains(example, value)) {
                  valueCounter++;
               }
            }
            if (valueCounter == ruleValues.length - 1) {
               String prediction = ruleValues[ruleValues.length - 1];
               String exampleOutcome = example[example.length - 1];
               if (prediction.equals(exampleOutcome)) {
                  correctCount++;
               }
            }

         }
      }
      double accuracy = correctCount / Double.valueOf(testingExamples.size());
      accuracy *= 100;
      System.out.println("Accuracy: " +   accuracy + "%");

   }

   public static boolean contains(String[] list, String keyword) {
      for (int i = 0; i < list.length; i++) {
         if (keyword.equals(list[i])) {
            return true;
         }
      }
      return false;
   }

   public static String[] processRules(String[] rules, ArrayList<String[]> examples) {
      String[] filteredRules = new String[rules.length];
      Set<String> keywords = setKeywords(examples);
      for (int i = 0; i < rules.length; i++) {
         String rule = rules[i];
         StringBuilder filteredRule = new StringBuilder();
         String[] parts = rule.split(" ");
         for (String part : parts) {
            if (keywords.contains(part)) {
               filteredRule.append(part + " ");
            }
         }
         filteredRule.deleteCharAt(filteredRule.length() - 1);
         filteredRules[i] = filteredRule.toString();
      }
      return filteredRules;
   }

   public static void makePrediction(Node n) {
      ArrayList<String[]> testingExamples = processExamples("Tennis-Test.txt");
      // store classifications
      ArrayList<String> outcomes = new ArrayList<>();
      for (String[] e : testingExamples) {
         if (!outcomes.contains(e[e.length - 1])) {
            outcomes.add(e[e.length - 1]);
         }
      }
      // track correct count for accuracy calculation
      double correctCount = 0;
      // get the attributes list
      String[] attributes = n.remainingAttributes;
      // for every example
      for (String[] e : testingExamples) {
         // tracker node
         Node current = n;
         // iterate down the tree until a leaf node is reached
         while (true) {
            // handle leaf nodes
            if (outcomes.contains(current.name)) {
               // make comparison
               if (e[e.length - 1].equals(current.name)) {
                  correctCount++;
               }
               // move on to next example
               break;

            }
            // Non leaf nodes

            // find attr index
            int index = 0;
            for (int i = 0; i < attributes.length; i++) {
               String attr = current.name;
               if (attributes[i].equals(attr)) {
                  index = i;
                  break;
               }
            }
            String key = e[index];
            current = current.children.get(key);

         }
      }
      double accuracy = correctCount / Double.valueOf(testingExamples.size());
      System.out.println(accuracy * 100 + "%");


   }
   public static ArrayList processExamples(String fileName) {
      // initiate examples list
      ArrayList<String[]> examples = new ArrayList<>();

      // read the file
      try (Scanner scan = new Scanner(Paths.get(fileName))) {
         while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] parts = line.split(" ");
            examples.add(parts);

         }

      } catch (Exception e) {
         System.out.println(e.getMessage());
      }

      return examples;
   }

   public static void printExamples(ArrayList<String[]> examples) {
      for (String[] s : examples) {
         for (String e : s) {
            System.out.print(e + " ");
         }
         System.out.println();
      }

   }
}
