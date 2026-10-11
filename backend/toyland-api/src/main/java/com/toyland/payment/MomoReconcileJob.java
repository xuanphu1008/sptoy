package com.toyland.payment;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Doi soat chu dong: moi 5 phut hoi MoMo ve cac giao dich da qua 3 phut ma van cho ket qua (IPN co the den cham
 * hoac that lac). IPN la co che chinh, job nay la luoi an toan nen chay thua va khong gay hai: ket qua duoc ap
 * dung idempotent. Moi giao dich co lap, mot cai loi khong lam hong ca lo.
 */
@Component
public class MomoReconcileJob {

	private static final Logger log = LoggerFactory.getLogger(MomoReconcileJob.class);

	private final PaymentTransactionRepository repository;
	private final MomoService momo;

	public MomoReconcileJob(PaymentTransactionRepository repository, MomoService momo) {
		this.repository = repository;
		this.momo = momo;
	}

	@Scheduled(fixedDelay = 300_000, initialDelay = 120_000)
	public void run() {
		if (!momo.isEnabled()) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		List<String> refs = repository.pendingMomoRefs(now.minusHours(2), now.minusMinutes(3), PageRequest.of(0, 20));
		for (String ref : refs) {
			try {
				momo.reconcile(ref);
			} catch (RuntimeException e) {
				log.warn("Doi soat MoMo {} khong thanh cong: {}", ref, e.getMessage());
			}
		}
	}
}
