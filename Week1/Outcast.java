import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
public class Outcast {
    private final WordNet wordnet;

    public Outcast(WordNet wordnet)         // constructor takes a WordNet object
    {
        this.wordnet = wordnet;
    }

    public String outcast(String[] nouns)   // given an array of WordNet nouns, return an outcast
    {
        int n = nouns.length;
        int maxDist = 0;
        String outcast = null;

        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int j = 0; j < n; j++) {
                sum = sum + this.wordnet.distance(nouns[i], nouns[j]);
            }
            if (sum > maxDist) {
                maxDist = sum;
                outcast = nouns[i];
            }

        }

        return outcast;
    }


    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));

        }
    }
}
