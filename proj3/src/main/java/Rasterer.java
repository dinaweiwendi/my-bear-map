import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private int depth;
    private MapServer sev;

    public Rasterer() {
        depth = 0;
        sev = new MapServer();
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        // System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        depth = 0;
        boolean querysuccess = true;
        Double userLrlon = params.get("lrlon"); //sev.ROOT_LRLON = -122.2119
        System.out.println(userLrlon);
        Double userUllon = params.get("ullon"); //sev.ROOT_ULLON= -122.2998
        System.out.println(userUllon);
        Double userUllat = params.get("ullat"); //sev.ROOT_ULLAT = 37.8912
        System.out.println(userUllat);
        Double userLrlat = params.get("lrlat"); //sev.ROOT_LRLAT = 37.8228
        System.out.println(userLrlat);
        Double width = params.get("w");
        Double height = params.get("h");
        Double userresolution = (-userUllon + userLrlon) / width;
        if (userUllon < sev.ROOT_ULLON || userUllon > sev.ROOT_LRLON
                || userLrlon < sev.ROOT_ULLON || userLrlon > sev.ROOT_LRLON) {
            querysuccess = false;
        }

        if (userUllat > sev.ROOT_ULLAT || userUllat < sev.ROOT_LRLAT
                || userLrlat > sev.ROOT_ULLAT || userLrlat < sev.ROOT_LRLAT) {
            querysuccess = false;
        }

        depth = caldepth(userresolution);


        double ullat = (caly(userUllat) - 1) / Math.pow(2, depth)
                * (sev.ROOT_LRLAT - sev.ROOT_ULLAT) + sev.ROOT_ULLAT;

        double lrlat = caly(userLrlat) / Math.pow(2, depth)
                * (sev.ROOT_LRLAT - sev.ROOT_ULLAT) + sev.ROOT_ULLAT;
        double ullon = calx(userUllon) / Math.pow(2, depth)
                * (sev.ROOT_LRLON - sev.ROOT_ULLON) + sev.ROOT_ULLON;
        double lrlon = (calx(userLrlon) + 1) / Math.pow(2, depth)
                * (sev.ROOT_LRLON - sev.ROOT_ULLON) + sev.ROOT_ULLON;


        int xstart = calx(userUllon);
        int xend = calx(userLrlon) + 1;
        int ystart = caly(userUllat) - 1;
        int yend = caly(userLrlat);
        System.out.println(xstart);
        System.out.println(xend);
        System.out.println(ystart);
        System.out.println(yend);
        String[][] grid = new String[yend - ystart][xend - xstart];
        for (int i = ystart; i < yend; i++) {
            for (int j = xstart; j < xend; j++) {
                String argu = "d" + depth + "_x" + j + "_y" + i + ".png";
                grid[i - ystart][j - xstart] = argu;
            }
        }
        results.put("raster_ul_lon", ullon);
        results.put("raster_ul_lat", ullat);
        results.put("raster_lr_lon", lrlon);
        results.put("raster_lr_lat", lrlat);
        results.put("query_success", querysuccess);
        results.put("render_grid", grid);
        results.put("depth", depth);

        return results;
    }

    private int caly(double p) {
        //y方向给出的是向下取整
        for (int i = 0; i < Math.pow(2, depth); i++) {
            if ((((sev.ROOT_ULLAT - sev.ROOT_LRLAT) * i
                    / Math.pow(2, depth)) + sev.ROOT_LRLAT) < p) {
                if ((((sev.ROOT_ULLAT - sev.ROOT_LRLAT) * (i + 1)
                        / Math.pow(2, depth)) + sev.ROOT_LRLAT) > p) {
                    return (int) (Math.pow(2, depth) - i);
                }
            }
        }
        return 0;
    }

    private int calx(double p) {
        //X方向给出的是向左取整
        for (int i = 0; i < Math.pow(2, depth); i++) {
            if ((sev.ROOT_LRLON - sev.ROOT_ULLON) * i
                    / Math.pow(2, depth) + sev.ROOT_ULLON < p) {
                if ((sev.ROOT_LRLON - sev.ROOT_ULLON) * (i + 1)
                        / Math.pow(2, depth) + sev.ROOT_ULLON > p) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int caldepth(Double userresolution) {
        Double curresove = (sev.ROOT_LRLON - sev.ROOT_ULLON) / 256;
        while (depth < 7 && curresove > userresolution) {
            curresove = curresove / 2.0;
            depth = depth + 1;
        }
        return depth;
    }

}
