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

package smile.imputation;

import smile.sort.QuickSort;

/**
 * Missing value imputation by k-nearest neighbors. The KNN-based method
 * selects instances similar to the instance of interest to impute
 * missing values. If we consider instance A that has one missing value on
 * attribute i, this method would find K other instances, which have a value
 * present on attribute 1, with values most similar (in term of some distance,
 * e.g. Euclidean distance) to A on other attributes without missing values.
 * The average of values on attribute i from the K nearest
 * neighbors is then used as an estimate for the missing value in instance A.
 * In the weighted average, the contribution of each instance is weighted by
 * similarity between it and instance A.
 *
 * @author Haifeng Li
 */
public class KNNImputation implements MissingValueImputation {

    /**
     * The number of neighbors used for imputation.
     */
    private final int k;

    /**
     * Constructor.
     * @param k the number of neighbors used for imputation.
     */
    public KNNImputation(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("Invalid number of nearest neighbors for imputation: " + k);
        }

        this.k = k;
    }

    @Override
    public void impute(double[][] data) throws MissingValueImputationException {
        int[] count = new int[data[0].length];
        for (int i = 0; i < data.length; i++) {
            int n = 0;
            for (int j = 0; j < data[i].length; j++) {
                if (Double.isNaN(data[i][j])) {
                    n++;
                    count[j]++;
                }
            }

            if (n == data[i].length) {
                throw new MissingValueImputationException("The whole row " + i + " is missing");
            }
        }

        for (int i = 0; i < data[0].length; i++) {
            if (count[i] == data.length) {
                throw new MissingValueImputationException("The whole column " + i + " is missing");
            }
        }

        double[] dist = new double[data.length];

        for (double[] x : data) {
            int missing = 0;
            for (double v : x) {
                if (Double.isNaN(v)) {
                    missing++;
                }
            }

            if (missing == 0)
                continue;

            for (int j = 0; j < data.length; j++) {
                double[] y = data[j];
                int n = 0;
                dist[j] = 0;
                for (int m = 0; m < x.length; m++) {
                    if (!Double.isNaN(x[m]) && !Double.isNaN(y[m])) {
                        n++;
                        double d = x[m] - y[m];
                        dist[j] += d * d;
                    }
                }

                if (n > (x.length - missing) / 2) {
                    dist[j] = x.length * dist[j] / n;
                } else {
                    dist[j] = Double.MAX_VALUE;
                }
            }

            double[][] dat = new double[data.length][];
            System.arraycopy(data, 0, dat, 0, data.length);

            QuickSort.sort(dist, dat);

            for (int j = 0; j < x.length; j++) {
                if (Double.isNaN(x[j])) {
                    x[j] = 0;
                    int n = 0;
                    for (int m = 0; n < k && m < dat.length; m++) {
                        if (!Double.isNaN(dat[m][j])) {
                            x[j] += dat[m][j];
                            n++;
                        }
                    }

                    x[j] /= n;
                }
            }
        }
    }
}
