package com.example.helpern2.utils

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import android.widget.Toast

class SyncJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("SyncJobService", "Background sync started...")
        // In a real app, you would perform a network sync here
        
        // Return true because the work is happening in a separate thread (if applicable)
        // For this demo, we'll just finish immediately
        jobFinished(params, false)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("SyncJobService", "Background sync stopped.")
        return true // Retry
    }
}
