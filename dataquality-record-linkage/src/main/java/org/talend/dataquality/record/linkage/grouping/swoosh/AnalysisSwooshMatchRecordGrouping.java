// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.record.linkage.grouping.swoosh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataquality.matchmerge.Attribute;
import org.talend.dataquality.record.linkage.constant.AttributeMatcherType;
import org.talend.dataquality.record.linkage.grouping.AnalysisMatchRecordGrouping;
import org.talend.dataquality.record.linkage.grouping.IRecordGrouping;
import org.talend.dataquality.record.linkage.grouping.MatchGroupResultConsumer;

/**
 * created by yyin on 2014-9-15 Detailled comment
 * 
 */
public class AnalysisSwooshMatchRecordGrouping extends AnalysisMatchRecordGrouping {

    private Map<Integer, Attribute> attributesAsMatchKey;

    private boolean isComponentMode = false;

    /**
     * Sets the isCompositeMode.
     * 
     * @param isCompositeMode the isCompositeMode to set
     */
    public void setComponentMode(boolean isCompositeMode) {
        this.isComponentMode = isCompositeMode;
    }

    /**
     * DOC yyin AnalysisSwooshMatchRecordGrouping constructor comment.
     * 
     * @param matchResultConsumer
     */
    public AnalysisSwooshMatchRecordGrouping(MatchGroupResultConsumer matchResultConsumer) {
        super(matchResultConsumer);
    }

    public AnalysisSwooshMatchRecordGrouping() {

    }

    @Override
    public void setSurvivorShipAlgorithmParams(SurvivorShipAlgorithmParams survivorShipAlgorithmParams) {
        super.setSurvivorShipAlgorithmParams(survivorShipAlgorithmParams);
        swooshGrouping.initialMFBForOneRecord(getCombinedRecordMatcher(), survivorShipAlgorithmParams);
    }

    @Override
    public void initialize() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (isComponentMode) {
            masterRecords.clear();
        } else {
            super.initialize();
        }
        // get the match keys attributes, and put them in the map, no need to do this again for each record
        attributesAsMatchKey = new HashMap<Integer, Attribute>();
        for (List<Map<String, String>> matchRule : getMultiMatchRules()) {
            for (Map<String, String> mkDef : matchRule) {
                String matcherType = mkDef.get(IRecordGrouping.MATCHING_TYPE);
                if (AttributeMatcherType.DUMMY.name().equals(matcherType)) {
                    continue;
                }
                int columnIndex = Integer.parseInt(mkDef.get(IRecordGrouping.COLUMN_IDX));
                Attribute attribute = new Attribute(mkDef.get(IRecordGrouping.ATTRIBUTE_NAME), columnIndex);
                attributesAsMatchKey.put(columnIndex, attribute);
            }
        }
    }

    @Override
    public void doGroup(RichRecord currentRecord) {
        // translate the record's attribute to -->origalRow, and attributes only contain match keys
        translateRecordForSwoosh(currentRecord);

        swooshGrouping.oneRecordMatch(currentRecord);
    }

    /**
     * To move all attributes into: OriginRow, and change the current attributes to only contains the column which is
     * used to be the match keys.
     * 
     * @param currentRecord
     */
    private void translateRecordForSwoosh(RichRecord currentRecord) {
        List<Attribute> matchAttrs = new ArrayList<Attribute>();
        List<DQAttribute<?>> rowList = new ArrayList<DQAttribute<?>>();
        for (Attribute attribute : currentRecord.getAttributes()) {
            DQAttribute<String> attri = new DQAttribute<String>(attribute.getLabel(), attribute.getColumnIndex(),
                    attribute.getValue());
            rowList.add(attri);
            // if the current attribute is definde as a match key.
            Attribute matchkey = attributesAsMatchKey.get(attribute.getColumnIndex());
            if (matchkey != null && StringUtils.equalsIgnoreCase(matchkey.getLabel(), attri.getLabel())) {
                matchAttrs.add(attri);
            }
        }
        currentRecord.getAttributes().clear();
        currentRecord.getAttributes().addAll(matchAttrs);
        currentRecord.setOriginRow(rowList);
    }

    private boolean matchFinished = false;

    @Override
    public void end() {
        if (isComponentMode) {
            matchFinished = false;
            if (isLinkToPrevious) {// use multipass
                swooshGrouping.swooshMatchWithMultipass(combinedRecordMatcher, survivorShipAlgorithmParams,
                        originalInputColumnSize);
            } else {
                // during the match, the output in processing will not output really
                swooshGrouping.swooshMatch(combinedRecordMatcher, survivorShipAlgorithmParams);
            }
            matchFinished = true;
        }
        // out put
        swooshGrouping.afterAllRecordFinished();

        if (isComponentMode) {
            for (RichRecord row : tmpMatchResult) {
                // For swoosh algorithm, the GID can only be know after all of the records are computed.
                outputRow(row);
            }
        }
        // Clear the GID map , no use anymore.
        swooshGrouping.getOldGID2New().clear();
        tmpMatchResult.clear();
    }

    /**
     * only used for tMatchGroup, and only after swoosh match finished.
     */
    @Override
    protected void outputRow(RichRecord row) {
        if (isComponentMode && !matchFinished) {
            tmpMatchResult.add(row);
        } else {
            List<DQAttribute<?>> originRow;
            if (isLinkToPrevious) {// use multipass
                originRow = row.getOutputRow(swooshGrouping.getOldGID2New(), originalInputColumnSize);
            } else {
                originRow = row.getOutputRow(swooshGrouping.getOldGID2New());
            }
            String[] strRow = new String[originRow.size()];
            int idx = 0;
            for (DQAttribute<?> attr : originRow) {
                if (row.isMaster() && row.isMerged()) {
                    strRow[idx] = attr.getValue();
                } else {
                    strRow[idx] = attr.getOriginalValue() == null ? attr.getValue() : String.valueOf(attr.getOriginalValue());
                }
                idx++;
            }
            outputRow(strRow);
        }
    }
}
