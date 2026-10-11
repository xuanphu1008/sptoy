package com.toyland.partner;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartnerWebhookApiImpl implements PartnerWebhookApi {

	private final PartnerRepository repository;

	public PartnerWebhookApiImpl(PartnerRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Target> targetOf(Long partnerId) {
		return repository.findById(partnerId).filter(Partner::isActive)
				.filter(p -> p.getWebhookUrl() != null && !p.getWebhookUrl().isBlank())
				.map(p -> new Target(p.getWebhookUrl().trim(), p.getSecret()));
	}
}
