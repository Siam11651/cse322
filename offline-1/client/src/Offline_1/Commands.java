package Offline_1;

public class Commands
{
    final public static String LOGIN = "login";
    final public static String USERS_LIST = "users-list";
    final public static String FILES_LIST = "files-list";
    final public static String FILE_REQUEST = "file-request";
    final public static String MESSAGES = "messages";
    final public static String UPLOAD = "upload";
    final public static String DOWNLOAD = "download";

    public class FilesListArguments
    {
        public static String OWN = "own";
    }

    public class FilePrivacy
    {
        public static String PUBLIC = "public";
        public static String PRIVATE = "private";
        public static String ALL = "all";
    };
}
