import java.util.concurrent.TimeUnit;

public class Operations {
    private static final int WAIT_SEC = 1; //time in seconds to wait to lock

    public static void main(String[] args) {
        final Account a = new Account(1000);
        final Account b = new Account(2000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //keep only one of following lines uncommented
                    transfer(a, b, 500);
//                    transferReentrantLock(a, b, 500);
                }
                catch (InsufficientFundsException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            //keep only one of following lines uncommented
            transfer(b, a, 300);
//            transferReentrantLock(b, a, 300);
        }
        catch (InsufficientFundsException e) {
            e.printStackTrace();
        }
    }

    //1.locks ranged by hashCode()
    static void transfer(Account acc1, Account acc2, int amount) throws InsufficientFundsException {
        if (acc1.getBalance() < amount)
            throw new InsufficientFundsException();

        //uncomment to get deadlock
//        Object lock1 = acc1;
//        Object lock2 = acc2;
        //comment these two lines if previous two are uncommented
        Object lock1 = acc1.hashCode() > acc2.hashCode() ? acc1 : acc2;
        Object lock2 = acc2.hashCode() <= acc1.hashCode() ? acc2 : acc1;

        try {
            synchronized (lock1) {
                System.out.println(lock1 + "locked");
                Thread.sleep(1000);
                synchronized (lock2) {
                    System.out.println(lock2 + "locked");
                    acc1.withdraw(amount);
                    acc2.deposit(amount);
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(String.format(
                "Transfer %d from %s to %s successful", amount, acc1.toString(), acc2.toString()
        ));
    }

    //2.try ReentrantLock.
    static void transferReentrantLock(
            Account acc1, Account acc2, int amount) throws InsufficientFundsException {
        if (acc1.getBalance() < amount)
            throw new InsufficientFundsException();

        try {
            if (acc1.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                try {
                    System.out.println(acc1.getLock());
                    if (acc2.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                        try {
                            System.out.println(acc2.getLock());
                            Thread.sleep(1000);
                            acc1.withdraw(amount);
                            acc2.deposit(amount);
                        }
                        finally {
                            acc2.getLock().unlock();
                        }
                    }
                } finally {
                    acc1.getLock().unlock();
                }
            }
            else {
                //Error waiting lock
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(String.format(
                "Transfer %d from %s to %s successful", amount, acc1.toString(), acc2.toString()
        ));
    }
}
