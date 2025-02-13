package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Immutable snapshot of an Info metric.
 */
public final class InfoSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link InfoSnapshot}, you can either call the constructor directly or use
     * the builder with {@link InfoSnapshot#builder()}.
     *
     * @param metadata the metric name in metadata must not include the {@code _info} suffix.
     *                 See {@link MetricMetadata} for more naming conventions.
     *                 The metadata must not have a unit.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public InfoSnapshot(MetricMetadata metadata, Collection<InfoDataPointSnapshot> data) {
        super(metadata, data);
        if (metadata.hasUnit()) {
            throw new IllegalArgumentException("An Info metric cannot have a unit.");
        }
    }

    @Override
    public List<InfoDataPointSnapshot> getDataPoints() {
        return (List<InfoDataPointSnapshot>) dataPoints;
    }

    @Override
    public InfoSnapshot merge(MetricSnapshot snapshot) {
        if (!(getMetadata().equals(snapshot.getMetadata())))
            throw new IllegalArgumentException("Unable to merge - metadata mismatch.");
        if (snapshot instanceof InfoSnapshot s) {
            var result = new ArrayList<InfoSnapshot.InfoDataPointSnapshot>();
            result.addAll(this.getDataPoints());
            result.addAll(s.getDataPoints());
            return new InfoSnapshot(getMetadata(), result);
        } else {
            throw new IllegalArgumentException("Unable to merge - invalid snapshot type");
        }
    }

    @Override
    public InfoSnapshot withNamePrefix(String prefix) {
        return new InfoSnapshot(getMetadata().withNamePrefix(prefix), getDataPoints());
    }

    /** Merge additional labels to all the data points. */
    public MetricSnapshot withLabels(Labels labels) {
        var points = getDataPoints()
                .stream()
                .map(point -> new InfoDataPointSnapshot(point.getLabels().merge(labels), point.getScrapeTimestampMillis()))
                .collect(Collectors.toList());
        return new InfoSnapshot(getMetadata(), points);
    }


    public static class InfoDataPointSnapshot extends DataPointSnapshot {

        /**
         * To create a new {@link InfoDataPointSnapshot}, you can either call the constructor directly or use the
         * Builder with {@link InfoDataPointSnapshot#builder()}.
         *
         * @param labels must not be null. Use {@link Labels#EMPTY} if there are no labels.
         */
        public InfoDataPointSnapshot(Labels labels) {
            this(labels, 0L);
        }

        /**
         * Constructor with an additional scrape timestamp.
         * This is only useful in rare cases as the scrape timestamp is usually set by the Prometheus server
         * during scraping. Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public InfoDataPointSnapshot(Labels labels, long scrapeTimestampMillis) {
            super(labels, 0L, scrapeTimestampMillis);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends DataPointSnapshot.Builder<Builder> {

            private Builder() {
            }

            public InfoDataPointSnapshot build() {
                return new InfoDataPointSnapshot(labels, scrapeTimestampMillis);
            }

            @Override
            protected Builder self() {
                return this;
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<InfoDataPointSnapshot> dataPoints = new ArrayList<>();

        private Builder() {
        }

        /**
         * Add a data point. Call multiple times for adding multiple data points.
         */
        public Builder dataPoint(InfoDataPointSnapshot dataPoint) {
            dataPoints.add(dataPoint);
            return this;
        }

        @Override
        public Builder unit(Unit unit) {
            throw new IllegalArgumentException("Info metric cannot have a unit.");
        }

        public InfoSnapshot build() {
            return new InfoSnapshot(buildMetadata(), dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
