package io.github.bridge_noob.strayarmor;

/** Per-render armor tint, kept on the render thread. */
public final class TintContext {

    private static final ThreadLocal<int[]> STACK = ThreadLocal.withInitial(() -> new int[8]);
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Boolean> REPLACE_BASE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private TintContext() {
    }

    public static void push(int tint) {
        push(tint, false);
    }

    /**
     * Pushes a render tint. When {@code replaceBase} is true the tint is treated
     * as a final color (vivid/solid) and overwrites the base color; when false it
     * is a tint factor that gets multiplied into the base (multiply mode).
     */
    public static void push(int tint, boolean replaceBase) {
        REPLACE_BASE.set(replaceBase);
        int depth = DEPTH.get();
        int[] stack = STACK.get();
        if (depth >= stack.length) {
            int[] grown = new int[stack.length * 2];
            System.arraycopy(stack, 0, grown, 0, stack.length);
            stack = grown;
            STACK.set(stack);
        }
        stack[depth] = tint;
        DEPTH.set(depth + 1);
    }

    public static void pop() {
        DEPTH.set(Math.max(0, DEPTH.get() - 1));
        REPLACE_BASE.set(false);
    }

    public static int current() {
        int depth = DEPTH.get();
        return depth > 0 ? STACK.get()[depth - 1] : 0;
    }

    public static int apply(int baseColor) {
        int tint = current();
        if (tint == 0) return baseColor;
        if (REPLACE_BASE.get()) return tint;
        return ColorUtil.multiply(baseColor, tint);
    }
}
