package com.maker.website.orders;

import com.maker.website.machines.MachineEntity;
import com.maker.website.users.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
public class ProductionOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserEntity requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductionOrderTypeENUM type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id")
    private MachineEntity machine;

    @Column(length = 1024)
    private String fileName;

    @Column(length = 255)
    private String contentType;

    private long fileSize;

    @Column(columnDefinition = "bytea")
    private byte[] fileData;

    @Column(nullable = false, length = 2000)
    private String notes;

    @Column(length = 2000)
    private String dimensions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductionOrderStatusENUM status;

    @Column(length = 500)
    private String adminNotes;

    private LocalDateTime estimatedCompletionAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC, id ASC")
    private List<ProductionOrderCommentEntity> comments = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = ProductionOrderStatusENUM.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addComment(ProductionOrderCommentEntity comment) {
        comments.add(comment);
        comment.setOrder(this);
    }
}
