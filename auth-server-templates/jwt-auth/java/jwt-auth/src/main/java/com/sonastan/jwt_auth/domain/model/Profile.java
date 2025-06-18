package com.sonastan.jwt_auth.domain.model;

import java.util.UUID;

import com.sonastan.jwt_auth.infrastructure.security.SpringSecurityAuditorAware;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@EntityListeners(SpringSecurityAuditorAware.class)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "profiles")
public class Profile extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false, name = "profile_id")
    private Long profileId;

    @Column(nullable = false, unique = true, updatable = false, name = "profile_uuid")
    private String profileUuid;

    @OneToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    public Profile(User user) {
        this.profileUuid = UUID.randomUUID().toString();
        this.user = user;
    }

}
