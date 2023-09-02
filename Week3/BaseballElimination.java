import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {
    private static final double FLOATING_POINT_EPSILON = 1.0E-11;
    private String[] team_Nam;
    private int numTeams;
    private int sourceCap;
    private FlowNetwork G;
    private int V;
    private int[] wins;
    private int[] losses;
    private int[] rGames;
    private int[][] gameSch;
    private Map<String, Integer> team_Map = new HashMap<>();
    private Map<String, Set<String>> eliminated_Map = new HashMap<String, Set<String>>();

    public BaseballElimination(String filename)                    // create a baseball division from given filename in format specified below
    {
        if (filename == null) throw new IllegalArgumentException("The input file is empty");

        In in = new In(filename);
        while (!in.isEmpty()) {
            String s = in.readLine();
            if (s.isEmpty()) continue;
            if (isInteger(s)) {
                numTeams = Integer.parseInt(s);// the following loop is used to extract the
                team_Nam = new String[numTeams];
                wins = new int[numTeams];
                losses = new int[numTeams];
                rGames = new int[numTeams];
                gameSch = new int[numTeams][numTeams];


                for (int i = 0; i < numTeams; i++) {
                    String s1 = in.readLine();
                    String[] rst = s1.trim().split("\\s+");
                    team_Map.put(rst[0], i);
                    team_Nam[i] = rst[0];
                    wins[i] = Integer.parseInt(rst[1]);
                    losses[i] = Integer.parseInt(rst[2]);
                    rGames[i] = Integer.parseInt(rst[3]);
                    for (int j = 0; j < numTeams; j++) {
                        gameSch[i][j] = Integer.parseInt(rst[j + 4]);
                    }

                }

            }

        }

        V = numTeams * numTeams + numTeams + 2; // add the two virtual nodes for source and sink
        G = new FlowNetwork(V);
        sourceCap = 0;

    }


    public int numberOfTeams()                        // number of teams
    {
        return numTeams;
    }

    public Iterable<String> teams()                                // all teams
    {
        List<String> teams = new ArrayList<String>();
        for (int i = 0; i < numTeams; i++) {
            teams.add(team_Nam[i]);
        }
        return teams;
    }

    public int wins(String team)                      // number of wins for given team
    {
        if (!team_Map.containsKey(team))
            throw new java.lang.IllegalArgumentException("No such team!");
        return wins[team_Map.get(team)];
    }

    public int losses(String team)                    // number of losses for given team
    {
        if (!team_Map.containsKey(team))
            throw new java.lang.IllegalArgumentException("No such team!");
        return losses[team_Map.get(team)];
    }

    public int remaining(String team)                 // number of remaining games for given team
    {
        if (!team_Map.containsKey(team))
            throw new java.lang.IllegalArgumentException("No such team!");
        return rGames[team_Map.get(team)];
    }

    public int against(String team1, String team2)    // number of remaining games between team1 and team2
    {
        if (!team_Map.containsKey(team1) || !team_Map.containsKey(team2))
            throw new java.lang.IllegalArgumentException("No such team!");
        return gameSch[team_Map.get(team1)][team_Map.get(team2)];
    }

    public boolean isEliminated(String team)              // is given team eliminated?
    {
        if (!team_Map.containsKey(team))
            throw new java.lang.IllegalArgumentException("No such team!");
        if (trivialEliminated(team)) return true;
        return nontrivialEliminated(team);
    }

    private boolean trivialEliminated(String team) {
        for (String teamTemp : this.teams()) {
            if (wins(team) + remaining(team) < wins(teamTemp) && !team.equals(teamTemp)) {
                Set<String> TeamSet = eliminated_Map.get(team);
                if (TeamSet == null) {
                    TeamSet = new HashSet<String>();
                    eliminated_Map.put(team, TeamSet);
                }
                TeamSet.add(teamTemp);
                //System.out.println("Teamset: " + TeamSet);
                //System.out.println("Teamset: " + eliminated_Map.get(team));

                return true;
            }
        }
        return false;

    }

    private void createFlownetwork(String team) {
        G = new FlowNetwork(V);
        int index1, index2, capacity, teamID;
        teamID = team_Map.get(team);
        // the following script is for the links from source (0) to the match nodes
        for (int i = 0; i < numTeams; i++) {
            for (int j = 0; j < numTeams; j++) {
                index1 = 1 + i * numTeams + j;
                if (i >= j || i == teamID || j == teamID)
                    capacity = 0; // assign 0 capacity to the links 1. the lower triangle in the match matrix 2. the studied team
                else capacity = gameSch[i][j];
                sourceCap += capacity;
                if (capacity > 0) {
                    FlowEdge e = new FlowEdge(0, index1, capacity);// create edge from layer 0 to layer 1 (from source node to match nodes)
                    G.addEdge(e);
                }

                // create edge from layer 1 to layer 2, assign infinite capacity to match-team links
                for (int k = 0; k < numTeams; k++) {
                    if ((k == i || k == j) && (i != teamID && j != teamID)) {
                        index2 = k + (1 + numTeams * numTeams);
                        FlowEdge e1 = new FlowEdge(index1, index2, Double.POSITIVE_INFINITY);
                        G.addEdge(e1);
                    }

                }

            }
        }
        // add edges from team nodes to the sink node
        for (int k = 0; k < numTeams; k++) {
            index2 = k + (1 + numTeams * numTeams);
            capacity = wins(team) + remaining(team) - wins[k];
            if (capacity < 0 || k == teamID)
                capacity = 0; // for the trivial case, this capacity value will be less than 0, then will be automatically converted to 0; also need to exclude the out-link capacity to the sink point
            if (capacity > 0) {
                FlowEdge e = new FlowEdge(index2, V - 1, capacity);// create edge from layer 2 to layer 3
                G.addEdge(e);
            }
        }
    }

    private boolean nontrivialEliminated(String team) {
        boolean Eliminated = false;
        createFlownetwork(team); // create the flow network
        FordFulkerson maxflow = new FordFulkerson(G, 0, V - 1);
        int teamIndex = numTeams * numTeams + 1;
        for (int v = teamIndex; v < G.V(); v++) {
            if (maxflow.inCut(v)) {
                Eliminated = true;
                Set<String> TeamSet = eliminated_Map.get(team);
                if (TeamSet == null) {
                    TeamSet = new HashSet<String>();
                    eliminated_Map.put(team, TeamSet);
                }
                TeamSet.add(team_Nam[v - teamIndex]);
            }
        }

        return Eliminated;
    }

    public Iterable<String> certificateOfElimination(String team)  // subset R of teams that eliminates given team; null if not eliminated
    {
        if (!team_Map.containsKey(team))
            throw new java.lang.IllegalArgumentException("No such team!");
        if (isEliminated(team)) {
            return eliminated_Map.get(team);

        } else return null;
    }

    //the following private helper function is design to determine if a string is a integer or not.
    private static boolean isInteger(String input) {
        boolean flag = true;
        for (int a = 0; a < input.length(); a++) {
            if (a == 0 && input.charAt(a) == '-')
                continue;
            if (!Character.isDigit(input.charAt(a)))
                flag = false;
        }
        return flag;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
