package nameservice;

import computationnode.ComputationNodeInfo;
import loadbalancer.LoadBalancer;
import loadbalancer.LoadBalancerInfo;
import shared.NameServiceInterface;

import javax.management.remote.rmi.RMIServer;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class NameService implements NameServiceInterface {
    private ArrayList<ComputationNodeInfo> computationNodes;
    private HashMap<String, LoadBalancerInfo> authorizedLoadBalancers;
    private static int portNumber;

    public static void main(String[] args) {
        NameService.portNumber = Integer.parseInt(args[0]);
	    NameService nameService = new NameService();
        nameService.run();
    }

    public NameService() {
        super();
        this.computationNodes = new ArrayList<ComputationNodeInfo>();
        this.authorizedLoadBalancers = new HashMap<String, LoadBalancerInfo>();
    }

    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            LocateRegistry.createRegistry(NameService.portNumber);
            NameServiceInterface stub = (NameServiceInterface) UnicastRemoteObject
                    .exportObject(this, NameService.portNumber);

            Registry registry = LocateRegistry.getRegistry(NameService.portNumber);
            registry.rebind("NameService", stub);
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est partie ?");
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        System.out.println("Name service is ready");
    }


    /*
     * Interface RMI
     *
     */

    public void register(ComputationNodeInfo computationNodeInfo) {
        this.computationNodes.add(computationNodeInfo);
        System.out.println("New node connected, number of available nodes : " + this.computationNodes.size());
    }

    public ArrayList<ComputationNodeInfo> getComputationNodes() {
        return this.computationNodes;
    }

    public void signUp(LoadBalancerInfo loadBalancerInfo) {
        try {
            String loadBalancerIp = RemoteServer.getClientHost(); // GOOD
            this.authorizedLoadBalancers.put(loadBalancerIp, loadBalancerInfo);
            System.out.println("New load balancer signed up, ip :" + loadBalancerIp + " username: " + loadBalancerInfo.username + " password : " + loadBalancerInfo.password);
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
    }

    public Boolean verify(String ip, LoadBalancerInfo loadBalancerInfo) {
        if (!this.authorizedLoadBalancers.containsKey(ip)) {
            System.out.println("name service map doesnt contain ip");
            return false;
        }
        LoadBalancerInfo loadBalancer = this.authorizedLoadBalancers.get(ip);
        return (loadBalancer.username.equals(loadBalancerInfo.username) && loadBalancer.password.equals(loadBalancerInfo.password));
    }
}
