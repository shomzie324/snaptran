package log;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** A log manager write log to file log.txt to record actions in this transit system. */
public class LogManager implements Observer, Serializable {

  private static LogManager instance;
  private final Logger logger;

  /** Create a new log manager. */
  private LogManager() {
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(System.currentTimeMillis());
    String filePath =
        "src/log/log_"
            + Integer.toString(date.get(Calendar.YEAR))
            + "-"
            + Integer.toString(date.get(Calendar.MONTH) + 1)
            + "-"
            + Integer.toString(date.get(Calendar.DAY_OF_MONTH))
            + "-"
            + Integer.toString(date.get(Calendar.HOUR_OF_DAY))
            + ":"
            + Integer.toString(date.get(Calendar.MINUTE))
            + ":"
            + Integer.toString(date.get(Calendar.SECOND))
            + ".txt";
    this.logger = Logger.getLogger("Transit System Logger");
    logger.setLevel(Level.ALL);
    try {
      boolean fileExist;
      File file = new File(filePath);
      fileExist = file.exists() || file.createNewFile();
      if (fileExist) {
        FileHandler fileHandler = new FileHandler(filePath);
        logger.addHandler(fileHandler);
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        fileHandler.setFormatter(simpleFormatter);
      }
    } catch (IOException e) {
      System.out.println("Exception: " + e);
    }
  }

  /**
   * Get an instance of log manager.
   *
   * @return an instance of log manager.
   */
  public static LogManager getInstance() {
    if (instance == null) {
      instance = new LogManager();
    }
    return instance;
  }

  /**
   * Once one of the observables has changed, log manager write log to file.
   *
   * @param observable the observable class that has changed
   * @param change what has changed in this observable
   */
  @Override
  public void update(Observable observable, Object change) {
    String message =
        System.lineSeparator()
            + observable.getClass()
            + " says: "
            + change
            + System.lineSeparator()
            + "-------------------------------------------------"
            + "----------------------------------------------------"
            + System.lineSeparator();
    logger.log(Level.FINE, message);
  }
}
