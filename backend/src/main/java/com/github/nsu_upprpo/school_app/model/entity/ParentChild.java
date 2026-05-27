package com.github.nsu_upprpo.school_app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "parent_children")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ParentChild.ParentChildId.class)
public class ParentChild {

    @Id
    @Column(name = "parent_id")
    private UUID parentId;

    @Id
    @Column(name = "child_id")
    private UUID childId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentChildId implements Serializable {
        private UUID parentId;
        private UUID childId;
    }
}
