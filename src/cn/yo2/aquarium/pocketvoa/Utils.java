package cn.yo2.aquarium.pocketvoa;

import java.io.File;

public class Utils {
	
	public static String extractFilename(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

	/**
	 * Delete file or directory
	 * 
	 * @param fileName the name of the file or directory to be deleted
	 * @return true if successful, false otherwise
	 */
	public static boolean delete(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			return false;
		} else {
			if (file.isFile()) {
				return deleteFile(fileName);
			} else {
				return deleteDirectory(fileName);
			}
		}
	}

	/**
	 * Delete a single file
	 * 
	 * @param fileName the name of the file to be deleted
	 * @return true if successful, false otherwise
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile() && file.exists()) {
			return file.delete();
		} else {
			return false;
		}
	}

	/**
	 * Recursive delete directory and files under it
	 * 
	 * @param dir the directory to be deleted
	 * @return true if successful, false otherwise
	 */
	public static boolean deleteDirectory(String dir) {
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		File dirFile = new File(dir);
		
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
			
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}

		if (!flag) {
			return false;
		}

		// delete current directory
		return dirFile.delete();
	}

	/**
	 * convert date string from yyyyMMdd to yyyy-MM-dd
	 * 
	 * @param date
	 * @return the converted date string
	 */
	public static String convertDateString(String date) {
		StringBuilder sb = new StringBuilder(date);
		sb.insert(4, '-');
		sb.insert(7, '-');
		return sb.toString();
	}

	/**
	 * convert date string from /\d{2}-\d{1,2}-\d{1,2}/ to yyyyMMdd
	 * 
	 * @param date
	 * @return the formated date string
	 */
	public static String formatDateString(String date) {
		String[] parts = date.split("-", 3);
		StringBuilder sb = new StringBuilder("20");
		sb.append(parts[0]);
		if (parts[1].length() == 1) {
			sb.append('0');
		}
		sb.append(parts[1]);
		if (parts[2].length() == 1) {
			sb.append('0');
		}
		sb.append(parts[2]);
		return sb.toString();
	}
}
