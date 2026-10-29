package com.lira.field

import android.app.Application
import com.lira.field.core.database.LiraDatabase
import com.lira.field.core.session.SessionRepository
import com.lira.field.data.LiraRepository

class LiraApp : Application() {

    lateinit var sessionRepository: SessionRepository
        private set
    lateinit var database: LiraDatabase
        private set
    lateinit var repository: LiraRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionRepository = SessionRepository(this)
        database = LiraDatabase.getInstance(this)
        repository = LiraRepository(this, sessionRepository, database)

        // Ensure fake mock data is purged and mock mode is strictly disabled
        val currentSession = sessionRepository.session.value
        if (sessionRepository.mockModeEnabled.value || currentSession?.token?.startsWith("DEMO_SESSION_TOKEN") == true) {
            repository.purgeAllFakeAndCachedData()
        }
    }

    companion object {
        lateinit var instance: LiraApp
            private set
    }
}

