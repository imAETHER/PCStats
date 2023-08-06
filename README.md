# <p align="center">Discord RPC Stats<br>![image](https://github.com/imAETHER/PCStats/assets/36291026/2563e075-1381-493f-88c0-f205f82bb564)</p>

# ✨ Usage
Either build it yourself or go to [releases](https://github.com/imAETHER/PCStats/releases) and download it.

This project uses OSHI to get processor temperatures, OSHI uses [OpenHardwareMonitor](https://openhardwaremonitor.org/) to get these, download and run it in the background while you use the RPC to get accurate readings.<br>

- Make a folder and place the jar in there
- Open the jar (with java 8 or above)
- It will automatically create `config.json` in the current directory and will open the GUI!
- You can now edit the RPC as you wish
  - Once the RPC is running you can click the close button to minimize it to tray, click the tray icon to bring PCStats back up.
  - To close it click "Stop RPC" and then you can close it normally.
- Configs are automatically saved on exit

### You can customize both text fields and images!<br>
![image](https://github.com/imAETHER/PCStats/assets/36291026/28eed7b1-5a0e-41a0-8aa7-d990ea16acc8)

# 💻 Supported platforms
All platforms since its java & its using OSHI (haven't tested tho), for now GPU usage and temperature **only works with NVIDIA gpus that have nvidia-smi**.
