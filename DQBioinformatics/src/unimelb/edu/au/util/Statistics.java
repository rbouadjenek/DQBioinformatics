/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.util;

import java.util.Arrays;

/**
 *
 * @author mbouadjenek
 */
public class Statistics {

    public static double getArithmeticMean(double[] data) {
        double sum = 0.0;
        for (double a : data) {
            sum += a;
        }
        if (data.length != 0) {
            return sum / data.length;
        } else {
            return 0;
        }
    }

    public static double getVariance(double[] data) {
        double mean = getArithmeticMean(data);
        double tmp = 0.0;
        for (double a : data) {
            tmp += (mean - a) * (mean - a);
        }
        if (data.length != 0) {
            return tmp / data.length;
        } else {
            return 0;
        }
    }

    public static double getStdDev(double[] data) {
        return (double) Math.sqrt((double) getVariance(data));
    }

    public static double getMedian(double[] data) {
        Arrays.sort(data);
        if (data.length % 2 == 0) {
            return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2;
        } else {
            return data[data.length / 2];
        }
    }

    public static double getMinArray(double[] data) {
        double out = Double.MAX_VALUE;
        for (double d : data) {
            out = Math.min(out, d);
        }
        if (data.length != 0) {
            return out;
        } else {
            return 0.0;
        }
    }

    public static double getMaxArray(double[] data) {
        double out = Double.MIN_VALUE;
        for (double d : data) {
            out = Math.max(out, d);
        }
        if (data.length != 0) {
            return out;
        } else {
            return 0.0;
        }
    }

    public static double getSumArray(double[] data) {
        double out = 0.0;
        for (double d : data) {
            out += d;
        }
        return out;
    }

//    public static double getGeometricMean(double[] data) {
//        double prod = 1.0;
//        for (double a : data) {
//            prod *= a;
//        }
//        if (data.length != 0) {
//            return (double) Math.pow((double) prod, 1 / (double) data.length);
//        } else {
//            return 0;
//        }
//    }
    public static double getGeometricMean(double[] data) {
        if (data.length != 0) {
            double prod = 1.0;
            for (double a : data) {
                prod *= Math.pow((double) a, 1 / (double) data.length);
            }
            return (double) prod;
        } else {
            return 0;
        }
    }

    public static double getHarmonicMean(double[] data) {
        double sum = 0;
        for (double a : data) {
            sum += (1 / a);
        }

        if (sum != 0) {
            return (double) data.length / sum;
        } else {
            return 0;
        }

    }

    public static double getCoefficientVariation(double[] data) {
        double getArithmeticMean = getArithmeticMean(data);
        if (getArithmeticMean != 0) {
            return (double) getStdDev(data) / getArithmeticMean;
        } else {
            return 0;
        }

    }

    public static void main(String[] args) {
        // TODO code application logic here
//        double[] test = new double[]{67.35706313575304, 31.38526530048736, 51.775443096447106, 32.26713835700524, 50.385441009227854, 57.15124708568996, 42.129144002993456, 23.607013175953913, 22.433414472430805, 60.77918487860691, 42.940404123503214, 20.89694161204111, 64.24909149314573, 38.118937022546405, 25.442536257851433, 33.00239759358824, 20.89694161204111, 58.083882187722566, 46.48874225595458, 55.609912388830665, 55.65338322983076, 31.698133220182207, 59.82261460675467, 39.019078385588145, 37.36111345389728, 22.42874714534182, 20.89694161204111, 27.173156311222186, 48.696749916652294, 58.3087248628593, 52.093797133956414, 50.47172726881298, 56.77793408371814, 29.45033084609331, 27.173156311222186, 31.030708030387338, 20.89694161204111, 62.41168108435381, 22.42874714534182, 27.173156311222186, 62.91721190338716, 50.410699875987596, 43.154928741013634, 27.173156311222186, 25.442536257851433, 20.89694161204111, 57.3182253547886, 55.609912388830665, 57.763437098290396, 39.08955764980962, 43.154928741013634, 20.89694161204111, 59.82282619083612, 56.95418809432578, 29.45033084609331, 15.54447796148881, 58.88113518338888, 20.89694161204111, 59.74008804405778, 54.50792227458273, 38.204494274742515, 56.95418809432578, 14.638736168238392, 39.788652415530116, 24.75657789118936, 20.89694161204111, 82.8329567330605, 19.629766064341386, 54.50792227458273, 24.75657789118936, 58.785054072686705, 20.89694161204111, 25.050055005645213, 24.75657789118936, 27.20369163063969, 22.484953306922083, 54.8977206121899, 47.76748441952759, 42.129144002993456, 22.42874714534182, 13.45902349858019, 43.853011232420236, 20.89694161204111, 61.50730642644521, 69.58337635271656, 25.050055005645213, 24.75657789118936, 27.20369163063969, 22.484953306922083, 57.0245293103729, 27.173156311222186, 25.442536257851433, 20.89694161204111, 51.212385668245076, 59.2156705946076, 19.69323921998708, 56.77793408371814, 22.42874714534182, 29.45033084609331, 63.38164850313509, 22.42874714534182, 27.20369163063969, 22.484953306922083, 34.84630966020779, 39.08955764980962, 19.865949659652145, 56.0981675172278, 50.410699875987596, 27.173156311222186, 54.81508139415495, 27.173156311222186, 67.8490185046529, 16.852751932200515, 25.924996059889768, 48.04148309135188, 47.900138786263575, 29.45033084609331, 20.89694161204111, 25.442536257851433, 58.88113518338888, 45.33578538665349, 65.00988775705355, 47.900138786263575, 29.45033084609331, 42.033518502354035, 15.430737624476896, 58.88113518338888, 20.89694161204111, 63.38164850313509, 22.42874714534182, 27.20369163063969, 22.484953306922083, 27.173156311222186, 50.576216710193115, 13.411400464344181, 50.397464120820565, 22.42874714534182, 55.59155088863104, 57.01722227781247, 55.59508580522843, 20.98304100499881, 13.569230475181913, 58.10125991709397, 50.397464120820565, 22.42874714534182, 55.59155088863104, 57.01722227781247, 55.267222277183095, 69.70186297062536, 20.98304100499881, 42.65747071074399, 50.65801276318229, 29.45033084609331, 39.439241065860536, 15.492729659759542, 20.89694161204111, 19.629766064341386, 62.285057488042305, 52.73881747834997, 65.23010307384706, 27.746491296386548, 20.89694161204111, 32.18284460286523, 22.42874714534182, 20.89694161204111, 59.82261460675467, 39.019078385588145, 37.36111345389728, 15.380359138089522, 20.89694161204111, 27.173156311222186, 38.118937022546405, 25.442536257851433, 44.27072992806197, 13.411400464344181, 60.46868600230938, 56.42354042904689, 27.173156311222186, 30.39855113388642, 24.75657789118936, 29.45033084609331, 20.89694161204111, 27.173156311222186, 47.900138786263575, 29.45033084609331, 58.88113518338888, 20.98304100499881, 27.173156311222186, 24.75657789118936, 20.89694161204111, 16.852751932200515, 29.45033084609331, 27.173156311222186, 47.900138786263575, 29.45033084609331, 39.788652415530116, 24.75657789118936, 20.98304100499881, 22.955530075469643, 16.867981806452597, 64.2671078196586};
        double[] test2 = new double[]{0.01, 1};
//        System.out.println(getArithmeticMean(test));
//        System.out.println(getCoefficientVariation(test));
        System.out.println(getHarmonicMean(test2));
//        System.out.println(getMaxArray(test));
//        System.out.println(getMinArray(test));
//        System.out.println(getStdDev(test));
//        System.out.println(getSumArray(test));
//        System.out.println(getVariance(test));
//        System.out.println(log2(1068245678));
//        System.out.println(Math.log10(1068245678));
//
//        System.out.println(log2(10));
//        System.out.println(Math.log10(10));
//        double j = 0.02;
//        for (int i = 0; i < 25; i++) {
//            j *= 2;
//            System.out.println(j);
//        }

    }

    public static double log2(double x) {
        return (double) (Math.log(x) / Math.log(2));
    }
}
