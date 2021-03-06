package etomica.math;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Created by alex on 4/30/17.
 */
@State(Scope.Benchmark)
@Measurement(time = 100, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ErfcBenchmark {

    @Param({"0.0", "0.05", "2.0"})
    public double x;

    @Benchmark
    public double etomicaErfc() {
        return etomica.math.SpecialFunctions.erfc(x);
    }

    @Benchmark
    public double apacheErfc() {
        return org.apache.commons.math3.special.Erf.erfc(x);
    }

}
