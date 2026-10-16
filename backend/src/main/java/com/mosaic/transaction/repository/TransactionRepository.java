package com.mosaic.transaction.repository;

import com.mosaic.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category WHERE t.id = :id AND t.user.id = :userId AND t.deletedAt IS NULL")
    Optional<Transaction> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    Page<Transaction> findAllByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);
}
