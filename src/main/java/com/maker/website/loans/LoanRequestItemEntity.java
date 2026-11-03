package com.maker.website.loans;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_request_items")
@Getter
@Setter
@NoArgsConstructor
public class LoanRequestItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_request_id", nullable = false)
    private LoanRequestEntity loanRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanResourceTypeENUM type;

    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private Integer quantity;
}
