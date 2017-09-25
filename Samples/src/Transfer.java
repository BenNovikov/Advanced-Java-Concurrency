import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Transfer implements Callable<Boolean> {
    private static final int WAIT_SEC = 4; //time in seconds to wait to lock

    private Account accountFrom;
    private Account accountTo;
    private int amount;

    public Transfer(Account accountFrom, Account accountTo, int amount) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.amount = amount;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            if (accountFrom.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                try {
                    if (accountFrom.getBalance() < amount)
                        throw new InsufficientFundsException();
//                    System.out.println(accountFrom.getLock());
                    if (accountTo.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                        try {
//                            System.out.println(accountTo.getLock());
                            accountFrom.withdraw(amount);
                            Thread.sleep(new Random().nextInt(2000));
                            accountTo.deposit(amount);
                            Thread.sleep(new Random().nextInt(1000));
                        }
                        finally {
                            accountTo.getLock().unlock();
                        }
                    }
                } finally {
                    accountFrom.getLock().unlock();
                }
            }
            else {
                accountFrom.incFailCount();
                accountTo.incFailCount();
                System.out.println(String.format(
                        "Transfer from %s to %s UNSUCCESSFUL", accountFrom.toString(), accountTo.toString()
                ));

                return false;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(String.format(
                "Transfer %d from %s to %s successful", amount, accountFrom.toString(), accountTo.toString()
        ));
        return true;
    }
}
