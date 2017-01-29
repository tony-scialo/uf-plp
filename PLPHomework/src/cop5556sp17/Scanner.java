package cop5556sp17;

import java.util.ArrayList;

public class Scanner {
	

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	
	/**
	 * Kind enum
	 */
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	public static enum State{
		START("start"), IN_DIGIT("in_digit"), IN_IDENT("in_ident"), OPERATOR("operator");
		
		State(String stateText){
			this.stateText = stateText;
		}
		
		final String stateText;
		
		public String getStateText(){
			return stateText;
		}
	}
	
	
	/**
	 * Constructor, takes in a String and scans it
	 * 
	 * @param chars
	 */
	public Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
	}
		
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException { 
		int length = chars.length(); //TODO do i need to see if chars is null?
		ScannerSwitchHelper switchHelper = new ScannerSwitchHelper(0, State.START);
		
		while(switchHelper.getCurrentPos() <= length){
			switch(switchHelper.getCurrentState()){
			case START:{
				caseStart(switchHelper, length);
				break;
				}
			case IN_DIGIT:{
				caseInDigit(switchHelper);
				break;
				}
			case IN_IDENT:{
				caseInIdent(switchHelper);
				break;
				}
			case OPERATOR:{
				
				}
			}
		}	
		return this;  
	}
	
	/**
	 * The starting case for the scanner
	 * 
	 * @param currentPos
	 */
	private void caseStart(ScannerSwitchHelper switchHelper, int length){
		switchHelper.setCurrentPos(skipWhiteSpace(switchHelper.getCurrentPos()));
		int currentChar = getCurrentChar(switchHelper.getCurrentPos(), length);
		
		if(currentChar == -1){//are we at the end of the line
			addNewToken(Kind.EOF, switchHelper.getCurrentPos(), 0, State.START, switchHelper, true);
		}
		else if (Character.isDigit(currentChar)) //char is a digit
			switchHelper.setCurrentState(State.IN_DIGIT);
		else if(Character.isJavaIdentifierStart(currentChar)){
			 switchHelper.setCurrentState(State.IN_IDENT);
		}else{
			//TODO
		}
	}
	
	/** 
	 * Case for when in a digit
	 * 
	 * @param switchHelper
	 */
	private void caseInDigit(ScannerSwitchHelper switchHelper) throws IllegalNumberException{
		
		int currentChar = chars.charAt(switchHelper.getCurrentPos());
		switchHelper.setStartPos(switchHelper.getCurrentPos());
		
		if(currentChar == '0'){ //if 0, create new token and got to Start
			addNewToken(Kind.INT_LIT, switchHelper.getCurrentPos(), 1, State.START, switchHelper, true);
		}else{
			
			while(switchHelper.getCurrentPos() < chars.length() && Character.isDigit(chars.charAt(switchHelper.getCurrentPos()))){
				switchHelper.incrememntCurrentPos();
			}
			
			String digit = chars.substring(switchHelper.getStartPos(), switchHelper.getCurrentPos()); //save in string for error checking
			
			try{
				Integer.parseInt(digit); //parse to verify it is an int
				addNewToken(Kind.INT_LIT, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
						State.START, switchHelper, false);
			}catch(NumberFormatException e){
				throw new IllegalNumberException("Digit is to large for int. Digit = " + digit);
			}
			
		}
	}
	
	/**
	 * Case for idents (checks for keywords also
	 * 
	 * @param switchHelper
	 */
	private void caseInIdent(ScannerSwitchHelper switchHelper){
		switchHelper.setStartPos(switchHelper.getCurrentPos());
		
		while(notAtEndOfString(switchHelper.getCurrentPos()) && 
				Character.isJavaIdentifierPart(chars.charAt(switchHelper.getCurrentPos()))){
			switchHelper.incrememntCurrentPos();
		}
		
		addNewToken(Kind.IDENT, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
						State.START, switchHelper, false);
		
		//TODO check to make sure its no a reserved word
	}
	
	/**
	 * Returns true if a separator, false if not
	 * 
	 * @param currentChar
	 * @return
	 */
	private boolean isCharSeparator(int currentChar){
		
		boolean isSeparator;
		
		switch(currentChar){
			case ';': 
				isSeparator = true; 
				break;
			case ',':
				isSeparator = true; 
				break;
			case '(':
				isSeparator = true; 
				break;
			case ')':
				isSeparator = true; 
				break;
			case '{':
				isSeparator = true; 
				break;
			case '}':
				isSeparator = true; 
				break;
			default:
				isSeparator = false;
				break;
		}
		
		return isSeparator;
	}
	
	/**
	 * Returns the char (as an int) at the current position, or -1 (for EOF)
	 * 
	 * @param currentPos
	 * @param length
	 * @return
	 */
	private int getCurrentChar(int currentPos, int length){
		return currentPos < length ?chars.charAt(currentPos) : -1;
	}
	
	/** skips white space at the start of a token
	 * @param currentPos
	 * @return
	 */
	private int skipWhiteSpace(int currentPos){
		while(currentPos < chars.length() && Character.isWhitespace(chars.charAt(currentPos)))
			currentPos++;
		return currentPos;
	}
	
	/**
	 * Adds a token to the token list, incremements the current position in the string, and goes to the specified state
	 * 
	 * @param kind
	 * @param start
	 * @param length
	 * @param nextState
	 * @param switchHelper
	 */
	private void addNewToken(Kind kind, int start, int length, State nextState, ScannerSwitchHelper switchHelper, 
			boolean increment){
		tokens.add(new Token(kind, start, length));
		if(increment)
			switchHelper.incrememntCurrentPos();
		switchHelper.setCurrentState(nextState);
	}
	
	private boolean notAtEndOfString(int currentPos){
		return currentPos < chars.length();
	}
	
	
	
	
	
	
	
	
	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	
	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		
	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS
			return null;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT THIS
			return null;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			return 0;
		}
		
		public String toString(){
			return "kind: " + kind.getText() + " pos: " + pos + " length:" + length;
		}
		
	}
	
	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum+1);		
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return null;
	}
	
	/**
	 * Created this class to help modularize scan() better (needed to return 2 values)
	 * 
	 * @author tony
	 *
	 */
	public class ScannerSwitchHelper{
		
		private int currentPos;
		private State currentState;
		private int startPos;
		
		public ScannerSwitchHelper(){
			
		}
		
		public void incrememntCurrentPos(){
			currentPos++;
		}
		
		public ScannerSwitchHelper(int currentPos, State currentState){
			this.currentPos = currentPos;
			this.currentState = currentState;
		}
		
		public int getCurrentPos() {
			return currentPos;
		}

		public void setCurrentPos(int currentPos) {
			this.currentPos = currentPos;
		}

		public State getCurrentState() {
			return currentState;
		}

		public void setCurrentState(State currentState) {
			this.currentState = currentState;
		}

		public int getStartPos() {
			return startPos;
		}

		public void setStartPos(int startPos) {
			this.startPos = startPos;
		}
	}

}
