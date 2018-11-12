package computationnode;

import shared.ComputationNodeInterface;

public class ComputationNodeStub {
    public ComputationNodeInterface instance;
    public String ipAddress;
    public Integer computationCapacity;

    public ComputationNodeStub(ComputationNodeInterface stub, String ipAddress, Integer computationCapacity) {
        this.instance = stub;
        this.ipAddress = ipAddress;
        this.computationCapacity = computationCapacity;
    }
}
