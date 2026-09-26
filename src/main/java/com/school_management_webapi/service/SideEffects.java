package com.school_management_webapi.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Runs the writes that accompany a request without being part of it - audit
 * entries and notifications - so they can neither fail it nor be undone by it
 * in the wrong way.
 *
 * Each runs in a transaction of its own, and every failure, including one at
 * commit, is caught and logged here. A `@Transactional(REQUIRES_NEW)` method
 * cannot promise that: an exception raised while committing escapes the
 * method's own try block and reaches the caller.
 */
@Slf4j
@Component
public class SideEffects {

	private final TransactionTemplate separate;

	public SideEffects(PlatformTransactionManager transactionManager) {
		this.separate = new TransactionTemplate(transactionManager);
		this.separate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	/**
	 * Now, independently of the request. For what must be kept even if the
	 * request then fails - a failed sign-in is the entry most worth keeping.
	 */
	public void now(String what, Runnable work) {
		try {
			separate.executeWithoutResult(status -> work.run());
		} catch (RuntimeException failure) {
			log.warn("Could not record {}: {}", what, failure.getMessage());
		}
	}

	/**
	 * Once the request's own transaction has committed, or at once if there is
	 * none. For what must only happen if the request did: announcing a payment
	 * that was rolled back would be announcing a payment that does not exist.
	 */
	public void afterCommit(String what, Runnable work) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			now(what, work);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				now(what, work);
			}
		});
	}
}
