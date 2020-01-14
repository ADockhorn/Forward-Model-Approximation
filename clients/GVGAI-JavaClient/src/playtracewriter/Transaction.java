package playtracewriter;

import playtracewriter.TickInfo;
import java.util.List;

/**
 * Created by dockhorn on 22.02.2018.
 * Transaction does not need to be a class, but maybe we add further features to it.
 */
public class Transaction {

    TickInfo lastTick;
    TickInfo currentTick;

    String transactionString;

    public Transaction(TickInfo lastTick, TickInfo currentTick){
        this.lastTick = lastTick;
        this.currentTick = currentTick;

        this.transactionString = null;
    }

    public String get_transaction_string(){
        if (this.transactionString == null)
        {
            StringBuilder builder = new StringBuilder();
            List<String> previous_tokens = lastTick.getTokenList();
            List<String> current_tokens = currentTick.getTokenList();

            for (String token : previous_tokens){
                if (!token.startsWith("GameState=")){
                    builder.append("c_" + token + " ");
                }
            }

            for (String token : current_tokens){
                builder.append("n_" + token + " ");
            }
            this.transactionString = builder.toString();
        }
        return this.transactionString;
    }
}
