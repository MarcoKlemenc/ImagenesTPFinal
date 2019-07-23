package Interfaces;

import javax.swing.*;

public class IntegerEntry {

    public static int[] getValues(String message, String[] labels) {
        JTextField[] inputs = new JTextField[labels.length];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new JTextField();
        }

        Object[] fields = new Object[labels.length * 2];
        for (int i = 0; i < inputs.length; i++) {
            fields[i * 2] = labels[i];
            fields[i * 2 + 1] = inputs[i];
        }

        int option = JOptionPane.showConfirmDialog(null, fields, message, JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int[] vals = new int[inputs.length];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = Integer.valueOf(inputs[i].getText());
            }
            return vals;
        }

        return null;
    }

}