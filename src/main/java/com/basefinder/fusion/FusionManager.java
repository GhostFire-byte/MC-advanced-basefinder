package com.basefinder.fusion;

import com.basefinder.engine.VillageContext;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Confidence;
import com.basefinder.model.EvidenceClass;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clusters raw findings into {@link BaseCandidate}s and fuses their confidences
 * with an <b>evidence-aware</b> model.
 *
 * <p>Each signal is tagged with an {@link EvidenceClass}. The fusion:
 * <ul>
 *   <li>Combines per-finding confidences with a probabilistic OR (noisy-or).</li>
 *   <li>Adds a <b>corroboration bonus</b> that scales with how many <em>distinct
 *       evidence classes</em> agree ("a lot of detection lines up").</li>
 *   <li>Requires <b>habitation</b> or a strong <b>marker</b> for a cluster to be a
 *       real base. A cluster of only activity/ambient evidence is classified as a
 *       {@link BaseCandidate.CandidateKind#MINING_AREA} / {@code ACTIVITY} and its
 *       confidence is capped, so strip mines and bare light trails don't read as bases.</li>
 *   <li>Applies <b>suppressors</b>: village context down-weights village-explainable clusters.</li>
 *   <li>Accrues <b>temporal persistence</b> across cycles; unobserved candidates decay.</li>
 * </ul>
 */
public final class FusionManager {

    public static final int CELL_SIZE = 24;
    private static final double PERSISTENCE_GAIN = 0.2;
    private static final double PERSISTENCE_DECAY = 0.34;
    private static final int MAX_PERSISTENCE_BONUS = 12;
    private static final int MAX_MISSED_CYCLES = 3;

    /** Confidence cap for clusters with no habitation/marker evidence. */
    private static final int ACTIVITY_ONLY_CAP = 55;
    /** Confidence cap for natural spawner chambers / dungeons (shown, but not as a base). */
    private static final int DUNGEON_CAP = 60;
    /** A spawner with at most this many chests (and nothing else) is a natural dungeon. */
    private static final int DUNGEON_MAX_CHESTS = 2;
    /** Up to this many sky-exposed containers, with nothing else, are explorer leftovers. */
    private static final int SURFACE_LEFTOVER_MAX = 2;
    /** Village suppression multiplier applied to village-explainable clusters. */
    private static final double VILLAGE_SUPPRESS = 0.45;

    /** Categories a village can plausibly explain (so they don't imply a player base). */
    private static final Set<FindingCategory> VILLAGE_SAFE = Set.of(
            FindingCategory.FARM, FindingCategory.DECORATION, FindingCategory.BED,
            FindingCategory.STRUCTURE, FindingCategory.ENTITY, FindingCategory.PLAYER,
            FindingCategory.STORAGE, FindingCategory.LIGHT, FindingCategory.LIGHTING);

    private final Map<String, BaseCandidate> candidates = new LinkedHashMap<>();
    private final Map<String, Integer> missedCycles = new LinkedHashMap<>();

    public synchronized List<BaseCandidate> update(List<Finding> findings, long tick) {
        return update(findings, tick, VillageContext.empty());
    }

    public synchronized List<BaseCandidate> update(List<Finding> findings, long tick, VillageContext village) {
        Map<String, List<Finding>> grouped = new LinkedHashMap<>();
        for (Finding f : findings) {
            grouped.computeIfAbsent(cellKey(f.pos()), k -> new ArrayList<>()).add(f);
        }

        for (Map.Entry<String, List<Finding>> e : grouped.entrySet()) {
            String key = e.getKey();
            List<Finding> cell = e.getValue();
            BaseCandidate c = candidates.get(key);
            if (c == null) {
                c = new BaseCandidate(key, centroid(cell), tick);
                candidates.put(key, c);
            } else {
                c.setCenter(centroid(cell));
            }
            c.replaceFindings(cell);
            c.setLastSeenTick(tick);
            c.incrementCycles();
            c.setPersistence(Math.min(1.0, c.persistence() + PERSISTENCE_GAIN));
            missedCycles.put(key, 0);

            Scored scored = score(cell, village, c.center());
            c.setKind(scored.kind);
            c.setBaseConfidence(scored.base);
            int bonus = (int) Math.round(c.persistence() * MAX_PERSISTENCE_BONUS);
            c.setFusedConfidence(Confidence.clamp(scored.base + bonus));
            c.setSeverity(severityFor(c.fusedConfidence(), scored.kind));
        }

        Iterator<Map.Entry<String, BaseCandidate>> it = candidates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BaseCandidate> entry = it.next();
            String key = entry.getKey();
            if (grouped.containsKey(key)) {
                continue;
            }
            int missed = missedCycles.getOrDefault(key, 0) + 1;
            missedCycles.put(key, missed);
            BaseCandidate c = entry.getValue();
            c.setPersistence(c.persistence() - PERSISTENCE_DECAY);
            if (missed >= MAX_MISSED_CYCLES || c.persistence() <= 0.0) {
                it.remove();
                missedCycles.remove(key);
            } else {
                int base = c.baseConfidence();
                int bonus = (int) Math.round(Math.max(0, c.persistence()) * MAX_PERSISTENCE_BONUS);
                c.setFusedConfidence(Confidence.clamp(base + bonus));
            }
        }

        return new ArrayList<>(candidates.values());
    }

    private record Scored(int base, BaseCandidate.CandidateKind kind) {
    }

    /** Evidence-aware scoring of one cluster. */
    private Scored score(List<Finding> cell, VillageContext village, Vec3i center) {
        double notAny = 1.0;
        EnumSet<EvidenceClass> classes = EnumSet.noneOf(EvidenceClass.class);
        EnumSet<FindingCategory> cats = EnumSet.noneOf(FindingCategory.class);
        int spawnerCount = 0;
        int trialSpawnerCount = 0;
        int chestCount = 0;
        int surfaceChestCount = 0;
        boolean hasStashAggregate = false;
        boolean otherBaseEvidence = false; // base-ish evidence beyond the spawner + its chests
        for (Finding f : cell) {
            double weighted = Math.min(1.0, (f.confidence() / 100.0) * Math.max(0.1, f.signalWeight()));
            notAny *= (1.0 - weighted);
            EvidenceClass ec = EvidenceClass.of(f.category());
            classes.add(ec);
            cats.add(f.category());

            if (f.category() == FindingCategory.SPAWNER) {
                spawnerCount++;
                if ("trial".equals(f.metadata().get("variant"))) {
                    trialSpawnerCount++;
                }
            } else if (f.category() == FindingCategory.STORAGE) {
                if (f.metadata().containsKey("count")) {
                    hasStashAggregate = true; // a dense stash is a real signal, not a leftover
                } else {
                    chestCount++;
                    if ("true".equals(f.metadata().get("surface"))) {
                        surfaceChestCount++;
                    }
                }
            } else if (ec == EvidenceClass.HABITATION || ec == EvidenceClass.MARKER
                    || ec == EvidenceClass.SECURITY) {
                otherBaseEvidence = true;
            }
        }
        int combined = Confidence.clamp((1.0 - notAny) * 100.0);

        // Corroboration: reward multiple DISTINCT evidence classes agreeing.
        int corroboration = Math.min(18, Math.max(0, classes.size() - 1) * 7);
        int score = Confidence.clamp(combined + corroboration);

        boolean habitation = classes.contains(EvidenceClass.HABITATION);
        boolean strongMarker = classes.contains(EvidenceClass.MARKER);
        boolean security = classes.contains(EvidenceClass.SECURITY);

        BaseCandidate.CandidateKind kind;
        boolean onlyStorage = cats.size() == 1 && cats.contains(FindingCategory.STORAGE);
        boolean surfaceLeftover = onlyStorage && !hasStashAggregate
                && chestCount >= 1 && chestCount <= SURFACE_LEFTOVER_MAX
                && surfaceChestCount == chestCount;

        if (surfaceLeftover) {
            // A couple of sky-exposed containers alone: explorer leftovers, not a
            // base. Hidden entirely (the same containers underground still flag).
            kind = BaseCandidate.CandidateKind.LEFTOVER;
            score = 0;
        } else if (isSpawnerChamber(spawnerCount, trialSpawnerCount, chestCount, otherBaseEvidence)) {
            // Natural monster-spawner dungeon (or trial chamber): NOT a player base.
            kind = BaseCandidate.CandidateKind.DUNGEON;
            score = Math.min(score, DUNGEON_CAP);
        } else if (habitation || strongMarker) {
            kind = BaseCandidate.CandidateKind.BASE;
            if (security) {
                score = Confidence.clamp(score + 5); // security corroborates an inhabited base
            }
        } else {
            // Only activity/ambient evidence: not a base. Classify and cap.
            boolean mining = cats.contains(FindingCategory.MINING_AREA)
                    || cats.contains(FindingCategory.PATH)
                    || cats.contains(FindingCategory.ACCESS_SHAFT);
            kind = mining ? BaseCandidate.CandidateKind.MINING_AREA : BaseCandidate.CandidateKind.ACTIVITY;
            score = Math.min(score, ACTIVITY_ONLY_CAP);
        }

        // Village suppression: down-weight clusters whose evidence a village fully explains.
        if (village != null && village.isVillage(center) && isVillageExplainable(cats)) {
            score = (int) Math.round(score * VILLAGE_SUPPRESS);
        }

        return new Scored(score, kind);
    }

    /**
     * Recognises a natural spawner chamber so it is not reported as a base:
     * <ul>
     *   <li>a single mob spawner with at most {@value #DUNGEON_MAX_CHESTS} chests
     *       (a vanilla dungeon), or</li>
     *   <li>any trial spawner (a trial chamber), with the same small-loot bound,</li>
     * </ul>
     * and in both cases NO other base-indicating evidence (beds, beacons, farms,
     * crafting, signs, markers, security, dense stashes). Multiple mob spawners are
     * deliberately NOT auto-suppressed.
     */
    private static boolean isSpawnerChamber(int spawnerCount, int trialSpawnerCount,
                                            int chestCount, boolean otherBaseEvidence) {
        if (spawnerCount == 0 || otherBaseEvidence || chestCount > DUNGEON_MAX_CHESTS) {
            return false;
        }
        boolean singleMobDungeon = trialSpawnerCount == 0 && spawnerCount == 1;
        boolean trialChamber = trialSpawnerCount >= 1;
        return singleMobDungeon || trialChamber;
    }

    /** True if every category in the cluster could plausibly come from a village. */
    private static boolean isVillageExplainable(EnumSet<FindingCategory> cats) {
        for (FindingCategory c : cats) {
            if (!VILLAGE_SAFE.contains(c)) {
                return false;
            }
        }
        return !cats.isEmpty();
    }

    private static Severity severityFor(int confidence, BaseCandidate.CandidateKind kind) {
        if (kind != BaseCandidate.CandidateKind.BASE) {
            return confidence >= 50 ? Severity.MEDIUM : Severity.LOW;
        }
        if (confidence >= 90) {
            return Severity.CRITICAL;
        }
        if (confidence >= 80) {
            return Severity.HIGH;
        }
        if (confidence >= 65) {
            return Severity.MEDIUM;
        }
        return Severity.LOW;
    }

    private static Vec3i centroid(List<Finding> cell) {
        long sx = 0, sy = 0, sz = 0;
        for (Finding f : cell) {
            sx += f.pos().x();
            sy += f.pos().y();
            sz += f.pos().z();
        }
        int n = cell.size();
        return new Vec3i((int) (sx / n), (int) (sy / n), (int) (sz / n));
    }

    private static String cellKey(Vec3i p) {
        return Math.floorDiv(p.x(), CELL_SIZE) + ":"
                + Math.floorDiv(p.y(), CELL_SIZE) + ":"
                + Math.floorDiv(p.z(), CELL_SIZE);
    }

    public synchronized List<BaseCandidate> candidates() {
        return new ArrayList<>(candidates.values());
    }

    public synchronized void reset() {
        candidates.clear();
        missedCycles.clear();
    }
}
