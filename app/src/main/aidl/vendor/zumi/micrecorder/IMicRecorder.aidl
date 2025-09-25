package vendor.zumi.micrecorder;

interface IMicRecorder {
    boolean recordMicrophone(in String filePath, int device, int micIndex,
                             int sampleRate, int bitsPerSample, int durationSec);

    void stopRecording(); // New method to stop recording early
}

