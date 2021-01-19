package nio;

public enum Commands {
    LS("ls"), CD("cd"), CAT("cat"), TOUCH("touch"), MKDIR("mkdir");

    private final String command;

    Commands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
