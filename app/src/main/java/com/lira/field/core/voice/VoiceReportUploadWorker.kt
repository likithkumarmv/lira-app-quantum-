package com.lira.field.core.voice

import android.content.Context
import androidx.work.*
import com.lira.field.core.network.NetworkClient
import com.lira.field.core.session.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

class VoiceReportUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_ASSIGNMENT_ID = "key_assignment_id"
        const val KEY_PLAN_VERSION = "key_plan_version"
        const val KEY_IDEMPOTENCY_KEY = "key_idempotency_key"
        const val KEY_FILE_PATH = "key_file_path"
        const val KEY_RECORDED_AT = "key_recorded_at"
        const val KEY_RESULT_REPORT_ID = "key_result_report_id"

        fun createWorkRequest(
            assignmentId: String,
            planVersion: Int,
            idempotencyKey: String,
            filePath: String,
            recordedAt: String
        ): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = workDataOf(
                KEY_ASSIGNMENT_ID to assignmentId,
                KEY_PLAN_VERSION to planVersion,
                KEY_IDEMPOTENCY_KEY to idempotencyKey,
                KEY_FILE_PATH to filePath,
                KEY_RECORDED_AT to recordedAt
            )

            return OneTimeWorkRequestBuilder<VoiceReportUploadWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val assignmentId = inputData.getString(KEY_ASSIGNMENT_ID) ?: return@withContext Result.failure()
        val planVersion = inputData.getInt(KEY_PLAN_VERSION, 1)
        val idempotencyKey = inputData.getString(KEY_IDEMPOTENCY_KEY) ?: return@withContext Result.failure()
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return@withContext Result.failure()
        val recordedAt = inputData.getString(KEY_RECORDED_AT) ?: return@withContext Result.failure()

        val audioFile = File(filePath)
        if (!audioFile.exists()) {
            return@withContext Result.failure()
        }

        // Delete if older than 24h
        if (System.currentTimeMillis() - audioFile.lastModified() > 24 * 3600 * 1000L) {
            audioFile.delete()
            return@withContext Result.failure()
        }

        val sessionRepo = SessionRepository(applicationContext)
        val apiService = NetworkClient.createApiService(
            baseUrl = sessionRepo.baseUrl.value,
            tokenProvider = { sessionRepo.getToken() }
        )

        val fileBody = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, fileBody)
        val idempotencyBody = idempotencyKey.toRequestBody("text/plain".toMediaTypeOrNull())
        val planVersionBody = planVersion.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val recordedAtBody = recordedAt.toRequestBody("text/plain".toMediaTypeOrNull())

        try {
            val response = apiService.uploadVoiceReport(
                assignmentId = assignmentId,
                audio = audioPart,
                idempotencyKey = idempotencyBody,
                planVersion = planVersionBody,
                clientRecordedAt = recordedAtBody
            )

            if (response.isSuccessful && response.body() != null) {
                val report = response.body()!!
                // Successfully uploaded, delete the file
                audioFile.delete()
                val output = workDataOf(
                    KEY_RESULT_REPORT_ID to report.reportId,
                    KEY_IDEMPOTENCY_KEY to idempotencyKey
                )
                Result.success(output)
            } else if (response.code() == 409) {
                // Stale plan version conflict, do not retry
                audioFile.delete()
                Result.failure()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
