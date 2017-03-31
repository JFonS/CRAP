grammar CRAP;

options {
    output = AST;
    ASTLabelType = CRAPTree;
}

// Imaginary tokens to create some AST nodes

tokens {
    LIST_FUNCTIONS; // List of functions (the root of the tree)
    ASSIGN;     // Assignment instruction
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
    PREFAB_PROPS;
    ARR_ACCESS;
    ARR_ELM_ASSIGN; // One element assign
    LIST;
}

@header {
package parser;
import interp.CRAPTree;
}

@lexer::header {
package parser;
}


// A program is a list of functions
prog: declaration+ EOF -> ^(LIST_FUNCTIONS declaration+);

declaration: globalVarDeclare | prefabDeclare | funcDeclare | timelineDeclare;

prop_list: (assign ';')* -> ^(PREFAB_PROPS assign*);
prefabDeclare  : PREFAB^ ID (':' ID)? '{'! 
		prop_list
	  '}'!;

globalVarDeclare: GLOBAL^ ID';'!;

funcDeclare: FUNCTION^ ID params '{'! instruction_list '}'!;
funcCall: ID '(' expr_list? ')' -> ^(FUNCALL ID ^(ARGLIST expr_list?));

timelineDeclare: TIMELINE^ ID params '{'! instruction_list '}'!;
timelineCall: ID time '(' expr_list? ')' -> ^(TIMELINECALL ID time ^(ARGLIST expr_list?));

params	: '(' paramlist? ')' -> ^(PARAMS paramlist?);
paramlist: param (','! param)*;
param   :   '&' id=ID -> ^(PREF[$id,$id.text])
        |   id=ID -> ^(PVALUE[$id,$id.text]);

key: KEY^ time '{'! instruction_list '}'!;
interp_type: LINEAR | CUBIC;

instruction_list:	instruction* -> ^(LIST_INSTR instruction*);
instruction
        :	ite_stmt          // if-then-else
        |	while_stmt        // while statement
	|       key
        |	assign       ';'! // Assignment
        |	tween        ';'! // Tween
        |       timelineCall ';'!
        |       funcCall     ';'! // Call to a procedure (no result produced)
        |	return_stmt  ';'! // Return statement
        |	read         ';'! // Read a variable
        | 	write        ';'! // Write a string or an expression
        |                    ';'! // Nothing
        ;

time: time_rel | time_abs;
time_rel: '<<' expr '>>' -> ^(TIME_REL expr);
time_abs: '[[' expr ']]' -> ^(TIME_ABS expr);

expr_list:  expr (','! expr)*;

// Assignment
tween   :       ID tw=TWEEN expr -> ^(TWEEN[$tw, "TWEEN"] ID expr);

assign	:	ID eq=EQUAL expr -> ^(ASSIGN[$eq,"ASSIGN"] ID expr)
	|	ID '[' index=expr ']' eq=EQUAL val=expr -> ^(ARR_ELM_ASSIGN ^(ARR_ACCESS ID $index) $val )
        ;

// if-then-else (else is optional)
ite_stmt	:	IF^ '('! expr ')'! '{'! instruction_list '}'! (ELSE! '{'! instruction_list '}'! )? 
            ;

// while statement
while_stmt	:	WHILE^ '('! expr ')'! '{'! instruction_list '}'!
            ;

// Return statement with an expression
return_stmt	:	RETURN^ expr?
        ;

// Read a variable
read	:	READ^ ID
        ;

// Write an expression or a string
write	:   WRITE^ (expr | STRING )
        ;


// Grammar for expressions with boolean, relational and aritmetic operators
expr    :   boolterm (OR^ boolterm)*;

boolterm:   boolfact (AND^ boolfact)*;

boolfact:   num_expr ((EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr)?;

num_expr:   term ( (PLUS^ | MINUS^) term)*;

term    :   factor ( (MUL^ | DIV^ | MOD^) factor)*;
factor  :   (NOT^ | PLUS^ | MINUS^)? atom;

atom    :   ID
        |   NUMBER
	    |   VEC^ '('! expr_list ')'!
    	|   (ID '[' expr ']') -> ^(ARR_ACCESS[$ID,"ARR_ACCESS"] ID expr)
        |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
        |   funcCall
        |   '('! expr ')'!
	    |   '[' expr_list ']' -> ^(LIST expr_list)
        ;


// Basic tokens
EQUAL	: '=' ;
TWEEN   : '->';
NOT_EQUAL: '!=' ;
LT	    : '<' ;
LE	    : '<=';
GT	    : '>';
GE	    : '>=';
PLUS	: '+' ;
MINUS	: '-' ;
MUL	    : '*';
DIV	    : '/';
MOD	    : '%' ;
NOT	    : 'not';
AND	    : 'and' ;
OR	    : 'or' ;
IF  	: 'if' ;
ELSE	: 'else' ;
WHILE	: 'while' ;
TIMELINE	: 'timeline' ;
FUNCTION	: 'function' ;
RETURN	: 'return' ;
READ	: 'read' ;
WRITE	: 'write' ;
TRUE    : 'true' ;
FALSE   : 'false';
KEY     : 'key';
VEC     : 'vec';
GLOBAL  : 'global';
PREFAB  :  'prefab'; 
LINEAR  : 'Linear';
CUBIC   : 'Cubic';
ID      :('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* | 
	 ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ('.' ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*)+;
NUMBER  : '0'..'9'+ | '0'..'9'+ '.' '0'..'9'+;

// C-style comments
COMMENT	: '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
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
WS  	: ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    	;


