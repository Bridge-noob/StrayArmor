package io.github.colusite.strayarmor;

/** Per-render armor tint, kept on the render thread. */
public final class TintContext {

    private static final ThreadLocal<int[]> STACK = ThreadLocal.withInitial(() -> new int[8]);
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private TintContext() {
    }

    public static void push(int tint) {
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
        int depth = DEPTH.get();
        if (depth > 0) DEPTH.set(depth - 1);
    }

    public static int current() {
        int depth = DEPTH.get();
        return depth > 0 ? STACK.get()[depth - 1] : 0;
    }

    public static int apply(int baseColor) {
        int tint = current();
        return tint == 0 ? baseColor : ColorUtil.multiply(baseColor, tint);
    }
}
