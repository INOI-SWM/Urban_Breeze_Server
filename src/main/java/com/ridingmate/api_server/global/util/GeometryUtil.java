package com.ridingmate.api_server.global.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;

public class GeometryUtil {

    private static final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    public static LineString polylineToLineString(String polyline) {
        List<Coordinate> coordinates = PolylineDecoder.decode(polyline);
        return factory.createLineString(coordinates.toArray(new Coordinate[0]));
    }

}
