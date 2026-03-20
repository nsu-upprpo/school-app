package com.github.nsu_upprpo.school_app.model.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @NoArgsConstructor @AllArgsConstructor
    public static class ParentChildId implements Serializable {
        private UUID parentId;
        private UUID childId;
    }
}
