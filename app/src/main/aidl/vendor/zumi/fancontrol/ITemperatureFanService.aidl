package vendor.zumi.fancontrol;


interface ITemperatureFanService {

    List<String> readAllSensorTemperatures();

    int getAverageTemperature();

    int controlFanBasedOnAverageTemperature();
}

