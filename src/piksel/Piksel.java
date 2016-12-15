/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package piksel;

/**
 *
 * @author student
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;

public class Piksel extends JFrame {

    BufferedImage image;
    JLabel promptLabel;
    JTextField prompt;
    JButton promptButton;
    JFileChooser fileChooser;
    JButton loadButton;
    JButton processingButton;
    JScrollPane scrollPane;
    JLabel imgLabel;

    public Piksel() {
        super("Image processing");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        JPanel inputPanel = new JPanel();
        promptLabel = new JLabel("Filename:");
        inputPanel.add(promptLabel);
        prompt = new JTextField(20);
        inputPanel.add(prompt);
        promptButton = new JButton("Browse");
        inputPanel.add(promptButton);
        contentPane.add(inputPanel, BorderLayout.NORTH);
        fileChooser = new JFileChooser();
        promptButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int returnValue
                        = fileChooser.showOpenDialog(null);
                        if (returnValue
                        == JFileChooser.APPROVE_OPTION) {
                            File selectedFile
                            = fileChooser.getSelectedFile();
                            if (selectedFile != null) {
                                prompt.setText(selectedFile.getAbsolutePath());
                            }
                        }
                    }
                }
        );

        imgLabel = new JLabel();
        scrollPane = new JScrollPane(imgLabel);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel();
        loadButton = new JButton("Load");
        outputPanel.add(loadButton);
        loadButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String name = prompt.getText();
                            File file = new File(name);
                            if (file.exists()) {
                                image = ImageIO.read(file.toURL());
                                if (image == null) {
                                    System.err.println("Invalid input file format");
                                } else {
                                    imgLabel.setIcon(new ImageIcon(image));
                                }
                            } else {
                                System.err.println("Bad filename");
                            }
                        } catch (MalformedURLException mur) {
                            System.err.println("Bad filename");
                        } catch (IOException ioe) {
                            System.err.println("Error reading file");
                        }
                    }
                }
        );

        processingButton = new JButton("Processing");
        outputPanel.add(processingButton);
        processingButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Processing(image);
                        imgLabel.setIcon(new ImageIcon(image));
                    }
                });

        contentPane.add(outputPanel, BorderLayout.SOUTH);
    }

    private static int getPixel(BufferedImage img, int x, int y) {
        int gray;
        int rgb = img.getRGB(x, y);
        int r = (rgb & 0x00ff0000) >>> 16;
        int g = (rgb & 0x0000ff00) >>> 8;
        int b = rgb & 0x000000ff;
        gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        return gray;
    }

    private static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(),
                source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    private static int filterPixel(BufferedImage img, int x, int y, int[][] maska) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (x > w || y > h || x < 0 || y < 0) {
            return 0;
        }

        int a = 0;
        int tempx = x - (maska.length / 2);
        int tempy = y - (maska[0].length / 2);

        for (int i = 1; i < maska.length; i++) {
            for (int j = 1; j < maska[0].length; j++) {
                a += getPixel(img, tempx + i, tempy + j) * maska[i][j];
            }
        }
        if (a > 255) {
            return 255;
        }
        if (a < 0) {
            return 0;
        }
        return a;
    }

    private static void Processing(BufferedImage img) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        BufferedImage temp = copyImage(img);
        int[][] maska = new int[][]{
            {-1, -1, -1},
            {-1, 8, -1},
            {-1, -1, -1},};

        for (int x = 1; x < w-1; x++) {
            for (int y = 1; y < h-1; y++) {
                int a = filterPixel(temp, x, y, maska);
                int RGB = a | (a << 8) | (a << 16) | (a << 24);
                img.setRGB(x, y, RGB);
            }
        }

    }

    public static void main(String args[]) {
        JFrame frame = new Piksel();
        frame.pack();
        frame.show();
    }
}
