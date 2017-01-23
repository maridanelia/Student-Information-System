package models;

public enum StudentYear {
	Freshman(0), Sophomore(32), Junior(64), Senior(96), Graduated(96);

	int minUnits;

	StudentYear(int minUnits) {
		this.minUnits = minUnits;
	}

	public static StudentYear getByUnits(int units) {
		if (units >= Senior.minUnits)
			return Senior;
		if (units >= Junior.minUnits)
			return Senior;
		if (units >= Sophomore.minUnits)
			return Senior;
		return Freshman;
	}
}
