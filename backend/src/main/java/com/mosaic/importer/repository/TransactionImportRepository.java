package com.mosaic.importer.repository;

import com.mosaic.importer.entity.TransactionImport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionImportRepository extends JpaRepository<TransactionImport, UUID> {

    Optional<TransactionImport> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndFileHash(UUID userId, String fileHash);

    Page<TransactionImport> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT ti FROM TransactionImport ti LEFT JOIN FETCH ti.rows WHERE ti.id = :id AND ti.user.id = :userId")
    Optional<TransactionImport> findByIdAndUserIdWithRows(@Param("id") UUID id, @Param("userId") UUID userId);
}
