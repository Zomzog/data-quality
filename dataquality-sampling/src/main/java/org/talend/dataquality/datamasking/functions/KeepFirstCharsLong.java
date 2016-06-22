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
package org.talend.dataquality.datamasking.functions;

/**
 * created by jgonzalez on 22 juin 2015. See KeepFirstAndGenerate.
 *
 */
public class KeepFirstCharsLong extends KeepFirstChars<Long> {

    private static final long serialVersionUID = 3522517905787655968L;

    @Override
    protected Long getDefaultOutput() {
        return 0L;
    }

    @Override
    protected Long getOutput(String string) {
        return Long.valueOf(string);
    }

    @Override
    protected boolean validParameters() {
        return (parameters.length == 1 || (parameters.length == 2 && patternDigit.matcher(parameters[1]).matches()))
                && patternNumber.matcher(parameters[0]).matches();
    }
}