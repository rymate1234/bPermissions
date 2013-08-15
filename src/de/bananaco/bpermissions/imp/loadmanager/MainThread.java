package de.bananaco.bpermissions.imp.loadmanager;

import java.util.ArrayList;
import java.util.List;

import de.bananaco.bpermissions.imp.Debugger;
import de.bananaco.bpermissions.imp.loadmanager.TaskRunnable.TaskType;

public class MainThread extends Thread implements TaskThread {
	
	private static MainThread thread = new MainThread();
	
	public static MainThread getInstance() {
		return thread;
	}
	
	// a list of tasks
	private List<Runnable> load = new ArrayList<Runnable>();
	private List<Runnable> save = new ArrayList<Runnable>();
	private List<Runnable> server = new ArrayList<Runnable>();
	private boolean running = true;
	private boolean started = false;
	
	@Override
	public void run() {
		while(running) {
			check();
		}
	}
	
	/**
	 * Internal method, check scheduler
	 */
	private synchronized void check() {
    try
    {
      if (hasTasks()) {
        TaskRunnable run = null;
        List tasks = null;
        if (getTasks(TaskRunnable.TaskType.LOAD).size() > 0)
          tasks = getTasks(TaskRunnable.TaskType.LOAD);
        else if (getTasks(TaskRunnable.TaskType.SAVE).size() > 0)
          tasks = getTasks(TaskRunnable.TaskType.SAVE);
        else if (getTasks(TaskRunnable.TaskType.SERVER).size() > 0) {
          tasks = getTasks(TaskRunnable.TaskType.SERVER);
        }

        if (tasks != null) {
          run = (TaskRunnable)tasks.get(0);
          tasks.remove(0);
          TaskRunnable r = run;

          r.run();
        }
      }
      else
      {
        sleep(10L);
      }
    }catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Internal method, concurrent modification exception prevention
	 * @return List<Runnable>
	 */
	private List<Runnable> getTasks(TaskType type) {
		if(type == TaskType.LOAD) {
			return load;
		}
		if(type == TaskType.SAVE) {
			return save;
		}
		if(type == TaskType.SERVER) {
			return server;
		}
		return null;
	}
	
	// from the interface
	
	public boolean hasTasks() {
		return load.size() + save.size() + server.size() > 0;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(final boolean running) {
		load.clear();
		save.clear();
		server.clear();
		if(!running) {
			thread.running = false;
			thread = null;
		}
	}
	
	public boolean getStarted() {
		return started;
	}
	
	public void setStarted(final boolean started) {
		TaskRunnable r = new TaskRunnable() {
			public void run() {
				thread.started = started;
				Debugger.log("Set started: "+started);
			}
			public TaskType getType() {
				return TaskType.SERVER;
			}
		};
		schedule(r);
	}
	
	public void schedule(TaskRunnable r) {
		getTasks(r.getType()).add(r);
	}

}
