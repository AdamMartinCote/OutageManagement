package computationnode;

import loadbalancer.LoadBalancerInfo;
import shared.NameServiceInterface;
import shared.ComputationNodeInterface;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import utils.Operations;

public class ComputationNode implements ComputationNodeInterface {
    private final String PELL = "pell";
    private final String PRIME = "prime";
    private final int UNAUTHORIZED = -1;
    private Integer computationCapacity;
    private Double failureRate;
    private String ipAddr;
    private String nameServiceIp;
    private String id;
    private ArrayList<String> authorizedLoadBalancers;
    private NameServiceInterface nameServerStub;
    private static int portNumber;
    private static int nameServicePort;
    public static ComputationNodeInterface stub;
    public static Registry reg;

    public ComputationNode(Integer computationCapacity, Double failureRate,
                           String nameServiceIp, String externalIp, String id) {
        this.computationCapacity = computationCapacity;
        this.failureRate = failureRate;
        this.nameServiceIp = nameServiceIp;
        this.ipAddr = externalIp;
        this.id = id;
        this.authorizedLoadBalancers = new ArrayList<String>();
    }

    public static void main(String[] args) {

        Integer computationCapacity = Integer.parseInt(args[0]);
        Double failurePercentage = Double.parseDouble(args[1]);
        String nameServiceIp = args[2];
        String ipAddr = args[3];
        ComputationNode.portNumber = Integer.parseInt(args[4]);
        ComputationNode.nameServicePort = Integer.parseInt(args[5]);
        String id = UUID.randomUUID().toString();
        ComputationNode computationNode = new ComputationNode(computationCapacity,
                failurePercentage, nameServiceIp, ipAddr, id);

        computationNode.run();
        while(true);
    }

    public void run() {
        try {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            // regiser computation node as a network object
            ComputationNode.stub = (ComputationNodeInterface) UnicastRemoteObject
                                .exportObject(this, this.portNumber);
            LocateRegistry.createRegistry(this.portNumber);

            ComputationNode.reg = LocateRegistry.getRegistry(this.portNumber);
            ComputationNode.reg.rebind(this.id, this);
            // get nameService
            Registry reg = LocateRegistry.getRegistry(nameServiceIp, ComputationNode.nameServicePort);
            this.nameServerStub = (NameServiceInterface) reg.lookup("NameService");

            nameServerStub.register(new ComputationNodeInfo(this.ipAddr, this.computationCapacity,
                    this.id, this.portNumber));

        }  catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Computation node is ready and registered");
        while(true);

    }

    public int compute(String command) {
        final int MAX_RETURN_VAL = 300;
        Random rand = new Random();
        int n = rand.nextInt(100) + 1;
        if (n < this.failureRate) {
            System.out.println("Compute is lying");
            return n = rand.nextInt(MAX_RETURN_VAL) + 1;
        }

        String[] args = command.split("\\s+");
        if (args[0].equals(PELL)) {
            return Operations.pell(Integer.parseInt(args[1]));
        } else if(args[0].equals(PRIME)) {
            return Operations.prime(Integer.parseInt(args[1]));
        } else {
            // TODO : handle error
            return -1;
        }
    }

    public int execute(LoadBalancerInfo loadBalancerInfo, String[] task) throws UnAvailableException, UnAuthorizedException {
        try {
            String loadBalancerIp = RemoteServer.getClientHost();
            if(!authorizedLoadBalancers.contains(loadBalancerIp)) {
                if(!this.nameServerStub.verify(loadBalancerIp, loadBalancerInfo)) {
                    throw new UnAuthorizedException("Wrong credentials");
                } else {
                    authorizedLoadBalancers.add(loadBalancerIp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!this.computationIsPossible(task.length)) throw new UnAvailableException("Node is currently unavailable for this task");
        Integer result = 0;
        for (int i = 0; i < task.length; i++) {
            result += compute(task[i]);
            result = result % 4000;
        }
        return result;
    }

    private boolean computationIsPossible(Integer computationQty) {
        Double failureProbabilty = (double)(computationQty - this.computationCapacity) / (double)(4 * this.computationCapacity);
        Random random = new Random();
        return !(random.nextDouble() < failureProbabilty);
    }

}
