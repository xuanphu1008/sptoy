package com.toyland.partner;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.common.exception.ApiException;
import com.toyland.partner.AdminPartnerController.CreatePartner;
import com.toyland.partner.AdminPartnerController.CreatedPartner;
import com.toyland.partner.AdminPartnerController.PartnerView;

/** Nghiep vu quan tri doi tac: cap khoa/secret ngau nhien (SecureRandom), bat/tat. */
@Service
public class PartnerAdminService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final PartnerRepository repository;

	public PartnerAdminService(PartnerRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<PartnerView> list() {
		return repository.findAll().stream().map(PartnerAdminService::view).toList();
	}

	@Transactional
	public CreatedPartner create(CreatePartner r) {
		Partner p = new Partner();
		p.setName(r.name().trim());
		p.setApiKey("tlp_" + randomHex(12));
		p.setSecret(randomHex(32));
		if (r.rateLimitPerMin() != null) {
			p.setRateLimitPerMin(r.rateLimitPerMin());
		}
		repository.save(p);
		return new CreatedPartner(p.getId(), p.getName(), p.getApiKey(), p.getSecret(),
				"Lưu secret ngay: hệ thống không hiển thị lại. Dùng để ký HMAC-SHA256, không gửi qua mạng.");
	}

	@Transactional
	public PartnerView setActive(Long id, boolean active) {
		Partner p = repository.findById(id).orElseThrow(() -> ApiException.notFound("đối tác", id));
		p.setActive(active);
		return view(p);
	}

	@Transactional
	public PartnerView setWebhook(Long id, String url) {
		Partner p = repository.findById(id).orElseThrow(() -> ApiException.notFound("đối tác", id));
		p.setWebhookUrl(url == null || url.isBlank() ? null : url.trim());
		return view(p);
	}

	private static PartnerView view(Partner p) {
		return new PartnerView(p.getId(), p.getName(), p.getApiKey(), p.isActive(), p.getRateLimitPerMin(), p.getLastUsedAt(),
				p.getWebhookUrl());
	}

	private static String randomHex(int bytes) {
		byte[] b = new byte[bytes];
		RANDOM.nextBytes(b);
		return HexFormat.of().formatHex(b);
	}
}
