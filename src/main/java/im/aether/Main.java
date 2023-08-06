package im.aether;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.jcm.discordgamesdk.Core;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.Sensors;
import oshi.util.GlobalConfig;
import oshi.util.tuples.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author imAETHER
 * 20-07-2022 @ 10:32AM
 */
public class Main {
    private static final File CONFIG = new File("config.json");
    public static final SystemInfo SYSTEM_INFO = new SystemInfo();

    public static void main(String[] args) throws Exception {
        FlatMacDarkLaf.setup();
        UIManager.put("TabbedPane.showTabSeparators", true);
        final GUI UI = new GUI();

        Logger.getGlobal().info("Checking for config file...");

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Config config;
        if (!CONFIG.exists()) {
            if (CONFIG.createNewFile()) Logger.getGlobal().info("Created config file.");
            else {
                Logger.getGlobal().severe("Failed to create config file. please create it and re-launch.");
                return;
            }
            final String serialized = gson.toJson(config = new Config());
            Files.write(CONFIG.toPath(), serialized.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
            Logger.getGlobal().info("Default config created and loaded.");
        } else {
            final String cfgData = Files.readAllLines(CONFIG.toPath(), StandardCharsets.UTF_8).stream().reduce("", (a, b) -> a + b);
            config = gson.fromJson(cfgData, Config.class);
            Logger.getGlobal().info("Existing config loaded from file.");
        }

        if (config == null) {
            Logger.getGlobal().severe("Failed to parse config file. Please check it and re-launch.");
            Logger.getGlobal().severe("An example config can be found in the README.");
            return;
        }

        UI.getAppIdField().setText(config.getAppId());
        UI.getBigImageField().setText(config.getLargeImage());
        UI.getSmallImageField().setText(config.getSmallImage());
        UI.getTopCustomText().setText(config.getTopText());
        UI.getBottomCustomText().setText(config.getBottomText());

        Logger.getGlobal().info("Downloading RPC lib...");

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
            UI.getOhmStatusLabel().setForeground(new Color(241, 38, 40));
            UI.getOhmStatusLabel().setText("OHM Status: NOT RUNNING");
            Logger.getGlobal().warning("OpenHardwareMonitor isn't running, please run it & restart me");
            Logger.getGlobal().warning("or you may get inaccurate readings. You can also try running me as admin.");
        } else {
            UI.getOhmStatusLabel().setForeground(new Color(35, 165, 90));
            UI.getOhmStatusLabel().setText("OHM Status: RUNNING");
        }

        final List<GraphicsCard> gpus = SYSTEM_INFO.getHardware().getGraphicsCards();

        // I doubt people wont have gpus but yk
        if (!gpus.isEmpty()) {
            for (GraphicsCard graphicsCard : gpus) {
                UI.getActiveGpus().addItem(graphicsCard.getName());
            }

            if (gpus.size() > 1) {
                UI.getActiveGpus().addItemListener(g -> {
                    config.setDisplayGpu((String) g.getItem());
                });
            } else {
                config.setDisplayGpu(gpus.get(0).getName());
            }
        }

        // This is for giving cpu usage readings closer to Task manager's
        GlobalConfig.set(GlobalConfig.OSHI_OS_WINDOWS_CPU_UTILITY, true);

        final Sensors sensors = SYSTEM_INFO.getHardware().getSensors();
        final CentralProcessor processor = SYSTEM_INFO.getHardware().getProcessor();
        final long[] cpuPrevTicks = processor.getSystemCpuLoadTicks();

        final Timer s = new Timer(1000, (e) -> {
            if (UI.getTopTextOpt().getSelectedItem() == null || UI.getBottomTextOpt().getSelectedItem() == null) return;

            int cpuTemp = (int) Math.round(sensors.getCpuTemperature());
            int cpuUsage = (int) Math.round(processor.getSystemCpuLoadBetweenTicks(cpuPrevTicks) * 100);

            final Pair<String, String> gpuInfoNvidia = getGPUInfoNvidia();

            final String cpuStats = "CPU Usage: " + cpuUsage + " % | Temp: " + cpuTemp + "°C";
            final String gpuStats = "GPU Usage: " + gpuInfoNvidia.getA() + " | Temp: " + gpuInfoNvidia.getB() + "°C";

            String topInfo = null;
            switch ((RTextOptions) UI.getTopTextOpt().getSelectedItem()) {
                case Custom:
                    topInfo = UI.getTopCustomText().getText();
                    break;
                case CPUStats:
                    topInfo = cpuStats;
                    break;
                case GPUStats:
                    topInfo = gpuStats;
                    break;
            }

            String bottomInfo = null;
            switch ((RTextOptions) UI.getBottomTextOpt().getSelectedItem()) {
                case Custom:
                    bottomInfo = UI.getBottomCustomText().getText();
                    break;
                case CPUStats:
                    bottomInfo = cpuStats;
                    break;
                case GPUStats:
                    bottomInfo = gpuStats;
                    break;
            }

            DiscordRPC.updateImages(UI.getBigImageField().getText(), UI.getSmallImageField().getText());
            DiscordRPC.update(
                    topInfo,
                    bottomInfo,
                    config.getDisplayGpu(),
                    processor.getProcessorIdentifier().getName() //cpu name
            );

            // Update the previous ticks
            System.arraycopy(processor.getSystemCpuLoadTicks(), 0, cpuPrevTicks, 0, cpuPrevTicks.length);
        });

        UI.getToggleButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (UI.getToggleButton().getText().contains("Start")) {
                    DiscordRPC.initActivity(config.getAppId(), config.isShowPCActiveTime());
                    DiscordRPC.updateImages(config.getLargeImage(), config.getSmallImage());
                    s.start();

                    UI.getToggleButton().setText("Stop RPC");
                } else {
                    s.stop();
                    DiscordRPC.dispose();
                    UI.getToggleButton().setText("Start RPC");
                }
            }
        });


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.getGlobal().info("Saving config...");

            // Theres probably a better way to do all of this, if so please tell me!
            config.setAppId(UI.getAppIdField().getText());
            config.setTopText(UI.getTopCustomText().getText());
            config.setBottomText(UI.getBottomCustomText().getText());
            config.setLargeImage(UI.getBigImageField().getText());
            config.setSmallImage(UI.getSmallImageField().getText());

            final String serialized = gson.toJson(config);
            try {
                Files.write(CONFIG.toPath(), serialized.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.getGlobal().severe("Failed to save the config on exit!");
            }

            s.stop();
            DiscordRPC.dispose();
        }));

        UI.getToggleButton().setEnabled(true);
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
