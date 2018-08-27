//import com.sun.xml.internal.xsom.impl.scd.Iterators;
//import com.sun.xml.internal.xsom.impl.scd.Iterators;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /**
     * Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc.
     */
    private final Map<Long, Node> nodes = new LinkedHashMap<>();
    private final Map<Long, Edge> edges = new LinkedHashMap<>();
    private final Map<Long, Edge> validedges = new LinkedHashMap<>();
    Node last = null;
    Long nodeid = Long.valueOf(0);
    Long wayid = Long.valueOf(0);


    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */


    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */


    private void clean() {
        Iterator<Long> iter = nodes.keySet().iterator();
        while (iter.hasNext()) {
            Long nodesid = iter.next();
            if (nodes.get(nodesid).connect.isEmpty()) {
                iter.remove();
            }

        }

    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of id's of all vertices in the graph.
     */
    Node getnode(Long i) {
        return nodes.get(i);
    }
    Edge getedge(Long i) {
        return edges.get(i);
    }
    Edge getvalidedge(Long i) {
        return validedges.get(i);
    }
    Map<Long, Edge> getedges() {
        return edges;
    }
    Edge deleteedge(Long i) {
        return edges.remove(i);
    }
    void addvalidedge(Long i, Edge e) {
        validedges.put(i, e);
    }
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     *
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        Node n = nodes.get(v);
        List<Long> adnode = new LinkedList<>();
        if (n != null) {
            if (n.connect.isEmpty()) {
                return null;
            }
            for (Long i : n.connect) {
                Edge e = validedges.get(i);
                int position = e.vertices.indexOf(v);
                if (position - 1 >= 0) {
                    adnode.add(e.vertices.get(position - 1));
                }
                if (position + 1 < e.vsize) {
                    adnode.add(e.vertices.get(position + 1));
                }
            }
            return adnode;
        }
        return null;
    }


    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

<<<<<<< HEAD
    double distance(double lonV, double latV, double lonW, double latW) {
=======
    static double distance(double lonV, double latV, double lonW, double latW) {
>>>>>>> 8255d8bba90c59a901eb0560103ec9f5772fb650
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        double min = 1000000000;
        Long target = null;

        for (Long i : nodes.keySet()) {
            //Node currnode = nodes.get(i);
            double dis = distance(lon(i), lat(i), lon, lat);
            if (dis < min) {
                min = dis;
                target = i;
            }
        }
        return target;
    }


    /**
     * Gets the longitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        Node n = nodes.get(v);
        return n.lon;
    }

    /**
     * Gets the latitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        Node n = nodes.get(v);
        return n.lat;
    }

    void addNode(Long id, Node n) {
        nodes.put(id, n);
        //      System.out.println("I have added a new node");
    }

    void addEdge(Long id, Edge e) {
        edges.put(id, e);
    }

    void highway(Long id) {
        edges.get(id).highway = true;
    }

    boolean valid(Long id) {
        return edges.get(id).highway;
    }

    void addvertice(Long edgeid, Long nodesid) {
        Edge e = this.edges.get(edgeid);
        int p = e.vsize;
        e.vertices.add(p, nodesid);
        e.vsize = e.vsize + 1;
    }

    void putname(Long id, String name) {
        this.nodes.get(id).name = name;
    }

    class Node implements Comparable<Node> {
        Long id;
        int version;
        double lon;
        double lat;
        Set<String> k;
        Set<String> v;
        Set<Long> connect;
        String name;
        double distance;
        Long prev;
        boolean mark;

        public Node(Long id, String longitude, String latitude) {
            this.id = id;
            version = 1;
            lon = Double.parseDouble(longitude);
            lat = Double.parseDouble(latitude);
            k = new HashSet();
            v = new HashSet<>();
            connect = new HashSet<Long>(); //这个connect是可能连接的edge
            prev = null;
            mark = false;
        }

        @Override
        public int compareTo(Node n) {
            if (this.distance > n.distance) {
                return 1;
            } else if (this.distance == n.distance) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    class Edge {
        Long id;
        int version;
        List<Long> vertices;
        Set<String> k;
        Set<String> v;
        int speed;
        boolean highway;
        String name;
        int vsize;

        public Edge(Long id) {
            this.id = id;
            version = 1;
            k = new HashSet<>();
            v = new HashSet<>();
            vertices = new ArrayList<>();
            speed = 0;
            highway = false;
            name = null;
            vsize = 0;
        }

        void addvertice(Long myid) {
            this.vertices.add(myid);
        }
    }
}



