package com.sik.markiv.utils;

/**
 * @author sik
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class SnowManager {

	public void updateSnow(final boolean snowing) {
		System.out.println("Updating snow background.");
		final File css = new File(
				"/Users/sik/Documents/My Webs/mkiv/mkivmenu.css");
		final File withSnow = new File(
				"/Users/sik/Documents/My Webs/mkiv/mkivmenu_snow.css");
		final File withoutSnow = new File(
				"/Users/sik/Documents/My Webs/mkiv/mkivmenu_nosnow.css");
		File inFile = null;
		FileChannel src = null;
		FileChannel dest = null;

		if (snowing) {
			inFile = withSnow;
		} else {
			inFile = withoutSnow;
		}
		try {
			src = new FileInputStream(inFile).getChannel();
			dest = new FileOutputStream(css).getChannel();
			dest.transferFrom(src, 0, src.size());
			src.close();
			dest.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		System.out.println("File copied:" + inFile.getName());

	}

}
