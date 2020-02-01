import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class Blockchain {
    public static ArrayList<Block> blockchain = new ArrayList<>(); // The blockchain is implemented as an arraylist of Blocks
    protected static ArrayList<Transaction> transactions = new ArrayList<>();

    public final static int NUM_BLOCKS = 20;
    protected static final int MAX_TXIONS_EACH_PERSON_EACH_EPOCH = 5;
    public final static int MINERS_NUM = 4;
    protected final static int DIFFICULTY = 4;
    private final static int MAX_BLOCKS = 6;

    protected static ArrayList<String> miners_address = new ArrayList<>();

    public static String getMinerAddr() {
        return miners_address.get((int) ((Math.random() * MINERS_NUM)));
    }

    public static Block createGenesisBlock() {
        return new Block(0, "\"Everything starts from here!\"", "0",4);
    }

    public static Block createNextBlock(Block prevBlock) {
        return new Block(prevBlock.index + 1, transactions.toString(), prevBlock.hash,4);
    }

    public static void printChain() {
        for (int i = 0; i < blockchain.size(); i++) {
            System.out.println(blockchain.get(i));
        }
    }

    public static void simulateTransactions() {
        // only work when at least one person holds some coins
        if (!coinHolders_address.isEmpty()) {
            int numTxions = 0;
            for (int i = 0; i < coinHolders_address.size(); i++) {
                numTxions += (int) (MAX_TXIONS_EACH_PERSON_EACH_EPOCH * Math.random());
            }
            for (int i = 0; i < numTxions; i++) {
                simulateAtransaction();
            }
        }
    }

    public static void simulateAtransaction() {
        // randomly pick an sender
        String sender = getSender();
        double issBallence = getBalance(sender);
        double amount = ((int) (issBallence * Math.random() * 100)) / 100.0;
        String recepient = getRecepient(sender);
        transactions.add(new Transaction(sender, recepient, amount));
    }

    public static String getSender() {
        return coinHolders_address.get((int) ((Math.random() * coinHolders_address.size())));
    }

    public static String getRecepient(String sender) {
        String recepient = null;
        double isNewUser = Math.random();
        if (coinHolders_address.size() == 1 || isNewUser < 0.5) {
            recepient = Encryption.sha256("coinHolder" + coinHolders_address.size());
        } else {
            do {
                recepient = coinHolders_address.get((int) ((Math.random() * coinHolders_address.size())));
            } while (recepient == null || recepient.equals(sender));
        }

        return recepient;
    }

    public static double getBalance(String addr) {
        double balance = 0;
        for (int i = 1; i < blockchain.size(); i++) {
            Block currB = blockchain.get(i);
            for (int j = 0; j < Block.BLOCK_SIZE; j++) {
                Transaction currT = currB.getTransactions()[j];
                if (currT == null) {
                    break;
                }
                if (currT.getRecepientID().equals(addr)) {
                    balance += currT.getAmount();
                } else if (currT.getSenderID().equals(addr)) {
                    balance -= currT.getAmount();
                }
            }
        }

        return balance;
    }

    // this PoW algorithm tries to find an integer 'nounce',
    // so that sha256(nounce+previous_block.timestamp) contains the required number
    // of leading 0's.
    public static String proofOFwork(long prevTimestamp) {
        int nounce = Integer.MIN_VALUE;
        while (!numLeading0is(DIFFICULTY, Encryption.sha256("" + nounce + prevTimestamp))) {
            nounce++;
            if (nounce == Integer.MAX_VALUE
                    && !numLeading0is(DIFFICULTY, Encryption.sha256("" + nounce + prevTimestamp))) {
                prevTimestamp++;
                nounce = Integer.MIN_VALUE;
            }
        }

        return ("" + nounce + prevTimestamp);
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

        return result;
    }

    public static void retreiveVerifiedTxions(Transaction[] nextToBeConfirmed) {
        HashMap<String, Double> tempBalanceMap = new HashMap<String, Double>();
        int i = 1;
        while (nextToBeConfirmed[nextToBeConfirmed.length - 1] == null && !transactions.isEmpty()) {
            Transaction curr = transactions.get(0);
            String sender = curr.getSenderID();
            double balance;
            if (tempBalanceMap.containsKey(sender)) {
                balance = tempBalanceMap.get(sender);
            } else {
                balance = getBalance(sender);
                tempBalanceMap.put(sender, balance);
            }
            if (balance < curr.getAmount() || curr.getAmount() == 0.0) {
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

    public static Block mine() {
        Block lastBlock = blockchain.get(blockchain.size() - 1);
        //String miner_address = getMinerAddr();
        Miner.reset();
        for (int i = 0; i < miners_address.size(); i++) {
            Miner mt = new Miner(miners_address.get(i), lastBlock.getTimestamp(), DIFFICULTY);
            miner_threads.add(mt);
            mt.start();
        }
        for (int i = 0; i < miner_threads.size(); i++) {
            try {
                miner_threads.get(i).join();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
            }
        }
        System.out.println();
        miner_threads.removeAll(miner_threads);
        //String nounce;
        //nounce = proofOFwork(lastBlock.getTimestamp());
        Block next = createNextBlock(lastBlock, Miner.final_nounce);
        Transaction nextToBeConfirmed[] = new Transaction[Block.BLOCK_SIZE];
        String miner_address = miners_address.get(Miner.claimerID);
        // rewards to the miner will be the first txion
        nextToBeConfirmed[0] = new Transaction("System", miner_address, MINING_REWARDS, true);
        retreiveVerifiedTxions(nextToBeConfirmed);
        next.setTransactions(nextToBeConfirmed);
        next.setNote("This is Block #" + next.getIndex());
        next.setHash();

        return next;
    }

    public static void main(String args[]) {
        Block newBlock = createGenesisBlock();
        blockchain.add(newBlock);
        miners_address = loadMiners();

        for (int i = 0; i < MAX_BLOCKS; i++) {
            simulateTransactions();
            newBlock = mine();
            blockchain.add(newBlock);
            updateCoinHolders(newBlock);
//          updateTransactions(newBlock);
        }

        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println(blockchainJson);
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
}