package com.toyland.order;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

/** Tac vu chay nen: huy don MoMo qua han thanh toan, tu hoan tat don da giao qua han tra hang. */
@Component
public class OrderScheduler {

	private static final Logger log = LoggerFactory.getLogger(OrderScheduler.class);

	private final OrderRepository orderRepository;
	@PersistenceContext
	private EntityManager em;

	private final OrderWorkflow workflow;

	@Value("${app.order.return-days}")
	private int returnDays;

	public OrderScheduler(OrderRepository orderRepository, OrderWorkflow workflow) {
		this.orderRepository = orderRepository;
		this.workflow = workflow;
	}

	@Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
	@Transactional
	public void cancelExpiredUnpaid() {
		for (Order o : orderRepository.findExpiredUnpaid(LocalDateTime.now())) {
			// khoa + nap lai: neu MoMo vua bao thanh toan thanh cong thi bo qua
			em.refresh(o, LockModeType.PESSIMISTIC_WRITE);
			if (o.getStatus() != OrderStatus.PENDING_PAYMENT || o.getPaymentStatus() == PaymentStatus.PAID) {
				continue;
			}
			o.setCancelReason("Quá hạn thanh toán online");
			workflow.transition(o, OrderStatus.CANCELLED, "Tự động huỷ do quá hạn thanh toán MoMo", "system");
			log.info("Tu huy don {} do qua han thanh toan", o.getOrderCode());
		}
	}

	@Scheduled(fixedDelay = 3_600_000, initialDelay = 60_000)
	@Transactional
	public void completeDelivered() {
		for (Order o : orderRepository.findDeliveredBefore(LocalDateTime.now().minusDays(returnDays))) {
			workflow.transition(o, OrderStatus.COMPLETED, "Tự động hoàn tất (hết hạn đổi trả " + returnDays + " ngày)", "system");
		}
	}
}
