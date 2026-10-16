package com.mosaic.transaction.repository;

import com.mosaic.transaction.dto.TransactionFilterRequest;
import com.mosaic.transaction.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Transaction> build(UUID userId, TransactionFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tenant isolation & soft-delete filter (MANDATORY)
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.startDate()));
            }

            if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.endDate()));
            }

            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }

            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("transactionType"), filter.type()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("transactionStatus"), filter.status()));
            }

            if (filter.source() != null) {
                predicates.add(cb.equal(root.get("source"), filter.source()));
            }

            if (filter.merchant() != null && !filter.merchant().isBlank()) {
                String normalizedSearch = filter.merchant().trim().toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("merchantName")), "%" + normalizedSearch + "%"));
            }

            if (filter.minAmountMinor() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amountMinor"), filter.minAmountMinor()));
            }

            if (filter.maxAmountMinor() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amountMinor"), filter.maxAmountMinor()));
            }

            if (filter.isReviewed() != null) {
                predicates.add(cb.equal(root.get("isReviewed"), filter.isReviewed()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
