/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Macro;

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

/**
 * This CCM macro creates CellSet and ThresholdPart in a CCM model. 
 * The CellSet coordinates are defined in a csv file and read into the macro. 
 * csv and java macro should be located in the same directory. 
 * The csv file format contains a header in row 1.
 * Coordinates data should start in row 2. csv file format information:
 * Column 1: x location of probe point [mm]
 * Column 2: y location of probe point [mm]
 * Column 3: z location of probe point [mm]
 * Column 4: x location of probe point [mm]
 * Column 5: y location of probe point [mm]
 * Column 6: z location of probe point [mm]
 * Column 7: cellset name
 * Column 8: part name in ThresholdPart properties, that may be part or region
 * Column 9: part type, only fill "Part" or "Region"
 * Column 10: threshold part name
 */
 
public class Cellset_Update extends StarMacro {

    /**
     * @param args the command line arguments
     */
    
    private static final String DELIMITER = ",";
    private static final String CSV_TABLEFILE = "cellset.csv"; // Insert path to csv file 
    
	public void execute() {
		execute0();
	}
	
    private void execute0() {
        Simulation sim = getActiveSimulation();
		String simFileName = sim.getPresentationName();
		
        if (sim == null) {
            System.out.println("Simulation object is null. Exiting the method.");
            return;
        }
		sim.println("Working Simulation is " + simFileName + ". Find under running info.");
		sim.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv\n");
		
        File file_coord = new File(resolvePath(CSV_TABLEFILE)); // CSV File path and name
    
        try (BufferedReader in = new BufferedReader(new FileReader(file_coord))) {
            // Skip first row - headers
            in.readLine();
            
            String row;
			
            int n = 0;
            
            while((row = in.readLine()) != null) {
                //Read string components
                String[] Coord = row.split(DELIMITER);
                double xp1 = Double.parseDouble(Coord[0]);
                double yp1 = Double.parseDouble(Coord[1]);
                double zp1 = Double.parseDouble(Coord[2]);
                double xp2 = Double.parseDouble(Coord[3]);
                double yp2 = Double.parseDouble(Coord[4]);
                double zp2 = Double.parseDouble(Coord[5]);
                String CellSetName = Coord[6];
                String PartName = Coord[7];
                String PartType = Coord[8];
                String ThresholdName = Coord[9];
				
                n++;
				
				
				// Create cellset with given coordinates
				LabCoordinateSystem labCoordinateSystem_0 = sim.getCoordinateSystemManager().getLabCoordinateSystem();
				FvRepresentation fvRepresentation_0 = ((FvRepresentation) sim.getRepresentationManager().getObject("Volume Mesh"));
				
				if (fvRepresentation_0 == null){
					sim.println("Volume Mesh Representation not found");
					return;
				}
				
				CellSetManager cellSetManager_0 = fvRepresentation_0.getCellSetManager();
				CellSet cellSet_0 = null;
				
				Units units_0 =(Units) sim.getUnitsManager().getObject("mm");
				
				boolean cellSetFound = false;
				
				for (CellSet cs : cellSetManager_0.getObjects()) {
					if (cs.getPresentationName().equals(CellSetName)){
						cellSet_0 = cs;
						sim.println("CellSet " + CellSetName + " found. Replace Old CellSet.");
						cellSet_0.replaceRange(new DoubleVector(new double[] {xp1, yp1, zp1}), new DoubleVector(new double[] {xp2, yp2, zp2}), new NeoObjectVector(new Object[] {units_0, units_0, units_0}), new NeoObjectVector(new Object[] {units_0, units_0, units_0}), labCoordinateSystem_0);
						sim.println(CellSetName + " replaced @ coordinates ["+xp1+","+yp1+","+zp1+"],["+xp2+","+yp2+","+zp2+"]");
						cellSetFound = true;
						break;
					}
				}
				if (!cellSetFound){
						cellSet_0 = cellSetManager_0.createEmptyCellSet();
						cellSet_0.addRange(new DoubleVector(new double[] {xp1, yp1, zp1}), new DoubleVector(new double[] {xp2, yp2, zp2}), new NeoObjectVector(new Object[] {units_0, units_0, units_0}), new NeoObjectVector(new Object[] {units_0, units_0, units_0}), labCoordinateSystem_0);
						cellSet_0.setPresentationName(CellSetName);
						sim.println(CellSetName + " created @ coordinates ["+xp1+","+yp1+","+zp1+"],["+xp2+","+yp2+","+zp2+"]");
						
						try {
							Thread.sleep(3000); // 延迟 2 秒
						} catch (InterruptedException e) {
								e.printStackTrace();
							}
						// get fieldFunction related to cellSet_0
						FieldFunctionManager fieldFunctionManager = sim.getFieldFunctionManager();
						FieldFunction matchedFieldFunction = fieldFunctionManager.getFunction(cellSet_0.getFunctionName());
						
						if (matchedFieldFunction != null){
						ThresholdPart thresholdPart_0 = (ThresholdPart) sim.getPartManager().getObject(ThresholdName);
						sim.println("ThresholdPart : " + thresholdPart_0 + " is found.");
						
						try {
							Thread.sleep(3000); // 延迟 2 秒
						} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
						if (thresholdPart_0 != null) {
							thresholdPart_0.setFieldFunction(matchedFieldFunction);
							sim.println("FieldFunction " + matchedFieldFunction.getPresentationName() + " set for ThresholdPart " + ThresholdName);
						} else {
							sim.println("**ATTENTION**: ThresholdPart " + ThresholdName + " not found.");
						}
						} else {
							sim.println("**ATTENTION**: No matching field function found for name: " + cellSet_0.getFunctionName());
						}
					}
					sim.println("------------------------------\n");
				}

			try {
				Thread.sleep(2000); // 延迟 2 秒
				} catch (InterruptedException e) {
					e.printStackTrace();
					}
			sim.println("\n Total < " + n + " > cellset updated");
			sim.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
			}catch (Exception ex){
				sim.println(ex.getMessage());
				}
	}
}