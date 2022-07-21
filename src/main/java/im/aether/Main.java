package im.aether;

import com.google.gson.Gson;
import de.jcm.discordgamesdk.Core;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Sensors;
import oshi.util.GlobalConfig;
import oshi.util.tuples.Pair;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * @author imAETHER
 * 20-07-2022 @ 10:32AM
 */
public class Main {
    private static final File CONFIG = new File("config.json");
    public static final SystemInfo SYSTEM_INFO = new SystemInfo();

    public static void main(String[] args) throws Exception {
        Logger.getGlobal().info("Checking for config file...");

        if (!CONFIG.exists()) {
            if (CONFIG.createNewFile()) Logger.getGlobal().info("Created config file.");
            else {
                Logger.getGlobal().severe("Failed to create config file. please create it and re-launch.");
                return;
            }
        }
        final String cfgData = Files.readAllLines(CONFIG.toPath(), StandardCharsets.UTF_8).stream().reduce("", (a, b) -> a + b);

        final Gson gson = new Gson();
        final Config config = gson.fromJson(cfgData, Config.class);

        if (config == null) {
            Logger.getGlobal().severe("Failed to parse config file. Please check it and re-launch.");
            Logger.getGlobal().severe("An example config can be found in the README.");
            return;
        }

        Logger.getGlobal().info("Config loaded. Downloading libs...");

        // temp(libs) and tempDir(lib directory), this is so Core doesn't create more temp dirs and just uses the given one
        final Pair<File, File> discordLib = DiscordRPC.downloadDiscordLibrary();
        if (discordLib == null) {
            Logger.getGlobal().severe("Failed to download discord library.");
        } else {
            Core.init(discordLib.getA(), discordLib.getB());
            Logger.getGlobal().info("Discord library loaded.");
        }

        boolean runningOhm = SYSTEM_INFO.getOperatingSystem().getProcesses().stream().anyMatch(
                process -> process.getName().equalsIgnoreCase("openhardwaremonitor")
        );
        if (!runningOhm) {
            Logger.getGlobal().warning("OpenHardwareMonitor isn't running, please run it & restart me");
            Logger.getGlobal().warning("or you may get inaccurate readings. You can also try running me as admin.");
        }

        DiscordRPC.initActivity(config.appId, config.showPCActiveTime);
        DiscordRPC.updateImages(config.largeImage, config.smallImage);

        // This is for giving cpu usage readings closer to Task manager's
        GlobalConfig.set(GlobalConfig.OSHI_OS_WINDOWS_CPU_UTILITY, true);

        final Sensors sensors = SYSTEM_INFO.getHardware().getSensors();
        final CentralProcessor processor = SYSTEM_INFO.getHardware().getProcessor();
        final long[] cpuPrevTicks = processor.getSystemCpuLoadTicks();

        final Timer s = new Timer(1000, (e) -> {
            int cpuTemp = (int) Math.round(sensors.getCpuTemperature());
            int cpuUsage = (int) Math.round(processor.getSystemCpuLoadBetweenTicks(cpuPrevTicks) * 100);

            final Pair<String, String> gpuInfoNvidia = getGPUInfoNvidia();

            DiscordRPC.update(
                    "CPU Usage: " + cpuUsage + " % | Temp: " + cpuTemp + "°C",
                    "GPU Usage: " + gpuInfoNvidia.getA() + " | Temp: " + gpuInfoNvidia.getB() + "°C",
                    SYSTEM_INFO.getHardware().getGraphicsCards().get(0).getName(), //gpu name
                    processor.getProcessorIdentifier().getName() //cpu name
            );

            // Update the previous ticks
            System.arraycopy(processor.getSystemCpuLoadTicks(), 0, cpuPrevTicks, 0, cpuPrevTicks.length);
        });
        s.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            s.stop();
            DiscordRPC.dispose();
        }));

        Logger.getGlobal().info("You can press CTRL+C to exit.");
    }

    /**
     * Uses NVIDIA's nvidia-smi to get the GPU usage and temperature.
     * TODO: Support for other GPU manufacturers.
     *
     * @return A pair of the GPU usage and temperature.
     */
    private static Pair<String, String> getGPUInfoNvidia() {
        try {
            final Process gpuTemp = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "nvidia-smi --query-gpu=temperature.gpu --format=csv,noheader"});
            gpuTemp.waitFor();
            //read the output from the command
            final BufferedReader reader = new BufferedReader(new InputStreamReader(gpuTemp.getInputStream()));
            final String gpuTempString = reader.readLine();
            reader.close();

            final Process gpuUsage = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader"});
            gpuUsage.waitFor();
            //read the output from the command
            final BufferedReader reader2 = new BufferedReader(new InputStreamReader(gpuUsage.getInputStream()));
            final String gpuUsageString = reader2.readLine();
            reader2.close();

            return new Pair<>(gpuUsageString, gpuTempString);
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>("N/A", "N/A");
        }
    }
}
