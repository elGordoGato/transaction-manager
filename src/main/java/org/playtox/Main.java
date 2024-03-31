package org.playtox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playtox.Account;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TransactionManager transactionManager = new TransactionManager();
        transactionManager.start();
    }
}