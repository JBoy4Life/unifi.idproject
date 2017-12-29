package id.unifi.service.common.rfid;

public class RfidReader {
    private final String sn;
    private final String modelName;
    private final RfidReaderStatus status;

    public RfidReader(String sn, String modelName, RfidReaderStatus status) {
        this.sn = sn;
        this.modelName = modelName;
        this.status = status;
    }

    public String getSn() {
        return sn;
    }

    public String getModelName() {
        return modelName;
    }

    public RfidReaderStatus getStatus() {
        return status;
    }

    public String toString() {
        return "RfidReader{" +
                "sn='" + sn + '\'' +
                ", modelName='" + modelName + '\'' +
                ", status=" + status +
                '}';
    }
}
