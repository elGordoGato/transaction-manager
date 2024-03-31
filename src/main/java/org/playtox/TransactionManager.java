package org.playtox;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playtox.domain.Account;
import org.playtox.domain.Transaction;
import org.playtox.domain.exception.TransactionError;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class TransactionManager {
    private final Logger logger = LogManager.getLogger(TransactionManager.class);
    private final Random random = new Random();
    private final int numberOfThreads;
    private final int numberOfTransactions;

    public void start(List<Account> accounts) {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger transactionCount = new AtomicInteger();

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                while (true) {
                    int sleepTime = 1000 + random.nextInt(1000);
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleepTime);
                        if (transactionCount.get() < numberOfTransactions) {
                            Transaction transaction = getRandomTransaction(accounts);
                            performTransaction(transaction, transactionCount);
                        } else break;
                    } catch (InterruptedException e) {
                        logger.error("Thread {} was interrupted",
                                Thread.currentThread().getName(), e);
                        break;
                    }
                }
            });
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private Transaction getRandomTransaction(List<Account> accounts) {
        int indexFrom = random.nextInt(accounts.size());
        int indexTo;
        do {
            indexTo = random.nextInt(accounts.size());
        } while (indexFrom == indexTo);


        Account from = accounts.get(indexFrom);
        Account to = accounts.get(indexTo);
        int amount = random.nextInt(from.getMoney());
        return new Transaction(from, to, amount);
    }

    private void performTransaction(Transaction transaction, AtomicInteger transactionCount) {
        Account from = transaction.getFrom();
        Account to = transaction.getTo();
        int amount = transaction.getAmount();
        try {
            boolean isAccountsLocked = from.lock() && to.lock();
            if (!isAccountsLocked) {
                throw new TransactionError("Accounts is used by another transaction");
            }
            if (from.changeBalance(amount, true)) {
                to.changeBalance(amount, false);
                int transactionNumber = transactionCount.incrementAndGet();
                logger.info("Transaction #{} completed", transactionNumber);
            } else {
                throw new TransactionError("Not enough money");
            }
        } catch (InterruptedException | TransactionError e) {
            logger.error("Transaction of {} from {} to {} unsuccessful",
                    amount, from.getId(), to.getId(), e);
        } finally {
            from.unlock();
            to.unlock();
        }


    }
}
