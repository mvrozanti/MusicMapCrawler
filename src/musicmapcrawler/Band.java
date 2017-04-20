package musicmapcrawler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Nexor
 */
public class Band {

    private String href;
    private String name;
    private Point2D.Double pos;

    public Band(String href, String name, double x, double y) {
        this.href = href;
        this.name = name;
        pos = new Point2D.Double(x, y);
    }

    public static Band parse(WebElement e) throws Exception {
        String regex = "display: block; left: (\\d+\\.\\d+)px; top: (\\d+\\.\\d+)px;";
        String name = e.getText();
        String style = e.getAttribute("style");
        String href = e.getAttribute("href");
        if (style.isEmpty()) {
            throw new Exception();
        }
        Matcher m = Pattern.compile(regex).matcher(style);
        Band b = null;
        m.find();
        double x = Double.parseDouble(m.group(1));
        double y = Double.parseDouble(m.group(2));
        b = new Band(href, name, x, y);
        return b;
    }

    public boolean isUnknown(Connection c) throws SQLException {
        ResultSet rs = c.prepareStatement("SELECT COUNT(*)\n"
                + "FROM BAND\n"
                + "WHERE HREF = '" + href + "'").executeQuery();
        rs.next();
        boolean unknown = rs.getInt(1) != 1;
        rs.close();
        return unknown;
    }

    public Point2D.Double getPos() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public void persist(Connection c) throws SQLException {
        c.prepareStatement("INSERT INTO Band VALUES ('"
                + href + "', '"
                + name + "', "
                + pos.x + ", "
                + pos.y + ")").executeUpdate();
    }

    public void persistIfUnknown(Connection c) throws SQLException {
        if (isUnknown(c)) {
            persist(c);
        }
    }

    public void display(Graphics2D g2d) {
        Shape s = new Ellipse2D.Double(pos.x, pos.y, 1, 1);
        g2d.setColor(Color.red);
        g2d.draw(s);
        g2d.drawChars(name.toCharArray(), 0, name.length(), ((int) pos.x) + 1, ((int) pos.y) + 1);
    }
}
