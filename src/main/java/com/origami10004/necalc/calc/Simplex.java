package com.origami10004.necalc.calc;

import com.origami10004.necalc.Necalc;

import java.util.Arrays;

public class Simplex {
	private static final double EPSILON = 1e-9;
    private static final double INF     = Double.MAX_VALUE / 2;
    private static final int MAX_ITER = 100000;
	
	public static enum Status {OPTIMAL, UNBOUNDED, INFEASIBLE};

	public static class Result {
		public final Status status;
		public final double[] solution;
		public final double optimalValue;
		public Result(Status status, double[] solution, double optimalValue) {
			this.status = status;
			this.solution = solution;
			this.optimalValue = optimalValue;
		}
	}
	
	public static Result solve(double[][] A, double[] b, double[] c) {
		Necalc.logger.info("Before solve - b: " + Arrays.toString(b));
		int m = A.length;
		int n = A[0].length;
		
		// m constraints, 1 objective function
		// n variables, m slack variables, m artificial variables, 1 RHS column
		int total = n + m + m;
		double tableu[][] = new double[m + 1][total + 1];
		int[] basis = new int[m];

		for(int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				tableu[i][j] = A[i][j];
			}
			tableu[i][total] = b[i]; // RHS
			if (Math.abs(b[i]) < EPSILON) {
				tableu[i][n + i] = 1.0; // slack variable
				basis[i] = n + i;
			} else {
				tableu[i][n + i] = -1.0; // slack variable
				tableu[i][n + m + i] = 1.0; // artificial variable
				basis[i] = n + m + i; // artificial variable
			}
		}

		// first iteration, remove articial variables
		for (int i = n + m; i < total; i++) tableu[m][i] = 1.0;
		for (int i = 0; i < m; i++) {
			if (tableu[m][basis[i]] != 0) {
				addRow(tableu, m, i, -tableu[m][basis[i]], total);
			}
		}
		Necalc.logger.info("Tableau RHS after init: row0=" + tableu[0][total] + " row1=" + tableu[1][total]);
		Status run1 = runSimplex(tableu, basis, m, total);
		if (run1 == Status.UNBOUNDED) {
			// Really shouldnt be possible?? but whatever
			return new Result(Status.INFEASIBLE, null, 0);
		}
		if (Math.abs(tableu[m][total]) > EPSILON) {
			// no starting solution, infeasible
			return new Result(Status.INFEASIBLE, null, 0);
		}
		Necalc.logger.info("Phase 1 result: " + run1 + " obj=" + tableu[m][total]);
		Necalc.logger.info("Phase 1 objective row: " + Arrays.toString(tableu[m]));
		Necalc.logger.info("Phase 1 basis: " + Arrays.toString(basis));

		// second iteration, solve original problem
		for (int i = 0; i <= total; i++) tableu[m][i] = 0.0;
		for (int j = 0; j < n; j++) tableu[m][j] = c[j];
		for (int i = 0; i < m; i++) {
			if (basis[i] < n && tableu[m][basis[i]] != 0) {
				addRow(tableu, m, i, -tableu[m][basis[i]], total);
			}
		}
		Status run2 = runSimplex(tableu, basis, m, total);
		if (run2 == Status.UNBOUNDED) {
			return new Result(Status.UNBOUNDED, null, 0);
		}
		double[] x = new double[n];
		for (int i = 0; i < m; i++) {
			if (basis[i] < n) {
				x[basis[i]] = tableu[i][total];
			}
		}
		return new Result(Status.OPTIMAL, x, tableu[m][total]);
	}

	private static Status runSimplex(double[][] tableu, int[] basis, int m, int total) {
		for (int iter = 0; iter < MAX_ITER; iter++) {
			int enterCol = -1;
			double minCost = -EPSILON;
			for (int j = 0; j < total; j++) {
				if (tableu[m][j] < minCost) {
					minCost = tableu[m][j];
					enterCol = j;
				}
			}
			if (enterCol == -1) {
				// optimal solution found
				return Status.OPTIMAL;
			}
			int leaveRow = -1;
			double minRatio = INF;
			for (int i = 0; i < m; i++) {
				if (tableu[i][enterCol] > EPSILON) {
					double ratio = tableu[i][total] / tableu[i][enterCol];
					if (ratio < minRatio) {
						minRatio = ratio;
						leaveRow = i;
					}
				}
			}
			if (leaveRow == -1) {
				// unbounded solution
				return Status.UNBOUNDED;
			}
			pivot(tableu, basis, m, leaveRow, enterCol, total);
		}
		return Status.UNBOUNDED;
	}

	private static void addRow(double[][] tableu, int targetRow, int sourceRow, double factor, int total) {
		for (int j = 0; j <= total; j++) {
			tableu[targetRow][j] += tableu[sourceRow][j] * factor;
		}
	}
	private static void pivot(double[][] tableu, int[] basis, int m, int pivotRow, int pivotCol, int total) {
		double pivotVal = tableu[pivotRow][pivotCol];
		for (int i = 0; i <= total; i++) {
			tableu[pivotRow][i] /= pivotVal;
		}
		for (int i = 0; i <= m; i++) {
			if (i != pivotRow) {
				if (Math.abs(tableu[i][pivotCol]) < EPSILON) continue;
				addRow(tableu, i, pivotRow, -tableu[i][pivotCol], total);
			}
		}
		basis[pivotRow] = pivotCol;
	}
}
