import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class SAP {
    private static final int INFINITY = Integer.MAX_VALUE;
    private Digraph G;
    private int[] marked;      // marked[v] = is there an s-v path
    private int[] edgeTo1;      // edgeTo[v] = previous edge on shortest s-v path
    private int[] distTo1;      // distTo[v] = number of edges shortest s-v path
    private int[] edgeTo2;      // edgeTo[v] = previous edge on shortest s-v path
    private int[] distTo2;      // distTo[v] = number of edges shortest s-v path
    private Stack<Integer> stack;
    private int ancestor;
    private Map<String, int[]> cache;
    //private final HashMap<HashSet<Integer>, int[]> cache;


    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException("null graph");
        }
        this.G = G;
        this.marked = new int[G.V()];
        this.distTo1 = new int[G.V()];
        this.edgeTo1 = new int[G.V()];
        this.distTo2 = new int[G.V()];
        this.edgeTo2 = new int[G.V()];
        stack = new Stack<Integer>();
        this.ancestor = -1;
        cache = new HashMap<>();
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        Queue<Integer> qv = new Queue<Integer>();
        Queue<Integer> qw = new Queue<Integer>();
        int distMax = Integer.MAX_VALUE;
        int distance = 0;
        String key = v + "-" + w;
        //HashSet<Integer> key = new HashSet<>();
        //key.add(v);
        //key.add(w);
        int[] value = new int[2];
        if (cache.containsKey(key))
            return cache.get(key)[0];

        if (v == w) {
            this.ancestor = v;
            return distance;
        }
        this.ancestor = -1;
        distTo1[v] = 0;
        marked[v] = 1;
        distTo2[w] = 0;
        marked[w] = 2;
        qv.enqueue(v);
        qw.enqueue(w);
        stack.push(v);
        stack.push(w);

        while (!qv.isEmpty() || !qw.isEmpty()) {
            if (!qv.isEmpty()) {
                int v1 = qv.dequeue();
                for (int w1 : G.adj(v1)) {
                    if (marked[w1] == 0) {
                        edgeTo1[w1] = v1;
                        distTo1[w1] = distTo1[v1] + 1;
                        marked[w1] = 1;
                        qv.enqueue(w1);
                        stack.push(w1);
                    } else if (marked[w1] == 2) {
                        edgeTo1[w1] = v1;
                        distTo1[w1] = distTo1[v1] + 1;
                        marked[w1] = 3;
                        qv.enqueue(w1);
                        int distTemp = distTo1[w1] + distTo2[w1];
                        if (distTemp < distMax) {
                            distMax = distTemp;
                            this.ancestor = w1;
                            distance = distTemp;
                        }
                        if (distTo1[w1] >= distMax) {
                            init();
                            value[0] = distance;
                            value[1] = this.ancestor;
                            cache.put(key, value);
                            return distance;
                        }
                    }
                }
            }
            if (!qw.isEmpty()) {
                int v2 = qw.dequeue();
                for (int w2 : G.adj(v2)) {
                    if (marked[w2] == 0) {
                        edgeTo2[w2] = v2;
                        distTo2[w2] = distTo2[v2] + 1;
                        marked[w2] = 2;
                        qw.enqueue(w2);
                        stack.push(w2);
                    } else if (marked[w2] == 1) {
                        edgeTo2[w2] = v2;
                        distTo2[w2] = distTo2[v2] + 1;
                        marked[w2] = 3;
                        qw.enqueue(w2);
                        int distTemp = distTo1[w2] + distTo2[w2];
                        if (distTemp < distMax) {
                            distMax = distTemp;
                            this.ancestor = w2;
                            distance = distTemp;
                        }
                        if (distTo2[w2] >= distMax) {
                            init();
                            value[0] = distance;
                            value[1] = this.ancestor;
                            cache.put(key, value);
                            return distance;
                        }
                    }
                }
            }

        }
        init();
        if (distance > 0) {
            value[0] = distance;
            value[1] = this.ancestor;
            cache.put(key, value);
            return distance;
        } else {
            value[0] = -1;
            value[1] = -1;
            cache.put(key, value);
            this.ancestor = -1;
            return -1;
        }
    }


    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        String key = v + "-" + w;

        //HashSet<Integer> key = new HashSet<>();
        //key.add(v);
        //key.add(w);
        if (cache.containsKey(key)) {
            //cache.remove(key);
            return cache.get(key)[1];
        }
        int len = length(v, w);
        //System.out.println(v + "-" + w + "-" + len);
        return this.ancestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        validateVertices(v);
        validateVertices(w);
        //for (int s : v) {
        //    System.out.println(s);
        // }
        //String key = v.toString() + "_" + w.toString();
        this.ancestor = -1;
        int dist = Integer.MAX_VALUE;
        int ancestorTemp = -1;
        for (int v1 : v) {
            for (int w1 : w) {
                int len = length(v1, w1);
                if (len < 0) continue;
                if (len < dist) {
                    dist = len;
                    ancestorTemp = ancestor(v1, w1);
                }
            }
        }
        this.ancestor = ancestorTemp;
        //cache.put(key, this.ancestor);
        return dist;
    }


    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        validateVertices(v);
        validateVertices(w);
        //String key = v.toString() + "_" + w.toString();
        /*
        if (cache.containsKey(key)) {
            int p = cache.get(key);
            cache.remove(key);
            return p;
        }*/
        int len = length(v, w);
        return this.ancestor;
    }

    // init auxiliary marked array for bfs
    private void init() {
        while (!stack.isEmpty()) {
            int v = stack.pop();
            marked[v] = 0;
        }
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        int V = marked.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
    }

    // throw an IllegalArgumentException if vertices is null, has zero vertices,
    // or has a vertex not between 0 and V-1
    private void validateVertices(Iterable<Integer> vertices) {
        if (vertices == null) {
            throw new IllegalArgumentException("argument is null");
        }
        int vertexCount = 0;
        for (Integer v : vertices) {
            vertexCount++;
            if (v == null) {
                throw new IllegalArgumentException("vertex is null");
            }
            validateVertex(v);
        }
        if (vertexCount == 0) {
            throw new IllegalArgumentException("zero vertices");
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);

        /*

        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
        */
        /*int[] arrv = {49952, 46599, 46600, 28075, 68015};
        int[] arrw = {49952, 46599, 46600, 28075, 68015};*/
        int[] arrv = {49952};
        int[] arrw = {49952, 49952, 46599, 46600, 28075, 68015};

        // After Java 8
        Iterable<Integer> v = () -> new Iterator<Integer>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return arrv.length > index;
            }

            @Override
            public Integer next() {
                return arrv[index++];
            }
        };

        // After Java 8
        Iterable<Integer> w = () -> new Iterator<Integer>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return arrw.length > index;
            }

            @Override
            public Integer next() {
                return arrw[index++];
            }
        };


        int length = sap.length(v, w);
        int ancestor = sap.ancestor(v, w);
        StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
    }
}
