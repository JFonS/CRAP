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
    ARGLIST;    // List of arguments passed in a function call
    LIST_INSTR; // Block of instructions
    TIMELINE_BODY;
    LIST_KEYS; // Block of keys
    BOOLEAN;    // Boolean atom (for Boolean constants "true" or "false")
    PVALUE;     // Parameter by value in the list of parameters
    PREF;       // Parameter by reference in the list of parameters
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
prog: routineDeclare+ EOF -> ^(LIST_FUNCTIONS routineDeclare+);

routineDeclare: funcDeclare | timelineDeclare;

funcDeclare: FUNCTION^ ID params '{'! instruction_list '}'!;
funcCall: ID '(' expr_list? ')' -> ^(FUNCALL ID ^(ARGLIST expr_list?));

timelineDeclare: TIMELINE^ ID params '{'! timeline_body '}'!;
timelineCall: ID '(' NUMBER ')' '(' expr_list? ')' -> ^(TIMELINECALL ID NUMBER ^(ARGLIST expr_list?));

params	: '(' paramlist? ')' -> ^(PARAMS paramlist?);
paramlist: param (','! param)*;
param   :   '&' id=ID -> ^(PREF[$id,$id.text])
        |   id=ID -> ^(PVALUE[$id,$id.text]);

key: KEY^ '('! NUMBER (','! interp_type)? ')'! '{'! instruction_list '}'!;
interp_type: LINEAR | CUBIC;

instruction_list:	instruction* -> ^(LIST_INSTR instruction*);
instruction
        :	assign       ';'! // Assignment
        |	ite_stmt          // if-then-else
        |	while_stmt        // while statement
        |   timelineCall ';'!
        |   funcCall     ';'! // Call to a procedure (no result produced)
        |	return_stmt  ';'! // Return statement
        |	read         ';'! // Read a variable
        | 	write        ';'! // Write a string or an expression
        |                ';'! // Nothing
        ;

timeline_body: (timeline_instruction)* -> ^(TIMELINE_BODY timeline_instruction*);
timeline_instruction: instruction | key;

expr_list:  expr (','! expr)*;

// Assignment
assign	:	ID eq=EQUAL expr -> ^(ASSIGN[$eq,"ASSIGN"] ID expr)
	//|	ID eq=EQUAL list -> ^(ARR_ASSIGN[$eq,"ARR_ASSIGN"] ID list )
	|	ID '[' index=expr ']' eq=EQUAL val=expr -> ^(ARR_ELM_ASSIGN ^(ARR_ACCESS ID $index) $val )
        ;

// if-then-else (else is optional)
ite_stmt	:	IF^ expr THEN! instruction_list (ELSE! instruction_list)? ENDIF!
            ;

// while statement
while_stmt	:	WHILE^ expr DO! instruction_list ENDWHILE!
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
    	|   (ID '[' expr ']') -> ^(ARR_ACCESS[$ID,"ARR_ACCESS"] ID expr)
        |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
        |   funcCall
        |   '('! expr ')'!
	    |   '[' expr_list ']' -> ^(LIST expr_list)
        ;


// Basic tokens
EQUAL	: '=' ;
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
THEN	: 'then' ;
ELSE	: 'else' ;
ENDIF	: 'endif' ;
WHILE	: 'while' ;
DO	    : 'do' ;
ENDWHILE: 'endwhile' ;
TIMELINE	: 'timeline' ;
FUNCTION	: 'function' ;
RETURN	: 'return' ;
READ	: 'read' ;
WRITE	: 'write' ;
TRUE    : 'true' ;
FALSE   : 'false';
KEY     : 'key';
LINEAR: 'Linear';
CUBIC: 'Cubic';
ID  	:	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
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


