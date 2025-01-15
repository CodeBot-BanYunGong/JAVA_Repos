package coSim_Package;

import star.base.neo.*;
import star.base.neo.StringVector;
import star.base.query.*;
import star.base.report.*;
import star.common.*;
import star.energy.AmbientTemperatureProfile;
import star.energy.HeatTransferCoefficientProfile;
import star.mapping.*;
import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class create_CoSim_Events_V2 extends StarMacro {
    private final String baseTableDirectory = "C:/Users/xwen14/Desktop/StarPrc/PP/";
    private final String[] heatRejectionDirs = {"Heat_Rejections_5kW", "Heat_Rejections_10kW", "Heat_Rejections_15kW", "Heat_Rejections_20kW", "Heat_Rejections_25kW", "Heat_Rejections_30kW"};
    private final String[] speedDirs = {"IDLE", "20kph", "40kph", "60kph", "80kph", "100kph", "120kph", "160kph", "200kph"};
    private final String tableBaseName = "P800_EV_K5.csv";
    private ReportMonitor heatRejectionMonitor;
    private ReportMonitor vehicleSpeedMonitor;

    public void execute() {
        Simulation sim = getSimulation();
        createReportsAndMonitors(sim);
        sim.getTableManager().getGroupsManager().createGroup("External_Table");

        for (String heatDir : heatRejectionDirs) {
            double heatValue = Double.parseDouble(heatDir.substring(heatDir.lastIndexOf("_") + 1).replace("kW", ""));

            for (String speedDir : speedDirs) {
                double speedValue = Double.parseDouble((speedDir.equals("IDLE") ? "0" : speedDir).replace("kph", ""));

                String fullPath = baseTableDirectory + heatDir + "/" + speedDir + "/" + tableBaseName;
                File tableFile = new File(fullPath);

                if (!tableFile.exists()) {
                    sim.println("Warning: Table file not found: " + fullPath + ". Skipping.");
                    continue;
                }

                try {
                    processTable(sim, fullPath, heatValue, speedValue);
                } catch (Exception e) {
                    sim.println("Error processing table '" + tableFile.getName() + "': " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        applyBoundaryConditions(sim);
    }

    private void createReportsAndMonitors(Simulation sim) {
        //  Create Parameter, Report, Monitor for Ambient_Temperature
        ScalarGlobalParameter scalarGlobalParameter_0 = (ScalarGlobalParameter) sim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "Scalar");
        scalarGlobalParameter_0.setPresentationName("Ambient_Temp");
        ExpressionReport expressionReport_AmbTemp = (ExpressionReport) sim.getReportManager().create("star.base.report.ExpressionReport");
        expressionReport_AmbTemp.setPresentationName("AMBIENT_TEMP");
        expressionReport_AmbTemp.setDefinition("${Ambient_Temp}");
        expressionReport_AmbTemp.setDimensions(Dimensions.Builder().temperature(1).build());
        expressionReport_AmbTemp.setUnits((Units) sim.getUnitsManager().getObject("C"));
        ReportMonitor ambTempMonitor = expressionReport_AmbTemp.createMonitor();

        // Create Report and Monitor for "HX_Heat_Rejection" and "Vehicle_Speed"
        ExpressionReport expressionReport_HX_HeatRejection = sim.getReportManager().create("star.base.report.ExpressionReport");
        expressionReport_HX_HeatRejection.setPresentationName("HX_Heat_Rejection");
        expressionReport_HX_HeatRejection.setDimensions(Dimensions.Builder().power(1).build());
        this.heatRejectionMonitor = expressionReport_HX_HeatRejection.createMonitor();

        ExpressionReport expressionReport_Veh_Speed = sim.getReportManager().create("star.base.report.ExpressionReport");
        expressionReport_Veh_Speed.setPresentationName("Vehicle_Speed");
        expressionReport_Veh_Speed.setDimensions(Dimensions.Builder().velocity(1).build());
//        expressionReport_Veh_Speed.setUnits((Units) sim.getUnitsManager().getObject("kph"));
        this.vehicleSpeedMonitor = expressionReport_Veh_Speed.createMonitor();

    }

    private void processTable(Simulation sim, String fullPath, double heatValue, double speedValue) {
        //  Create Upate Events and Set Up
        Double heatRejCenter = heatValue;
        Double heatRejMin, heatRejMax;
        Double heatRejectionMargin = 2.5;
        if (heatRejCenter == 5) {
            heatRejMin = 0.0;
            heatRejMax = heatRejCenter + heatRejectionMargin;
        } else {
            heatRejMin = heatRejCenter - heatRejectionMargin;
            heatRejMax = heatRejCenter + heatRejectionMargin;
        }

        Double vehSpeedCenter = speedValue;
        Double vehSpeedMin, vehSpeedMax;
        Double speedMargin = 10.0;
        if (vehSpeedCenter == 0) {
            vehSpeedMin = (double) 0;
            vehSpeedMax = speedMargin;
        } else {
            vehSpeedMin = vehSpeedCenter - speedMargin;
            vehSpeedMax = vehSpeedCenter + speedMargin;
        }

        String eventName = String.format("HXHR_%.0fkW_VehSp_%.0fkph", heatRejCenter, vehSpeedCenter);
        LogicUpdateEvent logicUpdateEvent = sim.getUpdateEventManager().createUpdateEvent(LogicUpdateEvent.class);
        logicUpdateEvent.setPresentationName(eventName);

        RangeMonitorUpdateEvent maxHeatRejEvent = (RangeMonitorUpdateEvent) logicUpdateEvent.getUpdateEventManager().createUpdateEvent(RangeMonitorUpdateEvent.class);
        maxHeatRejEvent.setPresentationName("Max_Heat_Rejection");
        maxHeatRejEvent.setMonitor(heatRejectionMonitor);
        maxHeatRejEvent.getRangeOption().setSelected(UpdateEventRangeOption.Type.LESS_THAN_OR_EQUALS);
        maxHeatRejEvent.getRangeQuantity().setValueAndUnits(heatRejMax, (Units) sim.getUnitsManager().getObject("kW"));

        RangeMonitorUpdateEvent minHeatRejEvent = (RangeMonitorUpdateEvent) logicUpdateEvent.getUpdateEventManager().createUpdateEvent(RangeMonitorUpdateEvent.class);
        minHeatRejEvent.setPresentationName("Min_Heat_Rejection");
        minHeatRejEvent.setMonitor(heatRejectionMonitor);
        minHeatRejEvent.getRangeOption().setSelected(UpdateEventRangeOption.Type.GREATER_THAN_OR_EQUALS);
        minHeatRejEvent.getRangeQuantity().setValueAndUnits(heatRejMin, (Units) sim.getUnitsManager().getObject("kW"));

        RangeMonitorUpdateEvent maxSpeedEvent = (RangeMonitorUpdateEvent) logicUpdateEvent.getUpdateEventManager().createUpdateEvent(RangeMonitorUpdateEvent.class);
        maxSpeedEvent.setPresentationName("Max_Vehicle_Speed");
        maxSpeedEvent.setMonitor(vehicleSpeedMonitor);
        maxSpeedEvent.getRangeOption().setSelected(UpdateEventRangeOption.Type.LESS_THAN_OR_EQUALS);
        maxSpeedEvent.getRangeQuantity().setValueAndUnits(vehSpeedMax, (Units) sim.getUnitsManager().getObject("kph"));

        RangeMonitorUpdateEvent minSpeedEvent = (RangeMonitorUpdateEvent) logicUpdateEvent.getUpdateEventManager().createUpdateEvent(RangeMonitorUpdateEvent.class);
        minSpeedEvent.setPresentationName("Min_Vehicle_Speed");
        minSpeedEvent.setMonitor(vehicleSpeedMonitor);
        minSpeedEvent.getRangeOption().setSelected(UpdateEventRangeOption.Type.GREATER_THAN_OR_EQUALS);
        minSpeedEvent.getRangeQuantity().setValueAndUnits(vehSpeedMin, (Units) sim.getUnitsManager().getObject("kph"));


        //  Upload Table
        String fullTableName = String.format("External_H_T_HXHR_%.0fkW_VehSp_%.0fkph",heatValue, speedValue);
        sim.println("The FullTableName is: " + fullTableName);
        FileTable fileTable = (FileTable) sim.getTableManager().createFromFile(resolvePath(fullPath),(ClientServerObjectGroup) sim.getTableManager().getGroupsManager().getObject("External_Table"));
        fileTable.setPresentationName(fullTableName);
        String[] dataColumns = fileTable.getDataSets().toStringArray();
        sim.println("Table '" + fullTableName + "' uploaded successfully.");

        //  Create DataMapper and Update Setting Up
        TabularDataMapper tabularDataMapper = sim.get(DataMapperManager.class).createMapper(TabularDataMapper.class, fullTableName);
        tabularDataMapper.setPresentationName(fullTableName);
        sim.println("DAtaMapper '" + fullTableName + "' created successfully.");

        tabularDataMapper.setTable(fileTable);
        tabularDataMapper.setDataColumns(new StringVector(new String[] {dataColumns[0],dataColumns[1]}));
        SurfaceTargetSpecification surfaceTargetSpecification = (SurfaceTargetSpecification) tabularDataMapper.getTargetSpecificationManager().getObject("Surface 1");
        surfaceTargetSpecification.getTargetParts().setQuery(new Query(new CompoundPredicate(CompoundOperator.And, Arrays.<QueryPredicate>asList(new NamePredicate(NameOperator.Contains, "external"), new NamePredicate(NameOperator.DoesNotContain, "current"))), Query.STANDARD_MODIFIERS));
        StarUpdate starUpdate = tabularDataMapper.getStarUpdate();
        starUpdate.setEnabled(true);
        starUpdate.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.EVENT);
        starUpdate.getEventUpdateFrequency().setUpdateEvent((LogicUpdateEvent) sim.getUpdateEventManager().getUpdateEvent(eventName));
        sim.println("Successfully updated DataMapper: " + tabularDataMapper + " with Event: " + eventName);
    }

    private void applyBoundaryConditions(Simulation sim) {
        TabularDataMapper tabularDataMapper = (TabularDataMapper) sim.get(DataMapperManager.class).getObject("External_H_T_HXHR_5kW_VehSp_0kph");
        tabularDataMapper.mapData();
        NeoProperty fieldName = tabularDataMapper.getMappedFieldNames();
        sim.println("The Field Name is :" + fieldName);
        String[] dataSources = tabularDataMapper.getDataColumns().toStringArray();
        sim.println("The data source name is :" + Arrays.toString(dataSources));
        sim.println("DataColumn[0] is " + dataSources[0]);
        sim.println("DataColumn[1] is " + dataSources[1]);
        String fieldNameString = fieldName.toString();
        Pattern pattern = Pattern.compile("'([^']+)': '([^']+)'");
        Matcher matcher = pattern.matcher(fieldNameString);
        String newTair = null;
        String newHTC = null;
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            sim.println(key + ":" + value);
            if (key.equals(dataSources[1])) {
                newTair = value;
            } else if (key.equals(dataSources[0])) {
                newHTC = value;
            }
        }

        try {
            Boundary boundaryExternal = sim.getRegionManager().getObject("SOLID_SE").getBoundaryManager().getBoundary("External");
            boundaryExternal.getValues().get(AmbientTemperatureProfile.class).getMethod(FunctionScalarProfileMethod.class).setFieldFunction((PrimitiveFieldFunction) sim.getFieldFunctionManager().getFunction(newTair));
            boundaryExternal.getValues().get(HeatTransferCoefficientProfile.class).getMethod(FunctionScalarProfileMethod.class).setFieldFunction((PrimitiveFieldFunction) sim.getFieldFunctionManager().getFunction(newHTC));
        } catch (ClassCastException ex) {
            sim.println("Error applying boundary conditions: " + ex.getMessage());
        }


    }

}
