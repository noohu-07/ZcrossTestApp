// IAdcService.aidl
package vendor.zumi.adc;

// Declare any non-default types here with import statements

interface IAdcService {
    /**
     * Reads all available ADC channels and returns their voltages in mV.
     * @return Array of strings in the format: "Channel N: Raw = X, Voltage = Y mV"
     */
    List<String> readAdcChannels();
}