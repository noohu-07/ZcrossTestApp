package vendor.zumi.miccontroller;

interface IMicController {
    /**
     * Set MICFIL Dataline value (0–3)
     */
    void setMicfilDataline(int value);

    /**
     * Returns true if mic is currently muted
     */
    boolean isMuted();
}

