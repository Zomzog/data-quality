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
        String regexStr = "(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, null);
        String maskResult = regexFunction.doGenerateMaskedField("any not empty value"); //$NON-NLS-1$
        Pattern compile = java.util.regex.Pattern.compile("(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}"); //$NON-NLS-1$
        Matcher matcher = compile.matcher(maskResult);
        Assert.assertTrue("maskResult is correct result:" + maskResult, matcher.matches()); //$NON-NLS-1$

        // use same seed should get same result
        regexFunction = new GenerateFromRegex();
        regexFunction.parse(regexStr, true, null);
        String secondMaskResult = regexFunction.doGenerateMaskedField("any not empty value"); //$NON-NLS-1$
        Assert.assertEquals("maskResult is correct result", maskResult, secondMaskResult); //$NON-NLS-1$
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
        String maskResult = regexFunction.doGenerateMaskedField("any not empty value"); //$NON-NLS-1$
        Pattern compile = java.util.regex.Pattern.compile("^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$"); //$NON-NLS-1$
        Matcher matcher = compile.matcher(maskResult);
        Assert.assertTrue("maskResult is correct result:" + maskResult, matcher.matches()); //$NON-NLS-1$

        // more than one ^ and $
        regexFunction = new GenerateFromRegex();
        regexStr = "^^^^^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$$$"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, null);
        maskResult = regexFunction.doGenerateMaskedField("any not empty value"); //$NON-NLS-1$
        compile = java.util.regex.Pattern.compile("^^^^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$$"); //$NON-NLS-1$
        matcher = compile.matcher(maskResult);
        Assert.assertTrue("maskResult is correct result:" + maskResult, matcher.matches()); //$NON-NLS-1$

        regexFunction = new GenerateFromRegex();
        regexStr = "^^^^\\^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$\\$$"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, null);
        maskResult = regexFunction.doGenerateMaskedField("any not empty value"); //$NON-NLS-1$
        compile = java.util.regex.Pattern.compile("^^^\\^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$\\$$"); //$NON-NLS-1$
        matcher = compile.matcher(maskResult);
        Assert.assertFalse("maskResult is correct result:" + maskResult, matcher.matches()); //$NON-NLS-1$
        Assert.assertEquals("maskResult is correct result: ^^+339 79-75 90.07$$$$$", "^^+339 79-75 90.07$$$$$", maskResult); //$NON-NLS-1$

        regexFunction = new GenerateFromRegex();
        regexStr = "\\^^^^^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$$\\$"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, null);
        maskResult = regexFunction.doGenerateMaskedField("any not empty value"); //$NON-NLS-1$
        compile = java.util.regex.Pattern.compile("\\^^^^^^(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}$$$$$$\\$"); //$NON-NLS-1$
        matcher = compile.matcher(maskResult);
        Assert.assertFalse("maskResult is correct result:" + maskResult, matcher.matches()); //$NON-NLS-1$
        Assert.assertEquals("maskResult is correct result: ^^^^^^00333-4908-75 09$$$$$$", "^^^^^^00333-4908-75 09$$$$$$", //$NON-NLS-1$
                maskResult);
    }

    /**
     * Test method for {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#isValidPattern(String)}.
     */
    @Test
    public void testIsValidPattern() {
        // US_PHONE case
        boolean isValidPattern = GenerateFromRegex.isValidPattern(
                "^(?:(?:(?:\\+|00)?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$");
        Assert.assertFalse("(?:pattern) is not support by this API by now", isValidPattern);
        // [UK_PHONE case
        isValidPattern = GenerateFromRegex.isValidPattern(
                "^(\\+44[[:space:]]?7[[:digit:]]{3}|\\(?07[[:digit:]]{3}\\)?)[[:space:]]?[[:digit:]]{3}[[:space:]]?[[:digit:]]{3}$");
        Assert.assertFalse("'[[:space:]]' and '[[:digit:]]' is not support by this API by now", isValidPattern);
        // DE_POSTAL_CODE case
        isValidPattern = GenerateFromRegex.isValidPattern("^(?!01000|99999)(0[1-9]\\d{3}|[1-9]\\d{4})$");
        Assert.assertFalse("'(?!pattern)' is not support by this API by now", isValidPattern);
        // (?=pattern) case
        isValidPattern = GenerateFromRegex.isValidPattern("^(?=01000|99999)(0[1-9]\\d{3}|[1-9]\\d{4})$");
        Assert.assertFalse("'(?=pattern)' is not support by this API by now", isValidPattern);
    }

}