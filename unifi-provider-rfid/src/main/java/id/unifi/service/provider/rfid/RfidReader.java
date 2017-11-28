package id.unifi.service.provider.rfid;

public class RfidReader {
    private final String serialNumber;
    private final String modelName;
    private final RfidReaderStatus status;

    RfidReader(String serialNumber, String modelName, RfidReaderStatus status) {
        this.serialNumber = serialNumber;
        this.modelName = modelName;
        this.status = status;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getModelName() {
        return modelName;
    }

    public RfidReaderStatus getStatus() {
        return status;
    }

    public String toString() {
        return "RfidReader{" +
                "serialNumber='" + serialNumber + '\'' +
                ", modelName='" + modelName + '\'' +
                ", status=" + status +
                '}';
    }
}
