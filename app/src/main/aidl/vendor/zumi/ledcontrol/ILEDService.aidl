package vendor.zumi.ledcontrol;

interface ILEDService  {
     // Configure UART device path
       void setDevicePath(String path);

       // Write data (color codes) to UART
       boolean writeData(String data);

       // Read response from UART
       String readData();
}

