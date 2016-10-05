package io.github.axion014.forgelang.exe;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.github.axion014.forgelang.core.DestinationWriter;
import io.github.axion014.forgelang.core.word.*;

public class AssemblyWriter implements DestinationWriter {

	private StringBuilder assembly;
	int line;
	private List<Func> funcs;
	private List<Symbol> afters;

	private void writeExpr(Word expr) {
		if (expr instanceof OmniInt) {
			assembly.append(String.format("movl $%d, %%eax\n\t", ((OmniInt) expr).value));
		} else if (expr instanceof OmniStr) {
			assembly.append("lea .s" + ((OmniStr) expr).pos + ", %eax\n\t");
		} else if (expr instanceof BinaryOperator) {
			if (((BinaryOperator) expr).type == BinaryOperatorType.SGN) {
				writeExpr(((BinaryOperator) expr).right);
				if (!(((BinaryOperator) expr).left instanceof Symbol))
					throw new InternalError("Invaild word found when writeing assembly");
				moveTo((Symbol) ((BinaryOperator) expr).left);
				return;
			}
			writeExpr(((BinaryOperator) expr).left);
			assembly.append("pushl %eax\n\t");
			writeExpr(((BinaryOperator) expr).right);
			switch (((BinaryOperator) expr).type) {
				case ADD:
					assembly.append("popl %ebx\n\taddl %ebx, %eax\n\t");
					break;
				case SUB:
					assembly.append("popl %ebx\n\tsubl %eax, %ebx\n\tmovl %ebx, %eax\n\t");
					break;
				case MUL:
					assembly.append("popl %ebx\n\timul %ebx, %eax\n\t");
					break;
				case DIV:
					assembly.append("movl %eax, %ebx\n\tpopl %eax\n\tmovl $0, %edx\n\tidiv %ebx\n\t");
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
				assembly.append("pushl %eax\n\t");
			}
			assembly.append("call " + ((FuncCall) expr).name + "\n\t");
		} else if (expr instanceof Func) {
			funcs.add((Func) expr);
		} else if (expr instanceof CFuncCall) {
			List<Word> args = new LinkedList<>(((CFuncCall) expr).args);
			Collections.reverse(args);
			for (Word arg : args) {
				writeExpr(arg);
				assembly.append("pushl %eax\n\t");
			}
			assembly.append("call _" + ((CFuncCall) expr).name + "\n\t");
			assembly.append("addl $" + args.size() * 4 + ", %esp\n\t");
		} else {
			throw new InternalError("Invaild word found when writeing assembly");
		}
	}
	
	private void writeSymbol(Symbol target) {
		if (target.before != null) {
			switch (target.before.type) {
				case INC:
					moveFrom(target);
					assembly.append("incl %eax\n\t");
					moveTo(target);
					break;
				case DEC:
					moveFrom(target);
					assembly.append("decl %eax\n\t");
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
			assembly.append("movl %eax, " + target.pos * 4 + "(%edx)\n\t");
		} else {
			assembly.append("movl %eax, -" + target.pos * 4 + "(%ebp)\n\t");
		}
	}
	
	private void moveFrom(Symbol target) {
		if (target.isparam) {
			assembly.append("movl " + target.pos * 4 + "(%edx), %eax\n\t");
		} else {
			assembly.append("movl -" + target.pos * 4 + "(%ebp), %eax\n\t");
		}
	}

	@Override
	public void writeCode(List<Word> exprs, OmniStr strings, StringBuilder assembly) {
		this.assembly = assembly;
		funcs = new LinkedList<>();
		afters = new LinkedList<>();
		if (strings != null) {
			assembly.append("\t.data\n");
			for (OmniStr str = strings; str != null; str = str.next) {
				assembly.append(".s" + str.pos + ":\n\t.string \"");
				assembly.append(str.value);
				assembly.append("\"\n");
			}
			assembly.append("\t");
		}
		assembly.append(".text\n\t.global _mymain\n_mymain:\n\t");
		line = 1;
		for (Word expr : exprs) {
			afters.clear();
			writeExpr(expr);
			for (Symbol target : afters) {
				switch (target.after.type) {
					case INC:
						moveFrom(target);
						assembly.append("incl %eax\n\t");
						moveTo(target);
						break;
					case DEC:
						moveFrom(target);
						assembly.append("decl %eax\n\t");
						moveTo(target);
						break;
					default:
						throw new InternalError();
				}
			}
			line++;
		}
		assembly.append("ret\n");
		for (Func func : funcs) { // function define
			assembly.append(func.name + ":\n\t");
			assembly.append("movl %esp, %edx\n\t");
			for (Word expr : func.scope) {
				writeExpr(expr);
			}
			assembly.append("ret $" + func.params.size() * 4 + "\n");
		}
	}
}
