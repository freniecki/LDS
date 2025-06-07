package pl.frot.fuzzy.base;

import lombok.Getter;

import java.util.List;

/**
 * Gaussian (bell-shaped) membership function.
 * μ(x) = exp(-0.5 * ((x - center) / sigma)²)
 *
 * Parameters:
 * - center (c): peak of the function where μ(c) = 1
 * - sigma (σ): controls the width/spread of the bell curve
 */
@Getter
public class GaussianFunction implements MembershipFunction<Double> {

    // Getters for testing and debugging
    private final double center;
    private final double sigma;

    /**
     * Constructor from parameter list
     * @param params List containing [center, sigma]
     */
    public GaussianFunction(List<Double> params) {
        if (params.size() != 2) {
            throw new IllegalArgumentException("Gaussian function requires exactly 2 parameters: [center, sigma]");
        }
        this.center = params.get(0);
        this.sigma = params.get(1);

        if (sigma <= 0) {
            throw new IllegalArgumentException("Sigma must be positive");
        }
    }

    /**
     * Direct constructor
     * @param center Peak of the gaussian curve
     * @param sigma Standard deviation (width parameter)
     */
    public GaussianFunction(double center, double sigma) {
        if (sigma <= 0) {
            throw new IllegalArgumentException("Sigma must be positive");
        }
        this.center = center;
        this.sigma = sigma;
    }

    @Override
    public double apply(Double x) {
        double normalized = (x - center) / sigma;
        return Math.exp(-0.5 * normalized * normalized);
    }

}