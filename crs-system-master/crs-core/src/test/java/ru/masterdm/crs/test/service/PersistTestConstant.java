package ru.masterdm.crs.test.service;

/**
 * Constants for {@link EntityServicePersistTest}.
 * @author Pavel Masalov
 */
public abstract class PersistTestConstant {

    public static final String PERSIST_META_KEY = "PERSIST_TEST";
    public static final String PERSIST_TEST_CHILD_META_KEY = "PERSIST_TEST_CHILD";

    /**
     * Parent entity attribute names constants.
     */
    public static class PersistTestFields {

        public static final String STRING = "PERSIST_TEST#STRING";
        public static final String BOOLEAN = "PERSIST_TEST#BOOLEAN";
        public static final String TEXT = "PERSIST_TEST#TEXT";
        public static final String NUMBER = "PERSIST_TEST#NUMBER";
        public static final String DATE = "PERSIST_TEST#DATE";
        public static final String DATETIME = "PERSIST_TEST#DATETIME";
        public static final String FILE = "PERSIST_TEST#FILE";
        public static final String FILE2 = "PERSIST_TEST#FILE2";
        public static final String REFERENCE = "PERSIST_TEST#REFERENCE";
        public static final String REFERENCE2 = "PERSIST_TEST#REFERENCE2";
        public static final String STRINGML = "PERSIST_TEST#STRINGML";
        public static final String STRINGML2 = "PERSIST_TEST#STRINGML2";
        public static final String STRINGML3 = "PERSIST_TEST#STRINGML3";
        public static final String TEXTML = "PERSIST_TEST#TEXTML";
        public static final String TEXTML2 = "PERSIST_TEST#TEXTML2";
        public static final String TEXTML3 = "PERSIST_TEST#TEXTML3";
        public static final String WHERE = "PERSIST_TEST#WHERE";
    }

    /**
     * Child entity attribute names constants.
     */
    static class PersistChildTestFields {

        public static final String STRING = "PERSIST_TEST_CHILD#STRING";
        public static final String BOOLEAN = "PERSIST_TEST_CHILD#BOOLEAN";
        public static final String TEXT = "PERSIST_TEST_CHILD#TEXT";
        public static final String NUMBER = "PERSIST_TEST_CHILD#NUMBER";
        public static final String DATE = "PERSIST_TEST_CHILD#DATE";
        public static final String DATETIME = "PERSIST_TEST_CHILD#DATETIME";
        public static final String FILE = "PERSIST_TEST_CHILD#FILE";
        public static final String STRINGML = "PERSIST_TEST_CHILD#STRINGML";
        public static final String TEXTML = "PERSIST_TEST_CHILD#TEXTML";
    }

    /**
     * Parent entity attribute names constants.
     */
    public static class RemoveTestFields {

        public static final String STRING = "REMOVE_TEST#STRING";
        public static final String BOOLEAN = "REMOVE_TEST#BOOLEAN";
        public static final String TEXT = "REMOVE_TEST#TEXT";
        public static final String NUMBER = "REMOVE_TEST#NUMBER";
        public static final String DATE = "REMOVE_TEST#DATE";
        public static final String DATETIME = "REMOVE_TEST#DATETIME";
        public static final String FILE = "REMOVE_TEST#FILE";
        public static final String FILE2 = "REMOVE_TEST#FILE2";
        public static final String REFERENCE = "REMOVE_TEST#REFERENCE";
        public static final String REFERENCE2 = "REMOVE_TEST#REFERENCE2";
        public static final String STRINGML = "REMOVE_TEST#STRINGML";
        public static final String STRINGML2 = "REMOVE_TEST#STRINGML2";
        public static final String STRINGML3 = "REMOVE_TEST#STRINGML3";
        public static final String TEXTML = "REMOVE_TEST#TEXTML";
        public static final String TEXTML2 = "REMOVE_TEST#TEXTML2";
        public static final String TEXTML3 = "REMOVE_TEST#TEXTML3";
        public static final String WHERE = "REMOVE_TEST#WHERE";
    }

    /**
     * Child entity attribute names constants.
     */
    static class RemoveChildTestFields {

        public static final String STRING = "REMOVE_TEST_CHILD#STRING";
        public static final String BOOLEAN = "REMOVE_TEST_CHILD#BOOLEAN";
        public static final String TEXT = "REMOVE_TEST_CHILD#TEXT";
        public static final String NUMBER = "REMOVE_TEST_CHILD#NUMBER";
        public static final String DATE = "REMOVE_TEST_CHILD#DATE";
        public static final String DATETIME = "REMOVE_TEST_CHILD#DATETIME";
        public static final String FILE = "REMOVE_TEST_CHILD#FILE";
        public static final String STRINGML = "REMOVE_TEST_CHILD#STRINGML";
        public static final String TEXTML = "REMOVE_TEST_CHILD#TEXTML";
    }

    /**
     * Child entity attribute names constants.
     */
    static class ConvertTestFields {

        public static final String STRING = "CONVERTER_TEST#STRING";
        public static final String BOOLEAN = "CONVERTER_TEST#BOOLEAN";
        public static final String TEXT = "CONVERTER_TEST#TEXT";
        public static final String NUMBER = "CONVERTER_TEST#NUMBER";
        public static final String DATE = "CONVERTER_TEST#DATE";
        public static final String DATETIME = "CONVERTER_TEST#DATETIME";
    }

}
