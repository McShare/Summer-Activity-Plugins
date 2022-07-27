package cc.venja.minebbs.battle.calculation;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.util.Vector;

import static org.bukkit.Bukkit.getServer;

public class GFG
{
 
    // Define Infinite (Using INT_MAX
    // caused overflow problems)
    public static int INF = 10000;
 
    // Given three collinear points p, q, r,
    // the function checks if point q lies
    // on line segment 'pr'
    public static boolean onSegment(Vector p, Vector q, Vector r)
    {
        return q.getBlockX() <= Math.max(p.getBlockX(), r.getBlockX()) &&
                q.getBlockX() >= Math.min(p.getBlockX(), r.getBlockX()) &&
                q.getBlockY() <= Math.max(p.getBlockY(), r.getBlockY()) &&
                q.getBlockY() >= Math.min(p.getY(), r.getBlockY());
    }
 
    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are collinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    public static int orientation(Vector p, Vector q, Vector r)
    {
        int val = (q.getBlockY() - p.getBlockY()) * (r.getBlockX() - q.getBlockX())
                - (q.getBlockX() - p.getBlockX()) * (r.getBlockY() - q.getBlockY());
 
        if (val == 0)
        {
            return 0; // collinear
        }
        return (val > 0) ? 1 : 2; // clock or counter clock wise
    }
 
    // The function that returns true if
    // line segment 'p1q1' and 'p2q2' intersect.
    public static boolean doIntersect(Vector p1, Vector q1,
                            Vector p2, Vector q2)
    {
        // Find the four orientations needed for
        // general and special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);
 
        // General case
        if (o1 != o2 && o3 != o4)
        {
            return true;
        }
 
        // Special Cases
        // p1, q1 and p2 are collinear and
        // p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1))
        {
            return true;
        }
 
        // p1, q1 and p2 are collinear and
        // q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1))
        {
            return true;
        }
 
        // p2, q2 and p1 are collinear and
        // p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2))
        {
            return true;
        }
 
        // p2, q2 and q1 are collinear and
        // q1 lies on segment p2q2
        // Doesn't fall in any of the above cases
        return o4 == 0 && onSegment(p2, q1, q2);
    }
 
    // Returns true if the point p lies
    // inside the polygon[] with n vertices
    public static boolean isInside(Vector[] polygon, int n, Vector p)
    {
        // There must be at least 3 vertices in polygon[]
        if (n < 3)
        {
            return false;
        }
 
        // Create a point for line segment from p to infinite
        Vector extreme = new Vector(INF, p.getBlockY(), 0);
 
        // Count intersections of the above line
        // with sides of polygon
        int count = 0, i = 0;
        do
        {
            int next = (i + 1) % n;
 
            // Check if the line segment from 'p' to
            // 'extreme' intersects with the line
            // segment from 'polygon[i]' to 'polygon[next]'
            if (doIntersect(polygon[i], polygon[next], p, extreme))
            {
                // If the point 'p' is collinear with line
                // segment 'i-next', then check if it lies
                // on segment. If it lies, return true, otherwise false
                if (orientation(polygon[i], p, polygon[next]) == 0) //玩家xz是否与墙xz相交
                {
                    return onSegment(polygon[i], p,
                                    polygon[next]);
                }
 
                count++;
            }
            i = next;
        } while (i != 0);
 
        // Return true if count is odd, false otherwise
        return (count % 2 == 1); // Same as (count%2 == 1)
    }

}