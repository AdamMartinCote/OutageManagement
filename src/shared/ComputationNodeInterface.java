package shared;

import computationnode.UnAuthorizedException;
import computationnode.UnAvailableException;
import loadbalancer.LoadBalancerInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ComputationNodeInterface extends Remote {
    public int  execute(LoadBalancerInfo loadBalancerInfo, String[] task) throws RemoteException, UnAuthorizedException, UnAvailableException;
}
