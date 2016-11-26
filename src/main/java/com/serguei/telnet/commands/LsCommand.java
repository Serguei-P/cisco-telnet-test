package com.serguei.telnet.commands;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Serguei
 * 
 *         This processes command "ls" Currently only one parameter is implemented - "-l"
 * 
 */
public class LsCommand extends Command {
	private final List<String> params;

	public LsCommand(UserSessionContext context, List<String> params) {
		super(context);
		this.params = params;
	}

	private String mask = null;
	private final List<String> paramList = new ArrayList<String>();

	private void getParams() {
		for (String param : params) {
			if (param.startsWith("-"))
				paramList.add(param.substring(1));
			else if (mask == null) {
				mask = param;
				if (File.separatorChar != '/')
					mask = mask.replace('/', File.separatorChar);
			}
		}
	}

	private static class MyFileFilter implements FileFilter {
		private final String mask;

		private MyFileFilter(String mask) {
			this.mask = mask;
		}

		public boolean accept(File file) {
			String fileName = file.getName();
			if (mask.startsWith("*") && mask.endsWith("*")) {
				return fileName.contains(mask.subSequence(1, mask.length() - 1));
			} else if (mask.startsWith("*")) {
				return fileName.endsWith(mask.substring(1));
			} else if (mask.endsWith("*")) {
				return fileName.startsWith(mask.substring(0, mask.length() - 1));
			} else
				return mask.equals(fileName);
		}
	}

	private File[] listFiles() {
		if (mask != null) {
			File file = new File(getContext().getCurrentDir() + File.separator + mask);
			if (file.exists()) {
				if (file.isFile()) {
					File[] result = new File[1];
					result[0] = file;
					return result;
				} else {
					return file.listFiles();
				}
			} else {
				int sepPos = -1;
				int pos = mask.indexOf(File.separatorChar);
				while (pos >= 0) {
					sepPos = pos;
					pos = mask.indexOf(File.separatorChar, sepPos + 1);
				}
				String fileMask;
				if (sepPos >= 0) {
					file = new File(getContext().getCurrentDir() + File.separator + mask.substring(0, sepPos));
					fileMask = mask.substring(sepPos + 1);
				} else {
					file = new File(getContext().getCurrentDir());
					fileMask = mask;
				}
				return file.listFiles(new MyFileFilter(fileMask));
			}
		} else {
			File file = new File(getContext().getCurrentDir());
			return file.listFiles();
		}
	}

	private void outputFiles(File[] fileList) {
		if (fileList.length > 0) {
			if (paramList.contains("l")) {
				outputLong(fileList);
			} else {
				outputShort(fileList);
			}
		}
	}

	private void outputLong(File[] fileList) {
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		for (File file : fileList) {
			StringBuilder line = new StringBuilder();
			if (file.isDirectory())
				line.append("d");
			else
				line.append(" ");
			if (file.canRead())
				line.append("r");
			else
				line.append(" ");
			if (file.canWrite())
				line.append("w");
			else
				line.append(" ");
			if (file.canExecute())
				line.append("e");
			else
				line.append(" ");
			if (file.isHidden())
				line.append("h");
			else
				line.append(" ");
			line.append(String.format("%1$,18d", Long.valueOf(file.length())));
			Date date = new Date(file.lastModified());
			line.append("  ").append(format.format(date));
			line.append("  ").append(file.getName());
			String outLine = line.toString();
			if (outLine.length() > getContext().maxOutputWidth())
				outLine = outLine.substring(0, getContext().maxOutputWidth());
			if (!outputLine(outLine))
				break;
		}
	}

	private void outputShort(File[] fileList) {
		int maxLen = 0;
		for (File file : fileList) {
			if (file.getName().length() > maxLen)
				maxLen = file.getName().length();
		}
		int colCount;
		if (paramList.contains("1"))
			colCount = 1;
		else {
			colCount = getContext().maxOutputWidth() / maxLen;
			while (colCount > 1 && getContext().maxOutputWidth() - colCount * maxLen < (colCount - 1) * 2)
				colCount--;
		}
		StringBuilder strBuilder = new StringBuilder();
		int colNo = 0;
		for (File file : fileList) {
			while (strBuilder.length() < colNo * (maxLen + 2))
				strBuilder.append(' ');
			strBuilder.append(file.getName());
			colNo++;
			if (colNo >= colCount) {
				if (!outputLine(strBuilder.toString()))
					return;
				strBuilder.setLength(0);
				colNo = 0;
			}
		}
		if (strBuilder.length() > 0)
			outputLine(strBuilder.toString());
	}

	@Override
	public void process() {
		getParams();
		File[] fileList = listFiles();
		if (fileList != null) {
			outputFiles(fileList);
		} else
			outputLine("Error: Wrong path");
	}

	@Override
	public String getName() {
		return "ls";
	}

}
