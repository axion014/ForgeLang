package io.github.axion014.forgelang;

import static io.github.axion014.forgelang.tool.Platforms.*;

import java.io.*;

import io.github.axion014.forgelang.analyze.Compiler;
import io.github.axion014.forgelang.analyze.exception.CompileFailedException;
import io.github.axion014.forgelang.tool.DebugUtil;
import io.github.axion014.forgelang.tool.Platforms;
import io.github.axion014.forgelang.tool.UnsuppotedPlatformException;
import io.github.axion014.forgelang.writer.AssemblyWriter;

public class Main {
	private static File input = null;
	private static Platforms outplatform = null;
	private static Platforms nativeplatform;
	private static File output = null;
	private static boolean is64bit;

	public static void main(String[] args) {
		processArguments(args);
		final String platform = System.getProperty("os.name").toLowerCase();
		final String architecture = System.getProperty("os.arch");
		is64bit = architecture.equals("x86_64");
		if (platform.startsWith("windows")) {
			nativeplatform = is64bit ? WIN64 : WIN;
		} else if (platform.startsWith("mac")) {
			nativeplatform = MAC;
		} else if (platform.startsWith("linux")) {
			nativeplatform = is64bit ? LINUX64 : LINUX;
		}
		if (outplatform == null) outplatform = nativeplatform;
		File outAssemblytmp = new File("tmp.asm");
		if (outAssemblytmp.exists()) {
			int i = 0;
			do {
				outAssemblytmp = new File("tmp" + i + ".asm");
				i++;
			} while (outAssemblytmp.exists());
		}
		final File outAssembly = outAssemblytmp;
		File outobjtmp = new File("out.o");
		if (outobjtmp.exists()) {
			int i = 0;
			do {
				outobjtmp = new File("out" + i + ".o");
				i++;
			} while (outobjtmp.exists());
		}
		final File outobj = outobjtmp;
		try {
			if (input == null) {
				System.out.println("Please input the source file path");
				input = new File(new BufferedReader(new InputStreamReader(System.in)).readLine());
				if (!input.exists())
					throw new FileNotFoundException("Input file " + input.getCanonicalPath() + " is not exist.");
			}

			DebugUtil.benchMark(() -> {
				try {
					outAssembly.createNewFile();
					try (FileWriter filewriter = new FileWriter(outAssembly)) {
						filewriter.write(new Compiler(input(input)).compileWithBenchmark(new AssemblyWriter(is64bit)));
					}
					assembleWithBenchmark(outAssembly, outobj);
				} catch (IOException | UnsuppotedPlatformException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}, "All elapsed time");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (CompileFailedException e) {
			System.err.print("Compile error(line " + e.line + ": " + e.code.substring(0, 12) + "...): ");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void processArguments(String[] args) {
		int count = 0;
		for (String arg : args) {
			switch (arg) {
				case "-win32":
					outplatform = WIN;
					break;
				case "-win64":
					outplatform = WIN64;
					break;
				case "-mac":
					outplatform = MAC;
					break;
				default:
					switch(count) {
						case 0:
							input = new File(arg);
						case 1:
							output  = new File(arg);
					}
			}
		}
	}

	private static void assemble(File assembly, File outobj) throws IOException, UnsuppotedPlatformException {
		System.out.println("Output to " + outobj.getCanonicalPath());
		String outplatstr;
		if (outplatform == WIN64) {
			outplatstr = "win64";
		} else if (outplatform == MAC) {
			outplatstr = "macho64";
		} else if (outplatform == LINUX) {
			outplatstr = "elf";
		} else if (outplatform == LINUX64) {
			outplatstr = "elf64";
		} else {
			outplatstr = "win";
		}
		Process process = null;
		if (nativeplatform == WIN || nativeplatform == WIN64) {
			process = new ProcessBuilder("lib\\nasm\\nasm.exe", "-f", outplatstr, "-o", outobj.getCanonicalPath(),
				assembly.getCanonicalPath()).start();
		} else if (nativeplatform == MAC) {
			process = new ProcessBuilder("lib/nasm/nasm", "-f", outplatstr, "-o", outobj.getCanonicalPath(),
				assembly.getCanonicalPath()).start();
		} else throw new UnsuppotedPlatformException();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader er = new BufferedReader(new InputStreamReader(process.getErrorStream()));) {
			while (process.isAlive()) {
				if (br.ready()) {
					String line = br.readLine();
					System.out.println(line);
				}
				if (er.ready()) {
					String line = er.readLine();
					System.err.println(line);
				}
			}
		}
	}

	private static void assembleWithBenchmark(File assembly, File outobj) throws UnsuppotedPlatformException {
		DebugUtil.benchMark(() -> {
			try {
				assemble(assembly, outobj);
			} catch (IOException e) {
				System.err.println("Internal error happened. Stack trace is: ");
				e.printStackTrace();
				System.exit(1);
			}
		}, "Assemble time");
	}

	private static String input(File input) {
		StringBuilder textList = new StringBuilder();
		try (FileInputStream is = new FileInputStream(input);
				InputStreamReader r = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(r)) {
			for (;;) {
				String text = br.readLine(); // 改行コードは含まれない
				if (text == null) {
					break;
				}
				textList.append(text);
				textList.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return textList.toString();
	}
}
