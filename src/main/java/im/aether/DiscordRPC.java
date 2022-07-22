package im.aether;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import oshi.util.tuples.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class DiscordRPC {

	private static Core core;
	private static Activity activity;

	private DiscordRPC() {
		throw new AssertionError();
	}

	/**
	 * not my code
	 *
	 * @return lib in the default temp dir
	 */
	public static Pair<File, File> downloadDiscordLibrary() {
		String name = "discord_game_sdk", suffix, osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
		String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

		if (osName.contains("windows")) suffix = ".dll";
		else if (osName.contains("linux")) suffix = ".so";
		else if (osName.contains("mac os")) suffix = ".dylib";
		else throw new RuntimeException("cannot determine OS type: " + osName);


		/*
		Some systems report "amd64" (e.g. Windows and Linux), some "x86_64" (e.g. Mac OS).
		At this point we need the "x86_64" version, as this one is used in the ZIP.
		 */
		if (arch.equals("amd64")) arch = "x86_64";

		// Path of Discord's library inside the ZIP
		String zipPath = "lib/" + arch + "/" + name + suffix;

		// Open the URL as a ZipInputStream
		try {
			final String url = "https://dl-game-sdk.discordapp.net/2.5.6/discord_game_sdk.zip";
			final URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("User-Agent",
			                              "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) " +
					                              "Gecko/20100316 Firefox/3.6.2"
			);

			final ZipInputStream zin = new ZipInputStream(connection.getInputStream());

			// Search for the right file inside the ZIP
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				if (entry.getName().equals(zipPath)) {
					// Create a new temporary directory
					// We need to do this, because we may not change the filename on Windows
					File tempDir = new File(System.getProperty("java.io.tmpdir"),
					                        "ae-rpc-" + name + System.nanoTime()
					);
					if (! tempDir.mkdir()) throw new IOException("Cannot create temporary directory");
					tempDir.deleteOnExit();

					// Create a temporary file inside our directory (with a "normal" name)
					File temp = new File(tempDir, name + suffix);
					temp.deleteOnExit();

					// Copy the file in the ZIP to our temporary file
					Files.copy(zin, temp.toPath());

					// We are done, so close the input stream
					zin.close();

					// Return our temporary file
					return new Pair<>(temp, tempDir);
				}
				// next entry
				zin.closeEntry();
			}
			zin.close();
		} catch (Exception e) {
			return null;
		}
		// We couldn't find the library inside the ZIP
		return null;
	}

	public static void initActivity(final String appId, final boolean activeTime) {
		if (core != null) {
			dispose();
		}
		// Set parameters for the Core
		try (CreateParams params = new CreateParams()) {
			params.setClientID(Long.parseLong(appId));
			params.setFlags(CreateParams.getDefaultFlags());

			core = new Core(params);
			activity = new Activity();

			getActivity().setDetails("Loading...");
			getActivity().setState("Made by github.com/imAETHER");

			if (activeTime) {
				//TODO: fix this dumb thing >:C
				long bootMillis = TimeUnit.SECONDS.toMillis(Main.SYSTEM_INFO.getOperatingSystem().getSystemBootTime());
				getActivity().timestamps().setStart(new Date(bootMillis).toInstant());
			}

			// Loading image, this will only show up for a few seconds
			getActivity().assets().setLargeImage("https://cdn.discordapp.com/emojis/924458990069497897.gif");

			// Finally, update the current activity to our activity
			core.activityManager().updateActivity(activity);
			new Thread(() -> {
				while (! Thread.interrupted()) {
					try {
						if (core != null) core.runCallbacks();
						Thread.sleep(16);
					} catch (Exception ignored) {
					}
				}
			}, "DiscordRPC Callbacks").start();
		}
	}

	public static Activity getActivity() {
		return activity;
	}

	/**
	 * Used to update the large & small images of the activity, if the application has image assets you can use those
	 * otherwise you can use links to an image/gif. The link length is limited to 127, otherwise an exception will be
	 * thrown.
	 *
	 * @param large the large image to use
	 * @param small the small image to use
	 */
	public static void updateImages(final String large, final String small) {
		if (core == null || activity == null) return;

		if (! activity.assets().getLargeImage().equals(large) && large.length() < 127)
			getActivity().assets().setLargeImage(large);

		if (! activity.assets().getSmallImage().equals(small) && small.length() < 127)
			getActivity().assets().setSmallImage(small);
	}

	/**
	 * Used to update the details of the activity, min length is of 2.
	 *
	 * @param details top most line
	 * @param state   second line
	 * @param large   the text when hovering over the large image
	 * @param small   the text when hovering over the small image
	 */
	public static void update(final String details, final String state, final String large, final String small) {
		if (core == null || activity == null) return;
		boolean updateDetails = ! activity.getDetails()
		                                  .equals(details) && details.length() >= 2, updateState =
				! activity.getState()
				          .equals(state) && state.length() >= 2, updateLarge = ! activity.assets()
		                                                                                 .getLargeText()
		                                                                                 .equals(large), updateSmall =
				! activity.assets()
				          .getSmallText()
				          .equals(small);

		if (updateDetails) activity.setDetails(details);

		if (updateState) activity.setState(state);

		if (updateLarge) activity.assets().setLargeText(large);

		if (updateSmall) activity.assets().setSmallText(small);

		if (updateDetails || updateState) core.activityManager().updateActivity(activity);
	}

	public static void dispose() {
		try {
			activity.close();
			core.close();
		} catch (Exception ignored) {
		}
		core = null;
	}

}
