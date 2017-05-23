/**
 * Copyright (c) 2011, Jordi Cortadella
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the <organization> nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package interp;

import parser.*;
import CRAP.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import javax.management.RuntimeErrorException;

import java.io.*;

import CRAP.CRAP;
import CRAP.Timeline;
import CRAP.Tween;
import CRAP.TweenManager;

/** Class that implements the interpreter of the language. */

public class Interp {
	private String currentInterpType;
	private float currentKeyTimeAbs = 0.0f;
	private float timeScopeStartAbs = 0.0f;
	private float timeScopeFinishAbs = 1.0f;

	public TimelineManager timelineManager;
	public TweenManager tweenManager;

	/** Memory of the virtual machine. */
	public Stack stack;

	/**
	 * Map between function names (keys) and ASTs (values). Each entry of the
	 * map stores the root of the AST correponding to the function.
	 */
	private HashMap<String, CRAPTree> FuncName2Tree;

	/** Standard input of the interpreter (System.in). */
	private Scanner stdin;

	/**
	 * Stores the line number of the current statement. The line number is used
	 * to report runtime errors.
	 */
	private int linenumber = -1;

	/** File to write the trace of function calls. */
	private PrintWriter trace = null;

	/** Nested levels of function calls. */
	private int function_nesting = -1;

	/**
	 * Constructor of the interpreter. It prepares the main data structures for
	 * the execution of the main program.
	 */
	public Interp(CRAPTree T, String tracefile) {
		assert T != null;
		stack = new Stack(); // Creates the memory of the virtual machine
		stack.pushActivationRecord("__global", 0);
		Data light0 = new Data(), light1 = new Data(), light2 = new Data();
		light0.ConvertToObject(); light1.ConvertToObject(); light2.ConvertToObject();
		light0.setProperty("Primitive", new Data("Light"));
		light1.setProperty("Primitive", new Data("Light"));
		light2.setProperty("Primitive", new Data("Light"));
		stack.defineVariable("Light0", light0);
		stack.defineVariable("Light1", light1);
		stack.defineVariable("Light2", light2);
		light0.setProperty("Visible", new Data(false));
		light1.setProperty("Visible", new Data(false));
		light2.setProperty("Visible", new Data(false));
		light0.setProperty("Range", new Data(1.0f));
		light1.setProperty("Range", new Data(1.0f));
		light2.setProperty("Range", new Data(1.0f));
		light0.setProperty("Intensity", new Data(64.0f));
		light1.setProperty("Intensity", new Data(64.0f));
		light2.setProperty("Intensity", new Data(64.0f));

		MapFunctions(T); // Creates the table to map function names into AST
							// nodes
		PreProcessAST(T); // Some internal pre-processing ot the AST

		// Initializes the standard input of the program
		stdin = new Scanner(
				new BufferedReader(new InputStreamReader(System.in)));
		if (tracefile != null) {
			try {
				trace = new PrintWriter(new FileWriter(tracefile));
			} catch (IOException e) {
				System.err.println(e);
				System.exit(1);
			}
		}
		function_nesting = -1;
	}

	public void Init() {
		tweenManager = new TweenManager();
		timelineManager = new TimelineManager(this);

		Timeline mainTimeline = new Timeline("Main", new ArrayList<Data>(),
				0.0f, 1.0f);
		executeTimeline(mainTimeline);
	}

	public void Update() {
		tweenManager.Update();
		timelineManager.Update();
	}

	/** Returns the contents of the stack trace */
	public String getStackTrace() {
		return stack.getStackTrace(lineNumber());
	}

	/** Returns a summarized contents of the stack trace */
	public String getStackTrace(int nitems) {
		return stack.getStackTrace(lineNumber(), nitems);
	}

	/**
	 * Gathers information from the AST and creates the map from function names
	 * to the corresponding AST nodes.
	 */
	private void MapFunctions(CRAPTree T) {
		assert T != null && T.getType() == CRAPLexer.LIST_FUNCTIONS;

		// Map globals and functions
		FuncName2Tree = new HashMap<String, CRAPTree>();
		int n = T.getChildCount();
		for (int i = 0; i < n; ++i) {
			CRAPTree f = T.getChild(i);
			String fname = f.getChild(0).getText();
			switch (f.getType()) {
			case CRAPLexer.GLOBAL:
				Data globalVariable = new Data();
				globalVariable.setType(Data.Type.OBJECT);
				stack.defineVariable(fname, globalVariable);
				break;

			case CRAPLexer.PREFAB:
			case CRAPLexer.TIMELINE:
			case CRAPLexer.FUNCTION:
				if (FuncName2Tree.containsKey(fname)) {
					throw new RuntimeException(
							"Multiple definitions of function " + fname);
				}
				FuncName2Tree.put(fname, f);
				break;
			}
		}
	}

	/**
	 * Performs some pre-processing on the AST. Basically, it calculates the
	 * value of the literals and stores a simpler representation. See
	 * CRAPTree.java for details.
	 */
	private void PreProcessAST(CRAPTree T) {
		if (T == null)
			return;
		switch (T.getType()) {
		case CRAPLexer.NUMBER:
			T.setFloatValue();
			break;
		case CRAPLexer.STRING:
			T.setStringValue();
			break;
		case CRAPLexer.BOOLEAN:
			T.setBooleanValue();
			break;
		default:
			break;
		}
		int n = T.getChildCount();
		for (int i = 0; i < n; ++i)
			PreProcessAST(T.getChild(i));
	}

	/**
	 * Gets the current line number. In case of a runtime error, it returns the
	 * line number of the statement causing the error.
	 */
	public int lineNumber() {
		return linenumber;
	}

	/** Defines the current line number associated to an AST node. */
	private void setLineNumber(CRAPTree t) {
		linenumber = t.getLine();
	}

	/** Defines the current line number with a specific value */
	private void setLineNumber(int l) {
		linenumber = l;
	}

	private Data executeFunction(String funcName, CRAPTree args, Timeline timeline, Data thisRef) {
		// Get the AST of the function
		CRAPTree f = FuncName2Tree.get(funcName);
		if (f == null)
			throw new RuntimeException(" function " + funcName
					+ " not declared");

		// Gather the list of arguments of the caller. This function
		// performs all the checks required for the compatibility of
		// parameters.
		ArrayList<Data> Arg_values = timeline == null ? listArguments(f, args)
				: timeline.GetArgs();

		// Dumps trace information (function call and arguments)
		if (trace != null)
			traceFunctionCall(f, Arg_values);

		// List of parameters of the callee
		CRAPTree p = f.getChild(1);
		int nparam = p.getChildCount(); // Number of parameters

		// Create the activation record in memory
		stack.pushActivationRecord(funcName, lineNumber());
		
		if (thisRef != null)
		{
			stack.defineVariable("this", thisRef);
		}

		if (timeline != null) {
			timeline.SetActivationRecord(stack.GetCurrentActivationRecord());
		}

		// Track line number
		setLineNumber(f);

		// Copy the parameters to the current activation record
		for (int i = 0; i < nparam; ++i) {
			String param_name = p.getChild(i).getText();
			stack.defineVariable(param_name, Arg_values.get(i));
		}

		// Execute the instructions
		Data result = executeListInstructions(f.getChild(2));

		// If the result is null, then the function returns void
		if (result == null)
			result = new Data();

		// Dumps trace information
		if (trace != null)
			traceReturn(f, result, Arg_values);

		// Destroy the activation record
		stack.popActivationRecord();

		return result;
	}

	private Data executeFunction(String funcName, CRAPTree args) {
		return executeFunction(funcName, args, null, null);
	}

	public Data executeTimeline(Timeline timeline) 
	{
		timeScopeStartAbs = timeline.GetStartTimeAbs();
		timeScopeFinishAbs = timeline.GetFinishTimeAbs();

		Data d = executeFunction(timeline.GetName(), null, timeline, null);
		return d;
	}

	/**
	 * Executes a block of instructions. The block is terminated as soon as an
	 * instruction returns a non-null result. Non-null results are only returned
	 * by "return" statements.
	 * 
	 * @param t
	 *            The AST of the block of instructions.
	 * @return The data returned by the instructions (null if no return
	 *         statement has been executed).
	 */
	private Data executeListInstructions(CRAPTree t) {
		assert t != null;
		Data result = null;
		int ninstr = t.getChildCount();
		for (int i = 0; i < ninstr; ++i) {
			result = executeInstruction(t.getChild(i));
			if (result != null)
				return result;
		}
		return null;
	}

	/**
	 * Executes an instruction. Non-null results are only returned by "return"
	 * statements.
	 * 
	 * @param t
	 *            The AST of the instruction.
	 * @return The data returned by the instruction. The data will be non-null
	 *         only if a return statement is executed or a block of instructions
	 *         executing a return.
	 */
	private Data executeInstruction(CRAPTree t) {
		assert t != null;

		setLineNumber(t);
		Data value = new Data(); // The returned value

		// A big switch for all type of instructions
		switch (t.getType()) {
		case CRAPLexer.ASSIGN: {
			value = evaluateExpression(t.getChild(1));
			
			CRAPTree var = t.getChild(0);
			String baseVar = var.getChild(0).getText();

			if (var.getChildCount() == 1) {
				value = value.isObject() ? value : new Data(value);
				stack.defineVariable(baseVar, value);
				return null;
			}

			Data data = stack.getVariable(baseVar);

			for (int i = 1; i < var.getChildCount() - 1; ++i) {
				CRAPTree vari = var.getChild(i);

				String index = vari.getType() == CRAPLexer.ARR_INDEX ? evaluateExpression(
						vari.getChild(0)).toArrIndex()
						: vari.getText();

				data = data.getProperty(index);
			}

			CRAPTree index = var.getLastChild();
			String indexStr = index.getType() == CRAPLexer.ARR_INDEX ? evaluateExpression(
					index.getChild(0)).toArrIndex()
					: index.getText();

			if (data.isVec()) {
				float[] values = value.isVec() ? value.getVecValue()
						.GetValues() : new float[] { value.getNumberValue() };
				data.getVecValue().Swizzle(indexStr, values);
			} else {
				value = value.isObject() ? value : new Data(value);
				data.setProperty(indexStr, value);
			}
			
			return null;
		}

		case CRAPLexer.TWEEN: {
			CRAPTree var = t.getChild(0);
			Data dataToTween = stack.getVariable(var.getChild(0).getText());

			boolean isSwizzling = false;
			String swizzle = "";
			for (int i = 1; i < var.getChildCount(); ++i) {
				if (dataToTween.isVec()) {
					isSwizzling = i < var.getChildCount();
					CRAPTree vari = var.getChild(i);
					swizzle = vari.getType() == CRAPLexer.ARR_INDEX ? evaluateExpression(
							vari.getChild(0)).toArrIndex()
							: vari.getText();
					break;
				}

				CRAPTree vari = var.getChild(i);
				String index = vari.getType() == CRAPLexer.ARR_INDEX ? evaluateExpression(
						vari.getChild(0)).toArrIndex()
						: vari.getText();

				dataToTween = dataToTween.getProperty(index);
			}

			Data finalValue = evaluateExpression(t.getChild(1));

			if (isSwizzling) {
				Vec v = new Vec(dataToTween.getVecValue());
				if (!finalValue.isVec()) {
					if (swizzle.length() > 1)
						throw new RuntimeException(
								"Tweening a vector to a wrong type.");
					if (!finalValue.isNumber())
						throw new RuntimeException(
								"Tweening a vector to a non numeric value.");
					v.Set(swizzle, finalValue.getNumberValue());
				} else {
					v.Swizzle(swizzle, finalValue.getVecValue().GetValues());
				}
				finalValue = new Data(v);
			}

			Tween tween = new Tween(dataToTween, currentKeyTimeAbs, finalValue,
					currentInterpType);

			tweenManager.AddTween(tween);
			return null;
		}

		// If-then-else
		case CRAPLexer.IF:
			value = evaluateExpression(t.getChild(0));
			checkBoolean(value);
			if (value.getBooleanValue())
				return executeListInstructions(t.getChild(1));
			// Is there else statement ?
			if (t.getChildCount() == 3)
				return executeListInstructions(t.getChild(2));
			return null;

			// While
		case CRAPLexer.WHILE:
			while (true) {
				value = evaluateExpression(t.getChild(0));
				checkBoolean(value);
				if (!value.getBooleanValue())
					return null;
				Data r = executeListInstructions(t.getChild(1));
				if (r != null)
					return r;
			}

		case CRAPLexer.KEY:
			float keyTime = evaluateExpression(t.getChild(0).getChild(0))
					.getNumberValue();
			if (t.getChild(0).getText().contains("REL")) {
				currentKeyTimeAbs = timeScopeStartAbs
						+ (timeScopeFinishAbs - timeScopeStartAbs) * keyTime;
			} else {
				currentKeyTimeAbs = timeScopeStartAbs + keyTime;
			}

			currentInterpType = t.getChildCount() == 3 ? t.getChild(1)
					.getText() : "Linear";

			executeListInstructions(t.getLastChild());

			return null;

			// Return
		case CRAPLexer.RETURN:
			if (t.getChildCount() != 0) {
				return evaluateExpression(t.getChild(0));
			}
			return new Data(); // No expression: returns void data

			// Read statement: reads a variable and raises an exception
			// in case of a format error.
		case CRAPLexer.READ:
			String token = null;
			Data val = new Data(0);
			;
			try {
				token = stdin.next();
				val.setValue(Integer.parseInt(token));
			} catch (NumberFormatException ex) {
				throw new RuntimeException(
						"Format error when reading a number: " + token);
			}
			stack.defineVariable(t.getChild(0).getText(), val);
			return null;

			// Write statement: it can write an expression or a string.
		case CRAPLexer.WRITE: {
			CRAPTree v = t.getChild(0);
			Data str = evaluateExpression(v);
			System.out.format(str.isString() ? str.getStringValue() : str
					.toString());
			return null;
		}
		case CRAPLexer.PRINT: {
			if (t.getChildCount() == 0) {
				System.out.println();
				return null;
			}

			CRAPTree v = t.getChild(0);
			Data str = evaluateExpression(v);
			System.out.format(str.isString() ? str.getStringValue() : str
					.toString());
			System.out.println();
			return null;
		}
		case CRAPLexer.DUMP: {
			System.out.println("####### STACK CONTENTS ######");
			if (t.getChildCount() == 1) {
				CRAPTree v = t.getChild(0);
				Data str = evaluateExpression(v);
				String title = str.isString() ? str.getStringValue() : str.toString();
				System.out.println("   " + title);
			}
			stack.Print();
			System.out.println("####### -------------- ######");
			return null;
		}
		// Function call
		case CRAPLexer.FUNCALL:
			executeFunction(t.getChild(0).getText(), t.getChild(1));
			return null;

		case CRAPLexer.TIMELINECALL:
			// Calculate the init and duration of the timeline call
			float initTimeAbs = currentKeyTimeAbs;
			float durationAbs = evaluateExpression(t.getChild(1).getChild(0))
					.getNumberValue();
			if (t.getChild(1).getText().contains("REL")) // Relative duration
			{
				durationAbs *= (timeScopeFinishAbs - timeScopeStartAbs);
			}

			String timelineName = t.getChild(0).getText();
			CRAPTree f = FuncName2Tree.get(timelineName);
			if (f == null)
				throw new RuntimeException(" timeline " + timelineName
						+ " not declared");
			ArrayList<Data> args = listArguments(
					FuncName2Tree.get(timelineName), t.getChild(2));

			// Enqueue the timeline with the specified init and finish time
			Timeline timeline = new Timeline(timelineName, args, initTimeAbs,
					initTimeAbs + durationAbs);

			timelineManager.AddTimeline(timeline);

			return null;

		default:
			assert false; // Should never happen
		}

		// All possible instructions should have been treated.
		assert false;
		return null;
	}

	/**
	 * Evaluates the expression represented in the AST t.
	 * 
	 * @param t
	 *            The AST of the expression
	 * @return The value of the expression.
	 */

	private Data evaluateExpression(CRAPTree t) {
		assert t != null;

		int previous_line = lineNumber();
		setLineNumber(t);
		int type = t.getType();

		Data value = null;
		// Atoms
		switch (type) {
		// A variable
		case CRAPLexer.VAR:
			Data var = evaluateVar(t);
			value = var.isObject() ? var : new Data(var);
			break;
		case CRAPLexer.EMPTYOBJ:
			value = new Data();
			value.setType(Data.Type.OBJECT);
			break;
		case CRAPLexer.RAND:
			Random r = new Random();
			value = new Data(r.nextFloat());
			break;
		case CRAPLexer.VEC:
			int nValues = Integer.parseUnsignedInt(t.getText().substring(3));
			float[] values = new float[nValues];

			int n = 0;
			for (int i = 0; i < t.getChildCount(); ++i) {
				Data v = evaluateExpression(t.getChild(i));
				if (v.isNumber()) {
					values[n++] = v.getNumberValue();
				} else if (v.isVec()) {
					Vec vec = v.getVecValue();
					int s = vec.Size();

					if (n + s > nValues) {
						throw new RuntimeException(
								"Too many values in vector initialization.");
					}
					for (int j = 0; j < s; ++j) {
						values[n++] = vec.Get(j);
					}
				}
			}

			if (n == 1) {
				value = new Data(new Vec(nValues, values[0]));
			} else {
				if (n != nValues)
					throw new RuntimeException(
							"Wrong number of values in vector initialization.");
				value = new Data(new Vec(nValues, values));
			}
			break;
		// An integer literal
		case CRAPLexer.NUMBER:
			value = new Data(Float.parseFloat(t.getText()));
			break;
		// A Boolean literal
		case CRAPLexer.BOOLEAN:
			value = new Data(t.getBooleanValue());
			break;
		case CRAPLexer.STRING:
			value = new Data(t.getStringValue());
			break;

		// A function call. Checks that the function returns a result.
		case CRAPLexer.FUNCALL:
			value = executeFunction(t.getChild(0).getText(), t.getChild(1));
			assert value != null;
			if (value.isVoid()) {
				throw new RuntimeException(
						"function expected to return a value");
			}
			break;
		case CRAPLexer.NEW:
			value = new Data();
			value.ConvertToObject();
			
			CRAPTree funcCall = t.getChild(0);
			
			String prefabName = funcCall.getChild(0).getText();
			if (!prefabName.equals("Object"))
			{
				this.executeFunction(prefabName,
								 	 funcCall.getChild(1), null, value);
			}

			break;
		default:
			break;
		}

		// Retrieve the original line and return
		if (value != null) {
			setLineNumber(previous_line);
			return value;
		}

		// Unary operators
		value = evaluateExpression(t.getChild(0));
		if (t.getChildCount() == 1) {
			switch (type) {
			case CRAPLexer.PLUS:
				checkInteger(value);
				break;
			case CRAPLexer.MINUS:
				checkInteger(value);
				value.setValue(-value.getNumberValue());
				break;
			case CRAPLexer.NOT:
				checkBoolean(value);
				value.setValue(!value.getBooleanValue());
				break;
			default:
				assert false; // Should never happen
			}
			setLineNumber(previous_line);
			return value;
		}

		// Two operands
		Data value2;
		switch (type) {
		// Relational operators
		case CRAPLexer.EQUAL:
		case CRAPLexer.NOT_EQUAL:
		case CRAPLexer.LT:
		case CRAPLexer.LE:
		case CRAPLexer.GT:
		case CRAPLexer.GE:
			value2 = evaluateExpression(t.getChild(1));
			if (value.getType() != value2.getType()) {
				throw new RuntimeException(
						"Incompatible types in relational expression");
			}
			value = value.evaluateRelational(type, value2);
			break;

		case CRAPLexer.CONCAT:
			value2 = evaluateExpression(t.getChild(1));
			value = new Data((value.isString() ? value.getStringValue()
					: value.toString())
					+ (value2.isString() ? value2.getStringValue()
							: value2.toString()));
			break;

		// Arithmetic operators
		case CRAPLexer.PLUS:
		case CRAPLexer.MINUS:
		case CRAPLexer.MUL:
		case CRAPLexer.DIV:
		case CRAPLexer.MOD:
			value2 = evaluateExpression(t.getChild(1));
			value.evaluateArithmetic(type, value2);
			break;

		// Boolean operators
		case CRAPLexer.AND:
		case CRAPLexer.OR:
			// The first operand is evaluated, but the second
			// is deferred (lazy, short-circuit evaluation).
			checkBoolean(value);
			value = evaluateBoolean(type, value, t.getChild(1));
			break;

		default:
			assert false; // Should never happen
		}

		setLineNumber(previous_line);
		return value;
	}

	private Data evaluateVar(CRAPTree t) {
		Data value;
		value = stack.getVariable(t.getChild(0).getText());

		for (int i = 1; i < t.getChildCount(); ++i) {
			CRAPTree ti = t.getChild(i);
			String index = ti.getType() == CRAPLexer.ARR_INDEX ? evaluateExpression(
					ti.getChild(0)).toArrIndex()
					: ti.getText();
			if (value.isVec()) {
				Vec vec = value.getVecValue();
				if (index.length() == 1) {
					value = new Data(vec.Get(index));
				} else {
					value = new Data(vec.Swizzle(index));
				}
				break;
			}
			value = value.getProperty(index);
		}
		return value;
	}

	/**
	 * Evaluation of Boolean expressions. This function implements a
	 * short-circuit evaluation. The second operand is still a tree and is only
	 * evaluated if the value of the expression cannot be determined by the
	 * first operand.
	 * 
	 * @param type
	 *            Type of operator (token).
	 * @param v
	 *            First operand.
	 * @param t
	 *            AST node of the second operand.
	 * @return An Boolean data with the value of the expression.
	 */
	private Data evaluateBoolean(int type, Data v, CRAPTree t) {
		// Boolean evaluation with short-circuit

		switch (type) {
		case CRAPLexer.AND:
			// Short circuit if v is false
			if (!v.getBooleanValue())
				return v;
			break;

		case CRAPLexer.OR:
			// Short circuit if v is true
			if (v.getBooleanValue())
				return v;
			break;

		default:
			assert false;
		}

		// Return the value of the second expression
		v = evaluateExpression(t);
		checkBoolean(v);
		return v;
	}

	/** Checks that the data is Boolean and raises an exception if it is not. */
	private void checkBoolean(Data b) {
		if (!b.isBoolean()) {
			throw new RuntimeException("Expecting Boolean expression");
		}
	}

	/** Checks that the data is integer and raises an exception if it is not. */
	private void checkInteger(Data b) {
		if (!b.isNumber()) {
			throw new RuntimeException("Expecting numerical expression");
		}
	}

	/**
	 * Gathers the list of arguments of a function call. It also checks that the
	 * arguments are compatible with the parameters. In particular, it checks
	 * that the number of parameters is the same and that no expressions are
	 * passed as parametres by reference.
	 * 
	 * @param AstF
	 *            The AST of the callee.
	 * @param args
	 *            The AST of the list of arguments passed by the caller.
	 * @return The list of evaluated arguments.
	 */

	private ArrayList<Data> listArguments(CRAPTree AstF, CRAPTree args) {
		if (args != null)
			setLineNumber(args);
		CRAPTree pars = AstF.getChild(1); // Parameters of the function

		// Create the list of parameters
		ArrayList<Data> Params = new ArrayList<Data>();
		int n = pars.getChildCount();

		// Check that the number of parameters is the same
		int nargs = (args == null) ? 0 : args.getChildCount();
		if (n != nargs) {
			throw new RuntimeException(
					"Incorrect number of parameters calling function "
							+ AstF.getChild(0).getText());
		}

		// Checks the compatibility of the parameters passed by
		// reference and calculates the values and references of
		// the parameters.
		for (int i = 0; i < n; ++i) {
			CRAPTree p = pars.getChild(i); // Parameters of the callee
			CRAPTree a = args.getChild(i); // Arguments passed by the caller
			setLineNumber(a);
			if (p.getType() == CRAPLexer.PVALUE) {
				// Pass by value: evaluate the expression
				Params.add(i, evaluateExpression(a));
			} else {
				// Pass by reference: check that it is a variable
				if (a.getType() != CRAPLexer.VAR) {
					throw new RuntimeException(
							"Wrong argument for pass by reference");
				}
				// Find the variable and pass the reference
				Data v = evaluateVar(a);
				Params.add(i, v);
			}
		}
		return Params;
	}

	/**
	 * Writes trace information of a function call in the trace file. The
	 * information is the name of the function, the value of the parameters and
	 * the line number where the function call is produced.
	 * 
	 * @param f
	 *            AST of the function
	 * @param arg_values
	 *            Values of the parameters passed to the function
	 */
	private void traceFunctionCall(CRAPTree f, ArrayList<Data> arg_values) {
		function_nesting++;
		CRAPTree params = f.getChild(1);
		int nargs = params.getChildCount();

		for (int i = 0; i < function_nesting; ++i)
			trace.print("|   ");

		// Print function name and parameters
		trace.print(f.getChild(0) + "(");
		for (int i = 0; i < nargs; ++i) {
			if (i > 0)
				trace.print(", ");
			CRAPTree p = params.getChild(i);
			if (p.getType() == CRAPLexer.PREF)
				trace.print("&");
			trace.print(p.getText() + "=" + arg_values.get(i));
		}
		trace.print(") ");

		if (function_nesting == 0)
			trace.println("<entry point>");
		else
			trace.println("<line " + lineNumber() + ">");
	}

	/**
	 * Writes the trace information about the return of a function. The
	 * information is the value of the returned value and of the variables
	 * passed by reference. It also reports the line number of the return.
	 * 
	 * @param f
	 *            AST of the function
	 * @param result
	 *            The value of the result
	 * @param arg_values
	 *            The value of the parameters passed to the function
	 */
	private void traceReturn(CRAPTree f, Data result, ArrayList<Data> arg_values) {
		for (int i = 0; i < function_nesting; ++i)
			trace.print("|   ");
		function_nesting--;
		trace.print("return");
		if (!result.isVoid())
			trace.print(" " + result);

		// Print the value of arguments passed by reference
		CRAPTree params = f.getChild(1);
		int nargs = params.getChildCount();
		for (int i = 0; i < nargs; ++i) {
			CRAPTree p = params.getChild(i);
			if (p.getType() == CRAPLexer.PVALUE)
				continue;
			trace.print(", &" + p.getText() + "=" + arg_values.get(i));
		}

		trace.println(" <line " + lineNumber() + ">");
		if (function_nesting < 0)
			trace.close();
	}
}
