package de.gruppe2.agamoTTTo.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_pool")
public class UserPool {

    @Id
    @GeneratedValue
    @Column(name = "user_pool_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "pool_id")
    private Pool pool;

    @Column(name = "is_active")
    private Boolean isActive = Boolean.TRUE;

    public UserPool(User user, Pool pool) {
        this.user = user;
        this.pool = pool;
    }

    public UserPool(Pool pool) {
        this.pool = pool;
    }
}
