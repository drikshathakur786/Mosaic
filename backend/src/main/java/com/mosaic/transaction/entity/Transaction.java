package com.mosaic.transaction.entity;

import com.mosaic.auth.entity.User;
import com.mosaic.category.entity.Category;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(name = "merchant_name", length = 255, nullable = false)
    private String merchantName;

    @Column(name = "normalized_merchant_name", length = 255)
    private String normalizedMerchantName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", length = 20, nullable = false)
    private TransactionStatus transactionStatus = TransactionStatus.COMPLETED;

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Column(length = 50, nullable = false)
    private String timezone = "UTC";

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TransactionSource source = TransactionSource.MANUAL;

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Column(name = "import_id")
    private UUID importId;

    @Column(name = "is_reviewed", nullable = false)
    private boolean isReviewed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Transaction() {}

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.normalizedMerchantName == null && this.merchantName != null) {
            this.normalizedMerchantName = normalizeMerchant(this.merchantName);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        if (this.merchantName != null) {
            this.normalizedMerchantName = normalizeMerchant(this.merchantName);
        }
    }

    public static String normalizeMerchant(String raw) {
        if (raw == null) return null;
        return raw.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public void setAmountMinor(long amountMinor) {
        this.amountMinor = amountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
        this.normalizedMerchantName = normalizeMerchant(merchantName);
    }

    public String getNormalizedMerchantName() {
        return normalizedMerchantName;
    }

    public void setNormalizedMerchantName(String normalizedMerchantName) {
        this.normalizedMerchantName = normalizedMerchantName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Instant transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public TransactionSource getSource() {
        return source;
    }

    public void setSource(TransactionSource source) {
        this.source = source;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public UUID getImportId() {
        return importId;
    }

    public void setImportId(UUID importId) {
        this.importId = importId;
    }

    public boolean isReviewed() {
        return isReviewed;
    }

    public void setReviewed(boolean reviewed) {
        isReviewed = reviewed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
