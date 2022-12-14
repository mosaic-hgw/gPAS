package org.emau.icmvc.ganimed.ttp.psn.config;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2022 Independent Trusted Third Party of the University Medicine Greifswald
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

import java.io.Serializable;

import org.emau.icmvc.ganimed.ttp.psn.enums.ForceCache;
import org.emau.icmvc.ganimed.ttp.psn.enums.ValidateViaParents;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;

public class DomainConfig implements Serializable
{
	private static final long serialVersionUID = -8036316501414002948L;
	public static final long MAX_PSEUDONYMS_FOR_DEFAULT_CACHE_ON = 1000000000; // 125 mb is needed for such a cache
	private static final String PROPERTY_DELIMITER = ";";
	private static final int DB_PSN_FIELD_LENGTH = 255;
	private int maxDetectedErrors = 2;
	private int psnLength = 8;
	private String psnPrefix = "";
	private String psnSuffix = "";
	private boolean includePrefixInCheckDigitCalculation = false;
	private boolean includeSuffixInCheckDigitCalculation = false;
	private int useLastCharAsDelimiterAfterXChars = 0;
	private boolean psnsDeletable = false;
	private ForceCache forceCache = ForceCache.DEFAULT;
	private ValidateViaParents validateValuesViaParents = ValidateViaParents.OFF;

	public DomainConfig()
	{}

	public DomainConfig(int maxDetectedErrors, int psnLength, String psnPrefix, String psnSuffix, boolean includePrefixInCheckDigitCalculation, boolean includeSuffixInCheckDigitCalculation,
			int useLastCharAsDelimiterAfterXChars, boolean psnsDeletable, ForceCache forceCache, ValidateViaParents validateValuesViaParents) throws InvalidParameterException
	{
		super();
		setMaxDetectedErrors(maxDetectedErrors);
		setPsnLength(psnLength);
		setPsnPrefix(psnPrefix);
		setPsnSuffix(psnSuffix);
		this.includePrefixInCheckDigitCalculation = includePrefixInCheckDigitCalculation;
		this.includeSuffixInCheckDigitCalculation = includeSuffixInCheckDigitCalculation;
		this.useLastCharAsDelimiterAfterXChars = useLastCharAsDelimiterAfterXChars;
		this.psnsDeletable = psnsDeletable;
		this.forceCache = forceCache;
		this.validateValuesViaParents = validateValuesViaParents;
	}

	public DomainConfig(DomainConfig config) throws InvalidParameterException
	{
		super();
		setMaxDetectedErrors(config.getMaxDetectedErrors());
		setPsnLength(config.getPsnLength());
		setPsnPrefix(config.getPsnPrefix());
		setPsnSuffix(config.getPsnSuffix());
		this.includePrefixInCheckDigitCalculation = config.isIncludePrefixInCheckDigitCalculation();
		this.includeSuffixInCheckDigitCalculation = config.isIncludeSuffixInCheckDigitCalculation();
		this.useLastCharAsDelimiterAfterXChars = config.getUseLastCharAsDelimiterAfterXChars();
		this.psnsDeletable = config.isPsnsDeletable();
		this.forceCache = config.getForceCache();
		this.validateValuesViaParents = config.getValidateValuesViaParents();
	}

	/**
	 * reads the old (pre version 1.10) domain config properties string<br>
	 * better don't use this
	 */
	@Deprecated
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
								setPsnLength(Integer.valueOf(propertyParts[1].trim()));
								break;
							case PSN_PREFIX:
								setPsnPrefix(propertyParts[1].trim());
								break;
							case PSN_SUFFIX:
								setPsnSuffix(propertyParts[1].trim());
								break;
							case PSNS_DELETABLE:
								setPsnsDeletable(Boolean.valueOf(propertyParts[1].trim()));
								break;
							case INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION:
								setIncludePrefixInCheckDigitCalculation(Boolean.valueOf(propertyParts[1].trim()));
								break;
							case INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION:
								setIncludeSuffixInCheckDigitCalculation(Boolean.valueOf(propertyParts[1].trim()));
								break;
							case MAX_DETECTED_ERRORS:
								setMaxDetectedErrors(Integer.valueOf(propertyParts[1].trim()));
								break;
							case USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS:
								setUseLastCharAsDelimiterAfterXChars(Integer.valueOf(propertyParts[1].trim()));
								break;
							case VALIDATE_VALUES_VIA_PARENTS:
								setValidateValuesViaParents(ValidateViaParents.valueOf(propertyParts[1].trim()));
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
	 * @throws InvalidParameterException
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
	 * @throws InvalidParameterException
	 */
	public void setPsnLength(int psnLength) throws InvalidParameterException
	{
		if (psnLength > 0)
		{
			this.psnLength = psnLength;
		}
		checkLength();
	}

	public String getPsnPrefix()
	{
		return psnPrefix;
	}

	/**
	 * @param psnPrefix
	 *            additional prefix<br>
	 *            default ""
	 * @throws InvalidParameterException
	 */
	public void setPsnPrefix(String psnPrefix) throws InvalidParameterException
	{
		if (psnPrefix != null)
		{
			this.psnPrefix = psnPrefix;
		}
		checkLength();
	}

	public String getPsnSuffix()
	{
		return psnSuffix;
	}

	/**
	 * @param psnSuffix
	 *            additional suffix<br>
	 *            default ""
	 * @throws InvalidParameterException
	 */
	public void setPsnSuffix(String psnSuffix) throws InvalidParameterException
	{
		if (psnSuffix != null)
		{
			this.psnSuffix = psnSuffix;
		}
		checkLength();
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
	 *            default 0 (which means dont use ...)
	 * @throws InvalidParameterException
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
	 * @throws InvalidParameterException
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

	/**
	 * @return the old (pre version 1.10) domain config properties string<br>
	 *         better don't use this
	 */
	@Deprecated
	public String getPropertiesString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(DomainProperties.FORCE_CACHE.toString());
		sb.append("=");
		sb.append(getForceCache().toString());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION.toString());
		sb.append("=");
		sb.append(isIncludePrefixInCheckDigitCalculation());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION.toString());
		sb.append("=");
		sb.append(isIncludeSuffixInCheckDigitCalculation());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.MAX_DETECTED_ERRORS.toString());
		sb.append("=");
		sb.append(getMaxDetectedErrors());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.PSN_LENGTH.toString());
		sb.append("=");
		sb.append(getPsnLength());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.PSN_PREFIX.toString());
		sb.append("=");
		sb.append(getPsnPrefix());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.PSN_SUFFIX.toString());
		sb.append("=");
		sb.append(getPsnSuffix());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.PSNS_DELETABLE.toString());
		sb.append("=");
		sb.append(isPsnsDeletable());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS.toString());
		sb.append("=");
		sb.append(getUseLastCharAsDelimiterAfterXChars());
		sb.append(PROPERTY_DELIMITER);
		sb.append(DomainProperties.VALIDATE_VALUES_VIA_PARENTS.toString());
		sb.append("=");
		sb.append(getValidateValuesViaParents().toString());
		sb.append(PROPERTY_DELIMITER);
		return sb.toString();
	}

	private void checkLength() throws InvalidParameterException
	{
		// *4, da utf8mb4 bald standard fuer utf8 in mysql werden soll
		int length = psnLength + (psnPrefix == null ? 0 : psnPrefix.length()) + (psnSuffix == null ? 0 : psnSuffix.length()) * 4;
		if (length > DB_PSN_FIELD_LENGTH)
		{
			throw new InvalidParameterException("the length of prefix + psn + suffix mustn't exceed " + DB_PSN_FIELD_LENGTH / 4);
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (forceCache == null ? 0 : forceCache.hashCode());
		result = prime * result + (includePrefixInCheckDigitCalculation ? 1231 : 1237);
		result = prime * result + (includeSuffixInCheckDigitCalculation ? 1231 : 1237);
		result = prime * result + maxDetectedErrors;
		result = prime * result + psnLength;
		result = prime * result + (psnPrefix == null ? 0 : psnPrefix.hashCode());
		result = prime * result + (psnSuffix == null ? 0 : psnSuffix.hashCode());
		result = prime * result + (psnsDeletable ? 1231 : 1237);
		result = prime * result + useLastCharAsDelimiterAfterXChars;
		result = prime * result + (validateValuesViaParents == null ? 0 : validateValuesViaParents.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		DomainConfig other = (DomainConfig) obj;
		if (forceCache != other.forceCache)
		{
			return false;
		}
		if (includePrefixInCheckDigitCalculation != other.includePrefixInCheckDigitCalculation)
		{
			return false;
		}
		if (includeSuffixInCheckDigitCalculation != other.includeSuffixInCheckDigitCalculation)
		{
			return false;
		}
		if (maxDetectedErrors != other.maxDetectedErrors)
		{
			return false;
		}
		if (psnLength != other.psnLength)
		{
			return false;
		}
		if (psnPrefix == null)
		{
			if (other.psnPrefix != null)
			{
				return false;
			}
		}
		else if (!psnPrefix.equals(other.psnPrefix))
		{
			return false;
		}
		if (psnSuffix == null)
		{
			if (other.psnSuffix != null)
			{
				return false;
			}
		}
		else if (!psnSuffix.equals(other.psnSuffix))
		{
			return false;
		}
		if (psnsDeletable != other.psnsDeletable)
		{
			return false;
		}
		if (useLastCharAsDelimiterAfterXChars != other.useLastCharAsDelimiterAfterXChars)
		{
			return false;
		}
		if (validateValuesViaParents != other.validateValuesViaParents)
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "DomainConfig [maxDetectedErrors=" + maxDetectedErrors + ", psnLength=" + psnLength + ", psnPrefix=" + psnPrefix + ", psnSuffix=" + psnSuffix + ", includePrefixInCheckDigitCalculation="
				+ includePrefixInCheckDigitCalculation + ", includeSuffixInCheckDigitCalculation=" + includeSuffixInCheckDigitCalculation + ", useLastCharAsDelimiterAfterXChars="
				+ useLastCharAsDelimiterAfterXChars + ", psnsDeletable=" + psnsDeletable + ", forceCache=" + forceCache + ", validateValuesViaParents=" + validateValuesViaParents + "]";
	}
}
