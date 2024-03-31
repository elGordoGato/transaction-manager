package org.playtox;

import org.playtox.domain.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    private static final int NUMBER_OF_THREADS = 2;
    private static final int NUMBER_OF_TRANSACTIONS = 30;
    private static final int INITIAL_BALANCE = 10000;
    private static final int NUMBER_OF_ACCOUNTS = 4;

    public static void main(String[] args) {
        TransactionManager transactionManager = new TransactionManager(
                NUMBER_OF_THREADS, NUMBER_OF_TRANSACTIONS);
        List<Account> accounts = createAccounts();

        transactionManager.start(accounts);
    }

    private static List<Account> createAccounts() {
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ACCOUNTS; i++) {
            accounts.add(new Account(UUID.randomUUID().toString(), INITIAL_BALANCE));
        }
        return accounts;
    }
}