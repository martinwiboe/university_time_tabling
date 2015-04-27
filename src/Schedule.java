public class Schedule {
    public int[][][] assignments; //The array of scheduled courses, look up using [day][period][room],Value is -1 if the room is empty

	public Schedule(int days, int periods, int rooms) {
		assignments = new int[days][periods][rooms];

		// initialize all assignments to -1, meaning no course
		for (int d = 0; d < assignments.length; d++) {
			for (int p = 0; p < assignments[d].length; p++) {
				for (int r = 0; r < assignments[d][p].length; r++) {
					assignments[d][p][r] = -1;
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int d = 0; d < assignments.length; d++) {
			for (int p = 0; p < assignments[d].length; p++) {
				for (int r = 0; r < assignments[d][p].length; r++) {
					int course = assignments[d][p][r];
					if (course == -1)
						continue;

					builder.append(String.format("C%04d %d %d R%04d\n", course, d, p, r));
				}
			}
		}
		return builder.toString();
	}
}
