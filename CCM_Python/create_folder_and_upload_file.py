import os
import shutil
import re

# 定义文件夹名称
heat_rejection_dirs = [
    "Heat_Rejections_5kW",
    "Heat_Rejections_10kW",
    "Heat_Rejections_15kW",
    "Heat_Rejections_20kW",
    "Heat_Rejections_25kW",
    "Heat_Rejections_30kW"
]

speed_dirs = [
    "IDLE", "20kph", "40kph", "60kph", "80kph",
    "100kph", "120kph", "160kph", "200kph"
]

# 根目录
base_dir = r"C:\Users\xwen14\Desktop\PP"

# 新文件名
new_filename = "Project_Name.csv" # 修改为你想要的扩展名

# 创建文件夹
for heat_dir in heat_rejection_dirs:
    heat_path = os.path.join(base_dir, heat_dir)
    os.makedirs(heat_path, exist_ok=True)
    for speed_dir in speed_dirs:
        speed_path = os.path.join(heat_path, speed_dir)
        os.makedirs(speed_path, exist_ok=True)

# 假设所有文件都在当前目录下
files = [f for f in os.listdir(base_dir) if f.endswith('.csv')]
print(files)

# 移动文件
for file in files:
    # 将文件名转换为小写
    file_lower = file.lower()
    print(file_lower)

    # 使用正则表达式提取文件名中的千瓦和速度值
    kw_match = re.search(r'(\d+)kw', file_lower)
    print(kw_match)
    speed_match = re.search(r'(\d+)kph', file_lower) or re.search(r'idle', file_lower)
    print(speed_match)

    if kw_match and speed_match:
        kw_value = kw_match.group(0)  # 提取千瓦数值
        print(kw_value)
        speed_value = speed_match.group(0)  # 提取速度数值
        print(speed_value)

        # 查找对应的文件夹
        for heat_dir in heat_rejection_dirs:
            # 将文件夹名称转换为小写进行比较
            if kw_value == heat_dir.lower().split('_')[2]:
                print(heat_dir.lower())
                print(f"{heat_dir.lower().split('_')[2]}\n")
                for speed_dir in speed_dirs:
                    if speed_value in speed_dir.lower():
                        # 构建目标路径
                        target_dir = os.path.join(base_dir, heat_dir, speed_dir)
                        copied_file_path = shutil.copy2(os.path.join(base_dir, file), os.path.join(target_dir, file))
                        new_file_path = os.path.join(target_dir, new_filename)
                        os.rename(copied_file_path, new_file_path)
                        break

print("Files have been organized into respective folders.")
