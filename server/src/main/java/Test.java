import java.util.Random;

public class Test {

	private static final int[] a = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int round = 2400000;
		int r = 0;

		int first = -1;
		int second = -1;
		int third = -1;

		int right = 0;
		while (r < round) {
			if (first != -1 && second != -1 && third != -1) {
				boolean result = a[first] < a[second] && a[second] < a[third];
				if (result) {
					right++;
				}
//				System.out.println("[" + r + "]---" + a[first] + "," + a[second] + "," + a[third] + "----" + result);
				first = second = third = -1;
				r++;
			}
			int rIndex = new Random().nextInt(a.length);

			if (rIndex == first || rIndex == second || rIndex == third) {
				continue;
			}

			if (first == -1) {
				first = rIndex;
			} else if (second == -1) {
				second = rIndex;
			} else if (third == -1) {
				third = rIndex;
			}
		}

		System.out.println("Probility:" + (right * 1.0 / round));
	}

}
