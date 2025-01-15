package ai.timefold.solver.core.impl.score.stream.bavet.common.tuple;

public final class UniversalTuple<A, B, C, D> implements QuadTuple<A, B, C, D> {

    private final TupleStore store;

    // Only a tuple's origin node may modify a fact.
    private A factA;
    private B factB;
    private C factC;
    private D factD;

    private TupleState state = TupleState.DEAD; // It's the node's job to mark a new tuple as CREATING.

    public UniversalTuple(A factA, int storeSize) {
        this.store = storeSize < 2 ? new SingleItemTupleStore() : new ArrayBackedTupleStore(storeSize);
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
        return store.get(index);
    }

    @Override
    public void setStore(int index, Object value) {
        store.set(index, value);
    }

    @Override
    public <Value_> Value_ removeStore(int index) {
        return store.remove(index);
    }

    @Override
    public String toString() {
        return "{" + factA + ", " + factB + ", " + factC + ", " + factD + "}";
    }

}
