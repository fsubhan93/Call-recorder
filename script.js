document.addEventListener('deviceready', onDeviceReady, false);

let mediaRecorder;
let audioChunks = [];
let recordings = [];

function onDeviceReady() {
    console.log('Device is ready');
    
    document.getElementById('startBtn').addEventListener('click', startRecording);
    document.getElementById('stopBtn').addEventListener('click', stopRecording);
    
    loadRecordings();
}

function startRecording() {
    const statusElement = document.getElementById('status');
    
    // Check for permissions first
    const permissions = cordova.plugins.permissions;
    permissions.checkPermission(permissions.RECORD_AUDIO, function(status) {
        if (!status.hasPermission) {
            permissions.requestPermission(permissions.RECORD_AUDIO, 
                function(status) {
                    if (status.hasPermission) {
                        actuallyStartRecording();
                    } else {
                        statusElement.textContent = "Permission denied - cannot record";
                    }
                }, 
                function(error) {
                    statusElement.textContent = "Error requesting permission";
                    console.error(error);
                }
            );
        } else {
            actuallyStartRecording();
        }
    });
}

function actuallyStartRecording() {
    const statusElement = document.getElementById('status');
    
    statusElement.textContent = "Recording...";
    document.getElementById('startBtn').disabled = true;
    document.getElementById('stopBtn').disabled = false;
    
    audioChunks = [];
    
    // For Cordova/PhoneGap implementation
    const src = 1; // 1 for microphone
    const mediaRec = new Media('recording_' + new Date().getTime() + '.mp3', 
        function() {
            console.log("Recording started successfully");
        },
        function(err) {
            statusElement.textContent = "Recording error: " + err.message;
            console.error(err);
        });
    
    mediaRec.startRecord();
    mediaRecorder = mediaRec;
}

function stopRecording() {
    const statusElement = document.getElementById('status');
    
    if (mediaRecorder) {
        mediaRecorder.stopRecord();
        
        mediaRecorder.play(); // This is just to get the file path in this simple example
        mediaRecorder.stop(); // Immediately stop playback
        
        const recordingName = 'Recording ' + new Date().toLocaleString();
        const recordingDate = new Date().toISOString();
        const recordingPath = mediaRecorder._path;
        
        recordings.push({
            name: recordingName,
            date: recordingDate,
            path: recordingPath
        });
        
        saveRecordings();
        updateRecordingsList();
        
        statusElement.textContent = "Recording saved";
    } else {
        statusElement.textContent = "No active recording";
    }
    
    document.getElementById('startBtn').disabled = false;
    document.getElementById('stopBtn').disabled = true;
}

function saveRecordings() {
    window.localStorage.setItem('callRecordings', JSON.stringify(recordings));
}

function loadRecordings() {
    const saved = window.localStorage.getItem('callRecordings');
    if (saved) {
        recordings = JSON.parse(saved);
        updateRecordingsList();
    }
}

function updateRecordingsList() {
    const listElement = document.getElementById('recordings');
    listElement.innerHTML = '';
    
    recordings.forEach((recording, index) => {
        const li = document.createElement('li');
        li.className = 'recording-item';
        
        const date = new Date(recording.date);
        const formattedDate = date.toLocaleString();
        
        li.innerHTML = `
            <div class="recording-info">
                <div>${recording.name}</div>
                <div class="recording-date">${formattedDate}</div>
            </div>
            <div>
                <button class="play-btn" data-index="${index}">Play</button>
                <button class="delete-btn" data-index="${index}">Delete</button>
            </div>
        `;
        
        listElement.appendChild(li);
    });
    
    // Add event listeners to the new buttons
    document.querySelectorAll('.play-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            playRecording(this.getAttribute('data-index'));
        });
    });
    
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            deleteRecording(this.getAttribute('data-index'));
        });
    });
}

function playRecording(index) {
    const recording = recordings[index];
    if (recording) {
        const media = new Media(recording.path,
            function() {
                console.log("Playback finished");
            },
            function(err) {
                console.error("Playback error: ", err);
            });
        media.play();
    }
}

function deleteRecording(index) {
    if (confirm("Delete this recording?")) {
        // In a real app, you would also delete the actual file here
        recordings.splice(index, 1);
        saveRecordings();
        updateRecordingsList();
    }
}
