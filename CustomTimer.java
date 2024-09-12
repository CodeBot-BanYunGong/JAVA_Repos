/*
 */
 package Macro;
 
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;
import java.lang.*;
import java.text.SimpleDateFormat;

import star.common.*;
import star.base.neo.*;
import star.vis.*;
import star.base.report.*;
import star.meshing.*;
import star.coremodule.*;

public class CustomTimer extends StarMacro{
	
	@Override
	public void execute(){
		
		Simulation sim = getActiveSimulation();
		
		// get file directory and name
		String filePath = sim.getSessionDir();
		String fileName = sim.getPresentationName();
		
		// 创建日期格式化器
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

        // 获取当前日期时间戳
        // String timestamp = sdf.format(new Date());
		
		// define save path, 定义保存路径，可以增加日期时间戳等
		// String savePath = filePath + File.separator + fileName + "_" + timestamp + ".sim";
		String savePath = filePath + File.separator + fileName + ".sim";
		
		// create Java Timer, interval is 30min
		Timer timer = new Timer();
		timer.schedule(new SaveTask(sim, savePath), 0, 1800*1000); // 1800sec = 30min
		
		// 输出定时器创建信息
        sim.println("Timer created to save simulation every 30 minutes.");
	}
	
	private class SaveTask extends TimerTask{
		private Simulation simulation;
		private String savePath;
		
		public SaveTask(Simulation simulation, String savePath){
			this.simulation = simulation;
			this.savePath = savePath;
		}
		
		@Override
		public void run(){
			try {
				/** // 创建日期格式化器
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

                // 获取当前日期时间戳
                String timestamp = sdf.format(new Date());

                // 动态更新保存路径以包含新的时间戳
                String dynamicSavePath = savePath.replace("_backup.sim", "_" + timestamp + "_backup.sim");
				
				simulation.saveState(resolvePath(dynamicSavePath));
                simulation.println("Simulation saved to: " + dynamicSavePath);
				*/
				
				simulation.saveState(resolvePath(savePath));
				simulation.println("Simulation saved to: " + savePath);
			} catch (Exception e){
				simulation.println("Failed to save simulation: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}