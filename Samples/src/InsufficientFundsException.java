import java.io.PrintStream;

public class InsufficientFundsException extends Exception {
    @Override
    public String getMessage() {
        return "No funds available!";
    }
}
