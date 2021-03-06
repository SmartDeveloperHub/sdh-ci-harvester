/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-mem:0.3.0
 *   Bundle      : ci-backend-mem-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;

import com.google.common.base.MoreObjects;

final class InMemoryTransaction implements Transaction {

	private interface TransactionState {

		TransactionState begin();

		TransactionState commit();

		TransactionState rollback();

		boolean isActive();

	}

	private final class PendingTransactionState implements TransactionState {

		private static final String TRANSACTION_NOT_INITIATED = "Transaction not initiated";

		@Override
		public TransactionState begin() {
			return new InFlightTransactionState();
		}

		@Override
		public TransactionState commit() {
			throw new IllegalStateException(TRANSACTION_NOT_INITIATED);
		}

		@Override
		public TransactionState rollback() {
			throw new IllegalStateException(TRANSACTION_NOT_INITIATED);
		}

		@Override
		public String toString() {
			return "pending";
		}

		@Override
		public boolean isActive() {
			return false;
		}

	}

	private final class InFlightTransactionState implements TransactionState {

		@Override
		public TransactionState begin() {
			throw new IllegalStateException("Transaction already initiated");
		}

		@Override
		public TransactionState commit() {
			InMemoryTransaction.this.transactionManager.disposeTransaction(InMemoryTransaction.this);
			return new CompletedTransactionState("commited");
		}

		@Override
		public TransactionState rollback() {
			InMemoryTransaction.this.transactionManager.disposeTransaction(InMemoryTransaction.this);
			return new CompletedTransactionState("rolledback");
		}

		@Override
		public String toString() {
			return "in-flight";
		}

		@Override
		public boolean isActive() {
			return true;
		}
	}

	private final class CompletedTransactionState implements TransactionState {

		private static final String TRANSACTION_ALREADY_FINISHED = "Transaction already finished";

		private final String message;

		private CompletedTransactionState(final String message) {
			this.message = message;
		}

		@Override
		public TransactionState begin() {
			throw new IllegalStateException(TRANSACTION_ALREADY_FINISHED);
		}

		@Override
		public TransactionState commit() {
			throw new IllegalStateException(TRANSACTION_ALREADY_FINISHED);
		}

		@Override
		public TransactionState rollback() {
			throw new IllegalStateException(TRANSACTION_ALREADY_FINISHED);
		}

		@Override
		public String toString() {
			return "completed ("+this.message+")";
		}

		@Override
		public boolean isActive() {
			return false;
		}

	}

	private final InMemoryTransactionManager transactionManager;
	private final long id;
	private TransactionState state;

	InMemoryTransaction(final long id, final InMemoryTransactionManager persistencyManager) {
		this.id = id;
		this.transactionManager = persistencyManager;
		this.state=new PendingTransactionState();
	}

	long id() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void begin() {
		this.state=this.state.begin();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit() {
		this.state=this.state.commit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback() {
		this.state=this.state.rollback();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return this.state.isActive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("id", this.id).
					add("state",this.state).
					add("transactionManager",this.transactionManager).
					toString();
	}

}
