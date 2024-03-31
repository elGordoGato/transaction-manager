package org.playtox.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Transaction {
    private Account from;
    private Account to;
    private int amount;
}
