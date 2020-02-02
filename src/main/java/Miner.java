import java.util.ArrayList;

public class Miner extends Thread {
    public static boolean solutionClaimed = false;
    public static int claimerID = -1;
    public static int candidate = Integer.MIN_VALUE;
    public static ArrayList<Boolean> consensusList = new ArrayList<Boolean>();
    public static int minerNum = 0;
    public static String final_nounce;

    private int difficulty;
    private long prevInfo;

    public int index;
    public String solution;

    public Miner(String minerID, long prevInfo, int difficulty) {
        super(minerID);
        index = minerNum;
        minerNum++;
        consensusList.add(false);
        solution = "";
        this.prevInfo = prevInfo;
        this.difficulty = difficulty;

        //System.out.println("Creating miner thread: " + minerID);
    }

    public static boolean numLeading0is(int amount, String hash) {
        boolean result;
        int count = 0;
        for (int i = 0; i < hash.length(); i++) {
            if (hash.charAt(i) == '0') {
                count++;
            } else {
                break;
            }
        }
        result = count == amount;

        return result;
    }

    private boolean consensusAchieved() {
        boolean agree = true;
        for (int i = 0; i < consensusList.size(); i++) {
            if (consensusList.get(i) == false) {
                agree = false;
                break;
            }
        }
        return agree;
    }

    // called from inside the class Miner
    private void resetConsensus() {
        // 1, reset the consensusList to all false
        for (int i = 0; i < consensusList.size(); i++) {
            consensusList.set(i, false);
        }
        // 2, reset candidate
        candidate = Integer.MIN_VALUE;
        // 3, reset solutionClaimed to false
        solutionClaimed = false;
        // 4, reset claimerID to -1
        claimerID = -1;
    }

    // called from outside the class Miner
    public static void reset() {
        solutionClaimed = false;
        claimerID = -1;
        candidate = Integer.MIN_VALUE;
        for (int i = 0; i < consensusList.size(); i++) {
            consensusList.removeAll(consensusList);
        }
        minerNum = 0;
        final_nounce = "";
    }

    @Override
    public void run() {
//      System.out.println("Running miner thread: " + this.getName());
        int nounce = Integer.MIN_VALUE;
        while (!consensusAchieved()) {
            while (!solutionClaimed && !numLeading0is(difficulty, Encryption.sha256("" + nounce + prevInfo))) {
                nounce++;
                if (nounce == Integer.MAX_VALUE
                        && !numLeading0is(difficulty, Encryption.sha256("" + nounce + prevInfo))) {
                    prevInfo++;
                    nounce = Integer.MIN_VALUE;
                }
            }
            if (solutionClaimed) {
                // if someone else claims that a solution is found, verify that
                if (numLeading0is(difficulty, Encryption.sha256("" + candidate + prevInfo))) {
                    consensusList.set(index, true);
                } else {
                    // if this candidate fails the verification
                    resetConsensus();
                }
            } else if (numLeading0is(difficulty, Encryption.sha256("" + nounce + prevInfo))) {
                // if this miner finds a solution, report to the public, and wait for
                // verification
                solutionClaimed = true;
                consensusList.set(index, true);
                candidate = nounce;
                claimerID = index;
            }
        }
        final_nounce = "" + candidate + prevInfo;
        System.out.println("Miner" + (this.index + 1) + "(" + this.getName() + ")" + " has approved that Miner"
                + (claimerID + 1) + " came up with the correct solution: " + "\"" + final_nounce + "\"");
    }

}
