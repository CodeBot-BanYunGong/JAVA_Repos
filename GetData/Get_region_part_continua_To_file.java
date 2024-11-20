package GetDataFromCCM;

import star.common.*;
import star.energy.*;
import star.flow.ConstantDensityProperty;
import star.material.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;

public class Get_region_part_continua_To_file extends StarMacro {

    public void execute() {
        execute0();
    }

    private void execute0() {
        Simulation sim = getActiveSimulation();
        String simFileName = sim.getPresentationName();
        Collection<Region> region_col = sim.getRegionManager().getObjects();

        String regionName;
        String continuumName;
        String header_str = "Geometry_part_list_";

        try {
            File f_0 = new File(sim.getSessionDir() + File.separator + header_str + simFileName + ".csv");
            FileWriter fw = new FileWriter(f_0, true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("Region,PartName,Continuum,Material,Density,SpecificHeat,ThermalConductivity");
            bw.newLine();

            for (Region REG : region_col) {
                // Get Region Name.
                regionName = REG.getPresentationName();
                // Get Continuum name.
                continuumName = sim.getRegionManager().getRegion(regionName).getPhysicsContinuum().getPresentationName();
                // Confirm if there's MaterialPartGroupManager in Region.
                MaterialPartGroupManager matManager = null;
                try {
                    matManager = REG.getValues().get(MaterialPartGroupManager.class);
                } catch (Exception ignored) {

                }
                // Make judgment for MaterialPartGroupManager. If not null, find material and part.
                if (matManager != null) {
                    handleMaterialPartGroupManager(matManager, regionName, continuumName, bw, sim);
                } else {
                    handleNoMaterialPartGroupManager(REG, regionName, continuumName, bw, sim);
                }
            }
            bw.close();
            sim.println("Successfully wrote " + sim.getSessionDir() + File.separator + header_str + simFileName + ".csv\n");

        } catch (Exception e) {
            sim.println("Error with writing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMaterialPartGroupManager(MaterialPartGroupManager matManager, String regionName, String continuumName, BufferedWriter bw, Simulation sim) {
        Collection<MaterialPartGroup> mat_col = matManager.getObjects();
        for (MaterialPartGroup MAT : mat_col) {
            String materialName = MAT.getPresentationName();
            String thermalConductivity = "N/A";
            // Get Material Properties from Continuum.
            MultiPartSolidModel multiPartSolidModel = sim.getContinuumManager().getContinuum(continuumName).getModelManager().getModel(MultiPartSolidModel.class);
            SolidComponent solidComponent = (SolidComponent) multiPartSolidModel.getMixture().getComponents().getComponent(materialName);
            double densityValue = ((ConstantMaterialPropertyMethod) solidComponent.getMaterialProperties().getMaterialProperty(ConstantDensityProperty.class).getMethod()).getQuantity().getInternalValue();
            String density = densityValue + "kg/m^3";
            double specificHeatValue = ((ConstantSpecificHeat) solidComponent.getMaterialProperties().getMaterialProperty(SpecificHeatProperty.class).getMethod()).getQuantity().getInternalValue();
            String specificHeat = specificHeatValue + "J/kg-K";
            MaterialPropertyMethod thermalConductivityMethod_0 = solidComponent.getMaterialProperties().getMaterialProperty(ThermalConductivityProperty.class).getMethod();
            if (thermalConductivityMethod_0 instanceof ConstantMaterialPropertyMethod) {
                thermalConductivity = ((ConstantMaterialPropertyMethod) thermalConductivityMethod_0).getQuantity().getInternalValue() + "W/m-K";
            } else if (thermalConductivityMethod_0 instanceof OrthotropicThermalConductivityMethod) {
                double xConductivityValue = ((ConstantMaterialPropertyMethod) (((OrthotropicThermalConductivityMethod) thermalConductivityMethod_0).getScalarProperty(0)).getMethod()).getQuantity().getInternalValue();
                double yConductivityValue = ((ConstantMaterialPropertyMethod) (((OrthotropicThermalConductivityMethod) thermalConductivityMethod_0).getScalarProperty(1)).getMethod()).getQuantity().getInternalValue();
                double zConductivityValue = ((ConstantMaterialPropertyMethod) (((OrthotropicThermalConductivityMethod) thermalConductivityMethod_0).getScalarProperty(2)).getMethod()).getQuantity().getInternalValue();
                thermalConductivity = xConductivityValue + "W/m-K; " + yConductivityValue + "W/m-K; " + zConductivityValue + "W/m-K;";
            }

            // Retrieve GeometryParts
            Collection<GeometryPart> part_col = MAT.getObjects();
            for (GeometryPart PART : part_col) {
                String partName = PART.getPresentationName();
                String built_str = regionName + "," + partName + "," + continuumName + "," + materialName + "," + density + "," + specificHeat + "," + thermalConductivity;
                try {
                    bw.write(built_str);
                    bw.newLine();
                } catch (Exception e) {
                    sim.println("Error writing part information: " + e.getMessage());
                }
            }
        }
    }

    private void handleNoMaterialPartGroupManager(Region REG, String regionName, String continuumName, BufferedWriter bw, Simulation sim) {
        String materialName = "N/A";
        String density = "N/A";
        String specificHeat = "N/A";
        String thermalConductivity = "N/A";

        PhysicsContinuum physicsContinuum = (PhysicsContinuum) sim.getContinuumManager().getContinuum(continuumName);
        Collection<Model> models = physicsContinuum.getModelManager().getObjects();
        for (Model model : models) {
            String modelName = model.getPresentationName();
            switch (modelName) {
                case "Gas" -> {
                    SingleComponentGasModel singleComponentGasModel = physicsContinuum.getModelManager().getModel(SingleComponentGasModel.class);
                    Gas gas = (Gas) singleComponentGasModel.getMaterial();
                    materialName = String.valueOf(gas);
                    Collection<MaterialProperty> matProperties = gas.getMaterialProperties().getMaterialProperties();
                    // The Material Properties Collection includes: [Thermal Conductivity, Molecular Weight, Specific Heat, Dynamic Viscosity]
                    for (MaterialProperty matProperty : matProperties) {
                        if (matProperty instanceof ThermalConductivityProperty) {
                            MaterialPropertyMethod method_0 = matProperty.getMethod();
                            if (method_0 instanceof ConstantMaterialPropertyMethod) {
                                thermalConductivity = ((ConstantMaterialPropertyMethod) method_0).getQuantity().getInternalValue() + "W/m-K";
                            } else if (method_0 instanceof TemperatureTableMethod) {
                                thermalConductivity = ((TemperatureTableMethod) method_0).getInterpolationTable().getTable().getPresentationName();
                            }
                        } else if (matProperty instanceof SpecificHeatProperty) {
                            MaterialPropertyMethod method_1 = matProperty.getMethod();
                            if (method_1 instanceof ConstantMaterialPropertyMethod) {
                                specificHeat = ((ConstantMaterialPropertyMethod) method_1).getQuantity().getInternalValue() + "J/kg-K";
                            } else if (method_1 instanceof SpecificHeatTemperatureTable) {
                                specificHeat = ((SpecificHeatTemperatureTable) method_1).getInterpolationTable().getTable().getPresentationName();
                            }
                        }
                    }
                }
                case "Liquid" -> {
                    SingleComponentLiquidModel singleComponentLiquidModel = physicsContinuum.getModelManager().getModel(SingleComponentLiquidModel.class);
                    Liquid liquid = (Liquid) singleComponentLiquidModel.getMaterial();
                    materialName = String.valueOf(liquid);
                    Collection<MaterialProperty> matProperties = liquid.getMaterialProperties().getObjects();
                    // The Material Properties are : [Polynomial Density, Specific Heat, Thermal Conductivity, Turbulent Prandtl Number, Dynamic Viscosity, Standard State Temperature]
                    for (MaterialProperty matProperty : matProperties) {
                        String matName = matProperty.getPresentationName();
                        switch (matName) {
                            case "Density" ->
                                    density = ((ConstantMaterialPropertyMethod) matProperty.getMethod()).getQuantity().getInternalValue() + "kg/m^3";
                            case "Polynomial Density" ->
                                    density = "\"" + ((TemperaturePolynomial) liquid.getMaterialProperties().getMaterialProperty(PolynomialDensityProperty.class).getMethod()).getPolynomial().getInput() + "\"";
                            case "Specific Heat" -> {
                                MaterialPropertyMethod matMethod_1 = matProperty.getMethod();
                                if (matMethod_1 instanceof ConstantMaterialPropertyMethod) {
                                    specificHeat = ((ConstantMaterialPropertyMethod) matMethod_1).getQuantity().getInternalValue() + "J/kg-K";
                                } else if (matMethod_1 instanceof PolynomialSpecificHeat) {
                                    specificHeat = "\"" + ((PolynomialSpecificHeat) matMethod_1).getPolynomial().getInput() + "\"";
                                }
                            }
                            case "Thermal Conductivity" -> {
                                MaterialPropertyMethod matMethod_1 = matProperty.getMethod();
                                if (matMethod_1 instanceof ConstantMaterialPropertyMethod) {
                                    thermalConductivity = ((ConstantMaterialPropertyMethod) matMethod_1).getQuantity().getInternalValue() + "W/m-K";
                                } else if (matMethod_1 instanceof TemperaturePolynomial) {
                                    thermalConductivity = "\"" + ((TemperaturePolynomial) matMethod_1).getPolynomialInput() + "\"";
                                }
                            }
                        }
                    }
                }
                case "Solid" -> {
                    materialName = continuumName;
                    SolidModel solidModel_0 = physicsContinuum.getModelManager().getModel(SolidModel.class);
                    Solid solid = (Solid) solidModel_0.getMaterial();
                    density = ((ConstantMaterialPropertyMethod) solid.getMaterialProperties().getMaterialProperty(ConstantDensityProperty.class).getMethod()).getQuantity().getInternalValue() + "kg/m^3";
                    specificHeat = ((ConstantSpecificHeat) solid.getMaterialProperties().getMaterialProperty(SpecificHeatProperty.class).getMethod()).getQuantity().getInternalValue() + "J/kg-K";
                    MaterialPropertyMethod thermalConductivityMethod_0 = solid.getMaterialProperties().getMaterialProperty(ThermalConductivityProperty.class).getMethod();
                    if (thermalConductivityMethod_0 instanceof ConstantMaterialPropertyMethod) {
                        thermalConductivity = ((ConstantMaterialPropertyMethod) thermalConductivityMethod_0).getQuantity().getInternalValue() + "W/m-K";
                    } else if (thermalConductivityMethod_0 instanceof OrthotropicThermalConductivityMethod) {
                        double xConductivityValue = ((ConstantMaterialPropertyMethod) (((OrthotropicThermalConductivityMethod) thermalConductivityMethod_0).getScalarProperty(0)).getMethod()).getQuantity().getInternalValue();
                        double yConductivityValue = ((ConstantMaterialPropertyMethod) (((OrthotropicThermalConductivityMethod) thermalConductivityMethod_0).getScalarProperty(1)).getMethod()).getQuantity().getInternalValue();
                        double zConductivityValue = ((ConstantMaterialPropertyMethod) (((OrthotropicThermalConductivityMethod) thermalConductivityMethod_0).getScalarProperty(2)).getMethod()).getQuantity().getInternalValue();
                        thermalConductivity = xConductivityValue + "W/m-K; " + yConductivityValue + "W/m-K; " + zConductivityValue + "W/m-K;";
                    }
                }
            }
        }
        // Retrieve GeometryParts
        Collection<GeometryPart> part_col = REG.getPartGroup().getObjects();
        for (GeometryPart PART : part_col) {
            String partName = PART.getPresentationName();
            String built_str = regionName + "," + partName + "," + continuumName + "," + materialName + "," + density + "," + specificHeat + "," + thermalConductivity;
            try {
                bw.write(built_str);
                bw.newLine();
            } catch (Exception e) {
                sim.println("Error writing part information: " + e.getMessage());
            }
        }
    }
}
