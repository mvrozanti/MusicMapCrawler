package musicmapcrawler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Nexor
 */
public class Band {

    private Point2D.Double pos;
    private String name;

    public Band(double x, double y, String name) {
        pos = new Point2D.Double(x, y);
        this.name = name;
    }

    public static Band parse(WebElement e) throws Exception {
        String regex = "display: block; left: (\\d+\\.\\d+)px; top: (\\d+\\.\\d+)px;";
        String name = e.getText();
        String style = e.getAttribute("style");
        if (style.isEmpty()) {
            throw new Exception();
        }
        Matcher m = Pattern.compile(regex).matcher(style);
        Band b = null;
        m.find();
        double x = Double.parseDouble(m.group(1));
        double y = Double.parseDouble(m.group(2));
        b = new Band(x, y, name);
        return b;
    }

    public Point2D.Double getPos() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public void persist(Connection c) throws SQLException {
        c.prepareStatement("INSERT INTO Band VALUES ('"
                + name + "', "
                + pos.x + ", "
                + pos.y + ")").executeUpdate();
    }

    public void display(Graphics2D g2d) {
        Shape s = new Ellipse2D.Double(pos.x, pos.y, 1, 1);
        g2d.setColor(Color.red);
        g2d.draw(s);
        g2d.drawChars(name.toCharArray(), 0, name.length(), ((int) pos.x) + 1, ((int) pos.y) + 1);
    }
}
