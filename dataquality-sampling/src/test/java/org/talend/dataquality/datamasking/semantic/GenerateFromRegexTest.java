// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.datamasking.semantic;

import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * The Function which used to generate data by regex
 */
public class GenerateFromRegexTest {

    /**
     * Test method for {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#setRandom(java.util.Random)}.
     */
    @Test
    public void testSetRandom() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.setRandom(null);
        Assert.assertTrue("setRandom method will not modify the value of regexFunction", regexFunction.generex == null); //$NON-NLS-1$
        Assert.assertTrue("setRandom method will create new random instance", //$NON-NLS-1$
                regexFunction.getRandom() != null && regexFunction.getRandom() instanceof SecureRandom);
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#parse(java.lang.String, boolean, java.util.Random)}.
     * case 1 normal case
     */
    @Test
    public void testParseCase1() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.parse("(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}", true, null); //$NON-NLS-1$
        Assert.assertTrue("regexFunction.generex should not be null", regexFunction.generex != null); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#parse(java.lang.String, boolean, java.util.Random)}.
     * case 2 extraParameter is null
     */
    @Test
    public void testParseCase2() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.parse(null, true, new Random(100l));
        Assert.assertTrue("regexFunction.generex should be null", regexFunction.generex == null); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#doGenerateMaskedField(java.lang.String)}.
     * case 1 keepNull is true and inputValue is null
     */
    @Test
    public void testDoGenerateMaskedFieldStringCase1() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.parse("(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}", true, null); //$NON-NLS-1$
        String maskResult = regexFunction.doGenerateMaskedField(null);
        Assert.assertEquals("maskResult should be null", null, maskResult); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#doGenerateMaskedField(java.lang.String)}.
     * case 2 keepNull is true and inputValue is empty
     */
    @Test
    public void testDoGenerateMaskedFieldStringCase2() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.parse("(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}", true, null); //$NON-NLS-1$
        String maskResult = regexFunction.doGenerateMaskedField(StringUtils.EMPTY);
        Assert.assertEquals("maskResult should be EMPTY", StringUtils.EMPTY, maskResult); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#doGenerateMaskedField(java.lang.String)}.
     * case 3 normal case
     */
    @Test
    public void testDoGenerateMaskedFieldStringCase3() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.setSeed(100L);
        String regexStr = "(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, new Random(100L));
        String maskResult = regexFunction.doGenerateMaskedField("00331-00-01-02-03"); //$NON-NLS-1$
        Pattern compile = java.util.regex.Pattern.compile("(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}"); //$NON-NLS-1$
        Matcher matcher = compile.matcher(maskResult);
        Assert.assertTrue("maskResult is expected to match the pattern but actually not: " + maskResult, matcher.matches()); //$NON-NLS-1$

        // use same seed should get same result
        regexFunction = new GenerateFromRegex();
        regexFunction.parse(regexStr, true, new Random(100L));
        String secondMaskResult = regexFunction.doGenerateMaskedField("00331-00-01-02-03"); //$NON-NLS-1$
        Assert.assertEquals("Unexpected maskResult.", maskResult, secondMaskResult); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#doGenerateMaskedField(java.lang.String)}.
     * case 4 There are contains invalid character "^" and "$"
     */
    @Test
    public void testDoGenerateMaskedFieldStringCase4() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        String regexStr = "^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, null);
        String maskResult = regexFunction.doGenerateMaskedField("00331-00-01-02-03"); //$NON-NLS-1$
        Pattern compile = java.util.regex.Pattern.compile("^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$"); //$NON-NLS-1$
        Matcher matcher = compile.matcher(maskResult);
        Assert.assertTrue("maskResult is expected to match the pattern but actually not: " + maskResult, matcher.matches()); //$NON-NLS-1$

        // more than one ^ and $
        regexFunction = new GenerateFromRegex();
        regexStr = "^^^^^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$$$"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, null);
        maskResult = regexFunction.doGenerateMaskedField("00331-00-01-02-03"); //$NON-NLS-1$
        compile = java.util.regex.Pattern.compile("^^^^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$$"); //$NON-NLS-1$
        matcher = compile.matcher(maskResult);
        Assert.assertTrue("maskResult is expected to match the pattern but actually not: " + maskResult, matcher.matches()); //$NON-NLS-1$

        regexFunction = new GenerateFromRegex();
        regexStr = "^^^^\\^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$\\$$"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, new Random(100L));
        maskResult = regexFunction.doGenerateMaskedField("00331-00-01-02-03"); //$NON-NLS-1$
        compile = java.util.regex.Pattern.compile("^^^\\^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$\\$$"); //$NON-NLS-1$
        matcher = compile.matcher(maskResult);
        Assert.assertFalse("maskResult is correct result:" + maskResult, matcher.matches()); //$NON-NLS-1$

        regexFunction = new GenerateFromRegex();
        regexStr = "\\^^^^^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$$\\$"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, new Random(100L));
        maskResult = regexFunction.doGenerateMaskedField("00331-00-01-02-03"); //$NON-NLS-1$
        compile = java.util.regex.Pattern.compile("\\^^^^^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$$\\$"); //$NON-NLS-1$
        matcher = compile.matcher(maskResult);
        Assert.assertFalse("maskResult is correct result:" + maskResult, matcher.matches()); //$NON-NLS-1$

    }

    /**
     * Test method for {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#isValidPattern(String)}.
     */
    @Test
    public void testIsValidPattern() {
        // US_PHONE case
        boolean isValidPattern = GenerateFromRegex.isValidPattern(
                "^(?:(?:(?:\\+|00)?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$"); //$NON-NLS-1$
        Assert.assertFalse("(?:pattern) is not support by this API by now", isValidPattern); //$NON-NLS-1$
        // [UK_PHONE case
        isValidPattern = GenerateFromRegex.isValidPattern(
                "^(\\+44[[:space:]]?7[[:digit:]]{3}|\\(?07[[:digit:]]{3}\\)?)[[:space:]]?[[:digit:]]{3}[[:space:]]?[[:digit:]]{3}$"); //$NON-NLS-1$
        Assert.assertFalse("'[[:space:]]' and '[[:digit:]]' is not support by this API by now", isValidPattern); //$NON-NLS-1$
        // DE_POSTAL_CODE case
        isValidPattern = GenerateFromRegex.isValidPattern("^(?!01000|99999)(0[1-9]\\d{3}|[1-9]\\d{4})$"); //$NON-NLS-1$
        Assert.assertFalse("'(?!pattern)' is not support by this API by now", isValidPattern); //$NON-NLS-1$
        // (?=pattern) case
        isValidPattern = GenerateFromRegex.isValidPattern("^(?=01000|99999)(0[1-9]\\d{3}|[1-9]\\d{4})$"); //$NON-NLS-1$
        Assert.assertFalse("'(?=pattern)' is not support by this API by now", isValidPattern); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#isValidData(String)}.
     */
    @Test
    public void testIsValidDataString() {
        String regexStr = "^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$"; //$NON-NLS-1$
        GenerateFromRegex generateFromRegex = new GenerateFromRegex();
        generateFromRegex.parse(regexStr, true, new Random(100L));
        boolean validData = generateFromRegex.isValidData("00331-01-03-02-00"); //$NON-NLS-1$
        Assert.assertTrue("00331-01-03-02-00 should be valid data", validData); //$NON-NLS-1$

        validData = generateFromRegex.isValidData("00330-01-03-02-00"); //$NON-NLS-1$
        Assert.assertFalse("00330-01-03-02-00 should be invalid data", validData); //$NON-NLS-1$

        regexStr = "^[0-9]{5}(-[0-9]{4})?$"; //$NON-NLS-1$
        generateFromRegex = new GenerateFromRegex();
        generateFromRegex.parse(regexStr, true, new Random(100L));
        String invalidData = "402095436"; //$NON-NLS-1$
        validData = generateFromRegex.isValidData(invalidData);
        Pattern compile = java.util.regex.Pattern.compile("[0-9]{5}(-[0-9]{4})?"); //$NON-NLS-1$
        Matcher matcher = compile.matcher(invalidData);
        Assert.assertTrue(invalidData + " should be valid data by matcher.find()", matcher.find()); //$NON-NLS-1$
        Assert.assertFalse(invalidData + " should be invalid data by generateFromRegex.isValidData", validData); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#generateValidMaskData(String)}.
     */
    @Test
    public void testGenerateValidMaskDataString() {
        String regexStr = "^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$"; //$NON-NLS-1$
        GenerateFromRegex generateFromRegex = new GenerateFromRegex();
        generateFromRegex.parse(regexStr, true, new Random(100L));
        String generateResult = generateFromRegex.generateValidMaskData("00330-01-03-02-00"); //$NON-NLS-1$
        Pattern compile = java.util.regex.Pattern.compile(regexStr);
        Matcher matcher = compile.matcher(generateResult);
        Assert.assertTrue(generateResult + " should be valid data", matcher.matches()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#generateInvalidMaskData(String)}.
     */
    @Test
    public void testGenerateInvalidMaskDataString() {
        String regexStr = "^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$"; //$NON-NLS-1$
        GenerateFromRegex generateFromRegex = new GenerateFromRegex();
        generateFromRegex.parse(regexStr, true, new Random(100L));
        String generateResult = generateFromRegex.generateInvalidMaskData("any invalid data"); //$NON-NLS-1$
        Pattern compile = java.util.regex.Pattern.compile(regexStr);
        Matcher matcher = compile.matcher(generateResult);
        Assert.assertFalse(generateResult + " should be invalid data", matcher.matches()); //$NON-NLS-1$
    }

}