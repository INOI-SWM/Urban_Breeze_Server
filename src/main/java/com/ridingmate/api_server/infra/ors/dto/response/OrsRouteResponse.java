package com.ridingmate.api_server.infra.ors.dto.response;

import java.util.List;

public record OrsRouteResponse(
        String type,
        List<Feature> features,
        Metadata metadata
) {
    public record Feature(
            String type,
            List<Double> bbox,
            Properties properties,
            Geometry geometry
    ) {}

    public record Properties(
            double ascent,
            double descent,
            Summary summary,
            List<Segment> segments,
            List<Integer> way_points
    ) {}

    public record Summary(
            double distance,
            double duration
    ) {}

    public record Segment(
            double distance,
            double duration,
            List<Step> steps,
            double ascent,
            double descent
    ) {}

    public record Step(
            double distance,
            double duration,
            int type,
            String instruction,
            String name,
            List<Integer> way_points
    ) {}

    public record Geometry(
            String type,
            List<List<Double>> coordinates
    ) {}

    public record Metadata(
            String service,
            long timestamp,
            Query query,
            Engine engine
    ) {}

    public record Query(
            List<List<Double>> coordinates,
            String profile,
            String profileName,
            String format,
            boolean elevation
    ) {}

    public record Engine(
            String version,
            String build_date,
            String graph_date
    ) {}
}
