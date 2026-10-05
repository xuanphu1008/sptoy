package com.toyland.common.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Chay mot tac vu SAU KHI giao dich commit thanh cong (vd gui email).
 * Tranh truong hop email "dat hang thanh cong" da gui nhung giao dich lai bi rollback.
 */
public final class AfterCommit {

	private AfterCommit() {
	}

	public static void run(Runnable task) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					task.run();
				}
			});
		} else {
			task.run();
		}
	}
}
