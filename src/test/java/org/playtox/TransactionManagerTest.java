package org.playtox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.playtox.domain.Account;
import org.playtox.domain.Transaction;
import org.playtox.domain.exception.TransactionError;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TransactionManagerTest {
    @Mock
    private Account mockAccount1;
    @Mock
    private Account mockAccount2;

    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockAccount1.getMoney()).thenReturn(10000);
        when(mockAccount2.getMoney()).thenReturn(11000);
        transactionManager = new TransactionManager(2, 30);
    }

    @Test
    void testGetRandomTransactionReturnsValidTransaction() {
        Transaction transaction = transactionManager.getRandomTransaction(List.of(mockAccount1, mockAccount2));
        assertNotNull(transaction);
        assertNotNull(transaction.getFrom());
        assertNotNull(transaction.getTo());
        assertTrue(transaction.getAmount() >= 0);
    }

    @Test
    void testPerformTransactionUpdatesBalances() throws InterruptedException, TransactionError {
        when(mockAccount1.lock()).thenReturn(true);
        when(mockAccount2.lock()).thenReturn(true);
        when(mockAccount1.changeBalance(anyInt(), eq(true))).thenReturn(true);
        when(mockAccount2.changeBalance(anyInt(), eq(false))).thenReturn(true);

        Transaction transaction = new Transaction(mockAccount1, mockAccount2, 100);
        transactionManager.performTransaction(transaction, new AtomicInteger());

        verify(mockAccount1).lock();
        verify(mockAccount2).lock();
        verify(mockAccount1).changeBalance(100, true);
        verify(mockAccount2).changeBalance(100, false);
        verify(mockAccount1).unlock();
        verify(mockAccount2).unlock();
    }
}