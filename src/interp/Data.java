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

/**
 * Class to represent data in the interpreter.
 * Each data item has a type and a value. The type can be integer
 * or Boolean. Each operation asserts that the operands have the
 * appropriate types.
 * All the arithmetic and Boolean operations are calculated in-place,
 * i.e., the result is stored in the same data.
 * The type VOID is used to represent void values on function returns.
 */

import parser.*;
import java.util.*;

import org.omg.CORBA.portable.StreamableValue;

public class Data {
    /** Types of data */
    public enum Type {VOID, BOOLEAN, NUMBER, OBJECT, STRING;}

    /** Type of data*/
    private Type type;

    /** Value of the data */
    private float value;
    private String str;
    private HashMap<String, Data> properties;

    public Data(int v) 
    { 
    	type = Type.NUMBER; 
    	value = v;
    	properties = new HashMap<String, Data>(); 
    }
    public Data(float v) 
    {
    	type = Type.NUMBER; 
    	value = v;
    	properties = new HashMap<String, Data>(); 
    }

    /** Constructor for Booleans */
    public Data(boolean b) 
    { 
    	type = Type.BOOLEAN; 
    	value = b ? 1 : 0;
		properties = new HashMap<String, Data>(); 
	}

    /** Constructor for void data */
    public Data() 
    {
    	type = Type.VOID; 
    	properties = new HashMap<String, Data>();
    }

    /** Copy constructor */
    public Data(Data copyThis)
    { 
    	setData(copyThis);
    }
    
    /** Constructor for Booleans */
    public Data(String s) 
    { 
    	type = Type.STRING; 
    	str = s;
    	properties = new HashMap<String, Data>();
	}

    /** Returns the type of data */
    public Type getType() { return type; }

    /** Indicates whether the data is Boolean */
    public boolean isBoolean() { return type == Type.BOOLEAN; }

    /** Indicates whether the data is integer */
    public boolean isNumber() { return type == Type.NUMBER; }

    /** Indicates whether the data is void */
    public boolean isVoid() { return type == Type.VOID; }
    
    public boolean isString() { return type == Type.STRING; }

    public boolean isObject() { return type == Type.OBJECT; }

    /**
     * Gets the value of an integer data. The method asserts that
     * the data is an integer.
     */
    public float getNumberValue() {
        assert type == Type.NUMBER;
        return value;
    }

    /**
     * Gets the value of a Boolean data. The method asserts that
     * the data is a Boolean.
     */
    public boolean getBooleanValue() {
        assert type == Type.BOOLEAN;
        return value == 1;
    }
    
    public String getStringValue() {
        assert type == Type.STRING;
        return str;
    }
    
    public Data getProperty(String propertyPath)
    {
    	if (!propertyPath.isEmpty())
    	{
    		String propName = propertyPath.split("\\.", 2)[0];

    		if (propertyPath.contains("."))
    		{
	    		String propPathNoName = propertyPath.split("\\.", 2)[1];
	    		return properties.get(propName).getProperty(propPathNoName);
    		}
    		else { return properties.get(propName); }
    	}
		return this;
    }

    /** Defines a Boolean value for the data */
    public void setValue(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; }

    /** Defines an integer value for the data */
    public void setValue(float v) { type = Type.NUMBER; value = v; }
    
    public void setValue(String s) { type = Type.STRING; str = s; }
    
    public void setType(Data.Type type) { this.type = type; }
    
    public void setProperty(String propertyPath, Data propertyValue)
    { 
        type = Type.OBJECT;

    	String propName = propertyPath.split("\\.", 2)[0];
    	Data prop;
    	if (properties.containsKey(propName)) { prop = properties.get(propName); }
    	else 
    	{ 
    		prop = new Data();
        	properties.put(propName, prop);
        }

        if (propertyPath.contains("."))
        {
        	prop.setProperty(propertyPath.split("\\.", 2)[1], propertyValue);
        }
        else
        {
        	prop.setData(propertyValue);
        }
    }

    /** Copies the value from another data */
    public void setData(Data copyThis)
    { 
    	type = copyThis.type; 
    	value = copyThis.value;
    	str = copyThis.str;
    	properties = new HashMap<String, Data>();
    	for (String propName : copyThis.properties.keySet())
    	{
    		Data v = new Data( copyThis.getProperty(propName) );
    		setProperty(propName, v);
    	}
    }
    
    /** Returns a string representing the data in textual form. */
    public String toString() 
    {
        if (type == Type.BOOLEAN) return value == 1 ? "true" : "false";
        if (type == Type.NUMBER) return Float.toString(value);
        if (type == Type.STRING) return "\"" + str + "\"";
        
        // Type OBJECT
        String str = "{";
        for (String propName: properties.keySet())
        {
            str += propName + ": "  + getProperty(propName).toString() + ", ";
        }
        str += "}";
        str = str.replace(", }", "}");
        return str;
    }
    
    /**
     * Checks for zero (for division). It raises an exception in case
     * the value is zero.
     */
    private void checkDivZero(Data d) {
        if (d.value == 0) throw new RuntimeException ("Division by zero");
    }

    /**
     * Evaluation of arithmetic expressions. The evaluation is done
     * "in place", returning the result on the same data.
     * @param op Type of operator (token).
     * @param d Second operand.
     */
     
    public void evaluateArithmetic (int op, Data d) {
        assert type == Type.NUMBER && d.type == Type.NUMBER;
        switch (op) {
            case CRAPLexer.PLUS: value += d.value; break;
            case CRAPLexer.MINUS: value -= d.value; break;
            case CRAPLexer.MUL: value *= d.value; break;
            case CRAPLexer.DIV: checkDivZero(d); value /= d.value; break;
            case CRAPLexer.MOD: checkDivZero(d); value %= d.value; break;
            default: assert false;
        }
    }

    /**
     * Evaluation of expressions with relational operators.
     * @param op Type of operator (token).
     * @param d Second operand.
     * @return A Boolean data with the value of the expression.
     */
    public Data evaluateRelational (int op, Data d) {
        assert type != Type.VOID && type == d.type;
        switch (op) {
            case CRAPLexer.EQUAL: return new Data(value == d.value);
            case CRAPLexer.NOT_EQUAL: return new Data(value != d.value);
            case CRAPLexer.LT: return new Data(value < d.value);
            case CRAPLexer.LE: return new Data(value <= d.value);
            case CRAPLexer.GT: return new Data(value > d.value);
            case CRAPLexer.GE: return new Data(value >= d.value);
            default: assert false; 
        }
        return null;
    }
}
