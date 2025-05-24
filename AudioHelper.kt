class AudioHelper {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFileName: String? = null
    
    fun startRecording(phoneNumber: String?) {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                
                currentFileName = generateFileName(phoneNumber)
                setOutputFile(getRecordingPath(currentFileName!!))
                
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioHelper", "Recording failed", e)
        }
    }
    
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentFileName = null
        } catch (e: Exception) {
            Log.e("AudioHelper", "Stop recording failed", e)
        }
    }
    
    private fun generateFileName(phoneNumber: String?): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return if (phoneNumber.isNullOrEmpty()) {
            "Recording_$timestamp.mp4"
        } else {
            "Call_${phoneNumber}_$timestamp.mp4"
        }
    }
    
    private fun getRecordingPath(fileName: String): String {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "CallRecordings"
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, fileName).absolutePath
    }
}
