import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Keys {

    public static void loadKeys(Node node) {
        File file = new File(node.getId() + ".ser");
    
        if (!file.exists())
            return;
    
        try {
            FileInputStream f = new FileInputStream(file);
            ObjectInputStream o = new ObjectInputStream(f);
        
            node.setKeys((ConcurrentHashMap<Long, String>) o.readObject());
        
            o.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void saveKeys(Node node) {
        try {
            FileOutputStream f = new FileOutputStream(new File(node.getId() + ".ser"));
            ObjectOutputStream o = new ObjectOutputStream(f);
        
            o.writeObject(node.getKeys());
        
            o.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void giveKeys(Node node, Node predecessor, HashMap<Long, String> keys) {
        if (keys.size() == 0)
            return;

        Iterator<Long> it = keys.keySet().iterator();

        while (it.hasNext()) {
            long i = it.next();

            if(keys.get(i).contains("-")) {
                byte[] content = node.getStorage().readChunk(i);
                node.executor.execute(new ChunkSenderThread(node, predecessor, i, keys.get(i), content, true));
            } else {
                class SendKey implements Runnable {
                    Node sender;
                    Node receiver;
                    long key;
                    String value;

                    SendKey(Node sender, Node receiver, long key, String value) {
                        this.sender = sender;
                        this.receiver = receiver;
                        this.key = key;
                        this.value = value;
                    }

                    public void run(){
                        storeKey(receiver, sender.getId(), key, value);
                        deleteKey(sender, sender.getId(), key);
                    }
                    
                }
                node.executor.execute(new SendKey(node, predecessor, i, keys.get(i)));
            }
        }
    }

    public static void storeKey(Node node, long requestId, long encrypted, String value) {
        node.getKeys().put(encrypted, value);
        saveKeys(node);
    }

    public static void deleteKey(Node node, long requestId, long encrypted) {
        node.getKeys().remove(encrypted);
        saveKeys(node);
    }

    public static HashMap<Long, String> computeKeys(Node node, long otherId) {
        HashMap<Long, String> keysToGive = new HashMap<>();
    
        Enumeration<Long> mapKeys = node.getKeys().keys();
    
        while (mapKeys.hasMoreElements()) {
            long i = mapKeys.nextElement();
        
            if (Node.between(node.getId(), i, otherId)) {
                keysToGive.put(i, node.getKeys().get(i));
                node.getKeys().remove(i);
                saveKeys(node);
            }
        }
    
        return keysToGive;
    }
}