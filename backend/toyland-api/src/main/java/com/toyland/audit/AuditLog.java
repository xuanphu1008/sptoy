package com.toyland.audit;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Nhat ky thao tac cua nhan vien / quan tri (ai - lam gi - luc nao - tu dau). */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "actor", nullable = false, length = 50)
	private String actor;

	@Column(name = "actor_role", length = 20)
	private String actorRole;

	@Column(name = "action", nullable = false, length = 30)
	private String action;

	@Column(name = "entity_type", nullable = false, length = 40)
	private String entityType;

	@Column(name = "entity_id", length = 40)
	private String entityId;

	@Column(name = "summary", nullable = false, length = 500)
	private String summary;

	@Column(name = "ip", length = 45)
	private String ip;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() { return id; }
	public String getActor() { return actor; }
	public void setActor(String actor) { this.actor = actor; }
	public String getActorRole() { return actorRole; }
	public void setActorRole(String actorRole) { this.actorRole = actorRole; }
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
	public String getEntityType() { return entityType; }
	public void setEntityType(String entityType) { this.entityType = entityType; }
	public String getEntityId() { return entityId; }
	public void setEntityId(String entityId) { this.entityId = entityId; }
	public String getSummary() { return summary; }
	public void setSummary(String summary) { this.summary = summary; }
	public String getIp() { return ip; }
	public void setIp(String ip) { this.ip = ip; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}
