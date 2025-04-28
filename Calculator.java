// Đặt package nếu bạn tổ chức theo thư mục, còn không thì xóa dòng này
package calculator1;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Calculator extends JFrame {
    private JTextField displayField;     // Ô hiển thị kết quả
    private JTextArea historyArea;       // Khu vực hiển thị lịch sử tính toán
    private StringBuilder currentInput = new StringBuilder();  // Dữ liệu người dùng nhập
    private ArrayList<String> history = new ArrayList<>();     // Lưu lịch sử các phép tính

    public Calculator() {
        setTitle("Máy tính đơn giản");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Thiết lập ô hiển thị kết quả
        displayField = new JTextField("0");
        displayField.setFont(new Font("Arial", Font.BOLD, 30));
        displayField.setEditable(false);
        displayField.setBackground(Color.BLACK);
        displayField.setForeground(Color.WHITE);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        add(displayField, BorderLayout.NORTH);

        // Thiết lập khu vực lịch sử
        historyArea = new JTextArea();
        historyArea.setFont(new Font("Arial", Font.PLAIN, 16));
        historyArea.setEditable(false);
        historyArea.setBackground(Color.BLACK);
        historyArea.setForeground(Color.WHITE);
        add(new JScrollPane(historyArea), BorderLayout.EAST);

        // Các nút chức năng
        String[] buttons = {
            "7", "8", "9", "/", "C",
            "4", "5", "6", "*", "<=",
            "1", "2", "3", "-", "√",
            "0", ".", "=", "+", "%",
            "(", ")", "^", "Copy", "Paste"
        };

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 5, 5, 5));
        buttonPanel.setBackground(Color.DARK_GRAY);

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.setBackground(Color.BLACK);
            button.setForeground(Color.WHITE);

            // Tô màu đặc biệt cho nút C, Copy, Paste
            if (text.equals("C") || text.equals("<=")) {
                button.setBackground(Color.RED);
            }
            if (text.equals("Copy") || text.equals("Paste")) {
                button.setBackground(Color.GRAY);
            }

            button.addActionListener(new ButtonClick());
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Xử lý các sự kiện nút bấm
    private class ButtonClick implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            switch (command) {
                case "C":
                    clear();          // Xóa toàn bộ
                    break;
                case "<=":
                    backspace();      // Xóa ký tự cuối
                    break;
                case "=":
                    evaluate();       // Tính toán biểu thức
                    break;
                case "√":
                    insertSquareRoot(); // Thêm sqrt vào biểu thức
                    break;
                case "Copy":
                    copyToClipboard(); // Copy kết quả
                    break;
                case "Paste":
                    pasteFromClipboard(); // Paste vào ô nhập
                    break;
                default:
                    appendInput(command); // Thêm ký tự bình thường
            }
        }
    }

    // Thêm ký tự vào biểu thức
    private void appendInput(String value) {
        if (displayField.getText().equals("0")) {
            currentInput.setLength(0);
        }
        currentInput.append(value);
        displayField.setText(currentInput.toString());
    }

    // Xóa toàn bộ biểu thức
    private void clear() {
        currentInput.setLength(0);
        displayField.setText("0");
    }

    // Xóa ký tự cuối
    private void backspace() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            if (currentInput.length() == 0) {
                displayField.setText("0");
            } else {
                displayField.setText(currentInput.toString());
            }
        }
    }

    // Thêm ký hiệu căn bậc hai vào biểu thức
    private void insertSquareRoot() {
        currentInput.append("sqrt(");
        displayField.setText(currentInput.toString());
    }

    // Copy kết quả vào clipboard
    private void copyToClipboard() {
        String result = displayField.getText();
        StringSelection selection = new StringSelection(result);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    // Paste dữ liệu từ clipboard
    private void pasteFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String pasteText = (String) clipboard.getData(DataFlavor.stringFlavor);
            currentInput.append(pasteText);
            displayField.setText(currentInput.toString());
        } catch (Exception ex) {
            displayField.setText("Lỗi khi Paste");
        }
    }

    // Tính toán biểu thức nhập vào
    private void evaluate() {
        try {
            String input = currentInput.toString()
                    .replaceAll("√", "sqrt")   // Hỗ trợ √ thay thành sqrt
                    .replaceAll("%", "/100")   // Hỗ trợ %
                    .replaceAll("\\^", "**");  // Hỗ trợ lũy thừa

            // Sử dụng ScriptEngine để tính biểu thức
            javax.script.ScriptEngine engine = new javax.script.ScriptEngineManager().getEngineByName("JavaScript");
            Object resultObj = engine.eval(replaceSqrt(input));
            String resultStr = resultObj.toString();

            // Hiển thị kết quả
            displayField.setText(resultStr);
            history.add(input + " = " + resultStr);
            updateHistory();

            // Reset để tiếp tục nhập
            currentInput.setLength(0);
            currentInput.append(resultStr);

        } catch (Exception ex) {
            displayField.setText("Lỗi biểu thức");
        }
    }

    // Chuyển sqrt thành Math.sqrt trong JavaScript
    private String replaceSqrt(String expr) {
        StringBuilder replaced = new StringBuilder();
        int len = expr.length();
        for (int i = 0; i < len; i++) {
            char c = expr.charAt(i);
            if (c == 's' && expr.startsWith("sqrt(", i)) {
                replaced.append("Math.sqrt(");
                i += 4; // Bỏ qua "sqrt"
            } else {
                replaced.append(c);
            }
        }
        return replaced.toString();
    }

    // Cập nhật lịch sử tính toán
    private void updateHistory() {
        historyArea.setText("");
        for (String record : history) {
            historyArea.append(record + "\n");
        }
    }

    // Hàm main để chạy chương trình
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator());
    }
}
