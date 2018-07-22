package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.LinkedList;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Editor extends Application {
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;
    private static final int STARTING_FONT_SIZE = 12;
    private static final double STARTING_TEXT_POSITION_X = 5.0;
    private static final double STARTING_TEXT_POSITION_Y = 0.0;
    private static final String STARTING_FONT_NAME = "Verdana";

    private Group root;
    private final Rectangle cursor;
    private Scene scene;
    private FLL displayText;
    private String fileName;

    public Editor() {
        Text temp = new Text(0, 0, "a");
        temp.setFont(Font.font(STARTING_FONT_NAME, STARTING_FONT_SIZE));
        cursor = new Rectangle(1, temp.getLayoutBounds().getHeight());
        cursor.setX(STARTING_TEXT_POSITION_X);
        cursor.setY(STARTING_TEXT_POSITION_Y);
        displayText = new FLL(WINDOW_WIDTH);

    }

    /** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {

        private int fontSize = STARTING_FONT_SIZE;
        private String fontName = STARTING_FONT_NAME;

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            scene.widthProperty().addListener(new ChangeListener<Number>() {
                @Override public void changed(
                        ObservableValue<? extends Number> observableValue,
                        Number oldScreenWidth,
                        Number newScreenWidth) {
                        displayText.updateLineWidth(newScreenWidth.intValue());
                        displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                        updateCursor();
                }
            });
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.isShortcutDown()) {
                if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {
                    fontSize += 4;
                    displayText.allSetFont(Font.font(fontName, fontSize));
                    displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                    updateCursorFont(fontName, fontSize);
                } else if (keyEvent.getCode() == KeyCode.MINUS) {                        
                    fontSize = Math.max(0, fontSize - 4);
                    displayText.allSetFont(Font.font(fontName, fontSize));
                    displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                    updateCursorFont(fontName, fontSize);
                } else if (keyEvent.getCharacter().charAt(0) == 'p') {
                    System.out.println(Math.round(cursor.getX()) + ", " + Math.round(cursor.getY()));
                } else if (keyEvent.getCharacter().charAt(0) == 's') {
                    writeAndSave();
                } 

            } else {

                if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                    // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                    // the KEY_TYPED event, javafx handles the "Shift" key and associated
                    // capitalization.
                    String characterTyped = keyEvent.getCharacter();
                    if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                        // Ignore control keys, which have non-zero length, as well as the backspace
                        // key, which is represented as a character of value = 8 on Windows.
                        displayText.addChar(characterTyped.charAt(0), Font.font(fontName, fontSize), VPos.TOP);
                        root.getChildren().add(displayText.getCurrText());
                        keyEvent.consume();
                    }
                    displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                    updateCursor();

                } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                    // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                    // events have a code that we can check (KEY_TYPED events don't have an associated
                    // KeyCode).
                    if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
                        if (displayText.isCurrFSentinel() == 0) {
                            Text deletedText = displayText.delChar();
                            root.getChildren().remove(deletedText);
                            displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                            updateCursor();
                        } else {
                            displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                            updateCursor();
                        }
                    } else if (keyEvent.getCode() == KeyCode.LEFT) {
                        displayText.moveCurrNodeLeft();
                        displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                        updateCursorbyLeftRight();
                    } else if (keyEvent.getCode() == KeyCode.RIGHT) {
                        displayText.moveCurrNodeRight();
                        displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                        updateCursorbyLeftRight();
                    } else if (keyEvent.getCode() == KeyCode.UP) {
                        int status = displayText.moveCurrNodeUp();
                        displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                        updateCursorbyUpDown(status);
                    } else if (keyEvent.getCode() == KeyCode.DOWN) {
                        int status = displayText.moveCurrNodeDown();
                        displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                        updateCursorbyUpDown(status);
                    }
                }
            }
        }
    }
    
    private class MouseClickEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent mouseEvent) {
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY();

            int status = displayText.mouseClickSetCurrNode(mousePressedX, mousePressedY);
            displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
            updateCursorbyMouseClick(status);
        }

    }
    
    private class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors = {Color.BLACK, Color.TRANSPARENT};

        CursorBlinkEventHandler() {
            blinkCursor();
        }

        private void blinkCursor() {
            cursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            blinkCursor();
        }
    }


    public void makeCursorBlink() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorBlink = new CursorBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorBlink);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    public void updateCursor() {
        if (displayText.isCurrFSentinel() == 1) {
            cursor.setX(STARTING_TEXT_POSITION_X);
            cursor.setY(STARTING_TEXT_POSITION_Y);
        } else {
            double cXPos = displayText.getCurrText().getX() + displayText.getCurrText().getLayoutBounds().getWidth();
            double cYPos = displayText.getCurrText().getY();
            if (eqNewline(displayText.getCurrText().getText().charAt(0))) {
                cursor.setX(STARTING_TEXT_POSITION_X);
                cursor.setY(displayText.getCurrText().getY() + (displayText.getCurrText().getLayoutBounds().getHeight() / 2));
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight() / 2);
            } else {
                cursor.setX(cXPos);
                cursor.setY(cYPos);
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight());
            }
        }
    }

    public void updateCursorbyMouseClick(int status) {
        if (displayText.isCurrFSentinel() == 1) {
            cursor.setX(STARTING_TEXT_POSITION_X);
            cursor.setY(STARTING_TEXT_POSITION_Y);
        } else if (status == 2) {
            double cXPos = displayText.getCurrText().getX() + displayText.getCurrText().getLayoutBounds().getWidth();
            double cYPos = displayText.getCurrText().getY();
            cursor.setX(cXPos);
            cursor.setY(cYPos);
            if (eqNewline(displayText.getCurrText().getText().charAt(0))) {
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight() / 2);
            } else {
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight());
            }
        } else {
            cursor.setX(STARTING_TEXT_POSITION_X);
            cursor.setY(displayText.getCurrText().getY() + (displayText.getCurrText().getLayoutBounds().getHeight() / 2));
            cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight() / 2);
        }
    }

    public void updateCursorbyUpDown(int status) {
        if (displayText.isCurrFSentinel() == 1) {
            cursor.setX(STARTING_TEXT_POSITION_X);
            cursor.setY(STARTING_TEXT_POSITION_Y);
        } else if (status == 1) { 
            cursor.setX(STARTING_TEXT_POSITION_X);
            if (eqNewline(displayText.getCurrText().getText().charAt(0))) {
                cursor.setY(displayText.getCurrText().getY() + (displayText.getCurrText().getLayoutBounds().getHeight() / 2));
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight() / 2);
            } else {
                cursor.setY(displayText.getCurrText().getY() + displayText.getCurrText().getLayoutBounds().getHeight());
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight());
            }
        } else if (status == 2) {
            double cXPos = displayText.getCurrText().getX() + displayText.getCurrText().getLayoutBounds().getWidth();
            double cYPos = displayText.getCurrText().getY();
            cursor.setX(cXPos);
            cursor.setY(cYPos);
            if (eqNewline(displayText.getCurrText().getText().charAt(0))) {
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight() / 2);
            } else {
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight());
            }
        }
    }

    public void updateCursorbyLeftRight() {
        if (displayText.isCurrFSentinel() == 1) {
            cursor.setX(STARTING_TEXT_POSITION_X);
            cursor.setY(STARTING_TEXT_POSITION_Y);
        } else {
            double cXPos = displayText.getCurrText().getX() + displayText.getCurrText().getLayoutBounds().getWidth();
            double cYPos = displayText.getCurrText().getY();
            if (eqNewline(displayText.getCurrText().getText().charAt(0))) {
                cursor.setX(STARTING_TEXT_POSITION_X);
                cursor.setY(displayText.getCurrText().getY() + (displayText.getCurrText().getLayoutBounds().getHeight() / 2));
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight() / 2);
            } else {
                cursor.setX(cXPos);
                cursor.setY(cYPos);
                cursor.setHeight(displayText.getCurrText().getLayoutBounds().getHeight());
            }
        }
    }

    public void updateCursorFont(String fontname, int fontsize) {
        Text temp = new Text("a");
        temp.setFont(Font.font(fontname, fontsize));
        cursor.setHeight(temp.getLayoutBounds().getHeight());
    }

    public boolean eqNewline(char c) {
        if (c == '\r' || c == '\n') {
            return true;
        } else {
            return false;
        }
    }

    public void writeAndSave() {
        try {
            FileWriter writer = new FileWriter(fileName);
            displayText.setCurrtoFSentinel();
            displayText.incCurrNode();
            while (displayText.isCurrBSentinel() == 0) {
                writer.write(displayText.getCurrText().getText().charAt(0));
                displayText.incCurrNode();
            }
            writer.close();
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Application.Parameters appArgs = getParameters();
        java.util.List<String> args = appArgs.getRaw();

        fileName = args.get(0);
        // Create a Node that will be the parent of all things displayed on the screen.
        root = new Group();
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);

        try {
            File inputFile = new File(fileName);
            if (inputFile.exists() && inputFile.isFile()) {
                FileReader reader = new FileReader(inputFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                int intRead = -1;
                while ((intRead = bufferedReader.read()) != -1) {
                    char charRead = (char) intRead;
                    if (charRead != '\r') {
                        displayText.addChar(charRead, Font.font(STARTING_FONT_NAME, STARTING_FONT_SIZE), VPos.TOP);
                        root.getChildren().add(displayText.getCurrText());
                    }
                }
                displayText.setCurrtoFSentinel();
                displayText.setTextXY(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y);
                updateCursor();
                bufferedReader.close();
            } else if (inputFile.isDirectory()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("Unable to open file with directory name: " + fileName);
            System.exit(1);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler());

        root.getChildren().add(cursor);
        makeCursorBlink();

        primaryStage.setTitle("Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No file name provided. The program will now exit.");
            System.exit(1);
        }
        launch(args);
    }
}
