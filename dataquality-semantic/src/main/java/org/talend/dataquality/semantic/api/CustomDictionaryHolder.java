package org.talend.dataquality.semantic.api;

import static org.talend.dataquality.semantic.api.CategoryRegistryManager.DICTIONARY_SUBFOLDER_NAME;
import static org.talend.dataquality.semantic.api.CategoryRegistryManager.METADATA_SUBFOLDER_NAME;
import static org.talend.dataquality.semantic.api.CategoryRegistryManager.PRODUCTION_FOLDER_NAME;
import static org.talend.dataquality.semantic.api.CategoryRegistryManager.REGEX_CATEGORIZER_FILE_NAME;
import static org.talend.dataquality.semantic.api.CategoryRegistryManager.REGEX_SUBFOLDER_NAME;
import static org.talend.dataquality.semantic.api.CategoryRegistryManager.REPUBLISH_FOLDER_NAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataquality.semantic.api.internal.CustomDocumentIndexAccess;
import org.talend.dataquality.semantic.api.internal.CustomMetadataIndexAccess;
import org.talend.dataquality.semantic.api.internal.CustomRegexClassifierAccess;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedCategory;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.index.DictionarySearchMode;
import org.talend.dataquality.semantic.index.LuceneIndex;
import org.talend.dataquality.semantic.model.CategoryType;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.model.DQDocument;
import org.talend.dataquality.semantic.snapshot.DictionarySnapshot;

/**
 * holder of tenant-specific categories, provides access to custom Metadata/DataDict/RegEx.
 */
public class CustomDictionaryHolder {

    public static final String TALEND = "Talend";

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDictionaryHolder.class);

    private static final String INITIALIZE_ACCESS = "Initialize %s %s access for [%s]";

    private static final String CUSTOM = "custom";

    private Map<String, DQCategory> metadata;

    private Directory dataDictDirectory;

    private UserDefinedClassifier regexClassifier;

    private CustomMetadataIndexAccess customMetadataIndexAccess;

    private CustomDocumentIndexAccess customDocumentIndexAccess;

    private CustomRegexClassifierAccess customRegexClassifierAccess;

    private CustomMetadataIndexAccess customRepublishMetadataIndexAccess;

    private CustomDocumentIndexAccess customRepublishDataDictIndexAccess;

    private CustomRegexClassifierAccess customRepublishRegexClassifierAccess;

    private LocalDictionaryCache localDictionaryCache;

    private String tenantID;

    /**
     * constructor
     *
     * @param tenantID
     */
    public CustomDictionaryHolder(String tenantID) {
        this.tenantID = tenantID;
        if (CategoryRegistryManager.isUsingLocalCategoryRegistry()) {
            // no need check tenant-specific folder for unit tests
            checkCustomFolders();
        }
    }

    private void checkCustomFolders() {
        File metadataFolder = new File(getIndexFolderPath(true, METADATA_SUBFOLDER_NAME));
        if (metadataFolder.exists()) {
            ensureMetadataIndexAccess();
            File dataDictFolder = new File(getIndexFolderPath(true, DICTIONARY_SUBFOLDER_NAME));
            if (dataDictFolder.exists()) {
                ensureDocumentDictIndexAccess();
            }
            // make a copy of shared regex classifiers
            ensureRegexClassifierAccess();
        }
    }

    public String getTenantID() {
        return tenantID;
    }

    private String getIndexFolderPath(boolean isProduction, String indexName) {
        return CategoryRegistryManager.getLocalRegistryPath() + File.separator + tenantID + File.separator
                + (isProduction ? PRODUCTION_FOLDER_NAME : REPUBLISH_FOLDER_NAME) + File.separator + indexName;
    }

    /**
     * Getter for metadata
     */
    public Map<String, DQCategory> getMetadata() {
        if (metadata == null) {
            metadata = CategoryRegistryManager.getInstance().getSharedCategoryMetadata();
        }
        return metadata;
    }

    /**
     * Getter for Lucene Directory of Data Dict
     */
    public Directory getDataDictDirectory() {
        if (dataDictDirectory == null) {
            ensureDocumentDictIndexAccess();
        }
        return dataDictDirectory;
    }

    /**
     * Getter for regex classifier
     */
    public UserDefinedClassifier getRegexClassifier() {
        // return shared regexClassifier if NULL
        if (regexClassifier == null) {
            return CategoryRegistryManager.getInstance().getRegexClassifier();
        }
        return regexClassifier;
    }

    private synchronized void ensureMetadataIndexAccess() {
        if (customMetadataIndexAccess == null) {
            LOGGER.info(String.format(INITIALIZE_ACCESS, CUSTOM, METADATA_SUBFOLDER_NAME, tenantID));
            String metadataIndexPath = getIndexFolderPath(true, METADATA_SUBFOLDER_NAME);
            File folder = new File(metadataIndexPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            try {
                customMetadataIndexAccess = new CustomMetadataIndexAccess(FSDirectory.open(folder));
                metadata = customMetadataIndexAccess.readCategoryMedatada();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private synchronized void ensureDocumentDictIndexAccess() {
        ensureMetadataIndexAccess();
        if (customDocumentIndexAccess == null) {
            LOGGER.info(String.format(INITIALIZE_ACCESS, CUSTOM, DICTIONARY_SUBFOLDER_NAME, tenantID));
            String dataDictIndexPath = getIndexFolderPath(true, DICTIONARY_SUBFOLDER_NAME);
            File folder = new File(dataDictIndexPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            try {
                dataDictDirectory = FSDirectory.open(folder);
                customDocumentIndexAccess = new CustomDocumentIndexAccess(dataDictDirectory);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private synchronized void ensureRepublishDataDictIndexAccess() {
        if (customRepublishDataDictIndexAccess == null) {
            LOGGER.info(String.format(INITIALIZE_ACCESS, REPUBLISH_FOLDER_NAME, DICTIONARY_SUBFOLDER_NAME, tenantID));
            String dataDictIndexPath = getIndexFolderPath(false, DICTIONARY_SUBFOLDER_NAME);
            File folder = new File(dataDictIndexPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            try {
                customRepublishDataDictIndexAccess = new CustomDocumentIndexAccess(FSDirectory.open(folder));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private synchronized void ensureRepublishMetadataIndexAccess() {
        if (customRepublishMetadataIndexAccess == null) {

            LOGGER.info(String.format(INITIALIZE_ACCESS, REPUBLISH_FOLDER_NAME, METADATA_SUBFOLDER_NAME, tenantID));
            String dataDictIndexPath = getIndexFolderPath(false, METADATA_SUBFOLDER_NAME);
            File folder = new File(dataDictIndexPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            try {
                customRepublishMetadataIndexAccess = new CustomMetadataIndexAccess(FSDirectory.open(folder));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Create a new category in index.
     *
     * @param category
     */
    public void createCategory(DQCategory category) {
        if (CategoryType.REGEX.equals(category.getType())) {
            insertOrUpdateRegexCategory(category);
        }
        category.setModified(true);
        ensureMetadataIndexAccess();
        customMetadataIndexAccess.createCategory(category);
        customMetadataIndexAccess.commitChanges();
        metadata = customMetadataIndexAccess.readCategoryMedatada();
    }

    /**
     * Update a category in index.
     *
     * @param category
     */
    public void updateCategory(DQCategory category) {
        if (CategoryType.DICT.equals(category.getType())) {
            copyDataDictByCategoryFromSharedDirectory(category.getId());
        } else if (CategoryType.REGEX.equals(category.getType())) {
            insertOrUpdateRegexCategory(category);
        }

        category.setModified(true);
        ensureMetadataIndexAccess();
        customMetadataIndexAccess.insertOrUpdateCategory(category);
        customMetadataIndexAccess.commitChanges();
        metadata = customMetadataIndexAccess.readCategoryMedatada();
    }

    private void copyDataDictByCategoryFromSharedDirectory(String categoryId) {
        DQCategory dqCat = getMetadata().get(categoryId);
        if (dqCat != null && !Boolean.TRUE.equals(dqCat.getModified()) && !Boolean.TRUE.equals(dqCat.getDeleted())) {
            // copy all existing documents from shared directory to custom directory
            ensureDocumentDictIndexAccess();
            customDocumentIndexAccess.copyBaseDocumentsFromSharedDirectory(dqCat);
        }
    }

    /**
     * Delete a category from index.
     *
     * @param category
     */
    public void deleteCategory(DQCategory category) {
        category.setDeleted(true);
        ensureMetadataIndexAccess();
        String categoryId = category.getId();
        if (CategoryType.REGEX.equals(category.getType())) {
            ensureRegexClassifierAccess();
            customRegexClassifierAccess.deleteRegex(categoryId);
        }
        if (TALEND.equals(category.getCreator())) {
            customMetadataIndexAccess.insertOrUpdateCategory(category);
            if (!CategoryType.REGEX.equals(category.getType()) && Boolean.TRUE.equals(category.getModified())) {
                ensureDocumentDictIndexAccess();
                customDocumentIndexAccess.deleteDocumentsByCategoryId(categoryId);
                customDocumentIndexAccess.commitChanges();
            }
        } else {
            customMetadataIndexAccess.deleteCategory(category);
            ensureDocumentDictIndexAccess();
            customDocumentIndexAccess.deleteDocumentsByCategoryId(categoryId);
            customDocumentIndexAccess.commitChanges();
        }
        customMetadataIndexAccess.commitChanges();
        metadata = customMetadataIndexAccess.readCategoryMedatada();
    }

    /**
     * Relead category metadata from index.
     */
    public void reloadCategoryMetadata() {
        checkCustomFolders();
        if (customMetadataIndexAccess != null) {
            metadata = customMetadataIndexAccess.readCategoryMedatada();
        }
        if (customRegexClassifierAccess != null) {
            regexClassifier = customRegexClassifierAccess.readUserDefinedClassifier();
        }
    }

    /**
     * Update a document into Data Dict index.
     *
     * @param documents
     */
    public void updateDataDictDocuments(List<DQDocument> documents) {
        ensureDocumentDictIndexAccess();
        operationDataDictDocuments(documents, customDocumentIndexAccess::insertOrUpdateDocument);
    }

    /**
     * Add a document into Data Dict index.
     *
     * @param documents
     */
    public void addDataDictDocuments(List<DQDocument> documents) {
        ensureDocumentDictIndexAccess();
        operationDataDictDocuments(documents, customDocumentIndexAccess::createDocument);
    }

    /**
     * Delete a document into Data Dict index.
     *
     * @param documents
     */
    public void deleteDataDictDocuments(List<DQDocument> documents) {
        ensureDocumentDictIndexAccess();
        operationDataDictDocuments(documents, customDocumentIndexAccess::deleteDocument);
    }

    private void operationDataDictDocuments(List<DQDocument> documents, Consumer<List<DQDocument>> function) {
        String categoryId = documents.get(0).getCategory().getId();
        DQCategory category = getMetadata().get(categoryId);
        if (category != null && !Boolean.TRUE.equals(category.getModified()) && !Boolean.TRUE.equals(category.getDeleted()))
            updateCategory(category);
        function.accept(documents);
        customDocumentIndexAccess.commitChanges();
    }

    void closeRepublishDictionaryAccess() {
        try {
            if (customRepublishMetadataIndexAccess != null) {
                customRepublishMetadataIndexAccess.close();
            }
            if (customRepublishDataDictIndexAccess != null) {
                customRepublishDataDictIndexAccess.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            customRepublishMetadataIndexAccess = null;
            customRepublishDataDictIndexAccess = null;
            customRepublishRegexClassifierAccess = null;
        }
    }

    private synchronized void ensureRegexClassifierAccess() {
        if (customRegexClassifierAccess == null) {
            LOGGER.info(String.format(INITIALIZE_ACCESS, CUSTOM, REGEX_SUBFOLDER_NAME, tenantID));
            customRegexClassifierAccess = new CustomRegexClassifierAccess(this);
            regexClassifier = customRegexClassifierAccess.readUserDefinedClassifier();
        }
    }

    private synchronized void ensureRepublishRegexClassifierAccess() {
        if (customRepublishRegexClassifierAccess == null) {
            LOGGER.info(String.format(INITIALIZE_ACCESS, REPUBLISH_FOLDER_NAME, REGEX_SUBFOLDER_NAME, tenantID));
            customRepublishRegexClassifierAccess = new CustomRegexClassifierAccess(
                    getIndexFolderPath(false, REGEX_SUBFOLDER_NAME + File.separator + REGEX_CATEGORIZER_FILE_NAME));
        }
    }

    /**
     * Add a regex category.
     *
     * @param category
     */
    private void insertOrUpdateRegexCategory(DQCategory category) {
        ensureRegexClassifierAccess();
        UserDefinedCategory regEx = DictionaryUtils.regexClassifierfromDQCategory(category);
        customRegexClassifierAccess.insertOrUpdateRegex(regEx);
        regexClassifier = customRegexClassifierAccess.readUserDefinedClassifier();
    }

    /**
     * Get the dictionary cache for suggestion of dictionary values.
     */
    public LocalDictionaryCache getDictionaryCache() {
        if (localDictionaryCache == null) {
            localDictionaryCache = new LocalDictionaryCache(this);
        }
        return localDictionaryCache;
    }

    /**
     * Close the dictionary cache instance
     */
    public void closeDictionaryCache() {
        if (localDictionaryCache != null) {
            localDictionaryCache.close();
            localDictionaryCache = null;
        }
    }

    /**
     * List all categories.
     *
     * @return collection of category objects
     */
    public Collection<DQCategory> listCategories() {
        return listCategories(true);
    }

    /**
     * List all categories.
     *
     * @param includeOpenCategories whether include incomplete categories
     * @return collection of category objects
     */
    public Collection<DQCategory> listCategories(boolean includeOpenCategories) {
        List<DQCategory> catList = new ArrayList<>();
        for (DQCategory dqCat : getMetadata().values()) {
            if (!Boolean.TRUE.equals(dqCat.getDeleted()) && (includeOpenCategories || dqCat.getCompleteness())) {
                catList.add(dqCat);
            }
        }
        return catList;
    }

    /**
     * List all categories of a given {@link CategoryType}.
     *
     * @param type the given category type
     * @return collection of category objects of the given type
     */
    public List<DQCategory> listCategories(CategoryType type) {
        List<DQCategory> catList = new ArrayList<>();
        for (DQCategory dqCat : getMetadata().values()) {
            if (!Boolean.TRUE.equals(dqCat.getDeleted()) && type.equals(dqCat.getType())) {
                catList.add(dqCat);
            }
        }
        return catList;
    }

    /**
     * Get the category object by its technical ID.
     *
     * @param catId the technical ID of the category
     * @return the category object
     */
    public DQCategory getCategoryMetadataById(String catId) {
        return getMetadata().get(catId);
    }

    /**
     * Get the category object by its functional ID (aka. name).
     *
     * @param catName the functional ID (aka. name)
     * @return the category object
     */
    public DQCategory getCategoryMetadataByName(String catName) {
        for (DQCategory cat : getMetadata().values()) {
            if (cat.getName().equals(catName)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * Get IndexWriter for category metadata index.
     */
    public IndexWriter getMetadataIndexWriter() throws IOException {
        ensureMetadataIndexAccess();
        return customMetadataIndexAccess.getWriter();
    }

    /**
     * Get IndexWriter for data dict index.
     */
    public IndexWriter getDataDictIndexWriter() throws IOException {
        ensureDocumentDictIndexAccess();
        return customDocumentIndexAccess.getWriter();
    }

    /**
     * Things to be done before receiving the republish events.
     */
    public void beforeRepublish() {
        ensureRepublishMetadataIndexAccess();
        for (DQCategory category : CategoryRegistryManager.getInstance().getSharedCategoryMetadata().values()) {
            category.setDeleted(true);
            customRepublishMetadataIndexAccess.insertOrUpdateCategory(category);
        }
    }

    /**
     * Republish a document into Data Dict index.
     *
     * @param documents
     * @throws IOException
     */
    public void republishDataDictDocuments(List<DQDocument> documents) {
        ensureRepublishDataDictIndexAccess();
        customRepublishDataDictIndexAccess.createDocument(documents);
    }

    /**
     * republish a category
     *
     * @param category
     */
    @Deprecated
    public void republishCategory(DQCategory category) {
        republishCategories(Arrays.asList(category));
    }

    /**
     * republish categories
     *
     * @param categories
     */
    public void republishCategories(List<DQCategory> categories) {
        ensureRepublishMetadataIndexAccess();
        for (DQCategory category : categories) {
            category.setModified(true);
            if (CategoryRegistryManager.getInstance().getSharedCategoryMetadata().containsKey(category.getId())) {
                if (category.getLastModifier() == null || TALEND.equals(category.getLastModifier())) {
                    category.setModified(false);
                }
                customRepublishMetadataIndexAccess.insertOrUpdateCategory(category);
            } else
                customRepublishMetadataIndexAccess.createCategory(category);

            if (CategoryType.REGEX.equals(category.getType())) {
                republishRegexCategory(category);
            }
        }
        customRepublishMetadataIndexAccess.commitChanges();
    }

    /**
     * Add a regex category.
     *
     * @param category
     */
    private void republishRegexCategory(DQCategory category) {
        ensureRepublishRegexClassifierAccess();
        UserDefinedCategory regEx = DictionaryUtils.regexClassifierfromDQCategory(category);
        customRepublishRegexClassifierAccess.insertOrUpdateRegex(regEx);
    }

    /**
     * copy the publish directory in the production directory once the republish is finished
     *
     * @throws IOException
     */
    public synchronized void publishDirectory() throws IOException {
        closeRepublishDictionaryAccess();
        File stagingIndexes = new File(CategoryRegistryManager.getLocalRegistryPath() + File.separator + tenantID + File.separator
                + REPUBLISH_FOLDER_NAME);

        File productionIndexes = new File(CategoryRegistryManager.getLocalRegistryPath() + File.separator + tenantID
                + File.separator + PRODUCTION_FOLDER_NAME);

        File backup = new File(productionIndexes.getPath() + ".old");
        // --- Don't do anything if a backup already exists (it means that there is currently a republish working) or if nothing
        // to republish
        if (!backup.exists() && stagingIndexes.exists()) {
            if (productionIndexes.exists()) {
                try {
                    LOGGER.info("[Post Republish] backup prod");
                    FileUtils.copyDirectory(productionIndexes, backup);
                    copyStagingToProd(stagingIndexes, backup, productionIndexes);
                } catch (IOException exception) { // --- Catch the error when copying from backup to prod
                    LOGGER.error(exception.getMessage(), exception);
                } finally { // --- whatever happened just before, we want to remove the backup directory
                    LOGGER.info("[Post Republish] delete backup");
                    FileUtils.deleteDirectory(backup);
                }
            } else {
                FileUtils.copyDirectory(stagingIndexes, productionIndexes);
            }
            LOGGER.info("[Post Republish] delete staging contents");
            FileUtils.deleteDirectory(stagingIndexes);

            reloadCategoryMetadata();
        }
    }

    private void copyStagingToProd(File stagingIndexes, File backup, File productionIndexes) throws IOException {
        try {
            LOGGER.info("[Post Republish] insert staging directory into prod");
            File metadataFolder = new File(stagingIndexes.getAbsolutePath() + File.separator + METADATA_SUBFOLDER_NAME);
            if (metadataFolder.exists()) {
                ensureMetadataIndexAccess();
                customMetadataIndexAccess.copyStagingContent(metadataFolder.getAbsolutePath());
            }

            File dictionaryFolder = new File(stagingIndexes.getAbsolutePath() + File.separator + DICTIONARY_SUBFOLDER_NAME);
            if (dictionaryFolder.exists()) {
                ensureDocumentDictIndexAccess();
                customDocumentIndexAccess.copyStagingContent(dictionaryFolder.getAbsolutePath());
            }

            File regexFile = new File(stagingIndexes.getAbsolutePath() + File.separator + REGEX_SUBFOLDER_NAME + File.separator
                    + REGEX_CATEGORIZER_FILE_NAME);
            if (regexFile.exists()) {
                ensureRegexClassifierAccess();
                customRegexClassifierAccess.copyStagingContent(regexFile.getAbsolutePath());
            }
        } catch (IOException exception) { // --- Catch the error when copying from staging to prod
            LOGGER.error(exception.getMessage(), exception);
            // --- Copy the backup to prod
            FileUtils.cleanDirectory(productionIndexes);
            FileUtils.copyDirectory(backup, productionIndexes);
        }
    }

    public DictionarySnapshot getDictionarySnapshot() {
        return new DictionarySnapshot(getMetadata(), //
                new LuceneIndex(CategoryRegistryManager.getInstance().getSharedDataDictDirectory(),
                        DictionarySearchMode.MATCH_SEMANTIC_DICTIONARY), //
                new LuceneIndex(getDataDictDirectory(), DictionarySearchMode.MATCH_SEMANTIC_DICTIONARY), //
                new LuceneIndex(CategoryRegistryManager.getInstance().getSharedKeywordDirectory(),
                        DictionarySearchMode.MATCH_SEMANTIC_KEYWORD), //
                getRegexClassifier()//
        );
    }

    public void delete() {
        LOGGER.info("Delete data for tenant " + tenantID);
        File rootFolder = new File(CategoryRegistryManager.getLocalRegistryPath() + File.separator + tenantID);
        try {
            FileUtils.deleteDirectory(rootFolder);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
