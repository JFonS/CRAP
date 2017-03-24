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

public class Data {
    /** Types of data */
    public enum Type {VOID, BOOLEAN, NUMBER, ARRAY;}

    /** Type of data*/
    private Type type;

    /** Value of the data */
    private float value;
    private ArrayList<Data> arrayValues;

    Data(int v) { type = Type.NUMBER; value = v; }
    Data(float v) { type = Type.NUMBER; value = v; }

    /** Constructor for Booleans */
    Data(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; }

    Data(ArrayList<Data> arr) { type = Type.ARRAY; value = -1; arrayValues = arr; }

    /** Constructor for void data */
    Data() {type = Type.VOID; }

    /** Copy constructor */
    Data(Data d) { type = d.type; value = d.value; arrayValues = d.arrayValues; }

    /** Returns the type of data */
    public Type getType() { return type; }

    /** Indicates whether the data is Boolean */
    public boolean isBoolean() { return type == Type.BOOLEAN; }

    /** Indicates whether the data is integer */
    public boolean isNumber() { return type == Type.NUMBER; }

    /** Indicates whether the data is void */
    public boolean isVoid() { return type == Type.VOID; }

    public boolean isArray() { return type == Type.ARRAY; }

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

    public Data getArrayValue(int index) {
        assert type == Type.ARRAY;
        return arrayValues.get( (index + arrayValues.size() * 999) % arrayValues.size()  );
    }

    /** Defines a Boolean value for the data */
    public void setValue(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; }

    /** Defines an integer value for the data */
    public void setValue(float v) { type = Type.NUMBER; value = v; }
    
    /** Defines an integer value for the data */
    public void setValue(ArrayList<Data> array)
    { 
        type = Type.ARRAY; 
        value = -1;
        arrayValues = array;
    }
    public void setValue(int index, Data value)
    { 
        type = Type.ARRAY;
        arrayValues.set(index, value);
    }

    /** Copies the value from another data */
    public void setData(Data d) { type = d.type; value = d.value; }
    
    /** Returns a string representing the data in textual form. */
    public String toString() {
        if (type == Type.BOOLEAN) return value == 1 ? "true" : "false";
        if (type == Type.NUMBER) return Float.toString(value);
        if (type == Type.ARRAY)
        {
            int s = arrayValues.size();
            if (s == 0) return "[]";
            
            String str = "[";
            for (int i = 0; i < s-1; ++i)
            {
                str += arrayValues.get(i).toString() + ", ";
            }
            str += arrayValues.get(s-1) + "]";
            return str;
        }

        return "<" + type.toString() + ">";
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
