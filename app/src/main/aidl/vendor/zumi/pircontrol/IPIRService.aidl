package vendor.zumi.pircontrol;

interface IPIRService {
    // Open the I²C device with given bus + slave address
    boolean openDevice(int bus, int address);

    // Close the I²C device
    void closeDevice();

    // Initialize PIR sensor (sysfs or default setup)
    boolean initPir();

    // Read PIR state (true = presence detected, false = none)
    boolean readPir();

    // Generic read register
    int readRegister(int reg);

    // Generic write register
    boolean writeRegister(int reg, int value);
}

