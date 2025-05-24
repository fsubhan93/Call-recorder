class CallRecorderService : Service() {
    private val audioHelper = AudioHelper()
    private lateinit var notificationManager: NotificationManager
    private lateinit var phoneStateListener: PhoneStateListener
    
    companion object {
        var isRunning = false
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setupPhoneStateListener()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        isRunning = true
        return START_STICKY
    }
    
    private fun setupPhoneStateListener() {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        // Call started
                        audioHelper.startRecording(phoneNumber)
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        // Call ended
                        audioHelper.stopRecording()
                    }
                }
            }
        }
        
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }
    
    private fun createNotification(): Notification {
        val channelId = "call_recorder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Call Recorder",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Call Recorder")
            .setContentText("Recording calls in background")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        isRunning = false
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private companion object {
        const val NOTIFICATION_ID = 101
    }
}
