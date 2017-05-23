grammar CRAP;

options {
    output = AST;
    ASTLabelType = CRAPTree;
}

// Imaginary tokens to create some AST nodes

tokens {
    LIST_FUNCTIONS; // List of functions (the root of the tree)
    ASSIGN;     // Assignment instruction
    TWEEN;
    PARAMS;     // List of parameters in the declaration of a function
    FUNCALL;    // Function call
    TIMELINECALL;
    TIME_REL;
    TIME_ABS;
    ARGLIST;    // List of arguments passed in a function call
    LIST_INSTR; // Block of instructions
    LIST_KEYS; // Block of keys
    BOOLEAN;    // Boolean atom (for Boolean constants "true" or "false")
    PVALUE;     // Parameter by value in the list of parameters
    PREF;       // Parameter by reference in the list of parameters
    ARR_ACCESS;
    ARR_ELM_ASSIGN; // One element assign
    LIST;
    ARR_INDEX;
    VAR;
}

@header {
package parser;
import interp.CRAPTree;
}

@lexer::header {
package parser;
}


// A program is a list of declarations
prog    :   declaration+ EOF -> ^(LIST_FUNCTIONS declaration+);

declaration     :   globalDeclare | prefabDeclare | funcDeclare | timelineDeclare;

prefabDeclare   :   PREFAB^ ID params '{'! instruction_list '}'!;

globalDeclare   :   GLOBAL^ ID ';'!;

funcDeclare     :   FUNCTION^ ID params '{'! instruction_list '}'!;

prefabCreation  :   NEW^ funcCall;
funcCall        :   ID '(' expr_list? ')' -> ^(FUNCALL ID ^(ARGLIST expr_list?));

timelineDeclare :   TIMELINE^ ID params '{'! instruction_list '}'!;
timelineCall    :   ID time '(' expr_list? ')' -> ^(TIMELINECALL ID time ^(ARGLIST expr_list?));

params          :   '(' paramlist? ')' -> ^(PARAMS paramlist?);

paramlist       :   param (','! param)*;

param           :   '&' id=ID -> ^(PREF[$id,$id.text])
                |   id=ID -> ^(PVALUE[$id,$id.text])
                ;

key         :   KEY^ time INTERP? '{'! instruction_list '}'!;

instruction_list:   instruction* -> ^(LIST_INSTR instruction*);
instruction
        :   ite_stmt          // if-then-else
        |   while_stmt        // while statement
        |   key
        |   assign       ';'! // Assignment
  //      |   tween        ';'! // Tween
        |   timelineCall ';'!
        |   funcCall     ';'! // Call to a procedure (no result produced)
        |   return_stmt  ';'! // Return statement
        |   read         ';'! // Read a variable
        |   write        ';'! // Write a string or an expression
        |   print        ';'! // Write a string or an expression followed by a newline char
        |   dump         ';'!
        |                ';'! // Nothing
        ;

time    :   time_rel | time_abs;
time_rel:   '<<' expr '>>' -> ^(TIME_REL expr);
time_abs:   '[[' expr ']]' -> ^(TIME_ABS expr);

expr_list:  expr (','! expr)*;

assign  : variable ((EQUAL expr) -> ^(ASSIGN variable expr) | (ARROW expr) -> ^(TWEEN variable expr))
		//variable op=(EQUAL|TWEEN) expr -> ^($op variable expr)
		;

// if-then-else (else is optional)
ite_stmt:   IF^ '('! expr ')'! '{'! instruction_list '}'! (ELSE! '{'! instruction_list '}'! )? ;

// while statement
while_stmt  :   WHILE^ '('! expr ')'! '{'! instruction_list '}'!;

// Return statement with an expression
return_stmt :   RETURN^ expr?;

// Read a variable
read        :   READ^ ID;

// Write an expression or a string
write       :   WRITE^ expr;
print       :   PRINT^ expr?;
dump        :   DUMP^  expr?;

// Grammar for expressions with boolean, relational and aritmetic operators
expr    :   boolexpr (CONCAT^ boolexpr)*;

boolexpr:   boolterm (OR^ boolterm)*;

boolterm:   boolfact (AND^ boolfact)*;

boolfact:   num_expr ((EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr)?;

num_expr:   term ( (PLUS^ | MINUS^) term)*;

term    :   factor ( (MUL^ | DIV^ | IDIV^ | MOD^) factor)*;
factor  :   (NOT^ | PLUS^ | MINUS^)? atom;

variableIndex: '[' expr ']' -> ^(ARR_INDEX expr);
variableElem: ID (variableIndex)*;
variable: r=variableElem ('.' c+=variableElem)* -> ^(VAR $r $c*);

atom    :   variable
		|   EMPTYOBJ
        |   STRING
        |   NUMBER
        |   VEC^ '('! expr_list ')'!
	|   prefabCreation
	|   RAND
        //|   (ID '[' expr ']') -> ^(ARR_ACCESS[$ID,"ARR_ACCESS"] ID expr)
        |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
        |   funcCall
        |   '('! expr ')'!
        //|   '[' expr_list ']' -> ^(LIST expr_list)
        ;


// Basic tokens
EQUAL   : '=' ;
ARROW   : '->';
NOT_EQUAL: '!=' ;
LT      : '<' ;
LE      : '<=';
GT      : '>';
GE      : '>=';
CONCAT  : '..';
PLUS    : '+' ;
MINUS   : '-' ;
MUL     : '*';
DIV     : '/';
IDIV    : ':/';
MOD     : '%' ;
EMPTYOBJ: '{' '}';
NOT     : 'not';
AND     : 'and' ;
OR      : 'or' ;
IF      : 'if' ;
ELSE    : 'else' ;
WHILE   : 'while' ;
TIMELINE    : 'timeline' ;
FUNCTION    : 'function' ;
RETURN  : 'return' ;
NEW     : 'new' ;
READ    : 'read' ;
WRITE   : 'write' ;
PRINT   : 'print' ;
RAND    : 'rand()' ;
DUMP    : 'dump';
TRUE    : 'true' ;
FALSE   : 'false';
KEY     : 'key';
VEC     : 'vec2' | 'vec3' | 'vec4';
GLOBAL  : 'global';
PREFAB  : 'prefab'; 
INTERP  : ('Back' | 'Bounce' | 'Circ' | 'Cubic' | 'Elastic' | 'Expo' | 'Linear' | 'Quad' | 'Quart' | 'Quint' | 'Sine') ('In' | 'Out' | 'InOut' | );
ID      : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
//|          ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ('.' ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*)+;
NUMBER  : '0'..'9'+ | '0'..'9'+ '.' '0'..'9'+;

// C-style comments
COMMENT : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
        | '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
        ;

// Strings (in quotes) with escape sequences
STRING  :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
        ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;

// White spaces
WS      : ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
        ;
