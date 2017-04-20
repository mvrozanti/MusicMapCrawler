/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicmapcrawler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * <p>
 * just typing into the screen should start researching</p>
 * <p>
 * every search ought to store the info on database</p>
 *
 * @author Nexor
 */
public class MusicMapCrawler extends WebRobot {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String dbURL = "jdbc:derby://localhost:1527/MusicMap;create=true;user=nexor;password=map";
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        Connection c = DriverManager.getConnection(dbURL);

        MusicMapCrawler mmc = new MusicMapCrawler();
        List<Band> bands = new ArrayList<>();
        mmc.driver.get("http://www.music-map.com/i+monster.html");
        WebElement gnodmap = mmc.driver.findElementById("gnodMap");
        List<WebElement> bandElements = gnodmap.findElements(By.tagName("a"));
        for (WebElement bandElement : bandElements) {
            Band b = null;
            try {
                b = Band.parse(bandElement);
            } catch (Exception ex) {// unparseable
                continue;
            }
            try {
                b.persistIfUnknown(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bands.add(b);
        }
        displayBands(bands);
    }

    public static void displayBands(List<Band> bands) {
        StringBuilder searchBuffer = new StringBuilder();
        JFrame f = new JFrame();
        f.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        f.setUndecorated(true);
        f.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    searchBuffer.append(' ');
                }
                if (Character.isLetter(e.getKeyChar())) {
                    searchBuffer.append(e.getKeyChar());
                }
            }
        });
        JPanel p = new JPanel() {
            private int searchBufferOpacity = 0;
            private int lastSearchBufferLen = 0;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                super.paintComponent(g2d);
                for (Band b : bands) {
                    b.display(g2d);
                }
                if (searchBuffer.length() > 0) {
                    if (lastSearchBufferLen != searchBuffer.length()) {
                        lastSearchBufferLen = searchBuffer.length();
                        searchBufferOpacity = 0;
                    }
                    Color c = Color.yellow;
                    for (int i = 0; i < searchBufferOpacity; i++) {
                        c = c.darker();
                    }
                    if (c.equals(Color.black)) {
                        searchBuffer.setLength(0);
                    }
                    g2d.setColor(c);
                    g2d.drawChars(searchBuffer.toString().toCharArray(),
                            0,
                            searchBuffer.length(),
                            800 - searchBuffer.length() * 3,
                            600);
                }
                if (System.currentTimeMillis() % 371 == 0) {
                    searchBufferOpacity++;
                }
                repaint();
            }
        };
        p.setBackground(Color.black);
        f.add(p);
        f.setVisible(true);
    }

}
