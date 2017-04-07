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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Class to represent the memory of the virtual machine of the
 * interpreter. The memory is organized as a stack of activation
 * records and each entry in the activation record contains is a pair
 * <name of variable,value>.
 */
 
public class Stack {

    /** Stack of activation records */
    private LinkedList< HashMap<String,Data> > actRecords;

    /** Reference to the current activation record */
    private HashMap<String,Data> currentAR = null;

    /**
     * Class to represent an item of the Stack trace.
     * For each function call, the function name and
     * the line number of the call are stored.
     */
    class StackTraceItem {
        public String fname; // Function name
        public int line; // Line number
        public StackTraceItem (String name, int l) {
            fname = name; line = l;
        }
    }

    /** Stack trace to keep track of function calls */
    private LinkedList<StackTraceItem> stackTrace;
    
    /** Constructor of the memory */
    public Stack() {
        actRecords = new LinkedList<HashMap<String,Data>>();
        currentAR = null;
        stackTrace = new LinkedList<StackTraceItem>();
    }

    /** Creates a new activation record on the top of the stack */
    public void pushActivationRecord(String name, int line) {
        currentAR = new HashMap<String,Data>();
        actRecords.addLast (currentAR);
        stackTrace.addLast (new StackTraceItem(name, line));
    }

    public HashMap<String, Data> GetCurrentActivationRecord()
    {
    	return actRecords.getLast();
    }
    
    /** Destroys the current activation record */
    public void popActivationRecord() {
        actRecords.removeLast();
        if (actRecords.isEmpty()) currentAR = null;
        else currentAR = actRecords.getLast();
        stackTrace.removeLast();
    }
    
    public String toString()
    {
    	String out = "{\n";
    	for (HashMap<String,Data> ar : actRecords)
    	{
    		for (String varName : ar.keySet())
    		{
    			Data data = ar.get(varName);
    			out += "  " + varName + ": " + data.toString() + "\n";
    		}
    	}
    	out += "}\n";
    	return out;
    }
    
    public void Print()
    {
    	System.out.println( this );
    }

    /** Defines the value of a variable. If the variable does not
     * exist, it is created. If it exists, the value and type of
     * the variable are re-defined.
     * @param name The name of the variable
     * @param value The value of the variable
     */
    public void defineVariable(String varName, Data value) 
    {
    	HashMap<String, Data> activationRecord = actRecords.getFirst();
        Data var = activationRecord.get(varName);
        if (var == null) // If its not globally defined, then use local one 
        {
        	activationRecord = currentAR;
        	var = activationRecord.get(varName); // Else modify local one
        }
    	
        if (var == null) 
        {
        	activationRecord.put(varName, value); // New definition
        }
        else
        {
        	var.setData(value);
        }
    }

    /** Gets the value of the variable. The value is represented as
     * a Data object. In this way, any modification of the object
     * implicitly modifies the value of the variable.
     * @param name The name of the variable
     * @return The value of the variable
     */
    public Data getVariable(String varName) 
    {
        Data v = actRecords.getFirst().get(varName);
        if (v == null)
        {
        	// Look for v in local AR, if not found global
        	v = currentAR.get(varName);
        }

        if (v != null)
        {
        	return v;
        }
        else { throw new RuntimeException ("Variable " + varName + " not defined"); }
    }
    
    public ArrayList<Data> GetGlobalDatas()
    {
    	HashMap<String,Data> globalAR = actRecords.getFirst();
    	ArrayList<Data> globalDatas = new ArrayList<Data>();
    	globalDatas.addAll(globalAR.values());
    	return globalDatas;
    }

    /**
     * Generates a string with the contents of the stack trace.
     * Each line contains a function name and the line number where
     * the next function is called. Finally, the line number in
     * the current function is written.
     * @param current_line program line executed when this function
     *        is called.
     * @return A string with the contents of the stack trace.
     */ 
    public String getStackTrace(int current_line) {
        int size = stackTrace.size();
        ListIterator<StackTraceItem> itr = stackTrace.listIterator(size);
        StringBuffer trace = new StringBuffer("---------------%n| Stack trace |%n---------------%n");
        trace.append("** Depth = ").append(size).append("%n");
        while (itr.hasPrevious()) {
            StackTraceItem it = itr.previous();
            trace.append("|> ").append(it.fname).append(": line ").append(current_line).append("%n");
            current_line = it.line;
        }
        return trace.toString();
    }

    /**
     * Generates a string with a summarized contents of the stack trace.
     * Only the first and last items of the stack trace are returned.
     * @param current_line program line executed when this function
     *        is called.
     * @param nitems number of function calls returned in the string
     *        at the beginning and at the end of the stack.
     * @return A string with the contents of the stack trace.
     */ 
    public String getStackTrace(int current_line, int nitems) {
        int size = stackTrace.size();
        if (2*nitems >= size) return getStackTrace(current_line);
        ListIterator<StackTraceItem> itr = stackTrace.listIterator(size);
        StringBuffer trace = new StringBuffer("---------------%n| Stack trace |%n---------------%n");
        trace.append("** Depth = ").append(size).append("%n");
        int i;
        for (i = 0; i < nitems; ++i) {
           StackTraceItem it = itr.previous();
           trace.append("|> ").append(it.fname).append(": line ").append(current_line).append("%n");current_line = it.line;
        }
        trace.append("|> ...%n");
        for (; i < size-nitems; ++i) current_line = itr.previous().line;
        for (; i < size; ++i) {
           StackTraceItem it = itr.previous();
           trace.append("|> ").append(it.fname).append(": line ").append(current_line).append("%n");current_line = it.line;
        }
        return trace.toString();
    } 
}
    
