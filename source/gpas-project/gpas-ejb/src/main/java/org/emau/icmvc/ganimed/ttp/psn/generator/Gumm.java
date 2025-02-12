package org.emau.icmvc.ganimed.ttp.psn.generator;

import java.io.Serial;

import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;

/**
 * A correct implementation of the algorithm for encoding of numbers to detect typing errors
 * using a check digit published by H.P. Gumm in 1985 (fixing the wrong implementation in {@link VerhoeffGumm}).
 *
 * <p>
 *     Refer to
 *  	<ul>
 *			<li><a href="https://www.mathematik.uni-marburg.de/~gumm/Papers/EncodingOfNumbers.pdf">Encoding of Numbers to Detect Typing Errors</a> or</li>
 *  		<li><a href="https://www.researchgate.net/publication/220684043_A_new_class_of_check-digit_methods_for_arbitrary_number_systems">A new class of check-digit methods for arbitrary number systems.</a></li>
 *  	</ul>
 * </p>
 *
 * In short, the implementation yields a check digit detecting <b>all single digit errors</b> and
 * <b>all errors arising from transposition of adjacent digits</b>. In contrast, {@link VerhoeffGumm} only recognizes
 * about 77% of all transposition errors.
 */
public class Gumm extends VerhoeffGumm
{
	@Serial
	private static final long serialVersionUID = -8101721431605959247L;

	/**
	 * Lookup table for the TAU-function (correcting {@link VerhoeffGumm#F}).
	 * Note the periodicity of 2 in the first 5 columns and the periodicity of 5 in the last 5 columns.
	 */
	protected static final int[][] TAU = new int[][] {
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
			{ 0, 4, 3, 2, 1, 8, 9, 5, 6, 7 },
			{ 0, 1, 2, 3, 4, 6, 7, 8, 9, 5 },
			{ 0, 4, 3, 2, 1, 9, 5, 6, 7, 8 },
			{ 0, 1, 2, 3, 4, 7, 8, 9, 5, 6 },
			{ 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 },
			{ 0, 1, 2, 3, 4, 8, 9, 5, 6, 7 },
			{ 0, 4, 3, 2, 1, 6, 7, 8, 9, 5 },
			{ 0, 1, 2, 3, 4, 9, 5, 6, 7, 8 },
			{ 0, 4, 3, 2, 1, 7, 8, 9, 5, 6 },
	};

	public Gumm()
	{
	}

	public Gumm(Alphabet alphabet, DomainConfig config) throws InvalidAlphabetException
	{
		super(alphabet, config);
	}

	/**
	 * This algorithm fixes the wrongly implemented {@link VerhoeffGumm} by correcting the faulty lookup table
	 * {@link VerhoeffGumm#F} with {@link #TAU} (the permutation function TAU original algorithm).
	 * So it actually enables detecting all individual typing errors and all swaps of neighbouring characters for any PSN.
	 * This algorithm is preferable to {@link VerhoeffGumm}.
	 * @return false
	 */
	@Override
	public boolean isDeprecated()
	{
		return false;
	}

	@Override
	protected char calculateCheckDigit(String message) throws CharNotInAlphabetException
	{
		return getAlphabet().getSymbol(checksum(toNumber(message))); // isCorrect(toNumber(message + checkDigit) -> TRUE
	}

	/**
	 * Computes a check digit for a number.
	 * @param number a number which is an array with the positions of the alphabet symbols
	 * @return the check digit
	 */
	protected int checksum(int[] number) {
		int length = number.length;
		int sum = 0;

		for (int i = 1; i <= length; i++) {
			int dig = number[length - i];
			int aux = TAU[i % 10][dig]; // tau(i, dig);
			sum = OP[aux][sum]; // add(aux, sum)
		}

		return INV[sum]; // inv(sum)
	}

	/**
	 * Checks a number.
	 *
	 * @param number the number including the check digit appended
	 * @return true if the number is correct
	 */
	protected boolean check(int[] number) {
		int length = number.length;
		int sum = 0;

		for (int i = 1; i <= length; i++) {
			int dig = number[length - i];
			int aux = TAU[(i - 1) % 10][dig]; // tau(i - 1, dig);
			sum = OP[aux][sum]; // add(aux, sum);
		}

		return sum == 0;
	}

	/**
	 * Addition in the dihedral group,
	 * used to produce the lookup table {@link #OP}.
	 *
	 * @param x the first summand
	 * @param y the second summand
	 * @return the sum in the dihedral group
	 */
	public static int add(int x, int y) {
		return x < 5 ?
				(y < 5 ? (x + y) % 5 : (x + y) % 5 + 5) :
				(y < 5 ? (x - y) % 5 + 5 : (x - y + 5) % 5);
	}

	/**
	 * {@return the inverse for the sum in the dihedral group,
	 * used to produce the lookup table {@link #INV}}
	 *
	 * @param x the element to invert
	 */
	public static int inv(int x) {
		return x < 5 ? (5 - x) % 5 : x;
	}

	/**
	 * {@return the permutation for an element x at position i,
	 * used to produce the lookup table {@link #TAU} (replacing {@link #F})}
	 *
	 * @param i the position
	 * @param x the elements value
	 */
	public static int tau(int i, int x) {
		if (x == 0)
		{
			return 0;
		}
		return x < 5 ?
				((i % 2 == 0) ? x : 5 - x) :
				((3 * i + x) % 5) + 5;
	}
}
