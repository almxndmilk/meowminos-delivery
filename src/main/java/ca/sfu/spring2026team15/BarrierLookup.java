package ca.sfu.spring2026team15;

@FunctionalInterface
public interface BarrierLookup {
    boolean isBarrier(float worldX, float worldY);
}
