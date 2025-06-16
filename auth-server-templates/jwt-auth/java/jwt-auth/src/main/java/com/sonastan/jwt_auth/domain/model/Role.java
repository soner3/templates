package com.sonastan.jwt_auth.domain.model;

import java.util.HashSet;
import java.util.Set;

import com.sonastan.jwt_auth.infrastructure.constants.UserRole;
import com.sonastan.jwt_auth.infrastructure.security.SpringSecurityAuditorAware;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@EntityListeners(SpringSecurityAuditorAware.class)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "roles")
public class Role extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, updatable = false, name = "role_id")
    private Integer roleId;

    @Column(unique = true, nullable = false, updatable = false, name = "role_uuid")
    private String roleUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole rolename;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "role", cascade = CascadeType.MERGE)
    private Set<User> users = new HashSet<>();
}
