package com.project.app.repository.access;

import com.project.app.entity.access.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByModule(String module);

    boolean existsByName(String name);
}