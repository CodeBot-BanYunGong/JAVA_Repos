// Simcenter STAR-CCM+ macro: PlotAndScene01.java
// Written by Simcenter STAR-CCM+ 18.06.007
package macro;

import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.regex.Pattern; // Add this import statement
import java.util.regex.Matcher; // Add this if you need to use Matcher class

import star.common.*;
import star.base.neo.*;
import star.vis.*;
import star.base.report.*;
import star.meshing.*;

public class Plot_Scene_Create extends StarMacro{
	
/* 	private static final String DELIMITER = ",";
	private static final String CSV_TABLEFILE= "plot_scene.csv"; // Update csv file name if needed. 
*/
	
	public void execute(){
		execute0();
	}
	
	private void execute0(){
		
		// Read Simulation and show its name.
		Simulation sim = getActiveSimulation();
		sim.println("Working simulation is < " + sim.getPresentationName() + " > . Find under running infomation.\n");
		sim.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
		
		/* File filePlotList = new File(resolvePath(CSV_TABLEFILE));
		
		try (BufferedReader bR = new BufferedReader(new FileReader(filePlotList))){
			// Skip first line - headers.
			bR.readLine();
			
			String line;
			int n = 0;
			
			while ((line = bR.readLine()) != null) {
				// Read string components.
				String[] plotList = line.split(DELIMITER);
				
			}
		} catch (IOException e){
			e.printStackTrace();
		} 
		*/
		
		// Create parameter for save time frequency. Default value is 25s. Can modify it in CCM after run script. 
		ScalarGlobalParameter scalarGP_0 = sim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "SaveTimeFreq");
		scalarGP_0.setDimensions(Dimensions.Builder().time(1).build());
		Units units_0 = ((Units) sim.getUnitsManager().getObject("s"));
		scalarGP_0.getQuantity().setValueAndUnits(25.0, units_0);
		sim.println("Save time frequency < " + scalarGP_0.getPresentationName() + " > is defined");
		int count = 0;
		
		for (int i =1; i<=10; i++){
			// Create monitor plot
			MonitorPlot monitorPlot_0 = sim.getPlotManager().create("star.common.MonitorPlot");
			monitorPlot_0.setPresentationName("MonitorPlot_" + i);
			Cartesian2DAxisManager cartesian2DAxisManager_0 = ((Cartesian2DAxisManager) monitorPlot_0.getAxisManager());
			Cartesian2DAxis cartesian2DAxis_0 = (Cartesian2DAxis) cartesian2DAxisManager_0.createAxis(Cartesian2DAxis.Position.Right);
			cartesian2DAxis_0.setVisible(false);
			PlotUpdate plotUpdate_0 = monitorPlot_0.getPlotUpdate();
			plotUpdate_0.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.DELTATIME);
			plotUpdate_0.setSaveAnimation(true);
			plotUpdate_0.setAnimationFilePath("plot\\");
			Units units_1 = sim.getUnitsManager().getPreferredUnits(Dimensions.Builder().time(1).build());
			DeltaTimeUpdateFrequency deltaTimeUpdateFrequency_0 = plotUpdate_0.getDeltaTimeUpdateFrequency();
			deltaTimeUpdateFrequency_0.setDeltaTime("${SaveTimeFreq}", units_1);
			
			// Create scalar scene
			sim.getSceneManager().createEmptyScene("Scene", null);
			Scene scene_0 = sim.getSceneManager().getScene("Scene " + i);
			CurrentView currentView_0 = scene_0.getCurrentView();
			currentView_0.setInput(new DoubleVector(new double[] {0.0, 0.0, 0.0}), new DoubleVector(new double[] {0.0, 0.0, 3.3460652149512318}), new DoubleVector(new double[] {0.0, 1.0, 0.0}), 0.8734983028551053, 1, 30.0);
			scene_0.closeInteractive();
			ScalarDisplayer scalarDisplayer_0 = scene_0.getDisplayerManager().createScalarDisplayer("Scalar", ClipMode.NONE);
			
			//Update Scalar Field Properties for scalar displayer
			PrimitiveFieldFunction primitiveFieldFunction_0 = ((PrimitiveFieldFunction) sim.getFieldFunctionManager().getFunction("Temperature"));
			scalarDisplayer_0.getScalarDisplayQuantity().setFieldFunction(primitiveFieldFunction_0);
			Units units_2 = ((Units) sim.getUnitsManager().getObject("C"));
			scalarDisplayer_0.getScalarDisplayQuantity().setUnits(units_2);
			scalarDisplayer_0.getScalarDisplayQuantity().getMinimumValue().setValueAndUnits(25.0, units_2);
			scalarDisplayer_0.getScalarDisplayQuantity().getMaximumValue().setValueAndUnits(100.0, units_2);
			
			//Update Color Bar for scalar displayer
			Legend legend_0 = scalarDisplayer_0.getLegend();
			BlueRedLookupTable blueRedLookupTable_0 = ((BlueRedLookupTable) sim.get(LookupTableManager.class).getObject("blue-red"));
			legend_0.setLookupTable(blueRedLookupTable_0);
			
			//Update Auto Save for scene
			SceneUpdate sceneUpdate_0 = scene_0.getSceneUpdate();
			sceneUpdate_0.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.DELTATIME);
			sceneUpdate_0.setSaveAnimation(true);
			sceneUpdate_0.setAnimationFilePath("scene\\");
			DeltaTimeUpdateFrequency deltaTimeUpdateFrequency_1 = sceneUpdate_0.getDeltaTimeUpdateFrequency();
			deltaTimeUpdateFrequency_1.setDeltaTime("${SaveTimeFreq}", units_1);
			PhysicalTimeAnnotation physicalTimeAnnotation_0 = ((PhysicalTimeAnnotation) sim.getAnnotationManager().getObject("Solution Time"));
			scene_0.getAnnotationPropManager().getAnnotationGroup().addObjects(physicalTimeAnnotation_0);
			
			count++;
		}
		sim.println("Total " + count + " plots are created. ");
		sim.println("Total " + count + " scenes are created. ");
		sim.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
	}
}