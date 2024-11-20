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
import star.vis.*;import star.base.neo.NamedObject;
import star.base.report.PhysicalTimeMonitor;
import star.common.*;
import star.vis.*;

import java.awt.*;

public class Create_Plot_Scene extends StarMacro{
    public void execute(){
        execute0();
    }
    private void execute0(){
        Simulation sim = getActiveSimulation();
        sim.println("\nWorking simulation is < " + sim.getPresentationName() + " >. Find under running information. \n");
        Units units_0 = sim.getUnitsManager().getObject("s");

//      create parameter for save file time frequency. Default is 25s. can modify it in CCM after run script.

        try {
            GlobalParameterManager globalPM = sim.get(GlobalParameterManager.class);
            String parameterName = "SaveTimeFreq";
            ScalarGlobalParameter existingParameter = null;
            for (NamedObject obj: globalPM.getObjects()) {
                if (obj instanceof ScalarGlobalParameter param) {
                    if (param.getPresentationName().equals(parameterName)) {
                        existingParameter = param;
                        break;
                    }
                }
            }
            if (existingParameter == null){
                ScalarGlobalParameter scalarGP_0 = sim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class,parameterName);
                scalarGP_0.setDimensions(Dimensions.Builder().time(1).build());
                scalarGP_0.getQuantity().setValueAndUnits(25.0, units_0);
                sim.println("Save Time Frequency < " + scalarGP_0.getPresentationName() + " > is created.\n");
            } else {
                sim.println("Parameter < " + parameterName + " > already exists.\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int c = 3;
        int N_plot = 0;
        int N_scene = 0;

//  create monitor plot
        for (int i = 1; i<=c; i++){

            MonitorPlot MP_0 = sim.getPlotManager().create("star.common.MonitorPlot");
            MP_0.setPresentationName("MonitorPlot_" + i);
            MP_0.setTitleFont(new Font("Siemens Sans Global", Font.PLAIN, 30));
            MP_0.setXAxisMonitor((PhysicalTimeMonitor)sim.getMonitorManager().getMonitor("Physical Time"));

//      modify properties of Bottom Axis
            Cartesian2DAxisManager cartesian2DAxisManager_0 = ((Cartesian2DAxisManager) MP_0.getAxisManager());
            Cartesian2DAxis cartesian2DAxis_b = ((Cartesian2DAxis) cartesian2DAxisManager_0.getAxis("Bottom Axis"));
            cartesian2DAxis_b.getTitle().setFont(new Font("Siemens Sans Global", Font.PLAIN, 24));
            cartesian2DAxis_b.getTitle().setText("Time (s)");
            cartesian2DAxis_b.getLabels().setFont(new Font("Siemens Sans Global", Font.PLAIN, 24));

//      modify properties of Left Axis
            Cartesian2DAxis cartesian2DAxis_l = ((Cartesian2DAxis) cartesian2DAxisManager_0.getAxis("Left Axis"));
            cartesian2DAxis_l.getTitle().setFont(new Font("Siemens Sans Global", Font.PLAIN, 24));
            cartesian2DAxis_l.getTitle().setText("Temp (C)");
            cartesian2DAxis_l.getLabels().setFont(new Font("Siemens Sans Global", Font.PLAIN, 24));

//      modify properties of Right Axis
            Cartesian2DAxis cartesian2DAxis_r = ((Cartesian2DAxis) cartesian2DAxisManager_0.createAxis(Cartesian2DAxis.Position.Right));
            cartesian2DAxis_r.getTitle().setFont(new Font("Siemens Sans Global", Font.PLAIN, 24));
            cartesian2DAxis_r.getTitle().setText("Current (A)");
            cartesian2DAxis_r.getLabels().setFont(new Font("Siemens Sans Global", Font.PLAIN, 24));

//      modify property of Legend
            MP_0.getLegend().setFont(new Font("Siemens Sans Global", Font.PLAIN, 24));

//      change curve color
            MP_0.getChartStyleAssignment().setColorPalette(sim.get(ColorPaletteManager.class).getObject("High Contrast Color Palette"));

//      modify properties of AutoSaveUpdate
            MP_0.getPlotUpdate().getUpdateModeOption().setSelected(StarUpdateModeOption.Type.DELTATIME);
            MP_0.getPlotUpdate().setSaveAnimation(true);
            MP_0.getPlotUpdate().setAnimationFilenameBase("MonitorPlot_" + i);
            MP_0.getPlotUpdate().setAnimationFilePath("Plot/");
            MP_0.getPlotUpdate().getDeltaTimeUpdateFrequency().setDeltaTime("${SaveTimeFreq}", units_0);
            MP_0.getPlotUpdate().getHardcopyProperties().setUseCurrentResolution(false);
            MP_0.getPlotUpdate().getHardcopyProperties().setOutputHeight(1080);
            MP_0.getPlotUpdate().getHardcopyProperties().setOutputWidth(1920);

            N_plot++;
        }
        sim.println("Total * " + N_plot + " * plots are created. \n");

//  create scalar scene
        for (int j = 1; j <= c; j++){

            sim.getSceneManager().createEmptyScene("ScalarScene", null);
            Scene ss_0 = sim.getSceneManager().getScene("ScalarScene "+ j);
            ss_0.closeInteractive();

//      modify properties of scalar

            ScalarDisplayer scalar_0 = ss_0.getDisplayerManager().createScalarDisplayer("Scalar", ClipMode.NONE);
            scalar_0.getLegend().setLookupTable(sim.get(LookupTableManager.class).getObject("blue-red"));
            scalar_0.setFillMode(ScalarFillMode.NODE_FILLED);
            scalar_0.getScalarDisplayQuantity().setFieldFunction(sim.getFieldFunctionManager().getFunction("Temperature"));
            Units units_1 = sim.getUnitsManager().getObject("C");
            scalar_0.getScalarDisplayQuantity().setUnits(units_1);
            scalar_0.getScalarDisplayQuantity().getMinimumValue().setValueAndUnits(25, units_1)
            ;
            scalar_0.getScalarDisplayQuantity().getMaximumValue().setValueAndUnits(100, units_1);

//      modify properties of AutoSaveUpdate
            ss_0.getSceneUpdate().getUpdateModeOption().setSelected(StarUpdateModeOption.Type.DELTATIME);
            ss_0.getSceneUpdate().setSaveAnimation(true);
            ss_0.getSceneUpdate().setAnimationFilenameBase("ScalarScene_" + j);
            ss_0.getSceneUpdate().setAnimationFilePath("ClPt/");
            ss_0.getSceneUpdate().getDeltaTimeUpdateFrequency().setDeltaTime("${SaveTimeFreq}", units_0);
            ss_0.getSceneUpdate().getHardcopyProperties().setUseCurrentResolution(false);
            ss_0.getSceneUpdate().getHardcopyProperties().setOutputHeight(1080);
            ss_0.getSceneUpdate().getHardcopyProperties().setOutputWidth(1920);
            ss_0.getAnnotationPropManager().getAnnotationGroup().addObjects(sim.getAnnotationManager().getObject("Solution Time"));

            N_scene++;
        }
        sim.println("Total * " + N_scene + " * scenes are created. \n");
    }
}

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
