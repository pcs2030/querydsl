package org.example.entity;

import jakarta.persistence.*;

@Entity
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
@Table(name = "Child")
public class ChildEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String sur;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSur() {
        return sur;
    }

    public void setSur(String sur) {
        this.sur = sur;
    }

    @ManyToOne
    @JoinColumn(name = "parentId")
    private ParentEntity parentEntity;

    public ParentEntity getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(ParentEntity parentEntity) {
        this.parentEntity = parentEntity;
    }
}
