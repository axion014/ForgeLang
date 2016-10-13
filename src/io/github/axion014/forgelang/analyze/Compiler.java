package io.github.axion014.forgelang.analyze;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.axion014.forgelang.analyze.exception.CompileFailedException;
import io.github.axion014.forgelang.analyze.exception.MatchingFailedException;
import io.github.axion014.forgelang.analyze.word.*;
import io.github.axion014.forgelang.tool.DebugUtil;
import io.github.axion014.forgelang.tool.Snippet;
import io.github.axion014.forgelang.tool.throwablefunction.*;
import io.github.axion014.forgelang.writer.DestinationWriter;

public class Compiler {
	private int cursor;
	private String code;
	/**
	 * コード内で定義された変数。
	 */
	public SymbolOriginal variables = null;
	/**
	 * コード内で定義された関数。
	 */
	public Func functions = null;
	private int line;
	private int nest;
	/**
	 * 現在のスコープ。
	 */
	public Scope scope;

	public Compiler(String code) {
		this.code = code;
	}
	
	/**
	 * コンパイルします。
	 * 
	 * @param writer
	 *            構文木から出力コードに変換するためのオブジェクト
	 * @return コンパイルの結果
	 * @throws CompileFailedException
	 */
	public String compile(DestinationWriter writer) throws CompileFailedException {
		StringBuilder output = new StringBuilder();
		List<Word> exprs = new LinkedList<>();
		try {
			cursor = 0;
			line = 1;
			nest = 0;
			do {
				registerFunction();
				line++;
			} while (cursor != code.length());
			cursor = 0;
			line = 1;
			nest = 0;
			do {
				exprs.add(readExpression(false));
				line++;
			} while (cursor != code.length());
			for (Func func = functions; func != null; func = func.next) {
				exprs.add(func);
			}
			System.out.println("Parse complete");
			System.out.println("Syntax Trees:");
			for (Word expr : exprs) {
				System.out.println(expr.toString());
			}
		} catch (CompileFailedException e) {
			e.line = line;
			throw e;
		}
		writer.writeCode(exprs, output);
		return output.toString();
	}

	private static final Pattern intPattern = Pattern.compile("\\A[1-9]\\d*|0"); // any integer
	private static final Pattern operatorPattern = Pattern.compile("\\A[^\\w\\s]+$?"); // 任意の記号列
	private static final Pattern symbolPattern = Pattern.compile("\\A[a-zA-Z_]\\w*"); // 1文字目はアルファベット、それ以降はアルファベットか数字
	private static final Pattern funcPattern = Pattern.compile("\\A[a-zA-Z_][\\w\\.]*(?= *\\((.|[\n])*\\))");
	private static final Pattern typePattern = Pattern.compile("\\A(int|string)");

	private void skipSpaces(boolean allownewline) {
		while (code.length() != cursor && Character.isWhitespace(hereChar()) && (allownewline || hereChar() != '\n')) {
			cursor++;
		}
	}

	/**
	 * カーソルが仮引数のカッコの前にあるときに呼び出すと、カーソルを次の行の初めに移動し、リストを返します。
	 * 
	 * @return 仮引数が含まれたリスト
	 * @throws CompileFailedException
	 */
	public List<Symbol> getParams() throws CompileFailedException {
		expect('(');
		List<Symbol> params = new LinkedList<>();
		for (;;) {
			skipSpaces(true);
			if (code.charAt(cursor) == ')') break;
			doIfMatchHereElse(typePattern, (type) -> {
				cursor += type.length();
				skipSpaces(false);
				params.add(makeSymbol(type).asParam());
			}, () -> {
				throw new CompileFailedException("Parameter expected");
			});
			if (code.charAt(cursor) == ')') break;
			expect(',');
		}
		cursor++;
		skipSpaces(false);
		cursor++;
		return params;
	}

	/**
	 * カーソルがブロックの初めにある状態で呼び出すと、カーソルをブロックの終わりまで移動し、中の式を返します。
	 * 
	 * @param isFunc
	 *            ブロックが関数か
	 * @return ブロック内にある式のリスト
	 * @throws CompileFailedException
	 */
	public List<Word> readBlock(boolean isFunc) throws CompileFailedException {
		nest++;
		List<Word> exprs = new LinkedList<>();
		for (;;) {
			for (int i = 0; i < nest; i++) {
				if (hereChar() != '\t') {
					nest = i;
					cursor--;
					return exprs;
				}
				cursor++;
			}
			line++;
			exprs.add(readExpression(isFunc));
		}
	}

	private Word readExpression(boolean isFunc) throws CompileFailedException {
		if (Character.isWhitespace(hereChar()) && hereChar() != '\n')
			throw new CompileFailedException("Unexpected whitespace");
		boolean isReturnWord = false;
		if (hereCode().startsWith("return") && Character.isWhitespace(code.charAt(cursor + 6))) {
			if (!isFunc) throw new CompileFailedException("can't return from outer of function");
			isReturnWord = true;
			cursor += 6;
		}
		Word expr = readNext(0);
		expr.isReturnWord = isReturnWord;
		skipSpaces(false);
		expect('\n', "Unterminated expression");
		return expr;
	}

	private void registerFunction() throws CompileFailedException {
		if (Character.isWhitespace(code.charAt(cursor)) && code.charAt(cursor) != '\n')
			throw new CompileFailedException("Unexpected whitespace");
		skipSpaces(true);
		char nowchar = code.charAt(cursor);
		if (nowchar == '"' || Character.isDigit(nowchar)) return;
		doIfMatchHere(typePattern, (type) -> {
			cursor += type.length();
			skipSpaces(false);
			Pattern funcDefPattern = Pattern.compile("\\A\\(([^,]+)?+(,([^,]+))*\\)\\s*\n(\t{" + (nest + 1) + "}.*)+");
			doIfMatchHere(funcPattern, (name) -> {
				cursor += name.length();
				if (funcDefPattern.matcher(hereCode()).find()) new Func(name, this);
			});
			Matcher m = symbolPattern.matcher(hereCode());
			if (m.find()) {
				String name = code.substring(cursor, m.end() + cursor);
				cursor += name.length();
				skipSpaces(false);
				if (code.charAt(cursor) == '\n') {
					cursor++;
					return;
				}
				Matcher m2 = operatorPattern.matcher(hereCode());
				if (!m2.find()) throw new CompileFailedException("Unterminated expression");
				String operatorstr = code.substring(cursor, m2.end() + cursor);
				if (!BinaryOperatorType.isExist(operatorstr)) throw new CompileFailedException("Invaild operator");
				BinaryOperatorType nowtype = BinaryOperatorType.from(operatorstr);
				if (nowtype != BinaryOperatorType.SGN) return;
				cursor++;
				skipSpaces(true);
				char nowchar2 = code.charAt(cursor);
				if (nowchar2 == '"' || Character.isDigit(nowchar2)) return;
				if (funcDefPattern.matcher(hereCode()).find()) new Func(name, this);
			}
			skipSpaces(false);
			expect('\n', "Unterminated expression");
		});
		endProgramLine:
		for (;;) {
			switch (lineEnd()) {
				case ',':
					moveToNextLine();
				default:
					break endProgramLine;
			}
		}
		moveToNextLine();
	}

	private Word readNext(int priority) throws CompileFailedException {
		Word word = readPrimitive(priority == 0);
		for (;;) {
			skipSpaces(false);
			Matcher m = operatorPattern.matcher(hereCode());
			if (!m.find()) {
				if (word instanceof SymbolOriginal) {
					// @formatter:off
					BinaryOperator tmp = new BinaryOperator() {{type = BinaryOperatorType.SGN;}};
					// @formatter:on
					switch(((Symbol)word).type) {
						case "int":
							tmp.right = new FLInt(0);
							break;
						case "string":
							tmp.right = new FLStr("");
							break;
						default:
							throw new CompileFailedException("Invaild default initialize");
					}
					tmp.left = word;
					word = tmp;
				}
				break;
			}
			String operatorstr = code.substring(cursor, m.end() + cursor);
			if (!BinaryOperatorType.isExist(operatorstr)) break;
			BinaryOperatorType nowtype = BinaryOperatorType.from(operatorstr);
			cursor += nowtype.length;
			if (priority <= nowtype.priority) {
				skipSpaces(false);
				// @formatter:off
				BinaryOperator tmp = new BinaryOperator() {{
					type = nowtype;
					right = readNext(nowtype.priority + 1);
				}};
				// @formatter:on
				tmp.left = word;
				word = tmp;
			} else {
				cursor -= ((Value) word).length;
				break;
			}
		}
		return word;
	}

	private Value readPrimitive(boolean isFirstWord) throws CompileFailedException {
		skipSpaces(isFirstWord);
		if (!isFirstWord && hereChar() == '"') {
			if (code.indexOf('"', cursor + 1) == -1) throw new CompileFailedException("Unterminated string");
			FLStr value = new FLStr(code.substring(cursor + 1, code.indexOf('"', cursor + 1)).replace("\n", ""));
			int localcursor = 0;
			int escape;
			while ((escape = value.value.indexOf('\\', localcursor)) != -1) {
				localcursor += escape + 1;
				switch (value.value.charAt(localcursor)) {
					case 'n':
						value.value.replaceFirst("\\n", "\n");
						break;
					case 't':
						value.value.replaceFirst("\\t", "\t");
						break;
					default:
						throw new CompileFailedException("Unterminated \\");
				}
			}
			cursor += value.length;
			return value;
		} else if (!isFirstWord && Character.isDigit(hereChar())) {
			try {
				return doIfMatchHere(intPattern, (hit) -> {
					FLInt value = new FLInt(Integer.parseInt(hit));
					cursor += value.length;
					return value;
				});
			} catch (MatchingFailedException e) {
				throw new InternalError();
			}
		} else {
			final int originalcursor = cursor;
			return doIfMatchHereElse(funcPattern, (name) -> {
				cursor += name.length();
				skipSpaces(false);
				expect('(');
				for (;;) {
					skipSpaces(true);
					if (hereChar() == ')') break;
					readNext(1);
					skipSpaces(false);
					if (hereChar() == ')') break;
					expect(',');
				}
				cursor = originalcursor + name.length();
				skipSpaces(false);
				cursor++;
				List<Word> args = new LinkedList<>();
				for (;;) {
					skipSpaces(true);
					if (hereChar() == ')') break;
					args.add(readNext(1));
					if (hereChar() == ')') break;
					expect(',');
				}
				cursor++;
				Value value;
				if (name.startsWith("c.")) {
					name = name.substring(2);
					value = new CFuncCall();
					((CFuncCall) value).name = name;
					((CFuncCall) value).args = args;
				} else {
					value = new FuncCall();
					((FuncCall) value).name = name;
					((FuncCall) value).args = args;
				}
				return value;
			}, () -> {
				return doIfMatchHereElse(typePattern, (type) -> {
					cursor += type.length();
					skipSpaces(false);
					if (Pattern.compile("\\A[a-zA-Z_]\\w*\\(([^,]+)?+(,([^,]+))*\\)\\s*\n(\t{" + (nest + 1) + "}.*)+")
							.matcher(hereCode()).find()) {
						moveToNextLine();
						skipBlock();
						return readPrimitive(true);
					}
					return makeSymbol(type);
				}, () -> {
					return readSymbol();
				});
			});
		}
	}

	private Symbol readSymbol() throws CompileFailedException {
		UnaryOperator before = doIfMatchHereElse(operatorPattern, (operatorstr) -> {
			if (UnaryOperatorType.isExist(operatorstr)) {
				UnaryOperatorType nowtype = UnaryOperatorType.from(operatorstr);
				cursor += nowtype.length;
				UnaryOperator ret = new UnaryOperator();
				ret.type = nowtype;
				return ret;
			}
			return null;
		}, () -> {
			return null;
		});
		return doIfMatchHereElse(symbolPattern, (name) -> {
			if (isKeyword(name)) throw new CompileFailedException("\"" + name + "\" is a keyword");
			Symbol value = findVariable(name);
			if (value == null) throw new CompileFailedException("local variable " + name + " is not exist");
			cursor += name.length();
			skipSpaces(false);
			doIfMatchHere(operatorPattern, (operatorstr) -> {
				if (UnaryOperatorType.isExist(operatorstr)) {
					UnaryOperatorType type = UnaryOperatorType.from(operatorstr);
					cursor += type.length;
					value.after = new UnaryOperator();
					value.after.type = type;
				}
			});
			if (before != null) {
				value.before = before;
			}
			return value;
		}, () -> {
			throw new CompileFailedException("Primitive expected");
		});
	}

	private Symbol makeSymbol(String type) throws CompileFailedException {
		return doIfMatchHereElse(symbolPattern, (name) -> {
			if (isKeyword(name)) throw new CompileFailedException("\"" + name + "\" is a keyword");
			if (isVariable(name)) throw new CompileFailedException("duplicated local variable " + name);
			cursor += name.length();
			return new SymbolOriginal(name, type, this);
		}, () -> {
			throw new CompileFailedException("Primitive expected");
		});
	}

	private Symbol findVariable(String name) {
		for (SymbolOriginal v = variables; v != null; v = v.next) {
			if (name.equals(v.name) && v.scope == scope) return new Symbol(v);
		}
		return null;
	}

	private boolean isVariable(String name) {
		for (SymbolOriginal v = variables; v != null; v = v.next) {
			if (name.equals(v.name) && v.scope == scope) return true;
		}
		return false;
	}

	/**
	 * 現在のスコープを脱出します。
	 */
	public void exitScope() {
		scope = scope.parent;
	}

	@SuppressWarnings("unused")
	private void deleteVariable(String name) {
		SymbolOriginal prev = null;
		for (SymbolOriginal v = variables; v != null; v = v.next) {
			if (name.equals(v.name) && prev != null) prev.next = v.next;
			prev = v;
		}
	}

	@SuppressWarnings("unused")
	private Func findFunction(String name) {
		for (Func v = functions; v != null; v = v.next) {
			if (name.equals(v.name)) return v;
		}
		return null;
	}

	private void moveToNextLine() {
		cursor = code.indexOf('\n', cursor) + 1;
		line++;
	}

	private void skipBlock() {
		for (;;) {
			for (int i = 0; i < nest + 1; i++) {
				if (hereChar() != '\t') return;
				cursor++;
			}
			moveToNextLine();
		}
	}

	private static boolean isKeyword(String str) {
		switch (str) {
			case "return":
				return true;
		}
		return false;
	}

	/**
	 * かかる時間を計測してコンパイルします。
	 * 
	 * @param writer
	 *            構文木から出力コードに変換するためのオブジェクト
	 * @return コンパイルの結果
	 * @throws CompileFailedException
	 */
	public String compileWithBenchmark(DestinationWriter writer) throws CompileFailedException {
		return DebugUtil.benchMark(() -> compile(writer), "Compile time");
	}

	private void expect(char c, String message) throws CompileFailedException {
		if (hereChar() != c) throw new CompileFailedException(message);
		cursor++;
	}

	private void expect(char c) throws CompileFailedException {
		expect(c, "'" + c + "' expected, but got '" + code.charAt(cursor) + "'");
	}

	private String hereCode() {
		return code.substring(cursor);
	}

	private char hereChar() {
		return code.charAt(cursor);
	}

	private char lineEnd() {
		if (hereChar() == '\n') return code.charAt(cursor - 1);
		String tmp = code.substring(cursor, code.indexOf('\n', cursor)).trim();
		return tmp.charAt(tmp.length() - 1);
	}

	private <E extends Throwable> void doIfMatchHere(Pattern pattern, ThrowableConsumer<String, E> task) throws E {
		Snippet.doIfMatch(pattern, hereCode(), task);
	}

	private <E extends Throwable, R> R doIfMatchHere(Pattern pattern, ThrowableFunction<String, R, E> task)
			throws E, MatchingFailedException {
		return Snippet.doIfMatch(pattern, hereCode(), task);
	}

	private <E extends Throwable, F extends Throwable> void doIfMatchHereElse(Pattern pattern,
			ThrowableConsumer<String, E> thentask, ThrowableRunnable<F> elsetask) throws E, F {
		Snippet.doIfMatchElse(pattern, hereCode(), thentask, elsetask);
	}

	private <E extends Throwable, F extends Throwable, R> R doIfMatchHereElse(Pattern pattern,
			ThrowableFunction<String, R, E> thentask, ThrowableSupplier<R, F> elsetask) throws E, F {
		return Snippet.doIfMatchElse(pattern, hereCode(), thentask, elsetask);
	}
}
