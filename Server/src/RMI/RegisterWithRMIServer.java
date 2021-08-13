package RMI;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegisterWithRMIServer {
        public static void main(String[] args) throws IOException, AlreadyBoundException {

        System.setSecurityManager(new SecurityManager());

        ServerInterface obj = new RMI_Server();
        Registry registry = LocateRegistry.createRegistry(1001);
        registry.bind("RemoteObjectName", obj);
        System.out.println("Sudoku server " + obj + " registered");
        System.out.println("Press Return to quit.");
        int key = System.in.read();
        System.exit(0);

    }
}
