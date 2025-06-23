package com.ridingmate.api_server.global.util;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class PolylineDecoder {

    public static List<Coordinate> decode(String encoded) {
        List<Coordinate> coordinates = new ArrayList<>();

        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < encoded.length()) {
            int[] resultLat = decodeValue(encoded, index);
            lat += resultLat[0];
            index = resultLat[1];

            int[] resultLng = decodeValue(encoded, index);
            lng += resultLng[0];
            index = resultLng[1];

            coordinates.add(new Coordinate(lng / 1e5, lat / 1e5));
        }

        return coordinates;
    }

    private static int[] decodeValue(String encoded, int startIndex) {
        int result = 0;
        int shift = 0;
        int index = startIndex;

        while (true) {
            int b = encoded.charAt(index++) - 63;
            result |= (b & 0x1F) << shift;
            shift += 5;
            if (b < 0x20) break;
        }

        int delta = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
        return new int[]{delta, index};
    }
}