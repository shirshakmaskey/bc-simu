import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Transaction {
    protected String senderID;
    protected String recepientID;
    protected double amount;
    protected boolean verified;

    public Transaction(String iID, String rID, double amt) {
        this.senderID = iID;
        this.recepientID = rID;
        this.amount = amt;
        this.verified = false;
    }
}