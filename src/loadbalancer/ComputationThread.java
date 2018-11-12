package loadbalancer;

import computationnode.ComputationNodeStub;
import computationnode.UnAuthorizedException;
import computationnode.UnAvailableException;
import shared.ComputationNodeInterface;

import java.rmi.RemoteException;

public class ComputationThread extends Thread {
    private ComputationNodeStub computationNode;
    private String[] task;
    private LoadBalancerInfo loadBalancerInfo;

    public ComputationThread(ComputationNodeStub computationNode, String[] task, LoadBalancerInfo loadBalancerInfo) {
        this.computationNode = computationNode;
        this.task = task;
        this.loadBalancerInfo = loadBalancerInfo;
    }
    public void run()
    {
        try
        {
            int result = computationNode.instance.execute(loadBalancerInfo, task);
            synchronized (LoadBalancer.result) {
               LoadBalancer.result += result;
               LoadBalancer.result = LoadBalancer.result % 4000;
            }
            synchronized (LoadBalancer.finishedComputations) {
               LoadBalancer.finishedComputations += task.length;
            }
            synchronized (LoadBalancer.inactiveNodes){
               LoadBalancer.inactiveNodes.add(this.computationNode);
            }
        }
        catch (RemoteException | UnAvailableException | UnAuthorizedException e)
        {
		System.err.println("exception: " + e.getMessage());
            synchronized (LoadBalancer.operationQueue) {
                for (int i = 0; i < task.length; i++) {
                    LoadBalancer.operationQueue.push(task[i]);
                }
            }
        }
    }
}


