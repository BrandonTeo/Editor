package editor;

import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.ArrayList;

public class FLL {

	private class Node {
		private Text textobject;
		private Node prev;
		private Node next;

		public Node(char c, Node p, Node n) {
			String s = "";
			s += c;
			this.textobject = new Text(0, 0, s);
			this.prev = p;
			this.next = n;
		}

		public char getFirstChar() {
			return textobject.getText().charAt(0);
		}
		
	}

	private Node frontsentinel;
	private Node backsentinel;
	private Node currentNode;
	private int currentPos;
	private double lineWidth;
	private ArrayList<Node> lineFTracker;
	private ArrayList<Node> lineBTracker;
	private int currentNodeLine;
	private int lastLine;
	private double charHeight;


	public FLL(double w) {
		frontsentinel = new Node('a', null, null);
		backsentinel = new Node('a', null, null);
		frontsentinel.next = backsentinel;
		backsentinel.prev = frontsentinel;
		currentNode = frontsentinel;
		currentPos = 0;
		lineFTracker = new ArrayList<Node>();
		lineBTracker = new ArrayList<Node>();
		lineWidth = w;
	}

	public void updateLineWidth(double w) {
		lineWidth = w;
	}

	public boolean eqNewline(char c) {
		if (c == '\r' || c == '\n') {
			return true;
		} else {
			return false;
		}
	}

	public void addChar(char c, Font f, VPos v) {
		Node nodeToAdd = new Node(c, currentNode, null);
		nodeToAdd.textobject.setTextOrigin(v);
		nodeToAdd.textobject.setFont(f);
		if (eqNewline(c)) {
			charHeight = (nodeToAdd.textobject.getLayoutBounds().getHeight()) / 2;
		} else {
			charHeight = nodeToAdd.textobject.getLayoutBounds().getHeight();
		}
		Node temp = currentNode.next;
		currentNode.next = nodeToAdd;
		currentNode = currentNode.next;
		currentNode.next = temp;
		temp.prev = currentNode;
		currentPos += 1;
	}

	public Text delChar() {
		currentPos -= 1;
		Node temp = currentNode;
		Text deletedText = temp.textobject;
		currentNode = currentNode.prev;
		currentNode.next = currentNode.next.next;
		currentNode.next.prev = currentNode;
		temp.prev = null;
		temp.next = null;
		return deletedText;
	}

	public int currentPos() {
		return this.currentPos;
	}

	public int isCurrFSentinel() {
		if (currentNode == frontsentinel) {
			return 1;
		} else 
			return 0;
	}

	public int isCurrBSentinel() {
		if (currentNode == backsentinel) {
			return 1;
		} else 
			return 0;
	}

	public void setCurrtoFSentinel() {
		currentNode = frontsentinel;
	}

	public void incCurrNode() {
		currentNode = currentNode.next;
	}

	public void allSetFont(Font f) {
		Node temp = frontsentinel.next;
		while (temp != backsentinel) {
			temp.textobject.setFont(f);
			temp = temp.next;
		}
		charHeight = temp.prev.textobject.getLayoutBounds().getHeight();
	}

	public Text getCurrText() {
		return currentNode.textobject;
	}

	public void moveCurrNodeRight() {
		if (currentNode.next != backsentinel) {
			if (eqNewline(currentNode.getFirstChar())) {
				currentNodeLine++;
			}
			currentNode = currentNode.next;
		}
	}

	public void moveCurrNodeLeft() {
		if (currentNode != frontsentinel) {
			currentNode = currentNode.prev;
			if (eqNewline(currentNode.getFirstChar())) {
				currentNodeLine--;
			}
		}
	}

	public int moveCurrNodeUp() {
		if (currentNodeLine != 0) {
			double sumWidth = 0.0;
			Node temp = lineFTracker.get(currentNodeLine);
			while (temp != currentNode) {
				sumWidth += temp.textobject.getLayoutBounds().getWidth();
				temp = temp.next;
			}
			sumWidth += temp.textobject.getLayoutBounds().getWidth();

			temp = lineFTracker.get(currentNodeLine - 1);
			double newSumWidth = 0.0;
			while (temp != lineBTracker.get(currentNodeLine - 1)) {
				newSumWidth += temp.textobject.getLayoutBounds().getWidth();
				temp = temp.next;
			}
			newSumWidth += temp.textobject.getLayoutBounds().getWidth();

			temp = lineFTracker.get(currentNodeLine - 1);
			if (currentNode == lineBTracker.get(currentNodeLine) && eqNewline(currentNode.getFirstChar())) {
				currentNode = lineBTracker.get(currentNodeLine - 1);
				currentNodeLine--;
				return 1;
			} else if (currentNode == lineBTracker.get(currentNodeLine) && currentNode.next == lineFTracker.get(currentNodeLine + 1)) {
				currentNode = lineBTracker.get(currentNodeLine - 1);
				currentNodeLine--;
				return 1;
			} else if (sumWidth >= newSumWidth) {
				if (eqNewline(lineBTracker.get(currentNodeLine - 1).getFirstChar()) && eqNewline(lineBTracker.get(currentNodeLine - 1).prev.getFirstChar())) {
					currentNode = lineBTracker.get(currentNodeLine - 1).prev;
					currentNodeLine -= 2;
					return 1;
				} else if (eqNewline(lineBTracker.get(currentNodeLine - 1).getFirstChar())) {
					currentNode = lineBTracker.get(currentNodeLine - 1).prev;
					currentNodeLine--;
				} else {
					currentNode = lineBTracker.get(currentNodeLine - 1);
					currentNodeLine--;
				}
				return 2;
			} else {
				while (sumWidth - temp.textobject.getLayoutBounds().getWidth() > 0.0) {
					sumWidth -= temp.textobject.getLayoutBounds().getWidth();
					temp = temp.next;
				}
				if (sumWidth > (temp.textobject.getLayoutBounds().getWidth() / 2)) {
					currentNode = temp;
				} else {
					currentNode = temp.prev;
				}
				currentNodeLine--;
				return 2;
			}
		} else if (currentNode == lineBTracker.get(currentNodeLine)) {
			currentNode = frontsentinel;
			return 0;
		} else 
			return 0;
				
	}

	public int moveCurrNodeDown() {
		if (currentNodeLine != lastLine) {
			if (currentNode == frontsentinel) {
				currentNode = lineBTracker.get(currentNodeLine);
				return 1;
			}

			double sumWidth = 0.0;
			Node temp = lineFTracker.get(currentNodeLine);
			while (temp != currentNode) {
				sumWidth += temp.textobject.getLayoutBounds().getWidth();
				temp = temp.next;
			}
			sumWidth += temp.textobject.getLayoutBounds().getWidth();

			temp = lineFTracker.get(currentNodeLine + 1);
			double newSumWidth = 0.0;
			while (temp != lineBTracker.get(currentNodeLine + 1)) {
				newSumWidth += temp.textobject.getLayoutBounds().getWidth();
				temp = temp.next;
			}
			newSumWidth += temp.textobject.getLayoutBounds().getWidth();

			temp = lineFTracker.get(currentNodeLine + 1);
			if (currentNode == lineBTracker.get(currentNodeLine) && eqNewline(currentNode.getFirstChar())) {
				currentNode = lineBTracker.get(currentNodeLine + 1);
				currentNodeLine++;
				return 1;
			} else if (currentNode == lineBTracker.get(currentNodeLine) && currentNode.next == lineFTracker.get(currentNodeLine + 1)) {
				currentNode = lineBTracker.get(currentNodeLine + 1);
				currentNodeLine++;
				return 1;
			} else if (sumWidth >= newSumWidth) {
				if (eqNewline(lineBTracker.get(currentNodeLine).getFirstChar()) && eqNewline(lineBTracker.get(currentNodeLine).next.getFirstChar())) {
					currentNode = lineBTracker.get(currentNodeLine);
					return 1;
				} else if (eqNewline(lineBTracker.get(currentNodeLine).getFirstChar())) {
					if (eqNewline(lineBTracker.get(currentNodeLine + 1).getFirstChar())) {
						currentNode = lineBTracker.get(currentNodeLine + 1).prev;
					} else {
						currentNode = lineBTracker.get(currentNodeLine + 1);
					}
					currentNodeLine++;
				} else {
					if (eqNewline(lineBTracker.get(currentNodeLine + 1).getFirstChar())) {
						currentNode = lineBTracker.get(currentNodeLine + 1).prev;
					} else {
						currentNode = lineBTracker.get(currentNodeLine + 1);
					}
					currentNodeLine++;
				}
				return 2;
			} else {
				while (sumWidth - temp.textobject.getLayoutBounds().getWidth() > 0.0) {
					sumWidth -= temp.textobject.getLayoutBounds().getWidth();
					temp = temp.next;
				}
				if (sumWidth > (temp.textobject.getLayoutBounds().getWidth() / 2)) {
					currentNode = temp;
				} else {
					currentNode = temp.prev;
				}
				currentNodeLine++;
				return 2;
			}
		} else
			return 0;
	}

	public int mouseClickSetCurrNode(double mousePressedX, double mousePressedY) {
		int corrLine = (int) Math.floor(mousePressedY / charHeight);
		double widthOfLine = mousePressedX;
		if (corrLine >= lastLine) {
			if (eqNewline(backsentinel.prev.getFirstChar())) {
				currentNode = backsentinel.prev;
				currentNodeLine = lastLine - 1;
				return 1;
			} else {
				corrLine = lastLine;
				Node temp = lineFTracker.get(corrLine);

				while ((widthOfLine - temp.textobject.getLayoutBounds().getWidth() - 5.0 > 0.0) && temp != backsentinel && temp != lineBTracker.get(corrLine)) {
					widthOfLine -= temp.textobject.getLayoutBounds().getWidth();
					temp = temp.next;
				}
				if (widthOfLine - temp.textobject.getLayoutBounds().getWidth() - 5.0 <= 0.0) {
					if (widthOfLine - 5.0 > (temp.textobject.getLayoutBounds().getWidth() / 2)) {
						currentNode = temp;
					} else {
						currentNode = temp.prev;
					}
					currentNodeLine = corrLine;
				} else if (temp == backsentinel) {
					currentNode = temp.prev;
					currentNodeLine = corrLine;
				} else if (temp == lineBTracker.get(corrLine)) {
					currentNode = temp;
					currentNodeLine = corrLine;
				}
				return 2;
			}
		} else {
			Node temp = lineFTracker.get(corrLine);

			while ((widthOfLine - temp.textobject.getLayoutBounds().getWidth() - 5.0 > 0.0) && temp != backsentinel && temp != lineBTracker.get(corrLine)) {
				widthOfLine -= temp.textobject.getLayoutBounds().getWidth();
				temp = temp.next;
			}
			if (widthOfLine - temp.textobject.getLayoutBounds().getWidth() - 5.0 <= 0.0) {
				if (widthOfLine - 5.0 > (temp.textobject.getLayoutBounds().getWidth() / 2)) {
					currentNode = temp;
				} else {
					currentNode = temp.prev;
				}
				currentNodeLine = corrLine;
			} else if (temp == backsentinel) {
				currentNode = temp.prev;
				currentNodeLine = corrLine;
			} else if (temp == lineBTracker.get(corrLine)) {
				if (eqNewline(temp.getFirstChar())) {
					if (eqNewline(lineFTracker.get(corrLine).getFirstChar())) {
						currentNode = temp.prev;
						return 1;
					} else {
						currentNode = temp.prev;
					}
				} else { 
					currentNode = temp;
				}
				currentNodeLine = corrLine;
			}
			return 2;
		}
			
	}


	public void setTextXY(double startingX, double startingY) {
		
		Node temp = frontsentinel.next;
		Node wrapper = null;
		double textTop = startingY;
        double textLeft = startingX;
        double prevTextHeight = 0.0;
        double prevTextWidth = 0.0;
        int count = 0;

        if (temp != backsentinel) {
        	lineFTracker.add(count, temp);
        }

		while (temp != backsentinel) {

			if (temp.getFirstChar() == ' ') {
				wrapper = temp;
			}

			textLeft = textLeft + prevTextWidth;
			char checkNewline = temp.getFirstChar();

				if (eqNewline(checkNewline)) {
					if (eqNewline(temp.prev.getFirstChar())) {
						lineFTracker.add(count, temp);
						lineBTracker.add(count, temp);
						count++;

						temp.textobject.setX(Math.round(textLeft));
						temp.textobject.setY(Math.round(textTop));

						textLeft = startingX;
        				textTop += (temp.textobject.getLayoutBounds().getHeight() / 2);
        				prevTextWidth = 0.0;

					} else {
						lineBTracker.add(count, temp);
						count++;

						temp.textobject.setX(Math.round(textLeft));
						temp.textobject.setY(Math.round(textTop));

						textLeft = startingX;
        				textTop += (temp.textobject.getLayoutBounds().getHeight() / 2);
        				prevTextWidth = 0.0;
					}

				} else if ((textLeft + temp.textobject.getLayoutBounds().getWidth()) > (lineWidth - 5.0)) {
					if (wrapper != null) {
						lineBTracker.add(count, wrapper);
						count++;
						textTop += temp.textobject.getLayoutBounds().getHeight();
						Node start = wrapper.next;
						lineFTracker.add(count, start);
						double textX = startingX;
						double prevWidth = 0.0;
						while (start != temp.next) {
							textX += prevWidth;
							start.textobject.setX(textX);
							start.textobject.setY(textTop);
				
							prevWidth = start.textobject.getLayoutBounds().getWidth();
							start = start.next;
						}
						textLeft = textX;
						prevTextWidth = prevWidth;
						wrapper = null;

					} else {
						lineBTracker.add(count, temp.prev);
						count++;
						lineFTracker.add(count, temp);
			
						textLeft = startingX;
						textTop += temp.textobject.getLayoutBounds().getHeight();
	
						temp.textobject.setX(Math.round(textLeft));
        				temp.textobject.setY(Math.round(textTop));
	
        				prevTextWidth = temp.textobject.getLayoutBounds().getWidth();
        			}
				} else {
					if (eqNewline(temp.prev.getFirstChar())) {
						lineFTracker.add(count, temp);
					}
					temp.textobject.setX(Math.round(textLeft));
        			temp.textobject.setY(Math.round(textTop));

        			prevTextWidth = temp.textobject.getLayoutBounds().getWidth();
				}
        		if (temp == currentNode) {
        			if (eqNewline(temp.getFirstChar())) {
        				currentNodeLine = count - 1;
        			} else {
						currentNodeLine = count;
        			}
				}
				temp = temp.next;
				if (temp == backsentinel) {
					lastLine = count;
					lineBTracker.add(count, temp.prev);
				}

		}
	}


}

