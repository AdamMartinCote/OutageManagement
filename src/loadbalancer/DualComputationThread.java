package loadbalancer;

import computationnode.ComputationNodeStub;
import shared.ComputationNodeInterface;

import java.rmi.RemoteException;
import computationnode.ComputationNodeStub;
import computationnode.UnAuthorizedException;
import computationnode.UnAvailableException;

public class DualComputationThread extends Thread {
    private ComputationNodeStub computationNode;
    private String[] task;
    private int pairNumber;
    private Chunk chunk;

    public DualComputationThread(ComputationNodeStub computationNode, Chunk chunk) {
        this.computationNode = computationNode;
        this.chunk = chunk;
        this.run();
    }

    public void run() {
        try {
            this.chunk.results.add(computationNode.instance.execute(LoadBalancer.info, chunk.getOperations()));
        } catch(RemoteException | UnAvailableException | UnAuthorizedException e) {
            System.err.println("RemoteException in DualComputationThread");
        }
    }
}


