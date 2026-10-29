package com.lira.field.core.database

import com.lira.field.core.model.Assignment

interface AssignmentDao {
    fun insertOrUpdateAll(assignments: List<Assignment>)
    fun insertOrUpdate(assignment: Assignment)
    fun getAssignmentsForTruck(truckId: String): List<Assignment>
    fun getAssignmentById(assignmentId: String): Assignment?
    fun updateAckState(assignmentId: String, ackState: String, newStatus: String? = null)
    fun clearForTruck(truckId: String)
    fun clearAll()
}

