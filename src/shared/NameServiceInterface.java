package shared;

import computationnode.ComputationNodeInfo;
import loadbalancer.LoadBalancer;
import loadbalancer.LoadBalancerInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface NameServiceInterface extends Remote {
    void register(ComputationNodeInfo computationNodeInfo) throws RemoteException;
    ArrayList<ComputationNodeInfo> getComputationNodes() throws RemoteException;
    void signUp(LoadBalancerInfo loadbalancer) throws  RemoteException;
    Boolean verify(String ip, LoadBalancerInfo loadBalancerInfo) throws RemoteException;
}

