/*
 * Copyright (c) 2010-2021 Haifeng Li. All rights reserved.
 *
 * Smile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Smile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Smile.  If not, see <https://www.gnu.org/licenses/>.
 */

package smile.feature;

import smile.classification.ClassLabels;
import smile.math.MathEx;

/**
 * The signal-to-noise (S2N) metric ratio is a univariate feature ranking metric,
 * which can be used as a feature selection criterion for binary classification
 * problems. S2N is defined as |&mu;<sub>1</sub> - &mu;<sub>2</sub>| / (&sigma;<sub>1</sub> + &sigma;<sub>2</sub>),
 * where &mu;<sub>1</sub> and &mu;<sub>2</sub> are the mean value of the variable
 * in classes 1 and 2, respectively, and &sigma;<sub>1</sub> and &sigma;<sub>2</sub>
 * are the standard deviations of the variable in classes 1 and 2, respectively.
 * Clearly, features with larger S2N ratios are better for classification.
 * 
 * <h2>References</h2>
 * <ol>
 * <li> M. Shipp, et al. Diffuse large B-cell lymphoma outcome prediction by gene-expression profiling and supervised machine learning. Nature Medicine, 2002.</li>
 * </ol>
 * 
 * @author Haifeng Li
 */
public class SignalNoiseRatio implements FeatureRanking {
    @Override
    public double[] rank(double[][] x, int[] y) {
        return of(x, y);
    }

    /**
     * Univariate feature ranking. Note that this method actually does NOT rank
     * the features. It just returns the metric values of each feature. The
     * use can then rank and select features.
     *
     * @param x a n-by-p matrix of n instances with p features.
     * @param y class labels.
     * @return the signal noise ratio for each feature.
     */
    public static double[] of(double[][] x, int[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("The sizes of X and Y don't match: %d != %d", x.length, y.length));
        }

        ClassLabels codec = ClassLabels.fit(y);
        y = codec.y;

        if (codec.k != 2) {
            throw new IllegalArgumentException("SignalNoiseRatio is applicable only to binary class.");
        }

        int n1 = 0;
        for (int yi : y) {
            if (yi == 0) {
                n1++;
            } else if (yi != 1) {
                throw new IllegalArgumentException("Invalid class label: " + yi);
            }
        }

        int n = x.length;
        int n2 = n - n1;
        double[][] x1 = new double[n1][];
        double[][] x2 = new double[n2][];
        for (int i = 0, j = 0, k = 0; i < n; i++) {
            if (y[i] == 0) {
                x1[j++] = x[i];
            } else {
                x2[k++] = x[i];
            }
        }

        double[] mu1 = MathEx.colMeans(x1);
        double[] mu2 = MathEx.colMeans(x2);
        double[] sd1 = MathEx.colSds(x1);
        double[] sd2 = MathEx.colSds(x2);

        int p = mu1.length;
        double[] s2n = new double[p];
        for (int i = 0; i < p; i++) {
            s2n[i] = Math.abs(mu1[i] - mu2[i]) / (sd1[i] + sd2[i]);
        }
        return s2n;
    }
}
