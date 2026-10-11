package com.toyland.partner;

import java.util.Optional;

/** Giao dien cong bo cua mien Doi tac: noi nhan webhook cua mot doi tac (bo phat su kien dung, khong dung repository). */
public interface PartnerWebhookApi {

	record Target(String url, String secret) {
	}

	/** Rong neu doi tac khong ton tai, da bi tat, hoac chua dat URL webhook. */
	Optional<Target> targetOf(Long partnerId);
}
