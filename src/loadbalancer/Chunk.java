package loadbalancer;

import java.util.*;
import computationnode.ComputationNodeStub;


public class Chunk {
    public ArrayList<Integer> results = new ArrayList<Integer>();

    public Chunk() {
        LoadBalancer.enqueueInactiveNodes();
        for (int i = 0; i < 2; ++i)
            this.computes.add(LoadBalancer.getBestAvailableNode());

        this.numberOfOperations = Math.min(this.computes.get(0).computationCapacity,
                this.computes.get(1).computationCapacity);
        this.numberOfOperations = Math.min(this.numberOfOperations,
                LoadBalancer.operationQueue.size());
        fetchOperations(this.numberOfOperations);
        this.compute();
    }

    public void compute() {
        DualComputationThread tids[] = new DualComputationThread[2];
        for (int i = 0; i < N_COMPUTES; ++i)
            tids[i] = new DualComputationThread(this.computes.get(i), this);
        try {
            for (int i = 0; i < N_COMPUTES; ++i) tids[i].join();
        } catch(Exception e) {
            System.err.println(e);
            System.exit(-4);
        }

        if (this.results.get(0).intValue() == this.results.get(1).intValue()) {
            LoadBalancer.result += this.results.get(0);
            LoadBalancer.result %= 4000;
            LoadBalancer.completedOperations += this.numberOfOperations;
        } else {
            requeueOperations();
        }
        LoadBalancer.chunkArrayList.remove(this);
    }

    public String[] getOperations() {
        return this.operations.toArray(new String[this.operations.size()]);
    }

    private final int N_COMPUTES = 2;
    private int numberOfOperations;
    private ArrayList<String> operations = new ArrayList<String>();
    private ArrayList<ComputationNodeStub> computes = new ArrayList<ComputationNodeStub>();

    private void fetchOperations(int n) {
        for (int i = 0; i < n; ++i){
            this.operations.add(LoadBalancer.operationQueue.pop());
        }
    }
    private void requeueOperations() {
        for (int i = 0; i < this.numberOfOperations; ++i) {
            LoadBalancer.operationQueue.add(this.operations.get(i));
        }
    }
}
