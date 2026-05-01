package com.project.app.entity.access;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who did the action
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actorUser;

    @Column(name = "action", nullable = false, length = 80)
    private String action;
    // USER_CREATED / ROLE_ASSIGNED / PERMISSION_GRANTED / BOOKING_CANCELLED ...

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;
    // USER / ROLE / HOTEL / BOOKING / PAYMENT ...

    @Column(name = "entity_id")
    private Long entityId; // id объекта (может быть null)

    @Column(name = "details", columnDefinition = "text")
    private String details; // можно хранить JSON строкой

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IPv4/IPv6

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AuditLog() {}

    // --- getters/setters ---

    public Long getId() { return id; }

    public User getActorUser() { return actorUser; }
    public void setActorUser(User actorUser) { this.actorUser = actorUser; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
