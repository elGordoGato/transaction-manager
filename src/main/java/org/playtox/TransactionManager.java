package org.playtox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionManager {
    private static final Logger logger = LogManager.getLogger(TransactionManager.class);
    private static final int INITIAL_BALANCE = 10000;
    private static final int NUMBER_OF_ACCOUNTS = 4;
    private static final int NUMBER_OF_THREADS = 2;
    private static final int NUMBER_OF_TRANSACTIONS = 30;
    private static final Random random = new Random();

    public void start() throws InterruptedException {
        logger.info("Application started");
        var context = new Object() {
            boolean keepRunning = true;
        };
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        ConcurrentHashMap<Integer, Account> accounts = createAccounts();
        AtomicInteger transactionCount = new AtomicInteger();

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            executorService.submit(() -> {
                        while (context.keepRunning) {
                            int sleepTime = 1000 + random.nextInt(1000);
                            try {
                                TimeUnit.MILLISECONDS.sleep(sleepTime);
                                context.keepRunning = performTransaction(accounts, transactionCount);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    });
        }
                    executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                    logger.info("Total money: {}", accounts.values().stream().mapToInt(Account::getMoney).sum());
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }

        private static ConcurrentHashMap<Integer, Account> createAccounts () {
            ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>(NUMBER_OF_ACCOUNTS);
            for (int i = 0; i < TransactionManager.NUMBER_OF_ACCOUNTS; i++) {
                accounts.put(i, new Account(i, INITIAL_BALANCE));
            }
            return accounts;
        }

        private static boolean performTransaction (ConcurrentHashMap < Integer, Account > accounts, AtomicInteger
        transactionCount) throws InterruptedException {
            if (transactionCount.getAndIncrement() >= NUMBER_OF_TRANSACTIONS) {
                return false;
            }
            List<Integer> keysAsList = new ArrayList<>(accounts.keySet());
            int indexFrom = keysAsList.get(random.nextInt(keysAsList.size()));
            Account from = accounts.remove(indexFrom);
            Account to;
            do {
                int indexTo = random.nextInt(keysAsList.size());
                to = accounts.remove(indexTo);
            } while (to == null);

            int fromPrevBalance = from.getMoney();

            int toPrevBalance = to.getMoney();
            int amount = random.nextInt(from.getMoney());

            if (from.withdraw(amount)) {
                to.deposit(amount);
                logger.info("""
                                Transaction #{} completed:
                                account {}: {} - {} = {}
                                account {}: {} + {} = {}""",
                        transactionCount.get(), from.getId(), fromPrevBalance, amount, from.getMoney(),
                        to.getId(), toPrevBalance, amount, to.getMoney());
            }
            accounts.put(from.getId(), from);
            accounts.put(to.getId(), to);
            return true;
        }
    }
