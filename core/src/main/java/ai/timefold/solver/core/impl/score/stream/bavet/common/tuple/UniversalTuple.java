package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

public final class UniversalTuple<A, B, C, D> implements QuadTuple<A, B, C, D> {

    // Only a tuple's origin node may modify a fact.
    private A factA;
    private B factB;
    private C factC;
    private D factD;

    /*
     * We create a lot of tuples, many of them having store size of 1.
     * If an array of size 1 was created for each such tuple, memory would be wasted and indirection created.
     * This trade-off of increased memory efficiency for marginally slower access time is proven beneficial.
     */
    private final boolean storeIsArray;

    private Object store;
    public TupleState state = TupleState.DEAD; // It's the node's job to mark a new tuple as CREATING.

    public UniversalTuple(A factA, int storeSize) {
        this.store = (storeSize < 2) ? null : new Object[storeSize];
        this.storeIsArray = store != null;
        this.setA(factA);
    }

    public UniversalTuple(A factA, B factB, int storeSize) {
        this(factA, storeSize);
        this.setB(factB);
    }

    public UniversalTuple(A factA, B factB, C factC, int storeSize) {
        this(factA, factB, storeSize);
        this.setC(factC);
    }

    public UniversalTuple(A factA, B factB, C factC, D factD, int storeSize) {
        this(factA, factB, factC, storeSize);
        this.setD(factD);
    }

    @Override
    public A getA() {
        return factA;
    }

    @Override
    public void setA(A a) {
        this.factA = a;
    }

    @Override
    public B getB() {
        return factB;
    }

    @Override
    public void setB(B b) {
        this.factB = b;
    }

    @Override
    public C getC() {
        return factC;
    }

    @Override
    public void setC(C c) {
        this.factC = c;
    }

    @Override
    public D getD() {
        return factD;
    }

    @Override
    public void setD(D d) {
        this.factD = d;
    }

    @Override
    public TupleState getState() {
        return state;
    }

    @Override
    public void setState(TupleState state) {
        this.state = state;
    }

    @Override
    public <Value_> Value_ getStore(int index) {
        return (Value_) (storeIsArray ? ((Object[]) store)[index] : store);
    }

    @Override
    public void setStore(int index, Object value) {
        if (storeIsArray) {
            ((Object[]) store)[index] = value;
        } else {
            store = value;
        }
    }

    @Override
    public <Value_> Value_ removeStore(int index) {
        Value_ value;
        if (storeIsArray) {
            Object[] array = (Object[]) store;
            value = (Value_) array[index];
            array[index] = null;
        } else {
            value = (Value_) store;
            store = null;
        }
        return value;
    }

    @Override
    public String toString() {
        return "{" + factA + ", " + factB + ", " + factC + ", " + factD + "}";
    }

}
