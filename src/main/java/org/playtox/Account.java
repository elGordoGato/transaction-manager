package org.playtox;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Account {
    private final int id;
    private int money;

    public synchronized boolean withdraw(int amount) {
        if (amount <= money) {
            money -= amount;
            return true;
        }
        return false;
    }

    public synchronized void deposit(int amount) {
        money += amount;
    }
}