# JAVA_Repos
JAVA_Class introduction _ https://www.javatpoint.com/java-constructor

## CellSet Create & Update
### CellSet_Create.java
> use this code to create **CellSet** and **ThresholdPart**
> ThresholdPart whould choose which part is used. The part type can be *geometry part* or *region*. So need to define whhich type is used for this threshold part.
> **"cellset.csv"** should be used for many parameters and names.
### CellSet_Update.java
> use this code to update **CellSet** even old cellsets are kept or removed.
> ThresholdPart does not need to update. The field function used by ThresholdPart will be kept and get in touch with new cellsets. 

## Plot & Scene Create
### Plot_Scene_Create.java
> Define one parameter **AutoSaveFreq** to control each image save frequency.
> Auto create 10 plots and 10 scenes. The base properties have been defined.
>  * UpdateMode: DeltaTIme
>  * SaveToFile: True
>  * UpdateFrequency: AutoSaveFreq
>  * ColorBar: blue-red
> 
> User should modify name of plot and scene when use them. That would make them clear.

## GetData
### Get_region_part_continua_To_file.java
> Defind this script to get data in CCM
> Get material properties data for each continua
