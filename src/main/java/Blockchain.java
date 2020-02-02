import com.google.gson.GsonBuilder;
import com.sun.java.util.jar.pack.FixedList;
import jdk.internal.org.objectweb.asm.tree.InsnList;

import javax.management.openmbean.TabularDataSupport;
import java.util.ArrayList;
import java.util.HashMap;

public class Blockchain {
    private static final double MINING_REWARDS = 100;
    public static ArrayList<Block> bc = new ArrayList<>();
    // The blockchain is implemented as an ArrayList of Blocks
    public static ArrayList<Transaction> transactions = new ArrayList<>();
    private static ArrayList<String> miners_address = new ArrayList<>();

    public final static int NUM_BLOCKS = 20;
    //comment gareko sabai variable/function tyo website ma nabhako haru ho
    //MINERS_NUM bhannale number of miners hola so 5 rakheko chhu
    public final static int MINERS_NUM = 5;
    private static final int MAX_TRANSACTIONS_EACH_PERSON_EACH_EPOCH = 5;
    private final static int MAX_BLOCKS = 6;
    private final static int DIFFICULTY = 4;
    //yo data type ho ki nai sure chhaina
    private final static TabularDataSupport coinHolders_address = null;

    public static Block createGenesisBlock() {
        return new Block(0, "\"Everything starts from here!\"", "0",4);
    }

    public static Block createNextBlock(Block prevBlock, String nonce, int difficulty) {
        //yaha bata transaction.toString() hatako chhu
        return new Block(prevBlock.index + 1, prevBlock.hash, nonce, difficulty);
    }

    public static void printChain() {
        for (Block block : bc) {
            System.out.println(block);
        }
    }

    public static void simulateTransactions() {
        // only work when at least one person holds some coins
        //yo coinholders_address bhaneko defined chhaina ani nabhaye ni hunchha
        if (!coinHolders_address.isEmpty()) {
            int numTxions = 0;
            for (int i = 0; i < coinHolders_address.size(); i++) {
                numTxions += (int) (MAX_TRANSACTIONS_EACH_PERSON_EACH_EPOCH * Math.random());
            }
            for (int i = 0; i < numTxions; i++) {
                simulateAtransaction();
            }
        }
    }

    public static void simulateAtransaction() {
        // randomly pick an sender
        String sender = getSender();
        double issBalance = getBalance(sender);
        double amount = ((int) (issBalance * Math.random() * 100)) / 100.0;
        String recipient = getRecipient(sender);
        transactions.add(new Transaction(sender, recipient, amount,true));
    }

    public static String getSender() {
        //string ma cast aafai gareko ho
        return (String) coinHolders_address.get((int) ((Math.random() * coinHolders_address.size())));
    }

    public static String getRecipient(String sender) {
        String recipient = null;
        double isNewUser = Math.random();
        if (coinHolders_address.size() == 1 || isNewUser < 0.5) {
            recipient = Encryption.sha256("coinHolder" + coinHolders_address.size());
        } else {
            do {
                //yaha cast o string gareko ho
                recipient = (String) coinHolders_address.get((int) ((Math.random() * coinHolders_address.size())));
            } while (recipient == null || recipient.equals(sender));
        }

        return recipient;
    }

    public static double getBalance(String addr) {
        double balance = 0;
        for (int i = 1; i < bc.size(); i++) {
            Block currB = bc.get(i);
            for (int j = 0; j < Block.BLOCK_SIZE; j++) {
                //yaha yo getTransactions bhanne function block class ma huna parne tara chhaina
                Transaction currT = currB.getTransactions()[j];
                if (currT == null) {
                    break;
                }
                //ani yo getRecipientID bhanne function ni chhain raichha
                //yeso herda yo Transaction class incomplete chha
                //get sender ra get recipient bhanne function tah chha eso herda id nai pathauchha
                //yo function lai transaction class ma sarera herchhu
                if (currT.getRecepientID().equals(addr)) {
                    balance += currT.getAmount();
                } else if (currT.getSenderID().equals(addr)) {
                    balance -= currT.getAmount();
                }
            }
        }

        return balance;
    }

    public static String getMinerAddr() {
        return miners_address.get((int) ((Math.random() * MINERS_NUM)));
    }

    public static void updateCoinHolders(Block block) {
        for (int i = 0; i < Block.BLOCK_SIZE; i++) {
            Transaction curr = block.getTransactions()[i];
            if (curr == null) {
                break;
            } else {
                addCoinHolder(curr.getRecepientID());
            }
        }
    }

    private static void addCoinHolder(Object recepientID) {
        //yo ni nabhako function ho
        //afai banako
    }

    public static void retreiveVerifiedTxions(Transaction[] nextToBeConfirmed) {
        HashMap<String, Double> tempBalanceMap = new HashMap<String, Double>();
        int i = 1;
        while (nextToBeConfirmed[nextToBeConfirmed.length - 1] == null && !transactions.isEmpty()) {
            Transaction curr = transactions.get(0);
            //same get sender id function chhaina
            String sender = curr.getSenderID();
            double balance;
            if (tempBalanceMap.containsKey(sender)) {
                balance = tempBalanceMap.get(sender);
            } else {
                balance = getBalance(sender);
                tempBalanceMap.put(sender, balance);
            }
            //get amount bhanne function ni chhaina rahechha
            //get Amount ko sato get Balance bhanne function chhai chha
            if (balance < curr.getAmount() || curr.getAmount() == 0.0) {
                //set verified function ni transaction ma huna parne
                curr.setVerified(false);
            } else {
                curr.setVerified(true);
                nextToBeConfirmed[i] = new Transaction(curr.getSenderID(), curr.getRecepientID(), curr.getAmount(),
                        true);
                i++;
                balance -= curr.getAmount();
                tempBalanceMap.put(sender, balance);
            }
            transactions.remove(curr);
        }
    }

    public static boolean numLeading0is(int amount, String hash) {
        boolean result = true;
        int count = 0;
        for (int i = 0; i < hash.length(); i++) {
            if (hash.charAt(i) == '0') {
                count++;
            } else {
                break;
            }
        }
        if (count != amount) {
            result = false;
        }

        return !result;
    }

    // this PoW algorithm tries to find an integer 'nounce',
    // so that sha256(nounce+previous_block.timestamp) contains the required number
    // of leading 0's.
    public static String proofOFwork(long prevTimestamp) {
        int nounce = Integer.MIN_VALUE;
        while (numLeading0is(DIFFICULTY, Encryption.sha256("" + nounce + prevTimestamp))) {
            nounce++;
            if (nounce == Integer.MAX_VALUE
                    && numLeading0is(DIFFICULTY, Encryption.sha256("" + nounce + prevTimestamp))) {
                prevTimestamp++;
                nounce = Integer.MIN_VALUE;
            }
        }

        return ("" + nounce + prevTimestamp);
    }

    public static Block mine() {
        Block lastBlock = bc.get(bc.size() - 1);
        //String miner_address = getMinerAddr();
        Miner.reset();
        FixedList<Miner> miner_threads;
        for (String minersAddress : miners_address) {
            Miner mt = new Miner(minersAddress, lastBlock.getTimestamp(), DIFFICULTY);
            miner_threads.add(mt);
            mt.start();
        }
        for (Miner miner_thread : miner_threads) {
            try {
                miner_thread.join();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
            }
        }
        System.out.println();
        miner_threads.removeAll(miner_threads);
        //String nounce;
        //nounce = proofOFwork(lastBlock.getTimestamp());
        Block next = createNextBlock(lastBlock, Miner.final_nounce,4);
        Transaction[] nextToBeConfirmed = new Transaction[Block.BLOCK_SIZE];
        String miner_address = miners_address.get(Miner.claimerID);
        // rewards to the miner will be the first txion
        nextToBeConfirmed[0] = new Transaction("System", miner_address, MINING_REWARDS, true);
        retreiveVerifiedTxions(nextToBeConfirmed);
        next.setTransactions(nextToBeConfirmed);
        next.setNote("This is Block #" + next.getIndex());
        next.setHash();

        return next;
    }

    public static ArrayList<String> loadMiners(){
        //yaha return miner ko array list ho
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        Block newBlock = createGenesisBlock();
        bc.add(newBlock);
        //yo load miners bhanne function le miner ko id arraylist banauchhha aru kei kaam gardaina re tara yo chhaina
        miners_address = loadMiners();

        for (int i = 0; i < MAX_BLOCKS; i++) {
            simulateTransactions();
            newBlock = mine();
            bc.add(newBlock);
            updateCoinHolders(newBlock);
//          updateTransactions(newBlock);
        }

        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(bc);
        System.out.println(blockchainJson);
    }

}