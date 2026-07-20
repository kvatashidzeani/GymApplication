package com.gymcrm.transaction;

import com.gymcrm.storage.TraineeStorage;
import com.gymcrm.storage.TrainerStorage;
import com.gymcrm.storage.TrainingStorage;
import com.gymcrm.storage.UserStorage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Transaction manager for in-memory storage: snapshots maps on begin and restores on rollback.
 */
@Component
@Primary
public class InMemoryTransactionManager extends AbstractPlatformTransactionManager {

    private final UserStorage userStorage;
    private final TraineeStorage traineeStorage;
    private final TrainerStorage trainerStorage;
    private final TrainingStorage trainingStorage;

    public InMemoryTransactionManager(UserStorage userStorage,
                                      TraineeStorage traineeStorage,
                                      TrainerStorage trainerStorage,
                                      TrainingStorage trainingStorage) {
        this.userStorage = userStorage;
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
        this.trainingStorage = trainingStorage;
    }

    @Override
    protected Object doGetTransaction() {
        return new InMemoryTransaction();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        InMemoryTransaction inMemoryTransaction = (InMemoryTransaction) transaction;
        inMemoryTransaction.snapshot = StorageSnapshot.capture(
                userStorage, traineeStorage, trainerStorage, trainingStorage);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        ((InMemoryTransaction) status.getTransaction()).snapshot = null;
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        InMemoryTransaction inMemoryTransaction = (InMemoryTransaction) status.getTransaction();
        if (inMemoryTransaction.snapshot != null) {
            StorageSnapshot.restore(
                    inMemoryTransaction.snapshot,
                    userStorage,
                    traineeStorage,
                    trainerStorage,
                    trainingStorage);
        }
    }

    private static final class InMemoryTransaction {
        private StorageSnapshot snapshot;
    }
}
