package org.emau.icmvc.ganimed.ttp.psn.generator;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2024 Independent Trusted Third Party of the University Medicine Greifswald
 * 							kontakt-ths@uni-greifswald.de
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt
 * 							docker
 * 							r.schuldt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.utils.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.emau.icmvc.ttp.util.FlexibleToStringStyle.THS_TO_STRING_STYLE;

/**
 * An abstract common parent class for all check-digit-generators. Subclasses only have to provide implementations for
 * {@link #generateCheckDigits(String)}, {@link #check(String, int)}, an {@link #getAlphabetRestriction()}.
 * For single-digit generating algorithms there is another abstract subclass {@link CheckDigit} simplifying
 * further subclassing only leaving {@link CheckDigit#calculateCheckDigit(String)} to implement.
 * <p>
 * In order to add further implementations and making them actually visible by the system,
 * a few other things (besides the actual implementation) must be taken into account.
 * Their class names must be added in (or registered with)
 * <ul>
 *   <li><a href="https://git.icm.med.uni-greifswald.de/ths/gpas/blob/master/gpas-web/src/main/java/org/emau/icmvc/ttp/psn/frontend/util/Generators.java">
 *       gpas-web/src/main/java/org/emau/icmvc/ttp/psn/frontend/util/Generators.java</a> and respected at its usages,</li>
 *   <li><a href="https://git.icm.med.uni-greifswald.de/ths/gpas/blob/master/gpas-web/src/main/resources/classPathReference.properties#L59">
 *       gpas-web/src/main/resources/classPathReference.properties:59:generator_class</a>,</li>
 *   <li><a href="https://git.icm.med.uni-greifswald.de/ths/gpas/blob/master/gpas-web/src/main/resources/generatorClasses.properties">
 *       gpas-web/src/main/resources/generatorClasses.properties</a>,</li>
 *   <li><a href="https://git.icm.med.uni-greifswald.de/ths/gpas/blob/master/gpas-web/src/main/resources/messages_de.properties">
 *       gpas-web/src/main/resources/messages_de.properties</a> with prefix <code>domain.checkDigitGenerator</code>, and</li>
 *   <li><a href="https://git.icm.med.uni-greifswald.de/ths/gpas/blob/master/gpas-web/src/main/resources/messages_en.properties">
 *       gpas-web/src/main/resources/messages_en.properties</a> with prefix <code>domain.checkDigitGenerator</code>.</li>
 * </ul>
 * </p>
 */
public abstract class CheckDigits implements Serializable
{
	@Serial
	private static final long serialVersionUID = -3868310985726157577L;

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	private final Alphabet alphabet;

	protected CheckDigits()
	{
		this(null);
	}

	protected CheckDigits(Alphabet alphabet)
	{
		this.alphabet = alphabet;
	}

	/**
	 * {@return the restriction for the number of chars within the alphabet}
	 */
	public abstract GeneratorAlphabetRestriction getAlphabetRestriction();

	/**
	 * @param message
	 *            string for which check digits should be generated
	 * @return check digits
	 * @throws CharNotInAlphabetException if the message contains characters which are not contained in the alphabet
	 * @throws InvalidParameterException if the message is not valid for other reasons
	 */
	public abstract String generateCheckDigits(String message) throws CharNotInAlphabetException, InvalidParameterException;

	/**
	 * @param value
	 *            string (including check digits) to be checked
	 * @param messageLength
	 *            length of [value] without check digits
	 * @throws CharNotInAlphabetException if the value contains characters which are not contained in the alphabet
	 * @throws InvalidPSNException if the value is not a valid pseudonym
	 */
	public abstract void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException;

	/**
	 * {@return the theoretic maximum length of messages this algorithm is able to compute check digits for
	 * (but at most {@link DomainConfig#getMaxNumberOfCharactersInFullyQualifiedPsn()})}
	 */
	public int getMaxMessageLength()
	{
		return DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn();
	}
	/**
	 * {@return the alphabet for this instance}
	 */
	public Alphabet getAlphabet()
	{
		return alphabet;
	}

	/**
	 * {@return the number of check digits computed by this algorithm for a message with a given length}
	 * @param messageLength the length of the message
	 */
	public abstract int getNumberOfCheckDigits(int messageLength);

	/**
	 * {@return the maximum length of messages for which the resulting value including check digits
	 * is not longer than {@link DomainConfig#getMaxNumberOfCharactersInFullyQualifiedPsn()}}
	 */
	public final int getEffectiveMaxMessageLength()
	{
		return getEffectiveMaxMessageLength(DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn());
	}

	/**
	 * {@return the maximum length of messages for which the resulting value including check digits
	 * is not longer than the given total}
	 */
	public int getEffectiveMaxMessageLength(int total)
	{
		int effMaxMessageLength = getMaxMessageLength(); // may be smaller than 255
		while (effMaxMessageLength + getNumberOfCheckDigits(effMaxMessageLength) > total)
		{
			effMaxMessageLength--;
			if (effMaxMessageLength == 0)
			{
				return 0; // for safety to avoid endless loops for badly implemented 'getNumberOfCheckDigits()' methods
			}
		}
		return effMaxMessageLength;
	}

	protected int[] toNumber(String message) throws CharNotInAlphabetException
	{
		int length = message.length();
		int[] number = new int[length];

		for (int i = 0; i < length; i++)
		{
			number[i] = getAlphabet().getPosForSymbol(message.charAt(i));
		}
		return number;
	}

	protected String toMessage(int[] number)
	{
		int length = number.length;
		char[] message = new char[length];

		for (int i = 0; i < length; i++)
		{
			message[i] = getAlphabet().getSymbol(number[i]);
		}
		return new String(message);
	}

	/**
	 * {@return true if this algorithm is deprecated and should not be used for new domains}
	 * Creating new domains with deprecated check digit algorithms is not permitted and leads to error messages.
	 * The default implementation returns false.
	 */
	public boolean isDeprecated()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, THS_TO_STRING_STYLE)
				.append("alphabet", alphabet)
				.toString();
	}

	/**
	 * {@return true if the given string describes a deprecated algorithm}
	 * Unknown or invalid algorithms are considered to be deprecated, too.
	 * @param checkDigitAlgorithm the algorithm class name
	 *
	 */
	public static boolean isDeprecated(String checkDigitAlgorithm)
	{
		try
		{
			return CheckDigits.createCheckDigits(checkDigitAlgorithm).isDeprecated();
		}
		catch (InvalidCheckDigitClassException e)
		{
			return true;
		}
	}

	/**
	 * Creates a CheckDigits class for a class name
	 * @param checkDigitClassName the name of the class
	 * @return the class for the name
	 * @throws InvalidCheckDigitClassException when the class name does not denote a visible CkeckDigits class
	 */
	public static Class<? extends CheckDigits> createCheckDigitClass(String checkDigitClassName) throws InvalidCheckDigitClassException
	{
		if (checkDigitClassName == null)
		{
			throw new InvalidCheckDigitClassException("check digits class name must not be null");
		}
		try
		{
			return Class.forName(checkDigitClassName).asSubclass(CheckDigits.class);
		}
		catch (ClassNotFoundException e)
		{
			throw new InvalidCheckDigitClassException("exception while loading check digits class " + checkDigitClassName, e);
		}
	}

	/**
	 * Creates a skeleton CheckDigits instance for a class name without alphabet and domain config.
	 * @param checkDigitClassName the name of the class
	 * @return the instance
	 * @throws InvalidCheckDigitClassException when the class name does not denote a visible CkeckDigits class
	 */
	public static CheckDigits createCheckDigits(String checkDigitClassName)
			throws InvalidCheckDigitClassException
	{
		String cacheKey = createCacheKey(checkDigitClassName);
		CheckDigits checkDigits = cachedCheckDigits.get(cacheKey);
		if (checkDigits != null)
		{
			return checkDigits;
		}
		try
		{
			Class<? extends CheckDigits> checkDigitClass = createCheckDigitClass(checkDigitClassName);
			Constructor<? extends CheckDigits> constructor = checkDigitClass.getConstructor();
			checkDigits = constructor.newInstance();
			cachedCheckDigits.put(cacheKey, checkDigits);
			return checkDigits;
		}
		catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			String message = "can't create temp instance of check digit class '" + checkDigitClassName + "': " + e.getMessage();
			throw new InvalidCheckDigitClassException(message, e);
		}
	}

	/**
	 * Creates a CheckDigits instance for a class name with alphabet and domain config.
	 * @param checkDigitClassName the name of the class
	 * @param alphabet the alphabet
	 * @param config the domain config
	 * @return the instance
	 * @throws InvalidCheckDigitClassException if the class name does not denote a visible CheckDigits class
	 * @throws InvalidGeneratorException if the CheckDigits algorithm is not compatible with the alphabet
	 */
	public static CheckDigits createCheckDigits(String checkDigitClassName, Alphabet alphabet, DomainConfig config)
			throws InvalidCheckDigitClassException, InvalidGeneratorException
	{
		return createCheckDigits(createCheckDigitClass(checkDigitClassName), alphabet, config);
	}

	/**
	 * Creates a CheckDigits instance for a given class with alphabet and domain config.
	 * @param checkDigitClass the class
	 * @param alphabet the alphabet
	 * @param config the domain config
	 * @return the instance
	 * @throws InvalidGeneratorException if the CheckDigits algorithm is not compatible with the alphabet
	 */
	public static CheckDigits createCheckDigits(Class<? extends CheckDigits> checkDigitClass, Alphabet alphabet, DomainConfig config)
			throws InvalidGeneratorException
	{
		// Attention: here we assume that all implementations of CheckDigits only use maxDetectedErrors from the config,
		// which currently is true but might change in the future
		String cacheKey = createCacheKey(checkDigitClass.getName(), alphabet, config);
		CheckDigits checkDigits = cachedCheckDigits.get(cacheKey);
		if (checkDigits != null)
		{
			return checkDigits;
		}
		try
		{
			Constructor<? extends CheckDigits> constructor = checkDigitClass.getConstructor(Alphabet.class, DomainConfig.class);
			checkDigits = constructor.newInstance(alphabet, config);
			cachedCheckDigits.put(cacheKey, checkDigits);
			return checkDigits;
		}
		catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e)
		{
			String message = "can't create generator for class " + checkDigitClass + " with alphabet " + alphabet + " and properties " + config;
			throw new InvalidGeneratorException(message, e);
		}
	}

	private static String createCacheKey(String checkDigitClassName)
	{
		return createCacheKey(checkDigitClassName, null, null);
	}

	private static String createCacheKey(String checkDigitClassName, Alphabet alphabet, DomainConfig config)
	{
		return checkDigitClassName + "|" + (alphabet != null ? alphabet.toAlphabetString() : "") + "|" + (config != null ? config.getMaxDetectedErrors() : "");
	}

	private static final Map<String, CheckDigits> cachedCheckDigits = new LRUCache<>(100);
}
