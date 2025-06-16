package com.sonastan.jwt_auth.domain.model;

import com.sonastan.jwt_auth.infrastructure.security.SpringSecurityAuditorAware;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "users")
public class User extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, updatable = false, name = "user_id")
    private Long userId;

    @Column(unique = true, nullable = false, updatable = false, name = "user_uuid")
    private String userUuid;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, name = "first_name")
    private String firstname;

    @Column(nullable = false, name = "last_name")
    private String lastname;

    @Column(nullable = false, name = "is_enabled")
    private boolean isEnabled;

    @Column(nullable = false, name = "is_credentials_non_expired")
    private boolean isCredentialsNonExpired;

    @Column(nullable = false, name = "is_account_non_locked")
    private boolean isAccountNonLocked;

    @Column(nullable = false, name = "is_account_non_expired")
    private boolean isAccountNonExpired;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "role_fk", nullable = false)
    private Role role;

    public User(String username, String email, String password, String firstname, String lastname) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
    }

}
