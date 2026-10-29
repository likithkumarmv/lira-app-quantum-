package com.lira.field.core.database

data class PendingAction(
    val id: String,
    val type: String, // "ACK", "CANNOT_COMPLY", "ETA_REPORT"
    val assignmentId: String?,
    val truckId: String,
    val payloadJson: String,
    val idempotencyKey: String,
    val timestamp: Long,
    val retryCount: Int = 0,
    val syncStatus: String = STATUS_PENDING,
    val lastError: String? = null
) {
    companion object {
        const val TYPE_ACK = "ACK"
        const val TYPE_CANNOT_COMPLY = "CANNOT_COMPLY"
        const val TYPE_ETA_REPORT = "ETA_REPORT"

        const val STATUS_PENDING = "PENDING"
        const val STATUS_SYNCING = "SYNCING"
        const val STATUS_FAILED = "FAILED"
    }
}

interface PendingActionDao {
    fun insert(action: PendingAction)
    fun getAllPending(): List<PendingAction>
    fun delete(id: String)
    fun updateStatus(id: String, status: String, error: String? = null)
    fun incrementRetry(id: String)
    fun getCount(): Int
}

