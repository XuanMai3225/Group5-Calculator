package calculator;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Calculator extends JFrame {
    private JTextField displayField;
    private JTextArea historyArea;
    private String currentInput = "";
    private String operator = "";
    private double num1 = 0;
    private ArrayList<String> history = new ArrayList<>();
    private boolean startNewNumber = true;
    private boolean afterEquals = false;

    public Calculator() {
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
            } else if (text.equals("√ ") || text.equals("+") || text.equals(".") || text.equals("-") ||
                      text.equals("*") || text.equals("/") || text.equals("%") || text.equals("^")) {
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
        setVisible(true);
    }

    private class ButtonClick implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            try {
                // Kiểm tra nếu là số hoặc dấu thập phân
                if (command.matches("[0-9]") || command.equals(".")) {
                    handleNumberOrDecimal(command);
                } 
                // Kiểm tra nếu là dấu âm ở đầu
                else if (command.equals("-") && (currentInput.isEmpty() || startNewNumber)) {
                    handleNegativeSign(); // Xử lý dấu âm
                } 
                // Kiểm tra nếu là các toán tử cơ bản (+, -, *, /)
                else if (command.equals("+") || command.equals("-") || 
                         command.equals("*") || command.equals("/")) {
                    handleBasicOperator(command);
                } 
                // Kiểm tra nếu là toán tử lũy thừa (^)
                else if (command.equals("^")) {
                    handlePowerOperator();
                } 
                // Kiểm tra nếu là căn bậc hai (√)
                else if (command.equals("√ ")) {
                    handleSquareRoot();
                } 
                // Kiểm tra nếu là phần trăm (%)
                else if (command.equals("%")) {
                    handlePercentage();
                } 
                // Kiểm tra nếu là dấu bằng (=)
                else if (command.equals("=")) {
                    handleEquals();
                } 
                // Kiểm tra nếu là nút xóa (C)
                else if (command.equals("C")) {
                    handleClear(); // Gọi phương thức xóa toàn bộ
                } else if (command.equals("<=")) {
                    handleBackspace(); // Gọi phương thức xóa ký tự cuối
                }
            } catch (NumberFormatException ex) {
                // Xử lý lỗi định dạng số
                displayField.setText("Lỗi: Định dạng dữ liệu nhập vào không hợp lệ");
                reset();
            } catch (ArithmeticException ex) {
                // Xử lý lỗi toán học (chia cho 0, căn bậc hai số âm, v.v.)
                displayField.setText("Lỗi: " + ex.getMessage());
                reset();
            } catch (Exception ex) {
                // Xử lý lỗi khác (tràn số, v.v.)
                displayField.setText("Lỗi: tràn bộ nhớ or thao tác không hợp lệ");
                reset();
            }
        }

        // Xử lý khi người dùng nhập số hoặc dấu thập phân
        private void handleNumberOrDecimal(String command) {
            if (afterEquals) {
                currentInput = ""; // Xóa đầu vào sau khi nhấn "="
                afterEquals = false;
            }
            if (startNewNumber) {
                if (!currentInput.equals("-")) { // Giữ lại dấu âm nếu có
                    currentInput = ""; // Bắt đầu số mới
                }
                startNewNumber = false;
            }
            if (command.equals(".") && (currentInput.contains(".") || currentInput.isEmpty())) {
                return; // Không cho phép nhập nhiều dấu "."
            }
            if (currentInput.startsWith("√") && command.equals("-")) {
                // Cho phép nhập dấu âm sau dấu căn
                if (currentInput.equals("√")) {
                    currentInput += "-";
                    displayField.setText(currentInput);
                } else {
                    displayField.setText("Lỗi: Không thể căn bậc hai số âm"); // Hiển thị lỗi nếu nhập dấu âm không hợp lệ
                }
                return;
            }
            currentInput += command; // Thêm số hoặc dấu "." vào đầu vào
            displayField.setText(currentInput); // Hiển thị đầu vào
        }

        // Xử lý khi người dùng nhập dấu âm
        private void handleNegativeSign() {
            if (currentInput.isEmpty()) {
                // Nếu đầu vào rỗng, thêm dấu âm
                currentInput = "-";
                displayField.setText(currentInput);
                startNewNumber = false; // Cho phép nhập tiếp số sau dấu âm
            } else if (currentInput.equals("-")) {
                // Nếu đã có dấu âm, xóa dấu âm
                currentInput = "";
                displayField.setText("0");
            } else {
                // Nếu đầu vào không rỗng, thêm hoặc thay đổi dấu âm
                if (currentInput.startsWith("-")) {
                    currentInput = currentInput.substring(1); // Xóa dấu âm
                } else {
                    currentInput = "-" + currentInput; // Thêm dấu âm
                }
                displayField.setText(currentInput);
            }
        }

        // Xử lý các toán tử cơ bản (+, -, *, /)
        private void handleBasicOperator(String command) {
            if (!currentInput.isEmpty()) {
                if (!operator.isEmpty() && !startNewNumber) {
                    // Thực hiện phép tính nếu đã có toán tử trước đó
                    double num2 = Double.parseDouble(currentInput);
                    double result = performOperation(num2);
                    String calc = formatNumber(num1) + " " + operator + " " + formatNumber(num2) + " = " + formatResult(result);
                    displayResult(result, calc);
                    num1 = result; // Cập nhật kết quả làm số thứ nhất
                } else {
                    num1 = Double.parseDouble(currentInput); // Lưu số thứ nhất
                }
                operator = command; // Lưu toán tử
                currentInput = ""; // Xóa đầu vào
                startNewNumber = true; // Bắt đầu số mới
                afterEquals = false;
            } else if (!operator.isEmpty()) {
                operator = command; // Cho phép thay đổi toán tử
            }
        }

        // Xử lý toán tử lũy thừa (^)
        private void handlePowerOperator() {
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
                operator = "^"; // Lưu toán tử lũy thừa
                currentInput = "";
                startNewNumber = true;
                afterEquals = false;
            }
        }

        // Xử lý toán tử căn bậc hai (√)
        private void handleSquareRoot() {
            if (afterEquals || startNewNumber) {
                // Nếu vừa nhấn "=" hoặc bắt đầu số mới, đặt lại trạng thái
                currentInput = "";
                afterEquals = false;
                startNewNumber = false;
            }

            if (currentInput.isEmpty()) {
                currentInput = "√"; // Hiển thị ký hiệu căn bậc hai
                displayField.setText(currentInput);
            } else if (currentInput.startsWith("√")) {
                // Nếu đã có ký hiệu √, thực hiện phép tính
                String numberStr = currentInput.substring(1); // Lấy phần số sau ký hiệu √
                if (numberStr.isEmpty() || numberStr.equals("-")) {
                    displayField.setText("Lỗi: Không thể căn bậc hai số âm"); // Lỗi nếu không có số hoặc số âm
                    return;
                }
                double number = Double.parseDouble(numberStr);
                if (number < 0) {
                    displayField.setText("Lỗi: Không thể căn bậc hai số âm"); // Lỗi nếu số âm
                    return;
                }
                double result = Math.sqrt(number); // Tính căn bậc hai
                checkOverflow(result); // Kiểm tra tràn số
                String calc = "√" + formatNumber(number) + " = " + formatResult(result);
                displayResult(result, calc);
                startNewNumber = true; // Bắt đầu số mới sau khi hiển thị kết quả
            } else {
                displayField.setText("Lỗi: Định dạng không hợp lệ"); // Lỗi nếu định dạng không đúng
            }
        }

        // Xử lý nút phần trăm (%)
        private void handlePercentage() {
            if (currentInput.isEmpty()) {
                displayField.setText("Lỗi: Không có dữ liệu đầu vào"); // Lỗi nếu không có đầu vào
                return;
            }
            double number = Double.parseDouble(currentInput);
            double result = number / 100; // Tính phần trăm
            displayField.setText(formatResult(result)); // Hiển thị kết quả
            currentInput = formatNumber(result); // Cập nhật đầu vào
        }

        // Xử lý nút dấu bằng (=)
        private void handleEquals() {
            if (currentInput.isEmpty()) {
                displayField.setText("Lỗi: Dữ liệu nhập vào không đủ"); // Lỗi nếu đầu vào không đầy đủ
                return;
            }

            try {
                double result;
                if (currentInput.startsWith("√")) {
                    // Xử lý trường hợp căn bậc hai
                    String numberStr = currentInput.substring(1); // Lấy phần số sau ký hiệu √
                    if (numberStr.isEmpty()) {
                        displayField.setText("Lỗi: Không có dữ liệu đầu vào"); // Lỗi nếu không có số
                        return;
                    }
                    double number = Double.parseDouble(numberStr);
                    if (number < 0) {
                        displayField.setText("Lỗi: Không thể căn bậc hai số âm"); // Lỗi nếu số âm
                        return;
                    }
                    result = Math.sqrt(number); // Tính căn bậc hai
                    checkOverflow(result); // Kiểm tra tràn số
                    String calc = "√" + formatNumber(number) + " = " + formatResult(result);
                    displayResult(result, calc);
                } else if (!operator.isEmpty()) {
                    // Xử lý các phép toán khác
                    double num2 = Double.parseDouble(currentInput);
                    result = performOperation(num2); // Thực hiện phép tính
                    displayResult(result, formatNumber(num1) + " " + operator + " " + formatNumber(num2) + " = " + formatResult(result));
                } else {
                    displayField.setText("Lỗi: Dữ liệu nhập vào không đủ"); // Lỗi nếu không có toán tử
                    return;
                }

                num1 = result;
                operator = "";
                startNewNumber = true;
                afterEquals = true; // Đặt trạng thái sau khi nhấn "="
            } catch (ArithmeticException ex) {
                displayField.setText("Lỗi: " + ex.getMessage());
                reset();
            }
        }

        // Thực hiện phép tính dựa trên toán tử
        private double performOperation(double num2) {
            switch (operator) {
                case "+":
                    return num1 + num2;
                case "-":
                    return num1 - num2;
                case "*":
                    return num1 * num2;
                case "/":
                    if (num2 == 0) {
                        throw new ArithmeticException("Không thể chia cho 0"); // Lỗi chia cho 0
                    }
                    return num1 / num2;
                case "^":
                    return Math.pow(num1, num2); // Lũy thừa
                default:
                    throw new IllegalStateException("Unknown operator: " + operator);
            }
        }
    }

    private void reset() {
        currentInput = "";
        num1 = 0;
        operator = "";
        startNewNumber = true;
        afterEquals = false;
        displayField.setText("0"); // Đặt lại màn hình hiển thị
    }

    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        }
        return String.format("%.10f", number).replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    private String formatResult(double result) {
        checkOverflow(result);
        return formatNumber(result);
    }

    private void checkOverflow(double result) {
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            throw new ArithmeticException("tràn bộ nhớ");
        }
        if (Math.abs(result) > 1e308 || (result != 0 && Math.abs(result) < 1e-308)) {
            throw new ArithmeticException("Số quá lớn/nhỏ");
        }
    }

    private void displayResult(double result, String calc) {
        displayField.setText(formatResult(result));
        currentInput = formatNumber(result);
        history.add(calc);
        updateHistory();
    }

    private void updateHistory() {
        historyArea.setText("");
        for (String calc : history) {
            historyArea.append(calc + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator());
    }

    private void handleBackspace() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1); // Xóa ký tự cuối
            if (currentInput.isEmpty()) {
                displayField.setText("0"); // Hiển thị 0 nếu không còn ký tự
            } else {
                displayField.setText(currentInput); // Hiển thị đầu vào còn lại
            }
        }
    }
    private void handleClear() {
        currentInput = "";
        num1 = 0;
        operator = "";
        startNewNumber = true;
        afterEquals = false;
        displayField.setText("0"); // Đặt lại màn hình hiển thị
        history.clear(); // Xóa lịch sử
        updateHistory(); // Cập nhật lịch sử hiển thị
    }
}


