package calculator3;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class Calculator3 extends JFrame {

    private JTextField displayField;
    private JTextArea historyArea;
    private JTextField searchField;
    private JButton searchButton;
    private ArrayList<String> history = new ArrayList<>();
    private final String HISTORY_FILE = "D:\\historycalculator.txt";

    private String currentInput = "";
    private String operator = "";
    private double num1 = 0;
    private boolean startNewNumber = true;
    private boolean afterEquals = false;

    public Calculator3() {
        setTitle("Simple Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1366, 768);
        setLocationRelativeTo(null);

        displayField = new JTextField("0");
        displayField.setFont(new Font("Arial", Font.BOLD, 30));
        displayField.setEditable(false);
        displayField.setBackground(Color.BLACK);
        displayField.setForeground(Color.WHITE);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        add(displayField, BorderLayout.NORTH);

        // Right panel: Search + History
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Search components
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchHistory());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        JButton clearHistoryButton = new JButton("Clear History");
        clearHistoryButton.addActionListener(e -> clearHistory());
        rightPanel.add(clearHistoryButton, BorderLayout.SOUTH);

        rightPanel.add(searchPanel, BorderLayout.NORTH);

        historyArea = new JTextArea(10, 20);
        historyArea.setFont(new Font("Arial", Font.PLAIN, 16));
        historyArea.setEditable(false);
        historyArea.setBackground(Color.BLACK);
        historyArea.setForeground(Color.WHITE);
        historyArea.addMouseListener(new HistoryClickListener());
        rightPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        add(rightPanel, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 5, 5, 5));
        buttonPanel.setBackground(Color.DARK_GRAY);

        String[] buttons = {
            "C", "√ ", "/", "<=", "",
            "7", "8", "9", "*", "",
            "4", "5", "6", "-", "",
            "1", "2", "3", "+", "",
            "%", "0", ".", "^", "="
        };

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 20));
            if (text.equals("C") || text.equals("<=")) {
                button.setBackground(Color.RED);
            } else if (text.equals("=")) {
                button.setBackground(Color.GRAY);
            } else if (text.equals("√ ") || text.equals("+") || text.equals(".") || text.equals("-")
                    || text.equals("*") || text.equals("/") || text.equals("%") || text.equals("^")) {
                button.setBackground(Color.BLACK);
            } else if (text.equals("")) {
                button.setBackground(Color.DARK_GRAY);
                button.setEnabled(false);
            } else {
                button.setBackground(Color.BLACK);
            }
            button.setForeground(Color.WHITE);
            button.addActionListener(new ButtonClick());
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);

        // Load history from file
        loadHistoryFromFile();

        setVisible(true);
    }

    private class ButtonClick implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            try {
                if (command.matches("[0-9]") || command.equals(".")) {
                    handleNumberOrDecimal(command);
                } else if (command.equals("-") && (currentInput.isEmpty() || startNewNumber)) {
                    handleNegativeSign();
                } else if (command.equals("+") || command.equals("-")
                        || command.equals("*") || command.equals("/")) {
                    handleBasicOperator(command);
                } else if (command.equals("^")) {
                    handlePowerOperator();
                } else if (command.equals("√ ")) {
                    handleSquareRoot();
                } else if (command.equals("%")) {
                    handlePercentage();
                } else if (command.equals("=")) {
                    handleEquals();
                } else if (command.equals("C")) {
                    handleClear();
                } else if (command.equals("<=")) {
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

        private void handleNumberOrDecimal(String command) {
            if (afterEquals) {
                currentInput = "";
                afterEquals = false;
            }
            if (startNewNumber) {
                currentInput = "";
                startNewNumber = false;
            }
            if (command.equals(".") && (currentInput.contains(".") || currentInput.isEmpty())) {
                return;
            }
            currentInput += command;
            displayField.setText(currentInput);
        }

        private void handleNegativeSign() {
            if (currentInput.isEmpty() && !afterEquals) {
                currentInput = "-";
                displayField.setText(currentInput);
            } else if (!currentInput.isEmpty()) {
                handleBasicOperator("-");
            }
        }

        private void handleBasicOperator(String command) {
            if (!currentInput.isEmpty()) {
                if (!operator.isEmpty() && !startNewNumber) {
                    double num2 = Double.parseDouble(currentInput);
                    double result = performOperation(num2);
                    String calc = formatNumber(num1) + " " + operator + " " + formatNumber(num2) + " = " + formatResult(result);
                    displayResult(result, calc);
                    num1 = result;
                } else {
                    num1 = Double.parseDouble(currentInput);
                }
                operator = command;
                currentInput = "";
                startNewNumber = true;
                afterEquals = false;
            } else if (!operator.isEmpty()) {
                operator = command;
            }
        }

        private void handlePowerOperator() {
            handleBasicOperator("^");
        }

        private void handleSquareRoot() {
            if (currentInput.isEmpty()) {
                displayField.setText("Error: No input");
                return;
            }
            double number = Double.parseDouble(currentInput);
            if (number < 0) {
                throw new ArithmeticException("Negative number");
            }
            double result = Math.sqrt(number);
            String calc = "√" + formatNumber(number) + " = " + formatResult(result);
            displayResult(result, calc);
            num1 = result;
            operator = "";
            startNewNumber = true;
            afterEquals = true;
        }

        private void handlePercentage() {
            if (currentInput.isEmpty()) {
                displayField.setText("Error: No input");
                return;
            }
            double number = Double.parseDouble(currentInput);
            double result;
            String calc;
            if (operator.isEmpty()) {
                result = number / 100;
                calc = formatNumber(number) + "% = " + formatResult(result);
            } else {
                result = num1 * (number / 100);
                calc = formatNumber(number) + "% of " + formatNumber(num1) + " = " + formatResult(result);
            }
            displayResult(result, calc);
            num1 = result;
            operator = "";
            startNewNumber = true;
            afterEquals = true;
        }

        private void handleEquals() {
            if (currentInput.isEmpty() && operator.isEmpty()) {
                return;
            }
            if (!currentInput.isEmpty() && operator.isEmpty()) {
                double number = Double.parseDouble(currentInput);
                String calc = formatNumber(number);
                displayResult(number, calc);
                num1 = number;
                startNewNumber = true;
                afterEquals = true;
                return;
            }
            if (currentInput.isEmpty() || operator.isEmpty()) {
                displayField.setText("Error: Incomplete input");
                return;
            }
            double num2 = Double.parseDouble(currentInput);
            double result = performOperation(num2);
            String calc = formatNumber(num1) + " " + operator + " " + formatNumber(num2) + " = " + formatResult(result);
            displayResult(result, calc);
            num1 = result;
            operator = "";
            startNewNumber = true;
            afterEquals = true;
        }

        private double performOperation(double num2) {
            double result = 0;
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
                    if (num2 == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    result = num1 / num2;
                    break;
                case "^":
                    result = Math.pow(num1, num2);
                    break;
            }
            return result;
        }

        private void handleClear() {
            reset();
            displayField.setText("0");
        }

        private void handleBackspace() {
            if (!currentInput.isEmpty()) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                displayField.setText(currentInput.isEmpty() ? "0" : currentInput);
            }
        }
    }

    private void reset() {
        currentInput = "";
        num1 = 0;
        operator = "";
        startNewNumber = true;
        afterEquals = false;
    }

    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        }
        return String.format("%.10f", number).replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    private String formatResult(double result) {
        return formatNumber(result);
    }

    private void displayResult(double result, String calc) {
        displayField.setText(formatResult(result));
        currentInput = formatNumber(result);
        history.add(calc);
        saveHistoryToFile(); // Lưu lịch sử vào file ngay khi có thay đổi
        updateHistory();
    }

    private void updateHistory() {
        historyArea.setText("");
        for (String calc : history) {
            historyArea.append(calc + "\n");
        }
    }

    private void saveHistoryToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            for (String calc : history) {
                writer.write(calc);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving history: " + e.getMessage());
        }
    }

    private void loadHistoryFromFile() {
        try {
            File file = new File(HISTORY_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Error loading history: " + e.getMessage());
        }
        updateHistory();
    }

    private void searchHistory() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            updateHistory();
            return;
        }
        List<String> results = history.stream()
                .filter(calc -> calc.contains(keyword))
                .collect(Collectors.toList());

        historyArea.setText("");
        for (String calc : results) {
            historyArea.append(calc + "\n");
        }
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this, "Clear all history?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            history.clear();
            saveHistoryToFile(); // Lưu lịch sử sau khi xóa
            updateHistory();
        }
    }

    private class HistoryClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                int offset = historyArea.viewToModel2D(e.getPoint());
                int start = historyArea.getLineStartOffset(historyArea.getLineOfOffset(offset));
                int end = historyArea.getLineEndOffset(historyArea.getLineOfOffset(offset));
                String selectedLine = historyArea.getText().substring(start, end).trim();
                if (!selectedLine.isEmpty()) {
                    int confirm = JOptionPane.showConfirmDialog(null, "Delete this history?\n" + selectedLine, "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        history.remove(selectedLine);
                        saveHistoryToFile(); // Lưu lịch sử sau khi xóa dòng
                        updateHistory();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator3());
    }
}
