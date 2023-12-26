package lk.dharanimart.mobile;

import android.os.AsyncTask;
import java.util.HashMap;
import java.util.Map;

public class TaskManager {

    private static final Map<String, AsyncTask<?, ?, ?>> taskMap = new HashMap<>();

    public static void executeTask(String identifier, AsyncTask<?, ?, ?> task) {
        taskMap.put(identifier, task);
        task.execute();
    }

    public static void cancelTasksByIdentifier(String identifier) {
        AsyncTask<?, ?, ?> task = taskMap.get(identifier);
        if (task != null) {
            task.cancel(true);
            taskMap.remove(identifier);
        }
    }
}
