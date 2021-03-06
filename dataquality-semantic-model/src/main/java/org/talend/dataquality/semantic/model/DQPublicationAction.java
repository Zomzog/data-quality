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
package org.talend.dataquality.semantic.model;

import java.util.List;

public class DQPublicationAction extends DQAction {

    private String id;

    private DQCategory category;

    private List<DQCategory> categories;

    private List<DQDocument> documents;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DQCategory getCategory() {
        return category;
    }

    public void setCategory(DQCategory category) {
        this.category = category;
    }

    public List<DQCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<DQCategory> categories) {
        this.categories = categories;
    }

    public List<DQDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DQDocument> documents) {
        this.documents = documents;
    }
}
