package io.github.axion014.forgelang.writer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.github.axion014.forgelang.analyze.word.*;

public class AssemblyWriter implements DestinationWriter {

	private static final String[] argregisters64 = {"rsi", "rdi", "rcx", "rdx", "r8", "r9"};
	private StringBuilder assembly;
	int line;
	private List<Func> funcs;
	private List<Symbol> afters;
	private List<FLStr> strings;
	private List<String> cfuncs;
	private String a;
	private String b;
	private String c;
	private String d;
	private String sp;
	private String bp;
	private boolean is64bit;

	public AssemblyWriter(boolean is64bit) {
		this.is64bit = is64bit;
		a = is64bit ? "rax" : "eax";
		b = is64bit ? "rbx" : "ebx";
		c = is64bit ? "rcx" : "ecx";
		d = is64bit ? "rdx" : "edx";
		sp = is64bit ? "rsp" : "esp";
		bp = is64bit ? "rbp" : "ebp";
	}

	private void writeExpr(Word expr) {
		if (expr instanceof FLInt) {
			assembly.append(String.format("mov " + a + ", %d\n\t", ((FLInt) expr).value));
		} else if (expr instanceof FLStr) {
			assembly.append("mov " + a + ", s" + ((FLStr) expr).pos + "\n\t");
		} else if (expr instanceof BinaryOperator) {
			if (((BinaryOperator) expr).type == BinaryOperatorType.SGN) {
				writeExpr(((BinaryOperator) expr).right);
				if (!(((BinaryOperator) expr).left instanceof Symbol))
					throw new InternalError("Invaild word found when writeing assembly");
				moveTo((Symbol) ((BinaryOperator) expr).left);
				return;
			}
			writeExpr(((BinaryOperator) expr).left);
			assembly.append("push " + a + "\n\t");
			writeExpr(((BinaryOperator) expr).right);
			switch (((BinaryOperator) expr).type) {
				case ADD:
					assembly.append("pop " + b + "\n\tadd " + a + ", " + b + "\n\t");
					break;
				case SUB:
					assembly.append("pop " + b + "\n\tsub " + b + ", " + a + "\n\tmov " + a + ", " + b + "\n\t");
					break;
				case MUL:
					assembly.append("pop " + b + "\n\timul " + a + ", " + b + "\n\t");
					break;
				case DIV:
					assembly.append(
						"mov " + b + ", " + a + "\n\tpop " + a + "\n\tmov " + d + ", 0\n\tidiv " + b + "\n\t");
					break;
				default:
					throw new InternalError("Invaild word found when writeing assembly");
			}
		} else if (expr instanceof Symbol) {
			writeSymbol((Symbol) expr);
		} else if (expr instanceof FuncCall) {
			List<Word> args = new LinkedList<>(((FuncCall) expr).args);
			Collections.reverse(args);
			for (Word arg : args) {
				writeExpr(arg);
				assembly.append("push " + a + "\n\t");
			}
			assembly.append("call " + ((FuncCall) expr).name + "\n\t");
		} else if (expr instanceof Func) {
			funcs.add((Func) expr);
		} else if (expr instanceof CFuncCall) {
			List<Word> args = new LinkedList<>(((CFuncCall) expr).args);
			Collections.reverse(args);
			int stack = args.size() * (is64bit ? 8 : 4);
			if (is64bit) {
				for (int i = 0; i < 6; i++) {
					if (args.isEmpty()) break;
					writeExpr(args.get(0));
					args.remove(0);
					assembly.append("mov " + argregisters64[i] + ", " + a + "\n\t");
				}
			}
			for (Word arg : args) {
				writeExpr(arg);
				assembly.append("push " + a + "\n\t");
			}
			assembly.append("call _" + ((CFuncCall) expr).name + "\n\t");
			assembly.append("sub " + sp + ", " + stack + "\n\t");
		} else {
			throw new InternalError("Invaild word found when writeing assembly");
		}
	}

	private void scanExpr(Word expr) {
		if (expr instanceof BinaryOperator) {
			if (((BinaryOperator) expr).type == BinaryOperatorType.SGN) {
				scanExpr(((BinaryOperator) expr).right);
				return;
			}
			scanExpr(((BinaryOperator) expr).left);
			scanExpr(((BinaryOperator) expr).right);
		} else if (expr instanceof FuncCall) {
			List<Word> args = new LinkedList<>(((FuncCall) expr).args);
			Collections.reverse(args);
			for (Word arg : args)
				scanExpr(arg);
		} else if (expr instanceof CFuncCall) {
			if (!cfuncs.contains(((CFuncCall) expr).name)) cfuncs.add(((CFuncCall) expr).name);
			List<Word> args = new LinkedList<>(((CFuncCall) expr).args);
			Collections.reverse(args);
			for (Word arg : args)
				scanExpr(arg);
		} else if (expr instanceof Func) {
			for (Word inexpr : ((Func) expr).scope.body)
				scanExpr(inexpr);
		} else if ((expr instanceof FLStr) && !strings.contains(expr)) strings.add((FLStr) expr);
	}

	private void writeSymbol(Symbol target) {
		if (target.before != null) {
			switch (target.before.type) {
				case INC:
					moveFrom(target);
					assembly.append("inc " + a + "\n\t");
					moveTo(target);
					break;
				case DEC:
					moveFrom(target);
					assembly.append("dec " + a + "\n\t");
					moveTo(target);
					break;
				default:
					throw new InternalError("Invaild word found when writeing assembly");
			}
		}
		moveFrom(target);
		if (target.after != null) {
			switch (target.after.type) {
				case INC:
				case DEC:
					afters.add(target);
					break;
				default:
					throw new InternalError("Invaild word found when writeing assembly");
			}
		}
	}

	private void moveTo(Symbol target) {
		if (target.isparam) {
			assembly.append("mov [" + bp + " + " + (target.pos + 1) * (is64bit ? 8 : 4) + "], " + a + "\n\t");
		} else {
			assembly.append("mov [" + bp + " - " + target.pos * (is64bit ? 8 : 4) + "], " + a + "\n\t");
		}
	}

	private void moveFrom(Symbol target) {
		if (target.isparam) {
			assembly.append("mov " + a + ", [" + bp + " + " + (target.pos + 1) * (is64bit ? 8 : 4) + "]\n\t");
		} else {
			assembly.append("mov " + a + ", [" + bp + " - " + target.pos * (is64bit ? 8 : 4) + "]\n\t");
		}
	}

	@Override
	public void writeCode(List<Word> exprs, StringBuilder assembly) {
		this.assembly = assembly;
		funcs = new LinkedList<>();
		afters = new LinkedList<>();
		strings = new LinkedList<>();
		cfuncs = new LinkedList<>();
		for (Word expr : exprs) {
			scanExpr(expr);
		}
		if (strings != null) {
			assembly.append("\tsection .data\n");
			for (FLStr string : strings) {
				string.value = string.value.replaceAll("\\\\n", "\", 0xa, \"");
				assembly.append("s" + string.pos + " db \"" + string.value + "\", 0\n");
			}
			assembly.append("\t");
		}
		assembly.append("section .text\n\tglobal _mymain\n");
		for (String funcname : cfuncs) {
			assembly.append("\textern _" + funcname + "\n");
		}
		assembly.append("_mymain:\n\tpush " + bp + "\n\tmov " + bp + ", " + sp + "\n\t");
		line = 1;
		for (Word expr : exprs) {
			afters.clear();
			writeExpr(expr);
			for (Symbol target : afters) {
				switch (target.after.type) {
					case INC:
						moveFrom(target);
						assembly.append("inc " + a + "\n\t");
						moveTo(target);
						break;
					case DEC:
						moveFrom(target);
						assembly.append("dec " + a + "\n\t");
						moveTo(target);
						break;
					default:
						throw new InternalError();
				}
			}
			line++;
		}
		assembly.append("leave\n\tret\n");
		for (Func func : funcs) { // function define
			assembly.append(func.name + ":\n\t");
			assembly.append("push " + bp + "\n\tmov " + bp + ", " + sp + "\n\t");
			for (Word expr : func.scope) {
				writeExpr(expr);
			}
			assembly.append("leave\n\tret " + func.params.size() * (is64bit ? 8 : 4) + "\n");
		}
	}
}
