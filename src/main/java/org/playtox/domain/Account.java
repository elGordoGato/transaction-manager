package org.playtox.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playtox.TransactionManager;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@AllArgsConstructor
public class Account {
    private final static Logger logger = LogManager.getLogger(TransactionManager.class);
    private final Lock lock = new ReentrantLock();
    private final String id;
    private int money;

    public boolean changeBalance(int amount, boolean isSubtract) {
        if (isSubtract && amount > money) {
            return false;
        }
        int initialMoney = money;
        money = isSubtract ? money - amount : money + amount;
        String operation = isSubtract ? "-" : "+";
        logger.info("Account #{}: {} {} {} = {}", id, initialMoney, operation, amount, money);
        return true;
    }

    public boolean lock() throws InterruptedException {
        return lock.tryLock(3, TimeUnit.SECONDS);
    }

    public void unlock() {
        lock.unlock();
    }
}