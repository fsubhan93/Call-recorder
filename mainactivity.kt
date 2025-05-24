class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val recordingsAdapter = RecordingsAdapter()
    private val permissionHelper = PermissionHelper(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        binding.recordingsRecyclerView.adapter = recordingsAdapter
        binding.toggleRecordingButton.setOnClickListener {
            if (CallRecorderService.isRunning) {
                stopRecordingService()
            } else {
                startRecordingService()
            }
        }
    }
    
    private fun checkPermissions() {
        if (!permissionHelper.hasAllPermissions()) {
            permissionHelper.requestPermissions()
        } else {
            loadRecordings()
        }
    }
    
    private fun startRecordingService() {
        val serviceIntent = Intent(this, CallRecorderService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        updateButtonState(true)
    }
    
    private fun stopRecordingService() {
        val serviceIntent = Intent(this, CallRecorderService::class.java)
        stopService(serviceIntent)
        updateButtonState(false)
    }
    
    private fun updateButtonState(isRecording: Boolean) {
        binding.toggleRecordingButton.text = if (isRecording) {
            "Stop Recording"
        } else {
            "Start Recording"
        }
    }
    
    private fun loadRecordings() {
        // Load saved recordings from storage
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionHelper.allPermissionsGranted(grantResults)) {
            loadRecordings()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }
}
