package com.basefinder.model;

import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One result emitted by a scanner. Immutable. Built via {@link #builder}.
 * {@code signalWeight} is the contribution this finding makes when the fusion
 * manager clusters nearby findings into a {@link BaseCandidate}.
 */
public final class Finding {

    private final String scannerId;
    private final FindingCategory category;
    private final Vec3i pos;
    private final Vec3d precisePos;
    private final int confidence;
    private final double signalWeight;
    private final Severity severity;
    private final VerificationStatus verification;
    private final String label;
    private final String detail;
    private final String source;
    private final long observedAtTick;
    private final Map<String, String> metadata;

    private Finding(Builder b) {
        this.scannerId = b.scannerId;
        this.category = b.category;
        this.pos = b.pos;
        this.precisePos = b.precisePos != null ? b.precisePos
                : new Vec3d(b.pos.x() + 0.5, b.pos.y() + 0.5, b.pos.z() + 0.5);
        this.confidence = Confidence.clamp(b.confidence);
        this.signalWeight = b.signalWeight;
        this.severity = b.severity;
        this.verification = b.verification;
        this.label = b.label;
        this.detail = b.detail;
        this.source = b.source;
        this.observedAtTick = b.observedAtTick;
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(b.metadata));
    }

    public String scannerId() {
        return scannerId;
    }

    public FindingCategory category() {
        return category;
    }

    public Vec3i pos() {
        return pos;
    }

    public Vec3d precisePos() {
        return precisePos;
    }

    public int confidence() {
        return confidence;
    }

    public double signalWeight() {
        return signalWeight;
    }

    public Severity severity() {
        return severity;
    }

    public VerificationStatus verification() {
        return verification;
    }

    public String label() {
        return label;
    }

    public String detail() {
        return detail;
    }

    public String source() {
        return source;
    }

    public long observedAtTick() {
        return observedAtTick;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public Builder toBuilder() {
        return new Builder(scannerId, category, pos)
                .precisePos(precisePos).confidence(confidence).signalWeight(signalWeight)
                .severity(severity).verification(verification).label(label)
                .detail(detail).source(source).observedAtTick(observedAtTick)
                .metadataAll(metadata);
    }

    public static Builder builder(String scannerId, FindingCategory category, Vec3i pos) {
        return new Builder(scannerId, category, pos);
    }

    @Override
    public String toString() {
        return "Finding[" + scannerId + " " + category + " @" + pos + " " + confidence + "%]";
    }

    /** Fluent builder with sensible defaults. */
    public static final class Builder {
        private final String scannerId;
        private final FindingCategory category;
        private final Vec3i pos;
        private Vec3d precisePos;
        private int confidence = 50;
        private double signalWeight = 1.0;
        private Severity severity = Severity.LOW;
        private VerificationStatus verification = VerificationStatus.HEURISTIC;
        private String label = "";
        private String detail = "";
        private String source = "";
        private long observedAtTick;
        private final Map<String, String> metadata = new LinkedHashMap<>();

        private Builder(String scannerId, FindingCategory category, Vec3i pos) {
            this.scannerId = scannerId;
            this.category = category;
            this.pos = pos;
            this.label = category.label();
        }

        public Builder precisePos(Vec3d p) {
            this.precisePos = p;
            return this;
        }

        public Builder confidence(int c) {
            this.confidence = c;
            return this;
        }

        public Builder signalWeight(double w) {
            this.signalWeight = w;
            return this;
        }

        public Builder severity(Severity s) {
            this.severity = s;
            return this;
        }

        public Builder verification(VerificationStatus v) {
            this.verification = v;
            return this;
        }

        public Builder label(String l) {
            this.label = l;
            return this;
        }

        public Builder detail(String d) {
            this.detail = d;
            return this;
        }

        public Builder source(String s) {
            this.source = s;
            return this;
        }

        public Builder observedAtTick(long t) {
            this.observedAtTick = t;
            return this;
        }

        public Builder meta(String k, String v) {
            this.metadata.put(k, v);
            return this;
        }

        public Builder metadataAll(Map<String, String> m) {
            this.metadata.putAll(m);
            return this;
        }

        public Finding build() {
            return new Finding(this);
        }
    }
}
