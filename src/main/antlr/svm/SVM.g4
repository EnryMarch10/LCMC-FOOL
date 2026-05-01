grammar SVM; // Stack Virtual Machine

@header {
package svm;

import java.util.*;
}   // ADDED FOR GRADLE

//@parser::header { PREV
//import java.util.*;
//}

@lexer::members {
public int lexicalErrors = 0;
}

@parser::members {
public int[] code = new int[ExecuteVM.CODESIZE];
private int i = 0;
private Map<String,Integer> labelDef = new HashMap<>();
private Map<Integer,String> labelRef = new HashMap<>();
}

/*------------------------------------------------------------------*
 * PARSER RULES, with EBNF (Extended Backus-Naur Form) grammars
 *------------------------------------------------------------------*/

assembly: instruction* EOF {for (Integer j: labelRef.keySet()) code[j]=labelDef.get(labelRef.get(j));};

instruction:  PUSH n=INTEGER {code[i++] = PUSH; code[i++] = Integer.parseInt($n.text);}
            | PUSH l=LABEL {code[i++] = PUSH; labelRef.put(i++,$l.text);}
            | POP {code[i++] = POP;}
            | ADD {code[i++] = ADD;}
            | SUB {code[i++] = SUB;}
            | MULT {code[i++] = MULT;}
            | DIV {code[i++] = DIV;}
            | STOREW {code[i++] = STOREW;}
            | LOADW {code[i++] = LOADW;}
            | l=LABEL COL {labelDef.put($l.text,i);}
            | BRANCH l=LABEL {code[i++] = BRANCH; labelRef.put(i++,$l.text);}
            | BRANCHEQ l=LABEL {code[i++] = BRANCHEQ; labelRef.put(i++,$l.text);}
            | BRANCHLESSEQ l=LABEL {code[i++] = BRANCHLESSEQ; labelRef.put(i++,$l.text);}
            | JS {code[i++] = JS;}
            | LOADRA {code[i++] = LOADRA;}
            | STORERA {code[i++] = STORERA;}
            | LOADTM {code[i++] = LOADTM;}
            | STORETM {code[i++] = STORETM;}
            | LOADFP {code[i++] = LOADFP;}
            | STOREFP {code[i++] = STOREFP;}
            | COPYFP {code[i++] = COPYFP;}
            | LOADHP {code[i++] = LOADHP;}
            | STOREHP {code[i++] = STOREHP;}
            | PRINT {code[i++] = PRINT;}
            | HALT {code[i++] = HALT;}
            ;

/*------------------------------------------------------------------*
 * LEXER RULES
 *------------------------------------------------------------------*/

// Every value (instruction in code section or value/address in stack/heap) is an integer (32 bits), we call it WORD
// REGISTERS:
// - $ip -> istruction pointer (same as $pc, program counter)
// - $sp -> stack pointer
// - $hp -> heap pointer
// - $fp -> frame pointer (control link, also used in chain as access link, reference position in ACTIVATION RECORDs, also called FRAMEs)
// - $ra -> return address (to restore $ip linked to JS instruction)
// - $tm -> temporary storage
PUSH         : 'push';  // puts next WORD from code on top of the stack
POP          : 'pop';   // gets a WORD from top of the stack
ADD          : 'add';   // POP two WORDs, add them and PUSH the result
SUB          : 'sub';   // POP two WORDs, subtract them and PUSH the result
MULT         : 'mult';  // POP two WORDs, multiply them and PUSH the result
DIV          : 'div';   // POP two WORDs, divide them and PUSH the result
STOREW       : 'sw';    // POP a WORD as address, writes at that address popped another WORD taken with POP
LOADW        : 'lw';    // POP a WORD as address, reads at that address popped and PUSH the read WORD
BRANCH       : 'b';     // jumps to address in next WORD from code
BRANCHEQ     : 'beq';   // takes next WORD from code, POP two WORDs, if popped WORDs equal does BRANCH
BRANCHLESSEQ : 'bleq';  // takes next WORD from code, POP two WORDs, if last popped equal or less first popped does BRANCH
JS           : 'js';    // POP a WORD as address, sets $ra to $ip and then $ip to address popped (jump to subroutine)
LOADRA       : 'lra';   // PUSH $ra
STORERA      : 'sra';   // POP a WORD into $ra
LOADTM       : 'ltm';   // PUSH $tm
STORETM      : 'stm';   // POP a WORD into $tm
LOADFP       : 'lfp';   // PUSH $fp
STOREFP      : 'sfp';   // POP a WORD into $fp
COPYFP       : 'cfp';   // sets $fp to $sp
LOADHP       : 'lhp';   // PUSH $hp
STOREHP      : 'shp';   // POP a WORD into $hp
PRINT        : 'print'; // prints top of the stack
HALT         : 'halt';  // ends the execution

COL: ':';
LABEL: ('a'..'z' | 'A'..'Z')('a'..'z' | 'A'..'Z' | '0'..'9')*;
INTEGER: '0' | ('-')?(('1'..'9')('0'..'9')*);

WHITESP: ('\t' | ' ' | '\r' | '\n')+ -> channel(HIDDEN);
COMMENT_MULTI: ('/*' .*? '*/') -> channel(HIDDEN);
COMMENT_SINGLE: ('//' .*? '\n') -> channel(HIDDEN);

ERR: . { System.out.println("Invalid char " + getText() + " at line " + getLine()); lexicalErrors++; } -> channel(HIDDEN);
