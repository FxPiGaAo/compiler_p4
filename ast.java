import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Wumbo program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Children
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, or
// internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  RepeatStmtNode,
//        CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    public void nameanalyse(PrintWriter p, SymTable a){};

    // this method can be used by the unparse methods to do indenting
    protected void addIndentation(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++) p.print(" ");
    }
    public static Boolean printtype = false;
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myDeclList.nameanalyse(p, a);
    };
    private DeclListNode myDeclList; 
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException" +
            " in DeclListNode.print");
            System.exit(-1);
        }
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).nameanalyse(p, a);;
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException" +
            " in DeclListNode.print");
            System.exit(-1);
        }
    };

    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    public void nameanalyse(PrintWriter p, SymTable a) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        while (it.hasNext()) { // if there is at least one element
            it.next().nameanalyse(p, a);
        }
    }

    public List<String> getstrings(){
        mystrings = new LinkedList<>();
        Iterator<FormalDeclNode> it = myFormals.iterator();
        while (it.hasNext()) { // if there is at least one element
            mystrings.add(it.next().gettype());
        }
        return mystrings;
    }
    private List<String> mystrings;
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public void nameanalyse(PrintWriter p, SymTable a) {
        myDeclList.nameanalyse(p, a);
        myStmtList.nameanalyse(p, a);
    }

    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    public void nameanalyse(PrintWriter p, SymTable a) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().nameanalyse(p, a);
        }
    }

    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    public void nameanalyse(PrintWriter p, SymTable a) {
        Iterator<ExpNode> it = myExps.iterator();
        while (it.hasNext()) {
            it.next().nameanalyse(p, a);
        }
    }

    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(";");
    }

    public void nameanalyse(PrintWriter p, SymTable a) {
        if(myType.myStrVal=="void"){
            ErrMsg.fatal(myId.getline(),myId.getchar(),
            "Non-function declared void");
            try {
                if(a.lookupLocal(myId.getname())!=null){
                    ErrMsg.fatal(myId.getline(),myId.getchar(),
                    "Multiply declared identifier");
                }
            } catch (EmptySymTableException ex) {
                System.err.println("unexpected EmptySymTableException!");
                System.exit(-1);
            }
            return;
        }
        if(mySize==NOT_STRUCT){//analyse normal nodes
            try {
                //check whether there is same variable name as struct type
                if(a.lookupLocal("struct "+myId.getname())!=null){
                    ErrMsg.fatal(myId.getline(),myId.getchar(),
                    "Multiply declared identifier");
                    return;
                }
                a.addDecl(myId.getname(), new Sym(myType.myStrVal));
            } catch (IllegalArgumentException ex) {
                System.err.println("unexpected IllegalArgumentException!");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("unexpected EmptySymTableException!");
                System.exit(-1);
            } catch (DuplicateSymException ex) {
                ErrMsg.fatal(myId.getline(),myId.getchar(),
                "Multiply declared identifier");
            }
        }else{//analyse struct nodes
            try {
                if(a.lookupGlobal(((StructNode)myType).getid())==null ||
                a.lookupGlobal(((StructNode)myType).getid())
                .getType()!="struct"){
                    ErrMsg.fatal(((StructNode)myType).getline(),
                    ((StructNode)myType).getchar(),"Invalid name of struct type");
                }else{
                    a.addDecl(myId.getname(),
                    new Sym(((StructNode)myType).getid()));
                }
            } catch (IllegalArgumentException ex) {
                System.err.println("unexpected IllegalArgumentException!");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("unexpected EmptySymTableException!");
                System.exit(-1);
            } catch (DuplicateSymException ex) {
                ErrMsg.fatal(myId.getline(),myId.getchar(),
                "Multiply declared identifier");
            }
        }
    }

    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    public void nameanalyse(PrintWriter p, SymTable a) {
        try {
            if(a.lookupLocal(myId.getname())!=null || a.lookupLocal("struct " + myId.getname())!=null){
                ErrMsg.fatal(myId.getline(),myId.getchar(),
                "Multiply declared identifier");
            }else{
                a.addDecl(myId.getname(),
                new Sym(myFormalsList.getstrings(),myType.myStrVal));
            }
            a.addScope();
            myFormalsList.nameanalyse(p, a);
            myBody.nameanalyse(p, a);
            a.removeScope();
        } catch (IllegalArgumentException ex) {
            System.err.println("unexpected IllegalArgumentException!");
            System.exit(-1);
        } catch (EmptySymTableException ex) {
            System.err.println("unexpected EmptySymTableException!");
            System.exit(-1);
        } catch (DuplicateSymException ex) {
            ErrMsg.fatal(myId.getline(),myId.getchar(),
            "Multiply declared identifier");
        }
    }

    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    public void nameanalyse(PrintWriter p, SymTable a) {
        if(myType.myStrVal=="void"){
            ErrMsg.fatal(myId.getline(),myId.getchar(),
            "Non-function declared void");
            try {
                if(a.lookupLocal(myId.getname())!=null){
                    ErrMsg.fatal(myId.getline(),myId.getchar(),
                    "Multiply declared identifier");
                }
            } catch (EmptySymTableException ex) {
                System.err.println("unexpected EmptySymTableException!");
                System.exit(-1);
            }
            return;
        }
        try {
            a.addDecl(myId.getname(), new Sym(myType.myStrVal));
        } catch (IllegalArgumentException ex) {
            System.err.println("unexpected IllegalArgumentException!");
            System.exit(-1);
        } catch (EmptySymTableException ex) {
            System.err.println("unexpected EmptySymTableException!");
            System.exit(-1);
        } catch (DuplicateSymException ex) {
            ErrMsg.fatal(myId.getline(),myId.getchar(),
            "Multiply declared identifier");
        }
    }

    public String gettype(){
        return myType.myStrVal;
    }

    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("struct ");
        myId.unparse(p, 0);
        p.println("{");
        myDeclList.unparse(p, indent+4);
        addIndentation(p, indent);
        p.println("};\n");
    }

    public void nameanalyse(PrintWriter p, SymTable a) {
        try {
            if(a.lookupLocal(myId.getname())==null){
                a.addScope();
                myDeclList.nameanalyse(p, a);
                Sym temp = new Sym(a.getheadlist());
                a.removeScope();
                a.addDecl("struct " + myId.getname(), temp);                
            }else{
                ErrMsg.fatal(myId.getline(),myId.getchar(),
                "Multiply declared identifier");
            };
        } catch (IllegalArgumentException ex) {
            System.err.println("unexpected IllegalArgumentException!");
            System.exit(-1);
        } catch (EmptySymTableException ex) {
            System.err.println("unexpected EmptySymTableException!");
            System.exit(-1);
        } catch (DuplicateSymException ex) {
            ErrMsg.fatal(myId.getline(),myId.getchar(),
            "Multiply declared identifier");
        }
    }

    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    public String myStrVal;
}

class IntNode extends TypeNode {
    public IntNode() {
        myStrVal = "int";
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
        myStrVal = "bool";
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
        myStrVal = "void";
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
        myStrVal = "struct";
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        myId.unparse(p, 0);
    }
    public String getid(){
        return "struct "+myId.getname();
    }
    public int getline(){
        return myId.getline();
    }
    public int getchar(){
        return myId.getchar();
    }
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        printtype = true;
        addIndentation(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
        printtype = false;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myAssign.nameanalyse(p, a);
    }

    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printtype = true;
        addIndentation(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
        printtype =false;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myExp.nameanalyse(p, a);
    }

    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printtype = true;
        addIndentation(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
        printtype = false;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myExp.nameanalyse(p, a);
    }

    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public void unparse(PrintWriter p, int indent) {
        printtype = true;
        addIndentation(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
        printtype = false;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myExp.nameanalyse(p, a);
    }

    // 1 child (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printtype = true;
        addIndentation(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
        printtype = false;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myExp.nameanalyse(p, a);
    }

    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("if (");
        printtype = true;
        myExp.unparse(p, 0);
        printtype = false;
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndentation(p, indent);
        p.println("}");
    }

    public void nameanalyse(PrintWriter p, SymTable a){        
        myExp.nameanalyse(p, a);        
        a.addScope();
        myDeclList.nameanalyse(p, a);
        myStmtList.nameanalyse(p, a);
        try{
            a.removeScope();
        }catch(EmptySymTableException e){
            System.err.println("unexpected EmptySymTableException");
            System.exit(-1);
        } 
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("if (");
        printtype = true;
        myExp.unparse(p, 0);
        printtype = false;
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        addIndentation(p, indent);
        p.println("}");
        addIndentation(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        addIndentation(p, indent);
        p.println("}");
    }

    public void nameanalyse(PrintWriter p, SymTable a){        
        myExp.nameanalyse(p, a);        
        a.addScope();
        myThenDeclList.nameanalyse(p, a);
        myThenStmtList.nameanalyse(p, a);
        try{
            a.removeScope();
        }catch(EmptySymTableException e){
            System.err.println("unexpected EmptySymTableException");
            System.exit(-1);
        }
        a.addScope();
        myElseDeclList.nameanalyse(p, a);
        myElseStmtList.nameanalyse(p, a);
        try{
            a.removeScope();
        }catch(EmptySymTableException e){
            System.err.println("unexpected EmptySymTableException");
            System.exit(-1);
        } 
    }

    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("while (");
        printtype = true;
        myExp.unparse(p, 0);
        printtype = false;
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndentation(p, indent);
        p.println("}");
    }

    public void nameanalyse(PrintWriter p, SymTable a){        
        myExp.nameanalyse(p, a);        
        a.addScope();
        myDeclList.nameanalyse(p, a);
        myStmtList.nameanalyse(p, a);
        try{
            a.removeScope();
        }catch(EmptySymTableException e){
            System.err.println("unexpected EmptySymTableException");
            System.exit(-1);
        }
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
	addIndentation(p, indent);
        p.print("repeat (");
        printtype = true;
        myExp.unparse(p, 0);
        printtype = false;
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndentation(p, indent);
        p.println("}");
    }

    public void nameanalyse(PrintWriter p, SymTable a){        
        myExp.nameanalyse(p, a);        
        a.addScope();
        myDeclList.nameanalyse(p, a);
        myStmtList.nameanalyse(p, a);
        try{
            a.removeScope();
        }catch(EmptySymTableException e){
            System.err.println("unexpected EmptySymTableException");
            System.exit(-1);
        }
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myCall.nameanalyse(p, a);        
    }

    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printtype = true;
        addIndentation(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
        printtype = false;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        if (myExp != null){
            myExp.nameanalyse(p, a);
        }
        
    }

    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    public boolean isidnode(){
        return false;
    }
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    public int getline(){
        return myLineNum;
    }
    
    public int getchar(){
        return myCharNum;
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    public int getline(){
        return myLineNum;
    }
    
    public int getchar(){
        return myCharNum;
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    public int getline(){
        return myLineNum;
    }
    
    public int getchar(){
        return myCharNum;
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    public int getline(){
        return myLineNum;
    }
    
    public int getchar(){
        return myCharNum;
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if(printtype == true){
            if(mySym==null){return;}
            if(mySym.getType().length()>6 && mySym.getType().substring(0,7).equals("struct ")){
                p.print("("+ mySym.getType().substring(7) +")");
            }else{
                p.print("("+ mySym +")");
            }            
        }
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        try{
            if(a.lookupGlobal(myStrVal)==null){
                ErrMsg.fatal(myLineNum,myCharNum,"Undeclared identifier");
            }else{
                mySym = a.lookupGlobal(myStrVal);
            }
        }catch(EmptySymTableException e){
            System.err.println("unexpected EmptySymTableException");
            System.exit(-1);
        }
    }

    public String getname(){
        return myStrVal;
    }

    public int getline(){
        return myLineNum;
    }
    
    public int getchar(){
        return myCharNum;
    }

    public Sym getsym(){
        return mySym;
    }

    public void setsym(Sym a){
        mySym = a;
    }

    public boolean isidnode(){
        return true;
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym = null;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);        
        p.print(").");
        myId.unparse(p, 0);
        if(structtype!=null){
            p.print("(" + structtype + ")");
        }
    }
    static public boolean success = true;
    public Sym loc_analyse(PrintWriter p, SymTable a){
        try {
            if(myLoc.isidnode()==false){
                Sym temp = ((DotAccessExpNode)myLoc).loc_analyse(p, a);//table
                if(success == false){
                    return null;
                }else{ 
                    if(temp.getTable().containsKey(myId.getname())==false){                        
                        ErrMsg.fatal(myId.getline(),myId.getchar(),
                        "Invalid struct field name");
                        success = false;
                        return null;
                    }else{
                        Sym new_temp = temp.getTable().get(myId.getname());//struct **
                        if(a.lookupGlobal(new_temp.getType())==null){
                            structtype = temp.getTable().get(myId.getname()).
                            getType();
                            ErrMsg.fatal(myId.getline(),myId.getchar(),
                            "Dot-access of non-struct type1");
                            success = false;
                            return null;
                        }else{
                            structtype = temp.getTable().get(myId.getname()).
                            getType().substring(7);
                            success = true;
                            return a.lookupGlobal(new_temp.getType());
                        }
                    }
                }
            }else{
                if(a.lookupGlobal(((IdNode)myLoc).getname())==null){
                    ErrMsg.fatal(((IdNode)myLoc).getline(),
                    ((IdNode)myLoc).getchar(),"Undeclared identifier");
                    success = false;
                    return null;
                }else{
                    Sym temp = a.lookupGlobal(((IdNode)myLoc).getname());//struct ***                    
                    if(a.lookupGlobal(temp.getType())==null){
                        ((IdNode)myLoc).setsym(temp);
                        ErrMsg.fatal(((IdNode)myLoc).getline(),
                        ((IdNode)myLoc).getchar(),
                        "Dot-access of non-struct type");
                        success = false;
                        return null;
                    }else{
                        ((IdNode)myLoc).setsym(new Sym(temp.getType().substring(7)));                        
                        //start dealing with myid
                        Sym temptable = a.lookupGlobal(temp.getType());
                        if(temptable.getTable().containsKey(myId.getname())==false){                        
                            ErrMsg.fatal(myId.getline(),myId.getchar(),
                            "Invalid struct field name");
                            success = false;
                            return null;
                        }else{
                            Sym new_temp = temptable.getTable().get(myId.getname());
                            //struct *** for mynode
                            if(a.lookupGlobal(new_temp.getType())==null){
                                ErrMsg.fatal(myId.getline(),myId.getchar(),
                                "Dot-access of non-struct type");
                                success = false;
                                structtype = new_temp.getType();
                                return null;
                            }else{
                                success = true;
                                structtype = new_temp.getType().substring(7);
                                return a.lookupGlobal(new_temp.getType());
                            }
                        }
                    }
                }
            }
        } catch (EmptySymTableException ex) {
            System.err.println("unexpected EmptySymTableException!");
            System.exit(-1);
        }
        p.println("shouldn't get here!");
        return null;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        try{
            success = true;
            if(myLoc.isidnode()==false){
                Sym temp = ((DotAccessExpNode)myLoc).loc_analyse(p, a);//table                
                if(success == true){                     
                    if(temp.getTable().containsKey(myId.getname())==false){                        
                        ErrMsg.fatal(myId.getline(),myId.getchar(),
                        "Invalid struct field name");
                        return;
                    }else{
                        Sym temp_struct = temp.getTable().get(myId.getname());
                        Sym new_temp = a.lookupGlobal(temp_struct.getType());//table
                        if(new_temp==null){
                            structtype = temp_struct.getType();
                            return;
                        }else{
                            structtype = temp_struct.getType().substring(7);
                        }
                    }
                }
            }else{
                if(a.lookupGlobal(((IdNode)myLoc).getname())==null){
                    ErrMsg.fatal(((IdNode)myLoc).getline(),
                    ((IdNode)myLoc).getchar(),"Undeclared identifier");
                    return;
                }else{
                    //struct *** for myloc
                    Sym temp = a.lookupGlobal(((IdNode)myLoc).getname());
                                        
                    if(a.lookupGlobal(temp.getType())==null){
                        ((IdNode)myLoc).setsym(temp);
                        ErrMsg.fatal(((IdNode)myLoc).getline(),
                        ((IdNode)myLoc).getchar(),
                        "Dot-access of non-struct type");
                        return;
                    }else{
                        ((IdNode)myLoc).setsym(new Sym(temp.getType().substring(7)));
                        success = true;
                        Sym new_temp = a.lookupGlobal(temp.getType());//table for myloc
                        if(new_temp.getTable().containsKey(myId.getname())==false){                        
                            ErrMsg.fatal(myId.getline(),myId.getchar(),
                            "Invalid struct field name");
                            return;
                        }else{
                            //struct *** for myid
                            Sym newnew_temp = new_temp.getTable().get(myId.getname());
                            if(newnew_temp==null){
                                ErrMsg.fatal(myId.getline(),myId.getchar(),
                                "Undeclared identifier");
                                return;
                            }else if(a.lookupGlobal(newnew_temp.getType())==null){
                                //myid not a struct, but it's okay
                                //since it's the last one
                                structtype = newnew_temp.getType();
                                return;
                            }else{
                                structtype = newnew_temp.getType().substring(7);
                                return;
                            }
                        }
                    }
                }
            }            
        } catch (EmptySymTableException ex) {
            System.err.println("unexpected EmptySymTableException!");
            System.exit(-1);
        }
    }

    private String structtype=null;
    private ExpNode myLoc;
    private IdNode myId;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printtype = true;
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");
        printtype = false;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myLhs.nameanalyse(p, a);
        myExp.nameanalyse(p, a);
    }

    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    public void unparse(PrintWriter p, int indent) {
        printtype = true;
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
        printtype = false;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        try{
            if(a.lookupGlobal(myId.getname())==null || a.lookupGlobal(myId.getname()).getparam()==null){
                ErrMsg.fatal(myId.getline(),myId.getchar(),"Undeclared identifier");
            }else{
                myId.setsym(a.lookupGlobal(myId.getname()));
            }
        }catch(EmptySymTableException e){
            System.err.println("unexpected EmptySymTableException");
            System.exit(-1);
        }
        myExpList.nameanalyse(p, a);
    }

    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myExp.nameanalyse(p, a);
    }

    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public void nameanalyse(PrintWriter p, SymTable a){
        myExp1.nameanalyse(p, a);
        myExp2.nameanalyse(p, a);
    }

    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
