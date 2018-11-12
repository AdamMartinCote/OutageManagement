package computationnode;
import java.io.Serializable;

public class ComputationNodeInfo implements Serializable {
    public String ipAdress;
    public Integer computationCapacity;
    public String id;
    public int portNumber;

    public ComputationNodeInfo(String ipAdress, Integer computationCapacity, String id, int portNumber) {
        this.ipAdress = ipAdress;
        this.computationCapacity = computationCapacity;
        this.id = id;
        this.portNumber = portNumber;
    }
}
