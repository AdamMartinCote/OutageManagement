package loadbalancer;

import computationnode.ComputationNode;
import computationnode.ComputationNodeStub;
import shared.ComputationNodeInterface;
import shared.NameServiceInterface;

import computationnode.ComputationNodeInfo;
import loadbalancer.Chunk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    public static volatile Integer result = 0;
    public static volatile Integer finishedComputations = 0;
    public static LinkedList<ComputationNodeStub> inactiveNodes = new LinkedList<ComputationNodeStub>();
    public static String operationFilePath = "";
    public static LinkedList<String> operationQueue = new LinkedList<String>();
    public static TreeMap<Integer, LinkedList<ComputationNodeStub>> computationNodes = new TreeMap<Integer, LinkedList<ComputationNodeStub>>();
    public static int computationsQty = 0;
    private static int nameServicePort;
    public static int completedOperations = 0;
    public static ArrayList<Chunk> chunkArrayList = new ArrayList<Chunk>();
    public static LoadBalancerInfo info;
    public static void main(String[] args) {

        LoadBalancer.nameServicePort = Integer.parseInt(args[3]);
        Boolean computationFinished = false;
        String operationFilePath = args[0];
        String nameServiceIp = args[1];
        Boolean unsecure = args[2].equals("yes");
        if (unsecure) {
            System.out.println("** Using unsecure mode **");
        }
        LoadBalancer.operationFilePath = operationFilePath;
        LoadBalancer.parseOperationFile();
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = reader.nextLine();
        System.out.println("Enter your password: ");
        String password = reader.nextLine();
        reader.close();
        long startTime = System.nanoTime();
        LoadBalancer.info = new LoadBalancerInfo(username, password);
        try {
            // get nameService
            Registry registry = LocateRegistry.getRegistry(nameServiceIp, LoadBalancer.nameServicePort);
            NameServiceInterface nameServerStub = (NameServiceInterface) registry.lookup("NameService");
            nameServerStub.signUp(LoadBalancer.info);
            ArrayList<ComputationNodeInfo> computationNodesInfo = nameServerStub.getComputationNodes();
            System.out.println("Received : " + computationNodesInfo.size() + " nodes");
            LoadBalancer.populateComputationNodesTree(computationNodesInfo);
            System.out.println("LoadBalancer has to compute: " + LoadBalancer.computationsQty + " operations");
            if (unsecure) {
                computeSecurely();
            } else {
                computeSimple();
            }
            long endTime   = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println("Result is : " + LoadBalancer.result);
            System.out.println("Computation took: " + totalTime/1e9 + " seconds");
        } catch (Exception e) {
            System.out.println(LoadBalancer.operationQueue.size());
            e.printStackTrace();
        }

    }

    private static void computeSimple() {
        Boolean computationFinished = false;
        while (!computationFinished) {
            if(LoadBalancer.finishedComputations == LoadBalancer.computationsQty) computationFinished = true;

            LoadBalancer.enqueueInactiveNodes();
            ComputationNodeStub bestCurrentNode =  LoadBalancer.getBestAvailableNode();
            Iterator<String> it = LoadBalancer.operationQueue.iterator();
            Integer computationQty = LoadBalancer.bestComputationQty(bestCurrentNode);
            // To make sure we dont bust array index
            int computationToDo = Math.min(computationQty, LoadBalancer.operationQueue.size());
            String[] task = new String[computationToDo];
            if (task.length > 0) {
                for (int i = 0; i < task.length; i++) {
                    synchronized (LoadBalancer.operationQueue) {
                        task[i] = LoadBalancer.operationQueue.pop();
                    }
                }
                new ComputationThread(bestCurrentNode, task, LoadBalancer.info).start();
            }
        }
    }

    private static void computeSecurely() {
        while (LoadBalancer.operationQueue.size() > 0
                && LoadBalancer.completedOperations < computationsQty) {
            LoadBalancer.chunkArrayList.add(new Chunk());
        }
    }

    public static void parseOperationFile() {
        BufferedReader reader;
        try {
            File file = new File(LoadBalancer.operationFilePath);
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                LoadBalancer.operationQueue.add(line);
                LoadBalancer.computationsQty++;
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void populateComputationNodesTree(ArrayList<ComputationNodeInfo> computationNodeInfos) {
        Iterator<ComputationNodeInfo> it = computationNodeInfos.iterator();
        while (it.hasNext()) {
            try {
                ComputationNodeInfo node = it.next();
                Registry registry = LocateRegistry.getRegistry(node.ipAdress, node.portNumber);
                for(int i = 0; i < 1; i ++) {

                }
                
                ComputationNodeInterface computationNode = (ComputationNodeInterface) registry.lookup(node.id);
                ComputationNodeStub stub = new ComputationNodeStub(computationNode, node.ipAdress, node.computationCapacity);
                if (LoadBalancer.computationNodes.containsKey(node.computationCapacity)) {
                    LinkedList<ComputationNodeStub> queue = LoadBalancer.computationNodes.get(node.computationCapacity);
                    queue.add(stub);
                } else {
                    LinkedList<ComputationNodeStub> newQueue = new LinkedList<ComputationNodeStub>();
                    newQueue.add(stub);
                    LoadBalancer.computationNodes.put(node.computationCapacity, newQueue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ComputationNodeStub getBestAvailableNode() {
        LinkedList<ComputationNodeStub> bestAvailableCapacity = LoadBalancer.computationNodes.lastEntry().getValue();
        ComputationNodeStub bestCurrentNode = bestAvailableCapacity.pop();
        if (bestAvailableCapacity.size() == 0) {
            LoadBalancer.computationNodes.remove(bestCurrentNode.computationCapacity);
        }
        // add to invalid queue
        if (LoadBalancer.computationNodes.containsKey(0)) {
            LoadBalancer.computationNodes.get(0).add(bestCurrentNode);
        } else {
            LinkedList<ComputationNodeStub> invalidNodeQueue = new LinkedList<ComputationNodeStub>();
            invalidNodeQueue.add(bestCurrentNode);
            LoadBalancer.computationNodes.put(0, invalidNodeQueue);
        }

        return bestCurrentNode;
    }

    public static void enqueueInactiveNodes() {
        while (LoadBalancer.inactiveNodes.size() > 0) {
            synchronized (LoadBalancer.inactiveNodes) {
                ComputationNodeStub node = LoadBalancer.inactiveNodes.pop();
                if (LoadBalancer.computationNodes.containsKey(node.computationCapacity)) {
                    LinkedList<ComputationNodeStub> queue = LoadBalancer.computationNodes.get(node.computationCapacity);
                    queue.add(node);
                } else {
                    LinkedList<ComputationNodeStub> newQueue = new LinkedList<ComputationNodeStub>();
                    newQueue.add(node);
                    LoadBalancer.computationNodes.put(node.computationCapacity, newQueue);
                }
            }
        }
    }
    public static Integer bestComputationQty(ComputationNodeStub computationNode) {
        Double qty =  ((double)(computationNode.computationCapacity * 5)) / 2;
        return qty.intValue();
    }

}

