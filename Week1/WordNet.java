import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class WordNet {
    private final Digraph G;
    private final SAP sap;
    private final Map<String, Set<Integer>> hypermap_Noun = new HashMap<>();
    private final Map<Integer, String> hypermap_ID = new HashMap<>();

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        validatePara(synsets);
        validatePara(hypernyms);
        int ID = 0;
        int V = 0;
        In in_syn = new In(synsets); // read synsets file
        while (!in_syn.isEmpty()) {
            String s = in_syn.readLine();
            String[] result1 = s.split(",");
            ID = Integer.parseInt(result1[0]);   //ID
            hypermap_ID.put(ID, result1[1]);  // add ID and synNoun to the Map relationship
            String[] result2 = result1[1].trim().split("\\s+");//synset might contain several words
            int n = result2.length;

            for (int i = 0; i < n; i++) {
                Set<Integer> synID_Set = new HashSet<Integer>();
                if (hypermap_Noun.containsKey(result2[i])) {
                    synID_Set = hypermap_Noun.get(result2[i]);
                    synID_Set.add(ID);
                    hypermap_Noun.put(result2[i], synID_Set);
                } else {
                    synID_Set.add(ID);
                    hypermap_Noun.put(result2[i], synID_Set);
                }
            }
        }
        // Create the Digraph based on the hypernyms.txt
        V = ID + 1;
        this.G = new Digraph(V);
        In in_hyper = new In(hypernyms); // read synsets file
        while (!in_hyper.isEmpty()) {
            String s = in_hyper.readLine();
            String[] IDs = s.trim().split(",");
            int n = IDs.length;
            int v = Integer.parseInt(IDs[0]);   //ID
            //System.out.println(s + "-" + n + "-" + v);
            for (int i = 1; i < n; i++) {
                int w = Integer.parseInt(IDs[i]);
                this.G.addEdge(v, w);
            }
        }
        validateDAG(G);
        this.sap = new SAP(this.G);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return this.hypermap_Noun.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        validatePara(word);
        return this.hypermap_Noun.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        validatePara(nounA);
        validatePara(nounB);
        return this.sap.length(this.hypermap_Noun.get(nounA), this.hypermap_Noun.get(nounB));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        validatePara(nounA);
        validatePara(nounB);
        int ID = this.sap.ancestor(this.hypermap_Noun.get(nounA), this.hypermap_Noun.get(nounB));
        return this.hypermap_ID.get(ID);
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateDAG(Digraph G) {
        // use topologcial method to detect if the cycle exist
        if (isCyclic(G)) throw new IllegalArgumentException("The input Digraph contains Cycle");
        // loop through the node to check if there are more than 1 root node
        int rootNum = 0;
        for (int v = 0; v < G.V(); v++) {
            if (G.outdegree(v) == 0) rootNum++;
        }
        if (rootNum != 1) throw new IllegalArgumentException("The input Digraph contains multiple/None roots");
    }

    private void validateNoun(String noun) {
        if (!isNoun(noun)) throw new IllegalArgumentException("Please input the word in the Wordnet");
    }

    private void validatePara(String para) {
        if (para == null) throw new IllegalArgumentException("The input is null");
    }

    private boolean isCyclic(Digraph G) {
        DirectedCycle dc = new DirectedCycle(G);
        if (dc.hasCycle()) return true;
        return false;

        /*
        boolean[] visited = new boolean[G.V()];
        boolean[] recStack = new boolean[G.V()];

        for (int i = 0; i < G.V(); i++)
            if (isCyclicUtil(i, visited, recStack))
                return true;
        return false;*/

    }

    // Function to check if cycle exists
    private boolean isCyclicUtil(int v, boolean[] visited, boolean[] recStack) {
        if (recStack[v])
            return true;
        if (visited[v])
            return false;
        visited[v] = true;
        recStack[v] = true;
        for (Integer w : G.adj(v))
            if (isCyclicUtil(v, visited, recStack))
                return true;
        recStack[v] = false;
        return false;
    }

    public static void main(String[] args) {
        String synsets = args[0];
        String hypernyms = args[1];
        WordNet wordNet = new WordNet(synsets, hypernyms);
        while (!StdIn.isEmpty()) {
            String v = StdIn.readString();
            String w = StdIn.readString();
            if (!wordNet.isNoun(v)) {
                StdOut.println(v + "not in the word net!");
                continue;
            }
            if (!wordNet.isNoun(w)) {
                StdOut.println(w + "not in the word net!");
                continue;
            }
            int distance = wordNet.distance(v, w);
            String ancestor = wordNet.sap(v, w);
            StdOut.printf("distance = %d, ancestor = %s\n", distance, ancestor);
        }


    }
}
