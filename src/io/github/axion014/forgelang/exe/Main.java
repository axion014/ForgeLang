package io.github.axion014.forgelang.exe;

import java.io.*;

import io.github.axion014.forgelang.core.Compiler;
import io.github.axion014.forgelang.core.exception.CompileFailedException;
import io.github.axion014.forgelang.tool.DebugUtil;

public class Main {
	public static void main(String[] args) {
		final File input;
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
			if (args.length == 0) {
				System.out.println("Please input the source file path");
				input = new File(new BufferedReader(new InputStreamReader(System.in)).readLine());
			} else {
				input = new File(args[0]);
			}
			if (!input.exists()) {
				throw new FileNotFoundException("Input file " + input.getCanonicalPath() + " is not exist.");
			}
			DebugUtil.benchMark(() -> {
				try {
					outAssembly.createNewFile();
					try (FileWriter filewriter = new FileWriter(outAssembly)) {
						filewriter.write(new Compiler(input(input)).compileWithBenchmark(new AssemblyWriter()));
					}
					assembleWithBenchmark(outAssembly, outobj);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}, "All elapsed time");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (CompileFailedException e) {
			System.err.print("Compile error(line " + e.line + "): ");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void assemble(File assembly, File outobj) throws IOException {
		System.out.println("Output to " + outobj.getCanonicalPath());
		Process process = new ProcessBuilder("lib\\windows\\as\\as.exe", "-o", outobj.getCanonicalPath(),
			assembly.getCanonicalPath()).start();
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

	private static void assembleWithBenchmark(File assembly, File outobj) {
		DebugUtil.benchMark(() -> {
			try {
				assemble(assembly, outobj);
			} catch (IOException e) {
				System.err.println("Internal error happened. Stack trace is: " + e.getStackTrace());
				System.exit(1);
			}
		}, "Assemble time");
	}

	private static String input(File input) {
		StringBuilder textList = new StringBuilder();
		try (FileInputStream is = new FileInputStream(input); InputStreamReader r = new InputStreamReader(is);
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
