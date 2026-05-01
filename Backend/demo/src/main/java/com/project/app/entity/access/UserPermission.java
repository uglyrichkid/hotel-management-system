package com.project.app.entity.access;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_permissions")
@IdClass(UserPermission.UserPermissionId.class)
public class UserPermission {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(name = "mode", nullable = false, length = 10)
    private String mode; // GRANT / DENY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    public UserPermission() {}

    // --- getters/setters ---

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    // --- composite key ---
    public static class UserPermissionId implements Serializable {
        private Long user;
        private Long permission;

        public UserPermissionId() {}

        public UserPermissionId(Long user, Long permission) {
            this.user = user;
            this.permission = permission;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserPermissionId)) return false;
            UserPermissionId that = (UserPermissionId) o;
            return Objects.equals(user, that.user)
                    && Objects.equals(permission, that.permission);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, permission);
        }
    }
}
