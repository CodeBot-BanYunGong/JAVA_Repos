/**
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
public class Cellset_Create extends StarMacro {

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
				CellSet cellSet_0 = fvRepresentation_0.getCellSetManager().createEmptyCellSet();
				Units units_0 = ((Units) sim.getUnitsManager().getObject("mm"));
				cellSet_0.addRange(new DoubleVector(new double[] {xp1, yp1, zp1}), new DoubleVector(new double[] {xp2, yp2, zp2}), new NeoObjectVector(new Object[] {units_0, units_0, units_0}), new NeoObjectVector(new Object[] {units_0, units_0, units_0}), labCoordinateSystem_0);
                cellSet_0.setPresentationName(CellSetName);
				
				sim.println("Cell Set " + CellSetName + " created @ coordinates ["+xp1+","+yp1+","+zp1+"],["+xp2+","+xp2+","+zp2+"]");
				
                // Create threshold for cellset based on Geometry Parts
                if (PartType.equals("Part")) {
					MeshPart meshPart_0 = null;
                    try {
						meshPart_0 = ((MeshPart) sim.get(SimulationPartManager.class).getPart(PartName));
						sim.println("**MeshPart** found: " + meshPart_0.getPresentationName());
					} catch (Exception ex) {
						sim.println("There no part " + PartName + " found.");
					}
					
					Units units_1 = sim.getUnitsManager().getPreferredUnits(Dimensions.Builder().build());
					
					try {
						// get fieldFunction related to cellSet_0
						FieldFunctionManager fieldFunctionManager = sim.getFieldFunctionManager();
						FieldFunction matchedFieldFunction = fieldFunctionManager.getFunction(cellSet_0.getFunctionName());
						
						// Check if get fieldFunction
						if (matchedFieldFunction != null) {
							sim.println("FieldFunction found: " + matchedFieldFunction.getPresentationName() + " (" + matchedFieldFunction.getType() + ") ");
							
							// Create ThresholdPart with primitiveFieldFunction
							if (meshPart_0 != null) {
							ThresholdPart thresholdPart_0 = sim.getPartManager().createThresholdPart(new NeoObjectVector(new Object[] {meshPart_0}), new DoubleVector(new double[] {1.0, 1.0}), units_1, matchedFieldFunction, 0, null);
							thresholdPart_0.setPresentationName(ThresholdName);
							sim.println("ThresholdPart : " + thresholdPart_0 + " is created @ MeshPart");
							} else {
								sim.println("**ATTENTION**: MeshPart is null, cannot create ThresholdPart.");
							}
						} else {
							sim.println("**ATTENTION**: FieldFunction is not found.");
						}
					} catch (Exception e) {
						sim.println("Error retrieving PrimitiveFieldFunction: " + e.getMessage());
						e.printStackTrace();
					}
					sim.println("------------------------------\n");
				} else if (PartType.equals("Region")) {
					Region region_0 = null;
					try {
						region_0 = sim.getRegionManager().getRegion(PartName);
						sim.println("**Region** found: " + region_0.getPresentationName());
					} catch (Exception ex) {
						sim.println("There no part " + PartName + " found.");
					}
					Units units_1 = sim.getUnitsManager().getPreferredUnits(Dimensions.Builder().build());
					try {
						// get fieldFunction related to cellSet_0
						FieldFunctionManager fieldFunctionManager = sim.getFieldFunctionManager();
						FieldFunction matchedFieldFunction = fieldFunctionManager.getFunction(cellSet_0.getFunctionName());
						
						// Check if get fieldFunction
						if (matchedFieldFunction != null) {
							sim.println("FieldFunction found: " + matchedFieldFunction.getPresentationName() + " (" + matchedFieldFunction.getType() + ") ");
							
							// Create ThresholdPart with primitiveFieldFunction
							if (region_0 != null) {
							ThresholdPart thresholdPart_0 = sim.getPartManager().createThresholdPart(new NeoObjectVector(new Object[] {region_0}), new DoubleVector(new double[] {1.0, 1.0}), units_1, matchedFieldFunction, 0, null);
							thresholdPart_0.setPresentationName(ThresholdName);
							sim.println("ThresholdPart : " + thresholdPart_0 + " is created @ Region");
								} else {
									sim.println("**ATTENTION**: Region is null, cannot create ThresholdPart.");
									}
							} else {
								sim.println("**ATTENTION**: FieldFunction is not found.");
								}
							} catch (Exception e) {
								sim.println("Error retrieving PrimitiveFieldFunction: " + e.getMessage());
								e.printStackTrace();
								}
					sim.println("------------------------------\n");
				} else {
					sim.println("No condition met for PartType: " + PartType);
				}
			}
			try {
				Thread.sleep(2000); // 延迟 2 秒
				} catch (InterruptedException e) {
					e.printStackTrace();
					}
			sim.println("Total quatity < " + n + " > CellSet and ThresholdPart are Created.");
			sim.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}
