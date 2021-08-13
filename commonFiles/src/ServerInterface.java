import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    boolean register(String name, String password) throws RemoteException, IOException;
    boolean login(String name, String password) throws RemoteException;
    void generateSudoku(Difficulty d) throws RemoteException;
    int[][] getSudoku() throws RemoteException;
    int[][] getSolvedSudoku() throws RemoteException;
    void finishGame(String name, Difficulty difficulty, boolean won, int time) throws IOException;
}
