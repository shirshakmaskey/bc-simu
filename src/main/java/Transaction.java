import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Transaction {
    private String senderID;
    private String recipientID;
    private double amount;
    private boolean verified;

    public Transaction(String iID, String rID, double amt) {
        this.senderID = iID;
        this.recipientID = rID;
        this.amount = amt;
        this.verified = false;
    }
}