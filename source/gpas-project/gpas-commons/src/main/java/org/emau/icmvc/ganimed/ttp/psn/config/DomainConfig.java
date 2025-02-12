package org.emau.icmvc.ganimed.ttp.psn.config;

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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.psn.enums.ForceCache;
import org.emau.icmvc.ganimed.ttp.psn.enums.ValidateViaParents;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

public class DomainConfig implements Serializable
{
	@Serial
	private static final long serialVersionUID = -6342969280186789931L;
	public static final int DEFAULT_PSN_LENGTH = 8;
	public static final long MAX_PSEUDONYMS_FOR_DEFAULT_CACHE_ON = 1000000000; // 125 mb is needed for such a cache
	private static final String PROPERTY_DELIMITER = ";";
	private static final int DB_PSN_FIELD_LENGTH = 255;

	private int maxDetectedErrors = 2;
	private int psnLength = DEFAULT_PSN_LENGTH;
	private String psnPrefix = "";
	private String psnSuffix = "";
	private boolean includePrefixInCheckDigitCalculation = false;
	private boolean includeSuffixInCheckDigitCalculation = false;
	private int useLastCharAsDelimiterAfterXChars = 0;
	private boolean psnsDeletable = false;
	private boolean sendNotificationsWeb = false;
	private boolean multiPsnDomain = false;
	private ForceCache forceCache = ForceCache.DEFAULT;
	private ValidateViaParents validateValuesViaParents = ValidateViaParents.OFF;

	public DomainConfig()
	{}

	public DomainConfig(DomainConfig config) throws InvalidParameterException
	{
		setMaxDetectedErrors(config.getMaxDetectedErrors());
		setPsnPrefixSuffixAndLength(config.getPsnPrefix(), config.getPsnSuffix(), config.getPsnLength());
		this.includePrefixInCheckDigitCalculation = config.isIncludePrefixInCheckDigitCalculation();
		this.includeSuffixInCheckDigitCalculation = config.isIncludeSuffixInCheckDigitCalculation();
		this.useLastCharAsDelimiterAfterXChars = config.getUseLastCharAsDelimiterAfterXChars();
		this.psnsDeletable = config.isPsnsDeletable();
		this.forceCache = config.getForceCache();
		this.validateValuesViaParents = config.getValidateValuesViaParents();
		this.sendNotificationsWeb = config.isSendNotificationsWeb();
		this.multiPsnDomain = config.isMultiPsnDomain();
	}

	/**
	 * Creates the domain config from a character delimited properties string as used for DB persistence.
	 * @param properties the properties string
	 */
	public DomainConfig(String properties) throws InvalidParameterException
	{
		if (properties != null)
		{
			String[] propertyList = properties.split(PROPERTY_DELIMITER);
			for (String property : propertyList)
			{
				String[] propertyParts = property.split("=");
				if (propertyParts.length == 2)
				{
					try
					{
						DomainProperties propertyName = DomainProperties.valueOf(propertyParts[0].trim().toUpperCase());
						switch (propertyName)
						{
							case FORCE_CACHE:
								setForceCache(ForceCache.valueOf(propertyParts[1].trim()));
								break;
							case PSN_LENGTH:
								setPsnLength(Integer.parseInt(propertyParts[1].trim()));
								break;
							case PSN_PREFIX:
								setPsnPrefix(propertyParts[1].trim());
								break;
							case PSN_SUFFIX:
								setPsnSuffix(propertyParts[1].trim());
								break;
							case PSNS_DELETABLE:
								setPsnsDeletable(Boolean.parseBoolean(propertyParts[1].trim()));
								break;
							case INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION:
								setIncludePrefixInCheckDigitCalculation(Boolean.parseBoolean(propertyParts[1].trim()));
								break;
							case INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION:
								setIncludeSuffixInCheckDigitCalculation(Boolean.parseBoolean(propertyParts[1].trim()));
								break;
							case MAX_DETECTED_ERRORS:
								setMaxDetectedErrors(Integer.parseInt(propertyParts[1].trim()));
								break;
							case USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS:
								setUseLastCharAsDelimiterAfterXChars(Integer.parseInt(propertyParts[1].trim()));
								break;
							case VALIDATE_VALUES_VIA_PARENTS:
								setValidateValuesViaParents(ValidateViaParents.valueOf(propertyParts[1].trim()));
								break;
							case SEND_NOTIFICATIONS_WEB:
								setSendNotificationsWeb(Boolean.parseBoolean(propertyParts[1].trim()));
								break;
							case MULTI_PSN_DOMAIN:
								setMultiPsnDomain(Boolean.parseBoolean(propertyParts[1].trim()));
								break;
							default:
								throw new InvalidParameterException("invalid property: " + propertyParts[0].trim());
						}
					}
					catch (IllegalArgumentException e)
					{
						throw new InvalidParameterException("invalid property: " + propertyParts[0].trim());
					}
				}
			}
		}
	}

	public int getMaxDetectedErrors()
	{
		return maxDetectedErrors;
	}

	/**
	 * @param maxDetectedErrors
	 *            numbers of check digits for {@link org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange}<br>
	 *            default 2
	 * @throws InvalidParameterException if maxDetectedErrors is less than 1 or greater than 9
	 */
	public void setMaxDetectedErrors(int maxDetectedErrors) throws InvalidParameterException
	{
		if (maxDetectedErrors > 0 && maxDetectedErrors < 10)
		{
			this.maxDetectedErrors = maxDetectedErrors;
		}
		else
		{
			throw new InvalidParameterException("invalid value for maxDetectedErrors (0 < value < 10): " + maxDetectedErrors);
		}
	}

	public int getPsnLength()
	{
		return psnLength;
	}

	/**
	 * @param psnLength
	 *            length of the generated pseudonym<br>
	 *            default 8
	 * @throws InvalidParameterException if the summed up length of prefix, psn, and suffix
	 *            exceeds the maximum number of characters in fully qualified PSN
	 */
	public void setPsnLength(int psnLength) throws InvalidParameterException
	{
		setPsnPrefixSuffixAndLength(psnPrefix, psnSuffix, psnLength);
	}

	public String getPsnPrefix()
	{
		return psnPrefix;
	}

	/**
	 * @param psnPrefix
	 *            additional prefix<br>
	 *            default ""
	 * @throws InvalidParameterException if the summed up length of prefix, psn, and suffix
	 *            exceeds the maximum number of characters in fully qualified PSN
	 */
	public void setPsnPrefix(String psnPrefix) throws InvalidParameterException
	{
		setPsnPrefixSuffixAndLength(psnPrefix, psnSuffix, psnLength);
	}

	public String getPsnSuffix()
	{
		return psnSuffix;
	}

	/**
	 * @param psnSuffix
	 *            additional suffix<br>
	 *            default ""
	 * @throws InvalidParameterException if the summed up length of prefix, psn, and suffix
	 *            exceeds the maximum number of characters in fully qualified PSN
	 */
	public void setPsnSuffix(String psnSuffix) throws InvalidParameterException
	{
		setPsnPrefixSuffixAndLength(psnPrefix, psnSuffix, psnLength);
	}

	public void setPsnPrefixSuffixAndLength(String psnPrefix, String psnSuffix, int psnLength) throws InvalidParameterException
	{
		checkPsnLength(psnPrefix, psnSuffix, psnLength);
		this.psnPrefix = psnPrefix != null ? psnPrefix : "";
		this.psnSuffix = psnSuffix != null ? psnSuffix : "";
		this.psnLength = psnLength;
	}

	public void resetPsnPrefixSuffixAndLength()
	{
		psnPrefix = "";
		psnSuffix = "";
		psnLength = DEFAULT_PSN_LENGTH;
	}

	public boolean isIncludePrefixInCheckDigitCalculation()
	{
		return includePrefixInCheckDigitCalculation;
	}

	/**
	 * @param includePrefixInCheckDigitCalculation
	 *            should the prefix be used to calculate the check digit(s)<br>
	 *            default false
	 */
	public void setIncludePrefixInCheckDigitCalculation(boolean includePrefixInCheckDigitCalculation)
	{
		this.includePrefixInCheckDigitCalculation = includePrefixInCheckDigitCalculation;
	}

	public boolean isIncludeSuffixInCheckDigitCalculation()
	{
		return includeSuffixInCheckDigitCalculation;
	}

	/**
	 * @param includeSuffixInCheckDigitCalculation
	 *            should the suffix be used to calculate the check digit(s)<br>
	 *            default false
	 */
	public void setIncludeSuffixInCheckDigitCalculation(boolean includeSuffixInCheckDigitCalculation)
	{
		this.includeSuffixInCheckDigitCalculation = includeSuffixInCheckDigitCalculation;
	}

	public int getUseLastCharAsDelimiterAfterXChars()
	{
		return useLastCharAsDelimiterAfterXChars;
	}

	/**
	 * @param useLastCharAsDelimiterAfterXChars
	 *            use last char of the given alphabet as delimiter symbol after the given number of other chars within the pseudonym<br>
	 *            e.g. 123.456.789 or abcd-efgh-ijkl<br>
	 *            default 0 (which means don't use ...)
	 * @throws InvalidParameterException for invalid values
	 */
	public void setUseLastCharAsDelimiterAfterXChars(int useLastCharAsDelimiterAfterXChars) throws InvalidParameterException
	{
		if (useLastCharAsDelimiterAfterXChars >= 0 && useLastCharAsDelimiterAfterXChars < 50)
		{
			this.useLastCharAsDelimiterAfterXChars = useLastCharAsDelimiterAfterXChars;
		}
		else
		{
			throw new InvalidParameterException("invalid value for useLastCharAsDelimiterAfterXChars (0 <= value < 50): " + useLastCharAsDelimiterAfterXChars);
		}
	}

	public boolean isPsnsDeletable()
	{
		return psnsDeletable;
	}

	/**
	 * @param psnsDeletable
	 *            is it allowed to delete entries within this project<br>
	 *            attention! {@link ValidateViaParents#CASCADE_DELETE} ignores this config entry<br>
	 *            default false
	 */
	public void setPsnsDeletable(boolean psnsDeletable)
	{
		this.psnsDeletable = psnsDeletable;
	}

	public ForceCache getForceCache()
	{
		return forceCache;
	}

	/**
	 * @param forceCache
	 *            should a cache be used for faster psn generation, see {@link ForceCache}<br>
	 *            memory consumption is one bit per possible pseudonym: mem_for_cache = alphabet_length ^ pseudonym_length / 8 / 1024 / 1024 MB<br>
	 *            e.g. alphabet = numbers, length = 8 -> mem_for_cache = 10 ^ 8 / (8 * 1024 * 1024) = 11.92 MB<br>
	 *            default {@link ForceCache#DEFAULT} if memory consumption < 120 MB (pseudonym_length = 9, alphabet_length = 10) then use cache, else don't
	 * @throws InvalidParameterException if forceCache is null
	 */
	public void setForceCache(ForceCache forceCache) throws InvalidParameterException
	{
		if (forceCache != null)
		{
			this.forceCache = forceCache;
		}
		else
		{
			throw new InvalidParameterException("invalid value for forceCache: " + forceCache);
		}
	}

	public ValidateViaParents getValidateValuesViaParents()
	{
		return validateValuesViaParents;
	}

	/**
	 * @param validateValuesViaParents
	 *            should the values in this domain be validated against the rules of their parent domains, see {@link ValidateViaParents}<br>
	 *            throws an {@link InvalidParameterException} if there's no parent domain set<br>
	 *            default {@link ValidateViaParents#OFF}
	 */
	public void setValidateValuesViaParents(ValidateViaParents validateValuesViaParents)
	{
		this.validateValuesViaParents = validateValuesViaParents;
	}

	public boolean isSendNotificationsWeb()
	{
		return sendNotificationsWeb;
	}

	public void setSendNotificationsWeb(boolean sendNotificationsWeb)
	{
		this.sendNotificationsWeb = sendNotificationsWeb;
	}

	public boolean isMultiPsnDomain()
	{
		return multiPsnDomain;
	}

	public void setMultiPsnDomain(boolean multiPsnDomain)
	{
		this.multiPsnDomain = multiPsnDomain;
	}

	/**
	 * {@return the domain config as delimited properties string for DB persistence}
	 */
	public String getPropertiesString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(DomainProperties.FORCE_CACHE);
		sb.append("=");
		sb.append(getForceCache().toString());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION);
		sb.append("=");
		sb.append(isIncludePrefixInCheckDigitCalculation());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION);
		sb.append("=");
		sb.append(isIncludeSuffixInCheckDigitCalculation());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.MAX_DETECTED_ERRORS);
		sb.append("=");
		sb.append(getMaxDetectedErrors());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.PSN_LENGTH);
		sb.append("=");
		sb.append(getPsnLength());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.PSN_PREFIX);
		sb.append("=");
		sb.append(getPsnPrefix());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.PSN_SUFFIX);
		sb.append("=");
		sb.append(getPsnSuffix());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.PSNS_DELETABLE);
		sb.append("=");
		sb.append(isPsnsDeletable());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS);
		sb.append("=");
		sb.append(getUseLastCharAsDelimiterAfterXChars());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.VALIDATE_VALUES_VIA_PARENTS);
		sb.append("=");
		sb.append(getValidateValuesViaParents().toString());
		sb.append(PROPERTY_DELIMITER);
		if (isSendNotificationsWeb())
		{
			sb.append(DomainProperties.SEND_NOTIFICATIONS_WEB);
			sb.append("=");
			sb.append(true);
			sb.append(PROPERTY_DELIMITER);
		}
		if (isMultiPsnDomain())
		{
			sb.append(DomainProperties.MULTI_PSN_DOMAIN);
			sb.append("=");
			sb.append(true);
			sb.append(PROPERTY_DELIMITER);
		}
		return sb.toString();
	}

	/**
	 * Returns the maximum number of (UTF8) characters that a fully qualified PSN including prefix, suffix, and check digits
	 * may consist of so that it can be stored in a database field (configured with UTF8MB4).
	 * Even though an UTF8 character could need up to 4 bytes to be encoded in UTF9MB4,
	 * there is no need to convert between byte and character count. Since MySQL 4.1
	 * the number N in 'varchar(N)' explicitly means the number of characters, not the number of bytes,
	 * so that e.g. 'varchar(N)' could take up to 1020 bytes.
	 * See also <a href="https://git.icm.med.uni-greifswald.de/ths/gpas/-/issues/243">32 Byte Lange Pseudonyme erlauben (RKI)</a>
	 * and <a href="https://stackoverflow.com/questions/1997540/mysql-varchar-lengths-and-utf-8">MySQL VARCHAR Lengths and UTF-8</a>.
	 *
	 * @return the maximum number of (UTF8) characters that a fully qualified PSN may consist of
	 */
	public static int getMaxNumberOfCharactersInFullyQualifiedPsn()
	{
		return DB_PSN_FIELD_LENGTH;
	}

	public static void checkPsnLength(String psnPrefix, String psnSuffix, int psnLength) throws InvalidParameterException
	{
		checkPsnLength(psnPrefix, psnSuffix, psnLength, null);
	}

	public static void checkPsnLength(String psnPrefix, String psnSuffix, int psnLength, CheckDigits checkDigits) throws InvalidParameterException
	{
		if (psnLength < 0)
		{
			throw new InvalidParameterException(
					"the psn length (" + psnLength + ") must not be less than 0.");
		}
		int additionalNumChars = (psnPrefix != null ? psnPrefix.length() : 0) + (psnSuffix != null ? psnSuffix.length() : 0);
		int max = getMaxNumberOfCharactersInFullyQualifiedPsn();
		int maxWithoutAdditionalCharacters = max - additionalNumChars;
		if (checkDigits != null)
		{
			int maxMessageLength = checkDigits.getEffectiveMaxMessageLength(maxWithoutAdditionalCharacters);
			if (psnLength > maxMessageLength)
			{
				throw new InvalidParameterException(
						"the psn length (" + psnLength + ") must not be greater than " + maxMessageLength +
						" for using " + checkDigits + " with additional " + additionalNumChars + " characters for prefix and suffix.");
			}
		}
		int fullLength = psnLength + additionalNumChars;
		if (fullLength > max)
		{
			throw new InvalidParameterException(
					"the length of [prefix + psn + suffix] (" + fullLength + ") must not exceed " + max);
		}
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(maxDetectedErrors)
				.append(psnLength)
				.append(psnPrefix)
				.append(psnSuffix)
				.append(includePrefixInCheckDigitCalculation)
				.append(includeSuffixInCheckDigitCalculation)
				.append(useLastCharAsDelimiterAfterXChars)
				.append(psnsDeletable)
				.append(sendNotificationsWeb)
				.append(multiPsnDomain)
				.append(forceCache)
				.append(validateValuesViaParents)
				.toHashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof DomainConfig that))
			return false;

		return new EqualsBuilder()
				.append(maxDetectedErrors, that.maxDetectedErrors)
				.append(psnLength, that.psnLength)
				.append(includePrefixInCheckDigitCalculation, that.includePrefixInCheckDigitCalculation)
				.append(includeSuffixInCheckDigitCalculation, that.includeSuffixInCheckDigitCalculation)
				.append(useLastCharAsDelimiterAfterXChars, that.useLastCharAsDelimiterAfterXChars)
				.append(psnsDeletable, that.psnsDeletable)
				.append(sendNotificationsWeb, that.sendNotificationsWeb)
				.append(multiPsnDomain, that.multiPsnDomain)
				.append(psnPrefix, that.psnPrefix)
				.append(psnSuffix, that.psnSuffix)
				.append(forceCache, that.forceCache)
				.append(validateValuesViaParents, that.validateValuesViaParents)
				.isEquals();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("maxDetectedErrors", maxDetectedErrors)
				.append("psnLength", psnLength)
				.append("psnPrefix", psnPrefix)
				.append("psnSuffix", psnSuffix)
				.append("includePrefixInCheckDigitCalculation", includePrefixInCheckDigitCalculation)
				.append("includeSuffixInCheckDigitCalculation", includeSuffixInCheckDigitCalculation)
				.append("useLastCharAsDelimiterAfterXChars", useLastCharAsDelimiterAfterXChars)
				.append("psnsDeletable", psnsDeletable)
				.append("sendNotificationsWeb", sendNotificationsWeb)
				.append("multiPsnDomain", multiPsnDomain)
				.append("forceCache", forceCache)
				.append("validateValuesViaParents", validateValuesViaParents)
				.toString();
	}
}
