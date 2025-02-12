package org.emau.icmvc.ganimed.ttp.psn.generator;

import java.io.Serial;

import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;

/**
 * Extends CheckDigits to be a common base for all algorithms handling a single check digit
 */
public abstract class CheckDigit extends CheckDigits
{
	@Serial
	private static final long serialVersionUID = -8607877005595426166L;

	protected CheckDigit()
	{
	}

	protected CheckDigit(Alphabet alphabet)
	{
		super(alphabet);
	}

	@Override
	public String generateCheckDigits(String message) throws CharNotInAlphabetException
	{
		logger.trace("add check digits for message '{}'", message);
		return "" + calculateCheckDigit(message);
	}

	@Override
	public void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException
	{
		logger.debug("check message '{}'", value);
		if (value.length() != messageLength + 1)
		{
			String message = "invalid value '" + value + "' - it should have a length of " + (messageLength + 1);
			logger.info(message);
			throw new InvalidPSNException(message);
		}
		// letztes zeichen des gegebenen strings und neu berechnetes pruefzeichen
		int lastPos = value.length() - 1;
		if (value.charAt(lastPos) != calculateCheckDigit(value.substring(0, lastPos)))
		{
			String message = "invalid check digits for '" + value + "'";
			logger.info(message);
			throw new InvalidPSNException(message);
		}
	}

	@Override
	public int getNumberOfCheckDigits(int messageLength)
	{
		return 1;
	}

	@Override
	public int getEffectiveMaxMessageLength(int total)
	{
		return Math.min(getMaxMessageLength(), total) - 1;
	}

	protected abstract char calculateCheckDigit(String message) throws CharNotInAlphabetException;
}
