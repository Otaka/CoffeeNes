package com.smalljnes;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Dmitry
 */
public class TestWindow extends JFrame {

    private Nes nes;
    private BufferedImage imageToDraw = new BufferedImage(256, 240, BufferedImage.TYPE_3BYTE_BGR);
    private byte joystickByte = 0;
    private Timer timer = new Timer(false);
    private JPanel drawPanel = new JPanel() {
        @Override
        public void paint(Graphics g) {
            g.drawImage(imageToDraw, 0, 0, getWidth(), getHeight(), this);
        }
    };

    public static void main(String[] args) throws IOException {
        System.out.println("Started smallJNes");
        new TestWindow().init();
    }

    private void init() throws IOException {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        nes = new Nes();
        Cartridge cartridge = new Cartridge();

        try {
            cartridge.load(new File("h:\\Games\\Emulation\\nes\\games\\Little N Mermaid The (Little Mermaid Hack).nes"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }

        nes.insertCartridge(cartridge);
        nes.setOnDrawFrameEvent(new OnDrawFrame() {
            int[] pixelsData = new int[256 * 240 * 3];

            @Override
            public void onDrawFrame(int[] buffer) {
                int index = 0;
                for (int i = 0; i < 256 * 240; i++, index += 3) {
                    int val = buffer[i];
                    pixelsData[index + 2] = val & 0xFF;
                    pixelsData[index + 1] = (val >> 8) & 0xFF;
                    pixelsData[index + 0] = (val >> 16) & 0xFF;
                }
                imageToDraw.getRaster().setPixels(0, 0, 256, 240, pixelsData);
                drawPanel.repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            Map<Integer, Integer> buttonsInfo = new HashMap<Integer, Integer>() {
                {
                    put(KeyEvent.VK_H, 0);
                    put(KeyEvent.VK_G, 1);
                    put(KeyEvent.VK_BACK_SPACE, 2);
                    put(KeyEvent.VK_ENTER, 3);
                    put(KeyEvent.VK_W, 4);
                    put(KeyEvent.VK_S, 5);
                    put(KeyEvent.VK_A, 6);
                    put(KeyEvent.VK_D, 7);
                }
            };

            @Override
            public void keyPressed(KeyEvent e) {
                Integer nesButton = buttonsInfo.get(e.getKeyCode());
                if (nesButton != null) {
                    joystickByte = (byte) (joystickByte | (1 << nesButton));
                }
                nes.getJoypad().setJoyPadButtonsState(joystickByte);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                Integer nesButton = buttonsInfo.get(e.getKeyCode());
                if (nesButton != null) {
                    joystickByte = (byte) (joystickByte & (~(1 << nesButton)));
                }
                nes.getJoypad().setJoyPadButtonsState(joystickByte);
            }

        });
        nes.powerUp();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                nes.getCpu().runFrame();
            }
        }, 0, 16);
        drawPanel.setPreferredSize(new Dimension(256, 240));
        getContentPane().add(drawPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

}
