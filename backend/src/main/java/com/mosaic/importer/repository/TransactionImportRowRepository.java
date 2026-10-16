package com.mosaic.importer.repository;

import com.mosaic.importer.entity.ImportRowStatus;
import com.mosaic.importer.entity.TransactionImportRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionImportRowRepository extends JpaRepository<TransactionImportRow, UUID> {
    List<TransactionImportRow> findAllByTransactionImportIdOrderByRowNumberAsc(UUID importId);
    List<TransactionImportRow> findAllByTransactionImportIdAndStatus(UUID importId, ImportRowStatus status);
}
