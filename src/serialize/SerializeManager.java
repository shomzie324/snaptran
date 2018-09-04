package serialize;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Observable;
import log.LogManager;
import system.TransitSystem;

public class SerializeManager extends Observable {

  private static final String filePath = "src/serialize/serial";
  private static SerializeManager instance;
  private TransitSystem transitSystem;

  private SerializeManager() {
    instance = this;
    addObserver(LogManager.getInstance());
    setChanged();
    notifyObservers("Serialize Manager is initialized!");
  }

  /**
   * Get a instance of SerializeManager.
   *
   * @return an instance of SerializeManager.
   */
  public static SerializeManager getInstance() {
    if (instance == null) {
      instance = new SerializeManager();
    }
    return instance;
  }

  public void setTransitSystem(TransitSystem transitSystem) {
    this.transitSystem = transitSystem;
  }

  /** Write the object in serial file. */
  public void writeObject() {
    try (ObjectOutputStream objectOutputStream =
        new ObjectOutputStream(new FileOutputStream(filePath))) {
      objectOutputStream.writeObject(transitSystem);
    } catch (IOException e) {
      System.out.println("Fail to Serialize:" + e);
    }
  }

  /** Read the object from serial file. */
  public Object readObject() throws IOException, ClassNotFoundException {
    /* Object Deserialization */
    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filePath));
    Object result = objectInputStream.readObject();
    if (result != null) {
      setChanged();
      notifyObservers("Transit System De-serialized!");
    }
    return result;
  }
}
