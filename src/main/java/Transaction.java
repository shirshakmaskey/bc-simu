import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Transaction {
    private String senderID;
    private String recipientID;
    private double amount;
    private boolean verified;



    public Transaction(String iID, String rID, double amt,boolean verified) {
        this.senderID = iID;
        this.recipientID = rID;
        this.amount = amt;
        this.verified = verified;
    }

    public String getRecepientID() {
        //yo yaha huna parne
        return "success";
    }

    public String getSenderID() {
        //nabhako func
        return "success";
    }

    public double getAmount() {
        //nabhako kura
        return 0;
    }

    public void setVerified(boolean b) {
        //nabhako kura haru
    }
}