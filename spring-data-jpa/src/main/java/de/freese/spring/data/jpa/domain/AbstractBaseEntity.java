// Created: 16.08.23
package de.freese.spring.data.jpa.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * @author Thomas Freese
 */
@MappedSuperclass
public abstract class AbstractBaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 5L;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdDate;

    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @LastModifiedDate
    @Column(name = "updated_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedDate;

    @JsonIgnore
    @Version
    private Integer version;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final AbstractBaseEntity that)) {
            return false;
        }

        return Objects.equals(createdDate, that.createdDate) && Objects.equals(id, that.id) && Objects.equals(updatedDate, that.updatedDate) && Objects.equals(version,
                that.version);
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdDate, id, updatedDate, version);
    }

    public void setCreatedDate(final LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setUpdatedDate(final LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }
}
