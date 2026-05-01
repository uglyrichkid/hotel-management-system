package com.project.app.repository.access;

import com.project.app.entity.access.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPermissionRepository extends JpaRepository<UserPermission, UserPermission.UserPermissionId> {

    List<UserPermission> findByUserId(Long userId);

    List<UserPermission> findByUserIdAndMode(Long userId, String mode);

    boolean existsByUserIdAndPermissionId(Long userId, Long permissionId);

    void deleteByUserIdAndPermissionId(Long userId, Long permissionId);
}