package Calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Calculator1 extends JFrame {

    private JTextField displayField;
    private JTextArea historyArea;
    private String currentInput = "";
    private String operator = "";
    private double num1 = 0;
    private ArrayList<String> history = new ArrayList<>();
    private boolean startNewInput = true;  // Sửa lại biến này để theo dõi trạng thái nhập mới
    private boolean afterEquals = false;
    private boolean isWhiteMode = true;

    public Calculator1() {
        setTitle("Simple Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1366, 768);
        setLocationRelativeTo(null);
        setupKeyboardShortcuts();  // Cập nhật phương thức xử lý sự kiện bàn phím

        displayField = new JTextField("0");
        displayField.setFont(new Font("Arial", Font.BOLD, 30));
        displayField.setEditable(false);
        displayField.setBackground(Color.BLACK);
        displayField.setForeground(Color.WHITE);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        add(displayField, BorderLayout.NORTH);

        historyArea = new JTextArea(10, 20);
        historyArea.setFont(new Font("Arial", Font.PLAIN, 16));
        historyArea.setEditable(false);
        historyArea.setBackground(Color.BLACK);
        historyArea.setForeground(Color.WHITE);
        add(new JScrollPane(historyArea), BorderLayout.EAST);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 5, 5, 5));
        buttonPanel.setBackground(Color.DARK_GRAY);

        String[] buttons = {
            "C", "√", "/", "x", "S",
            "7", "8", "9", "*", "",
            "4", "5", "6", "-", "",
            "1", "2", "3", "+", "",
            "%", "0", ".", "^", "="
        };

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 20));
            if (text.equals("C") || text.equals("x")) {
                button.setBackground(Color.RED);
            } else if (text.equals("=")) {
                button.setBackground(Color.GRAY);
            } else if (text.equals("√") || text.equals("+") || text.equals(".") || text.equals("-")
                    || text.equals("*") || text.equals("/") || text.equals("%") || text.equals("^")) {
                button.setBackground(Color.BLACK);
            } else if (text.equals("S")) {
                button.setBackground(Color.BLACK);
                button.addActionListener(e -> openSettingsDialog());
            } else {
                button.setBackground(Color.BLACK);
            }
            button.setForeground(Color.WHITE);
            if (!text.equals("S")) {  // Không gán ButtonClick cho nút S
                button.addActionListener(new ButtonClick());
            }
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void setupKeyboardShortcuts() {
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                String key = String.valueOf(keyChar);

                try {
                    if (Character.isDigit(keyChar) || keyChar == '.') {
                        handleKeyboardInput(key);
                    } else if (key.equals("+") || key.equals("-") || key.equals("*") || key.equals("/") || key.equals("^")) {
                        handleKeyboardOperator(key);
                    } else if (keyChar == '%') {
                        handlePercentage();
                    } else if (keyChar == '\n') { // Enter
                        handleEquals();
                    } else if (keyChar == 'c' || keyChar == 'C') {
                        handleClear();
                    } else if (keyChar == 'x' || keyChar == 'X') {
                        handleBackspace();
                    }
                } catch (Exception ex) {
                    displayField.setText("Error");
                    reset();
                }
            }
        };
        addKeyListener(keyAdapter);
        setFocusable(true);
    }

    private void handleNumberOrDecimal(String input) {
    if (startNewInput) {
        currentInput = input;
        startNewInput = false;
    } else {
        currentInput += input;
    }
    displayField.setText(currentInput);
}


    private void handleNegativeSign() {
        if (!currentInput.startsWith("-")) {
            currentInput = "-" + currentInput;
        } else {
            currentInput = currentInput.substring(1);
        }
        displayField.setText(currentInput);
    }

    private void handleBasicOperator(String op) {
        if (!currentInput.isEmpty()) {
            if (afterEquals) {  // Nếu sau dấu "=" thì reset num1
                num1 = Double.parseDouble(currentInput);
            }
            operator = op;
            startNewInput = true;
            afterEquals = false;  // Reset lại trạng thái để bắt đầu phép tính mới
        }
    }

    private void handlePowerOperator() {
        if (!currentInput.isEmpty()) {
            num1 = Double.parseDouble(currentInput);
            operator = "^";
            startNewInput = true;
        }
    }

    private void handleSquareRoot() {
        if (!currentInput.isEmpty()) {
            double value = Double.parseDouble(currentInput);
            if (value < 0) {
                displayField.setText("Error: sqrt of negative");
                reset();
            } else {
                currentInput = String.valueOf(Math.sqrt(value));
                displayField.setText(currentInput);
                startNewInput = true;
            }
        }
    }

    private void reset() {
        currentInput = "";
        num1 = 0;
        operator = "";
        startNewInput = true;
    }

    private class ButtonClick implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            try {
                if (command.matches("[0-9]") || command.equals(".")) {
                    handleNumberOrDecimal(command);
                } else if (command.equals("-") && (currentInput.isEmpty() || startNewInput)) {
                    handleNegativeSign();
                } else if (command.equals("+") || command.equals("-")
                        || command.equals("*") || command.equals("/")) {
                    handleBasicOperator(command);
                } else if (command.equals("^")) {
                    handlePowerOperator();
                } else if (command.equals("√")) {
                    handleSquareRoot();
                } else if (command.equals("%")) {
                    handlePercentage();
                } else if (command.equals("=")) {
                    handleEquals();
                } else if (command.equals("C")) {
                    handleClear();
                } else if (command.equals("x")) {
                    handleBackspace();
                }
            } catch (NumberFormatException ex) {
                displayField.setText("Error: Invalid input");
                reset();
            } catch (ArithmeticException ex) {
                displayField.setText("Error: " + ex.getMessage());
                reset();
            } catch (Exception ex) {
                displayField.setText("Error: Overflow");
                reset();
            }
        }
    }

    private void handleKeyboardInput(String key) {
        if (startNewInput || afterEquals) {
            currentInput = key;
            startNewInput = false;
            afterEquals = false;
        } else {
            currentInput += key;
        }
        displayField.setText(currentInput);
    }

    private void handleKeyboardOperator(String key) {
        if (!currentInput.isEmpty()) {
            num1 = Double.parseDouble(currentInput);
            operator = key;
            currentInput = "";
            startNewInput = true;
        }
    }

    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.format("%d", (long) number);  // Nếu là số nguyên thì in như int
        } else {
            return String.valueOf(number);  // Ngược lại giữ nguyên
        }
    }

    private void handleEquals() {
    if (!currentInput.isEmpty() && operator != null && !operator.isEmpty()) {
        double num2 = Double.parseDouble(currentInput);
        double result = 0;
        try {
            switch (operator) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 != 0) {
                        result = num1 / num2;
                    } else {
                        displayField.setText("Error: Divide by zero");
                        reset();
                        return;
                    }
                    break;
                case "^":
                    result = Math.pow(num1, num2);
                    break;
            }
            currentInput = String.valueOf(result);
            displayField.setText(formatNumber(result)); // In kết quả không có .0
            history.add(formatNumber(num1) + " " + operator + " " + formatNumber(num2) + " = " + formatNumber(result));
            updateHistory();
            operator = "";  // Reset operator để không giữ lại phép tính
            num1 = result;  // Cập nhật lại num1 với kết quả phép tính
            startNewInput = true;  // Sẵn sàng cho phép tính tiếp theo
            afterEquals = true;  // Đánh dấu là phép tính đã thực hiện
        } catch (Exception e) {
            displayField.setText("Error: Invalid input");
            reset();
        }
    }
}


    private void handleClear() {
        currentInput = "";
        operator = "";
        num1 = 0;
        displayField.setText("0");
        startNewInput = true;
        afterEquals = false;  // Đánh dấu không phải sau phép tính
    }

    private void handleBackspace() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            displayField.setText(currentInput.isEmpty() ? "0" : currentInput);
        }
    }

    private void handlePercentage() {
        if (!currentInput.isEmpty()) {
            double value = Double.parseDouble(currentInput);
            currentInput = String.valueOf(value / 100);
            displayField.setText(currentInput);
        }
    }

    private void updateHistory() {
        historyArea.setText(String.join("\n", history));
    }

    private void openSettingsDialog() {
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setSize(400, 400);
        settingsDialog.setLayout(new GridLayout(6, 1, 10, 10));
        settingsDialog.setLocationRelativeTo(this);

        // Dark/Light mode
        JButton darkModeButton = new JButton("Dark Mode");
        JButton lightModeButton = new JButton("Light Mode");

        darkModeButton.addActionListener(e -> applyDarkMode());
        lightModeButton.addActionListener(e -> applyLightMode());

        // Chọn Font
        JButton fontButton = new JButton("Change Font");
        fontButton.addActionListener(e -> {
            String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            String font = (String) JOptionPane.showInputDialog(settingsDialog, "Choose Font:",
                    "Font Selection", JOptionPane.PLAIN_MESSAGE, null, fonts, "Arial");
            if (font != null) {
                displayField.setFont(new Font(font, Font.BOLD, 30));
                historyArea.setFont(new Font(font, Font.PLAIN, 16));
            }
        });

        // Chọn Color
        JButton colorButton = new JButton("Change Color");
        colorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(settingsDialog, "Choose Text Color", Color.WHITE);
            if (color != null) {
                displayField.setForeground(color);
                historyArea.setForeground(color);
            }
        });

        settingsDialog.add(darkModeButton);
        settingsDialog.add(lightModeButton);
        settingsDialog.add(fontButton);
        settingsDialog.add(colorButton);

        settingsDialog.setVisible(true);
    }

    private void applyDarkMode() {
        getContentPane().setBackground(Color.DARK_GRAY);
        displayField.setBackground(Color.BLACK);
        displayField.setForeground(Color.WHITE);
        historyArea.setBackground(Color.BLACK);
        historyArea.setForeground(Color.WHITE);
    }

    private void applyLightMode() {
        getContentPane().setBackground(Color.LIGHT_GRAY);
        displayField.setBackground(Color.WHITE);
        displayField.setForeground(Color.BLACK);
        historyArea.setBackground(Color.WHITE);
        historyArea.setForeground(Color.BLACK);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator1());
    }
}