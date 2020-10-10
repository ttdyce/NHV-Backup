package com.ttdyce;

import java.awt.Container;
import java.awt.EventQueue;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DisplayImage extends JFrame {

    public DisplayImage(String imagePath) {
        initUI(imagePath);
    }

    private void initUI(String imagePath) {

        ImageIcon ii = loadImage(imagePath);

        JLabel label = new JLabel(ii);

        createLayout(label);

        setTitle("Image");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private ImageIcon loadImage(String imagePath) {
        return new ImageIcon(imagePath);
    }

    private void createLayout(JComponent... arg) {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addComponent(arg[0])
        );

        gl.setVerticalGroup(gl.createParallelGroup()
                .addComponent(arg[0])
        );

        pack();
    }

//    public static void main(String[] args) {
//
//        EventQueue.invokeLater(() -> {
//            DisplayImage ex = new DisplayImage();
//            ex.setVisible(true);
//        });
//    }
}