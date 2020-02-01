import java.util.Date;

import lombok.Getter;

@Getter
public class Block {
    protected int index;
    protected long timestamp;
    protected String note;
    protected Transaction transactions[];
    protected String prev_hash;
    protected String hash;
    protected int difficulty;
    protected String nounce;

    public static final int BLOCK_SIZE = 20;

    public Block(int index, String prev_hash, String nounce, int difficulty) {
        this.index = index;
        this.timestamp = new Date().getTime();
        this.prev_hash = prev_hash;
        this.hash = calcHash();
        this.nounce = nounce;
        this.difficulty = difficulty;
        transactions = new Transaction[BLOCK_SIZE];
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTransactions(Transaction[] transactions) {
        // make a deep copy
        for (int i = 0; i < this.transactions.length; i++) {
            this.transactions[i] = transactions[i];
        }
    }

    public String toString() {
        return ("Block #" + this.index + "\n\tmined at: " + this.timestamp + "\n\tNote: " + this.note
                + "\n\tTransactions: " + this.transactions + "\n\tHash: {" + this.hash + "}\n");
    }

    protected String calcHash() {
        return Encryption.sha256(this.index + this.timestamp + this.note + this.transactions + this.prev_hash);
    }
}
